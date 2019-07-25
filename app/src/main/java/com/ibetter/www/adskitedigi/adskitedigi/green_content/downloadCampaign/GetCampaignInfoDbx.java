package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.files.FileMetadata;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.drop_box.DropboxClientFactory;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;


public class GetCampaignInfoDbx extends Thread {
    private String campaignName,savePath;
    private Context downloadCampaignsService;
    private ResultReceiver resultReceiver;

    public GetCampaignInfoDbx(String campaignName, Context downloadCampaignsService,ResultReceiver resultReceiver,String savePath) {

        this.campaignName = campaignName;
        this.downloadCampaignsService = downloadCampaignsService;
        this.resultReceiver=resultReceiver;
        this.savePath=savePath;

    }


    public void run() {
        try {
            downloadFile();
            Log.i("GetCampaignInfoDbx", "inside thread" + campaignName);

        }catch (Exception E)
        {
            sendFailedResponse(false,"Unable to download");

        }
    }

    private void downloadFile() throws InterruptedException
    {

        Exception mException = null;

        try {

                DbxDownloader<FileMetadata> downloadedInfo = DropboxClientFactory.getClient().files().download(savePath+campaignName+".txt");
                FileMetadata info = downloadedInfo.getResult();

                if(info!=null&&info.getSize()>0)
                {
                    sendSuccessResponse();
                }
                else
                {
                    sendFailedResponse(false,"File is not exist");
                }

        }

        catch ( Exception e) {
            mException = e;
        }finally
        {
            if(mException!=null)
            {

                sendFailedResponse(false,mException.getMessage());
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
