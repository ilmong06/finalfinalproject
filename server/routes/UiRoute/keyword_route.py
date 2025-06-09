from flask import Blueprint, request, jsonify
from routes.Appservice import keyword_service
from routes.DataControl import validator
from Mysqldb.models import get_connection

keyword_bp = Blueprint('keyword', __name__)

@keyword_bp.route('/register_keyword', methods=['POST'])
def register_keyword_route():
    print("📡 POST /register_keyword 요청 수신됨")

    print("✅ request.content_type:", request.content_type)
    print("✅ request.form:", request.form)
    print("✅ request.values:", request.values)
    print("✅ raw data:", request.get_data())

    try:
        uuid = request.form.get("uuid") or request.values.get("uuid")
        keyword = request.form.get("keyword") or request.values.get("keyword")
        order = request.form.get("order") or request.values.get("order")

        print("📥 uuid:", uuid)
        print("📥 keyword:", keyword)
        print("📥 order:", order)

        if not uuid or not keyword or not order:
            return jsonify({"error": "Missing data"}), 400

        result = keyword_service.register_keyword(uuid, keyword, order)
        return jsonify(result), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500



@keyword_bp.route('/get_keywords', methods=['POST'])
def get_keywords():
    data = request.get_json()
    uuid = data.get("uuid")
    if not uuid:
        return jsonify({"error": "UUID 누락"}), 400

    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            # ✅ 필요한 모든 컬럼 선택
            cursor.execute("""
                SELECT keywd_text, add_date, keywd_order
                FROM keyword
                WHERE uuid = %s
                ORDER BY keywd_order
            """, (uuid,))
            rows = cursor.fetchall()

            # ✅ 전체 JSON 배열 형태로 응답 구성
            keywords = []
            for row in rows:
                keywords.append({
                    "keywd_text": row["keywd_text"],
                    "add_date": row["add_date"].strftime("%Y-%m-%d") if isinstance(row["add_date"], (str, type(None))) == False else row["add_date"],
                    "keywd_order": row["keywd_order"]
                })

            return jsonify(keywords), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        conn.close()
        
#키워드 삭제
@keyword_bp.route('/delete_keywords', methods=['POST'])
def delete_keywords():
    data = request.get_json()
    uuid = data.get("uuid")
    keywords = data.get("keywords")

    if not uuid or not keywords:
        return jsonify({"error": "Invalid data"}), 400

    try:
        connection = get_connection()
        with connection.cursor() as cursor:
            for kw in keywords:
                cursor.execute("DELETE FROM Keyword WHERE uuid = %s AND keywd_text = %s", (uuid, kw))
        connection.commit()
        return jsonify({"message": "Keywords deleted successfully"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
