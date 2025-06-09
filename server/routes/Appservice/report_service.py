from Mysqldb.models import get_connection
from Mysqldb import models
from datetime import datetime
import pymysql


# ✅ 신고 이력 조회 서비스 + keywd_text
def get_reports_with_keywords():
    conn = get_connection()
    try:
        with conn.cursor(pymysql.cursors.DictCursor) as cursor:
            sql = '''
                SELECT 
                    r.id, r.uuid, r.latitude, r.longitude, r.address, r.report_time, k.keywd_text
                FROM ReportGPS r
                LEFT JOIN (
                    SELECT k1.uuid, k1.keywd_text
                    FROM keyword k1
                    INNER JOIN (
                        SELECT uuid, MAX(add_date) AS max_date
                        FROM keyword
                        GROUP BY uuid
                    ) k2 ON k1.uuid = k2.uuid AND k1.add_date = k2.max_date
                ) k ON r.uuid = k.uuid
                ORDER BY r.report_time DESC
            '''
            cursor.execute(sql)
            return cursor.fetchall()
    finally:
        conn.close()


def save_location(uuid, lat, lng, address):
    return models.insert_location_with_address(uuid, lat, lng, address, datetime.now())
