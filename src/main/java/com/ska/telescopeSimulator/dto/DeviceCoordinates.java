package com.ska.telescopeSimulator.dto;

import java.util.Objects;

public class DeviceCoordinates {

    private final Double x;
    private final Double y;
    private final Double z;

    public DeviceCoordinates(Double x, Double y, Double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static DeviceCoordinates zeroed(){
        return new DeviceCoordinates(0.0,0.0,0.0);
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getZ() {
        return z;
    }

    public double distanceTo(DeviceCoordinates otherDevice) {
        double dx = otherDevice.x - this.x;
        double dy = otherDevice.y - this.y;
        double dz = otherDevice.z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceCoordinates that = (DeviceCoordinates) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "x=" + x +
                ", y=" + y +
                ", z=" + z;
    }
}
