import pandas as pd

def search_wine(df,query, page=1, size=10):
    data = df
    
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