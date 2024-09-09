from flask import Flask, request, jsonify, json, Response, send_file
import pandas as pd
from mainPage import get_basic_red_wines, get_basic_white_wines
from readPage import get_data, chart
from search import search_wine
from reviewView import save_map_to_html
from readPredict import WinePredictor
from similar import find_similar_wines_from_name

app = Flask(__name__)

@app.route('/')
def index():
    return '나는영훈'

@app.route('/wine/list.json')
def wine():
    try:
        args = request.args
        page = int(args.get('page', 1))
        start = (page - 1) * 10
        end = page * 10

        df = pd.read_csv('data/Combined_Wine_Data.csv')
        total = len(df)

        df = df[start:end]
        wine_list = df.to_json(orient='records')
        wine_list = json.loads(wine_list)

        data = {'total': total, 'list': wine_list}
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/wine/basicred.json')
def basicred():
    try:
        args = request.args
        page = int(args.get('page', 1))
        price_range = args.get('price', 'all')

        data = get_basic_red_wines(page, price_range)
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/wine/basicwhite.json')
def basicwhite():
    try:
        args = request.args
        page = int(args.get('page', 1))
        price_range = args.get('price', 'all')

        data = get_basic_white_wines(page, price_range)
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/wine/<int:index>')
def read(index):
    try:
        data = get_data(index)
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/image/<int:index>')
def get_image(index):
    try:
        buf = chart(index)
        return Response(buf, mimetype='image/png')
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/search', methods=['GET'])
def search():
    try:
        query = request.args.get('query', '')
        page = int(request.args.get('page', 1))
        size = int(request.args.get('size', 10))

        if not query:
            return jsonify({'error': '검색어를 입력하세요'}), 400

        results = search_wine(query, page, size)
        return jsonify(results)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/receive-email', methods=['POST'])
def receive_email():
    try:
        data = request.get_json()

        if 'email' not in data:
            return jsonify({'error': 'No email field in request'}), 400

        email = data['email']
        print(f"받은 이메일: {email}")
        
        # 이메일을 파일에 저장
        with open('received_email.txt', 'w') as file:
            file.write(email)

        return jsonify({'status': 'success', 'email': email}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/map', methods=['GET'])
def map():
    # 이메일과 CSV 파일 경로 설정
    email_file_path = 'received_email.txt'
    csv_path = 'data/Combined_Wine_Data.csv'
    output_html_path = 'folium_map.html'

    # HTML 파일로 지도를 저장
    save_map_to_html(email_file_path, csv_path, output_html_path)

    # HTML 파일을 클라이언트에게 전달
    return send_file(output_html_path)



# Firebase와 CSV 파일 경로 설정
firebase_key_path = 'C:/pododoc/key/kosmo-96bbe-60906db745e9.json'
csv_path = 'data/Combined_Wine_Data.csv'

# WinePredictor 인스턴스 초기화
predictor = WinePredictor(firebase_key_path, csv_path)

@app.route('/predict', methods=['GET'])
def predict():
    # 요청 데이터에서 index 추출
    index = request.args.get('index')

    # 로그 추가: 요청 데이터 출력
    print(f"받은 인덱스번호: {index}")
    
    if not index:
        return jsonify({'error': 'Index parameter is required'}), 400
    
    try:
        index = int(index)  # index를 정수로 변환
        
        # Combined_Wine_Data.csv 파일에서 해당 index에 맞는 데이터 추출
        wine_data = pd.read_csv(csv_path)
        wine_row = wine_data[wine_data['index'] == index]
        
        if wine_row.empty:
            return jsonify({'error': 'Index not found in data'}), 404
        
        # 와인 데이터에서 속성 추출
        body = wine_row['body'].values[0]
        texture = wine_row['texture'].values[0]
        sweetness = wine_row['sweetness'].values[0]
        flavor1 = wine_row['flavor1'].values[0]
        flavor2 = wine_row['flavor2'].values[0]
        flavor3 = wine_row['flavor3'].values[0]
        acidity = wine_row['acidity'].values[0]

        # 예측 수행
        score = predictor.predict_wine_score(body, texture, sweetness, acidity, flavor1, flavor2, flavor3)

        # 로그 추가: 예측 결과 출력
        print(f"예측점수 {index}: {score}")
        
        # 결과 반환
        return jsonify({'predicted_score': score})
    
    except ValueError:
        return jsonify({'error': 'Invalid index value'}), 400






@app.route('/similar/<int:index>')
def similar(index):
    df_sorted = find_similar_wines_from_name(index)
    return jsonify(df_sorted[1:6])

#이메일을 받아서 
if __name__ == '__main__':
    app.run(port=5000, debug=True, host='192.168.0.11')
