package com.pododoc.app;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    Retrofit retrofit;
    RemoteService remoteService;
    int page = 1;
    JSONArray redArray = new JSONArray();
    JSONArray whiteArray = new JSONArray();
    WineAdapter redAdapter = new WineAdapter(true);
    WineAdapter whiteAdapter = new WineAdapter(false);
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    String priceRange = "50000";

    private List<JSONObject> mywineList = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        retrofit = new Retrofit.Builder()
                .baseUrl(RemoteService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        remoteService = retrofit.create(RemoteService.class);

        // 버튼 초기화 및 클릭 리스너 설정
        Button btnUnder50000 = view.findViewById(R.id.btn_under_50000);
        Button btnUnder150000 = view.findViewById(R.id.btn_under_150000);
        Button btnOver150000 = view.findViewById(R.id.btn_over_150000);

        btnUnder50000.setOnClickListener(v -> {
            priceRange = "50000";
            getMyWine();
        });
        btnUnder150000.setOnClickListener(v -> {
            priceRange = "150000";
            getMyWine();
        });
        btnOver150000.setOnClickListener(v -> {
            priceRange = "over150000";
            getMyWine();
        });

        RecyclerView redWineList = view.findViewById(R.id.red_wine_list);
        RecyclerView whiteWineList = view.findViewById(R.id.white_wine_list);

        redWineList.setAdapter(redAdapter);
        whiteWineList.setAdapter(whiteAdapter);

        StaggeredGridLayoutManager redManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        StaggeredGridLayoutManager whiteManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);

        redWineList.setLayoutManager(redManager);
        whiteWineList.setLayoutManager(whiteManager);
        //마이와인 데이터체크
        getMyWine();

        // 기본 데이터 로드
//        getFilteredWines("50000");
        return view;
    }

    private void handleWineListUpdate() {
        if (mywineList.size() > 10) {
            getRecommendRed();
        } else {
            getFilteredWines();
        }
    }

    public void getRecommendRed(){
        String email = user.getEmail();
        Call<HashMap<String, Object>> redMain = remoteService.redmain(email,priceRange);
        redMain.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (response.body() != null) {
                    try {
                        JSONObject object = new JSONObject(response.body());
                        redArray = object.getJSONArray("list");
                        redAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                Log.e("getRecommendRed", "Error: " + t.getMessage());
            }
        });

        // 화이트와인 데이터 요청
        Call<HashMap<String, Object>> whitemain = remoteService.whitemain(email, priceRange);
        whitemain.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (response.body() != null) {
                    try {
                        JSONObject object = new JSONObject(response.body());
                        whiteArray = object.getJSONArray("list");
                        whiteAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    public void getFilteredWines() {
        // 레드와인 데이터 요청
        String email = user.getEmail();

        Call<HashMap<String, Object>> callRed = remoteService.basicRed(page, priceRange);
        callRed.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (response.body() != null) {
                    try {
                        JSONObject object = new JSONObject(response.body());
                        redArray = object.getJSONArray("list");
                        redAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                Log.e("getFilteredWines", "Error: " + t.getMessage());
            }
        });

        // 화이트와인 데이터 요청
        Call<HashMap<String, Object>> callWhite = remoteService.basicWhite(page, priceRange);
        callWhite.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (response.body() != null) {
                    try {
                        JSONObject object = new JSONObject(response.body());
                        whiteArray = object.getJSONArray("list");
                        whiteAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    class WineAdapter extends RecyclerView.Adapter<WineAdapter.ViewHolder> {
        private boolean isRedWine;

        public WineAdapter(boolean isRedWine) {
            this.isRedWine = isRedWine;
        }

        @NonNull
        @Override
        public WineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wine_search, parent, false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull WineAdapter.ViewHolder holder, int position) {
            JSONArray dataArray = isRedWine ? redArray : whiteArray;
            try {
                JSONObject obj = dataArray.getJSONObject(position);
                String image = obj.getString("wine_image");
                int index = obj.getInt("index");
                Picasso.with(getActivity()).load(image).into(holder.image);
                holder.index.setText(String.valueOf(index));
                holder.rating.setRating((float) obj.getDouble("wine_rating"));
                holder.country.setText(obj.getString("wine_country"));
                holder.type.setText(obj.getString("wine_type"));
                holder.name.setText(obj.getString("wine_name"));
                holder.winery.setText(obj.getString("wine_winery"));
                holder.region.setText(obj.getString("wine_region") + " /");
                holder.point.setText("(" + obj.getString("wine_rating") + ")");

                String price = obj.optString("wine_price", "");
                if (!price.isEmpty()) {
                    // 소수점 제거
                    price = price.split("\\.")[0];
                    // 숫자를 천 단위로 포맷팅
                    try {
                        int priceValue = Integer.parseInt(price);
                        // 천 단위로 콤마를 넣기 위해 NumberFormat 사용
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        price = numberFormat.format(priceValue);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                holder.price.setText("판매가: " + price + "원");

                List<String> flavors = new ArrayList<>();
                if (!obj.optString("flavor1", "").isEmpty()) flavors.add(obj.optString("flavor1", ""));
                if (!obj.optString("flavor2", "").isEmpty()) flavors.add(obj.optString("flavor2", ""));
                if (!obj.optString("flavor3", "").isEmpty()) flavors.add(obj.optString("flavor3", ""));
                String taste = TextUtils.join(", ", flavors);
                holder.taste.setText(taste);

                String strCountry = obj.getString("wine_country").toLowerCase().replace(" ", "");
                TypedArray icons= getResources().obtainTypedArray(R.array.flags);
                String[] countries = getResources().getStringArray(R.array.countries);
                int flagIndex = Arrays.asList(countries).indexOf(strCountry);
                if (flagIndex >= 0) {
                    holder.ImageView.setImageDrawable(icons.getDrawable(flagIndex));
                } else {
                    holder.ImageView.setImageResource(R.drawable.flag); // 기본 이미지
                }
                icons.recycle();
                holder.card.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ReadActivity.class);
                    intent.putExtra("index", index);
                    startActivity(intent);
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return (isRedWine ? redArray : whiteArray).length();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, ImageView;
            TextView name, type, country, price, index, region, taste, winery, point;
            RatingBar rating;
            CardView card;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.card);
                ImageView = itemView.findViewById(R.id.flag);
                image = itemView.findViewById(R.id.image);
                name = itemView.findViewById(R.id.name);
                type = itemView.findViewById(R.id.type);
                country = itemView.findViewById(R.id.country);
                price = itemView.findViewById(R.id.price);
                index = itemView.findViewById(R.id.index);
                region = itemView.findViewById(R.id.region);
                taste = itemView.findViewById(R.id.taste);
                winery = itemView.findViewById(R.id.winery);
                point = itemView.findViewById(R.id.point);
                rating = itemView.findViewById(R.id.rating);
            }
        }
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
                            mywineList.clear(); // Clear existing data
                            for (Object item : list) {
                                if (item instanceof LinkedTreeMap) {
                                    // Convert LinkedTreeMap to JSONObject
                                    LinkedTreeMap<String, Object> treeMap = (LinkedTreeMap<String, Object>) item;
                                    JSONObject jsonObject = new JSONObject(treeMap);
                                    mywineList.add(jsonObject);
                                    Log.i("size", String.valueOf(mywineList.size()));
                                } else {
                                    Log.e("Mywine", "Unexpected item type: " + item.getClass().getName());
                                }
                            }
                            handleWineListUpdate();
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
}
