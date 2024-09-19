from flask import Flask, request
import pandas as pd
import json

app = Flask(__name__)

@app.route('/wine/basicrecommend.json')
def wine(df):
    args = request.args
    page = int(args.get('page'))
    start = (page-1) * 10
    end = page *10 

    df = pd.read_csv('data/Combined_Wine_Data.csv')
    total = len(df)

    df = df[start:end]
    list = df.to_json(orient='records')
    list = json.loads(list)

    data = {'total':total, 'list':list}
    return data