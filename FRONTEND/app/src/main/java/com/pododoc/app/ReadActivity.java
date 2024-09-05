package com.pododoc.app;

import static com.pododoc.app.RemoteService.BASE_URL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Rating;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
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
    ImageView image, imgChart;
    TextView name, txtIndex, ratingPoint, predictPoint, price, flavor1, flavor2, flavor3, winery;
    RatingBar rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        getSupportActionBar().setTitle("와인정보");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        index = intent.getIntExtra("index", 0);

        image = findViewById(R.id.image);
        imgChart = findViewById(R.id.chart);
        name = findViewById(R.id.name);
        txtIndex = findViewById(R.id.index);
        ratingPoint = findViewById(R.id.ratingPoint);
        rating = findViewById(R.id.rating);
        flavor1 = findViewById(R.id.flavor1);
        flavor2 = findViewById(R.id.flavor2);
        flavor3 = findViewById(R.id.flavor3);
        price = findViewById(R.id.price);
        winery = findViewById(R.id.winery);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RemoteService.class);

        Call<HashMap<String, Object>> call = service.read(index);
        call.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                HashMap<String, Object> vo = response.body();
                Log.i("name", vo.get("wine_name").toString());
                name.setText(vo.get("wine_name").toString());
                Picasso.with(ReadActivity.this).load(vo.get("wine_image").toString()).into(image);
                ratingPoint.setText(vo.get("wine_rating").toString());
                rating.setRating(Float.parseFloat(vo.get("wine_rating").toString()));
                flavor1.setText(vo.get("flavor1").toString());
                flavor2.setText(vo.get("flavor2").toString());
                flavor3.setText(vo.get("flavor3").toString());
                winery.setText(vo.get("wine_winery").toString());
                price.setText(vo.get("wine_price").toString() + " 원");
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}