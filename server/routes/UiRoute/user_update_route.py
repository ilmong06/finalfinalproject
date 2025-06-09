from flask import Blueprint, request, jsonify
from Mysqldb import models
from datetime import datetime, timedelta
import jwt

user_info_bp = Blueprint('user_info', __name__)
SECRET_KEY = "your-secret-key"  # ⚠️ 실제 서비스에서는 .env 파일을 통해 관리 권장


# 🔐 JWT 토큰 생성 함수
def generate_token(uuid):
    payload = {
        "uuid": uuid,
        "exp": datetime.utcnow() + timedelta(days=7)
    }
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")

@user_info_bp.route('/api/update_userinfo', methods=['POST'])
def update_user_info():
    data = request.get_json()

    # 🔹 필드 수신
    uuid_val = data.get("uuid")
    name = data.get("name")
    phnum = data.get("phnum")
    birthdate = data.get("birthdate")  # ✅ 추가
    gender = data.get("gender")
    em_name = data.get("em_name")
    em_phnum = data.get("em_phnum")    # ✅ 수정
    em_relation = data.get("em_relation")  # ✅ 수정

    if not all([uuid_val, name, phnum, birthdate, gender, em_name, em_phnum, em_relation]):
        return jsonify({"error": "입력값 누락"}), 400

    try:
        connection = models.get_connection()
        cursor = connection.cursor()

        # 🔸 userinfo 수정
        update_user_sql = """
            UPDATE userinfo
            SET Name = %s, PhNum = %s, birthdate = %s, gender = %s
            WHERE uuid = %s
        """
        cursor.execute(update_user_sql, (name, phnum, birthdate, gender, uuid_val))

        # 🔸 보호자 정보 수정
        update_em_sql = """
            UPDATE Em_noPhNum
            SET Em_Name = %s, Em_PhNum = %s, Em_parent = %s
            WHERE uuid = %s
        """
        cursor.execute(update_em_sql, (em_name, em_phnum, em_relation, uuid_val))

        connection.commit()

        # 🔸 토큰 재발급
        token = generate_token(uuid_val)
        if isinstance(token, bytes):
            token = token.decode("utf-8")

        return jsonify({
            "message": "사용자 정보 수정 완료",
            "token": token
        }), 200

    except Exception as e:
        connection.rollback()
        return jsonify({"error": f"❌ DB 오류: {str(e)}"}), 500

    finally:
        cursor.close()
        connection.close()
