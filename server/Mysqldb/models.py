import pymysql

# MySQL DB 연결 함수
def get_connection():
    return pymysql.connect(
        host='localhost',
        user='root',
        password='',
        database='endproject',
        cursorclass=pymysql.cursors.DictCursor
    )

# ✅ 중복 사용자 확인 (이름 + 전화번호 기준)
def check_user_exists(name, phnum):
    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT COUNT(*) AS cnt FROM userinfo WHERE Name = %s AND Phnum = %s

            """, (name, phnum))
            result = cursor.fetchone()
            return result['cnt'] > 0
    finally:
        connection.close()

# ✅ 사용자 정보 삽입 (userinfo 테이블)
def insert_user(uuid, name, phnum, birthdate, gender, reg_date):
    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            sql = """
                INSERT INTO userinfo (uuid, Name, Phnum, birthdate, gender, voicedt, reg_date)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """
            cursor.execute(sql, (uuid, name, phnum, birthdate, gender, None, reg_date))
            connection.commit()
    finally:
        connection.close()

# ✅ 보호자 정보 삽입 (Em_noPhNum 테이블)
def insert_guardian(phnum, uuid, em_name, em_phnum, em_parent, reg_date):
    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            sql = """
                INSERT INTO em_nophnum (Phnum, uuid, em_name, em_phnum, em_parent, reg_date)
                VALUES (%s, %s, %s, %s, %s, %s)
            """
            cursor.execute(sql, (phnum, uuid, em_name, em_phnum, em_parent, reg_date))
            connection.commit()
    finally:
        connection.close()
        
 # ✅ GPS 정보 삽입 (ReportGPS 테이블)
def insert_gps(uuid, latitude, longitude):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            sql = "INSERT INTO ReportGPS (uuid, latitude, longitude) VALUES (%s, %s, %s)"
            cursor.execute(sql, (uuid, latitude, longitude))
        conn.commit()
        return True
    finally:
        conn.close()

# ✅ 키워드 삽입 (keyword 테이블)
def insert_keyword(uuid, keyword, order, add_date):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            sql = """
                INSERT INTO keyword (uuid, keywd_text, keywd_order, add_date)
                VALUES (%s, %s, %s, %s)
            """
            cursor.execute(sql, (uuid, keyword, order, add_date))
        conn.commit()
    finally:
        conn.close()

        

# ✅ UUID로 사용자 조회
def get_user_by_uuid(uuid):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            sql = "SELECT * FROM userinfo WHERE uuid = %s"
            cursor.execute(sql, (uuid,))
            result = cursor.fetchone()
            return result
    finally:
        conn.close()

# ✅ 전화번호로 사용자 조회()
def get_user_by_phnum(phnum):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            sql = "SELECT * FROM userinfo WHERE phnum = %s"
            cursor.execute(sql, (phnum,))
            result = cursor.fetchone()
            return result
    finally:
        conn.close()

# ✅ 신고이력 조회(activity_history.xml)
def get_reports(uuid, start_date=None, end_date=None, keyword=None):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            query = "SELECT report_time, latitude, longitude, address, keyword FROM reportGPS WHERE uuid = %s"
            params = [uuid]

            if start_date:
                query += " AND report_time >= %s"
                params.append(start_date)

            if end_date:
                query += " AND report_time <= %s"
                params.append(end_date)

            if keyword:
                query += " AND keyword LIKE %s"
                params.append(f"%{keyword}%")

            query += " ORDER BY report_time DESC"
            cursor.execute(query, params)
            results = cursor.fetchall()
            return results
    finally:
        conn.close()

# ✅ 사용자 정보 조회 + 보호자 정보 조회(activity_mypage.xml)
def get_user_and_emergency_info(uuid):
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            sql = """
                SELECT 
                    u.name, u.phnum, u.birthdate, u.gender, u.language,
                    e.em_name, e.em_phnum, e.em_relation
                FROM User u
                LEFT JOIN Em_noPhNum e ON u.uuid = e.uuid
                WHERE u.uuid = %s
            """
            cursor.execute(sql, (uuid,))
            return cursor.fetchone()
    finally:
        conn.close()


