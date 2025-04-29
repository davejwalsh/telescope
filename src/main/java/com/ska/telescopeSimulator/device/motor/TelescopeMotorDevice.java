package com.ska.telescopeSimulator.device.motor;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.states.StatusResponse;

import java.util.concurrent.CompletableFuture;

public interface TelescopeMotorDevice {
    StatusResponse setTargetCoordinates(DeviceCoordinates targetCoordinates);

    StatusResponse setMovementRate(Double rateInDegPerSecond);

    StatusResponse moveToTargetCoordinates();

    StatusResponse stopMoving();

    StatusResponse pauseMoving();

    CompletableFuture<StatusResponse> reset();

    void addMotorStateListener(MotorStateListener listener);

    void removeMotorStateListener(MotorStateListener listener);

    void addMotorPositionListener(MotorPositionListener listener);

    void removeMotorPositionListener(MotorPositionListener listener);

    void addMotorTargetPositionListener(MotorPositionListener listener);

    void removeMotorTargetPositionListener(MotorPositionListener listener);

    DeviceCoordinates getCurrentDeviceCoordinates();

    DeviceCoordinates getTargetDeviceCoordinates();

    boolean isAtTargetCoordinates();

    boolean isMoving();

    StatusResponse induceMotorError();

    StatusResponse notifyAllChange();

    void enableListeners(boolean isSelected);
}
