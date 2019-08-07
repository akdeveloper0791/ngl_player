package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

public class AutoCampaignDownloadAlarmRX  extends BroadcastReceiver
{
    public static String ACTION= "com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoCampaignDownloadAlarmRX";


    @Override
    public void onReceive(Context context, Intent intent) {

        ContextCompat.startForegroundService(context,new Intent(context,AutoDownloadCampaignTriggerService.class));
    }

}
