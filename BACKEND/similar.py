import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.metrics.pairwise import euclidean_distances
import re

def find_similar_wines_from_name(target_index):
    # 데이터 로딩
    df = pd.read_csv('data/Clean_Red_data.csv')

    def find_wine_name(target_index):
        return df.loc[df['index'] == target_index, 'wine_name'].values[0]
    target_wine_name= find_wine_name(target_index)
    
    # 평점 가중치 계산
    m = df['wine_reviews'].min()
    C = df['wine_rating'].mean()
    
    def weighted_rating(x, m=m, C=C):
        v = x['wine_reviews']
        R = x['wine_rating']
        return (v/(v+m) * R) + (m/(v+m) * C)
    
    df['point'] = df.apply(weighted_rating, axis=1)

    # 정규화 (0에서 1 사이로 변환)
    df_features = df[['flavor1', 'flavor2', 'flavor3', 'body', 'texture', 'sweetness', 'acidity']].copy()
    for column in ['body', 'texture', 'sweetness', 'acidity']:
        df_features[column] = df_features[column] / 100.0

    # ColumnTransformer 정의: 원-핫 인코딩 및 표준화 적용
    ct = ColumnTransformer(
        transformers=[
            ('encoder', OneHotEncoder(drop='first'), ['flavor1', 'flavor2', 'flavor3']),  # 범주형 데이터 원-핫 인코딩
            ('scaler', StandardScaler(), ['body', 'texture', 'sweetness', 'acidity'])  # 숫자형 데이터 표준화
        ],
        remainder='passthrough'
    )

    # 변환 적용
    X = ct.fit_transform(df_features)

    def calculate_distance_with_flavors(df, target_index, ct, columns):
        # 모든 feature를 인코딩 및 스케일링된 형태로 변환
        df_encoded = ct.transform(df[columns])
        
        # 특정 와인 벡터
        target_vector = df_encoded[target_index, :].reshape(1, -1)
        
        # 데이터 벡터
        data_vectors = df_encoded
        
        # 유클리디안 거리 계산
        distance_scores = euclidean_distances(target_vector, data_vectors)
        
        # 결과 반환
        return distance_scores.flatten()

    def find_wine_index(df, wine_name):
        return df[df['wine_name'] == wine_name].index[0]

    def remove_year_from_name(wine_name):
        """ 와인 이름에서 연도나 숫자 부분을 제거합니다. """
        return re.sub(r'\d+', '', wine_name).strip()

    def filter_unique_wine_names(df):
        """ 와인 이름에서 숫자 부분을 제거하고, 고유한 와인 이름만 필터링합니다. """
        # 와인 이름에서 숫자 부분을 제거한 새로운 컬럼 생성
        df.loc[:, 'wine_name_clean'] = df['wine_name'].apply(remove_year_from_name)
        
        # 중복 와인 이름 제거 (최소 거리 와인만 남기기)
        df_sorted_filtered = df.sort_values(by='distance', ascending=True)
        df_unique = df_sorted_filtered.drop_duplicates(subset='wine_name_clean', keep='first')
        
        # 불필요한 컬럼 삭제
        df_unique = df_unique.drop(columns=['wine_name_clean'])
        
        return df_unique

    columns = ['flavor1', 'flavor2', 'flavor3', 'body', 'texture', 'sweetness', 'acidity']
    target_index = find_wine_index(df, target_wine_name)
    distances = calculate_distance_with_flavors(df, target_index, ct, columns)
    
    # 유사도와 데이터프레임 결합 (유클리디안 거리이므로 거리 값이 작은 순서가 유사도 높은 것)
    df['distance'] = distances
    
    # 거리 값이 작은 순으로 정렬
    df_sorted = df.sort_values(by='distance', ascending=True)
    
    # `point`가 4점 이상인 와인만 필터링
    df_sorted_filtered = df_sorted[df_sorted['point'] >= 4]

    # 중복된 와인 이름 제거
    df_unique = filter_unique_wine_names(df_sorted_filtered)
    
    # # 필요한 컬럼만 선택
    # df_unique = df_unique[['wine_name', 'wine_price', 'wine_country', 'point', 'distance']]
    
    return df_unique.to_dict(orient='records')

# 실행 예
# target_index = 152
# target_wine_name = 'Pauillac 1970'
# df_sorted = find_similar_wines_from_name(target_index)

# print("거리 순으로 정렬된 데이터프레임 (중복 제거된 와인 이름):")
# print(df_sorted[1:11])  # 자기 자신을 제외한 유사도가 높은(거리 값이 낮은) 상위 10개 와인 출력
