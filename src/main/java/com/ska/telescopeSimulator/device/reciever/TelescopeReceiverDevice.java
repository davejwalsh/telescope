package com.ska.telescopeSimulator.device.reciever;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.sky.SimulatedSky;
import com.ska.telescopeSimulator.states.StatusResponse;

import java.util.concurrent.CompletableFuture;

public interface TelescopeReceiverDevice {


    void addStateListener(ReceiverStateListener listener);

    void removeStateListener(ReceiverStateListener listener);

    void addPowerMeasurementListener(PowerMeasurementListener listener);

    void addOverpowerMeasurementListener(OverpoweredMeasurementListener listener);

    void removePowerListener(PowerMeasurementListener listener);

    StatusResponse setShutterOpenTime(Double milliseconds);

    StatusResponse startReceiving(DeviceCoordinates coordinates);

    StatusResponse stopReceiving();

    boolean isReceiving();

    CompletableFuture<StatusResponse> reset();

    void addSimulatedSky(SimulatedSky simulatedSky);

    StatusResponse induceReceiverError();

    boolean overPower();

    StatusResponse notifyAllChange();

    void enableListeners(boolean isSelected);
}
