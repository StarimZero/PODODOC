import pandas as pd

def load_data():
    return pd.read_csv('data/Combined_Wine_Data.csv')

def search_wine(query, page=1, size=10):
    data = load_data()
    
    query = query.lower()
    
    filtered_data = data[data['wine_name'].str.lower().str.contains(query, na=False)]
    
    total = len(filtered_data)
    
    start = (page - 1) * size
    end = page * size
    paginated_data = filtered_data.iloc[start:end]
    
    results = paginated_data.to_dict(orient='records')
    
    return {
        'total': total,
        'page': page,
        'size': size,
        'results': results
    }