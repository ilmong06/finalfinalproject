package com.example.wav2vecapp;

public class UserInfo {

    public String uuid;
    public String name;
    public String phnum;
    public String language;
    public String birthdate;
    public String gender;
    public String emergency_name;
    public String emergency_phnum;
    public String emergency_relation;

    public UserInfo(String uuid, String name, String phnum, String language, String birthdate, String gender,
                    String emergency_name, String emergency_phnum, String emergency_relation) {
        this.uuid = uuid;
        this.name = name;
        this.phnum = phnum;
        this.language = language;
        this.birthdate = birthdate;
        this.gender = gender;
        this.emergency_name = emergency_name;
        this.emergency_phnum = emergency_phnum;
        this.emergency_relation = emergency_relation;
    }


    // 서버 응답 처리용 내부 클래스 (필요할 경우)
    public static class UserInfoResponse {
        private String uuid;
        private String name;  // Name → name (서버 JSON 키 대소문자와 일치)

        private String phnum;

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public String getPhnum() {
            return phnum;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPhnum(String phnum) {
            this.phnum = phnum;
        }
    }

}
