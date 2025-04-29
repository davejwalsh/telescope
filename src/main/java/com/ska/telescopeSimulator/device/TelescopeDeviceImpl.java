package com.ska.telescopeSimulator.device;

import com.ska.telescopeSimulator.device.motor.MotorPositionListener;
import com.ska.telescopeSimulator.device.motor.MotorStateListener;
import com.ska.telescopeSimulator.device.motor.TelescopeMotorDevice;
import com.ska.telescopeSimulator.device.motor.TelescopeMotorDeviceImpl;
import com.ska.telescopeSimulator.device.reciever.*;
import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.dto.StatusCode;
import com.ska.telescopeSimulator.exception.TelescopeDeviceException;
import com.ska.telescopeSimulator.sky.SimulatedSky;
import com.ska.telescopeSimulator.states.DeviceState;
import com.ska.telescopeSimulator.dto.PowerMeasurement;
import com.ska.telescopeSimulator.states.MotorState;
import com.ska.telescopeSimulator.states.ReceiverState;
import com.ska.telescopeSimulator.states.StatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TelescopeDeviceImpl implements TelescopeDevice {


    private static final Logger LOGGER = LoggerFactory.getLogger(TelescopeDeviceImpl.class);

    private DeviceCoordinates deviceCoordinates = DeviceCoordinates.zeroed();
    private DeviceCoordinates targetCoordinates = DeviceCoordinates.zeroed();
    private DeviceState deviceState = DeviceState.OFF;

    private TelescopeMotorDevice motorDevice = new TelescopeMotorDeviceImpl();
    private TelescopeReceiverDevice receiverDevice = new TelescopeReceiverDeviceImpl();
    private MotorState motorState = MotorState.IDLE;
    private ReceiverState receiverState = ReceiverState.IDLE;
    private PowerMeasurement powerMeasurement = PowerMeasurement.noMeasurement();

    private final List<DeviceStateListener> deviceStateListeners = new ArrayList<>();

    private boolean isStarted = false;

    private Integer deviceId;
    private boolean isSelectedDevice;

    public TelescopeDeviceImpl(Integer deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void addSimulatedSky(SimulatedSky simulatedSky) {
        receiverDevice.addSimulatedSky(simulatedSky);
    }

    @Override
    public Integer getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceSelected(boolean isSelected) {
        this.isSelectedDevice = isSelected;
        motorDevice.enableListeners(isSelected);
        receiverDevice.enableListeners(isSelected);
    }

    @Override
    public DeviceCoordinates getCurrentCoordinates() {
        return motorDevice.getCurrentDeviceCoordinates();
    }

    @Override
    public DeviceCoordinates getSetTargetCoordinates() {
        return motorDevice.getTargetDeviceCoordinates();
    }

    @Override
    public void addDeviceStateListener(DeviceStateListener listener) {
        deviceStateListeners.add(listener);
    }

    @Override
    public void addMotorStateListener(MotorStateListener listener) {
        motorDevice.addMotorStateListener(listener);
    }

    @Override
    public void addMotorPositionListener(MotorPositionListener listener) {
        motorDevice.addMotorPositionListener(listener);
    }

    @Override
    public void addMotorTargetPositionListener(MotorPositionListener listener) {
        motorDevice.addMotorTargetPositionListener(listener);
    }

    @Override
    public void addReceiverStateListener(ReceiverStateListener listener) {
        receiverDevice.addStateListener(listener);
    }

    @Override
    public void addPowerMeasurementListener(PowerMeasurementListener listener) {
        receiverDevice.addPowerMeasurementListener(listener);
    }

    @Override
    public void addOverpoweredMeasurementListener(OverpoweredMeasurementListener listener) {
        receiverDevice.addOverpowerMeasurementListener(listener);
    }

    @Override
    public StatusResponse start() {
        if (!deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is already on");
        isStarted = true;
        updateDeviceState();
        notifyDeviceStateListeners();
        return new StatusResponse(StatusCode.SUCCESS, "Device started");
    }

    @Override
    public StatusResponse stop() {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        isStarted = false;
        StatusResponse motorStatus = motorDevice.stopMoving();
        StatusResponse receiverStatus = receiverDevice.stopReceiving();
        updateDeviceState();
        notifyDeviceStateListeners();
        if (motorStatus.getStatus().equals(StatusCode.FAIL)
                || motorStatus.getStatus().equals(StatusCode.ERROR)
                || receiverStatus.getStatus().equals(StatusCode.FAIL)
                || receiverStatus.getStatus().equals(StatusCode.ERROR)) {
            return new StatusResponse(StatusCode.FAIL, "Problem stopping device, motor status: " + motorStatus.getStatus().toString() + " , receiver status: " + receiverStatus.getStatus().toString());
        }
        return new StatusResponse(StatusCode.SUCCESS, "Device stopped");
    }

    @Override
    public StatusResponse pause() {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        StatusResponse motorStatus = motorDevice.pauseMoving();
        updateDeviceState();
        notifyDeviceStateListeners();
        if (motorStatus.getStatus().equals(StatusCode.FAIL)
                || motorStatus.getStatus().equals(StatusCode.ERROR)) {
            return new StatusResponse(StatusCode.FAIL, "Problem pausing device, motor status: " + motorStatus.getStatus().toString());
        }
        return new StatusResponse(StatusCode.SUCCESS, "Device paused");
    }

    @Override
    public StatusResponse startReceiving() {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        if (canStartReceiving()) {
            StatusResponse receiverState = receiverDevice.startReceiving(getCurrentCoordinates());
            updateDeviceState();
            return receiverState;
        }
        updateDeviceState();
        return new StatusResponse(StatusCode.FAIL, "Device not ready, not at correct coordinates or already moving");
    }

    @Override
    public StatusResponse stopReceiving() {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        StatusResponse receiverState = receiverDevice.stopReceiving();
        updateDeviceState();
        return receiverState;
    }

    @Override
    public StatusResponse moveToSetTarget() throws TelescopeDeviceException {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        if (canMove()) {
            if (deviceState.equals(DeviceState.READY)) {
                return motorDevice.moveToTargetCoordinates();
            }
            if (deviceState.equals(DeviceState.RECEIVING)) {
                return new StatusResponse(StatusCode.FAIL, "Already receiving data from a specified coordinate");
            }
            if (deviceState.equals(DeviceState.MOVING)) {
                return new StatusResponse(StatusCode.FAIL, "Device already moving!");
            }
        } else {
            return new StatusResponse(StatusCode.FAIL, "Can't move telescope - device not started?");
        }
        throw new TelescopeDeviceException("Error");
    }

    @Override
    public CompletableFuture<StatusResponse> reset() {
        return CompletableFuture.supplyAsync(() -> {
            if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
            try {
//                if (deviceState != DeviceState.STOPPED) {
//                    return new StatusResponse(StatusCode.FAIL, "Device currently running!");
//                }
                CompletableFuture<StatusResponse> motorResetFuture = motorDevice.reset();
                CompletableFuture<StatusResponse> receiverResetFuture = receiverDevice.reset();
                CompletableFuture<Void> combinedResetFuture = CompletableFuture.allOf(motorResetFuture, receiverResetFuture);
                combinedResetFuture.join();
                if (motorResetFuture.get().getStatus().equals(StatusCode.SUCCESS) && receiverResetFuture.get().getStatus().equals(StatusCode.SUCCESS)) {
                    deviceState = DeviceState.READY;
                    updateDeviceState();
                    notifyDeviceStateListeners();
                    return new StatusResponse(StatusCode.SUCCESS, "Device reset");
                } else {
                    return new StatusResponse(StatusCode.FAIL, "Problem stopping device, motor status: " + motorResetFuture.get().getStatus().toString() + " , receiver status: " + receiverResetFuture.get().getStatus().toString());
                }

            } catch (Exception e) {
                LOGGER.error("Error during reset: " + e.getMessage());
                return new StatusResponse(StatusCode.FAIL, "Error during reset: " + e.getMessage());
            }
        });
    }

    @Override
    public StatusResponse setTargetCoordinate(DeviceCoordinates targetCoordinates) {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        return motorDevice.setTargetCoordinates(targetCoordinates);
    }

    @Override
    public StatusResponse setMotorMovementRate(Double newRate) {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        return motorDevice.setMovementRate(newRate);
    }

    @Override
    public StatusResponse setShutterTime(Double shutterTimeInMs) {
        if (deviceState.equals(DeviceState.OFF)) return new StatusResponse(StatusCode.FAIL, "Device is not on");
        return receiverDevice.setShutterOpenTime(shutterTimeInMs);
    }

    @Override
    public PowerMeasurement getPowerMeasurement() {
        if (deviceState.equals(DeviceState.RECEIVING)) {
            return powerMeasurement;
        }
        return PowerMeasurement.noMeasurement();
    }

    @Override
    public DeviceState getDeviceState() {
        return deviceState;
    }

    @Override
    public StatusResponse induceMotorError() {
        return motorDevice.induceMotorError();
    }

    @Override
    public StatusResponse induceReceiverError() {
        return receiverDevice.induceReceiverError();
    }


    @Override
    public synchronized StatusResponse notifyAllChange() {
        updateDeviceState();
        notifyDeviceStateListeners();
        motorDevice.notifyAllChange();
        receiverDevice.notifyAllChange();
        LOGGER.info("All  listeners notified");
        return new StatusResponse(StatusCode.SUCCESS, "All  listeners notified");
    }


    public void removeListener(DeviceStateListener listener) {
        deviceStateListeners.remove(listener);
    }

    private synchronized void updateDeviceState() {
        if (isStarted) {
            if (motorState == MotorState.ERROR || receiverState == ReceiverState.ERROR) {
                deviceState = DeviceState.ERROR;
            } else if (motorState == MotorState.MOVING) {
                deviceState = DeviceState.MOVING;
            } else if (motorState == MotorState.PAUSED) {
                deviceState = DeviceState.PAUSED;
            } else if (receiverState == ReceiverState.RECEIVING) {
                deviceState = DeviceState.RECEIVING;
            } else {
                deviceState = DeviceState.READY;
            }
        } else {
            deviceState = DeviceState.OFF;
        }
    }

    private void notifyDeviceStateListeners() {
        if (isSelectedDevice) {
            for (DeviceStateListener listener : deviceStateListeners) {
                listener.onDeviceStateChanged(deviceState);
            }
        }
    }


    private boolean canStartReceiving() {
        return motorDevice.isAtTargetCoordinates() && !motorDevice.isMoving() && isStarted;
    }

    private boolean canMove() {
        return !receiverDevice.isReceiving() && isStarted;
    }

}
