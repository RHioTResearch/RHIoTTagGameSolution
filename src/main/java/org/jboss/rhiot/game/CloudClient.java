package org.jboss.rhiot.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

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
 * An implementation of the EdcCallbackHandler that registers
 * No changes should be required to this class.
 *
 * @see CodeSourceTODOs for changes that you need to make
 */
public class CloudClient implements EdcCallbackHandler {
    private static final Logger log = Logger.getLogger(CloudClient.class);

    // DO NOT CHANGE THESE. See the CodeSourceTODOs.java file
    private static final String GW_IP_BASE   = "192.168.2.";
    private static final String GW_ID        = "DN2016-GW"+ CodeSourceTODOs.MY_GW_NO;
    private static final String ACCOUNT_NAME = "Red-Hat";
    private static final String ASSET_ID     = GW_ID+"-client-"+ CodeSourceTODOs.MY_TAG_NO;
    private static final String BROKER_URL   = "mqtt://broker-Red-Hat.everyware-cloud.com:1883/";
    private static final String USERNAME     = "s-stark";
    private static final String TOPIC_ROOT = GW_ID+"/org.jboss.rhiot.services.RHIoTTagScanner";
    private static final String CONTROL_TOPIC = "org.jboss.rhiot.services.RHIoTTagScanner/control";

    private EdcCloudClient edcCloudClient;
    private ICloudListener listener;
    /** Synchornization latch to allow client startup to wait for ack of topic subscriptions */
    private CountDownLatch subConfirmLatch;

    /**
     * Generate the gateway IP based on the MY_GW_NO
     * @return the IP address for MY_GW_NO
     */
    public static String calcGWIP() {
        int subnet = 100 + CodeSourceTODOs.MY_GW_NO;
        String gwIP = GW_IP_BASE + subnet;
        return gwIP;
    }
    public static boolean isTagRegistered(String tagAddress) {
        return false;
    }

    /**
     * Do a GET REST request against the gateway RHIoTTagServlet
     * @param path - the path for the GET call
     * @return the string content returned by the call
     * @throws IOException
     */
    public static String doGet(String path) throws IOException {
        String gwIP = calcGWIP();
        URL getURL = new URL("http://"+gwIP+":8080/rhiot/"+path);
        String reply = null;
        try(InputStream is = getURL.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            reply = reader.readLine();
        }
        return reply;
    }

    public void start(ICloudListener listener, String tagAddress) throws Exception {
        start(listener, ASSET_ID, tagAddress);
    }
    public void start(ICloudListener listener, String assetID, String tagAddress) throws Exception {
        String password = System.getenv("PASSWORD");
        if(password == null) {
            if(password == null || password.length() == 0)
                throw new IllegalStateException("Failed to get cloud password from PASSWORD env or GW_IP rest call, "+calcGWIP());
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
        prof.setDisplayName("RHIoTTagGameSolution");
        prof.setModelName("JavaFX 8 Client");

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
        subConfirmLatch = new CountDownLatch(2);
        final String DATA_TOPIC = CodeSourceTODOs.getSubscriptionTopic(TOPIC_ROOT);
        log.info("Subscribe to data topics under: "+DATA_TOPIC);
        int dataSubID = edcCloudClient.subscribe(DATA_TOPIC, "#", 1);

        System.out.println("Subscribe to control topics of all assets in the account");
        int controlSubID = edcCloudClient.controlSubscribe("+", "#", 1);

        // Wait until the subscriptions have been confirmed
        subConfirmLatch.await();
        /*
        String ack = doGet("gamesm?address="+tagAddress);
        System.out.printf("Get gamesm: %s\n", ack);
        */
    }

    public void stop() throws EdcClientException {
        //
        // Stop the session and close the connection
        //
        if(edcCloudClient != null) {
            log.info("Terminating Cloud Client");
            edcCloudClient.stopSession();
            edcCloudClient.terminate();
        }
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
        log.debug("Data publish arrived on topic: " + topic + ", qos: " + qos + ", assetId: " + assetId+", metrics: "+msg.metrics());

        if(listener != null) {
            long time = msg.getTimestamp().getTime();
            if(msg.metrics().containsKey(IRHIoTTagScanner.TAG_TEMP)) {
                // Tag data information
                double temp = CodeSourceTODOs.extractTemperature(msg);
                int keys = CodeSourceTODOs.extractKeyState(msg);
                RHIoTTag.KeyState keyState = RHIoTTag.keyStateForMask(keys);
                int lux = CodeSourceTODOs.extractLuxReading(msg);
                listener.tagData(time, temp, keyState, lux);
            }
            if(msg.metrics().containsKey(IRHIoTTagScanner.TAG_GAME_SCORE)) {
                int gameScore = CodeSourceTODOs.extractGameScore(msg);
                int gameTimeLeft = CodeSourceTODOs.extractGameTimeLeft(msg);
                int shootingTimeLeft = CodeSourceTODOs.extractShootingWindowTimeLeft(msg);
                int shotsLeft = CodeSourceTODOs.extractShotsLeft(msg);
                // General game information
                listener.gameInfo(shootingTimeLeft, shotsLeft, gameScore, gameTimeLeft);
            }
            // Game state information sent only on an event
            if(msg.metrics().containsKey(IRHIoTTagScanner.TAG_EVENT)) {
                GameStateMachine.GameState newState = CodeSourceTODOs.extractState(msg);
                GameStateMachine.GameState prevState = CodeSourceTODOs.extractPrevState(msg);
                GameStateMachine.GameEvent event = CodeSourceTODOs.extractEvent(msg);
                listener.stateChange(prevState, newState, event);
            }
            // Hit information sent only when a hit is detected
            if(msg.metrics().containsKey(IRHIoTTagScanner.TAG_HIT_SCORE)) {
                int hitScore = CodeSourceTODOs.extractHitScore(msg);
                int hitRingsOffCenter = CodeSourceTODOs.extractHitDistance(msg);
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
        log.info("Publish message ID: " + messageId + " confirmed");
    }

    public void subscribed(int messageId) {
        subConfirmLatch.countDown();
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
