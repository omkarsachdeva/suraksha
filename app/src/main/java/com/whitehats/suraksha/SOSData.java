package com.whitehats.suraksha;

public class SOSData {
    public double latitude;
    public double longitude;

    public SOSData() {
        // Default constructor required for calls to DataSnapshot.getValue(SOSData.class)
    }

    public SOSData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
}
}