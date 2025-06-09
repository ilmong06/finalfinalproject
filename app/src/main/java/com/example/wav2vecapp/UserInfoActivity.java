package com.example.wav2vecapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 사용자 정보 입력 후 서버에 등록하고 토큰과 uuid 저장
 */
public class UserInfoActivity extends AppCompatActivity {

    // 사용자 입력 필드
    EditText etName, etPhone, etVerificationCode, etBirth, etGender, etEmergencyName, etEmergencyPhone;
    Spinner spinnerLanguage, spinnerRelation;
    Button btnRequestVerification, btnSubmit;
    TextView tv_ver_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        // 🔗 UI 연결
        etName = findViewById(R.id.plain_text_input);

        etBirth = findViewById(R.id.birth);
        etGender = findViewById(R.id.gender);
        etEmergencyName = findViewById(R.id.et_emergency_name);
        etEmergencyPhone = findViewById(R.id.et_emergency_phone);

        spinnerLanguage = findViewById(R.id.spinner_language);
        spinnerRelation = findViewById(R.id.spinner_relation);

        /// 전화번호 동일하지 않을 경우 발생.
        etVerificationCode = findViewById(R.id.et_verification_code);
        tv_ver_message = findViewById(R.id.tv_verification_message);
        etPhone = findViewById(R.id.phone_text_input);

        String phoneNumber = etPhone.getText().toString().trim();
        String verificationCode = etVerificationCode.getText().toString().trim();
        // 일치 여부 확인


        btnSubmit = findViewById(R.id.btn_submit);


        // ✅ 완료 버튼 클릭 시 등록 요청
        btnSubmit.setOnClickListener(v -> {

            if (!phoneNumber.equals(verificationCode)) {
                // 다르면 안내 메시지 표시
                tv_ver_message.setText("입력한 전화번호가 일치하지 않습니다.");
                tv_ver_message.setVisibility(View.VISIBLE);
            } else {
                // 같으면 메시지 숨김
                tv_ver_message.setVisibility(View.GONE);
                saveUserData();
            }



        });
    }

    /**
     * 사용자 입력값을 수집하고 서버로 전송
     */
    private void saveUserData() {
        // 1️⃣ 입력값 가져오기
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "이름이 없습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "전화번호가 없습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String birthRaw = etBirth.getText().toString().trim();
        if (birthRaw.isEmpty()) {
            Toast.makeText(this, "생년월일이 없습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String genderCode = etGender.getText().toString().trim();
        if (genderCode.isEmpty()) {
            Toast.makeText(this, "성별 코드가 없습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String emergencyName = etEmergencyName.getText().toString().trim();
        if (emergencyName.isEmpty()) {
            Toast.makeText(this, "비상 연락처 이름이 없습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String emergencyPhone = etEmergencyPhone.getText().toString().trim();
        if (emergencyPhone.isEmpty()) {
            Toast.makeText(this, "비상 연락처 전화번호가 없습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String language = spinnerLanguage.getSelectedItem().toString();
        String relation = spinnerRelation.getSelectedItem().toString();

        // 2️⃣ 생년월일 변환 (yyyyMMdd)
        int yearPrefix = Integer.parseInt(birthRaw.substring(0, 2));
        String fullBirthYear = (yearPrefix <= 25 ? "20" : "19") + birthRaw.substring(0, 2);
        String fullBirth = fullBirthYear + birthRaw.substring(2);

        // 3️⃣ 성별 코드 해석
        String gender = switch (genderCode) {
            case "1", "3" -> "남자";
            case "2", "4" -> "여자";
            default -> "기타";
        };

        // 4️⃣ UUID 생성
        String generatedUuid = UUID.randomUUID().toString();

        // 5️⃣ 서버로 전송할 객체 구성
        UserInfo userInfo = new UserInfo(
                generatedUuid,
                name,
                phone,
                language,
                fullBirth,
                gender,
                emergencyName,
                emergencyPhone,
                relation
        );

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.registerUser(userInfo);

        // 6️⃣ 비동기 전송
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String token = json.getString("token");
                        String returnedUuid = json.getString("uuid");

                        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("uuid", returnedUuid);
                        editor.apply();


                        Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(UserInfoActivity.this, "응답 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserInfoActivity.this, "등록 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(UserInfoActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
