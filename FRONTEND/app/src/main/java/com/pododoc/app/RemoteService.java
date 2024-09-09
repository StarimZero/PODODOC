package com.pododoc.app;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RemoteService {
    public static final String BASE_URL = "http://192.168.0.11:5000/";

    @GET("wine/list.json")
    Call<HashMap<String,Object>> list(@Query("page") int page);

    @GET("wine/basicred.json")
    Call<HashMap<String, Object>> basicRed(@Query("page") int page, @Query("price") String price);

    @GET("wine/basicwhite.json")
    Call<HashMap<String, Object>> basicWhite(@Query("page") int page, @Query("price") String price);

    @GET("/wine/{index}")
    Call<HashMap<String,Object>> read(@Path("index") int index);

    @GET("/image/{index}")
    Call<ResponseBody> get_image(@Path("index") int index);

    @GET("search")
    Call<HashMap<String, Object>> search(@Query("query") String query, @Query("page") int page, @Query("size") int size);

    @GET("similar/{index}")
    Call<List<HashMap<String,Object>>> similar(@Path("index") int index);
}
