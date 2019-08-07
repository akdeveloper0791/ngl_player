package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;


import java.lang.ref.WeakReference;

public class AutoDownloadCampaignReceiver extends ResultReceiver {
    private CallBack callBack;
    private WeakReference serviceContext;

    public static final int DOWNLOAD_LIST_CAMPAIGN_SUCCESS=1;
    public static final int DOWNLOAD_LIST_API_ERROR=2;
    public static final int STOP_SERVICE=3;
    public static final int DOWNLOAD_CAMPAIGN_INFO_API_RESPONSE=4;
    public static final int SYNC_RULES_API_ERROR=5;
    public static final int SYNC_RULES_API_SUCCESS=6;


    public AutoDownloadCampaignReceiver(Handler handler, AutoDownloadCampaignTriggerService context, CallBack callBacks)
    {
        super(handler);
        serviceContext = new WeakReference<>(context);
        this.callBack = callBacks;
    }
    public interface CallBack {
        void initDownloadListApiError(Bundle values);
        void initDownloadListApiResponse(Bundle values);
        void stopService(Bundle values);
        void downloadCampaignsInfoResponse(Bundle values);
        void syncRulesApiError(Bundle values);
        void syncRulesApiSuccess(Bundle values);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle values)
    {

        switch (resultCode)
        {

            case DOWNLOAD_LIST_CAMPAIGN_SUCCESS:
                callBack.initDownloadListApiResponse(values);
                break;
            case DOWNLOAD_LIST_API_ERROR:
                callBack.initDownloadListApiError(values);
                break;
            case STOP_SERVICE:
                callBack.stopService(values);
                break;

            case DOWNLOAD_CAMPAIGN_INFO_API_RESPONSE:
                callBack.downloadCampaignsInfoResponse(values);
                break;

            case SYNC_RULES_API_ERROR:
                callBack.syncRulesApiError(values);
                break;

            case SYNC_RULES_API_SUCCESS:


                callBack.syncRulesApiSuccess(values);
                break;
        }
    }
}
