package com.ibetter.www.adskitedigi.adskitedigi.player_statistics;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignTriggerService;

import java.lang.ref.WeakReference;

public class PlayerStatisticsCollectionRx extends ResultReceiver
{
    private CallBack callBack;
    private WeakReference serviceContext;

    public static final int UPLOAD_PLAYER_REPORT_COLLECTION_RESPONSE=1;
    public static final int STOP_SERVICE=3;



    public PlayerStatisticsCollectionRx(Handler handler, PlayerStatisticsCollectionService context, PlayerStatisticsCollectionRx.CallBack callBacks)
    {
        super(handler);
        serviceContext = new WeakReference<>(context);
        this.callBack = callBacks;
    }


    public interface CallBack {

        void uploadPlayerStatisticsReportResponse(Bundle values);
        void stopService(Bundle values);

    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle values)
    {

        switch (resultCode)
        {

            case UPLOAD_PLAYER_REPORT_COLLECTION_RESPONSE:

                callBack.uploadPlayerStatisticsReportResponse(values);
                break;

            case STOP_SERVICE:
                callBack.stopService(values);
                break;





        }




    }
}
