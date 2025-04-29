package com.ska.telescopeSimulator.device;

import com.ska.telescopeSimulator.device.motor.MotorPositionListener;
import com.ska.telescopeSimulator.device.motor.MotorStateListener;
import com.ska.telescopeSimulator.device.reciever.OverpoweredMeasurementListener;
import com.ska.telescopeSimulator.device.reciever.PowerMeasurementListener;
import com.ska.telescopeSimulator.device.reciever.ReceiverStateListener;
import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.exception.TelescopeDeviceException;
import com.ska.telescopeSimulator.sky.SimulatedSky;
import com.ska.telescopeSimulator.states.DeviceState;
import com.ska.telescopeSimulator.dto.PowerMeasurement;
import com.ska.telescopeSimulator.states.StatusResponse;

import java.util.concurrent.CompletableFuture;

public interface TelescopeDevice {

    DeviceCoordinates getSetTargetCoordinates();

    void addDeviceStateListener(DeviceStateListener listener);

    void addMotorStateListener(MotorStateListener listener);

    void addMotorPositionListener(MotorPositionListener listener);

    void addMotorTargetPositionListener(MotorPositionListener listener);

    void addReceiverStateListener(ReceiverStateListener listener);

    void addPowerMeasurementListener(PowerMeasurementListener listener);

    void addOverpoweredMeasurementListener(OverpoweredMeasurementListener listener);

    public StatusResponse start();

    public StatusResponse stop();

    public StatusResponse pause();

    StatusResponse startReceiving();

    StatusResponse stopReceiving();

    StatusResponse moveToSetTarget() throws TelescopeDeviceException;

    public CompletableFuture<StatusResponse> reset();

    public StatusResponse setTargetCoordinate(DeviceCoordinates deviceCoordinates);

    public StatusResponse setMotorMovementRate(Double newRate);

    public StatusResponse setShutterTime(Double shutterTimeInMs);

    public PowerMeasurement getPowerMeasurement();

    void setDeviceSelected(boolean isSelected);

    public DeviceCoordinates getCurrentCoordinates();

    public DeviceState getDeviceState();

    public void addSimulatedSky(SimulatedSky simulatedSky);

    public Integer getDeviceId();

    StatusResponse induceMotorError();

    StatusResponse induceReceiverError();

    StatusResponse notifyAllChange();
}
