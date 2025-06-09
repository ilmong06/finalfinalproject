import requests

#kakao api를 이용한 상세주소변환
def get_address_from_kakao(lat, lng):
    try:
        KAKAO_REST_API_KEY = "e0e121735e6baa9a63e6c58abb0d9f64"  # 카카오 api키
        headers = {"Authorization": f"KakaoAK {KAKAO_REST_API_KEY}"}
        url = f"https://dapi.kakao.com/v2/local/geo/coord2address.json?x={lng}&y={lat}"

        response = requests.get(url, headers=headers)
        result = response.json()

        if "documents" in result and len(result["documents"]) > 0:
            return result["documents"][0]["address"]["address_name"]
        else:
            return "주소 정보 없음"
    except Exception as e:
        return f"주소 변환 오류: {str(e)}"
