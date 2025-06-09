package com.example.wav2vecapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReportHistoryActivity extends AppCompatActivity {

    Button btnStartDate, btnEndDate, btnSearch;
    Button btnBack;
    EditText etKeyword;
    RecyclerView recyclerView;
    ReportAdapter adapter;
    List<ReportItem> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSearch = findViewById(R.id.btnSearch);
        etKeyword = findViewById(R.id.etKeyword);
        recyclerView = findViewById(R.id.recyclerViewReports);
        btnBack = findViewById(R.id.history_btnBack);

        adapter = new ReportAdapter(reportList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Date pickers
        btnStartDate.setOnClickListener(v -> showDatePicker(btnStartDate));
        btnEndDate.setOnClickListener(v -> showDatePicker(btnEndDate));

        btnSearch.setOnClickListener(v -> {
            String keyword = etKeyword.getText().toString();
            String startDate = btnStartDate.getText().toString();
            String endDate = btnEndDate.getText().toString();
            fetchReportHistory(startDate, endDate, keyword);
        });

        btnBack.setOnClickListener(v-> finish());
    }

    private void showDatePicker(Button targetButton) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 0~11
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    // 날짜 선택 후 버튼 텍스트 업데이트
                    String selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
                    targetButton.setText(selectedDate);
                },
                year, month, day
        );
        dialog.show();
    }

    private void fetchReportHistory(String start, String end, String keyword) {

        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String uuid = prefs.getString("uuid", null);
        if (uuid == null) return;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)  // 예: http://10.0.2.2:5001/
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);
        Call<List<ReportItem>> call = api.getReportHistory(uuid, start, end, keyword);

        call.enqueue(new Callback<List<ReportItem>>() {
            @Override
            public void onResponse(Call<List<ReportItem>> call, Response<List<ReportItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reportList.clear();
                    reportList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.i("ReportFetch", "✅ 신고 이력 " + reportList.size() + "건 수신됨");
                } else {
                    Log.e("ReportFetch", "❌ 응답 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ReportItem>> call, Throwable t) {
                Log.e("ReportFetch", "🚫 네트워크 오류: " + t.getMessage());
            }
        });
    }

}

