package com.example.wav2vecapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.MediaType;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KeywordActivity extends AppCompatActivity {

    private EditText etKeyword;
    private Button btnAddKeyword, btnBack, delete, edit;

    private LinearLayout kListLayout;
    private boolean isEditMode = false;
    private String uuid;
    private SharedPreferences sharedPreferences;
    private int keywordOrder = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyword);

        etKeyword = findViewById(R.id.etKeyword);
        btnAddKeyword = findViewById(R.id.btnAddKeyword);
        btnBack = findViewById(R.id.btnBack);

        delete = findViewById(R.id.btnDeleteSelected);
        edit = findViewById(R.id.editKeyword);

        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        uuid = sharedPreferences.getString("uuid", "");
        Log.d("UUID", "📌 UUID 불러오기 결과: " + uuid);

        /// get Keywords
        getKeywordListFromServer(uuid);


        /// 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());


        /// 키워드 등록
        btnAddKeyword.setOnClickListener(v -> {
            String keyword = etKeyword.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(this, "❗ 키워드를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 현재 시간 등록 날짜로 사용
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // 인덱스 증가
            keywordOrder++;

            // UI에 추가 (index, keyword, date 모두 전달)
            addKeywordRow(keywordOrder, keyword, currentDate);

            // 서버 전송
            sendKeywordToServer(keyword, keywordOrder, currentDate);

            // 입력창 비우기
            etKeyword.setText("");
        });

        /// 편집 버튼
        edit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            edit.setText(isEditMode ? "완료" : "편집");
            delete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
            toggleCheckBoxes(isEditMode);
        });

        /// 삭제 버튼
        delete.setOnClickListener(v -> {

            deleteKeywords(uuid);
              // SharedPreferences 등에서 UUID 가져오는 메소드

        });
    }


    /// 키워드 삭제 버튼
    private void deleteKeywords(String uuid) {
        List<String> selectedKeywords = new ArrayList<>();
        LinearLayout kListLayout = findViewById(R.id.k_list);

        // 1번 줄은 헤더 → 건너뛰기
        for (int i = 1; i < kListLayout.getChildCount(); i++) {
            View item = kListLayout.getChildAt(i);
            if (item instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) item;

                // 체크박스는 4번째 요소 (index 3)
                CheckBox checkBox = (CheckBox) row.getChildAt(3);

                // 키워드는 2번째 요소 (index 1)
                TextView keywordTextView = (TextView) row.getChildAt(1);
                String keyword = keywordTextView.getText().toString();

                if (checkBox.isChecked()) {
                    selectedKeywords.add(keyword);
                }
            }
        }

        if (selectedKeywords.isEmpty()) {
            Toast.makeText(this, "❗ 삭제할 키워드를 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        DeleteKeywordRequest rq = new DeleteKeywordRequest(uuid, selectedKeywords);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteKeywords(rq);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(KeywordActivity.this, "✅ 삭제 성공", Toast.LENGTH_SHORT).show();
                    getKeywordListFromServer(uuid);  // 삭제 후 목록 새로고침
                } else {
                    Toast.makeText(KeywordActivity.this, "⚠️ 서버 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(KeywordActivity.this, "❌ 통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /// 체크박스 생성기
    private void toggleCheckBoxes(boolean visible) {
        for (int i = 0; i < kListLayout.getChildCount(); i++) {
            View view = kListLayout.getChildAt(i);
            if (view instanceof LinearLayout l) {
                for (int j = 0; j < l.getChildCount(); j++) {
                    View child = l.getChildAt(j);
                    if (child instanceof CheckBox) {
                        child.setVisibility(visible ? View.VISIBLE : View.GONE);
                    }
                }
            }
        }
    }

    /// 서버에서 키워드 가져오기
    private void getKeywordListFromServer(String uuid) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        KeywordRequest rq = new KeywordRequest(uuid);
        Call<List<KeywordItem>> call = apiService.getKeywordList(rq);

        call.enqueue(new Callback<List<KeywordItem>>() {
            @Override
            public void onResponse(Call<List<KeywordItem>> call, Response<List<KeywordItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<KeywordItem> keywordList = response.body();  // ✅ 수정

                    kListLayout = findViewById(R.id.k_list);  // LinearLayout
                    kListLayout.removeAllViews();  // 기존 목록 초기화

                    // 헤더 행 추가
                    addKeywordHeaderRow();

                    for (KeywordItem item : keywordList) {
                        addKeywordRow(item.getOrder(), item.getKeyword(), item.getDate());
                    }
                } else {
                    Toast.makeText(KeywordActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<KeywordItem>> call, Throwable t) {
                Toast.makeText(KeywordActivity.this, "키워드 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /// 키워드를 목록에 추가
    private void addKeywordHeaderRow() {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.setBackgroundColor(Color.parseColor("#DDDDDD"));
        rowLayout.setPadding(8, 8, 8, 8);

        String[] headers = {"인덱스", "키워드", "등록 날짜", "선택"};
        float[] weights = {1, 3, 3, 1};

        for (int i = 0; i < headers.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(headers[i]);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[i]));
            rowLayout.addView(tv);
        }

        kListLayout.addView(rowLayout);
    }

    private void addKeywordRow(int index, String keyword, String date) {
        kListLayout = findViewById(R.id.k_list);
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.setPadding(8, 8, 8, 8);

        // 인덱스
        TextView tvIndex = new TextView(this);
        tvIndex.setText(String.valueOf(index));
        tvIndex.setGravity(Gravity.CENTER);
        tvIndex.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        rowLayout.addView(tvIndex);

        // 키워드
        TextView tvKeyword = new TextView(this);
        tvKeyword.setText(keyword);
        tvKeyword.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3));
        rowLayout.addView(tvKeyword);

        // 등록 날짜
        TextView tvDate = new TextView(this);
        tvDate.setText(date);
        tvDate.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3));
        rowLayout.addView(tvDate);

        // 체크박스
        CheckBox checkBox = new CheckBox(this);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        checkBox.setVisibility(View.GONE);  // 초기 상태는 숨김
        rowLayout.addView(checkBox);

        // 레이아웃에 추가
        kListLayout.addView(rowLayout);
    }






    private void sendKeywordToServer(String keyword, int order, String currentDate) {
        RequestBody uuidBody = RequestBody.create(MediaType.parse("text/plain"), uuid);  // UUID가 먼저
        RequestBody keywordBody = RequestBody.create(MediaType.parse("text/plain"), keyword);
        RequestBody orderBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(order));

// Retrofit 객체 생성
        Retrofit retrofit = getRetrofitClient();
        ApiService apiService = retrofit.create(ApiService.class);

// 전송
        Call<ResponseBody> call = apiService.registerKeyword(uuidBody, keywordBody, orderBody);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(KeywordActivity.this, "✅ 등록 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(KeywordActivity.this, "❌ 서버 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(KeywordActivity.this, "🚫 통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Retrofit getRetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void deleteKeywords() {
        for (int i = kListLayout.getChildCount() - 1; i >= 0; i--) {
            View view = kListLayout.getChildAt(i);
            if (view instanceof LinearLayout row) {
                CheckBox checkBox = (CheckBox) row.getChildAt(2);
                if (checkBox.isChecked()) {
                    kListLayout.removeViewAt(i);
                }
            }
        }
    }
}
