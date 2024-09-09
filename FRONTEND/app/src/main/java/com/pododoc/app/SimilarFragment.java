package com.pododoc.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class SimilarFragment extends Fragment {
    private static final String ARG_WINE = "wine";

    public static SimilarFragment newInstance(HashMap<String, Object> wine) {
        SimilarFragment fragment = new SimilarFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WINE, wine);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_similar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 데이터 가져오기
        HashMap<String, Object> wine = (HashMap<String, Object>) getArguments().getSerializable(ARG_WINE);

        if (wine != null) {
            TextView name = view.findViewById(R.id.name);
            TextView ratingPoint = view.findViewById(R.id.ratingPoint);
            RatingBar rating = view.findViewById(R.id.rating);
            TextView price = view.findViewById(R.id.price);
            TextView winery = view.findViewById(R.id.winery);
            TextView region = view.findViewById(R.id.region);
            TextView country = view.findViewById(R.id.country);
            ImageView image = view.findViewById(R.id.image);

            try {
                name.setText(wine.get("wine_name") != null ? wine.get("wine_name").toString() : "Unknown");
                ratingPoint.setText(wine.get("wine_rating") != null ? wine.get("wine_rating").toString() : "0");
                rating.setRating(wine.get("wine_rating") != null ? Float.parseFloat(wine.get("wine_rating").toString()) : 0);
                price.setText(wine.get("wine_price") != null ? wine.get("wine_price").toString() + " 원" : "Unknown");
                winery.setText(wine.get("wine_winery") != null ? wine.get("wine_winery").toString() : "Unknown");
                region.setText(wine.get("wine_region") != null ? wine.get("wine_region").toString() + " /" : "Unknown /");
                country.setText(wine.get("wine_country") != null ? wine.get("wine_country").toString() : "Unknown");
                Picasso.with(getContext()).load(wine.get("wine_image").toString()).into(image);
            } catch (NumberFormatException e) {
                e.printStackTrace(); // 로그를 통해 예외 추적
            } catch (Exception e) {
                e.printStackTrace(); // 일반 예외 처리
            }
        }
    }
}