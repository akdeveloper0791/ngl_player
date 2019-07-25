package com.ibetter.www.adskitedigi.adskitedigi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.CheckAndRestartSMServiceOreo;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

import static com.ibetter.www.adskitedigi.adskitedigi.fcm.MyFirebaseMessagingService.checkAndUploadFCM;


/**
 * Created by vineeth_ibetter on 5/25/15.
 */
public class MonitorInternetChanges extends BroadcastReceiver {

    private Context context;


    public void onReceive(Context context, Intent intent) {

        Log.i("Info","internet changes");


        //Toast.makeText(context,"Inside internet changes receiver",Toast.LENGTH_SHORT).show();
        Log.i("internet changes", "Inside internet changes receiver");

        this.context = context;

        //new DisplayDebugLogs(context).execute("Inside internet changes receiver");

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if ((wifi.isWifiEnabled() || isInternet(context))) {

            //check and start auto sync service
            Log.i("internet changes", "Inside internet changes receiver active internet");

            notifyChangesInDisplay();

            //check and restart sm enterprise mode
            checkAndRestratSMService();

            //check and upload fcm
            checkAndUploadFCM(context);


        }

    }

    private boolean isInternet(Context context) {
        ConnectionDetector cd = new ConnectionDetector(context);

        return cd.isConnectingToInternet();
    }


    private void notifyChangesInDisplay() {
       /* Intent intent = new Intent(DisplayAdsScreenActivity.MONITOR_INTERNET_CHANGES_ACTION);
        intent.putExtra(context.getString(R.string.app_default_flag_text), true);

        context.sendBroadcast(intent);*/
    }


    private void checkAndRestratSMService()
    {
        ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );

       /* int mode=new User().getUserPlayingMode(context);

        switch (mode)
        {
            case Constants.NEAR_BY_MODE:
                ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );
                break;
            case Constants.CLOUD_MODE:
                ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );
                checkAndRestartPlayerStatisticsCollectionService();
                checkRestartAutoCampaignDownloadService();
                break;
            case Constants.ENTERPRISE_MODE:
                ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );
                checkAndRestartPlayerStatisticsCollectionService();
                checkRestartAutoCampaignDownloadService();
                break;
        }*/
        //context.startService(new Intent(context, CheckAndRestartSMService.class));
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