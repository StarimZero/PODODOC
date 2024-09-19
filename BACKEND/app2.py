import gspread
from oauth2client.service_account import ServiceAccountCredentials
import pandas as pd

# 구글 API 인증 및 서비스 계정 설정
scope = ["https://www.googleapis.com/auth/spreadsheets", "https://www.googleapis.com/auth/drive"]
creds = ServiceAccountCredentials.from_json_keyfile_name('peaceful-fact-428901-n3-ff34ae41e693.json', scope)
client = gspread.authorize(creds)

# 구글 시트 열기
sheet = client.open_by_url('https://docs.google.com/spreadsheets/d/139cNp6-AsxuRS1rYcQyBT-syOltwuncXIJCcoxapPxY/edit?gid=0#gid=0').sheet1

# 시트 데이터 가져오기
Clean_Red_data = sheet.get_all_records()
df = pd.DataFrame(Clean_Red_data)

# 데이터 확인
print(df.head())
