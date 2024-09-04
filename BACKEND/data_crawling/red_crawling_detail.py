from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import pandas as pd
import time
import re

# 웹 드라이버 초기화
browser = webdriver.Chrome()
wait = WebDriverWait(browser, 180)  # 타임아웃을 180초로 설정하여 페이지 로딩 대기

# CSV 파일 읽기
input_csv = 'wineInfo_Red2.csv'
output_csv = 'wineInfo_Red_detailV2.csv'
df = pd.read_csv(input_csv)

def scroll_quarterway():
    """ 페이지의 25%만큼 스크롤하고 새 콘텐츠가 로드될 때까지 대기 """
    # 페이지의 현재 높이 가져오기
    current_height = browser.execute_script("return document.body.scrollHeight")
    # 페이지를 25%만큼 스크롤
    browser.execute_script("window.scrollTo(0, arguments[0] * 0.25);", current_height)
    time.sleep(10)  # 페이지 로딩 및 스크롤 애니메이션 대기

def get_details_from_link(link):
    browser.get(link)
    
    # 페이지를 25%만큼 스크롤합니다
    scroll_quarterway()

    try:
        # 와인 이름 추출
        wine_name = browser.find_element(By.CSS_SELECTOR, 'span.vintage').text.split('\n')[-1].strip()

        # 국가 정보 추출
        country_element = browser.find_element(By.CSS_SELECTOR, 'a[data-cy="breadcrumb-country"]')
        country_info = country_element.text

        # 지역 정보 추출
        region_element = browser.find_element(By.CSS_SELECTOR, 'a[data-cy="breadcrumb-region"]')
        region_info = region_element.text

        # 와이너리 정보 추출
        winery_element = browser.find_element(By.CSS_SELECTOR, 'a[data-cy="breadcrumb-winery"]')
        winery_info = winery_element.text

        # 와인 타입 정보 추출
        wine_type_element = browser.find_element(By.CSS_SELECTOR, 'a[data-cy="breadcrumb-winetype"]')
        wine_type_info = wine_type_element.text

        # 포도 품종 정보 추출
        grape_element = browser.find_element(By.CSS_SELECTOR, 'a[data-cy="breadcrumb-grape"]')
        grape_info = grape_element.text

        # 이미지 src 추출
        img_element = browser.find_element(By.CSS_SELECTOR, 'img.image')
        img_src = img_element.get_attribute('src')

        # 스타일 정보 추출
        styles = {}
        progress_elements = browser.find_elements(By.CSS_SELECTOR, 'span.indicatorBar__progress--3aXLX')
        for i, element in enumerate(progress_elements):
            style_attr = element.get_attribute('style')
            match = re.search(r'left:\s*([\d.]+)%', style_attr)
            if match:
                left_value = float(match.group(1))
                new_value = left_value + 7.5
                styles[f'style_{i}'] = new_value
            else:
                styles[f'style_{i}'] = '정보 없음'

        # 플래버 정보 추출
        flavors = {}
        flavor_elements = browser.find_elements(By.CSS_SELECTOR, 'span.tasteNote__flavorGroup--1Uaen')
        for i, element in enumerate(flavor_elements[:3]):  # 처음 3개 플래버 텍스트만 저장
            flavors[f'flavor_{i}'] = element.text

        return (wine_name, country_info, region_info, winery_info, wine_type_info, grape_info, 
                img_src, styles.get('style_0', '정보 없음'), styles.get('style_1', '정보 없음'), 
                styles.get('style_2', '정보 없음'), styles.get('style_3', '정보 없음'), 
                flavors.get('flavor_0', '정보 없음'), flavors.get('flavor_1', '정보 없음'), 
                flavors.get('flavor_2', '정보 없음'))
    except Exception as e:
        print(f"오류 발생: {e}")
        return ("정보 없음", "정보 없음", "정보 없음", "정보 없음", "정보 없음", "정보 없음", "정보 없음", 
                "정보 없음", "정보 없음", "정보 없음", "정보 없음", "정보 없음", "정보 없음", "정보 없음")

# 링크에서 와인 이름과 빈티지, 국가, 지역, 와이너리, 와인 타입, 포도 품종, 이미지 URL, 스타일, 플래버 정보 수집
df[['wine_name', 'wine_country', 'wine_region', 'wine_winery', 'wine_type', 'wine_grape', 
    'wine_image', 'body', 'texture', 'sweetness', 'acidity', 'flavor1', 'flavor2', 'flavor3']] = df['wine_link'].apply(lambda link: pd.Series(get_details_from_link(link)))

# 필요한 컬럼만 선택
df_filtered = df[['index', 'wine_name', 'wine_country', 'wine_region', 'wine_winery', 
                  'wine_type', 'wine_grape', 'wine_image', 'body', 'texture', 'sweetness', 
                  'acidity', 'flavor1', 'flavor2', 'flavor3']]

# 새 CSV 파일로 저장
df_filtered.to_csv(output_csv, index=False, encoding='utf-8')

print("데이터 저장 완료")

# 브라우저 닫기
browser.quit()
