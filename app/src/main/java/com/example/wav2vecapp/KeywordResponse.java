package com.example.wav2vecapp;

import java.util.List;


/// 음성 녹음에서 키워드 조회하는 데에 사용하는 클래스
///
public class KeywordResponse {
    private List<String> keywords;

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
