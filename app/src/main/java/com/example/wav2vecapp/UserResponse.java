package com.example.wav2vecapp; // ← 패키지 경로는 프로젝트에 맞게 수정

public class UserResponse {
    public boolean exists;

    // 선택: Getter/Setter를 추가하면 IDE 자동완성 및 확장에 유리
    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
