package com.pododoc.app;

import static com.pododoc.app.RemoteService.BASE_URL;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReadActivity extends AppCompatActivity {
    Retrofit retrofit;
    RemoteService service;
    int index = 0;
    ImageView image, imgChart, flag, heart, reviewPhoto;
    TextView name, ratingPoint, predictPoint, region, price, flavor1, flavor2, flavor3, winery, country;
    RatingBar rating, predictRating;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ArrayList<ReviewVO> array = new ArrayList();
    HashMap<String, Object> vo;
    List<HashMap<String, Object>> wineList;
    ReviewAdapter adapter = new ReviewAdapter();
    SimilarAdapter sAdapter = new SimilarAdapter(ReadActivity.this);
    WineVO wineVO= new WineVO();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        getSupportActionBar().setTitle("와인정보");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();

        index = intent.getIntExtra("index", 0);
        heart = findViewById(R.id.heart);
        image = findViewById(R.id.image);
        imgChart = findViewById(R.id.chart);
        name = findViewById(R.id.name);
        ratingPoint = findViewById(R.id.ratingPoint);
        rating = findViewById(R.id.rating);
        flavor1 = findViewById(R.id.flavor1);
        flavor2 = findViewById(R.id.flavor2);
        flavor3 = findViewById(R.id.flavor3);
        price = findViewById(R.id.price);
        winery = findViewById(R.id.winery);
        flag = findViewById(R.id.flag);
        country = findViewById(R.id.country);
        region = findViewById(R.id.region);
        predictPoint = findViewById(R.id.predictPoint);
        predictRating = findViewById(R.id.predictRating);

        String email = user.getEmail();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RemoteService.class);


        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = db.getReference("/like/" + user.getUid() + "/" + index);
                ref.setValue(wineVO);
                Toast.makeText(ReadActivity.this, "등록성공!", Toast.LENGTH_SHORT).show();
            }
        });

        Call<HashMap<String, Object>> call = service.read(index);
        call.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                vo = response.body();
                Log.i("name", vo.get("wine_name").toString());
                name.setText(vo.get("wine_name").toString());
                Picasso.with(ReadActivity.this).load(vo.get("wine_image").toString()).into(image);
                ratingPoint.setText(vo.get("wine_rating").toString());
                rating.setRating(Float.parseFloat(vo.get("wine_rating").toString()));
                flavor1.setText(vo.get("flavor1").toString());
                flavor2.setText(vo.get("flavor2").toString());
                flavor3.setText(vo.get("flavor3").toString());
                winery.setText(vo.get("wine_winery").toString());
                region.setText(vo.get("wine_region").toString());
                country.setText(vo.get("wine_country").toString());
                String strPrice = vo.get("wine_price").toString();
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
                price.setText(strPrice + "원");
                if(vo.get("acidity").equals("")){
                    wineVO.setAcidity(0.0f);
                }else{
                    wineVO.setAcidity(Float.parseFloat(vo.get("acidity").toString()));
                }
                wineVO.setBody(Float.parseFloat(vo.get("body").toString()));
                wineVO.setSweetness(Float.parseFloat(vo.get("sweetness").toString()));
                wineVO.setTexture(Float.parseFloat(vo.get("texture").toString()));
                wineVO.setWineCountry(vo.get("wine_country").toString());
                wineVO.setWineName(vo.get("wine_name").toString());
                wineVO.setIndex((int)Float.parseFloat(vo.get("index").toString()));
                wineVO.setWineImage(vo.get("wine_image").toString());
                wineVO.setWineRating(Float.parseFloat(vo.get("wine_rating").toString()));
                wineVO.setWineType(vo.get("wine_type").toString());
                wineVO.setWineRegion(vo.get("wine_region").toString());
                wineVO.setWineReviews((int)Float.parseFloat(vo.get("wine_reviews").toString()));
                wineVO.setWineWinery(vo.get("wine_winery").toString());
                wineVO.setFlavor1(vo.get("flavor1").toString());
                wineVO.setFlavor2(vo.get("flavor2").toString());
                wineVO.setFlavor3(vo.get("flavor3").toString());

                // 배열이나 리스트로 뷰와 관련된 데이터를 정의합니다.
                int[] flavorViews = {R.id.flavor1, R.id.flavor2, R.id.flavor3};
                String[] flavorKeys = {"flavor1", "flavor2", "flavor3"};

                TypedArray colorArray = getResources().obtainTypedArray(R.array.colors);
                String[] flavors = getResources().getStringArray(R.array.flavors);

                for (int i = 0; i < flavorViews.length; i++) {
                    // 뷰를 찾아옵니다.
                    View flavorView = findViewById(flavorViews[i]);
                    // 해당 flavor의 값을 가져옵니다.
                    String strFlavor = vo.get(flavorKeys[i]).toString().toLowerCase().replace(" ", "");
                    // flavor의 색상 인덱스를 찾습니다.
                    int colorIndex = Arrays.asList(flavors).indexOf(strFlavor);

                    if (colorIndex >= 0 && colorIndex < colorArray.length()) {
                        // colorArray.getColor는 색상 값을 반환합니다.
                        int color = colorArray.getColor(colorIndex, Color.BLACK);
                        // 배경 색상 설정
                        flavorView.setBackgroundColor(color);
                    } else {
                        // 기본 배경 색상 설정
                        flavorView.setBackgroundResource(R.color.default_background_color);
                    }
                }
                colorArray.recycle();

                String strCountry = vo.get("wine_country").toString().toLowerCase().replace(" ", "");
                TypedArray icons = getResources().obtainTypedArray(R.array.flags);
                String[] countries = getResources().getStringArray(R.array.countries);
                int flagIndex = Arrays.asList(countries).indexOf(strCountry);
                if (flagIndex >= 0) {
                    flag.setImageDrawable(icons.getDrawable(flagIndex));
                } else {
                    flag.setImageResource(R.drawable.flag); // 기본 이미지
                }
                icons.recycle();
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
            }
        });

        Call<ResponseBody> chart = service.get_image(index);
        chart.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                InputStream inputStream = response.body().byteStream();
                // InputStream을 Bitmap으로 변환
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                // 이미지 뷰에 설정
                imgChart.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

        Call<List<HashMap<String, Object>>> similar = service.similar(index);
        similar.enqueue(new Callback<List<HashMap<String, Object>>>() {
            @Override
            public void onResponse(Call<List<HashMap<String, Object>>> call, Response<List<HashMap<String, Object>>> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        wineList = response.body();
                        if (wineList != null) {
                            Log.i("winelist", wineList.toString());
                            Log.i("size", wineList.size() + "");
                            sAdapter.setWineList(wineList);
                            sAdapter.notifyDataSetChanged();
                            getSimilarList();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<HashMap<String, Object>>> call, Throwable t) {
                Log.e("ReadActivity", "Failed to fetch similar wines", t);
            }
        });

        findViewById(R.id.write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReadActivity.this, ReviewInsertActivity.class);
                intent.putExtra("index", index);
                intent.putExtra("url", vo.get("wine_image").toString());
                startActivity(intent);
            }
        });


        // 예측 요청
        predictWineScore(index);
        getReviewList();
        RecyclerView list = findViewById(R.id.list);
        list.setAdapter(adapter);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        list.setLayoutManager(manager);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getSimilarList() {
        RecyclerView similarList = findViewById(R.id.similarList);
        similarList.setAdapter(sAdapter);
        StaggeredGridLayoutManager sManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        similarList.setLayoutManager(sManager);
    }


    //리뷰목록
    public void getReviewList() {
        firestore.collection("review")
                .whereEqualTo("index", index)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            ReviewVO vo = new ReviewVO();
                            vo.setId(doc.getId());
                            // photo 필드 null 체크
                            if (doc.getData().get("photo") == null) {
                                vo.setPhoto("");
                            } else {
                                vo.setPhoto(doc.getData().get("photo").toString());
                            }
                            vo.setIndex(Integer.parseInt(doc.getData().get("index").toString()));
                            vo.setEmail(doc.getData().get("email").toString());
                            vo.setContents(doc.getData().get("contents").toString());
                            vo.setDate(doc.getData().get("date").toString());
                            float rating = Float.parseFloat(doc.getData().get("rating").toString());
                            vo.setRating(rating);
                            Log.i("vo", vo.toString());
                            array.add(vo);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    //ReviewAdapter
    class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = getLayoutInflater().inflate(R.layout.item_review, parent, false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ReviewVO vo = array.get(position);
            holder.email.setText(vo.getEmail());
            holder.contents.setText(vo.getContents());
            holder.date.setText(vo.getDate());
            holder.ratingBar.setRating(vo.getRating());

            if (vo.getPhoto().equals("")) {
                holder.reviewPhoto.setImageResource(R.drawable.person);
            } else {
                Picasso.with(ReadActivity.this).load(vo.getPhoto()).into(holder.reviewPhoto);
            }

            // 클릭 이벤트
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ReadActivity.this, ReviewReadActivity.class);
                intent.putExtra("id", vo.getId());
                intent.putExtra("email",vo.getEmail());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return array.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView email, contents, date;
            RatingBar ratingBar;
            ImageView reviewPhoto;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                email = itemView.findViewById(R.id.email);
                contents = itemView.findViewById(R.id.contents);
                date = itemView.findViewById(R.id.date);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                reviewPhoto = itemView.findViewById(R.id.reviewPhoto);
            }
        }
    }

    public static class SimilarAdapter extends RecyclerView.Adapter<SimilarAdapter.SViewHolder> {
        private final Context context;

        private List<HashMap<String, Object>> wineList;

        public SimilarAdapter(Context context) {
            this.context = context;
        }

        public void setWineList(List<HashMap<String, Object>> wineList) {
            this.wineList = wineList;
        }

        @NonNull
        @Override
        public SViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_similar, parent, false);
            return new SViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SViewHolder holder, int position) {
            HashMap<String, Object> wine = wineList.get(position);
            holder.name.setText(wine.get("wine_name").toString());
            holder.ratingPoint.setText(wine.get("wine_rating").toString());
            holder.rating.setRating(Float.parseFloat(wine.get("wine_rating").toString()));
            holder.winery.setText(wine.get("wine_winery").toString());
            holder.region.setText(wine.get("wine_region").toString());
            holder.country.setText(wine.get("wine_country").toString());
            Picasso.with(holder.itemView.getContext()).load(wine.get("wine_image").toString()).into(holder.image);

            String strCountry = wine.get("wine_country").toString().toLowerCase().replace(" ", "");
            TypedArray icons = context.getResources().obtainTypedArray(R.array.flags);
            String[] countries = context.getResources().getStringArray(R.array.countries);
            int flagIndex = Arrays.asList(countries).indexOf(strCountry);
            if (flagIndex >= 0) {
                holder.flag.setImageDrawable(icons.getDrawable(flagIndex));
            } else {
                holder.flag.setImageResource(R.drawable.flag); // 기본 이미지
            }
            icons.recycle();

            holder.linearSimilar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ReadActivity.class);
                    String indexString = wine.get("index").toString();
                    int index = (int) Float.parseFloat(indexString);
                    intent.putExtra("index", index);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return wineList.size();
        }

        class SViewHolder extends RecyclerView.ViewHolder {
            LinearLayout linearSimilar;
            ImageView image, flag;
            TextView name, ratingPoint, winery, region, country, predictPoint;
            RatingBar rating, predictRating;

            public SViewHolder(@NonNull View itemView) {
                super(itemView);
                linearSimilar = itemView.findViewById(R.id.linearSimilar);
                image = itemView.findViewById(R.id.image);
                name = itemView.findViewById(R.id.name);
                ratingPoint = itemView.findViewById(R.id.ratingPoint);
                rating = itemView.findViewById(R.id.rating);
                winery = itemView.findViewById(R.id.winery);
                region = itemView.findViewById(R.id.region);
                country = itemView.findViewById(R.id.country);
                flag = itemView.findViewById(R.id.flag);
                predictPoint = itemView.findViewById(R.id.predictPoint);
                predictRating = itemView.findViewById(R.id.predictRating);
            }
        }
    }

    @Override
    protected void onRestart() {
        array.clear();
        getReviewList();
        super.onRestart();
    }

    private void predictWineScore(int index) {
        String email = user.getEmail();
        Call<ResponseBody> call = service.predict(email,index);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // 서버의 응답을 문자열로 변환
                        String responseBody = response.body().string();
                        Log.i("Prediction", "Predicted score: " + responseBody);
                        // 여기에 예측 결과를 처리하는 로직 추가

                        // JSON 객체로 변환
                        JSONObject jsonObject = new JSONObject(responseBody);

                        // 예측 점수 추출
                        double predictedScore = jsonObject.optDouble("predicted_score", -1);

                        if (predictedScore != -1) {
                            // 예측 점수를 화면에 표시
                            predictPoint.setText(String.format("%.1f", predictedScore));
                            predictRating.setRating((float) predictedScore);

                        } else {
                            Toast.makeText(ReadActivity.this, "No predicted score found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ReadActivity.this, "Error parsing prediction result", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReadActivity.this, "Error retrieving prediction", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ReadActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

