package com.ska.telescopeSimulator.device.motor;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.dto.StatusCode;
import com.ska.telescopeSimulator.exception.TelescopeDeviceException;
import com.ska.telescopeSimulator.states.StatusResponse;
import com.ska.telescopeSimulator.states.MotorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.*;

public class TelescopeMotorDeviceImpl implements TelescopeMotorDevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelescopeMotorDeviceImpl.class);

    private long scheduledRate = 1000L;

    private final ArrayList<MotorStateListener> motorStateListeners = new ArrayList<>();
    private final ArrayList<MotorPositionListener> motorPositionListeners = new ArrayList<>();
    private final ArrayList<MotorPositionListener> motorTargetPositionListeners = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> movementTask;

    private DeviceCoordinates targetCoordinates = DeviceCoordinates.zeroed();
    private DeviceCoordinates currentDeviceCoordinates = DeviceCoordinates.zeroed();

    private MotorState motorState = MotorState.IDLE;
    private Double movementRate = 1.0;
    private Double maxMovementRate = 25.0;

    private boolean isMoving = false;
    private boolean listenersEnabled = false;

    @Override
    public StatusResponse setTargetCoordinates(DeviceCoordinates targetCoordinates) {
        this.targetCoordinates = targetCoordinates;
        notifyMotorTargetPositionChanged(targetCoordinates);
        LOGGER.info("Target co-ordinates set");
        return new StatusResponse(StatusCode.SUCCESS, "Device target coordinates set to " + targetCoordinates.toString());
    }

    @Override
    public StatusResponse setMovementRate(Double rateInDegPerSecond) {
        if (rateInDegPerSecond <= 0.0 || rateInDegPerSecond > maxMovementRate) {
            LOGGER.error("Out of rate limits!");
            return new StatusResponse(StatusCode.FAIL, "Unable to set movement rate, Out of rate limits! Max: " + maxMovementRate);
        }
        this.movementRate = rateInDegPerSecond;
        LOGGER.info("Movement Rate Set");
        return new StatusResponse(StatusCode.SUCCESS, "Movement Rate Set");
    }

    @Override
    public StatusResponse moveToTargetCoordinates() {
        if (movementTask != null && !movementTask.isDone()) {
            movementTask.cancel(true);
        }
        movementTask = scheduler.scheduleAtFixedRate(this::performMovementTask, 0, scheduledRate, TimeUnit.MILLISECONDS);
        isMoving = true;
        LOGGER.info("Movement started");
        return new StatusResponse(StatusCode.SUCCESS, "Movement Started");
    }

    @Override
    public StatusResponse stopMoving() {
        if (movementTask != null && !movementTask.isDone()) {
            movementTask.cancel(true);
        }
        isMoving = false;
        setMotorState(MotorState.IDLE);
        LOGGER.info("Movement stopped");
        return new StatusResponse(StatusCode.SUCCESS, "Movement Stopped");
    }

    @Override
    public StatusResponse pauseMoving() {
        isMoving = false;
        movementTask.cancel(true);
        setMotorState(MotorState.PAUSED);
        LOGGER.info("Movement paused");
        return new StatusResponse(StatusCode.SUCCESS, "Movement Paused");
    }

    @Override
    public CompletableFuture<StatusResponse> reset() {
        return CompletableFuture.supplyAsync(() -> {
            setMotorState(MotorState.IDLE);
            try {
                if (isMoving) {
                    stopMoving();
                    isMoving = false;
//                    return new StatusResponse(StatusCode.ERROR, "Motor has not stopped moving, cannot reset!");
                }
                targetCoordinates = DeviceCoordinates.zeroed();
                setTargetCoordinates(targetCoordinates);
                moveToTargetCoordinates();
                while (isMoving) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return new StatusResponse(StatusCode.ERROR, "Reset interrupted");
                    }
                }
                setMotorState(MotorState.IDLE);
                LOGGER.info("Motor reset completed and moved to initial position.");
                return new StatusResponse(StatusCode.SUCCESS, "Motor reset completed");
            } catch (TelescopeDeviceException e) {
                LOGGER.error("Error during reset: " + e.getMessage());
                return new StatusResponse(StatusCode.ERROR, "Error during reset: " + e.getMessage());
            }
        });
    }

    @Override
    public void addMotorStateListener(MotorStateListener listener) {
        motorStateListeners.add(listener);
    }

    @Override
    public void removeMotorStateListener(MotorStateListener listener) {
        motorStateListeners.remove(listener);
    }

    @Override
    public void addMotorPositionListener(MotorPositionListener listener) {
        motorPositionListeners.add(listener);
    }

    @Override
    public void removeMotorPositionListener(MotorPositionListener listener) {
        motorPositionListeners.remove(listener);
    }

    @Override
    public void addMotorTargetPositionListener(MotorPositionListener listener) {
        motorTargetPositionListeners.add(listener);
    }

    @Override
    public void removeMotorTargetPositionListener(MotorPositionListener listener) {
        motorTargetPositionListeners.remove(listener);
    }

    @Override
    public DeviceCoordinates getCurrentDeviceCoordinates() {
        return currentDeviceCoordinates;
    }

    @Override
    public DeviceCoordinates getTargetDeviceCoordinates() {
        return targetCoordinates;
    }

    @Override
    public boolean isAtTargetCoordinates() {
        return currentDeviceCoordinates.equals(targetCoordinates);
    }

    @Override
    public boolean isMoving(){
        return isMoving;
    }

    @Override
    public void enableListeners(boolean isSelected) {
        this.listenersEnabled = isSelected;
    }

    /***
     *
     *  induceMotorError()
     *
     *  Induces an error that will stop the performTask event loop and force a recoverable error
     *
     *
     * @return
     */
    @Override
    public StatusResponse induceMotorError() {
//        stopMoving();
        motorState = MotorState.ERROR;
        setMotorState(motorState);
        return new StatusResponse(StatusCode.ERROR, "Induced Motor Error!");
    }

    /***
     *
     *  notifyAllChange()
     *
     *  Updates all listeners, called to update the GUI when the currently selected device is changed
     *
     * @return
     */
    @Override
    public synchronized StatusResponse notifyAllChange(){
        if (listenersEnabled) {
            for (MotorPositionListener listener : motorPositionListeners) {
                listener.onMotorPositionChanged(getCurrentDeviceCoordinates());
            }
            for (MotorStateListener listener : motorStateListeners) {
                listener.onMotorStateChanged(motorState);
            }
            for (MotorPositionListener listener : motorTargetPositionListeners) {
                listener.onMotorPositionChanged(targetCoordinates);
            }
            LOGGER.info("All motor listeners notified");
            return new StatusResponse(StatusCode.SUCCESS, "All motor listeners notified");
        }
        return new StatusResponse(StatusCode.SUCCESS,"No listeners notified");
    }




    /***
     *
     *  Main event loop to perform the motor movement task
     *
     */
    private void performMovementTask() {
        if (motorState.equals(MotorState.ERROR)) {
            if (movementTask != null) {
                movementTask.cancel(true);
            }
//            notifyMotorPositionChanged(currentDeviceCoordinates);
            notifyMotorStateChanged(motorState);
            LOGGER.error("Error when moving the motor! ");
            isMoving = false;
            return;
        }
        double distance = currentDeviceCoordinates.distanceTo(targetCoordinates);
        if (distance == 0.0) {
            setMotorState(MotorState.IDLE);
            if (movementTask != null) {
                movementTask.cancel(false);
            }
            notifyMotorPositionChanged(currentDeviceCoordinates);
            LOGGER.info("Arrived at target. MotorState: " + motorState);
            isMoving = false;
            return;
        }
        setMotorState(MotorState.MOVING);
        LOGGER.info("Current position: " + currentDeviceCoordinates.getX() + ", " + currentDeviceCoordinates.getY() + ", " + currentDeviceCoordinates.getZ());
        double moveFraction = movementRate / distance;
        if (moveFraction > 1.0) moveFraction = 1.0;
        double newX = currentDeviceCoordinates.getX() + (targetCoordinates.getX() - currentDeviceCoordinates.getX()) * moveFraction;
        double newY = currentDeviceCoordinates.getY() + (targetCoordinates.getY() - currentDeviceCoordinates.getY()) * moveFraction;
        double newZ = currentDeviceCoordinates.getZ() + (targetCoordinates.getZ() - currentDeviceCoordinates.getZ()) * moveFraction;
        currentDeviceCoordinates = new DeviceCoordinates(newX, newY, newZ);
        notifyMotorPositionChanged(currentDeviceCoordinates);

    }

    private void notifyMotorStateChanged(MotorState newState) {
        for (MotorStateListener listener : motorStateListeners) {
            listener.onMotorStateChanged(newState);
        }
    }

    private void notifyMotorPositionChanged(DeviceCoordinates newPosition) {
        if (listenersEnabled) {
            for (MotorPositionListener listener : motorPositionListeners) {
                listener.onMotorPositionChanged(newPosition);
            }
        }
    }

    private void notifyMotorTargetPositionChanged(DeviceCoordinates newPosition) {
        if (listenersEnabled) {
            for (MotorPositionListener listener : motorTargetPositionListeners) {
                listener.onMotorPositionChanged(newPosition);
            }
        }
    }

    private void setMotorState(MotorState newState) {
        if (listenersEnabled) {
            if (this.motorState != newState) {
                this.motorState = newState;
                notifyMotorStateChanged(newState);
            }
        }
    }


}
