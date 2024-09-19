import pandas as pd
import firebase_admin
from firebase_admin import credentials, firestore
from collections import Counter
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.metrics.pairwise import euclidean_distances
import re
import logging
import json

def initialize_firebase():
    if not len(firebase_admin._apps):
        cred = credentials.Certificate('C:/pododoc/key/kosmo-96bbe-60906db745e9.json')
        firebase_admin.initialize_app(cred)

def fetch_data_by_email(email):
    db = firestore.client()
    reviews_ref = db.collection('review')
    
    # Correct usage of the where method
    query = reviews_ref.where('email', '==', email)
    
    results = query.stream()
    data = []
    for doc in results:
        doc_data = doc.to_dict()
        if 'index' in doc_data and 'rating' in doc_data:
            data.append({
                'index': doc_data['index'],
                'rating': doc_data['rating'],
            })
    return data

def load_wine_data(df):
    df_white_wine = df
    
    # 가격을 숫자로 변환합니다
    df_white_wine['wine_price'] = df_white_wine['wine_price'].replace(r'[\$,]', '', regex=True).astype(float)
    return df_white_wine

def filter_by_price(df_white_wine, price_range):
    # 가격을 숫자로 변환합니다
    df_white_wine['wine_price'] = df_white_wine['wine_price'].replace(r'[\$,]', '', regex=True).astype(float)

    # 가격 범위에 따른 필터링
    if price_range == '50000':
        df_filtered = df_white_wine[df_white_wine['wine_price'] <= 50000]
    elif price_range == '150000':
        df_filtered = df_white_wine[(df_white_wine['wine_price'] > 50000) & (df_white_wine['wine_price'] <= 150000)]
    elif price_range == 'over150000':
        df_filtered = df_white_wine[df_white_wine['wine_price'] > 150000]
    else:
        df_filtered = df_white_wine
    
    return df_filtered

def process_data(email, price_range,df):
    initialize_firebase()
    data = fetch_data_by_email(email)
    
    if not data:
        return {"error": "No data found for the provided email."}
    
    wine_data = load_wine_data(df)
    
    # 가격 범위 필터링
    filtered_wine_data = filter_by_price(wine_data, price_range)
    
    indices = [entry['index'] for entry in data]
    filtered_wine_data = filtered_wine_data[filtered_wine_data['index'].isin(indices)]
    
    required_columns = ['index', 'body', 'texture', 'sweetness', 'flavor1', 'flavor2', 'flavor3']
    filtered_wine_data = filtered_wine_data[required_columns]

    rating_dict = {entry['index']: entry['rating'] for entry in data}
    filtered_wine_data['rating'] = filtered_wine_data['index'].map(rating_dict)
    
    numeric_columns = ['body', 'texture', 'sweetness']
    filtered_wine_data[numeric_columns] = filtered_wine_data[numeric_columns].apply(pd.to_numeric, errors='coerce')
    filtered_wine_data = filtered_wine_data.dropna()

    def weighted_average(column):
        weights = filtered_wine_data['rating']
        values = filtered_wine_data[column]
        return (weights * values).sum() / weights.sum()

    weighted_averages = {
        'body': weighted_average('body'),
        'texture': weighted_average('texture'),
        'sweetness': weighted_average('sweetness')
    }

    def weighted_mode(column):
        counts = Counter(filtered_wine_data[column])
        weighted_counts = {}
        for item in counts:
            weighted_counts[item] = (filtered_wine_data[filtered_wine_data[column] == item]['rating']).sum()
        return max(weighted_counts, key=weighted_counts.get)

    flavor1_mode = weighted_mode('flavor1')
    flavor2_mode = weighted_mode('flavor2')
    flavor3_mode = weighted_mode('flavor3')

    result_df = pd.DataFrame({
        'flavor1': [flavor1_mode],
        'flavor2': [flavor2_mode],
        'flavor3': [flavor3_mode],
        'body': [weighted_averages['body']],
        'texture': [weighted_averages['texture']],
        'sweetness': [weighted_averages['sweetness']]
    })

    return result_df

def find_similar_wines_from_result_df(result_df, target_index, original_data):
    df_features = result_df[['flavor1', 'flavor2', 'flavor3', 'body', 'texture', 'sweetness']].copy()
    original_features = original_data[['flavor1', 'flavor2', 'flavor3', 'body', 'texture', 'sweetness']].copy()

    ct = ColumnTransformer(
        transformers=[
            ('encoder', OneHotEncoder(handle_unknown='ignore', drop='first'), ['flavor1', 'flavor2', 'flavor3']),
            ('scaler', StandardScaler(), ['body', 'texture', 'sweetness'])
        ],
        remainder='passthrough'
    )

    X = ct.fit_transform(df_features)
    X_original = ct.transform(original_features)

    def calculate_distance_with_flavors(df_encoded, target_index, original_encoded):
        target_vector = df_encoded[target_index, :].reshape(1, -1)
        distance_scores = euclidean_distances(target_vector, original_encoded)
        return distance_scores.flatten()

    distances = calculate_distance_with_flavors(X, target_index, X_original)
    distances[target_index] = float('inf')

    similar_indices = distances.argsort()
    top_similar_indices = similar_indices[:20]

    similar_wines = original_data.iloc[top_similar_indices].copy()
    similar_wines['distance'] = distances[top_similar_indices]
    similar_wines = similar_wines.sort_values(by='distance')

    return similar_wines[['index', 'distance']]

def load_and_prepare_data(df):
    original_data = df
    if 'index' not in original_data.columns:
        original_data = original_data.reset_index()
        original_data.rename(columns={'index': 'index'}, inplace=True)
    return original_data

def get_wine_details(similar_wines_df, original_data):
    similar_indices = similar_wines_df['index'].tolist()
    wine_details = original_data[original_data['index'].isin(similar_indices)]
    wine_details = wine_details.set_index('index').reindex(similar_indices).reset_index()
    wine_details = wine_details.merge(similar_wines_df, on='index')
    
    # columns_of_interest = [
    #     'index', 'wine_name', 'wine_rating', 'wine_price',
    #     'wine_country', 'wine_region', 'wine_winery', 'wine_type', 'wine_grape', 'wine_image', 'distance'
    # ]
    
    # wine_details = wine_details[columns_of_interest]
    
    def extract_name_prefix(name):
        match = re.match(r'^(.*\D)', name)
        return match.group(1) if match else name
    
    wine_details['name_prefix'] = wine_details['wine_name'].apply(extract_name_prefix)
    wine_details = wine_details.drop_duplicates(subset='name_prefix')
    
    if len(wine_details) > 10:
        wine_details = wine_details.head(10)
    
    wine_details = wine_details.sort_values(by='distance')
    wine_details = wine_details.drop(columns='name_prefix')
    
    return wine_details

def recommend_whitewine(email, price_range, df):
    result_df = process_data(email, price_range,df)
    if 'error' in result_df:
        return result_df
    
    #csv_file_path = 'data/Clean_White_data.csv'
    original_data = load_and_prepare_data(df)
    target_index = 0
    
    similar_wines_indices = find_similar_wines_from_result_df(result_df, target_index, original_data)
    wine_details = get_wine_details(similar_wines_indices, original_data)
    
    # 가격 범위 필터링 적용
    filtered_wine_details = filter_by_price(wine_details, price_range)
       # 데이터프레임을 JSON으로 변환
    list_paginated = filtered_wine_details.to_json(orient='records')
    list_paginated = json.loads(list_paginated)
    
    data = {
    'total': len(filtered_wine_details),
    'list': list_paginated
    }
    return data

