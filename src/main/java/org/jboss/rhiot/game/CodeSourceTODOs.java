package org.jboss.rhiot.game;

import com.eurotech.cloud.message.EdcPayload;
import org.jboss.rhiot.services.api.IRHIoTTagScanner;
import org.jboss.rhiot.services.fsm.GameStateMachine;

/**
 * This is the only file you should need to edit.
 *
 * 1. Assign your MY_TAG_NO value to your seat number to ensure you have a unique client id for connecting to the cloud broker
 * @see #MY_TAG_NO
 *
 * 2. Assign your MY_GW_NO value to the gateway number at your table. This ensures you are filtering your cloud broker
 * subscription to messages published by your table gateway. You will receive no messages if this is not set correctly.
 * @see #MY_GW_NO
 *
 * 3. Assign your MY_TAG_ADDRESS to the BLE address printed on you RHIoTTag. This is required in order for the gateway at
 * your table to accept the advertising events from your RHIoTTag for both publishing to the cloud broker as well as
 * running the game state machine.
 * @see #MY_TAG_ADDRESS
 *
 * 4. Complete the first set of methods that extract the metrics for the tag data. This information provides feedback on the
 * button press states of the tag as well as the light sensor reading.
 * @see #extractTemperature(EdcPayload)
 * @see #extractKeyState(EdcPayload)
 * @see #extractLuxReading(EdcPayload)
 *
 * 5. Complete the second set of methods that extract the metrics for the tag game state information. This information provides
 * feeback about the game state machine and is only sent when there is a state change.
 * @see #extractEvent(EdcPayload)
 * @see #extractPrevState(EdcPayload)
 * @see #extractState(EdcPayload)
 *
 * 6. Complete the third set of methods that extract the game run information. This information tells you how many shots
 * are left, how much time is left in the shooting window and the game, as well as the score.
 * @see #extractGameScore(EdcPayload)
 * @see #extractGameTimeLeft(EdcPayload)
 * @see #extractShootingWindowTimeLeft(EdcPayload)
 * @see #extractShotsLeft(EdcPayload)
 */
public class CodeSourceTODOs {
    // TODO: CHANGE THIS; will be 0..7 based on seat number at table
    public static final int MY_TAG_NO = 0;

    // TODO: CHANGE THIS; will be 0..14 based on gateway number at table
    public static final int MY_GW_NO = 0;

    // TODO: CHANGE THIS; this should be set to the BLE address string printed on your RHIoTTag
    public static final String MY_TAG_ADDRESS = "F0:B4:48:D6:DA:E0";

    /** Set from the first argument to app command line. Not needed for lab. */
    public static String MY_TAG_ADDRESS_OVERRIDE;

    /**
     * TODO: Change this method to return the name of the topic the client will subscribe to in order to received messages
     * about the data and game information associated with your tag.
     * @return topic name to select your tag's messages
     * @param topicRoot
     */
    public static String getSubscriptionTopic(String topicRoot) {
        String topic = topicRoot + "/data/"+getMyTagAddress();
        return topic;
    }

// Set 1, the tag data sent on every advertisement event
    /**
     * TODO: extract the tag temperature from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_TEMP
     * @return temperature
     */
    public static double extractTemperature(EdcPayload msg) {
        double tempC = (double) msg.getMetric(IRHIoTTagScanner.TAG_TEMP);
        return tempC;
    }

    /**
     * TODO: extract the tag key state from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_KEYS
     * @return key state
     */
    public static int extractKeyState(EdcPayload msg) {
        int keyState = (int) msg.getMetric(IRHIoTTagScanner.TAG_KEYS);
        return keyState;
    }

