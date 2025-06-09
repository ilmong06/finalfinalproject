import os
os.environ["SPEECHBRAIN_LOCAL_FILE_STRATEGY"] = "copy"
import pymysql
from datetime import datetime
from flask import Flask, request, jsonify
from flask_cors import CORS
from speechbrain.pretrained import SpeakerRecognition
from vosk import Model as VoskModel, KaldiRecognizer
from pydub import AudioSegment
from pathlib import Path
import torchaudio
import torch.nn as nn
import torch
import uuid
import json
import traceback
import subprocess
import numpy as np
import matplotlib.pyplot as plt
import wave
from g2pk import G2p
g2p = G2p()

os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "google_stt_key.json"
from google.cloud import speech

def get_connection():
    return pymysql.connect(
        host="localhost",
        user="root",
        password="",  # 비밀번호 없는 경우
        database="endproject",
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor
    )
# ✅ MatchboxNet 인코더 임포트
from train_matchboxnet_protonet import MatchboxNetEncoder

# ✅ Flask 설정
app = Flask(__name__)
CORS(app)


# ✅ Vosk 모델 로드
vosk_model = VoskModel("model")


def segment_waveform(waveform, sample_rate=16000, segment_ms=250  ):
    segment_samples = int(sample_rate * segment_ms / 1000  )
    segments = []
    for i in range(0, waveform.shape[1], segment_samples):
        chunk = waveform[:, i:i+segment_samples]
        if chunk.shape[1] == segment_samples:
            energy = chunk.pow(2).mean().item()
            if energy > 1e-8:
                segments.append(chunk)
    return segments

# ✅ MatchboxNet 모델 로드
matchbox_model = MatchboxNetEncoder()
if os.path.exists("matchbox_model.pt"):
    state = torch.load("matchbox_model.pt", map_location="cpu")
    matchbox_model.load_state_dict(state["model"])
    matchbox_model.eval()
    print("✅ matchbox_model.pt 로드 완료")
else:
    print("⚠️ matchbox_model.pt 없음 → 키워드 등록만 가능")

# ✅ 화자 인식 모델
speaker_model = SpeakerRecognition.from_hparams(source="speechbrain/spkrec-ecapa-voxceleb")
TEMP_VECTORS_FILE = "registered_vectors.json"
FINAL_VECTOR_FILE = "registered_speaker.json"

# ✅ 화자 등록
@app.route("/register", methods=["POST"])
def register_speaker():
    if "file" not in request.files:
        return jsonify({"error": "No file part"}), 400
    file = request.files["file"]
    if file.filename == "":
        return jsonify({"error": "No selected file"}), 400

    temp_filename = f"register_{uuid.uuid4().hex}.wav"
    file.save(temp_filename)

    try:
        audio = AudioSegment.from_file(temp_filename).set_frame_rate(16000).set_channels(1)
        audio.export(temp_filename, format="wav")
        waveform, sr = torchaudio.load(temp_filename)
        embedding = speaker_model.encode_batch(waveform).squeeze().numpy()
        norm_vector = embedding / np.linalg.norm(embedding)

        vectors = []
        if os.path.exists(TEMP_VECTORS_FILE):
            with open(TEMP_VECTORS_FILE, "r") as f:
                vectors = json.load(f)
        vectors.append(norm_vector.tolist())
        with open(TEMP_VECTORS_FILE, "w") as f:
            json.dump(vectors, f)

        if len(vectors) == 4:
            vectors_np = np.array(vectors)
            mean_vector = np.mean(vectors_np, axis=0)
            final_vector = mean_vector / np.linalg.norm(mean_vector)
            with open(FINAL_VECTOR_FILE, "w") as f:
                json.dump(final_vector.tolist(), f)
            os.remove(TEMP_VECTORS_FILE)
            return jsonify({"message": "화자 등록 완료 (4/4)"})
        else:
            return jsonify({"message": f"등록 {len(vectors)}/4 완료"})

    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        os.remove(temp_filename)

