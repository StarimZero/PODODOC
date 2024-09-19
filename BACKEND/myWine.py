import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd
import numpy as np
import matplotlib
matplotlib.use('Agg')  # GUI 모드가 아닌 이미지 생성 모드로 설정
import matplotlib.pyplot as plt
import io

def initialize_firebase():
    """Firebase Admin SDK 초기화."""
    if not len(firebase_admin._apps):
        cred = credentials.Certificate('kosmo-96bbe-60906db745e9.json')
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
    
    return data if data else []

def filter_wine_data(indices, df):
    """CSV 파일에서 주어진 index를 기준으로 데이터를 필터링합니다."""
    wine_data = df #pd.read_csv('data/Combined_Wine_Data.csv')
    filtered_wine_data = wine_data[wine_data['index'].isin(indices)]
    return filtered_wine_data

def get_combined_data(email, df):
    """Firestore 데이터와 CSV 데이터를 결합하여 반환합니다."""
    """Firestore 데이터와 CSV 데이터를 결합하여 반환합니다."""
    firestore_data = fetch_data_by_email(email)
    
    if not firestore_data:  # Firestore에서 가져온 데이터가 없을 경우 빈 리스트를 반환
        print("Firestore에서 가져온 데이터가 없습니다.")
        return pd.DataFrame()  # 빈 데이터프레임 반환
    
    indices = [entry['index'] for entry in firestore_data]
    filtered_wine_data = filter_wine_data(indices , df)
    
    firestore_df = pd.DataFrame(firestore_data)
    # 필요한 컬럼만 포함시킵니다.
    firestore_df = firestore_df[['index', 'photo', 'rating', 'date']]
    
    # CSV에서 필요한 컬럼만 선택합니다.
    filtered_wine_data = filtered_wine_data[['index', 'wine_country', 'wine_grape', 'wine_name', 'wine_region', 'wine_winery']]
    
    # Firestore 데이터와 CSV 데이터를 결합합니다.
    combined_data = pd.merge(firestore_df, filtered_wine_data, on='index')
    
    # NaN을 None으로 변환
    combined_data = combined_data.where(pd.notnull(combined_data), None)
    
    # rating을 소수점 1자리까지 올림
    combined_data['rating'] = np.ceil(combined_data['rating'] * 10) / 10
    
    return combined_data

def rating_graph(df):
    # 평점별 리뷰 개수를 계산 (rating이 없는 경우 빈 시리즈가 반환됨)
    if 'rating' not in df or df['rating'].dropna().empty:
        # rating 컬럼이 없거나 데이터가 없는 경우
        rating_counts = pd.Series([0] * 11, index=[round(i * 0.5, 1) for i in range(11)])
    else:
        rating_counts = df['rating'].value_counts().sort_index()
    # x축 값 설정 (0부터 5까지 0.5 스텝)
    x_ticks = [round(i * 0.5, 1) for i in range(11)]
    # 평점 분포를 시리즈로 변환하여 x축과 y축 값을 맞춤
    rating_series = pd.Series(index=x_ticks).fillna(0)
    rating_series.update(rating_counts)
    
    fig, ax = plt.subplots(figsize=(14, 10))
    # 막대 그래프 생성
    ax.bar(rating_series.index, rating_series.values, width=0.3, color='orange', edgecolor='black')
    ax.set_xlabel('')
    ax.set_ylabel('')
    # x축 레이블 설정
    ax.set_xticks(x_ticks)
    ax.set_xticklabels([f'{x:.1f}' for x in x_ticks], fontsize=25 , color='white')
    # y축 스텝을 1로 설정
    ax.set_yticks(range(int(rating_series.max()) + 1))
    
    # 레이블과 격자 설정
    ax.grid(False)
    # ax.set_xticks([])
    ax.set_yticklabels([])
    ax.set_facecolor('#121212')
    
    # 스파인 제거
    for spine in ax.spines.values():
        spine.set_visible(False)
    
    # 차트의 여백과 테두리 제거
    fig.patch.set_visible(False)
    ax.patch.set_visible(False)
    
    # 그래프를 메모리 버퍼에 저장
    buf = io.BytesIO()
    plt.savefig(buf, format='png', bbox_inches='tight', pad_inches=0)
    buf.seek(0)
    plt.close()  # 메모리에서 그래프를 지웁니다.

    return buf  # 이미지 데이터 반환

