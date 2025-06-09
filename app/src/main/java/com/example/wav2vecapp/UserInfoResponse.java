package com.example.wav2vecapp;

import com.google.gson.annotations.SerializedName;

public class UserInfoResponse {

    @SerializedName("name")
    private String name;             // 사용자 이름

    @SerializedName("phnum")
    private String phnum;            // 사용자 전화번호

    @SerializedName("em_name")
    private String emergencyName;    // 보호자 이름

    @SerializedName("em_phone")
    private String emergencyPhone;   // 보호자 전화번호

    @SerializedName("em_parent")
    private String relation;         // 보호자와의 관계


    public String getName() {
        return name;
    }

    public String getPhnum() {
        return phnum;
    }


    public String getEmergencyName() {
        return emergencyName;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public String getRelation() {
        return relation;
    }





}
