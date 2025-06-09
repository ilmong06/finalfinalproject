package com.example.wav2vecapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenManager {

    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_TOKEN = "token";
    private SharedPreferences prefs;
    private Context context;

    public TokenManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 🔐 토큰 저장
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    // 🔓 토큰 불러오기
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // 🚫 기간 만료 토큰 제거
    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    // ✅ 이 클래스에 포함된 JWT 인터셉터 제공
    public Interceptor getAuthInterceptor() {
        return chain -> {
            String token = getToken();

            Request request = chain.request();
            if (token != null) {
                request = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
            }

            Response response = chain.proceed(request);

            if (response.code() == 401) {
                clearToken(); // ⛔️ 저장된 토큰 삭제

                // UI 스레드에서 처리
                new Handler(context.getMainLooper()).post(() -> {
                    Toast.makeText(context, "세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, UserInfoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                });
            }

            return response;
        };
    }
}


