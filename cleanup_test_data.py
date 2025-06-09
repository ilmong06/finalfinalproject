# cleanup_test_data.py

import os
import shutil

# 삭제 대상 JSON 파일
json_files = [
    "fewshot_model.pt",
    "registered_speaker.json",
    "label_map.json"
]

# 폴더 경로
test_audio_dir = "test_audio"
custom_data_dir = "data/custom"
uploads_dir = "uploads"  # ✅ test_audio_dir처럼 uploads 폴더도 변수로 정의

def delete_file(filepath):
    if os.path.exists(filepath):
        os.remove(filepath)
        print(f"[삭제 완료] {filepath}")
    else:
        print(f"[건너뜸] {filepath} 없음")

def delete_folder_contents(folder_path):
    if not os.path.exists(folder_path):
        print(f"[건너뜸] {folder_path} 없음")
        return
    for root, dirs, files in os.walk(folder_path):
        for file in files:
            file_path = os.path.join(root, file)
            os.remove(file_path)
            print(f"[삭제 완료] {file_path}")

def delete_folder(folder_path):
    if os.path.exists(folder_path):
        shutil.rmtree(folder_path)
        print(f"[폴더 삭제 완료] {folder_path}")
    else:
        print(f"[건너뜸] {folder_path} 없음")

# 실행
if __name__ == "__main__":
    print("📁 JSON 파일 삭제:")
    for jf in json_files:
        delete_file(jf)

    print("\n📁 사용자 키워드 오디오 전체 삭제:")
    delete_folder(custom_data_dir)

    print("\n📁 업로드된 음성 전체 삭제:")
    delete_folder(uploads_dir)  # ✅ uploads 폴더 삭제
