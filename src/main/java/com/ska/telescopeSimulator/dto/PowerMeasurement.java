package com.ska.telescopeSimulator.dto;

public class PowerMeasurement {

    private final Double powerMeasurement;
    private boolean validMeasurement;

    public PowerMeasurement(Double powerMeasurement) {
        this.powerMeasurement = powerMeasurement;
        this.validMeasurement = true;
    }

    public static PowerMeasurement noMeasurement() {
        PowerMeasurement powerMeasurement = new PowerMeasurement(-1.0);
        powerMeasurement.validMeasurement = false;
        return powerMeasurement;
    }

    public Double getMeasurement(){
        return powerMeasurement;
    }
}
