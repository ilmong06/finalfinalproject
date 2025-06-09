from Mysqldb import models
from routes.DataControl import validator
from datetime import datetime

# 🔵 사용자 등록 함수
def register_user(uuid, name, phnum, birthdate, gender,
                  emergency_name, emergency_phnum, emergency_relation, language):
    """
    새로운 사용자 + 보호자 등록
    - 사용자: uuid, name, phnum, birthdate, gender
    - 보호자: emergency_name, emergency_phnum, emergency_relation
    """

    # 1. 전화번호 형식 검사
    if not validator.is_valid_phnum(phnum):
        raise ValueError("전화번호는 11자리 숫자여야 합니다.")

    # 2. 전화번호 중복 검사
    if validator.is_duplicate_phnum(phnum):
        raise ValueError("이미 등록된 전화번호입니다.")

    # 3. DB에 사용자 정보 저장
    now = datetime.now()
    models.insert_user(uuid, name, phnum, birthdate, gender, now)

    # 4. DB에 보호자 정보 저장
    models.insert_guardian(phnum, uuid, emergency_name, emergency_phnum, emergency_relation, now)

    return {"message": "사용자 및 보호자 등록 성공"}
