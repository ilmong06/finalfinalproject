/* 앱의 메인화면 */

package com.example.wav2vecapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * MainActivity:
 * - 사용자 정보를 서버에서 조회해 화면에 출력
 * - 키워드 등록 화면으로 이동
 * - 위치 권한 요청 포함
 */
public class MainActivity extends AppCompatActivity {

    private TextView welcomeMessage, phoneNumber;
    private Button btnMoveKeywordPage, btnVoiceRegisterPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔗 UI 요소 연결
        welcomeMessage = findViewById(R.id.welcomeMessage);
        phoneNumber = findViewById(R.id.phoneNumber);
        btnMoveKeywordPage = findViewById(R.id.btnMoveKeywordPage);




        // 🧾 사용자 정보 불러오기
        loadUserInfoFromServer();

        // 👉 키워드 등록 화면 이동
        btnMoveKeywordPage.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, KeywordActivity.class);
            startActivity(intent);
        });

        // 👉 화자 등록 화면 이동
        btnVoiceRegisterPage.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, VoiceRegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 앱 실행 시 권한 요청
     */


    /**
     * 서버로부터 사용자 이름/전화번호 가져오기
     */
    private void loadUserInfoFromServer() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String token = prefs.getString("user_token", null);

        if (token == null) {
            Log.e("JWT", "❌ 토큰 없음, 사용자 정보 요청 불가");
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build()
                ))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.FLASK_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonApiService api = retrofit.create(JsonApiService.class);
        Call<UserInfo> call = api.getMyInfo();

        call.enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo user = response.body();
                    welcomeMessage.setText("환영합니다 " + user.name + "님");
                    phoneNumber.setText(formatPhone(user.phnum));
                } else {
                    Log.e("JWT", "❌ 사용자 정보 요청 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e("JWT", "🚫 서버 요청 실패: " + t.getMessage());
            }
        });
    }

    /**
     * 전화번호 하이픈 포맷
     */
    private String formatPhone(String raw) {
        return raw.length() == 11 ?
                raw.substring(0, 3) + "-" + raw.substring(3, 7) + "-" + raw.substring(7) :
                raw;
    }
}
