package com.example.wav2vecapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LogoActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5초 로딩 시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo); // ✅ 변경된 레이아웃 이름 사용

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String accessToken = prefs.getString("access_token", null);

            if (accessToken != null && !accessToken.isEmpty()) {
                // ✅ 토큰이 존재 → 메인화면으로 이동
                Intent intent = new Intent(LogoActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                // ❌ 토큰 없음 → 사용자 등록화면으로 이동
                Intent intent = new Intent(LogoActivity.this, UserInfoActivity.class);
                startActivity(intent);
            }

            finish(); // 로고화면 종료
        }, SPLASH_DELAY);
    }
}
