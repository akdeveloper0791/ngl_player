package com.ibetter.www.adskitedigi.adskitedigi.login;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;

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

public class SendOTPService extends IntentService {

    public final static String STOP_SERVICE_ACTION = "com.ibetter.www.adskitedigi.adskitedigi.login.SendOTPService.STOPServiceReceiver";
    private String intentAction;

    private Context context;

    public SendOTPService()
    {
        super(SendOTPService.class.getName());
    }

    public final static void startServiceCall(Context context,String intentAction,String mobileNumber,String email)
    {
        Intent intent = new Intent(context,SendOTPService.class);
        intent.putExtra("intent_action",intentAction);
        intent.putExtra("mobile_number",mobileNumber);
        intent.putExtra("email",email);
        context.startService(intent);
    }
    public void onHandleIntent(Intent intent)
    {
        context= SendOTPService.this;
      registerStopServiceReceiver();
      intentAction = intent.getStringExtra("intent_action");

      sendOTPRequest(intent.getStringExtra("mobile_number"),
              intent.getStringExtra("email"));
    }

    private void registerStopServiceReceiver()
    {
        IntentFilter intentFilter = new IntentFilter(STOP_SERVICE_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(new STOPServiceReceiver(),intentFilter);
    }

    public final static void StopServiceCall(Context context)
    {
        Intent intent = new Intent(STOP_SERVICE_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        context.sendBroadcast(intent);

    }

    public class STOPServiceReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context,Intent intent)
        {
            unregisterReceiver(this);
            stopSelf();
        }
    }

    private void sendOTPRequest(String mobileNumber,String email)
    {
        RequestBody requestBody = new MultipartBody.Builder().
                setType(MultipartBody.FORM)
                .addFormDataPart("mobile_number",mobileNumber)
                .addFormDataPart("email",email)
                .build();
        Request request = new Request.Builder()
                .post(requestBody)
                .url(GCUtils.SEND_OTP_REGISTER)
                .build();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.MINUTES)
                .writeTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
            sendResponse(false, "Unable to register , please check your internet and try again",null);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String responseString = response.body().string().trim();
            Log.d("SendOTP","response in SendOTP api is - "+responseString.trim());
            handleResponse(responseString);
        }
      });
    }

    private void handleResponse(String response)
    {

        try {
            JSONObject jsonObject = new JSONObject(response);
            Object mobileNumberResponseObj = jsonObject.get("mobileNumberResponse");

            StringBuilder sb =  new StringBuilder();
            boolean isMobileOTPSent = mobileNumberResponseObj instanceof Boolean;

            if(!isMobileOTPSent)
            {
                sb.append("Unable to send OTP to mobile number, "+(String)mobileNumberResponseObj);
                sb.append("\n");

            }else
            {
                sb.append("OTP has been sent to mobile number");
                sb.append("\n");
            }

            Object emailResponseObj = jsonObject.get("emailResponse");
            boolean isEmailOTPSent = emailResponseObj instanceof Boolean;

            if(!isEmailOTPSent)
            {
                sb.append("Unable to send OTP to email, "+(String)emailResponseObj);
                sb.append("\n");
            }else
            {
                sb.append("OTP has been sent to email address.");
                sb.append("\n");
            }


            sendResponse((isMobileOTPSent||isEmailOTPSent),sb.toString(),jsonObject.getString("randomOTP"));
        }catch (Exception ex)
        {
            ex.printStackTrace();
            sendResponse(false,"Unable to parse response"+ex.getMessage(),null);
        }

    }

    private void sendResponse(boolean flag,String status,String otp)
    {
        Intent intent = new Intent(intentAction);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("flag",flag);
        intent.putExtra("status",status);
        if(otp!=null)
        {
            intent.putExtra("otp",otp);
        }
        sendBroadcast(intent);

        StopServiceCall(context);
    }
}
