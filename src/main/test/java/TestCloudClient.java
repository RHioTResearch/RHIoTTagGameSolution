import java.time.Duration;

import com.eurotech.cloud.client.EdcClientException;
import org.jboss.rhiot.ble.bluez.RHIoTTag;
import org.jboss.rhiot.game.CloudClient;
import org.jboss.rhiot.game.ICloudListener;
import org.jboss.rhiot.services.fsm.GameStateMachine;

/**
 * Created by starksm on 6/10/16.
 */
public class TestCloudClient implements ICloudListener {
    private CloudClient cloudClient;

    public static void main(String[] args) throws Exception {
        TestCloudClient test = new TestCloudClient();
        test.run();
        Thread.sleep(60000);
        test.stop();
    }

    public void run() throws Exception {
        cloudClient = new CloudClient();
        cloudClient.start(this, "DN2016-GW0-client-1", "68:C9:0B:06:F3:0A");

    }
    public void stop() throws EdcClientException {
        cloudClient.stop();
    }

    @Override
    public void stateChange(GameStateMachine.GameState prevState, GameStateMachine.GameState newState, GameStateMachine.GameEvent event) {
        System.out.printf("stateChange(%s -> %s on: %s\n", prevState, newState, event);
    }

    @Override
    public void tagData(long time, double temp, RHIoTTag.KeyState keyState, int lux) {
        System.out.printf("tagData(%d; %.2f, %s, %d)\n", time, temp, keyState, lux);
    }

    @Override
    public void gameInfo(int shootingTimeLeft, int shotsLef, int gameScore, int gameTimeLeft) {
        System.out.printf("gameInfo(%d, %d, %s, %d)\n", shootingTimeLeft, shotsLef, gameScore, gameTimeLeft);
        Duration stDuration = Duration.ofMillis(shootingTimeLeft);
        long mins = stDuration.toMinutes();
        long secs = stDuration.getSeconds() - mins*60;
        System.out.printf("%02dM:%02dS\n", mins, secs);
        Duration gameDuration = Duration.ofMillis(gameTimeLeft);
        mins = gameDuration.toMinutes();
        secs = gameDuration.getSeconds() - mins*60;
        System.out.printf("%02dM:%02dS\n", mins, secs);
    }

    @Override
    public void hitDetected(int hitScore, int ringsOffCenter) {
        System.out.printf("hitDetected(%d, %d)\n", hitScore, ringsOffCenter);
    }
}
