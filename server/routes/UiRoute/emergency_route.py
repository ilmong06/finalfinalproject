from flask import Blueprint, jsonify

emergency_bp = Blueprint('emergency', __name__)

@emergency_bp.route('/send_alert', methods=['POST'])
def send_alert():
    # 보호자에게 문자 전송 (구현 예정)
    return jsonify({'message': '긴급 문자 전송 완료'})