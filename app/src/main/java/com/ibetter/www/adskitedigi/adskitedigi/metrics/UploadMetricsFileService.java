package com.ibetter.www.adskitedigi.adskitedigi.metrics;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.iot_devices.IOTDevice;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.internal.MetricsService;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class UploadMetricsFileService extends IntentService
{
    private Context context;
    private String filePath;


    private ResultReceiver receiver;
    public static final int UPLOAD_FILE_ACTION=501;

    public UploadMetricsFileService()
    {
        super("UploadMetricsFileService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        context= UploadMetricsFileService.this;

        filePath=intent.getStringExtra("file_path");
        if(intent.getExtras().containsKey("receiver"))
        {
            receiver = intent.getParcelableExtra("receiver");
        }
        Log.i("UploadMetricsFileSvice", "UploadMetricsFileService filepath:" +filePath);

        checkAndUploadFile();

    }


    private void checkAndUploadFile()
    {
        if(filePath!=null)
        {
            File file=new File(filePath);
            if(file.exists())
            {
                uploadFile(file);
            }else
            {

                sendResponse(false);
            }

        }else
        {
            sendResponse(false);
        }


    }


    public void uploadFile(File file)
    {
        try {
            String url=getURL();
            if(url!=null)
            {
                RequestBody file_body = RequestBody.create(MediaType.parse("file/*"), file);
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        //.addFormDataPart("text_file", file.getName(), com.squareup.okhttp.RequestBody.create(com.squareup.okhttp.MediaType.parse("file/*"), file)
                        .addFormDataPart("file", file.getName(), file_body)
                        .addFormDataPart("player", String.valueOf(IOTDevice.getDeviceId(context)))
                        .addFormDataPart("p_key", IOTDevice.getIOTDeviceKey(context))

                        .build();

                //  MultipartBody requestBody = GCUtils.uploadRequestBody(CampaignModel.getFileNameWithOutExt(campaignName),String.valueOf(campId),new User().getGCUserUniqueKey(context), file);

                CountingRequestBody monitoredRequest = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
                    @Override
                    public void onRequestProgress(long bytesWritten, long contentLength) {
                        //Update a progress bar with the following percentage
                        float percentage = 100f * bytesWritten / contentLength;
                        if (percentage >= 0) {
                            //TODO: Progress bar

                            Log.d("progress ", percentage + "");
                        } else {
                            //Something went wrong
                            Log.d("No progress ", 0 + "");

                        }
                    }
                });

                Request request = new Request.Builder().url(url).post(monitoredRequest).build();

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(2, TimeUnit.MINUTES)
                        .writeTimeout(2, TimeUnit.MINUTES)
                        .readTimeout(2, TimeUnit.MINUTES)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Handle the error
                        Log.i("camera", "inside update metrics onFailure-" + e.getMessage());

                        sendResponse(false);
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                        String result = response.body().string();

                        Log.i("camera", " inside update metrics reposense " + result);

                        if (!response.isSuccessful())
                        {
                            // Handle the error

                            Log.i("camera", "reposense failure");
                            sendResponse(false);
                        } else {
                            // Upload successful
                            // onDownloadComplete();
                            // Log.i("UploadCampaignService", "UploadTextFileService response:"+result);
                            processResponse(result);

                        }

                    }
                });

            }else
            {

                sendResponse(false);
            }
         // String response = GCUtils.POSTRequest(client, GCUtils.UPLOAD_CAMPAIGN_SUPPORT_FILE_URL, monitoredRequest);

        } catch (Exception ex) {
            // Handle the error
            ex.printStackTrace();
            sendResponse(false);
        }

    }


    private void processResponse(String result)
    {

        Log.i("result",result);
        try
        {
            JSONObject jsonObject=new JSONObject(result);
            int statusCode=jsonObject.getInt("statusCode");
            if(statusCode==0)
            {
                //check and handle rule
                checkAndHandleRule(jsonObject);
                sendResponse(true);
            }else
            {

                sendResponse(false);
            }
        }catch (Exception e)
        {
            sendResponse(false);
            e.printStackTrace();
        }

    }

    private void checkAndHandleRule(JSONObject jsonObject) throws JSONException
    {
       if(jsonObject.has("rule"))
       {
           ProcessRule.startService(context,jsonObject.getString("rule"),jsonObject.getString("push_time"),
                   jsonObject.getInt("delay_time"));


       }
    }


    private String getURL()
    {
        int mode=new User().getUserPlayingMode(context);
        String userId=new User().getGCUserUniqueKey(context);
        if(mode== Constants.CLOUD_MODE)
        {
            if(userId!=null)
            {
                return GCUtils.UPLOAD_METRICS_FILE_URL;
            }else
            {
                return null;
            }
        }
        else if(mode== Constants.ENTERPRISE_MODE)
        {
            if(userId!=null)
            {
                return new User().getEnterPriseURL(context)+ GCUtils.UPLOAD_METRICS_FILE_URL_END_POINT;
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
    private void sendResponse(boolean flag)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",flag);
        //bundle.putString("resultString",resultString);
        if(receiver!=null)
        {
            receiver.send(UPLOAD_FILE_ACTION,bundle);
        }else
        {
            MetricsService.cameraServiceResultReceiver.send(CameraServiceResultReceiver.UPLOAD_METRICS_FILE_SERVICE,bundle);
        }

    }

}
