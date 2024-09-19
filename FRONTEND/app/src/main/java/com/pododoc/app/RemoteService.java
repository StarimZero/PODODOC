package com.pododoc.app;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RemoteService {
    public static final String BASE_URL = "http://192.168.0.238:5000/";

    public static final int CAMERA_REQUEST = 1888;

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

    @GET("/map")
    Call<ResponseBody> getMapData(@Query("email") String email);

    @GET("/predict")
    Call<ResponseBody> predict(@Query("email") String email, @Query("index") int index);

    @GET("similar/{index}")
    Call<List<HashMap<String,Object>>> similar(@Path("index") int index);

    @GET("mywine")
    Call<HashMap<String, Object>> getMyWine(@Query("email") String email);

    @GET("wine/redrecommend")
    Call<HashMap<String, Object>> redmain(@Query("email") String email, @Query("price") String price);

    @GET("wine/whiterecommend")
    Call<HashMap<String, Object>> whitemain(@Query("email") String email, @Query("price") String price);

    @GET("/ratingGraph")
    Call<ResponseBody> ratingGraphImage(@Query("email") String email);
}
