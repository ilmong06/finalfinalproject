package com.example.wav2vecapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * activity_access_mypage, activity_denied
 * 개인 정보 수정하기 전, 입력한 정보가 맞는지 검사하는 화면.
 * 1) 이름과 전화번호 입력.
 * 2) DB와 비교
 * 3) 일치하면 MyPage 화면으로 이동
 *
 * */

public class AccessActivity extends AppCompatActivity {

    Button confirm, back;
    EditText identify, ph;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_mypage);

        confirm = findViewById(R.id.ac_btn_submit);
        identify = findViewById(R.id.ac_plain_text_input);
        ph = findViewById(R.id.ac_phone_text_input);
        back = findViewById(R.id.ac_btnBack);


        /// 완료 버튼을 누르면 아이디, 연락처를 비교.
        /// 있으면 회원정보 수정 화면으로 이동.
        confirm.setOnClickListener(v -> {
            String name = identify.getText().toString().trim();
            String phone = ph.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "이름과 전화번호를 모두 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            checkUserExists(name, phone);
        });

        back.setOnClickListener(view -> {
            finish();
        });
    }


    private void checkUserExists(String name, String phone) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.checkUser(name, phone);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().exists) {
                    // ✅ 사용자 존재 → 다음 화면으로 이동
                    Intent intent = new Intent(AccessActivity.this, MyPageActivity.class);
                    startActivity(intent);
                } else {
                    // ❌ 사용자 없음
                    Toast.makeText(AccessActivity.this, "사용자 정보가 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(AccessActivity.this, "서버 연결에 실패했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
