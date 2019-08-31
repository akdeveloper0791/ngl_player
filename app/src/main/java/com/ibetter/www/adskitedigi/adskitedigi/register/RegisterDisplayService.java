package com.ibetter.www.adskitedigi.adskitedigi.register;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils.LICENCE_REGISTER;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class RegisterDisplayService  extends IntentService
{
   private Context context;
   private String intentAction;

    public RegisterDisplayService()
    {
        super("RegisterDisplayService");

    }

    public void onHandleIntent(Intent intent)
    {

        context = RegisterDisplayService.this;
        intentAction = intent.getStringExtra(context.getString(R.string.app_default_intent_action_text));


        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("mac", new DeviceModel().getMacAddress());
            jsonObject.put("mobile_number", new User().getUserMobileNumber(context));
            String email =  new User().getUserEmailAddress(context);
            if(email!=null) {
                jsonObject.put("org_email", email);
            }

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", jsonObject.toString())
                    .build();

            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(LICENCE_REGISTER)
                    .build();

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendErrorResponse(false, "Unable to register , please check your internet and try again", getString(R.string.no_internet_error_code));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string().trim();
                    //Log.d("TestRestApi","response in api is - "+responseString.trim());
                    handleResponse(responseString);
                }
            });

        }
        catch (Exception E)
        {
            sendErrorResponse(false, "Unable to register ,", getString(R.string.no_internet_error_code));
        }


    }

    private void handleResponse(String response)
    {

        Log.i("response",response);
        try
        {
            JSONObject info = new JSONObject(response);

            Log.i("info response",response);

            if(info.getInt("statusCode")==0)
            {
                int status = info.getInt("d_status");
                String expiryDate = info.getString("expiry_date");
                saveLicenseDetails(status,expiryDate);
                sendResponse(true,"Registered Successfully",String.valueOf(status));

            }else
            {
                sendErrorResponse(false,info.getString("status"),getString(R.string.internal_server_error_code));
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
            sendErrorResponse(false,"Unable to register"+e.getMessage(),getString(R.string.internal_server_error_code));
        }
    }


    private void sendResponse(boolean flag,String message,String  successCode)
    {

        Log.i(message,"sendResponse");

        Intent intent = new Intent(intentAction);
        intent.putExtra(context.getString(R.string.app_default_flag_text), flag);
        intent.putExtra(context.getString(R.string.app_default_success_msg_text),message);
        intent.putExtra(context.getString(R.string.app_default_success_code_text), successCode);
        sendBroadcast(intent);

    }

    //send response
    private void sendErrorResponse(boolean flag, String errorMsg, String errorCode) {

        Log.i("intent_error_response",intentAction);

        Intent intent = new Intent(intentAction);
        intent.putExtra(context.getString(R.string.app_default_flag_text), flag);
        intent.putExtra(context.getString(R.string.app_default_error_code_text), errorCode);
        intent.putExtra(context.getString(R.string.app_default_error_msg_text), errorMsg);
        sendBroadcast(intent);

    }


    private void saveLicenseDetails(int status,String expiryDate)
    {

        SimpleDateFormat expiryCheckDateSDF = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String expiryDateCheckedAt = expiryCheckDateSDF.format(calendar.getTime());
        User.saveDeviceLicenceDetails(context,status,expiryDate,expiryDateCheckedAt);

    }

}
