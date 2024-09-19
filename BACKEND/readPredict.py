import pandas as pd
import firebase_admin
from firebase_admin import credentials, firestore
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import KFold

class WinePredictor:
    def __init__(self, firebase_key_path, df):
        # Firebase Admin SDK 초기화
        if not len(firebase_admin._apps):
            cred = credentials.Certificate(firebase_key_path)
            firebase_admin.initialize_app(cred)
        self.db = firestore.client()
        
        # 데이터 읽기
        self.wine_data = df #pd.read_csv(csv_path)
        self.red_wine_data = self.wine_data[self.wine_data['wine_type'] == 'Red wine']
        self.white_wine_data = self.wine_data[self.wine_data['wine_type'] == 'White wine']
        
        # 모델 및 스케일러
        self.model_red = None
        self.model_white = None
        self.scaler_red = None
        self.scaler_white = None
        self.label_encoders = {}
        
           # 모델 학습
        if self._has_reviews():
            self._train_models()
        else:
            print("리뷰 데이터가 없습니다. 모델 학습을 건너뜁니다.")
    
    def _has_reviews(self):
        # Firestore에서 리뷰 데이터가 존재하는지 확인
        email = self._read_email()
        reviews_ref = self.db.collection('review')
        query = reviews_ref.where('email', '==', email).limit(1)
        results = query.stream()
        
        return any(results)  # 데이터가 존재하면 True 반환

    def _train_models(self):
        if not self._check_sample_size(min_samples=10):
            print("샘플 수가 부족하여 모델 학습을 건너뜁니다.")
            return
    
        # Firestore에서 데이터 가져오기
        email = self._read_email()
        data = self._fetch_data_by_email(email)
        attributes = self._get_wine_attributes([entry['index'] for entry in data])
        
        # 데이터 결합
        final_data = self._combine_data(data, attributes)
        
        # 별점 기반 가중치 컬럼 추가
        final_data['weight'] = final_data['rating'] * 1000
        
        # 범주형 데이터 전처리
        self._encode_labels(final_data)
        
        # 데이터와 타겟 분리
        features = final_data.drop(['index', 'rating', 'weight'], axis=1)
        target = final_data['rating']
        weights = final_data['weight']
        
        # 정규화
        self.scaler_red = StandardScaler()
        features_scaled = self.scaler_red.fit_transform(features)
        
        # 모델 정의
        self.model_red = GradientBoostingRegressor(n_estimators=200, learning_rate=0.1, random_state=42)
        self.model_white = GradientBoostingRegressor(n_estimators=200, learning_rate=0.1, random_state=42)

        # 교차 검증 설정
        cv = KFold(n_splits=5, shuffle=True, random_state=42)

        # Red wine 모델 학습 및 평가
        scores_red = cross_val_score(self.model_red, features_scaled, target, cv=cv, scoring='r2', fit_params={'sample_weight': weights})
        print(f"Red wine 모델 교차 검증 성능 (R^2 scores): {scores_red}")
        print(f"Red wine 모델 평균 교차 검증 성능 (R^2 score): {scores_red.mean()}")

        # White wine 모델 학습 및 평가
        scores_white = cross_val_score(self.model_white, features_scaled, target, cv=cv, scoring='r2', fit_params={'sample_weight': weights})
        print(f"White wine 모델 교차 검증 성능 (R^2 scores): {scores_white}")
        print(f"White wine 모델 평균 교차 검증 성능 (R^2 score): {scores_white.mean()}")

        # 전체 데이터로 모델 학습
        self.model_red.fit(features_scaled, target, sample_weight=weights)
        self.model_white.fit(features_scaled, target, sample_weight=weights)
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

    def preprocess_input_data(self, body, texture, sweetness, acidity=None, flavor1=None, flavor2=None, flavor3=None):
        # 입력 데이터 전처리
        flavor1_encoded = -1 if flavor1 not in self.label_encoders['flavor1'].classes_ else self.label_encoders['flavor1'].transform([flavor1])[0]
        flavor2_encoded = -1 if flavor2 not in self.label_encoders['flavor2'].classes_ else self.label_encoders['flavor2'].transform([flavor2])[0]
        flavor3_encoded = -1 if flavor3 not in self.label_encoders['flavor3'].classes_ else self.label_encoders['flavor3'].transform([flavor3])[0]
        
        data = {
            'body': [body],
            'texture': [texture],
            'sweetness': [sweetness],
            'flavor1': [flavor1_encoded],
            'flavor2': [flavor2_encoded],
            'flavor3': [flavor3_encoded]
        }
        
        if acidity is not None:
            data['acidity'] = [acidity]
        
        # 화이트 와인에서 acidity가 없는 경우
        return pd.DataFrame({
            'body': [body],
            'texture': [texture],
            'sweetness': [sweetness],
            'acidity': [acidity if acidity is not None else -1],  # Default to -1 if acidity is None
            'flavor1': [flavor1_encoded],
            'flavor2': [flavor2_encoded],
            'flavor3': [flavor3_encoded]
        })

    def predict_red_wine_score(self, body, texture, sweetness, acidity, flavor1, flavor2, flavor3):
        if not self._check_sample_size(min_samples=10):
            print("샘플 수가 부족하여 예측을 건너뜁니다.")
            return None
        
        # 입력 데이터 전처리
        input_data = self.preprocess_input_data(body, texture, sweetness, acidity, flavor1, flavor2, flavor3)
        
        # 데이터 정규화
        if self.scaler_red:
            input_data_scaled = self.scaler_red.transform(input_data)
        else:
            input_data_scaled = input_data  # No scaling if scaler is not available
        
        # 예측
        predicted_score = self.model_red.predict(input_data_scaled)
        
        return predicted_score[0]

    def predict_white_wine_score(self, body, texture, sweetness, flavor1, flavor2, flavor3):
        if not self._check_sample_size(min_samples=10):
            print("샘플 수가 부족하여 예측을 건너뜁니다.")
            return None
        # 입력 데이터 전처리
        input_data = self.preprocess_input_data(body, texture, sweetness, None, flavor1, flavor2, flavor3)
        
        # 데이터 정규화
        if self.scaler_white:
            input_data_scaled = self.scaler_white.transform(input_data)
        else:
            input_data_scaled = input_data  # No scaling if scaler is not available
        
        # 예측
        predicted_score = self.model_white.predict(input_data_scaled)
        
        return predicted_score[0]
        
    def _check_sample_size(self, min_samples=10):
        # Firestore에서 데이터 가져오기
        email = self._read_email()
        data = self._fetch_data_by_email(email)
        
        # 샘플 수 체크
        return len(data) >= min_samples