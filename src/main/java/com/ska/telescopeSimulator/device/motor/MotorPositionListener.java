package com.ska.telescopeSimulator.device.motor;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;

public interface MotorPositionListener {
    void onMotorPositionChanged(DeviceCoordinates newState);
}
