package com.example.wav2vecapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MyPageActivity extends AppCompatActivity {

    Button confirm, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        confirm = findViewById(R.id.mp_btn_submit);
        btnBack = findViewById(R.id.mp_btnBack);


        /// 뒤로가기 버튼
        btnBack.setOnClickListener(view -> {
            finish();
        });


        /// 완료 버튼
        confirm.setOnClickListener(view -> {

        });

    }
}
