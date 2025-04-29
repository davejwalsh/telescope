package com.ska.telescopeSimulator.device;

import com.ska.telescopeSimulator.states.DeviceState;

public interface DeviceStateListener {
    void onDeviceStateChanged(DeviceState newState);
}
