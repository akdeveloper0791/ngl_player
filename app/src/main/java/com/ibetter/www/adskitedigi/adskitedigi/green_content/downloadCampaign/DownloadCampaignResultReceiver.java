package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService;

import java.lang.ref.WeakReference;


public class DownloadCampaignResultReceiver extends ResultReceiver
{
    private CallBack callBack;
    private WeakReference serviceContext;

    public static final int INIT_DOWNLOAD_CAMPAIGN=1;
    public static final int INIT_DOWNLOAD_API_ERROR=2;
    public static final int INIT_DOWNLOAD_API_RESPONSE=3;
    public static final int DBX_RESOURCE_FILE_DOWNLOAD_SUCCESS=4;
    public static final int DBX_RESOURCE_FILE_DOWNLOAD_FAILURE=5;
    public static final int STOP_SERVICE=6;
    public static final int DBX_RESOURCE_FILE_CHUNK_DOWNLOAD_SUCCESS=7;
    public static final int REQUEST_FOR_DOWNLOADING_CAMPAIGNS=8;
    public static final int INTERRUPT_SERVICE=9;

    public DownloadCampaignResultReceiver(Handler handler, DownloadCampaignsService context, CallBack callBacks)
    {
        super(handler);
        serviceContext = new WeakReference<>(context);
        this.callBack = callBacks;
    }


    public interface CallBack {

        void initDownloadApiError(Bundle values);
        void initDownloadApiResponse(Bundle values);
        void downloadResourceFileSuccess(Bundle values);
        void downloadResourceFileFailure(Bundle values);
        void initDownloadCampaign(Bundle values);
        void resourceFileChunkDownloadSuccess();
        void requestForDownloadingCampaigns();
        void stopService(Bundle values);
        void interruptService(Bundle values);
        }

    @Override
    protected void onReceiveResult(int resultCode, Bundle values)
    {

        switch (resultCode)
        {

            case INIT_DOWNLOAD_API_ERROR:

                callBack.initDownloadApiError(values);
                break;
            case INIT_DOWNLOAD_API_RESPONSE:
                callBack.initDownloadApiResponse(values);
                break;
            case DBX_RESOURCE_FILE_DOWNLOAD_SUCCESS:
                callBack.downloadResourceFileSuccess(values);
                break;
            case DBX_RESOURCE_FILE_DOWNLOAD_FAILURE:
                callBack.downloadResourceFileFailure(values);
                break;
            case INIT_DOWNLOAD_CAMPAIGN:
                callBack.initDownloadCampaign(values);
                break;
            case DBX_RESOURCE_FILE_CHUNK_DOWNLOAD_SUCCESS:
                callBack.resourceFileChunkDownloadSuccess();
                break;
            case REQUEST_FOR_DOWNLOADING_CAMPAIGNS:
                callBack.requestForDownloadingCampaigns();
                break;
            case STOP_SERVICE:
                callBack.stopService(values);
                break;
            case INTERRUPT_SERVICE:
                callBack.interruptService(values);
                break;
        }

    }




}
