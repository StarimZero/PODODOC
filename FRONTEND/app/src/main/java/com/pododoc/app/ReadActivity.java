package com.pododoc.app;

import static com.pododoc.app.RemoteService.BASE_URL;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Rating;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser user=mAuth.getCurrentUser();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ArrayList<ReviewVO> array= new ArrayList();
    HashMap<String, Object> vo;
    ReviewAdapter adapter = new ReviewAdapter();

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

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RemoteService.class);

        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref=db.getReference("/like/" + user.getUid() + "/" + index);
                ref.setValue(index);
                Toast.makeText(ReadActivity.this,"등록성공!", Toast.LENGTH_SHORT).show();
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
                region.setText(vo.get("wine_region").toString() + " /");
                country.setText(vo.get("wine_country").toString());
                price.setText(vo.get("wine_price").toString() + " 원");

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
                TypedArray icons= getResources().obtainTypedArray(R.array.flags);
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

        findViewById(R.id.write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReadActivity.this, ReviewInsertActivity.class);
                intent.putExtra("index",index);
                intent.putExtra("url",vo.get("wine_image").toString());
                startActivity(intent);
            }
        });


        // 예측 요청
        predictWineScore(index);

        getReviewList();
        ListView list =findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    //리뷰목록
    public void getReviewList(){
        firestore.collection("review")
                .whereEqualTo("index",index)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot doc:task.getResult()){
                            ReviewVO vo = new ReviewVO();
                            vo.setId(doc.getId());
                            // photo 필드 null 체크
                           if(doc.getData().get("photo")==null){
                               vo.setPhoto("");
                           }else {
                               vo.setPhoto(doc.getData().get("photo").toString());
                           }
                            vo.setIndex(Integer.parseInt(doc.getData().get("index").toString()));
                            vo.setEmail(doc.getData().get("email").toString());
                            vo.setContents(doc.getData().get("contents").toString());
                            vo.setDate(doc.getData().get("date").toString());
                            float rating= Float.parseFloat(doc.getData().get("rating").toString());
                            vo.setRating(rating);
                            Log.i("vo",vo.toString());
                            array.add(vo);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    //adapter
    class ReviewAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return array.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.item_review,viewGroup,false);
            TextView email,contents,date;
            RatingBar ratingBar;
            ReviewVO vo= array.get(i);

            email= view.findViewById(R.id.email);
            email.setText(vo.getEmail());

            contents=view.findViewById(R.id.contents);
            contents.setText(vo.getContents());

            date= view.findViewById(R.id.date);
            date.setText(vo.getDate());

            ratingBar = view.findViewById(R.id.ratingBar);
            ratingBar.setRating(vo.getRating());

            reviewPhoto = view.findViewById(R.id.reviewPhoto);
            if(vo.getPhoto().equals("")){
                reviewPhoto.setImageResource(R.drawable.person);
            }else {
                Picasso.with(ReadActivity.this).load(vo.getPhoto()).into(reviewPhoto);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ReadActivity.this, ReviewReadActivity.class);
                    intent.putExtra("id",vo.getId());
                    startActivity(intent);
                }
            });
            return view;
        }
    }
    @Override
    protected void onRestart() {
        array.clear();
        getReviewList();
        super.onRestart();
    }

    private void predictWineScore(int index) {
        Call<ResponseBody> call = service.predict(index);
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
                            predictRating.setRating((float) predictedScore); // RatingBar에 점수 설정
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