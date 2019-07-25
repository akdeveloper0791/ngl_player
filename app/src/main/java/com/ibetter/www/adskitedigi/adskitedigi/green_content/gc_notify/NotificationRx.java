package com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService;

import java.lang.ref.WeakReference;

public class NotificationRx extends BroadcastReceiver {
    WeakReference<DownloadCampaignsService> weakReference;
    public NotificationRx(WeakReference<DownloadCampaignsService> weakReference) {
       this.weakReference=weakReference;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int action=intent.getIntExtra("action",0);


        switch (action)
        {
            case GCNotification.ACTION_RETRY:

                weakReference.get().retryCampaign();

                break;
            case GCNotification.ACTION_SKIP:

                weakReference.get().skipCampaign();

                break;
        }



    }
}
