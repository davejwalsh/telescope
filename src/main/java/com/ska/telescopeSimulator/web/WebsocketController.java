package com.ska.telescopeSimulator.web;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.dto.PowerMeasurement;
import com.ska.telescopeSimulator.states.DeviceState;
import com.ska.telescopeSimulator.states.MotorState;
import com.ska.telescopeSimulator.states.ReceiverState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;


@Controller
public class WebsocketController{

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebsocketController(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingTemplate = simpMessagingTemplate;
    }

    @Async
    public void sendDeviceStateUpdate(DeviceState newState) {
       messagingTemplate.convertAndSend("/telescope/deviceState", newState);
    }

    @Async
    public void sendMotorStateUpdate(MotorState motorState) {
        messagingTemplate.convertAndSend("/telescope/motorState", motorState);
    }

    @Async
    public void sendMotorPositionUpdate(DeviceCoordinates newCoordinates) {
        messagingTemplate.convertAndSend("/telescope/motorPosition", newCoordinates);
    }

    @Async
    public void sendMotorTargetPositionUpdate(DeviceCoordinates newCoordinates) {
        messagingTemplate.convertAndSend("/telescope/targetPosition", newCoordinates);
    }

    @Async
    public void sendReceiverStateUpdate(ReceiverState receiverState) {
        messagingTemplate.convertAndSend("/telescope/receiverState", receiverState);
    }

    @Async
    public void sendPowerMeasurementUpdate(PowerMeasurement powerMeasurement) {
        messagingTemplate.convertAndSend("/telescope/powerMeasurement", powerMeasurement);
    }

    @Async
    public void sendOverpoweredUpdate(Boolean isOverpowered) {
        messagingTemplate.convertAndSend("/telescope/overpowered", isOverpowered);
    }

}
