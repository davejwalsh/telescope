package com.ska.telescopeSimulator.web;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.dto.PowerMeasurement;
import com.ska.telescopeSimulator.states.DeviceState;
import com.ska.telescopeSimulator.states.MotorState;
import com.ska.telescopeSimulator.states.ReceiverState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController{

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebsocketController(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingTemplate = simpMessagingTemplate;
    }

    public void sendDeviceStateUpdate(DeviceState newState) {
        messagingTemplate.convertAndSend("/telescope/deviceState", newState);
    }

    public void sendMotorStateUpdate(MotorState motorState) {
        messagingTemplate.convertAndSend("/telescope/motorState", motorState);
    }

    public void sendMotorPositionUpdate(DeviceCoordinates newCoordinates) {
        messagingTemplate.convertAndSend("/telescope/motorPosition", newCoordinates);
    }

    public void sendMotorTargetPositionUpdate(DeviceCoordinates newCoordinates) {
        messagingTemplate.convertAndSend("/telescope/targetPosition", newCoordinates);
    }

    public void sendReceiverStateUpdate(ReceiverState receiverState) {
        messagingTemplate.convertAndSend("/telescope/receiverState", receiverState);
    }

    public void sendPowerMeasurementUpdate(PowerMeasurement powerMeasurement) {
        messagingTemplate.convertAndSend("/telescope/powerMeasurement", powerMeasurement);
    }

    public void sendOverpoweredUpdate(Boolean isOverpowered) {
        messagingTemplate.convertAndSend("/telescope/overpowered", isOverpowered);
    }

}
