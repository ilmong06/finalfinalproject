package com.example.wav2vecapp;

public class GpsRequest {
    private String uuid;
    private double latitude;
    private double longitude;

    public GpsRequest(String uuid, double latitude, double longitude) {
        this.uuid = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}

