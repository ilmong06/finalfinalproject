package com.example.wav2vecapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 사용자의 현재 위치를 가져오고,
 * 위도/경도를 서버로 전송하는 기능을 담당하는 도우미 클래스
 */
public class LocationHelper {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // 액티비티 및 UI 출력용 컴포넌트
    private final Activity activity;
    private final TextView textView;
    private final TextView textRegisterStep;

    // FusedLocationProviderClient : 현재 위치 요청 처리 객체
    private final FusedLocationProviderClient fusedLocationClient;

    // 사용자 고유 식별자
    private final String uuid;

    /**
     * 생성자
     * @param activity 호출 액티비티
     * @param textView 결과 메시지 표시용 TextView
     * @param textRegisterStep 위치 요청 상태 메시지 표시용
     * @param uuid 사용자 고유 ID
     */
    public LocationHelper(Activity activity, TextView textView, TextView textRegisterStep, String uuid) {
        this.activity = activity;
        this.textView = textView;
        this.textRegisterStep = textRegisterStep;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        this.uuid = uuid;
    }

    /**
     * 위치 권한 요청 함수
     * 권한이 없으면 사용자에게 요청, 있으면 위치 바로 요청
     */
    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            showCurrentLocation();
        }
    }

    /**
     * 위치 권한 요청 결과를 받는 콜백 함수
     */
    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation();
            } else {
                textView.setText("❌ 위치 권한이 거부되었습니다.");
            }
        }
    }

    /**
     * 현재 위치를 요청하고 서버로 전송하는 함수
     */
    private void showCurrentLocation() {
        Log.i("LocationHelper", "🔍 showCurrentLocation 호출됨");

        // 권한 체크 (최종 안전망)
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            textView.setText("❗ 위치 권한이 없습니다.");
            return;
        }

        // 위치 요청 설정
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // 고정밀 위치
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setNumUpdates(1);  // 한 번만 응답 받음

        // 위치 결과 콜백 처리
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    textView.setText("❌ 위치를 가져올 수 없습니다. 다시 시도하세요.");
                    textRegisterStep.setText("📡 위치 요청 실패");
                    return;
                }

                // 위치 가져오기 성공
                Location location = locationResult.getLastLocation();
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                String locationText = "📍 현재 위치\n위도: " + lat + "\n경도: " + lon;

                Log.i("LocationHelper", "✅ 위치 획득: " + locationText);
                textView.setText(locationText);
                textRegisterStep.setText(locationText);

                // 서버 전송
                sendGpsToServer(lat, lon);


            }
        };

        // 위치 업데이트 요청
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, activity.getMainLooper());
    }

    /**
     * 위도, 경도를 서버로 전송하는 함수
     */
    private void sendGpsToServer(double latitude, double longitude) {
        GpsRequest gpsRequest = new GpsRequest(uuid, latitude, longitude);
        ApiService apiService = RetrofitClient.getApiService();

        apiService.sendGpsData(gpsRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("LocationHelper", "✅ 위치 서버 전송 성공");
                } else {
                    Log.e("LocationHelper", "❌ 위치 서버 전송 실패 (응답 오류)");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("LocationHelper", "❌ 위치 서버 전송 실패: " + t.getMessage());
            }
        });
    }
}

