import os
import json
import numpy as np

# 모델 저장 경로
SPEAKER_VECTOR_FILE = 'model/registered_speaker.json'

# 🔵 화자 등록
def register_speaker(uuid, files):
    """
    사용자가 화자 음성 파일 6개를 등록
    - uuid : 사용자 고유 ID
    - files : 화자 음성 녹음 파일들 (6개)
    """
    speaker_vectors = []

    for file in files:
        # 실제로는 여기에 모델 인퍼런스 추가해야 함
        dummy_vector = np.random.rand(512)  # 512차원 dummy
        speaker_vectors.append(dummy_vector)

    speaker_avg_vector = np.mean(speaker_vectors, axis=0)

    # 저장
    if os.path.exists(SPEAKER_VECTOR_FILE):
        with open(SPEAKER_VECTOR_FILE, 'r') as f:
            data = json.load(f)
    else:
        data = {}

    data[uuid] = speaker_avg_vector.tolist()

    with open(SPEAKER_VECTOR_FILE, 'w') as f:
        json.dump(data, f)
