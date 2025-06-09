from flask import Blueprint, request, jsonify
from routes.Appservice import user_service, keyword_service, speaker_service
from routes.DataControl import validator

register_bp = Blueprint('register', __name__)

# 🔵 사용자 등록 (회원가입)
@register_bp.route('/register_user', methods=['POST'])
def register_user():
    try:
        data = request.get_json()

        uuid = data.get('uuid')
        name = data.get('name')
        phnum = data.get('phnum')
        birthdate = data.get('birthdate')
        gender = data.get('gender')
        emergency_name = data.get('emergency_name')
        emergency_phnum = data.get('emergency_phnum')
        emergency_relation = data.get('emergency_relation')
        language = data.get('language')

        # 필수 항목 확인
        if not all([uuid, name, phnum, birthdate, gender]):
            return jsonify({"error": "필수 데이터 누락"}), 400

        # 사용자 + 보호자 정보 등록 처리
        result = user_service.register_user(
            uuid, name, phnum, birthdate, gender,
            emergency_name, emergency_phnum, emergency_relation, language
        )

        return jsonify({
            "message": "사용자 등록 성공",
            "uuid": uuid,
            "token": "your_generated_token"
        }), 200

    except ValueError as ve:
        return jsonify({"error": str(ve)}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# 🔵 키워드 등록
@register_bp.route('/register_keyword', methods=['POST'])
def register_keyword():
    try:
        uuid = request.form['uuid']
        keyword_text = request.form['keyword']
        files = request.files.getlist('files')

        if not uuid or not keyword_text or not files:
            return jsonify({"error": "Missing data"}), 400

        if validator.is_keyword_exist(uuid, keyword_text):
            return jsonify({"error": "Keyword already exists"}), 409

        keyword_service.register_keyword(uuid, keyword_text, files)
        return jsonify({"message": "Keyword registered successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# 🔵 화자 등록
@register_bp.route('/register_speaker', methods=['POST'])
def register_speaker():
    try:
        uuid = request.form['uuid']
        files = request.files.getlist('files')

        if not uuid or not files:
            return jsonify({"error": "Missing data"}), 400

        speaker_service.register_speaker(uuid, files)
        return jsonify({"message": "Speaker registered successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500
