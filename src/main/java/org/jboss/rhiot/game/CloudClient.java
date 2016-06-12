package org.jboss.rhiot.game;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.eurotech.cloud.client.EdcCallbackHandler;
import com.eurotech.cloud.client.EdcClientException;
import com.eurotech.cloud.client.EdcClientFactory;
import com.eurotech.cloud.client.EdcCloudClient;
import com.eurotech.cloud.client.EdcConfiguration;
import com.eurotech.cloud.client.EdcConfigurationFactory;
import com.eurotech.cloud.client.EdcDeviceProfile;
import com.eurotech.cloud.client.EdcDeviceProfileFactory;
import com.eurotech.cloud.message.EdcBirthPayload;
import com.eurotech.cloud.message.EdcPayload;
import org.apache.log4j.Logger;
import org.jboss.rhiot.ble.bluez.RHIoTTag;
import org.jboss.rhiot.services.api.IRHIoTTagScanner;
import org.jboss.rhiot.services.fsm.GameStateMachine;


/**
 * Created by starksm on 6/10/16.
 */
public class CloudClient implements EdcCallbackHandler {
    private static final Logger log = Logger.getLogger(CloudClient.class);

    // CHANGE THIS; will be 0..7 based on seat number at table
    private static final int MY_TAG_NO = 0;
    // CHANGE THIS; will be 0..14 based on gateway number at table
    private static final int MY_GW_NO = 0;

    // DO NOT CHANGE THESE
    private static final String GW_IP_BASE   = "192.168.1.";
    private static final String GW_ID        = "DN2016-GW"+MY_GW_NO;
    private static final String ACCOUNT_NAME = "Red-Hat";
    private static final String ASSET_ID     = GW_ID+"-client-"+MY_TAG_NO;
    private static final String BROKER_URL   = "mqtt://broker-sandbox.everyware-cloud.com:1883";
    private static final String CLIENT_ID    = ASSET_ID;
    private static final String USERNAME     = "s-stark";
    private static final String SUBSCRIBE_TOPIC = GW_ID+"/org.jboss.rhiot.services.RHIoTTagScanner/data";

    private EdcCloudClient edcCloudClient;
    private ICloudListener listener;

    public static String calcGWIP() {
        int subnet = 100 + MY_GW_NO;
        String gwIP = GW_IP_BASE + subnet;
        //return gwIP;
        return "192.168.1.104";
    }

    public void start(ICloudListener listener) throws Exception {
        start(listener, ASSET_ID);
    }
    public void start(ICloudListener listener, String assetID) throws Exception {
        String password = System.getenv("PASSWORD");
        if(password == null) {
            // Query the GW for the password
            String gwIP = calcGWIP();
            URL getPassURL = new URL("http://"+gwIP+":8080/rhiot/cloud-password");
            try(InputStream is = getPassURL.openStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                password = reader.readLine();
            }
            if(password == null || password.length() == 0)
                throw new IllegalStateException("Failed to get cloud password from PASSWORD env or GW_IP rest call, "+gwIP);
        }

        this.listener = listener;
        //
        // Configure: create client configuration, and set its properties
        //
        EdcConfigurationFactory confFact = EdcConfigurationFactory.getInstance();
        EdcConfiguration conf = confFact.newEdcConfiguration(ACCOUNT_NAME,
                assetID,
                BROKER_URL,
                assetID,
                USERNAME,
                password);

        EdcDeviceProfileFactory profFactory = EdcDeviceProfileFactory.getInstance();
        EdcDeviceProfile prof = profFactory.newEdcDeviceProfile();
        prof.setDisplayName("sstark-gateway client test");			// friendly name for this CLIENT_ID, for display in the Cloud
        prof.setModelName("Eclipse Java Client");

        //set GPS position in device profile - this is sent only once, with the birth certificate
        prof.setLongitude(-122.4194);
        prof.setLatitude(37.7749);

        //
        // Connect and start the session
        //
        edcCloudClient = EdcClientFactory.newInstance(conf, prof, this);
        edcCloudClient.startSession();
        log.info("Session started");

        //
        // Subscribe

        log.info("Subscribe to data topics under: "+SUBSCRIBE_TOPIC);
        edcCloudClient.subscribe(SUBSCRIBE_TOPIC, "+", 1);

        System.out.println("Subscribe to control topics of all assets in the account");
        edcCloudClient.controlSubscribe("+", "#", 1);
    }


