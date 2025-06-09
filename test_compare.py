# compare_request.py
import requests
import os

def get_base_url():
    with open("secrets.properties", "r") as f:
        for line in f:
            if line.startswith("FLASK_BASE_URL"):
                return line.strip().split("=")[1].rstrip("/")
    raise ValueError("FLASK_BASE_URL not found in secrets.properties")

BASE_URL = get_base_url()

def send_compare_request(compare_path):
    url = f"{BASE_URL}/stt"
    with open(compare_path, 'rb') as f:
        files = {'file': (os.path.basename(compare_path), f, 'audio/wav')}
        response = requests.post(url, files=files)
        print(f"[비교 요청] {compare_path} -> {response.status_code}")
        try:
            print(response.json())
        except Exception:
            print("\u26a0 JSON 파싱 실패. 응답 본문:\n", response.text)

if __name__ == "__main__":
    compare_file = "test_audio/compare.wav"
    send_compare_request(compare_file)
