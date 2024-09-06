import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd

def initialize_firebase():
    """Initialize Firebase Admin SDK."""
    # Firebase 인증서 파일 경로
    cred = credentials.Certificate('C:/pododoc/key/kosmo-96bbe-60906db745e9.json')
    firebase_admin.initialize_app(cred)

def fetch_review_indexes(email):
    """Fetch document IDs from Firestore where email matches."""
    # Firestore 인스턴스 가져오기
    db = firestore.client()
    
    # 'review' 컬렉션 참조
    collection_ref = db.collection('review')
    
    # 특정 이메일을 가진 문서들 조회
    query = collection_ref.where('email', '==', email).stream()
    
    # 문서 ID 추출
    indexes = [doc.id for doc in query]
    
    return indexes

def load_csv(file_path):
    """Load CSV file and return DataFrame."""
    return pd.read_csv(file_path)

def count_wind_country_occurrences(df, indexes):
    """Count occurrences of wind_country based on indexes."""
    # DataFrame에서 'index' 컬럼이 indexes에 있는 행만 필터링
    filtered_df = df[df['index'].isin(indexes)]
    
    # 'wind_country' 컬럼의 값들에 대한 빈도수 계산
    wind_country_counts = filtered_df['wind_country'].value_counts()
    
    return wind_country_counts

if __name__ == '__main__':
    initialize_firebase()
    
    # Firestore에서 이메일로 문서 ID를 가져옴
    email = 'blue@test.com'
    review_indexes = fetch_review_indexes(email)
    
    # CSV 파일 로드
    csv_file_path = 'C:/pododoc/Combined_Wine_Data.csv'
    wine_df = load_csv(csv_file_path)
    
    # wind_country 빈도수 계산
    wind_country_occurrences = count_wind_country_occurrences(wine_df, review_indexes)
    
    # 결과 출력
    print(wind_country_occurrences)