    /**
     * TODO: extract the tag light sensor reading from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_LUX
     * @return lux
     */
    public static int extractLuxReading(EdcPayload msg) {
        int lux = (int) msg.getMetric(IRHIoTTagScanner.TAG_LUX);
        return lux;
    }

// Set 2, the game state information sent on an event such as a key press or state timeout
    /**
     * TODO: extract the previous game state from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_PREV_STATE
     * @return game previous state
     */
    public static GameStateMachine.GameState extractPrevState(EdcPayload msg) {
        String name = (String) msg.getMetric(IRHIoTTagScanner.TAG_PREV_STATE);
        GameStateMachine.GameState prevState = GameStateMachine.GameState.valueOf(name);
        return prevState;
    }

    /**
     * TODO: extract the game new state from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_NEW_STATE
     * @return game new state
     */
    public static GameStateMachine.GameState extractState(EdcPayload msg) {
        String name = (String) msg.getMetric(IRHIoTTagScanner.TAG_NEW_STATE);
        GameStateMachine.GameState newState = GameStateMachine.GameState.valueOf(name);
        return newState;
    }

    /**
     * TODO: extract the game event from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_EVENT
     * @return game event causing state transition
     */
    public static GameStateMachine.GameEvent extractEvent(EdcPayload msg) {
        String name = (String) msg.getMetric(IRHIoTTagScanner.TAG_EVENT);
        GameStateMachine.GameEvent event = GameStateMachine.GameEvent.valueOf(name);
        return event;
    }

// Set 3, the game progress information sent while the game is active.
    /**
     * TODO: extract the time left in game from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_GAME_TIME_LEFT
     * @return time remaining till end of game
     */
    public static int extractGameTimeLeft(EdcPayload msg) {
        int timeLeft = (int) msg.getMetric(IRHIoTTagScanner.TAG_GAME_TIME_LEFT);
        return timeLeft;
    }

    /**
     * TODO: extract the time left in shooting window from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_SHOOTING_TIME_LEFT
     * @return time remaining in shooting window
     */
    public static int extractShootingWindowTimeLeft(EdcPayload msg) {
        int timeLeft = (int) msg.getMetric(IRHIoTTagScanner.TAG_SHOOTING_TIME_LEFT);
        return timeLeft;
    }

    /**
     * TODO: extract the shots left in clip from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_SHOTS_LEFT
     * @return shots left in clip
     */
    public static int extractShotsLeft(EdcPayload msg) {
        int shots = (int) msg.getMetric(IRHIoTTagScanner.TAG_SHOTS_LEFT);
        return shots;
    }

    /**
     * TODO: extract the cummulative game score from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_GAME_SCORE
     * @return cummulative game score
     */
    public static int extractGameScore(EdcPayload msg) {
        int score = (int) msg.getMetric(IRHIoTTagScanner.TAG_GAME_SCORE);
        return score;
    }

// Set 4, the information about a hit on the light sensor sent when a sensor reading above a threshold value is detected
    /**
     * TODO: extract the score of the hit from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_HIT_SCORE
     * @return score of last hit
     */
    public static int extractHitScore(EdcPayload msg) {
        int score = (int) msg.getMetric(IRHIoTTagScanner.TAG_HIT_SCORE);
        return score;
    }
    /**
     * TODO: extract the number rings off center for hit from the msg
     * @param msg
     * @see IRHIoTTagScanner#TAG_HIT_RINGS_OFF_CENTER
     * @return number rings off center for hit
     */
    public static int extractHitDistance(EdcPayload msg) {
        int rings = (int) msg.getMetric(IRHIoTTagScanner.TAG_HIT_RINGS_OFF_CENTER);
        return rings;
    }

    /**
     * A helper method that first looks to the MY_TAG_ADDRESS_OVERRIDE variable which is set from the command line, and
     * then to MY_TAG_ADDRESS. This allows multiple games to be run with alternative tags.
     * @see #MY_TAG_ADDRESS_OVERRIDE
     * @see #MY_TAG_ADDRESS
     * @return
     */
    public static String getMyTagAddress() {
        if(MY_TAG_ADDRESS_OVERRIDE != null)
            return MY_TAG_ADDRESS_OVERRIDE;
        return MY_TAG_ADDRESS;
    }

}
