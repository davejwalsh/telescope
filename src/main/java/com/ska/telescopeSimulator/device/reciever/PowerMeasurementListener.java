package com.ska.telescopeSimulator.device.reciever;

import com.ska.telescopeSimulator.dto.PowerMeasurement;

public interface PowerMeasurementListener {
    void onPowerMeasurementChanged(PowerMeasurement powerMeasurement);
}
