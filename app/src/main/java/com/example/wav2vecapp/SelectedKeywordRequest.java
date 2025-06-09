package com.example.wav2vecapp;

public class SelectedKeywordRequest {
    private String uuid;
    private String keyword_text;

    public SelectedKeywordRequest(String uuid, String keyword_text) {
        this.uuid = uuid;
        this.keyword_text = keyword_text;
    }

    public String getUuid() {
        return uuid;
    }

    public String getKeyword_text() {
        return keyword_text;
    }
}
