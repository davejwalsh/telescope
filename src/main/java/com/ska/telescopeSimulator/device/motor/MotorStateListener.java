package com.ska.telescopeSimulator.device.motor;

import com.ska.telescopeSimulator.states.MotorState;

public interface MotorStateListener {
    void onMotorStateChanged(MotorState newState);
}