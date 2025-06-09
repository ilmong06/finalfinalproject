from flask import Blueprint, request, jsonify
from Mysqldb.models import check_user_exists

# Blueprint 정의
user_check_bp = Blueprint('user_check', __name__)

# ✅ 사용자 존재 여부 확인 API
@user_check_bp.route("/user/check", methods=["GET"])
def check_user():
    try:
        name = request.args.get("name")
        phone = request.args.get("phone")

        if not name or not phone:
            return jsonify({"error": "이름 또는 전화번호가 누락되었습니다."}), 400

        # DB 조회
        user = check_user_exists(name, phone)  # 이미 models.py에 정의되어 있음

        return jsonify({
            
            "exists": True,
            "uuid": user['uuid']
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500
