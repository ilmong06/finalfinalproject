
CREATE DATABASE endproject;
use endproject;
DROP database endproject;

create table userinfo ( #사용자 테이블
   uuid varchar(36) primary key, #주요키는 uuid 자동생성 36자까지로 되어있음
   name varchar(8), #사용자이름
   phnum varchar(11), # 사용자연락처  ex 01023899074
   birthdate varchar(10), # 사용자주민등록번호 앞자리 6자리 뒷자리 하나 1) 6자리 입력 2) 앞 두자리로 연도 확인 3) 20 or 19 +6자리 4) 8자리 반환
   gender varchar(2),  #사용자성별 male, female 둘중하나
   voicedt varchar(255), #음성데이터 파일[voicedata]로uploads 폴더에 저장해 디렉토리로 불러오기
   reg_date datetime #사용자 등록날짜
);

ALTER TABLE userinfo
ADD COLUMN selected_keyword INT;


CREATE TABLE keyword ( #키워드 테이블
    	id INT AUTO_INCREMENT PRIMARY KEY, #주요키는 입력된 순서로 표기
    	uuid VARCHAR(36), #userinfo에서 가져온 uuid그대로 적용
    	keywd_text VARCHAR(20), #키워드 
		keywd_order INT, #키워드들를 번호로 구분해 따로 저장
    	add_date DATETIME, #키워드 추가한 날짜
    	FOREIGN KEY (uuid) REFERENCES userinfo(uuid) #외래키로 userinfo(uuid) 참조해 그대로 사용

);


create table Em_noPhNum( #보호자정보 테이블

   phnum varchar(11) primary key, #주요키는 사용자연락처
   uuid VARCHAR(36), #userinfo에서 가져온 uuid그대로 적용
   em_name varchar(8), #보호자성명
   em_phnum varchar(11), #보호자 연락처
   em_parent varchar(8), #보호자 관계 ex 부,모, 할아버지 등등 혹시몰라 8자까지

   reg_date DATETIME, #보호자 정보 추가날짜
   FOREIGN KEY (uuid) REFERENCES userinfo(uuid) #외래키로 userinfo(uuid) 참조해 그대로 사용
);

CREATE TABLE ReportGPS ( #신고기록, 주소 테이블
       id INT AUTO_INCREMENT PRIMARY KEY, #순번대로 정리해 기록을 최신순으로 불러오기
       uuid VARCHAR(36), #해당기능을 사용한 사용자의 고유uid
       latitude DOUBLE, #위도
    longitude DOUBLE, #경도
    address VARCHAR(255),  #상세주소
       report_time DATETIME, #신고한 날짜
       FOREIGN KEY (uuid) REFERENCES userinfo(uuid) #외래키로 userinfo(uuid) 참조해 그대로 사용
);
CREATE TABLE voice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    keyword_id INT,
    uuid VARCHAR(36),
    voice_index INT,
    voice_path VARCHAR(255),
    reg_date DATETIME,
    FOREIGN KEY (keyword_id) REFERENCES keyword(id),
    FOREIGN KEY (uuid) REFERENCES userinfo(uuid)
);

	PhNum varchar(11) primary key, #주요키는 사용자연락처 
	uuid VARCHAR(36), #userinfo에서 가져온 uuid그대로 적용
	Em_Name varchar(8), #보호자성명
	Em_PhNum varchar(11), #보호자 연락처
	Em_parent varchar(8), #보호자 관계 ex 부,모, 할아버지 등등 혹시몰라 8자까지

	reg_date DATETIME, #보호자 정보 추가날짜
	FOREIGN KEY (uuid) REFERENCES userinfo(uuid) #외래키로 userinfo(uuid) 참조해 그대로 사용
);


select * from userinfo where Name="최동욱";
select * from em_nophnum where Em_Name="최재민";

select * from userinfo;
select * from keyword;
select * from em_nophnum;
select * from ReportGPS;

DROP TABLE IF EXISTS userinfo;
DROP TABLE IF EXISTS ReportGPS;
DROP TABLE IF EXISTS Em_noPhNum;
DROP TABLE IF EXISTS keyword;


INSERT INTO userinfo (
    uuid, Name, PhNum, birthdate, gender, voicedt, reg_date
) VALUES (
    UUID(),
    '이순신',
    '01098765432',
    '01010101',
    'male',
    'uploads/voicedata/example.wav',
    NOW()
);

SELECT
    u.uuid,
    u.Name AS 사용자이름,
    u.PhNum AS 사용자번호,

    e.Em_Name AS 보호자이름,
    e.Em_PhNum AS 보호자번호,
    e.Em_parent AS 보호자관계,
    e.reg_date AS 보호자등록일,

    k.keywd_text AS 키워드,
    k.keywd_order AS 키워드순서,
    k.add_date AS 키워드등록일
FROM userinfo u
LEFT JOIN Em_noPhNum e ON u.uuid = e.uuid
LEFT JOIN keyword k ON u.uuid = k.uuid
WHERE u.Name = 'tt';
 #사용자와 보호자 정보를 함께 조회하는 SQL


INSERT INTO Em_noPhNum (
    PhNum,
    uuid,
    Em_Name,
    Em_PhNum,
    Em_parent,
    reg_date
) VALUES (
    '01012345678',  -- 사용자 번호
    'ab3ab66a-41cd-11f0-9a15-d8f8837c2602',  -- userinfo 테이블에 존재하는 uuid
    '최재민',       -- 보호자 성명
    '01098765432',  -- 보호자 연락처
    '부',           -- 보호자 관계
    NOW()           -- 현재 날짜/시간
);#  보호자 정보 임의 등록 예시

