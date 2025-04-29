package com.ska.telescopeSimulator.sky;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;

public class SimulatedStar {

    private final DeviceCoordinates coordinates;
    private final Double intensity;
    private final Double radius;

    public SimulatedStar(DeviceCoordinates coordinates, Double intensity, Double radius){
        this.coordinates = coordinates;
        this.intensity = intensity;
        this.radius = radius;
    }

    public DeviceCoordinates getCoordinates() {
        return coordinates;
    }

    public Double getIntensity() {
        return intensity;
    }

    public Double getRadius() {
        return radius;
    }
}
