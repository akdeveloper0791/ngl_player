package com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TestRestApi extends IntentService {

    public TestRestApi()
    {
        super("TestRestApi");
    }

   public void onHandleIntent(Intent intent)
   {
       String url = "https://www.greencontent.in/campaigns/list_my_campaigns/";

       //OkHttpClient httpClient = new OkHttpClient();

       RequestBody requestBody = new MultipartBody.Builder()
               .setType(MultipartBody.FORM)
               .addFormDataPart("secretKey","7dd9d8c065424f7ea2845add69b7171e")
               .addFormDataPart("password","!Vin497520")
               .build();

       Request request = new Request.Builder()
               .url(url)
               .post(requestBody)
               .build();
       //  httpClient.newCall(request).execute();

       Log.i("TestRestApi","inside before request");

       OkHttpClient httpClient = new OkHttpClient.Builder()
               .connectTimeout(1, TimeUnit.SECONDS)
               .writeTimeout(1, TimeUnit.SECONDS)
               .readTimeout(2, TimeUnit.SECONDS)
               .build();

       httpClient.newCall(request).enqueue(new Callback() {
           @Override
           public void onFailure(okhttp3.Call call, IOException e) {
               e.printStackTrace();
               Log.d("TestRestApi","response in api error in Test rest api - "+e.getMessage());
           }

           @Override
           public void onResponse(okhttp3.Call call, Response response) throws IOException {
               Log.d("TestRestApi","response in api is - "+response.body().string().trim());

           }
       });


   }
}
