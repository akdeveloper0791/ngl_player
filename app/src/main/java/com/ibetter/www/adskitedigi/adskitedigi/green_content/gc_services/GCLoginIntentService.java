package com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;


import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class GCLoginIntentService extends IntentService
{
    private Context context;
    private HttpURLConnection urlConnection;
    private ResultReceiver receiver;

    public static final int LOGIN_SUCCESS =240;

    public GCLoginIntentService()
    {
        super(GCLoginIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent( Intent intent)
    {
        context=GCLoginIntentService.this;

        receiver = intent.getParcelableExtra("receiver");
        String emailId=intent.getStringExtra("email_id");
        String password=intent.getStringExtra("password");

        if(emailId!=null && password!=null)
        {
            checkUserLogin(emailId,password);
        }else
        {
            sendResponse(false,"Invalid User Info, Please Try Again");
        }

    }

    private void checkUserLogin(String emailId,String password)
    {
        try {

            urlConnection = (HttpURLConnection) GCUtils.makePostRequest("POST", GCUtils.GC_LOGIN_API_URI_STRING,emailId,password,String.valueOf(new User().getPlayerId(context)));

            InputStream inputStream;
            // get stream
            if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }
            // parse response stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp,response="";
            while ((temp = bufferedReader.readLine()) != null)
            {
                response+= temp;
            }
            Log.i("GCLoginIntentService","API Response:"+response);

            processResponse(response);

        } catch (IOException e)
        {
            e.printStackTrace();
            sendResponse(false,e.toString());

        } finally {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }
    }

    private void processResponse(String response)
    {
        try
        {
            JSONObject responseObject=new JSONObject(response);
            boolean status=responseObject.getBoolean("status");

            if(status)
            {

                if(saveGCUserInfoInSP(responseObject))
                {
                    sendResponse(true,"User Login is Successful");
                }
                else
                {
                    sendResponse(false,"Unable to Login, Please Try Again After Some Time");
                }

            }
            else
            {
                sendResponse(false,responseObject.getString("res"));
            }

        }catch (Exception e)
        {
            e.printStackTrace();
            sendResponse(false,e.toString());
        }
    }

    //save Green Content user login credentials in SP
    private boolean saveGCUserInfoInSP(JSONObject jsonObject)throws JSONException
    {
        JSONObject infoObject=jsonObject.getJSONObject("res");
        String email=infoObject.getString("email");
        int userId=infoObject.getInt("id");
        String uniqueKey=infoObject.getString("user_unique_key");
        String firstName=infoObject.getString("first_name");

        SharedPreferences.Editor userInfoEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userInfoEditor.putInt(context.getString(R.string.gc_user_id),userId);
        userInfoEditor.putString(context.getString(R.string.gc_user_unique_key),uniqueKey);
        userInfoEditor.putString(context.getString(R.string.gc_user_email_id),email);
        userInfoEditor.putString(context.getString(R.string.gc_user_first_name),firstName);
        return userInfoEditor.commit();
    }

    private void sendResponse(boolean flag,String status)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",flag);
        bundle.putString("status",status);
        receiver.send(LOGIN_SUCCESS,bundle);
    }

    @Override
    public void onDestroy()
    {
        if(urlConnection != null)
        {
            urlConnection.disconnect();
        }
        super.onDestroy();
    }
}