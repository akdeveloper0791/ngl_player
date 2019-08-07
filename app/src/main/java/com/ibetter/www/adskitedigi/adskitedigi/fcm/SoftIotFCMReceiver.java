package com.ibetter.www.adskitedigi.adskitedigi.fcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.ProcessRule;

public class SoftIotFCMReceiver extends BroadcastReceiver {

    public final static String ACTION="com.ibetter.www.adskitedigi.adskitedigi.fcm.SoftIotFCMReceiver";
    private final static int CAMERA_METRICS = 1;
    private final static int MICRO_PHONE_METRICS = 2;

    public void onReceive(Context context, Intent intent)
    {

        switch(intent.getIntExtra("metrics_service",-1))
        {
            case CAMERA_METRICS:
                 processCameraMetrics(context,intent);
                break;
            case MICRO_PHONE_METRICS:
                if(DisplayLocalFolderAds.isServiceRunning)
                {
                    processMicroPhoneMetrics(context,intent);
                }

                break;
        }

    }

    private void processCameraMetrics(Context context,Intent intent)
    {

        ProcessRule.startService(context,intent.getStringExtra("rule"),intent.getStringExtra("push_time"),
                intent.getIntExtra("delay_time",0));
    }

    private void processMicroPhoneMetrics(Context context,Intent intent)
    {
        try
        {
            Intent serviceIntent = new Intent(context,SoftIOTFCMService.class);
            serviceIntent.putExtras(intent);
            context.startService(serviceIntent);
        }catch(Exception e)
        {
            e.printStackTrace();
           // Toast.makeText(context,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }
}
