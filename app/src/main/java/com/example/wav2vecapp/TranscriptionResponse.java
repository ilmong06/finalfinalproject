package com.example.wav2vecapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TranscriptionResponse {
    @SerializedName("text")
    public String text;

    @SerializedName("triggered_keyword")
    public String triggeredKeyword;

    @SerializedName("speaker_vector")
    public List<Float> speakerVector;
}
