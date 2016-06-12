package org.jboss.rhiot.game;

import org.jboss.rhiot.ble.bluez.RHIoTTag;
import org.jboss.rhiot.services.fsm.GameStateMachine;

/**
 * Created by starksm on 6/10/16.
 */
public interface ICloudListener {
    public void stateChange(GameStateMachine.GameState prevState, GameStateMachine.GameState newState, GameStateMachine.GameEvent event);
    public void gameInfo(int shootingTimeLeft, int shotsLeft, int gameScore, int gameTimeLeft);
    public void hitDetected(int hitScore, int ringsOffCenter);
    public void tagData(long time, double temp, RHIoTTag.KeyState keyState, int lux);
}
