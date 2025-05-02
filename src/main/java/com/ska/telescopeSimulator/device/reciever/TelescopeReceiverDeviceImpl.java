package com.ska.telescopeSimulator.device.reciever;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.dto.PowerMeasurement;
import com.ska.telescopeSimulator.dto.StatusCode;
import com.ska.telescopeSimulator.sky.SimulatedSky;
import com.ska.telescopeSimulator.states.StatusResponse;
import com.ska.telescopeSimulator.states.ReceiverState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.*;

public class TelescopeReceiverDeviceImpl implements TelescopeReceiverDevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelescopeReceiverDeviceImpl.class);
    private static final Double RECEIVED_POWER_LIMIT = 30.0;//70.0;

    private ReceiverState receiverState = ReceiverState.IDLE;
    private final ArrayList<ReceiverStateListener> receiverStateListeners = new ArrayList<>();
    private final ArrayList<PowerMeasurementListener> powerListeners = new ArrayList<>();
    private final ArrayList<OverpoweredMeasurementListener> overpowerListeners = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> receivingTask;

    private Double receivingRate = 1.0;
    private long scheduledRate = 1000L;
    private PowerMeasurement currentReceivedPower = PowerMeasurement.noMeasurement();

    private DeviceCoordinates currentCoordinates = DeviceCoordinates.zeroed();


    private SimulatedSky simulatedSky;
    private Double shutterOpenTime = -1.0;
    private Double currentTaskRunningFor = 0.0;
    private boolean isOverpowered = false;
    private boolean listenersEnabled = false;

    public TelescopeReceiverDeviceImpl() {
        /*NOOP*/
    }

    @Override
    public void addSimulatedSky(SimulatedSky simulatedSky) {
        this.simulatedSky = simulatedSky;
    }


    @Override
    public StatusResponse setShutterOpenTime(Double milliseconds) {
        this.shutterOpenTime = milliseconds;
        return new StatusResponse(StatusCode.SUCCESS, "Shutter open time set to " + milliseconds.toString());
    }

    @Override
    public StatusResponse startReceiving(DeviceCoordinates coordinates) {
        currentCoordinates = coordinates;
        if (canReceive()) {
            if (receivingTask != null && !receivingTask.isDone()) {
                receivingTask.cancel(true);
            }
            receiverState = ReceiverState.RECEIVING;
            receivingTask = scheduler.scheduleAtFixedRate(this::performReceivingTask, 0, scheduledRate, TimeUnit.MILLISECONDS);
            LOGGER.info("Started receiving telescope telemetry");
            return new StatusResponse(StatusCode.SUCCESS, "Started receiving telescope telemetry");
        }
        return new StatusResponse(StatusCode.ERROR, "Telescope not Idle!");
    }

    @Override
    public StatusResponse stopReceiving() {
        if (shutterOpenTime != -1.0) {
            if (currentTaskRunningFor < shutterOpenTime) {
                return new StatusResponse(StatusCode.FAIL, "Shutter time set, " + (shutterOpenTime - currentTaskRunningFor) + "s left before telescope can be moved");
            }
        }
        if (receivingTask != null && !receivingTask.isDone()) {
            receivingTask.cancel(true);
        }
        currentTaskRunningFor = 0.0;
        LOGGER.info("Stopped receiving telescope telemetry");
        receiverState = ReceiverState.IDLE;
        notifyReceiverStateChanged();
        return new StatusResponse(StatusCode.SUCCESS, "Stopped receiving telescope telemetry");
    }

    @Override
    public boolean isReceiving() {
        return receiverState.equals(ReceiverState.RECEIVING);
    }

    @Override
    public CompletableFuture<StatusResponse> reset() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (receivingTask != null && !receivingTask.isDone()) {
                    receivingTask.cancel(true); // Cancel any ongoing receiving task
                }
                isOverpowered = false;
                this.currentCoordinates = DeviceCoordinates.zeroed();
                receiverState = ReceiverState.IDLE;
                currentTaskRunningFor = 0.0;
                notifyOverpowerChanged();
                notifyReceiverStateChanged();
                LOGGER.info("Reset telescope receiver");
                return new StatusResponse(StatusCode.SUCCESS, "Reset telescope receiver");
            } catch (Exception e) {
                LOGGER.error("Error during receiver reset: " + e.getMessage());
                return new StatusResponse(StatusCode.ERROR, "Error during receiver reset: " + e.getMessage());
            }
        });
    }

    @Override
    public void addStateListener(ReceiverStateListener listener) {
        receiverStateListeners.add(listener);
    }

    @Override
    public void removeStateListener(ReceiverStateListener listener) {
        receiverStateListeners.remove(listener);
    }

    @Override
    public void addPowerMeasurementListener(PowerMeasurementListener listener) {
        powerListeners.add(listener);
    }

    @Override
    public void addOverpowerMeasurementListener(OverpoweredMeasurementListener listener) {
        overpowerListeners.add(listener);
    }

    @Override
    public void removePowerListener(PowerMeasurementListener listener) {
        powerListeners.remove(listener);
    }



    /***
     *
     *  induceReceiverError()
     *
     *  Induces an error that will stop the performTask event loop and force a recoverable error
     *
     *
     * @return
     */
    @Override
    public StatusResponse induceReceiverError() {
        receiverState = ReceiverState.ERROR;
        notifyReceiverStateChanged();
        return new StatusResponse(StatusCode.ERROR, "Induced Receiver Error!");
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
    public StatusResponse notifyAllChange(){
        if (listenersEnabled) {
            for (PowerMeasurementListener listener : powerListeners) {
                listener.onPowerMeasurementChanged(currentReceivedPower);
            }
            for (ReceiverStateListener listener : receiverStateListeners) {
                listener.onReceiverStateChanged(receiverState);
            }
            for (OverpoweredMeasurementListener listener : overpowerListeners) {
                listener.onOverpowerMeasurementChanged(overPower());
            }
            LOGGER.info("All receiver listeners notified");
            return new StatusResponse(StatusCode.SUCCESS, "All receiver listeners notified");
        }
        return new StatusResponse(StatusCode.SUCCESS, "No listeners notified");
    }

    @Override
    public void enableListeners(boolean isSelected) {
        this.listenersEnabled = isSelected;
    }

    /***
     *  overPower()
     *
     *  Exposes whether or not the receiver is subjected to a signal over the power threshold.
     *  Seperated from general StatusResponse errors due to representing a 'danger' to the system,
     *  therefore this could be used to interface with external interlocking/safety systems without
     *  relying on a status return
     *
     * @return
     */
    @Override
    public boolean overPower(){
        return isOverpowered;
    }


    /***
     *
     *  Main event loop to perform the receiving task
     *
     */
    private void performReceivingTask() {
        if (receiverState.equals(ReceiverState.ERROR)){
            if (receivingTask != null){
                receivingTask.cancel(true);
            }
            notifyReceiverStateChanged();
            LOGGER.error("Error in the receiver! ");
            return;
        }
        currentTaskRunningFor++;
        currentReceivedPower = new PowerMeasurement(simulatedSky.getValueAt(currentCoordinates));
        if (currentReceivedPower.getMeasurement() > RECEIVED_POWER_LIMIT){
            receiverState = ReceiverState.ERROR;
            isOverpowered = true;
            notifyOverpowerChanged();
            notifyReceiverStateChanged();
            LOGGER.error("Error in the receiver! ");
            return;
        }
        notifyReceivedPowerChanged(currentReceivedPower);
        notifyReceiverStateChanged();
    }

    public void currentPosition(DeviceCoordinates currentCoordinates) {
        this.currentCoordinates = currentCoordinates;
    }

    private boolean canReceive() {
        return receiverState.equals(ReceiverState.IDLE);
    }

    private void notifyReceivedPowerChanged(PowerMeasurement newReceivedPower) {
        if (listenersEnabled) {
            for (PowerMeasurementListener listener : powerListeners) {
                listener.onPowerMeasurementChanged(newReceivedPower);
            }
        }
    }

    private void notifyReceiverStateChanged() {
        if (listenersEnabled) {
            for (ReceiverStateListener listener : receiverStateListeners) {
                listener.onReceiverStateChanged(receiverState);
            }
        }
    }

    private void notifyOverpowerChanged() {
        if (listenersEnabled) {
            for (OverpoweredMeasurementListener listener : overpowerListeners) {
                listener.onOverpowerMeasurementChanged(isOverpowered);
            }
        }
    }
}
