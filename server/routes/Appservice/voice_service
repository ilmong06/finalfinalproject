from Mysqldb.models import get_connection
import os
from datetime import datetime

# ✅ 음성 파일 저장 후 DB에 경로 등록
def save_voice_file(user_uuid, file_path):
    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            sql = """
                UPDATE userinfo
                SET voicedt = %s, reg_date = %s
                WHERE uuid = %s
            """
            cursor.execute(sql, (file_path, datetime.now(), user_uuid))
        connection.commit()
    finally:
        connection.close()
