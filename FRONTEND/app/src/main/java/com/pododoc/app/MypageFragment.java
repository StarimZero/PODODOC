// MypageFragment.java
package com.pododoc.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.ResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MypageFragment extends Fragment {
    private Retrofit retrofit;
    private RemoteService remoteService;
    private WebView webView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private static final int MAX_RETRIES = 10;
    private int retryCount = 0;
    EditText emailEditText;
    Button myWineButton;
    ImageView ratingGraph;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        // Initialize Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(RemoteService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) // Use GsonConverterFactory for JSON
                .build();
        remoteService = retrofit.create(RemoteService.class);

        ratingGraph = view.findViewById(R.id.ratingGraph);

        // Initialize WebView
        webView = view.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());


        myWineButton = view.findViewById(R.id.mywine);

        myWineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MywineActivity.class);
                startActivity(intent);
            }
        });

        emailEditText = view.findViewById(R.id.email);

        if (user != null) {
            emailEditText.setText(user.getEmail());
        }


        // Load map data
        loadMapData(String.valueOf(user));

        Button logoutButton = view.findViewById(R.id.logout);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

                getActivity().finish();

            }
        });

        getRatingGraph();

        return view;
    }

    private void loadMapData(String email) {
        Call<ResponseBody> call = remoteService.getMapData(email);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("mapResponse", "Response code: " + response.code()); // Log response code
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Read the HTML content from ResponseBody
                        InputStream inputStream = response.body().byteStream();
                        String htmlContent = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

                        // Load the HTML content into WebView
                        webView.loadDataWithBaseURL(null, htmlContent, "text/html", StandardCharsets.UTF_8.name(), null);

                        // Reset retry count after successful response
                        retryCount = 0;

                    } catch (Exception e) {
                        Log.wtf("mapError", e.toString());
                    }
                } else {
                    Log.e("mapError", "Response not successful or body is null: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("mapError", "Network request failed", t);
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.i("mapRetry", "Retrying request (" + retryCount + "/" + MAX_RETRIES + ")");

                    // Retry after a delay
                    new Handler().postDelayed(() -> loadMapData(email), 2000);
                } else {
                    Log.e("mapError", "Max retry attempts reached");
                }
            }
        });
    }

    public void getRatingGraph() {
        if (user != null) {
            String email = user.getEmail();
            Call<ResponseBody> ratingGraphImage = remoteService.ratingGraphImage(email);
            ratingGraphImage.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    InputStream inputStream = response.body().byteStream();
                    // InputStream을 Bitmap으로 변환
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    // 이미지 뷰에 설정
                    ratingGraph.setImageBitmap(bitmap);
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getRatingGraph();
    }
}
