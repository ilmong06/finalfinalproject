import re
from Mysqldb import models


# 🔵 전화번호 형식 검사
def is_valid_phnum(phnum):
    """
    전화번호가 11자리 숫자인지 확인
    ex) 01012345678
    """
    pattern = re.compile(r'^\d{11}$')
    return bool(pattern.match(phnum))

# 🔵 전화번호 중복 체크
def is_duplicate_phnum(phnum):
    """
    DB에 같은 전화번호가 이미 존재하는지 확인
    """
    existing_user = models.get_user_by_phnum(phnum)
    return existing_user is not None

# 🔵 키워드 중복 체크 (기존 있던 것 유지)
def is_keyword_exist(uuid, keywd_text):
    conn = models.get_connection()
    try:
        with conn.cursor() as cursor:
            sql = "SELECT COUNT(*) AS cnt FROM keyword WHERE uuid = %s AND keywd_text = %s"
            cursor.execute(sql, (uuid, keywd_text))
            result = cursor.fetchone()
            return result['cnt'] > 0
    finally:
        conn.close()
