package com.example.wav2vecapp;



/// KeywordResponse.Java와 한 묶음으로 사용되는 클래스(단일 키워드 조회)
public class KeywordRequest {
    private String uuid;


    public KeywordRequest(String uuid) {
        this.uuid = uuid;
    }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
}
