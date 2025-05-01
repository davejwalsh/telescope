package com.ska.telescopeSimulator.web;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.service.DeviceService;
import com.ska.telescopeSimulator.states.StatusResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class EndpointsController {

    private final DeviceService service;

    public EndpointsController(DeviceService service) {
        this.service = service;
    }

    @GetMapping("/getAvailableDevices")
    public List<Integer> getAvailableDevices() {
        return service.getAvailableDevices();
    }

    @PostMapping("/induceMotorError")
    public StatusResponse induceMotorError() {
        return service.induceMotorError();
    }

    @PostMapping("/induceReceiverError")
    public StatusResponse induceReceiverError() {
        return service.induceReceiverError();
    }

    @PostMapping("/startDevice")
    public StatusResponse startDevice() {
        return service.startSelectedDevice();
    }

    @PostMapping("/stopDevice")
    public StatusResponse stopDevice() {
       return  service.stopSelectedDevice();
    }

    @PostMapping("/setSelectedDevice")
    public StatusResponse setSelectedDevice(@RequestParam Integer deviceId) {
         service.setSelectedDevice(deviceId);
         return service.notifyAllChange();
    }

    @PostMapping("/addNewDevice")
    public StatusResponse addDevice() {
        return service.addNewDevice();
    }

    @PostMapping("/setTargetCoordinates")
    public StatusResponse setTargetCoordinates(@RequestParam Double xCoord, @RequestParam Double yCoord, @RequestParam Double zCoord) {
        return service.setTargetCoordinatesForSelectedDevice(new DeviceCoordinates(xCoord, yCoord, zCoord));
    }

    @PostMapping("/moveToTargetCoordinates")
    public StatusResponse moveToTargetCoordinates() {
        return service.moveToTargetCoordinatesForSelectedDevice();
    }

    @PostMapping("/setShutterTime")
    public StatusResponse setShutterTime(@RequestParam Double shutterTime) {
        return service.setShutterTime(shutterTime);
    }

    @PostMapping("/setMovementRate")
    public StatusResponse setMovementRate(@RequestParam Double movementRate) {
        return service.setMotorMovementRateForSelectedDevice(movementRate);
    }

    @PostMapping("/pauseMovement")
    public StatusResponse pauseMovement() {
        return service.pauseSelectedDevice();
    }

    @PostMapping("/startReceiving")
    public StatusResponse startReceiving() {
        return service.startReceivingSelectedDevice();
    }

    @PostMapping("/stopReceiving")
    public StatusResponse stopReceiving() {
       return service.stopReceivingSelectedDevice();
    }

    @PostMapping("/resetDevice")
    public StatusResponse resetDevice() {
        service.resetSelectedDevice();
        return service.notifyAllChange();
    }
}
