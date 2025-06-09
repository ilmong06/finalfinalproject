package com.example.wav2vecapp;

public class ReportItem {
    private int id;
    private String date;
    private String location;
    private String keyword;

    public ReportItem(int id,String date, String location, String keyword) {
        this.id=id;
        this.date = date;
        this.location = location;
        this.keyword = keyword;
    }

    public int getId(){
        return id;
    }
    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getKeyword() {
        return keyword;
    }
}

