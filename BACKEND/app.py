from flask import Flask
import pandas as pd

app = Flask(__name__)

@app.route('/')
def index():
    return 'PODODOC'

@app.route('/wine/list.json')
def wind():
    df = pd.read_csv('data/WineInfo_Red_detail.csv')
    dff = df[:10]
    return dff.to_json(orient='records')

if __name__ == '__main__':
    app.run(port=5000, debug=True, host='192.168.0.11')