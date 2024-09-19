package com.pododoc.app;

import static com.pododoc.app.RemoteService.BASE_URL;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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

public class SearchFragment extends Fragment {
    Retrofit retrofit;
    RemoteService remoteService;
    int page = 1;
    int total = 0;
    JSONArray array = new JSONArray();
    WineAdapter adapter = new WineAdapter();

    LinearLayout linearLayout;  // LinearLayout을 추가
    ImageView searchButton;      // 검색 버튼을 추가
    private EditText searchInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Retrofit 초기화
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        remoteService = retrofit.create(RemoteService.class);

        // LinearLayout 초기화 및 클릭 리스너 설정
        linearLayout = view.findViewById(R.id.linear);
        searchButton = view.findViewById(R.id.btnSearch);
        searchInput = view.findViewById(R.id.search);



        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchPageActivity.class);
                startActivity(intent);
            }
        });
        // 검색 버튼 클릭 시 SearchPageActivity로 이동
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchPageActivity.class);
                startActivity(intent);
            }
        });
        // EditText의 키를 눌렀을 때도 SearchPageActivity로 이동
        searchInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchPageActivity.class);
                startActivity(intent);
            }
        });

        // RecyclerView 설정
        RecyclerView list = view.findViewById(R.id.list);
        list.setAdapter(adapter);
        StaggeredGridLayoutManager manager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        list.setLayoutManager(manager);

        // 데이터 가져오기
        getList();

        return view;
    }//onCreateView

    public void getList() {
        Call<HashMap<String, Object>> call = remoteService.list(page);
        call.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                if (response.body() != null) {
                    try {
                        // 응답 처리
                        JSONObject object = new JSONObject(response.body());
                        total = object.getInt("total");
                        array = object.getJSONArray("list");
                        Log.i("total", total + "");
                        Log.i("length", array.length() + "");
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }//getList()

    class WineAdapter extends RecyclerView.Adapter<WineAdapter.ViewHolder> {

        @NonNull
        @Override
        public WineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = getLayoutInflater().inflate(R.layout.item_wine_search, parent, false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull WineAdapter.ViewHolder holder, int position) {
            try {
                JSONObject obj = array.getJSONObject(position);

                String image = obj.getString("wine_image");
                int index = obj.getInt("index");
                Picasso.with(getActivity()).load(image).into(holder.image);
                holder.index.setText(String.valueOf(index));
                float rating = Float.parseFloat(obj.getString("wine_rating"));
                holder.rating.setRating(rating);
                String country = obj.getString("wine_country");
                holder.country.setText(country);
                String type = obj.getString("wine_type");
                holder.type.setText(type);
                String name = obj.getString("wine_name");
                holder.name.setText(name);
                String winery = obj.getString("wine_winery");
                holder.winery.setText(winery);
                String region = obj.getString("wine_region");
                holder.region.setText(region + " /");
                holder.point.setText("(" + obj.getString("wine_rating") + ")");
                String strPrice = obj.optString("wine_price", "");
                if (!strPrice.isEmpty()) {
                    // 소수점 제거
                    strPrice = strPrice.split("\\.")[0];
                    // 숫자를 천 단위로 포맷팅
                    try {
                        int priceValue = Integer.parseInt(strPrice);
                        // 천 단위로 콤마를 넣기 위해 NumberFormat 사용
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        strPrice = numberFormat.format(priceValue);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                holder.price.setText("판매가: " + strPrice + "원");
                // 맛 설정: flavor1, flavor2, flavor3을 리스트로 받아서 하나의 문자열로 합치기
                String flavor1 = obj.optString("flavor1", "");
                String flavor2 = obj.optString("flavor2", "");
                String flavor3 = obj.optString("flavor3", "");

                // 빈값이 아닌 flavor들을 합쳐서 문자열로 만들기
                List<String> flavors = new ArrayList<>();
                if (!flavor1.isEmpty()) flavors.add(flavor1);
                if (!flavor2.isEmpty()) flavors.add(flavor2);
                if (!flavor3.isEmpty()) flavors.add(flavor3);

                String taste = TextUtils.join(", ", flavors);
                holder.taste.setText(taste);

                String strCountry = country.toLowerCase().replace(" ", "");

                TypedArray icons = getResources().obtainTypedArray(R.array.flags);
                String[] countries = getResources().getStringArray(R.array.countries);
                int flagIndex = Arrays.asList(countries).indexOf(strCountry);
                if (flagIndex >= 0) {
                    holder.ImageView.setImageDrawable(icons.getDrawable(flagIndex));
                } else {
                    holder.ImageView.setImageResource(R.drawable.flag); // 기본 이미지
                }

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ReadActivity.class);
                        intent.putExtra("index", index);
                        startActivity(intent);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return array.length();
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
                rating = itemView.findViewById(R.id.rating);
                index = itemView.findViewById(R.id.index);
                taste = itemView.findViewById(R.id.taste);
                region = itemView.findViewById(R.id.region);
                price = itemView.findViewById(R.id.price);
                winery = itemView.findViewById(R.id.winery);
                point = itemView.findViewById(R.id.point);
            }
        }
    }
}//fragment
