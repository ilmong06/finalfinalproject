package com.example.wav2vecapp;

public class KeywordRequest {
    private String uuid;
    public KeywordRequest(String uuid) {
        this.uuid = uuid;
    }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
}
