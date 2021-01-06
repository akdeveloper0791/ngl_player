package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.net.HttpURLConnection;
import java.net.URL;

public class GetCampaignInfoFromLocalServer extends Thread {

    private String campaignName,savePath;
    private Context downloadCampaignsService;
    private ResultReceiver resultReceiver;

    private Context context;
    private    HttpURLConnection urlCon;
    private static final int TIME_OUT=120000;//2min

    public GetCampaignInfoFromLocalServer(Context context, String campaignName, Context downloadCampaignsService, ResultReceiver resultReceiver, String savePath)
    {

        this.campaignName = campaignName;
        this.downloadCampaignsService = downloadCampaignsService;
        this.resultReceiver=resultReceiver;
        this.savePath=savePath;
        this.context=context;

    }

    public void run()
    {
        try {
            downloadFile();
            Log.i(" GetCampaioFromServe", "inside thread" + campaignName);

        }catch (Exception E)
        {
            sendFailedResponse(false,"Unable to download");
        }
    }

    private void downloadFile() throws InterruptedException
    {
        Exception mException = null; boolean isError = false;

        try {


            URL url = new URL(new User().getEnterPriseURL(context)+"media"+Constants.replaceSpecialCharacters(savePath+campaignName+".txt"));

            urlCon = (HttpURLConnection) url.openConnection();

            //urlCon.setReadTimeout(TIME_OUT);
            urlCon.setConnectTimeout(TIME_OUT);

            urlCon.setDefaultUseCaches( false );

            Log.i("URL Respnse Code: " ,""+ urlCon.getResponseCode());
          if(urlCon.getResponseCode()==200)
          {
              long fileSize ;

              if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                  fileSize = urlCon.getContentLengthLong();
              }else
              {
                  fileSize = urlCon.getContentLength();
              }

              Log.i("URL Content-Length: " ,""+ fileSize);

              if(fileSize<=0)
              {
                  isError=true;
                  sendFailedResponse(false,"File info not exist");
              }
          }else {
              isError=true;
              sendFailedResponse(false,"Campaign not exist");
              Log.d("DownloadCampaign","Inside get campaign info from local");
          }



        }catch (Exception E)
        {
            mException=E;
            E.printStackTrace();
            Log.i("URL error ",E.getMessage());

        }
       finally
        {
            try
            {
                if(urlCon!=null) {
                    urlCon.disconnect();
                }
            }
            catch (Exception E)
            {
                mException=E;
                E.printStackTrace();
            }

            if(mException!=null)
            {
                sendFailedResponse(false,mException.getMessage());
            }else
            {
                if(!isError)
                {
                    sendSuccessResponse();
                }

            }

        }
    }

    private void sendSuccessResponse()
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",true);
        bundle.putString("campaign_name",campaignName);
        bundle.putSerializable("status","file is exist");
        resultReceiver.send(DownloadCampaignResultReceiver.INIT_DOWNLOAD_API_RESPONSE,bundle);
    }

    private void sendFailedResponse(boolean flag,String status)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",flag);
        bundle.putString("campaign_name",campaignName);
        bundle.putString("status",status);
        resultReceiver.send(DownloadCampaignResultReceiver.INIT_DOWNLOAD_API_ERROR,bundle);
    }
}
