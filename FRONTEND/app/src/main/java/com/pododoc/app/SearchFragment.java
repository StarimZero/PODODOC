package com.pododoc.app;

import static com.pododoc.app.RemoteService.BASE_URL;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_search, container, false);
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        remoteService=retrofit.create(RemoteService.class);
        getList();

        RecyclerView list=view.findViewById(R.id.list);
        list.setAdapter(adapter);
        StaggeredGridLayoutManager manager=
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        list.setLayoutManager(manager);
        return view;
    }//onCreateView

    public void getList(){
        Call<HashMap<String,Object>> call=remoteService.list(page);
        call.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                JSONObject object=new JSONObject(response.body());
                try {
                    total = object.getInt("total");
                    array = object.getJSONArray("list");
                    Log.i("total", total + "");
                    Log.i("length", array.length() + "");
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {

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
                JSONObject obj=array.getJSONObject(position);

                WineVO wineVO = new WineVO();
                wineVO.setWineImage(obj.getString("wine_image"));
                wineVO.setIndex(obj.getInt("index"));
                wineVO.setWineRating(Float.parseFloat(obj.getString("wine_rating")));
                wineVO.setWineCountry(obj.getString("wine_country"));
                wineVO.setWineType(obj.getString("wine_type"));
                wineVO.setWineName(obj.getString("wine_name"));
                wineVO.setWineWinery(obj.getString("wine_winery"));
                wineVO.setWineRegion(obj.getString("wine_region"));
                wineVO.setWinePrice(obj.optString("wine_price", ""));
                wineVO.setFlavor1(obj.optString("flavor1", ""));
                wineVO.setFlavor2(obj.optString("flavor2", ""));
                wineVO.setFlavor3(obj.optString("flavor3", ""));

                String image=obj.getString("wine_image");
                int index = obj.getInt("index");
                Picasso.with(getActivity()).load(image).into(holder.image);
                holder.index.setText(String.valueOf(index));
                float rating=Float.parseFloat(obj.getString("wine_rating"));
                holder.rating.setRating(rating);
                String country=obj.getString("wine_country");
                holder.country.setText(country);
                String type=obj.getString("wine_type");
                holder.type.setText(type);
                String name=obj.getString("wine_name");
                holder.name.setText(name);
                String winery=obj.getString("wine_winery");
                holder.winery.setText(winery);
                String region=obj.getString("wine_region");
                holder.region.setText(region+" "+"/");
                holder.point.setText("("+obj.getString("wine_rating")+")");

                String price = obj.optString("wine_price", "");
                holder.price.setText(price+"원");
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

                String strCountry= country.toLowerCase().replace(" ", "");
                int flagImage;
                switch (strCountry) {
                    case "argentina":
                        flagImage = R.drawable.argentina;
                        break;
                    case "australia":
                        flagImage = R.drawable.australia;
                        break;
                    case "austria":
                        flagImage = R.drawable.austria;
                        break;
                    case "canada":
                        flagImage = R.drawable.canada;
                        break;
                    case "chile":
                        flagImage = R.drawable.chile;
                        break;
                    case "france":
                        flagImage = R.drawable.france;
                        break;
                    case "georgia":
                        flagImage = R.drawable.georgia;
                        break;
                    case "germany":
                        flagImage = R.drawable.germany;
                        break;
                    case "hungary":
                        flagImage = R.drawable.hungary;
                        break;
                    case "israel":
                        flagImage = R.drawable.israel;
                        break;
                    case "italy":
                        flagImage = R.drawable.italy;
                        break;
                    case "newzealnd":
                        flagImage = R.drawable.newzealnd;
                        break;
                    case "portugal":
                        flagImage = R.drawable.portugal;
                        break;
                    case "romania":
                        flagImage = R.drawable.romania;
                        break;
                    case "southafrica":
                        flagImage = R.drawable.southafrica;
                        break;
                    case "spain":
                        flagImage = R.drawable.spain;
                        break;
                    case "switzerland":
                        flagImage = R.drawable.switzerland;
                        break;
                    case "unitedstates":
                        flagImage = R.drawable.unitedstates;
                        break;
                    default:
                        flagImage = R.drawable.flag; // 기본 이미지
                        break;
                }
                holder.ImageView.setImageResource(flagImage);

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ReadActivity.class);
                        intent.putExtra("wineVO", wineVO);
                        startActivity(intent);
                    }
                });

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image, ImageView ;
            TextView name, type, country, price, index,region,taste,winery,point;
            RatingBar rating;
            CardView card;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                card= itemView.findViewById(R.id.card);
                ImageView = itemView.findViewById(R.id.flag);
                image = itemView.findViewById(R.id.image);
                name = itemView.findViewById(R.id.name);
                type = itemView.findViewById(R.id.type);
                country = itemView.findViewById(R.id.country);
                rating = itemView.findViewById(R.id.rating);
                index = itemView.findViewById(R.id.index);
                taste = itemView.findViewById(R.id.taste);
                region= itemView.findViewById(R.id.region);
                price = itemView.findViewById(R.id.price);
                winery= itemView.findViewById(R.id.winery);
                point=itemView.findViewById(R.id.point);

            }
        }
    }
}//fragment