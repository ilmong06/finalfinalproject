package com.example.wav2vecapp;

import java.util.List;

public class DeleteKeywordRequest {

    private String uuid;
    private List<String> keywords;

    public DeleteKeywordRequest(String uuid, List<String> keywords) {
        this.uuid = uuid;
        this.keywords = keywords;
    }
}
