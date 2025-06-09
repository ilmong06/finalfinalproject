/*자동 로그인 유지 여부를 판단해, 사용자 상태에 따라 다음 화면으로 전환하는 클래스*/

package com.example.wav2vecapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        long loginTime = prefs.getLong("login_time", 0);
        long currentTime = System.currentTimeMillis();

        boolean isLoggedIn = (currentTime - loginTime) <= 604800000L; // 7일 in milliseconds

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, UserInfoActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
