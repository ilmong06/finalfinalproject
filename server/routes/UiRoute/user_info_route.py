from flask import Blueprint, request, jsonify
from Mysqldb import models
import uuid
import jwt
from datetime import datetime,timedelta

SECRET_KEY = "your-secret-key"  # TODO: 보안 상 .env로 분리 권장

user_info_bp = Blueprint('user_info', __name__)

#JWT토큰 생성
def generate_token(uuid):
    payload = {
        "uuid": uuid,
        "exp": datetime.utcnow() + timedelta(days=7)  # 유효기간 7일
    }
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")



#유저 정보 입력값을 2개 테이블로 분배해 저장
@user_info_bp.route('/userinfo', methods=['POST'])

#사용자입력 정보저장
def save_user_info():
    data = request.get_json()

    # UserInfoActivity.java에서 오는 key에 맞춰 파싱
    name = data.get('name')
    phnum = data.get('phnum')
    birthdate = data.get('fullBirth')  # yyyyMMdd
    gender_text = data.get('gender')   # "남자", "여자", "기타"
    em_name = data.get('emergencyName')
    em_phnum = data.get('emergencyPhone')
    em_parent = data.get('relation')
    # language는 무시

    # gender 텍스트를 코드로 변환
    gender_map = {"남자": "1", "여자": "2"}
    gender_code = gender_map.get(gender_text, "3")  # 기타는 3

    # 필수 값 확인
    if not all([name, phnum, birthdate, gender_code, em_name, em_phnum, em_parent]):
        return jsonify({"error": "입력값 누락"}), 400

    try:
        connection = models.get_connection()
        cur = connection.cursor()

        # ✅ 사용자 UUID 생성
        user_uuid = str(uuid.uuid4())
        reg_date = datetime.now()

        # userinfo 테이블 저장
        insert_user_sql = """
            INSERT INTO userinfo (uuid, Name, PhNum, birthdate, gender, reg_date)
            VALUES (%s, %s, %s, %s, %s, %s)
        """
        cur.execute(insert_user_sql, (user_uuid, name, phnum, birthdate, gender_code, reg_date))

        # Em_noPhNum 테이블 저장
        insert_guardian_sql = """
            INSERT INTO Em_noPhNum (PhNum, uuid, Em_Name, Em_PhNum, Em_parent, reg_date)
            VALUES (%s, %s, %s, %s, %s, %s)
        """
        cur.execute(insert_guardian_sql, (phnum, user_uuid, em_name, em_phnum, em_parent, reg_date))

        connection.commit()
        
        # ✅ 토큰 발행
        token = generate_token(user_uuid)
        if isinstance(token,bytes):
            token=token.decode('utf-8')
        
        return jsonify({
            "message": " 사용자 및 보호자 정보 저장 완료",
            "uuid": user_uuid,
            "token":token}), 201
    
    except Exception as e:
        connection.rollback()
        return jsonify({"error": f"❌ DB 오류: {str(e)}"}), 500

    finally:
        cur.close()
        connection.close()