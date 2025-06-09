/* 앱의 메인화면 */

package com.example.wav2vecapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * MainActivity:
 * - 사용자 정보를 서버에서 조회해 화면에 출력
 * - 키워드 등록 화면으로 이동
 * - 위치 권한 요청 포함
 */
public class MainActivity extends AppCompatActivity {


    /// 사용자 정보
    private TextView welcomeMessage, phoneNumber;

    /// 가운데 4개 버튼
    private Button keyWord, voiceRecord, micOn;
    /// menu 버튼
    private Button menu;
    /// app Name
    private TextView appLogo;


    ///메뉴 레이아웃에 포함된 버튼/TextView
    private TextView tvReportHistory, tvNotice, tvPrivacy;

    private SwitchCompat switchMockReport;
    private Button btnMyPage, btnSettings;
    private DrawerLayout drawerLayout;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private File wavFile;
    private int bufferSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// 🔗 UI 요소 연결
        // 앱 이름
        appLogo = findViewById(R.id.appLogo);
        // 사용자 정보
        welcomeMessage = findViewById(R.id.welcomeMessage);
        phoneNumber = findViewById(R.id.phoneNumber);

        //가운데 4개 버튼
        keyWord = findViewById(R.id.keyWord);
        voiceRecord = findViewById(R.id.registerButton);
        micOn = findViewById(R.id.micOnOff);
        micOn.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
                micOn.setText("마이크 OFF");
            } else {
                stopRecording();
                micOn.setText("마이크 ON");
            }
        });

        //btnMoveKeywordPage = findViewById(R.id.btnMoveKeywordPage);

        //햄버거 메뉴 버튼
        menu = findViewById(R.id.btnMenu);

        // 그 속의 내용물들
        tvReportHistory = findViewById(R.id.tvReportHistory);
        tvNotice = findViewById(R.id.tvNotice);
        tvPrivacy = findViewById(R.id.tvPrivacy);
        switchMockReport = findViewById(R.id.switchMockReport);
        btnMyPage = findViewById(R.id.btnMyPage);
        btnSettings = findViewById(R.id.btnSettings);
        drawerLayout = findViewById(R.id.drawerLayout);




        // 🧾 사용자 정보 불러오기
        //loadUserInfoFromServer();

        // 👉 키워드 등록 화면 이동
        /*btnMoveKeywordPage.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, KeywordActivity.class);
            startActivity(intent);
        });

        // 👉 화자 등록 화면 이동
        btnVoiceRegisterPage.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, VoiceRegisterActivity.class);
            startActivity(intent);
        });*/

        ///햄버거 메뉴버튼
        menu.setOnClickListener(v -> {
            //클릭하면 오른쪽에서 레이아웃 등장
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END);
            } else {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });

        /// 여백 클릭시 레이아웃 닫기




        ///  키워드 등록 화면 이동
        keyWord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, KeywordActivity.class);
            startActivity(intent);
        });

        /// ️음성 등록 버튼 → 음성 등록 화면 이동
        voiceRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VoiceRegisterActivity.class);
            startActivity(intent);
        });

        /// 6GetHelp! 텍스트 클릭 → 새로고침
        appLogo.setOnClickListener(v -> {
            recreate(); // 현재 액티비티 새로고침
        });


        /// 햄버거 메뉴 클릭 시 등장하는 컴포넌트의 이벤트


        tvReportHistory.setOnClickListener(v -> {
            // 신고 내역 화면으로 이동
            Intent intent = new Intent(MainActivity.this, ReportHistoryActivity.class);
            startActivity(intent);
        });

        tvNotice.setOnClickListener(v -> {
            // 안내 화면으로 이동
        });

        tvPrivacy.setOnClickListener(v -> {
            // 개인정보처리방침 화면으로 이동
        });

        switchMockReport.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 스위치 on/off 이벤트 처리
        });

        btnMyPage.setOnClickListener(v -> {
            // 마이페이지 이동
            Intent intent = new Intent(MainActivity.this, AccessActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            // 설정 화면 이동
        });



    }
    private void startRecording() {
        int sampleRate = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, bufferSize);

        wavFile = new File(getExternalFilesDir(null), "main_recorded_audio.wav");

        audioRecord.startRecording();
        isRecording = true;

        recordingThread = new Thread(() -> writeAudioDataToFile(), "AudioRecorder Thread");
        recordingThread.start();
    }
    private void stopRecording() {
        if (audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;
            writeWavHeader();  // WAV 헤더 작성
            uploadWavToServer(wavFile);
        }
    }
    private void writeAudioDataToFile() {
        byte[] data = new byte[bufferSize];
        try (FileOutputStream os = new FileOutputStream(wavFile)) {
            while (isRecording) {
                int read = audioRecord.read(data, 0, bufferSize);
                if (read > 0) {
                    os.write(data, 0, read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeWavHeader() {
        try {
            File tmpFile = new File(getExternalFilesDir(null), "main_recorded_audio.wav");
            RandomAccessFile wav = new RandomAccessFile(tmpFile, "rw");
            long totalAudioLen = wav.length() - 44;
            long totalDataLen = totalAudioLen + 36;
            int sampleRate = 16000;
            int channels = 1;
            long byteRate = 16 * sampleRate * channels / 8;

            wav.seek(0);
            wav.writeBytes("RIFF");
            wav.writeInt(Integer.reverseBytes((int) totalDataLen));
            wav.writeBytes("WAVE");
            wav.writeBytes("fmt ");
            wav.writeInt(Integer.reverseBytes(16));
            wav.writeShort(Short.reverseBytes((short) 1));
            wav.writeShort(Short.reverseBytes((short) channels));
            wav.writeInt(Integer.reverseBytes(sampleRate));
            wav.writeInt(Integer.reverseBytes((int) byteRate));
            wav.writeShort(Short.reverseBytes((short) (channels * 2)));
            wav.writeShort(Short.reverseBytes((short) 16));
            wav.writeBytes("data");
            wav.writeInt(Integer.reverseBytes((int) totalAudioLen));
            wav.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void uploadWavToServer(File file) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // ✅ uuid 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String uuid = sharedPreferences.getString("uuid", "");

        // ✅ uuid 포함하여 Multipart 구성
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/wav"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "compare.wav", requestFile);
        RequestBody uuidBody = RequestBody.create(MediaType.parse("text/plain"), uuid);

        // ✅ uuid도 함께 전송
        Call<ResponseBody> call = apiService.uploadVoice(body, uuidBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("업로드", "✅ compare.wav 업로드 성공");
                } else {
                    Log.e("업로드", "❌ 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("업로드", "🚫 오류: " + t.getMessage());
            }
        });
    }



    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
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
