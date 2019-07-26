package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoCampaignDownloadAlarmRX  extends BroadcastReceiver
{
    public static String ACTION= "com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoCampaignDownloadAlarmRX";


    @Override
    public void onReceive(Context context, Intent intent) {

        context.startService(new Intent(context,AutoDownloadCampaignTriggerService.class));
    }

}
