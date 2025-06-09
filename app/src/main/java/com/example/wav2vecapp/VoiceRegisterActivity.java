package com.example.wav2vecapp;

import android.app.Dialog;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoiceRegisterActivity extends AppCompatActivity {

    private Button btnBack, btnStartRecording, btnDeleteRecording;
    private TextView tvKeywordGuide;


    private SharedPreferences sharedPreferences;
    private String uuid;
    private List<String> keywordList = new ArrayList<>();
    private boolean isRecording = false;
    private int recordCount = 0;
    private List<File> recordedFiles = new ArrayList<>();
    private AudioRecord audioRecord;
    private Thread recordingThread;
    private File wavFile;
    private static final int SAMPLE_RATE = 16000;


    private SharedPreferences sharedPreferences;
    private String uuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);


        // UUID 가져오기

        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        uuid = sharedPreferences.getString("uuid", "");
        Log.d("UUID", "📌 UUID 불러오기 결과: " + uuid);

        // UI 연결
        tvKeywordGuide = findViewById(R.id.tvKeywordGuide);
        btnBack = findViewById(R.id.btnBack);
        btnStartRecording = findViewById(R.id.btnRecord);
        btnDeleteRecording = findViewById(R.id.btnDelete);




        btnBack.setOnClickListener(v -> finish());
        btnStartRecording.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
                btnStartRecording.setText("녹음 중지");
            } else {
                stopRecording();
                btnStartRecording.setText("녹음 시작");
            }
        });
    }

    private String getUUIDFromPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("uuid", "");
    }

    private void startRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        byte[] audioData = new byte[bufferSize];
        isRecording = true;

        wavFile = new File(getExternalCacheDir(), "record_" + recordCount + ".wav");

        recordingThread = new Thread(() -> {
            try (FileOutputStream fos = new FileOutputStream(wavFile)) {
                writeWavHeader(fos, SAMPLE_RATE, 1, 16); // WAV 헤더 초기화
                audioRecord.startRecording();

                while (isRecording) {
                    int read = audioRecord.read(audioData, 0, audioData.length);
                    if (read > 0) fos.write(audioData, 0, read);
                }

                updateWavHeader(wavFile); // WAV 헤더 최종 갱신

                runOnUiThread(() -> {
                    recordedFiles.add(wavFile);
                    recordCount++;
                    if (recordCount >= 4) {
                        sendFilesToServer();
                    } else {
                        Toast.makeText(this, "녹음 " + recordCount + "회 완료", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        recordingThread.start();
    }
    private void stopRecording() {
        if (audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;
        }
    }
    private void writeWavHeader(FileOutputStream out, int sampleRate, int channels, int bitsPerSample) throws IOException {
        byte[] header = new byte[44];

        long byteRate = sampleRate * channels * bitsPerSample / 8;

        // ChunkID "RIFF"
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';

        // ChunkSize (파일 크기 - 8) → 임시값 0
        header[4] = 0; header[5] = 0; header[6] = 0; header[7] = 0;

        // Format "WAVE"
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';

        // Subchunk1ID "fmt "
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';

        // Subchunk1Size (PCM은 16)
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;

        // AudioFormat (PCM = 1)
        header[20] = 1; header[21] = 0;

        // NumChannels
        header[22] = (byte) channels; header[23] = 0;

        // SampleRate
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);

        // ByteRate = SampleRate * NumChannels * BitsPerSample / 8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // BlockAlign = NumChannels * BitsPerSample / 8
        int blockAlign = channels * bitsPerSample / 8;
        header[32] = (byte) (blockAlign & 0xff);
        header[33] = (byte) ((blockAlign >> 8) & 0xff);

        // BitsPerSample
        header[34] = (byte) bitsPerSample; header[35] = 0;

        // Subchunk2ID "data"
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';

        // Subchunk2Size (데이터 크기) → 임시값 0
        header[40] = 0; header[41] = 0; header[42] = 0; header[43] = 0;

        out.write(header, 0, 44);
    }


    private void updateWavHeader(File wavFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(wavFile, "rw");

        long totalAudioLen = raf.length() - 44;
        long totalDataLen = totalAudioLen + 36;

        raf.seek(4); // ChunkSize 위치
        raf.write((byte) (totalDataLen & 0xff));
        raf.write((byte) ((totalDataLen >> 8) & 0xff));
        raf.write((byte) ((totalDataLen >> 16) & 0xff));
        raf.write((byte) ((totalDataLen >> 24) & 0xff));

        raf.seek(40); // Subchunk2Size 위치
        raf.write((byte) (totalAudioLen & 0xff));
        raf.write((byte) ((totalAudioLen >> 8) & 0xff));
        raf.write((byte) ((totalAudioLen >> 16) & 0xff));
        raf.write((byte) ((totalAudioLen >> 24) & 0xff));

        raf.close();
    }

    private void sendFilesToServer() {


        for (int i = 0; i < recordedFiles.size(); i++) {
            File file = recordedFiles.get(i);

            RequestBody requestFile = RequestBody.create(file, MediaType.parse("audio/wav"));
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            RequestBody uuidBody = RequestBody.create(MediaType.parse("text/plain"), uuid);
            RequestBody indexBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(i + 1));


            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<ResponseBody> call = apiService.registerVoice(filePart, uuidBody, indexBody);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d("녹음 전송", "성공");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }




}

        // 키워드 불러오기
        loadKeywords(uuid);

        // 버튼 이벤트
        btnBack.setOnClickListener(v -> finish());
        btnStartRecording.setOnClickListener(v -> showRecordStartPopup());
        btnDeleteRecording.setOnClickListener(v -> showRecordDeletePopup());
    }

    private void loadKeywords(String uuid) {
        ApiService apiService = RetrofitClient.getApiService();
        KeywordRequest rq = new KeywordRequest(uuid);

        Call<KeywordResponse> call = apiService.getKeywords(rq);
        call.enqueue(new Callback<KeywordResponse>() {
            @Override
            public void onResponse(Call<KeywordResponse> call, Response<KeywordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> keywords = response.body().getKeywords();
                    StringBuilder guide = new StringBuilder("📌 등록된 키워드 목록:\n");
                    for (String keyword : keywords) {
                        guide.append("• ").append(keyword).append("\n");
                    }
                    tvKeywordGuide.setText(guide.toString());
                } else {
                    tvKeywordGuide.setText("❌ 키워드 불러오기 실패");
                    Log.e("Keyword", "서버 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<KeywordResponse> call, Throwable t) {
                tvKeywordGuide.setText("❌ 네트워크 오류");
                Log.e("Keyword", "API 호출 실패: " + t.getMessage());
            }
        });
    }

    // ======================== 녹음 팝업 관련 ==========================

    private Button btnClose;
    private Button btnRecord, btnC, btnRetry;
    private Button btnFinish;
    private int recordCount = 0;

    private void showRecordStartPopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_voice_popup);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView countText = dialog.findViewById(R.id.tvCount);
        recordCount = 0;
        countText.setText("등록 완료 0/4");

        btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> dialog.dismiss());

        btnRecord = dialog.findViewById(R.id.btnRecord);
        btnRecord.setEnabled(true);
        btnRecord.setAlpha(1.0f);
        btnRecord.setOnClickListener(view -> {
            btnC.setEnabled(true);
            btnC.setAlpha(1.0f);
        });

        btnC = dialog.findViewById(R.id.btnC);
        btnC.setEnabled(false);
        btnC.setAlpha(0.5f);
        btnC.setOnClickListener(view -> {
            if (recordCount < 4) {
                recordCount++;
                countText.setText("등록 완료 " + recordCount + "/4");
            } else {
                Toast.makeText(this, "최대 4개까지 등록 가능합니다.", Toast.LENGTH_SHORT).show();
            }

            btnC.setEnabled(false);
            btnC.setAlpha(0.5f);
        });

        btnRetry = dialog.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(view -> {
            recordCount = 0;
            countText.setText("등록 완료 0/4");
        });

        btnFinish = dialog.findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(view -> dialog.dismiss());

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    // ======================== 삭제 팝업 관련 ==========================

    private Button btnYes, btnNo;

    private void showRecordDeletePopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_delete_confirm);

        btnYes = dialog.findViewById(R.id.btnYes);
        btnYes.setOnClickListener(v -> dialog.dismiss());

        btnNo = dialog.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

