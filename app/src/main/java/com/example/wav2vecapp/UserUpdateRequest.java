package com.example.wav2vecapp;

import com.google.gson.annotations.SerializedName;

/**
 * 🔧 사용자 정보 수정 요청을 위한 DTO 클래스
 * - MyPageActivity에서 사용자 정보를 서버에서 주고받을 때 사용
 */
public class UserUpdateRequest {

    @SerializedName("name")
    private String name;             // 사용자 이름

    @SerializedName("phnum")
    private String phnum;            // 사용자 전화번호

    @SerializedName("birthdate")
    private String birthdate;        // 사용자 생년월일 (yyyyMMdd 형식)

    @SerializedName("gender")
    private String gender;           // 사용자 성별 ("남자" 또는 "여자")

    @SerializedName("em_name")
    private String emergencyName;    // 보호자 이름

    @SerializedName("em_phone")
    private String emergencyPhone;   // 보호자 전화번호

    @SerializedName("em_parent")
    private String relation;         // 보호자와의 관계

    // ✅ 생성자: 모든 필드를 초기화하여 서버로 전송
    public UserUpdateRequest(String uuid, String name, String phnum, String birthdate, String gender,
                             String emergencyName, String emergencyPhone, String relation) {
        this.name = name;
        this.phnum = phnum;
        this.birthdate = birthdate;
        this.gender = gender;
        this.emergencyName = emergencyName;
        this.emergencyPhone = emergencyPhone;
        this.relation = relation;
    }

    // ✅ Getter 메서드들: Retrofit에서 JSON 변환에 사용됨

    public String setName() {
        return name;
    }

    public String setPhnum() {
        return phnum;
    }

    public String setBirthdate() {
        return birthdate;
    }

    public String setGender() {
        return gender;
    }

    public String setEmergencyName() {
        return emergencyName;
    }

    public String setEmergencyPhone() {
        return emergencyPhone;
    }

    public String setRelation() {
        return relation;
    }

    // ⚠️ 필요 시 Setter 메서드도 추가 가능 (현재는 불변 객체로 유지)
}
