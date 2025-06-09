package com.example.wav2vecapp;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.GET;
import okhttp3.ResponseBody;

public interface JsonApiService {
    @POST("/api/userinfo")
    Call<ResponseBody> sendUserInfo(@Body UserInfo userInfo);

    @GET("/api/userinfo/me")
    Call<UserInfo> getMyInfo();

    @POST("/api/GpsRequest")
    Call<ResponseBody> sendGpsData(@Body GpsRequest gpsRequest);

}

