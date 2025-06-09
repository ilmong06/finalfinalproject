# routes/UiRoute/report_route.py

from flask import Blueprint, request, jsonify
from routes.Appservice import report_service

report_bp = Blueprint('report', __name__)

@report_bp.route('/get_reports', methods=['GET'])
def get_reports():
    try:
        result = report_service.get_reports_with_keywords()
        return jsonify(result), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500