@app.route("/register_keyword", methods=["POST"])
def register_keyword():
    raw_keyword = request.form.get("keyword")
    uuid_value = request.form.get("uuid")
    order_value = request.form.get("order")


    # ✅ UUID는 파일명 또는 요청에서 받아야 함 (예: Android 앱에서 함께 전송)
    uuid_value = request.form.get("uuid")
    if not uuid_value:
        return jsonify({"error": "UUID 누락"}), 400

    try:
        conn = get_connection()
        with conn.cursor() as cursor:
            cursor.execute("""
                SELECT keywd_text FROM keyword WHERE uuid = %s ORDER BY keywd_order ASC
            """, (uuid_value,))
            rows = cursor.fetchall()
        keyword_list = [row["keywd_text"] for row in rows]
    except Exception as db_err:
        return jsonify({"error": f"키워드 DB 조회 실패: {str(db_err)}"}), 500

    if not keyword_list:
        return jsonify({"error": "해당 사용자의 등록 키워드 없음"}), 403
    # ✅ 키워드 등록 성공 메시지 반환
    return jsonify({"message": "키워드 확인 완료 ✅", "keywords": keyword_list}), 200


    if not raw_keyword or not uuid_value or not order_value:
        return jsonify({"error": "키워드, UUID, 순번 누락"}), 400

    # ✅ 등록된 키워드를 파일로 저장 (예: keywords.txt)
    keyword_file = "keywords.txt"
    keywords = []
    if os.path.exists(keyword_file):
        with open(keyword_file, "r", encoding="utf-8") as f:
            keywords = f.read().splitlines()

    if raw_keyword not in keywords:
        keywords.append(raw_keyword)
        keywords.append(g2p(raw_keyword).replace(" ", ""))  # 발음형도 같이 저장
        with open(keyword_file, "w", encoding="utf-8") as f:
            f.write("\n".join(keywords))

    # ✅ MySQL DB에 저장
    try:
        conn = get_connection()
        with conn.cursor() as cursor:
            sql = """
                INSERT INTO keyword (uuid, keywd_text, keywd_order, add_date)
                VALUES (%s, %s, %s, %s)
            """
            cursor.execute(sql, (uuid_value, raw_keyword, int(order_value), datetime.now()))
        conn.commit()
    except Exception as e:
        return jsonify({"error": f"MySQL 저장 실패: {str(e)}"}), 500
    finally:
        conn.close()

    return jsonify({"message": f"{raw_keyword} 키워드 등록 완료 ✅"}), 200


