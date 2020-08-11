package com.example.actionaidactivista.retrofit;

import com.example.actionaidactivista.urls.hostserveraddress;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    //public static final String BASE_URL = "http://10.0.2.2:81/byro/retrofit/";
    //public static final String BASE_URL = hostserveraddress.BASE_URL();//hotspot
    //public static final String BASE_URL = hostserveraddress.BASE_URL_2();//default for emulator
    public static final String BASE_URL = hostserveraddress.BASE_SERVER_URL();//online server
    public static Retrofit retrofit = null;

    public static Retrofit getApiClient(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10,TimeUnit.MINUTES)
                .readTimeout(30,TimeUnit.MINUTES)
                .writeTimeout(30,TimeUnit.MINUTES)
                .build();

        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
