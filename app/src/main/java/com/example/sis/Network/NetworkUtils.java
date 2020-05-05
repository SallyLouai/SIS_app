package com.example.sis.Network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkUtils {

    private static Retrofit retrofit;
    private APIEndPoint apiEndPoint;

    //private User user;

    public APIEndPoint getApiEndPoint() {
        return apiEndPoint;
    }

    private static OkHttpClient buildClient() {
        return new OkHttpClient
                .Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    public NetworkUtils() {
        retrofit = new Retrofit.Builder()
                .client(buildClient())
                .baseUrl("http://sisksa.aou.edu.kw/MobileApp/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiEndPoint = retrofit.create(APIEndPoint.class);
    }


}