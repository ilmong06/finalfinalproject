import numpy as np
from Mysqldb import models
from datetime import datetime

# 🔵 키워드 등록
def register_keyword(uuid, keyword_text, order):
    now = datetime.now()

    # 1. DB에 저장
    models.insert_keyword(uuid, keyword_text, int(order), now)

    # ✅ JSON 저장, 벡터 생성 등은 제거됨

    return {"result": "success", "keyword": keyword_text}
