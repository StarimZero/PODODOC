package com.pododoc.app;

import static com.pododoc.app.RemoteService.BASE_URL;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchPageActivity extends AppCompatActivity {

    Retrofit retrofit;
    RemoteService remoteService;
    int page = 1;
    int total = 0;
    int size = 10;
    String query = "";
    WineAdapter adapter=new WineAdapter();
    JSONArray array=new JSONArray();
    private Handler handler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);
        getSupportActionBar().setTitle("와인 검색");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        retrofit=new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        remoteService = retrofit.create(RemoteService.class);

        RecyclerView list=findViewById(R.id.search_results);
        list.setAdapter(adapter);
        StaggeredGridLayoutManager manager=new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        list.setLayoutManager(manager);

        EditText searchInput = findViewById(R.id.search_input);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove any previously scheduled searches
                handler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newQuery = s.toString().trim();
                query = newQuery;
                if (!newQuery.isEmpty()) {
                    // 1초뒤에 검색
                    searchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            page = 1; // 페이지리셋
                            array = new JSONArray(); // 이전결과클리어
                            getSearchList();
                        }
                    };
                    handler.postDelayed(searchRunnable, 1000); //딜레이1초
                }
            }
        });
        getSearchList();

    }//oncreate

    public void getSearchList() {
        Call<HashMap<String, Object>> call = remoteService.search(query, page, size);
        call.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                try {
                    // Get the body from response and cast it to HashMap
                    HashMap<String, Object> responseBody = response.body();
                    if (responseBody != null) {
                        // Convert HashMap to JSONObject
                        JSONObject object = new JSONObject(responseBody);

                        // Extract total
                        total = object.getInt("total");

                        // Extract results array
                        JSONArray arr = object.getJSONArray("results");

                        // Parse the results array
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject wineObject = arr.getJSONObject(i);
                            array.put(wineObject);
                        }

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Log.i("getSearchList", e.toString());
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                // Handle failure case
                Log.e("getSearchList", "Failure: " + t.getMessage());
            }
        });
    }
    class WineAdapter extends RecyclerView.Adapter<WineAdapter.ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item=getLayoutInflater().inflate(R.layout.item_wine_search_page,parent,false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull WineAdapter.ViewHolder holder, int position) {
            try {
                JSONObject obj=array.getJSONObject(position);
                int index = obj.getInt("index");
                String strImage=obj.getString("wine_image");
                String strName=obj.getString("wine_name");
                String strCountry=obj.getString("wine_country");
                String strWinery = obj.getString("wine_winery");
                String strGrape = obj.getString("wine_grape");
                String strRegion = obj.getString("wine_region");

                holder.winery.setText(strWinery);
                holder.grape.setText(strGrape);
                holder.region.setText(strRegion);
                holder.name.setText(strName);
                holder.country.setText(strCountry);
                Picasso.with(SearchPageActivity.this).load(strImage).into(holder.image);

                String Country=strCountry.toLowerCase().replace(" ", "");
                TypedArray icons = getResources().obtainTypedArray(R.array.flags);
                String[] countries = getResources().getStringArray(R.array.countries);
                int flagIndex = Arrays.asList(countries).indexOf(Country);
                if (flagIndex >= 0) {
                    holder.flag.setImageDrawable(icons.getDrawable(flagIndex));
                } else {
                    holder.flag.setImageResource(R.drawable.flag); // 기본 이미지
                }

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(SearchPageActivity.this, ReadActivity.class);
                        intent.putExtra("index", index);
                        startActivity(intent);
                    }
                });
            } catch (JSONException e) {
                Log.i("error", e.toString());
                throw new RuntimeException(e);
            }

        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView image, flag;
            TextView name, country, winery, grape, region;
            CardView card;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                image=itemView.findViewById(R.id.image);
                winery=itemView.findViewById(R.id.winery);
                name=itemView.findViewById(R.id.name);
                grape=itemView.findViewById(R.id.grape);
                region=itemView.findViewById(R.id.region);
                country=itemView.findViewById(R.id.country);
                flag=itemView.findViewById(R.id.flag);
                card=itemView.findViewById(R.id.list);

            }
        }
    }//Adapter

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}//activity