import requests
from flask import Blueprint, request, jsonify
from Mysqldb import models
from datetime import datetime
from routes.Appservice import report_service
from routes.Appservice import location_service

<<<<<<< HEAD
=======

>>>>>>> 93056c2 (addserver)
location_bp = Blueprint('location', __name__)

# 🔵 긴급 위치 저장
@location_bp.route('/location', methods=['POST'])
def location():
    try:
        uuid = request.form['uuid']
        latitude = request.form['latitude']
        longitude = request.form['longitude']

        if not uuid or not latitude or not longitude:
            return jsonify({"error": "Missing data"}), 400

        # DB에 위치 저장
        models.save_location(uuid, latitude, longitude, datetime.now())

        print(f"🚨 긴급 위치 수신: UUID={uuid}, 위도={latitude}, 경도={longitude}")

        return jsonify({"message": "Location saved successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500




#위도,경도,상세주소 변환 저장    
@location_bp.route('/send_gps', methods=['POST'])
def send_gps():
    try:
        data = request.get_json()
        uuid = data.get('uuid')
        lat = data.get('latitude')
        lng = data.get('longitude')

        if not uuid or lat is None or lng is None:
            return jsonify({"error": "필수값 누락"}), 400

<<<<<<< HEAD
        address = get_address_from_kakao(lat, lng)  # ✅ 주소 변환
=======
        address = location_service.get_address_from_kakao(lat, lng)  # ✅ 주소 변환
>>>>>>> 93056c2 (addserver)

        # DB 저장
        result = report_service.save_location(uuid, lat, lng, address)

        return jsonify({"message": "위치 저장 성공", "address": address}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