    public void stop() throws EdcClientException {
        //
        // Stop the session and close the connection
        //
        edcCloudClient.stopSession();
        edcCloudClient.terminate();
        log.info("Terminating EDC Cloud Client");
    }

    // -----------------------------------------------------------------------
    //
    //    MQTT Callback methods
    //
    // -----------------------------------------------------------------------

    //display control messages received from broker
    public void controlArrived(String assetId, String topic, EdcPayload msg, int qos, boolean retain)
    {
        log.info("Control publish arrived on semantic topic: " + topic + " , qos: " + qos);
        // Print all the metrics
        for (String name : msg.metricNames()) {
            System.out.println(name + ":" + msg.getMetric(name));
        }

        if (topic.contains("BC")) {
            EdcBirthPayload edcBirthMessage;
            try {
                edcBirthMessage = new EdcBirthPayload(msg);
                System.out.println("Birth certificate arrived: " + edcBirthMessage.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //display data messages received from broker
    public void publishArrived(String assetId, String topic, EdcPayload msg, int qos, boolean retain) {
        log.debug("Data publish arrived on semantic topic: " + topic + ", qos: " + qos + ", assetId: " + assetId);

        if(listener != null) {
            long time = msg.getTimestamp().getTime();
            double temp = (double) msg.getMetric(IRHIoTTagScanner.TAG_TEMP);
            int keys = (int) msg.getMetric(IRHIoTTagScanner.TAG_KEYS);
            RHIoTTag.KeyState keyState = RHIoTTag.keyStateForMask(keys);
            int lux = (int) msg.getMetric(IRHIoTTagScanner.TAG_LUX);
            int gameScore = (int) msg.getMetric(IRHIoTTagScanner.TAG_GAME_SCORE);
            int gameTimeLeft = (int) msg.getMetric(IRHIoTTagScanner.TAG_GAME_TIME_LEFT);
            int shootingTimeLeft = (int) msg.getMetric(IRHIoTTagScanner.TAG_SHOOTING_TIME_LEFT);
            int shotsLef = (int) msg.getMetric(IRHIoTTagScanner.TAG_SHOTS_LEFT);
            // Tag data and general game information sent everytime
            listener.tagData(time, temp, keyState, lux);
            listener.gameInfo(shootingTimeLeft, shotsLef, gameScore, gameTimeLeft);
            // Game state information sent only on an event
            if(msg.metrics().containsKey(IRHIoTTagScanner.TAG_EVENT)) {
                String name = msg.getMetric(IRHIoTTagScanner.TAG_NEW_STATE).toString();
                GameStateMachine.GameState newState = GameStateMachine.GameState.valueOf(name);
                name = msg.getMetric(IRHIoTTagScanner.TAG_PREV_STATE).toString();
                GameStateMachine.GameState prevState = GameStateMachine.GameState.valueOf(name);
                name = msg.getMetric(IRHIoTTagScanner.TAG_EVENT).toString();
                GameStateMachine.GameEvent event = GameStateMachine.GameEvent.valueOf(name);
                listener.stateChange(prevState, newState, event);
            }
            // Hit information sent only when a hit is detected
            if(msg.metrics().containsKey(IRHIoTTagScanner.TAG_HIT_SCORE)) {
                int hitScore = (int) msg.getMetric(IRHIoTTagScanner.TAG_HIT_SCORE);
                int hitRingsOffCenter = (int) msg.getMetric(IRHIoTTagScanner.TAG_HIT_RINGS_OFF_CENTER);
                listener.hitDetected(hitScore, hitRingsOffCenter);
            }
        }
    }

    public void connectionLost() {
        log.warn("EDC client connection lost");
    }

    public void connectionRestored() {
        log.warn("EDC client reconnected");
    }

    public void published(int messageId) {
        log.debug("Publish message ID: " + messageId + " confirmed");
    }

    public void subscribed(int messageId) {
        log.info("Subscribe message ID: " + messageId + " confirmed");
    }

    public void unsubscribed(int messageId) {
        log.info("Unsubscribe message ID: " + messageId + " confirmed");
    }

    public void controlArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
        log.info("controlArrived, assetId: "+assetId+", topic: "+topic);
    }

    public void publishArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
        log.debug("publishArrived, assetId: "+assetId+", topic: "+topic);
    }

}
