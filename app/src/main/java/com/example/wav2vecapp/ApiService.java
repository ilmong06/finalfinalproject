package com.example.wav2vecapp;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import retrofit2.http.Path;

import retrofit2.http.Query;

public interface ApiService {

    @POST("/api/register_user")
    Call<ResponseBody> registerUser(@Body UserInfo userInfo);

    @GET("/api/user_info")
    Call<UserInfo> getMyInfo();




    ///사용자정보 수정 불러오기
    @POST("/api/update_userinfo")
    Call<UserUpdateResponse> updateUserInfo(@Body UserUpdateRequest request);




    @Multipart
    @POST("/api/stt")
    Call<TranscriptionResponse> uploadAudio(@Part MultipartBody.Part file);

    @Multipart
    @POST("/api/register_keyword")
    Call<ResponseBody> registerKeyword(
            @Part("uuid") RequestBody uuid,
            @Part("keyword") RequestBody keyword,
            @Part("order") RequestBody order
    );

    @Multipart
    @POST("/api/register_speaker")
    Call<ResponseBody> registerSpeaker(@Part MultipartBody.Part file);

    // ✅ 추가된 현재 위치 전송 API
    @POST("/api/report_gps")
    Call<ResponseBody> sendGpsData(@Body GpsRequest gpsRequest);

    @GET("/api/get_reports")
    Call<List<ReportItem>> getReportHistory(
            @Query("uuid") String uuid,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("keyword") String keyword
    );



    @GET("/user/check")
    Call<UserResponse> checkUser(@Query("name") String name, @Query("phone") String phone);

    @POST("/api/get_keywords")
    Call<List<KeywordItem>> getKeyword(@Body KeywordRequest request);


    ///사용자 정보 불러오기 -mypage-
    @POST("/api/userinfo_mypage")
    Call<UserInfoResponse> getUserInformation(@Body KeywordRequest request);





    ///이름과 전화번호로 사용자 체크
    @GET("/user/check")
    Call<UserResponse> checkUser(@Query("name") String name, @Query("phone") String phone);



    /// 키워드 하나 조회
    @POST("/api/get_keywords")
    Call<KeywordResponse> getKeywords(@Body KeywordRequest request);


    /// 키워드 리스트 조회
    @POST("/api/get_keywords")
    Call<List<KeywordItem>> getKeywordList(@Body KeywordRequest request);


    @POST("/api/delete_keywords")
    Call<Void> deleteKeywords(@Body DeleteKeywordRequest request);

    @POST("/api/set_selected_keyword")
    Call<ResponseBody> setSelectedKeyword(@Body SelectedKeywordRequest request);
    @Multipart
    @POST("/api/register_voice")
    Call<ResponseBody> registerVoice(
            @Part MultipartBody.Part file,
            @Part("uuid") RequestBody uuid,
            @Part("index") RequestBody index
            // ✅ 추가
    );
    @Multipart
    @POST("/api/upload_voice")
    Call<ResponseBody> uploadVoice(
            @Part MultipartBody.Part file,
            @Part("uuid") RequestBody uuid
    );



    ///ApiService 키워드 삭제
    @POST("/api/delete_keywords")
    Call<Void> deleteKeywords(@Body DeleteKeywordRequest request);



}

