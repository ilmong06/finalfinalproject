package com.example.wav2vecapp;


import com.google.gson.annotations.SerializedName;
/// 키워드 등록/수정 화면에서 키워드 목록 조회하는 데에 사용하는 클래스

/// .키워드 목록 조회
public class KeywordItem {

    @SerializedName("keywd_text")  // JSON 필드명과 일치해야 함
    private String keywd_text;

    @SerializedName("add_date")
    private String add_date;

    @SerializedName("keywd_order")
    private int keywd_order;

    // Getter
    public String getKeyword() {
        return keywd_text;
    }

    public String getDate() {
        return add_date;
    }
    public int getOrder(){
        return keywd_order;
    }
}
