
package com.example.wav2vecapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MypageActivity extends AppCompatActivity {

    private EditText nameInput, phoneInput;
    private EditText emNameInput, emPhoneInput;
    private Spinner relationSpinner;
    private Button submitButton, backButton;
    private SharedPreferences sharedPreferences;

    private TokenManager tokenManager;
    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        tokenManager = new TokenManager(getApplicationContext());



        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        uuid = sharedPreferences.getString("uuid", "");
        Log.d("UUID", "📌 UUID 불러오기 결과: " + uuid);

        if (uuid == null || uuid.isEmpty()) {
            Toast.makeText(this, "UUID가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 🔗 UI 연결
        nameInput = findViewById(R.id.mp_plain_text_input);
        phoneInput = findViewById(R.id.mp_phone_text_input);
        emNameInput = findViewById(R.id.mp_et_emergency_name);
        emPhoneInput = findViewById(R.id.mp_et_emergency_phone);
        relationSpinner = findViewById(R.id.mp_spinner_relation);
        submitButton = findViewById(R.id.mp_btn_submit);
        backButton = findViewById(R.id.mp_btnBack);

        // 🔄 사용자 정보 불러오기
        loadUserData();

        // ← 버튼 → 메인화면 이동
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(MypageActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // ✅ 완료 버튼 → 정보 저장 후 메인화면 이동
        submitButton.setOnClickListener(view -> saveUserInfo());
    }

    private void saveUserInfo() {
        String name = nameInput.getText().toString();
        String phone = phoneInput.getText().toString();
        String emName = emNameInput.getText().toString();
        String emPhone = emPhoneInput.getText().toString();
        String relation = relationSpinner.getSelectedItem().toString();

        try {
            JSONObject json = new JSONObject();
            json.put("uuid", uuid);
            json.put("name", name);
            json.put("phnum", phone);
            json.put("em_name", emName);
            json.put("em_phnum", emPhone);
            json.put("em_relation", relation);

            RequestBody requestBody = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://192.168.45.131:5001/api/update_userinfo")
                    .post(requestBody)
                    .build();

            new Thread(() -> {
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);
                        String newToken = responseJson.getString("token");

                        tokenManager.saveToken(newToken); // ✅ 토큰 갱신

                        runOnUiThread(() -> {
                            Toast.makeText(this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MypageActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "수정 실패: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "서버 오류: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "데이터 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 🔄 사용자 정보 조회 및 필드 채우기
    private void loadUserData(String uuid) {

        if (uuid == null || uuid.isEmpty()) {
            Toast.makeText(this, "UUID가 없습니다. 사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        KeywordRequest request = new KeywordRequest(uuid);  // uuid 담은 request 객체

        Call<UserInfoResponse> call = apiService.getUserInformation(request);
        call.enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfoResponse user = response.body();

                    nameInput.setText(user.getName());
                    phoneInput.setText(user.getPhnum());
                    emNameInput.setText(user.getEmergencyName());
                    emPhoneInput.setText(user.getEmergencyPhone());


                    String[] relationArray = getResources().getStringArray(R.array.relationship_list);
                    for (int i = 0; i < relationArray.length; i++) {
                        if (relationArray[i].equals(user.getRelation())) {
                            relationSpinner.setSelection(i);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(MypageActivity.this, "사용자 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Call<UserInfoResponse> call, retrofit2.Response<UserInfoResponse> response) {

            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(MypageActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}
