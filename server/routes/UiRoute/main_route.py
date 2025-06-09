from flask import Blueprint, request, jsonify
from flask_cors import CORS
import pymysql
import os
import requests
import traceback  # ✅ 추가

main_bp = Blueprint('main', __name__)
CORS(main_bp)

# ✅ DB 연결 함수
def get_connection():
    return pymysql.connect(
        host="localhost",
        user="root",
        password="",
        database="endproject",
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor
    )

# ✅ /upload_voice 라우트
@main_bp.route("/upload_voice", methods=["POST"])
def upload_voice():
    if 'file' not in request.files:
        return jsonify({"error": "파일이 포함되어야 합니다."}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "파일명이 비어 있습니다."}), 400

    # 저장 경로 설정
    save_dir = "compare"
    os.makedirs(save_dir, exist_ok=True)
    save_path = os.path.join(save_dir, "compare.wav")
    file.save(save_path)

    # ✅ STT 서버로 compare.wav 전송
    try:
        stt_url = "http://192.168.219.105:5000/stt"
        with open(save_path, 'rb') as f:
            files = {'file': f}
            data = {'uuid': request.form.get("uuid")}  # 클라이언트에서 uuid도 함께 전송해야 함
            response = requests.post(stt_url, files=files, data=data)

        if response.status_code != 200:
            print("🛑 STT 서버 응답 실패 코드:", response.status_code)
            print("📦 응답 본문:", response.text)
            return jsonify({"error": "STT 요청 실패", "status": response.status_code}), 500

        stt_result = response.json()
        return jsonify({
            "message": "compare.wav 저장 완료 및 STT 요청 성공",
            "stt_result": stt_result
        }), 200

    except Exception as e:
        print("❌ [upload_voice 예외 발생]")
        traceback.print_exc()  # ✅ 전체 traceback 출력
        return jsonify({"error": str(e)}), 500
