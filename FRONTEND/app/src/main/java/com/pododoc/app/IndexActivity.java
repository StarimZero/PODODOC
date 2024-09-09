package com.pododoc.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        // ImageView를 찾아서 GIF를 로드
        ImageView imageView = findViewById(R.id.main_image_view);
        Glide.with(this)
                .asGif()
                .load(R.drawable.mainlogo) // GIF 파일을 drawable 폴더에 넣고 로드
                .into(imageView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // LoginActivity로 전환
                Intent intent = new Intent(IndexActivity.this, LoginActivity.class);
                startActivity(intent);
                // 현재 IndexActivity 종료
                finish();
            }
        }, 4500); // 4초가적당
    }
}
