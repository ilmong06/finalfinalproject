from flask import Blueprint, jsonify, request
from functools import wraps
import jwt
import os

secure_bp = Blueprint('secure', __name__)

SECRET_KEY = os.getenv("SECRET_KEY", "fallback-secret")  # 실제는 .env에서 읽어야 함

# ✅ 인증 데코레이터 정의
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization', '').replace('Bearer ', '')
        if not token:
            return jsonify({"error": "토큰 없음"}), 401
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
            request.user_uuid = payload["uuid"]
        except jwt.ExpiredSignatureError:
            return jsonify({"error": "토큰 만료"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"error": "유효하지 않은 토큰"}), 401
        return f(*args, **kwargs)
    return decorated

# ✅ 토큰 검증이 필요한 라우트
@secure_bp.route("/secure", methods=["GET"])
@token_required
def secure_area():
    return jsonify({"message": "안전구역 접근 성공", "uuid": request.user_uuid})

@secure_bp.route("/get_reports", methods=["GET"])
@token_required
def get_reports_route():
    uuid = request.user_uuid
    start = request.args.get("start_date")
    end = request.args.get("end_date")
    keyword = request.args.get("keyword")

    reports = get_reports(uuid, start, end, keyword)
    return jsonify({"reports": reports})

