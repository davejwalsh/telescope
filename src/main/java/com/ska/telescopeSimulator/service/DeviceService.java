package com.ska.telescopeSimulator.service;

import ch.qos.logback.core.status.Status;
import com.ska.telescopeSimulator.device.reciever.OverpoweredMeasurementListener;
import com.ska.telescopeSimulator.dto.StatusCode;
import com.ska.telescopeSimulator.exception.TelescopeDeviceException;
import com.ska.telescopeSimulator.states.StatusResponse;
import com.ska.telescopeSimulator.web.WebsocketController;
import com.ska.telescopeSimulator.device.DeviceStateListener;
import com.ska.telescopeSimulator.device.TelescopeDevice;
import com.ska.telescopeSimulator.device.TelescopeDeviceImpl;
import com.ska.telescopeSimulator.device.motor.MotorPositionListener;
import com.ska.telescopeSimulator.device.motor.MotorStateListener;
import com.ska.telescopeSimulator.device.reciever.PowerMeasurementListener;
import com.ska.telescopeSimulator.device.reciever.ReceiverStateListener;
import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.dto.PowerMeasurement;
import com.ska.telescopeSimulator.sky.SimulatedSky;
import com.ska.telescopeSimulator.states.DeviceState;
import com.ska.telescopeSimulator.states.MotorState;
import com.ska.telescopeSimulator.states.ReceiverState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private WebsocketController websocketController;

    HashMap<Integer, TelescopeDevice> telescopeDeviceMap = new HashMap<>();
    SimulatedSky simulatedSky;
    ArrayList<Integer> deviceIds = new ArrayList<>();
    private Integer selectedDevice = 0;

    public DeviceService(SimulatedSky simulatedSky, WebsocketController websocketController) {
        this.websocketController = websocketController;
        this.simulatedSky = simulatedSky;
        addNewDevice();
        setSelectedDevice(0);
        LOGGER.info("Device Service initalised");
    }

    public StatusResponse addNewDevice() {
        TelescopeDevice telescopeDevice = new TelescopeDeviceImpl(getNextUniqueDeviceId());
        telescopeDevice.addSimulatedSky(simulatedSky);
        telescopeDevice.addDeviceStateListener(new DeviceStateListener() {
            @Override
            public void onDeviceStateChanged(DeviceState newState) {
                websocketController.sendDeviceStateUpdate(newState);
            }
        });

        telescopeDevice.addMotorStateListener(new MotorStateListener() {
            @Override
            public void onMotorStateChanged(MotorState newMotorState) {
                websocketController.sendMotorStateUpdate(newMotorState);
            }
        });

        telescopeDevice.addMotorPositionListener(new MotorPositionListener() {
            @Override
            public void onMotorPositionChanged(DeviceCoordinates newPosition) {
                websocketController.sendMotorPositionUpdate(newPosition);
            }
        });

        telescopeDevice.addMotorTargetPositionListener(new MotorPositionListener() {
            @Override
            public void onMotorPositionChanged(DeviceCoordinates newPosition) {
                websocketController.sendMotorTargetPositionUpdate(newPosition);
            }
        });

        telescopeDevice.addReceiverStateListener(new ReceiverStateListener() {
            @Override
            public void onReceiverStateChanged(ReceiverState newReceiverState) {
                websocketController.sendReceiverStateUpdate(newReceiverState);
            }
        });

        telescopeDevice.addPowerMeasurementListener(new PowerMeasurementListener() {
            @Override
            public void onPowerMeasurementChanged(PowerMeasurement newPowerMeasurement) {
                websocketController.sendPowerMeasurementUpdate(newPowerMeasurement);
            }
        });

        telescopeDevice.addOverpoweredMeasurementListener(new OverpoweredMeasurementListener() {
            @Override
            public void onOverpowerMeasurementChanged(boolean isOverpowered) {
                websocketController.sendOverpoweredUpdate(isOverpowered);
            }
        });


        telescopeDeviceMap.put(telescopeDevice.getDeviceId(), telescopeDevice);
        selectedDevice = telescopeDevice.getDeviceId();
        LOGGER.info("New device added, ID number: " + telescopeDevice.getDeviceId().toString());
        return new StatusResponse(StatusCode.SUCCESS, "New device added");
    }

    private Integer getNextUniqueDeviceId() {
        Integer nextId = 0;
        if (deviceIds.size() > 0) {
            Integer lastId = deviceIds.get(deviceIds.size() - 1);
            nextId = lastId + 1;
        }
        deviceIds.add(nextId);
        return nextId;
    }

    public StatusResponse setSelectedDevice(Integer id) {
        this.selectedDevice = id;

        for (Map.Entry<Integer, TelescopeDevice> entry : telescopeDeviceMap.entrySet()) {
            if (entry.getKey().equals(selectedDevice)) {
                entry.getValue().setDeviceSelected(true);
            } else {
                entry.getValue().setDeviceSelected(false);
            }
        }

        return new StatusResponse(StatusCode.SUCCESS, "Device " + id + " selected");
    }

    public Integer getSelectedDevice() {
        return selectedDevice;
    }

    public DeviceState getDeviceState(){
        return telescopeDeviceMap.get(selectedDevice).getDeviceState();
    }

    public DeviceCoordinates getSetTargetCoordinates(){
        return telescopeDeviceMap.get(selectedDevice).getSetTargetCoordinates();
    }

    public StatusResponse resetSelectedDevice() {
        telescopeDeviceMap.get(selectedDevice).reset();
        return new StatusResponse(StatusCode.SUCCESS, "Device reset command sent");
    }

    public StatusResponse startSelectedDevice() {
        return telescopeDeviceMap.get(selectedDevice).start();
    }

    public StatusResponse stopSelectedDevice() {
        return telescopeDeviceMap.get(selectedDevice).stop();
    }

    public StatusResponse pauseSelectedDevice() {
        return telescopeDeviceMap.get(selectedDevice).pause();
    }

    public StatusResponse startReceivingSelectedDevice() {
        return telescopeDeviceMap.get(selectedDevice).startReceiving();
    }

    public StatusResponse stopReceivingSelectedDevice() {
        return telescopeDeviceMap.get(selectedDevice).stopReceiving();
    }

    public StatusResponse setShutterTime(Double shutterTimeInMs) {
        return telescopeDeviceMap.get(selectedDevice).setShutterTime(shutterTimeInMs);
    }

    public DeviceCoordinates getCurrentlySelectedDeviceCoordinates() {
        return telescopeDeviceMap.get(selectedDevice).getCurrentCoordinates();
    }

    public StatusResponse setTargetCoordinatesForSelectedDevice(DeviceCoordinates coordinates) {
        return telescopeDeviceMap.get(selectedDevice).setTargetCoordinate(coordinates);
    }

    public StatusResponse setMotorMovementRateForSelectedDevice(Double coordinates) {
        return telescopeDeviceMap.get(selectedDevice).setMotorMovementRate(coordinates);
    }

    public StatusResponse moveToTargetCoordinatesForSelectedDevice() {
            return telescopeDeviceMap.get(selectedDevice).moveToSetTarget();
    }

    public Double getPowerForCurrentlySelectedDevice() {
        return telescopeDeviceMap.get(selectedDevice).getPowerMeasurement().getMeasurement();
    }

    public List<Integer> getAvailableDevices() {
        return telescopeDeviceMap.keySet().stream().toList();
    }

    public StatusResponse induceMotorError(){
        return telescopeDeviceMap.get(selectedDevice).induceMotorError();
    }

    public StatusResponse induceReceiverError(){
        return telescopeDeviceMap.get(selectedDevice).induceReceiverError();
    }

    public StatusResponse notifyAllChange(){
        return telescopeDeviceMap.get(selectedDevice).notifyAllChange();
    }


}
