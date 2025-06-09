from flask import Flask,render_template_string
from flask_cors import CORS

from routes.UiRoute.user_info_route import user_info_bp #userinforoute
from routes.UiRoute.secure_route import secure_bp
from routes.UiRoute.register_route import register_bp
from routes.UiRoute.keyword_route import keyword_bp
from routes.UiRoute.location_route import location_bp
from routes.UiRoute.report_route import report_bp
from routes.UiRoute.voice_route import voice_bp
from routes.UiRoute.main_route import main_bp

from routes.UiRoute.user_check_route import user_check_bp



# ✅ Android layout 디렉토리 경로 지정
ANDROID_LAYOUT_PATH = "C/Users/nick_kim/Android/coke/app/src/main/res/layout"


app = Flask(__name__)
CORS(app)

# Blueprint 등록
app.register_blueprint(user_info_bp, url_prefix='/api')
app.register_blueprint(secure_bp, url_prefix="/api")
app.register_blueprint(keyword_bp, url_prefix="/api")
app.register_blueprint(register_bp, url_prefix="/api")
app.register_blueprint(location_bp, url_prefix='/api')
app.register_blueprint(report_bp, url_prefix='/api')
app.register_blueprint(voice_bp, url_prefix='/api')
app.register_blueprint(main_bp, url_prefix='/api')



app.register_blueprint(user_check_bp, url_prefix="/")

#xml 파일 못읽었을시 예외처리 <- 필수는 아님
def render_xml_file(filename):
    try:
        full_path = f"{ANDROID_LAYOUT_PATH}/{filename}"
        with open(full_path, "r", encoding="utf-8") as f:
            content = f.read()
        # HTML에서 < >가 깨지지 않도록 escape 처리
        return render_template_string("<pre>{{ content }}</pre>", content=content)
    except FileNotFoundError:
        return f"파일 {filename}을 찾을 수 없습니다.", 404

#activity_main.xml 앱 메인 화면 레이아웃
@app.route('/')
def mainpage():
    return render_xml_file("activity_main.xml")

#activity_keyword.xml 사용자 정의 키워드 등록 및 녹음 화면
@app.route('/keyword')
def insertkeyword():
    return render_xml_file("activity_keyword.xml")

#activity_userinfo.xml 사용자 정보 입력 화면
@app.route('/userinfo')
def inserttable():
    return render_xml_file("activity_userinfo.xml")

#activity_voice.xml 사용자 음성 등록 화면
@app.route('/voice')
def insertvoice():
    return render_xml_file("activity_voice.xml")

#사용자 음성 삭제 팝업창 음성데이터를 삭제하겠습니까? 예/아니요
@app.route('/voice/voice_delete')
def deletevoice_pop():
    return render_xml_file("activity_delete_confirm.xml")

#음성 데이터 등록하는 팝업창
@app.route('/voice/voice_insert')
def insertvoice_pop():

    return render_xml_file("activity_voice_popup.xml")

#사용자 정보조회(이름,전화번호 입력)
@app.route('/access')
def ac_mypage():
    return render_xml_file("activity_access_mypage.xml")

#access한 사용자 정보 조회, 수정(userinfo랑 동일)
@app.route('/mypage')
def fix_mypage():
    return render_xml_file("activity_mypage.xml")


#신고기록 확인
@app.route('/report_item')
def report_item():
    return render_xml_file("activity_report_item.xml")

#신고이력 조회화면 
@app.route('/history')
def history():
    return render_xml_file("activity_history.xml")

#관리자 전용 기능 화면으로 이동
@app.route('/admin_page')
def admin_page():
    return render_xml_file("activity_admin.xml")

#admin report 조회
@app.route('/admin_histories')
def admin_histories():
    return render_xml_file("admin_histories.xml")

#admin keyword table 조회
@app.route('/admin_keywords')
def admin_keywords():
    return render_xml_file("admin_keywords.xml")

#admin em_phnum table 조회
@app.route('/admin_parents')
def admin_parents():
    return render_xml_file("admin_parents.xml")

#admin userinfo table 조회
@app.route('/admin_userinfo')
def admin_userinfo():
    return render_xml_file("admin_userinfo.xml")


if __name__ == '__main__':
    
    print("\n[현재 등록된 라우트 목록]")
    for rule in app.url_map.iter_rules():
        print(f"{rule.methods} -> {rule}")
    
    app.run(host='0.0.0.0', port=5001, debug=True)


