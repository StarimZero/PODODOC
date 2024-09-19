import firebase_admin, requests
from firebase_admin import credentials, firestore
from collections import Counter
import pandas as pd
import folium
from folium.plugins import HeatMap
from geopy.geocoders import Nominatim
from pathlib import Path

def fetch_review_indexes(email):
    """이메일에 해당하는 Firestore의 인덱스를 조회."""
    db = firestore.client()
    collection_ref = db.collection('review')
    query = collection_ref.where('email', '==', email).stream()
    indexes = [doc.to_dict().get('index') for doc in query if 'index' in doc.to_dict()]
    return indexes

def initialize_firebase():
    """Firebase Admin SDK 초기화."""
    if not len(firebase_admin._apps):
        cred = credentials.Certificate('C:/pododoc/key/kosmo-96bbe-60906db745e9.json')
        firebase_admin.initialize_app(cred)

def load_country_data(df):
    """CSV 파일에서 국가 데이터를 로드하고, 국가 이름을 일치시킵니다."""
    df = df
    country_data = df.set_index('index')['wine_country'].to_dict()
    
    # 미국의 이름을 일치시키기
    country_data = {k: 'United States of America' if v == 'United States' else v for k, v in country_data.items()}
    
    return country_data

def create_folium_map_choropleth(indexes, country_data):
    """folium을 사용하여 2D 지도 시각화 생성 (Choropleth 사용)"""
    m = folium.Map(location=[24 + 40/60 + 17.4/3600, -(38 + 23/60 + 45.2/3600)], zoom_start=0.5)
    
    # 세계 행정구역 GeoJSON 데이터 로드
    world_geojson_url = 'https://raw.githubusercontent.com/johan/world.geo.json/master/countries.geo.json'
    response = requests.get(world_geojson_url)
    world_geojson = response.json()
    
    # 국가별 빈도 계산
    counter = Counter(indexes)
    data = {country_data.get(index, 'Unknown'): count for index, count in counter.items()}
    
    # Choropleth 레이어 추가
    folium.Choropleth(
        geo_data=world_geojson,
        name='choropleth',
        data=pd.DataFrame(list(data.items()), columns=['Country', 'Count']),
        columns=['Country', 'Count'],
        key_on='feature.properties.name',  # 이 부분은 GeoJSON 파일의 속성 이름에 따라 달라질 수 있습니다
        fill_color='YlOrRd',
        fill_opacity=0.7,
        line_opacity=0.2,
        legend_name='Review Count'
    ).add_to(m)
    
    return m

def save_map_to_html(email_file_path, df, output_html_path):
    """지도를 생성하고 HTML 파일로 저장합니다."""
    initialize_firebase()
    
    # 이메일 읽기
    with open(email_file_path, 'r') as file:
        email = file.read().strip()

    indexes = fetch_review_indexes(email)
    country_data = load_country_data(df)
    folium_map = create_folium_map_choropleth(indexes, country_data)
    folium_map.save(output_html_path)
    
    # CSS 스타일 추가
    add_custom_css_to_html(output_html_path)

def add_custom_css_to_html(html_path):
    """HTML 파일에 직접 CSS를 삽입합니다."""
    css_content = """
    <style>
        /* 레전드 SVG 스타일 */
        .legend.leaflet-control svg {
            width: 10%;
            height: auto;
            max-width: 300px; 
            max-height: 100px; 
        }
        .leaflet-control-zoom {
            display: none;
        }
    </style>
    """
    
    
    with open(html_path, 'r', encoding='utf-8') as file:
        html_content = file.read()
    
    # <head> 태그가 있는지 확인하고 CSS 스타일을 추가합니다.
    if '<head>' in html_content:
        html_content = html_content.replace('<head>', '<head>' + css_content)
    else:
        # <head> 태그가 없는 경우, 파일 시작 부분에 CSS 스타일을 추가합니다.
        html_content = css_content + html_content
    
    with open(html_path, 'w', encoding='utf-8') as file:
        file.write(html_content)
