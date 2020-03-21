package com.ibetter.www.adskitedigi.adskitedigi.iot_devices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils.GC_IOT_DEVICE_REGISTER_URL;

public class RegisterIOTDevice extends IntentService
{
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.ibetter.www.adskitedigi.adskitedigi.login.action.FOO.RegisterIOTDevice";

    private Context context;

    private ResultReceiver receiver;
    public static final int REGISTER_IOT_DEVICE_ACTION=01;

    public RegisterIOTDevice() {
        super("RegisterIOTDevice");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            context = RegisterIOTDevice.this;

            if(intent.getExtras().containsKey("receiver"))
            {
                receiver = intent.getParcelableExtra("receiver");
            }

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("name",new DeviceModel().getDeviceModelName(context));
            jsonObject.put("mac",new DeviceModel().getMacAddress());
            jsonObject.put("device_type","camera");

            int playingMode=new User().getUserPlayingMode(context);


            String URL = null;
            RequestBody requestBody=null;
            if (playingMode == Constants.CLOUD_MODE) {
                URL = GC_IOT_DEVICE_REGISTER_URL;
                 requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user_email", new User().getGCUserMailId(context))
                        .addFormDataPart("pwd",new User().getGCUserPwd(context))
                        .addFormDataPart("data", jsonObject.toString())
                        .build();
            }
            else
            {
            String    enterpriseUrl=new User().getEnterPriseURL(context);
                if (enterpriseUrl != null && enterpriseUrl.length() > 1) {
                    URL = enterpriseUrl + "iot_device/register";
                     requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("user_email", new User().getEnterpriseUserMailId(context))
                            .addFormDataPart("pwd",new User().getEnterpriseUserPwd(context))
                            .addFormDataPart("data", jsonObject.toString())
                            .build();
                }
            }

            Log.i("url",URL+"");


            if (URL != null&&requestBody!=null) {
                Request request = new Request.Builder()
                        .post(requestBody)
                        .url(URL)
                        .build();

                OkHttpClient httpClient = new OkHttpClient.Builder()
                        .connectTimeout(1, TimeUnit.MINUTES)
                        .writeTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.MINUTES)
                        .build();

                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        sendStatus(false, "Unable to register , please check your internet and try again");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString = response.body().string().trim();
                        //Log.d("TestRestApi","response in api is - "+responseString.trim());
                        handleResponse(responseString);
                    }
                });
            } else {
                sendStatus(false, "Url is not found");
            }


        }catch (Exception E)
        {
            sendStatus(false,"Unable to login"+E.getMessage());
            E.printStackTrace();
        }
    }

    private void handleResponse(String response)
    {
        try
        {
            Log.i("info response",response);
            JSONObject info = new JSONObject(response);

            Log.i("info response",response);

            if(info.getInt("statusCode")==0)
            {
                //success,, save response to cache
                SharedPreferences.Editor userInfoEditor = new SharedPreferenceModel().getIOTDevicesSharedPreference(this).edit();

                userInfoEditor.putLong(getString(R.string.iot),info.getLong("iot"));
                userInfoEditor.putString(getString(R.string.iot_key),info.getString("key"));

                boolean isCommit = userInfoEditor.commit();

                if(isCommit)
                {
                    sendStatus(true,info.getString("status"));

                }else
                {
                    sendStatus(false,"Unable to register, please try again later");
                }


            }else
            {
                sendStatus(false,info.getString("status"));
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
            sendStatus(false,"Unable to register"+e.getMessage());
        }
    }

    private void sendStatus(boolean flag,String status)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",flag);
        bundle.putString("status",status);
        //bundle.putString("resultString",resultString);
        if(receiver!=null)
        {
            receiver.send(REGISTER_IOT_DEVICE_ACTION,bundle);
        }
    }
}
