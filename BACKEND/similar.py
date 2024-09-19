import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.metrics.pairwise import euclidean_distances
import re

def find_similar_whites_from_index(target_index,df):
    # 데이터 로딩
    df = df
    #타겟 와인 타입
    target_wine_type = df.loc[df['index'] == target_index, 'wine_type'].values[0]
    if target_wine_type == 'White wine':
        # 전체 화이트 와인 필터링
        df = df[df['wine_type'] == 'White wine']
    else:
        df = df[df['wine_type'] == 'Red wine']
    
    # 평점 가중치 계산
    m = df['wine_reviews'].min()
    C = df['wine_rating'].mean()
    
    def weighted_rating(x, m=m, C=C):
        v = x['wine_reviews']
        R = x['wine_rating']
        return (v/(v+m) * R) + (m/(v+m) * C)
    
    df['point'] = df.apply(weighted_rating, axis=1)
    
    # 'acidity' 컬럼 삭제
    if target_wine_type=='White wine':
        df = df[df['wine_type'] == 'White wine'].drop(columns=['acidity'], inplace=False)

    # 특성 추출
    if target_wine_type == 'White wine':
        columns = ['flavor1', 'flavor2', 'flavor3', 'body', 'texture', 'sweetness']
        df_features = df[columns].copy()
        ct = ColumnTransformer(
        transformers=[
            ('encoder', OneHotEncoder(drop='first'), ['flavor1', 'flavor2', 'flavor3']),
            ('scaler', StandardScaler(), ['body', 'texture', 'sweetness'])
        ],
        remainder='passthrough'
        )
        
        for column in ['body', 'texture', 'sweetness']:
            df_features[column] = df_features[column] / 100.0

        X = ct.fit_transform(df_features)
    else:
        columns = ['flavor1', 'flavor2', 'flavor3', 'body', 'texture', 'sweetness', 'acidity']
        df_features = df[columns].copy()
        ct = ColumnTransformer(
        transformers=[
            ('encoder', OneHotEncoder(drop='first'), ['flavor1', 'flavor2', 'flavor3']),
            ('scaler', StandardScaler(), ['body', 'texture', 'sweetness', 'acidity'])
        ],
        remainder='passthrough'
        )
        
        for column in ['body', 'texture', 'sweetness', 'acidity']:
            df_features[column] = df_features[column] / 100.0

        X = ct.fit_transform(df_features)

    # 거리 계산 및 유사도 측정 로직
    def calculate_distance_with_flavors(df, target_index, ct, columns):
        df_encoded = ct.transform(df[columns])
        target_row = df[df['index'] == target_index]
        if target_row.empty:
            raise ValueError("Target index not found in the filtered data.")
        target_vector = ct.transform(target_row[columns])[0].reshape(1, -1)
        data_vectors = df_encoded
        distance_scores = euclidean_distances(target_vector, data_vectors)
        return distance_scores.flatten()

    # 거리 계산
    distances = calculate_distance_with_flavors(df, target_index, ct, columns)
    
    df['distance'] = distances
    df_sorted = df.sort_values(by='distance', ascending=True)
    df_sorted_filtered = df_sorted[df_sorted['point'] >= 4]

    # 중복된 와인 이름 제거 (와인 이름에서 연도 제거)
    def remove_year_from_name(wine_name):
        return re.sub(r'\d+', '', wine_name).strip()

    df_sorted_filtered['wine_name_clean'] = df_sorted_filtered['wine_name'].apply(remove_year_from_name)
    df_unique = df_sorted_filtered.drop_duplicates(subset='wine_name_clean', keep='first').drop(columns=['wine_name_clean'])
    #df_unique = df_unique[['wine_name', 'wine_price', 'wine_country', 'point', 'distance']]
    return df_unique.to_dict(orient='records')