# ✅ STT + 화자 + 키워드 텍스트 인증
@app.route("/stt", methods=["POST"])
def transcribe():
    if "file" not in request.files:
        return jsonify({"error": "No file part"}), 400
    file = request.files["file"]
    if file.filename == "":
        return jsonify({"error": "No selected file"}), 400


    uuid_value = request.form.get("uuid")
    if not uuid_value:
        return jsonify({"error": "UUID 누락"}), 400


    temp_filename = f"temp_{uuid.uuid4().hex}.wav"
    file.save(temp_filename)

    try:

        # ✅ 음성 전처리

        audio = AudioSegment.from_file(temp_filename).set_frame_rate(16000).set_channels(1)
        audio.export(temp_filename, format="wav")
        waveform, sr = torchaudio.load(temp_filename)


        # ✅ 화자 인증

        # 🔐 화자 인증

        speaker_embedding = speaker_model.encode_batch(waveform)
        speaker_vector = speaker_embedding.squeeze().numpy()
        norm_vector = speaker_vector / np.linalg.norm(speaker_vector)

        if not os.path.exists(FINAL_VECTOR_FILE):
            return jsonify({"error": "등록된 화자가 없습니다"}), 403
        with open(FINAL_VECTOR_FILE, "r") as f:
            registered_vector = np.array(json.load(f)).flatten()

        sim_sp = float(np.dot(norm_vector, registered_vector))
        print(f"[DEBUG] 🔐 화자 유사도: {sim_sp:.4f}")


        # ✅ Google STT

        # ✅ Google STT로 텍스트 추출

        try:
            client = speech.SpeechClient()
            with open(temp_filename, "rb") as audio_file:
                content = audio_file.read()
            audio_g = speech.RecognitionAudio(content=content)
            config = speech.RecognitionConfig(
                encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
                sample_rate_hertz=16000,
                language_code="ko-KR",
            )
            response = client.recognize(config=config, audio=audio_g)
            transcript = " ".join(result.alternatives[0].transcript for result in response.results)
            print(f"[DEBUG] 🗣️ Google STT 결과: {transcript}")
        except Exception as stt_err:
            transcript = ""
            print(f"[ERROR] Google STT 실패: {stt_err}")

        # ✅ 발음 변환
        phonetic_transcript = g2p(transcript).replace(" ", "")
        print(f"[DEBUG] 🔤 변환된 발음: {phonetic_transcript}")


        # ✅ 키워드 DB에서 가져오기
        try:
            conn = get_connection()
            with conn.cursor() as cursor:
                cursor.execute("""
                    SELECT keywd_text FROM keyword WHERE uuid = %s ORDER BY keywd_order ASC
                """, (uuid_value,))
                rows = cursor.fetchall()
            keyword_list = [row["keywd_text"] for row in rows]
        except Exception as db_err:
            return jsonify({"error": f"키워드 DB 조회 실패: {str(db_err)}"}), 500

        if not keyword_list:
            return jsonify({"error": "해당 사용자의 등록 키워드 없음"}), 403

        # ✅ 키워드 매칭
        matched_keyword = None
        sim_kw = 0.0
        match_type = ""

        # ✅ 키워드 포함 여부만으로 판별
        keyword_file = "keywords.txt"
        if not os.path.exists(keyword_file):
            return jsonify({"error": "등록된 키워드 없음"}), 403

        with open(keyword_file, "r", encoding="utf-8") as f:
            keyword_list = f.read().splitlines()


        for keyword in keyword_list:
            original_keyword = keyword
            g2p_keyword = g2p(keyword).replace(" ", "")


            if original_keyword in transcript:
                matched_keyword = original_keyword
                sim_kw = 1.0
                match_type = "원본 텍스트 포함"
                break
            elif g2p_keyword in transcript.replace(" ", ""):
                matched_keyword = original_keyword
                sim_kw = 1.0
                match_type = "g2p 키워드가 텍스트에 포함"
                break
            elif original_keyword in phonetic_transcript:
                matched_keyword = original_keyword
                sim_kw = 1.0
                match_type = "텍스트가 키워드에 포함"
                break
            elif g2p_keyword in phonetic_transcript:
                matched_keyword = original_keyword
                sim_kw = 1.0
                match_type = "g2p 기준 음소 일치"
                break

        # ✅ 디버그 로그
        if matched_keyword:
            print(f"[DEBUG] ✅ 키워드 매칭 성공 → '{matched_keyword}' | 방식: {match_type} | 유사도: {sim_kw:.4f}")
        else:
            print("[DEBUG] ❌ 키워드 일치 실패")
        print(f"[DEBUG] 📌 UUID: {uuid_value}")
        print(f"[DEBUG] 🗣️ 전체 텍스트: {transcript}")
        print(f"[DEBUG] 🔍 등록된 키워드 목록: {keyword_list}")

            if (original_keyword in transcript or
                g2p_keyword in transcript.replace(" ", "") or
                original_keyword in phonetic_transcript or
                g2p_keyword in phonetic_transcript):
                matched_keyword = original_keyword
                sim_kw = 1.0
                break


        print(f"[DEBUG] 🔍 키워드 유사도: {sim_kw:.4f}")



        if not matched_keyword:
            return jsonify({
                "error": "키워드 인증 실패",
                "triggered_keyword": None,
                "similarity": sim_kw,
                "text": transcript
            }), 403


        # ✅ 최종 결과 반환
        return jsonify({
            "uuid": uuid_value,
            "text": transcript.strip(),
            "speaker_similarity": round(sim_sp, 4),
            "triggered_keyword": matched_keyword,
            "triggered_keyword_g2p": g2p(matched_keyword).replace(" ", "") if matched_keyword else None,
            "keyword_similarity": sim_kw,
            "s_total": round(sim_sp + sim_kw, 4),
            "registered_keywords": keyword_list

        return jsonify({
            "text": transcript.strip(),
            "speaker_similarity": round(sim_sp, 4),
            "triggered_keyword": matched_keyword,
            "triggered_keyword_g2p": g2p(matched_keyword).replace(" ", ""),
            "keyword_similarity": sim_kw,
            "s_total": round(sim_sp + sim_kw, 4)

        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500
    finally:
        if os.path.exists(temp_filename):
            try:
                os.remove(temp_filename)
            except Exception as e:
                print("[WARN] 파일 삭제 실패:", e)


import requests

@app.route("/train_from_voice_db", methods=["POST"])
def train_from_voice_db():
    try:
        uuid_value = request.form.get("uuid")
        if not uuid_value:
            return jsonify({"error": "UUID 누락"}), 400

        # ✅ DB 연결 후 keyword_list 조회 (사용자 UUID 기준)
        try:
            conn = get_connection()
            with conn.cursor() as cursor:
                cursor.execute("SELECT keywd_text FROM keyword WHERE uuid = %s", (uuid_value,))
                rows = cursor.fetchall()
            keyword_list = [row["keywd_text"] for row in rows]
        except Exception as e:
            return jsonify({"error": "키워드 DB 조회 실패"}), 500

        if not keyword_list:
            return jsonify({"error": "등록된 키워드가 없습니다"}), 403

        # ✅ keyword_id, keyword_text 조회 (사용자 테이블의 selected_keyword 기준이 아닌 전체 키워드 중 첫 번째 사용)
        keyword_text = keyword_list[0]  # 예: 첫 번째 키워드 사용
        conn = get_connection()
        with conn.cursor() as cursor:
            cursor.execute("""
                SELECT id FROM keyword
                WHERE uuid = %s AND keywd_text = %s
            """, (uuid_value, keyword_text))
            row = cursor.fetchone()
            if not row:
                return jsonify({"error": "해당 키워드가 DB에 없음"}), 404
            keyword_id = row["id"]

            # ✅ 해당 키워드에 대한 음성 파일 경로 조회
            cursor.execute("""
                SELECT voice_path
                FROM voice
                WHERE uuid = %s AND voice_index IS NOT NULL
                ORDER BY voice_index ASC
                LIMIT 4
            """, (uuid_value,))
            voice_rows = cursor.fetchall()

        if len(voice_rows) < 4:
            return jsonify({"error": "음성 데이터가 4개 미만입니다."}), 400

        BASE_DIR = r"C:\Users\user\Wav2Vec2_Android_Java_Final\server"

        # ✅ 1. 키워드 등록 요청
        data = {
            "uuid": uuid_value,
            "keyword": keyword_text,
            "order": 1
        }
        res_kw = requests.post("http://127.0.0.1:5000/register_keyword", data=data)
        print(f"[INFO] 키워드 등록 응답: {res_kw.json()}")

        # ✅ 2. 4개의 음성 파일을 /register로 순차 전송
        for i, row in enumerate(voice_rows):
            voice_path = os.path.join(BASE_DIR, row["voice_path"])
            if not os.path.exists(voice_path):
                return jsonify({"error": f"파일 없음: {voice_path}"}), 400

            with open(voice_path, 'rb') as f:
                files = {'file': f}
                res = requests.post("http://127.0.0.1:5000/register", files=files)
                print(f"[INFO] 등록 요청 {i+1}/4 → 응답: {res.json()}")

        return jsonify({"message": "화자 + 키워드 등록 완료 ✅"}), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500



# ✅ 실행
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
