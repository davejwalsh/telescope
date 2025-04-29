package com.ska.telescopeSimulator.device.reciever;

import com.ska.telescopeSimulator.dto.PowerMeasurement;

public interface OverpoweredMeasurementListener {
    void onOverpowerMeasurementChanged(boolean isOverpowered);
}
