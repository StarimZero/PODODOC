from flask import Flask, request, jsonify, json, Response
import pandas as pd
from mainPage import get_basic_red_wines, get_basic_white_wines
from readPage import get_data, chart
from search import search_wine

app = Flask(__name__)

@app.route('/')
def index():
    return '나는영훈'

@app.route('/wine/list.json')
def wine():
    args = request.args
    page = int(args.get('page'))
    start = (page-1) * 10
    end = page *10 

    df = pd.read_csv('data/Combined_Wine_Data.csv')
    total = len(df)

    df = df[start:end]
    list = df.to_json(orient='records')
    list = json.loads(list)

    data = {'total':total, 'list':list}
    return data

@app.route('/wine/basicred.json')
def basicred():
    args = request.args
    page = int(args.get('page', 1))  # 페이지 파라미터가 없으면 기본값 1로 설정
    price_range = args.get('price', 'all')  # 가격 범위 파라미터

    # mainPage.py에서 정의한 함수 호출
    try:
        data = get_basic_red_wines(page, price_range)
        return jsonify(data)
    except Exception as e:
        # 에러 발생 시 JSON 응답으로 에러 메시지를 반환합니다
        return jsonify({"error": str(e)}), 500
    
@app.route('/wine/basicwhite.json')
def basicwhite():
    args = request.args
    page = int(args.get('page', 1))  # 페이지 파라미터가 없으면 기본값 1로 설정
    price_range = args.get('price', 'all')  # 가격 범위 파라미터

    # mainPage.py에서 정의한 함수 호출
    try:
        data = get_basic_white_wines(page, price_range)
        return jsonify(data)
    except Exception as e:
        # 에러 발생 시 JSON 응답으로 에러 메시지를 반환합니다
        return jsonify({"error": str(e)}), 500
    
@app.route('/wine/<int:index>')
def read(index):
    data= get_data(index)
    return jsonify(data)

@app.route('/image/<int:index>')
def get_image(index):
    buf = chart(index)
    return Response(buf, mimetype='image/png')

@app.route('/search', methods=['GET'])
def search():
    query = request.args.get('query', '')
    page = int(request.args.get('page', 1))
    size = int(request.args.get('size', 10))
    
    if not query:
        return jsonify({'error': '검색어를 입력하세요'}),
    
    # 검색과 페이지네이션
    results = search_wine(query, page, size)
    
    return jsonify(results)

@app.route('/api/receive-email', methods=['POST'])
def receive_email():
    # POST 요청으로부터 JSON 데이터 추출
    data = request.get_json()
    
    if 'email' not in data:
        # 이메일 필드가 없는 경우 에러 응답 반환
        return jsonify({'error': 'No email field in request'}), 400
    
    email = data['email']
    
    # 이메일을 파일에 저장
    with open('received_email.txt', 'w') as file:
        file.write(email)
    
    # 성공 응답 반환
    return jsonify({'status': 'success', 'email': email}), 200

#이메일을 받아서 
if __name__ == '__main__':
    app.run(port=5000, debug=True, host='192.168.0.11')
        
# if __name__ == '__main__':
#     app.run(port=5000, debug=True, host='192.168.0.238')