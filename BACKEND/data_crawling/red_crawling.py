from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
import csv
import re

# 웹 드라이버 초기화
browser = webdriver.Chrome()
browser.get('https://www.vivino.com/explore?e=eJzLLbI1VMvNzLM1UMtNrLA1NQABteRKW-8gtWQgEa5WAFSQnmZblliUmVqSmKOWX5Rim5JanKyWn1RpW5RYkpmXXhyfnF-aV6JWXhIda2sIAEepG8A%3D')

def wait_until(xpath, timeout=10):
    WebDriverWait(browser, timeout).until(EC.presence_of_all_elements_located((By.XPATH, xpath)))

# 창 최대화 및 페이지 로드 대기
browser.maximize_window()
time.sleep(2)

# 변수 초기화
prev_height = browser.execute_script("return document.body.scrollHeight")
data_collected = []
unique_identifier_collected = set()  # (wine_name, wine_region) 조합을 저장하여 중복 방지

def load_more_data():
    # 페이지 스크롤 내리기
    browser.execute_script("window.scrollTo(0, document.body.scrollHeight)")
    time.sleep(2)  # 새 데이터 로드 대기 (이미지 로딩을 고려하여 시간 조정)

while len(data_collected) < 150000000:
    try:
        # 카드 요소가 로드될 때까지 대기
        WebDriverWait(browser, 10).until(
            EC.presence_of_all_elements_located((By.CSS_SELECTOR, 'a.wineCard__cardLink--3F_uB'))
        )

        # 와인 카드 수집
        wine_cards = browser.find_elements(By.CSS_SELECTOR, 'a.wineCard__cardLink--3F_uB')

        for card in wine_cards:
            try:
                # 각 카드에서 와인 정보 추출
                wine_name_element = card.find_element(By.CSS_SELECTOR, '.wineInfoVintage__vintage--VvWlU')
                wine_region_element = card.find_element(By.CSS_SELECTOR, '.wineInfoVintage__truncate--3QAtw')
                wine_country_element = card.find_element(By.CSS_SELECTOR, '.wineInfoLocation__regionAndCountry--1nEJz')
                wine_rating_element = card.find_element(By.CSS_SELECTOR, '.vivinoRating_averageValue__uDdPM')
                wine_reviews_element = card.find_element(By.CSS_SELECTOR, '.vivinoRating_caption__xL84P')
                wine_price_element = card.find_element(By.CSS_SELECTOR, '.addToCart__subText--1pvFt')

                # 와인 이름과 지역 정보 추출
                wine_name = wine_name_element.text
                wine_region = wine_region_element.text
                wine_country = wine_country_element.text
                wine_rating = wine_rating_element.text
                wine_reviews = wine_reviews_element.text
                wine_price = wine_price_element.text

                # country에서 ',' 뒤의 부분만 추출
                country_info = wine_country.split(',')[-1].strip() if ',' in wine_country else wine_country

                # reviews에서 숫자만 추출
                review_number_match = re.search(r'\d+', wine_reviews)
                review_info = review_number_match.group() if review_number_match else ""

                # 가격에서 ₩ 기호 뒤의 숫자만 추출
                price_number_match = re.search(r'₩([\d,]+)', wine_price)
                price_info = price_number_match.group(1) if price_number_match else ""

                # (wine_name, wine_region) 조합을 고유 식별자로 사용
                unique_identifier = f"{wine_name}{wine_region}"
                
                if unique_identifier in unique_identifier_collected:
                    continue  # 이미 수집된 와인 이름과 지역 조합은 건너뜁니다

                # 링크 추출
                relative_link = card.get_attribute('href')
                full_link = f'https://www.vivino.com{relative_link}' if relative_link.startswith('/') else relative_link

                data_collected.append({
                    "index": len(data_collected) + 1,  # 인덱스 추가
                    "wine_name": wine_name,
                    "wine_region": wine_region,
                    "wine_country": country_info,  # 수정된 country 정보
                    "wine_rating": wine_rating,
                    "wine_reviews": review_info,  # 숫자만 추출된 리뷰
                    "wine_price": price_info,  # 가격 정보
                    "wine_link": full_link
                })
                unique_identifier_collected.add(unique_identifier)  # 수집된 고유 식별자 추가

            except Exception as e:
                print(f"카드 데이터 수집 중 오류 발생: {e}")
                continue

    except Exception as e:
        print(f"데이터 수집 중 오류 발생: {e}")
        break

    # 데이터 수집 후 추가 데이터 로드
    load_more_data()
    
    # 페이지 끝에 도달했는지 확인
    curr_height = browser.execute_script("return document.body.scrollHeight")
    if prev_height == curr_height:
        break  # 새 콘텐츠가 로드되지 않으면 중단
    prev_height = curr_height

# 수집된 데이터를 CSV 파일로 저장
with open('wineInfo_Red2.csv', 'w', newline='', encoding='utf-8') as csvfile:
    fieldnames = ['index', 'wine_name', 'wine_region', 'wine_country', 'wine_rating', 'wine_reviews', 'wine_price', 'wine_link']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()
    writer.writerows(data_collected)

print("데이터 저장 완료")

# 브라우저 닫기
browser.quit()
