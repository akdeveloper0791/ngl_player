package com.ibetter.www.adskitedigi.adskitedigi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import com.ibetter.www.adskitedigi.adskitedigi.AppEntryClass;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.HandleDelayRuleBootService;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.MetricsModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.CheckAndRestartSMServiceOreo;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings.SetBootTimeForMediaService;
import com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings.SetBootTimeForMediaSettingsConstants;

/**
 * Created by vineeth_ibetter on 6/27/16.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;

        try {

            //new DisplayDebugLogs(context).execute("inside booting\n");

            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

                //start rule delay service
                ContextCompat.startForegroundService(context, new Intent(context, HandleDelayRuleBootService.class));


               checkAndRestratSMService();

               checkTimeSyncSettings();

                if(new SetBootTimeForMediaSettingsConstants().getAutoRestartOnRebootSettings(context)) {
                    Intent startApp = new Intent(context, AppEntryClass.class);
                    startApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startApp);
                }


            }
        } catch (Exception e) {
           e.printStackTrace();
         //   new DisplayDebugLogs(context).execute("booting\n"+e.getMessage());
        }

    }

    private void checkAndRestratSMService()
    {
        int mode=new User().getUserPlayingMode(context);

       switch (mode)
       {
           case Constants.NEAR_BY_MODE:
               ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );
               break;
           case Constants.CLOUD_MODE:
               ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );
               checkAndRestartImageCaptureService();
               checkAndRestartPlayerStatisticsCollectionService();
               checkRestartAutoCampaignDownloadService();
               break;
           case Constants.ENTERPRISE_MODE:
               ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );

               checkAndRestartImageCaptureService();
               checkAndRestartPlayerStatisticsCollectionService();
               checkRestartAutoCampaignDownloadService();
           break;
       }
       //context.startService(new Intent(context, CheckAndRestartSMService.class));
    }

    private void checkTimeSyncSettings()
    {

        if(new SetBootTimeForMediaSettingsConstants().getTimeSyncSettings(context))
        {
            context.startService(new Intent(context, SetBootTimeForMediaService.class));

        }
    }

    private void checkAndRestartImageCaptureService()
    {
        MetricsModel.startMetricsService(context);
    }


    private void checkAndRestartPlayerStatisticsCollectionService()
    {
        PlayerStatisticsCollectionModel.checkRestartUploadCampaignReportsService(context);
    }

    private void checkRestartAutoCampaignDownloadService()
    {
        AutoDownloadCampaignModel.checkRestartAutoCampaignDownloadService(context);
    }

}