import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd
import numpy as np

def initialize_firebase():
    """Firebase Admin SDK 초기화."""
    if not len(firebase_admin._apps):
        cred = credentials.Certificate('C:/pododoc/key/kosmo-96bbe-60906db745e9.json')
        firebase_admin.initialize_app(cred)

def fetch_data_by_email(email):
    """Firestore에서 이메일에 기반하여 리뷰 데이터를 가져옵니다."""
    initialize_firebase()
    db = firestore.client()
    reviews_ref = db.collection('review')
    query = reviews_ref.where('email', '==', email)
    results = query.stream()
    
    data = []
    for doc in results:
        doc_data = doc.to_dict()
        if 'index' in doc_data and 'rating' in doc_data and 'date' in doc_data and 'photo' in doc_data:
            data.append({
                'index': doc_data['index'],
                'rating': doc_data['rating'],
                'date': doc_data['date'],
                'photo': doc_data['photo']
            })
    
    return data

def filter_wine_data(indices):
    """CSV 파일에서 주어진 index를 기준으로 데이터를 필터링합니다."""
    wine_data = pd.read_csv('data/Combined_Wine_Data.csv')
    filtered_wine_data = wine_data[wine_data['index'].isin(indices)]
    return filtered_wine_data

def get_combined_data(email):
    """Firestore 데이터와 CSV 데이터를 결합하여 반환합니다."""
    firestore_data = fetch_data_by_email(email)
    indices = [entry['index'] for entry in firestore_data]
    filtered_wine_data = filter_wine_data(indices)
    
    firestore_df = pd.DataFrame(firestore_data)
    # 필요한 컬럼만 포함시킵니다.
    firestore_df = firestore_df[['index', 'photo', 'rating']]
    
    # CSV에서 필요한 컬럼만 선택합니다.
    filtered_wine_data = filtered_wine_data[['index', 'wine_country', 'wine_grape', 'wine_name', 'wine_region', 'wine_winery']]
    
    # Firestore 데이터와 CSV 데이터를 결합합니다.
    combined_data = pd.merge(firestore_df, filtered_wine_data, on='index')
    
    # NaN을 None으로 변환
    combined_data = combined_data.where(pd.notnull(combined_data), None)
    
    # rating을 소수점 1자리까지 올림
    combined_data['rating'] = np.ceil(combined_data['rating'] * 10) / 10
    
    return combined_data