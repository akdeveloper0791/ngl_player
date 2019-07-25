package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FetchBasicCampInfoService extends IntentService {

    private Context context;
    private ResultReceiver receiver;
    private HttpURLConnection urlConnection;

    public static final int GC_FETCH_CAMPAIGNS_ACTION=2002;

    public FetchBasicCampInfoService()
    {
        super(FetchBasicCampInfoService.class.getName());
    }

    @Override
    public void onHandleIntent(@Nullable Intent intent)
    {

        context=FetchBasicCampInfoService.this;
        receiver = intent.getParcelableExtra("receiver");

        //get campaign basic info list from GC server
        getCampaignList();

    }

    private void getCampaignList()
    {

        String url=getCampaignsDownloadURL();

        if(url!=null)
        {
            try {

               // OkHttpClient httpClient = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("secretKey",new User().getGCUserUniqueKey(context))
                        .addFormDataPart("player",String.valueOf(new User().getPlayerId(context)))
                        .build();


                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()
                        ;


                OkHttpClient httpClient = new OkHttpClient.Builder()
                        .connectTimeout(1, TimeUnit.MINUTES)
                        .writeTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.MINUTES)
                        .build();

                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.d("TestRestApi","response in api error in Test rest api - "+e.getMessage());
                        sendFailedResponse(false,e.toString());

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                       String responseString=response.body().string().trim();
                        Log.d("TestRestApi","response in api is - "+responseString.trim());

                        if (response.isSuccessful()) {
                            processResponse(responseString);
                        }
                    }


                });

            }

            catch (Exception e)
            {
                e.printStackTrace();
                sendFailedResponse(false,e.toString());
            }

            finally {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
            }

        }else
        {
            sendFailedResponse(false,"Invalid User Id, Please login and try again.");
        }

    }

    private synchronized void processResponse(String response)
    {
        try
        {
           //Log.i("DownloadBasicCampInfo","responseObject:"+response);

           JSONObject responseObject=new JSONObject(response);
           int statusCode=responseObject.getInt("statusCode");

           if(statusCode==0)
            {
                ArrayList<GCModel> campList=new ArrayList<>();

                JSONArray campArray=responseObject.getJSONArray("campaigns");
                if(campArray!=null&& campArray.length()>0)
                {
                    for(int i=0;i<campArray.length();i++)
                    {
                        JSONObject campObject=campArray.getJSONObject(i);
                        GCModel gcModel=new GCModel();
                        gcModel.setCampaignFile(campObject.getString("text_file"));
                        //gcModel.setCreatedAt(campObject.getString("created_date"));
                        gcModel.setCreatedAt(campObject.getString("created_date"));

                        gcModel.setStoreLocation(campObject.getInt("stor_location"));
                        gcModel.setInfo(campObject.getString("info"));
                        gcModel.setCampaignName(campObject.getString("campaign_name"));
                        gcModel.setSavePath(campObject.getString("save_path"));
                        campList.add(gcModel);

                    }

                    if(campList!=null && campList.size()>0)
                    {
                        sendSuccessResponse(campList);
                    }else
                    {
                        sendFailedResponse(false,"No Campaigns Found...");
                    }

                }else
                {
                    sendFailedResponse(false,"No Campaigns Found...");
                }

            }else
            {
                sendFailedResponse(false,responseObject.getString("status"));

            }

        }catch (Exception e)
        {
            e.printStackTrace();
            sendFailedResponse(false,"Unable to get the response, Please try again.");
        }
    }


    private String getCampaignsDownloadURL()
    {
        int mode=new User().getUserPlayingMode(context);
        String userId=new User().getGCUserUniqueKey(context);


        if(mode== Constants.CLOUD_MODE)
        {
            if(userId!=null)
            {
                return GCUtils.GET_GC_ALL_DROP_BOX_CAMPAIGNS_URL;
            }else
            {
                return null;
            }
        }else
        if(mode== Constants.ENTERPRISE_MODE)
        {
            if(userId!=null)
            {
                return new User().getEnterPriseURL(context)+GCUtils.GET_ALL_DROP_BOX_CAMPAIGNS_URL_END_POINT;
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

    private void sendSuccessResponse(ArrayList<GCModel>campaignList)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",true);
        bundle.putSerializable("campaign_list",campaignList);
        receiver.send(GC_FETCH_CAMPAIGNS_ACTION,bundle);
    }

    private void sendFailedResponse(boolean flag,String status)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",flag);
        bundle.putString("status",status);
        receiver.send(GC_FETCH_CAMPAIGNS_ACTION,bundle);
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
