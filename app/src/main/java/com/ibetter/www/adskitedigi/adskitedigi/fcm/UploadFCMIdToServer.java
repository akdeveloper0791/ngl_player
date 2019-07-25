package com.ibetter.www.adskitedigi.adskitedigi.fcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadFCMIdToServer extends IntentService {
    private final static String fcmREQUEST_PARAM="fcm";
    private final static String intentActionREQEST_PARAM = "intent_action";
    private String intentAction;
    private Context context;

    public UploadFCMIdToServer()
    {
        super("UploadFCMIdToServer");
        context = UploadFCMIdToServer.this;
    }

    public static void startService(Context context,String intentAction,String FCMTokenId)
    {
        Intent intent = new Intent(context,UploadFCMIdToServer.class);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(intentActionREQEST_PARAM,intentAction);
        intent.putExtra(fcmREQUEST_PARAM,FCMTokenId);
        context.startService(intent);
    }

    protected void onHandleIntent(Intent intent)
    {
        intentAction = intent.getStringExtra(intentActionREQEST_PARAM);

        //upload code
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("player", String.valueOf(User.getPlayerId(context)))
                .addFormDataPart("p_mac",User.getPlayerMac(context))
                .addFormDataPart("fcm",intent.getStringExtra(fcmREQUEST_PARAM))
                .build();
        String url = getUrl();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        try
        {
            Response response =  okHttpClient.newCall(request).execute();
            String responseString = response.body().string().trim();
            //Log.d("UploadFCM","Inside upload fcm service response"+responseString);
            processInfo(responseString);
        }catch(IOException ex)
        {
            //sendResponse
            sendResponse(false,"Please check your internet connection");
        }

    }

    private void processInfo(String responseString)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(responseString);
           if(jsonObject.getInt("statusCode") == 0)
           {
               sendResponse(true,jsonObject.getString("status"));
           }else
           {
               sendResponse(false,jsonObject.getString("status"));
           }
        }catch(JSONException ex)
        {
            sendResponse(false,"Unable to process info "+ex.getMessage());
        }
    }

    private void sendResponse(boolean flag,String status)
    {
        Intent intent = new Intent(intentAction);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("flag",flag);
        intent.putExtra("status",status);
        sendBroadcast(intent);
    }

    private String getUrl()
    {
        int mode=new User().getUserPlayingMode(context);
        String userId=new User().getGCUserUniqueKey(context);


        if(mode== Constants.CLOUD_MODE)
        {
            if(userId!=null)
            {
                return GCUtils.GC_PLAYER_REFRESH_FCM_URL;
            }else
            {
                return null;
            }
        }else
        if(mode== Constants.ENTERPRISE_MODE)
        {
            if(userId!=null)
            {
                return new User().getEnterPriseURL(context)+GCUtils.ENTERPRISE_PLAYER_REFRESH_FCM_URL;
            }else
            {
                return null;
            }
        }
        else
        {
            return null;
        }

    }
}
