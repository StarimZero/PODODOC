
import pandas as pd
import firebase_admin
from firebase_admin import credentials, firestore
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.ensemble import GradientBoostingRegressor

class WinePredictor:
    def __init__(self, firebase_key_path, csv_path):
        # Firebase Admin SDK 초기화
        if not len(firebase_admin._apps):
            cred = credentials.Certificate(firebase_key_path)
            firebase_admin.initialize_app(cred)
        self.db = firestore.client()
        
        # 데이터 읽기
        self.wine_data = pd.read_csv(csv_path)
        self.red_wine_data = self.wine_data[self.wine_data['wine_type'] == 'Red wine']
        
        # 모델 및 스케일러
        self.model = None
        self.scaler = None
        self.label_encoders = {}
        
        # 모델 학습
        self._train_model()

    def _train_model(self):
        # Firestore에서 데이터 가져오기
        email = self._read_email()
        data = self._fetch_data_by_email(email)
        attributes = self._get_wine_attributes([entry['index'] for entry in data])
        
        # 데이터 결합
        final_data = self._combine_data(data, attributes)
        
        # 별점 기반 가중치 컬럼 추가
        final_data['weight'] = final_data['rating']
        
        # 범주형 데이터 전처리
        self._encode_labels(final_data)
        
        # 데이터와 타겟 분리
        features = final_data.drop(['index', 'rating', 'weight'], axis=1)
        target = final_data['rating']
        weights = final_data['weight']
        
        # 정규화
        self.scaler = StandardScaler()
        features_scaled = self.scaler.fit_transform(features)
        
        # 데이터 분할
        X_train, X_test, y_train, y_test, w_train, w_test = train_test_split(features_scaled, target, weights, test_size=0.2, random_state=42)
        
        # 모델 정의 및 학습
        self.model = GradientBoostingRegressor(n_estimators=100, learning_rate=0.1, random_state=42)
        self.model.fit(X_train, y_train, sample_weight=w_train)
        
        # 모델 평가
        score = self.model.score(X_test, y_test)
        print(f"모델 성능 (R^2 score): {score}")

    def _read_email(self):
        # 이메일 읽기
        with open('received_email.txt', 'r') as file:
            email = file.read().strip()
        return email

    def _fetch_data_by_email(self, email):
        # 이메일로 데이터 조회하는 함수 정의
        reviews_ref = self.db.collection('review')
        query = reviews_ref.where('email', '==', email)
        results = query.stream()
        
        data = []
        for doc in results:
            doc_data = doc.to_dict()
            if 'index' in doc_data and 'rating' in doc_data:
                data.append({
                    'index': doc_data['index'],
                    'rating': doc_data['rating']
                })
        
        return data

    def _get_wine_attributes(self, index_list):
        attributes = []
        for idx in index_list:
            row = self.red_wine_data[self.red_wine_data['index'] == idx]
            if not row.empty:
                attributes.append({
                    'index': idx,
                    'body': row['body'].values[0],
                    'texture': row['texture'].values[0],
                    'sweetness': row['sweetness'].values[0],
                    'acidity': row['acidity'].values[0],
                    'flavor1': row['flavor1'].values[0],
                    'flavor2': row['flavor2'].values[0],
                    'flavor3': row['flavor3'].values[0]
                })
        return attributes

    def _combine_data(self, data, attributes):
        final_data = []
        for entry in data:
            index = entry['index']
            rating = entry['rating']
            attribute_data = next((attr for attr in attributes if attr['index'] == index), None)
            if attribute_data:
                final_data.append({
                    'index': index,
                    'rating': rating,
                    'body': attribute_data['body'],
                    'texture': attribute_data['texture'],
                    'sweetness': attribute_data['sweetness'],
                    'acidity': attribute_data['acidity'],
                    'flavor1': attribute_data['flavor1'],
                    'flavor2': attribute_data['flavor2'],
                    'flavor3': attribute_data['flavor3']
                })
        return pd.DataFrame(final_data)

    def _encode_labels(self, df):
        for col in ['flavor1', 'flavor2', 'flavor3']:
            le = LabelEncoder()
            df[col] = le.fit_transform(df[col])
            self.label_encoders[col] = le

    def preprocess_input_data(self, body, texture, sweetness, acidity, flavor1, flavor2, flavor3):
        # 입력 데이터 전처리
        flavor1_encoded = -1 if flavor1 not in self.label_encoders['flavor1'].classes_ else self.label_encoders['flavor1'].transform([flavor1])[0]
        flavor2_encoded = -1 if flavor2 not in self.label_encoders['flavor2'].classes_ else self.label_encoders['flavor2'].transform([flavor2])[0]
        flavor3_encoded = -1 if flavor3 not in self.label_encoders['flavor3'].classes_ else self.label_encoders['flavor3'].transform([flavor3])[0]
        
        return pd.DataFrame({
            'body': [body],
            'texture': [texture],
            'sweetness': [sweetness],
            'acidity': [acidity],
            'flavor1': [flavor1_encoded],
            'flavor2': [flavor2_encoded],
            'flavor3': [flavor3_encoded]
        })

    def predict_wine_score(self, body, texture, sweetness, acidity, flavor1, flavor2, flavor3):
        # 입력 데이터 전처리
        input_data = self.preprocess_input_data(body, texture, sweetness, acidity, flavor1, flavor2, flavor3)
        
        # 데이터 정규화
        input_data_scaled = self.scaler.transform(input_data)
        
        # 예측
        predicted_score = self.model.predict(input_data_scaled)
        
        return predicted_score[0]
