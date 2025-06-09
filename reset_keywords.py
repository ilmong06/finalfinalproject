import shutil
import os

# 경로 설정
paths_to_remove = [
    "data/custom",                        # 키워드 음성 저장 폴더
    "registered_keyword_vectors.json",   # 키워드 벡터 저장 파일
    "fewshot_model.pt",                  # 학습된 모델
    "fewshot_labelmap.json",              # 라벨 매핑
    "registered_speakers.json",
    "registered_speaker.json",
    "label_map.json",
    "keyword_vector.json"
]

for path in paths_to_remove:
    if os.path.isdir(path):
        shutil.rmtree(path)
        print(f"📁 폴더 삭제됨: {path}")
    elif os.path.isfile(path):
        os.remove(path)
        print(f"🗑️ 파일 삭제됨: {path}")
    else:
        print(f"❓ 존재하지 않음: {path}")
