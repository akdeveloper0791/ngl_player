package com.ibetter.www.adskitedigi.adskitedigi.player_statistics;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.CountingRequestBody;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionRx.UPLOAD_PLAYER_REPORT_COLLECTION_RESPONSE;

public class UploadPlayerStatisticsCollectionService extends IntentService {

    private Context context;

    private String collectionInfo;
    private StopServiceReceiver stopServiceReceiver;
    public final static String STOP_SERVICE_ACTION="com.ibetter.www.adskitedigi.adskitedigi.player_statistics.UploadPlayerStatisticsCollectionService.STOP_SERVICE";


    public UploadPlayerStatisticsCollectionService()
    {
        super("UploadPlayerStatisticsCollectionService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        context= UploadPlayerStatisticsCollectionService.this;

        collectionInfo=intent.getStringExtra("collectionInfo");

        registerStopServiceReceiver();

        Log.i("UploadStatiService", "UploadPlayerStatisticsCollectionService filepath:" +collectionInfo);

        checkAndUpload();
    }


    private void checkAndUpload()
    {
       if(collectionInfo!=null) {

           uploadFile(collectionInfo);
       }
    }


    public void uploadFile(String collectionInfo)
    {
        try {

            String url=getURL();
            if(url!=null) {
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        //.addFormDataPart("text_file", file.getName(), com.squareup.okhttp.RequestBody.create(com.squareup.okhttp.MediaType.parse("file/*"), file)
                        .addFormDataPart("data", collectionInfo)
                        .addFormDataPart("player", String.valueOf(User.getPlayerId(context)))
                        .addFormDataPart("p_mac", User.getPlayerMac(context))

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

                        sendResponse(false);
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                        String result = response.body().string();

                        Log.i("reports", " inside update collectiom reposense " + result);

                        if (!response.isSuccessful()) {
                            // Handle the error

                            Log.i("reports", "reposense failure");

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
            handleRule(jsonObject.getString("rule"));
        }
    }

    //handle individual rule
    private void handleRule(String rule)
    {

        if(rule!=null) {
            Cursor ruleInfoCursor = new CampaignRulesDBModel(context).getRuleInfo(rule);
            if(ruleInfoCursor!=null && ruleInfoCursor.moveToFirst())
            {
                //rule has info to process,, get info (campaigns to play)
                String assignedCampaigns =  ruleInfoCursor.getString(ruleInfoCursor.getColumnIndex(CampaignRulesDBModel.RULE_ASSIGNED_CAMP_LIST));
                DataBaseHelper.closeCursor(ruleInfoCursor);

                //prepare the campaigns file array
                if(assignedCampaigns!=null)
                {
                    //split campaigns with comma seperator
                    String[] campaignsArray = assignedCampaigns.split(",");
                    ArrayList<File> campaignsFileArray = new ArrayList<>(campaignsArray.length);
                    for (String campaign: campaignsArray)
                    {
                        File campaignFile = new File(new User().getUserPlayingFolderModePath(context)+File.separator+campaign);
                        if(campaignFile.exists())
                        {
                            campaignsFileArray.add(campaignFile);
                        }
                    }

                    if(campaignsFileArray.size()>=1)
                    {
                        Bundle extras = new Bundle(2);
                        extras.putSerializable("campaignFiles", campaignsFileArray);
                        DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.HANDLE_CAMPAIGN_RULE_ACTION_CODE,extras);
                    }
                }
            }
        }

    }


    private void sendResponse(boolean flag)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",flag);
        PlayerStatisticsCollectionService.playerStatisticsCollectionRx.send(UPLOAD_PLAYER_REPORT_COLLECTION_RESPONSE,bundle);
    }

    @Override
    public void onDestroy()
    {
        unRegisterStopServiceReceiver();
        super.onDestroy();
    }

    //register stop service receiver
    private void registerStopServiceReceiver()
    {
        IntentFilter intentFilter=new IntentFilter(STOP_SERVICE_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        stopServiceReceiver=new StopServiceReceiver();
        registerReceiver(stopServiceReceiver, intentFilter);

    }

    //stop service receiver
    private class StopServiceReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context,Intent intent)
        {

            //stop executing
            stopSelf();


        }
    }

    //un register StopServiceReceiver
    private void unRegisterStopServiceReceiver()
    {
        try
        {
            unregisterReceiver(stopServiceReceiver);
        }catch(Exception e)
        {

        }finally {
            stopServiceReceiver=null;
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
                return GCUtils.UPLOAD_PLAYER_STATISTICS_COLLECTION_URL;
            }else
            {
                return null;
            }
        }else
        if(mode== Constants.ENTERPRISE_MODE)
        {
            if(userId!=null)
            {
                return new User().getEnterPriseURL(context)+GCUtils.UPLOAD_PLAYER_STATISTICS_COLLECTION_URL_END_POINT;
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
