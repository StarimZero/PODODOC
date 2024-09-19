package com.pododoc.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MywineActivity extends AppCompatActivity {

    Retrofit retrofit;
    RemoteService remoteService;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    WineAdapter adapter = new WineAdapter();
    private List<JSONObject> wineList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywine);
        getSupportActionBar().setTitle("내와인");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        // Retrofit 초기화
        retrofit = new Retrofit.Builder()
                .baseUrl(RemoteService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // RemoteService 초기화
        remoteService = retrofit.create(RemoteService.class);

        // RecyclerView 설정
        RecyclerView recyclerView = findViewById(R.id.mywine);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // My Wine 데이터 가져오기
        getMyWine();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getMyWine() {
        if (user != null) {
            String email = user.getEmail();
            Call<HashMap<String, Object>> call = remoteService.getMyWine(email);
            call.enqueue(new Callback<HashMap<String, Object>>() {
                @Override
                public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        HashMap<String, Object> responseBody = response.body();
                        // "results" 값을 가져와서 타입 확인
                        Object results = responseBody.get("results");
                        if (results instanceof ArrayList) {
                            ArrayList<Object> list = (ArrayList<Object>) results;
                            wineList.clear(); // Clear existing data
                            for (Object item : list) {
                                if (item instanceof LinkedTreeMap) {
                                    // Convert LinkedTreeMap to JSONObject
                                    LinkedTreeMap<String, Object> treeMap = (LinkedTreeMap<String, Object>) item;
                                    JSONObject jsonObject = new JSONObject(treeMap);
                                    wineList.add(jsonObject);
                                } else {
                                    Log.e("Mywine", "Unexpected item type: " + item.getClass().getName());
                                }
                            }
                            adapter.notifyDataSetChanged(); // Notify adapter of data change
                        } else {
                            Log.e("Mywine", "Expected ArrayList but got: " + results.getClass().getName());
                        }
                    } else {
                        Log.e("Mywine", "Response not successful or body is null");
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                    Log.e("Mywine", "Failure: " + t.getMessage());
                }
            });
        }
    }


    class WineAdapter extends RecyclerView.Adapter<WineAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = getLayoutInflater().inflate(R.layout.item_mywine, parent, false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject obj = wineList.get(position);

                // Extract data from the JSON object
                int index = obj.getInt("index");
                String strImage = obj.getString("photo");
                String strName = obj.getString("wine_name");
                String strCountry = obj.getString("wine_country");
                String strWinery = obj.getString("wine_winery");
                String strGrape = obj.getString("wine_grape");
                String strRegion = obj.getString("wine_region");
                float rating = (float) obj.getDouble("rating");
                String strDate = obj.getString("date");

                // Set data to views
                holder.winery.setText(strWinery);
                holder.grape.setText(strGrape);
                holder.region.setText(strRegion);
                holder.name.setText(strName);
                holder.country.setText(strCountry);
                holder.ratingBar.setRating(rating);
                holder.ratingScore.setText(String.valueOf(rating));
                holder.date.setText(strDate);

                // Load wine image using Picasso
                Picasso.with(MywineActivity.this).load(strImage).into(holder.photo);

                // Set flag image based on country
                String countryNormalized = strCountry.toLowerCase().replace(" ", "");
                TypedArray icons = getResources().obtainTypedArray(R.array.flags);
                String[] countries = getResources().getStringArray(R.array.countries);
                int flagIndex = Arrays.asList(countries).indexOf(countryNormalized);
                if (flagIndex >= 0) {
                    holder.flag.setImageDrawable(icons.getDrawable(flagIndex));
                } else {
                    holder.flag.setImageResource(R.drawable.flag); // Default image
                }

                // Handle click event on card
                holder.card.setOnClickListener(view -> {
                    Intent intent = new Intent(MywineActivity.this, ReadActivity.class);
                    intent.putExtra("index", index);
                    startActivity(intent);
                });

            } catch (JSONException e) {
                Log.e("WineAdapter", "Error parsing JSON data: ", e);
            }
        }

        @Override
        public int getItemCount() {
            return wineList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, winery, grape, country, region, date, ratingScore;
            ImageView photo, flag;
            RatingBar ratingBar;
            CardView card;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                winery = itemView.findViewById(R.id.winery);
                grape = itemView.findViewById(R.id.grape);
                country = itemView.findViewById(R.id.country);
                region = itemView.findViewById(R.id.region);
                date = itemView.findViewById(R.id.date);
                ratingScore = itemView.findViewById(R.id.ratingScore);
                photo = itemView.findViewById(R.id.photo);
                flag = itemView.findViewById(R.id.flag);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                card = itemView.findViewById(R.id.list);
            }
        }
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        getMyWine();
    }
}


