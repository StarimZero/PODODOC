package com.pododoc.app;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RemoteService {
    public static final String BASE_URL = "http://192.168.0.11:5000/";

    @GET("wine/list.json")
    Call<HashMap<String,Object>> list(@Query("page") int page);

    @GET("wine/basicred.json")
    Call<HashMap<String, Object>> basicRed(@Query("page") int page, @Query("price") String price);

    @GET("wine/basicwhite.json")
    Call<HashMap<String, Object>> basicWhite(@Query("page") int page, @Query("price") String price);

}
