import pandas as pd
import json

def get_basic_red_wines(page, price_range, df):
    start = (page - 1) * 10
    end = page * 10

    try:
        # CSV 파일을 로드합니다
        #df = pd.read_csv('data/Combined_Wine_Data.csv')

        # 'Red wine'만 필터링합니다
        df_red_wine = df[df['wine_type'] == 'Red wine']

        # 가격을 숫자로 변환하고 소수점을 제거합니다
        df_red_wine['wine_price'] = df_red_wine['wine_price'].replace('[\$,]', '', regex=True).astype(float).round(0).astype(int)

        # 가격 범위에 따른 필터링
        if price_range == '50000':
            df_filtered = df_red_wine[df_red_wine['wine_price'] <= 50000]
        elif price_range == '150000':
            df_filtered = df_red_wine[(df_red_wine['wine_price'] > 50000) & (df_red_wine['wine_price'] <= 150000)]
        elif price_range == 'over150000':
            df_filtered = df_red_wine[df_red_wine['wine_price'] > 150000]
        else:
            df_filtered = df_red_wine


        # 페이지네이션 적용
        df_paginated = df_filtered[start:end]

        # JSON으로 변환
        list_paginated = df_paginated.to_json(orient='records')
        list_paginated = json.loads(list_paginated)

        # 응답 데이터를 준비합니다
        data = {
            'total': len(df_filtered),
            'list': list_paginated
        }

        return data
    except Exception as e:
        # 에러 발생 시 JSON 응답으로 에러 메시지를 반환합니다
        return {"error": str(e)}


def get_basic_white_wines(page, price_range, df):
    start = (page - 1) * 10
    end = page * 10

    try:
        # CSV 파일을 로드합니다
        #df = pd.read_csv('data/Combined_Wine_Data.csv')

        # 'White wine'만 필터링합니다
        df_red_wine = df[df['wine_type'] == 'White wine']

        # 가격을 숫자로 변환합니다
        df_red_wine['wine_price'] = df_red_wine['wine_price'].replace('[\$,]', '', regex=True).astype(float)

        # 가격 범위에 따른 필터링
        if price_range == '50000':
            df_filtered = df_red_wine[df_red_wine['wine_price'] <= 50000]
        elif price_range == '150000':
            df_filtered = df_red_wine[(df_red_wine['wine_price'] > 50000) & (df_red_wine['wine_price'] <= 150000)]
        elif price_range == 'over150000':
            df_filtered = df_red_wine[df_red_wine['wine_price'] > 150000]
        else:
            df_filtered = df_red_wine

        # 페이지네이션 적용
        df_paginated = df_filtered[start:end]

        # JSON으로 변환
        list_paginated = df_paginated.to_json(orient='records')
        list_paginated = json.loads(list_paginated)

        # 응답 데이터를 준비합니다
        data = {
            'total': len(df_filtered),
            'list': list_paginated
        }

        return data
    except Exception as e:
        # 에러 발생 시 JSON 응답으로 에러 메시지를 반환합니다
        return {"error": str(e)}