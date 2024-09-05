import pandas as pd
import json
import matplotlib
matplotlib.use('Agg')  # GUI 모드가 아닌 이미지 생성 모드로 설정
import matplotlib.pyplot as plt
import numpy as np
import io
from PIL import Image

def get_data(index):
    df = pd.read_csv('data/Combined_Wine_Data.csv')
    filt= df['index'] == index
    df=df[filt]

    data = df.to_json(orient='records')
    data = json.loads(data)
    
    return data[0]

def chart(index):
    df = pd.read_csv('data/Combined_Wine_Data.csv')
    categories = ['body', 'texture', 'sweetness', 'acidity']
    values = [df[df['index'] == index][col].values[0] for col in categories]
    fig, ax = plt.subplots(figsize=(10, 6))

    # x축 범위와 레이블 설정
    ax.set_xlim(0, 100)
    ax.set_facecolor('#121212')
    # 각 속성에 대한 색상
    colors = {
        'body': '#003f5c',
        'texture': '#58508d',
        'sweetness': '#ff6361',
        'acidity': '#ffa600'
    }

    # 각 속성에 대해 두꺼운 막대와 점을 표시
    bar_width = 0.47
    for i, (category, value) in enumerate(zip(categories, values)):
        # 두꺼운 막대 추가
        ax.barh(i, 100, color='grey', height=bar_width, alpha=0.3)
        # 데이터 값 위치에 점 추가
        ax.plot(value, i, 'o', markersize=40, color=colors[category], label=f'{category}: {value:.2f}')

     # 레이블과 격자 설정
    ax.set_yticks(np.arange(len(categories)))
    ax.set_yticklabels(categories, fontsize=40, color='white')  # 글자 크기와 색상 설정
    ax.grid(False)
    ax.set_xticks([])
    ax.set_xlabel('')
    ax.set_ylabel('')
    # 스파인 제거
    for spine in ax.spines.values():
        spine.set_visible(False)
    # 차트의 여백과 테두리 제거
    fig.patch.set_visible(False)
    ax.patch.set_visible(False)
   # 그래프를 메모리 버퍼로 저장
    buf = io.BytesIO()
    plt.savefig(buf, format='png', bbox_inches='tight')
    buf.seek(0)

    # 메모리에서 그래프를 지웁니다.
    plt.close()

    return buf