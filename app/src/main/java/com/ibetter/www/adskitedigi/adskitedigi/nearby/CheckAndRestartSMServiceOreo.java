package com.ibetter.www.adskitedigi.adskitedigi.nearby;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.logs.DisplayDebugLogs;
import com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

public class CheckAndRestartSMServiceOreo extends Service {

   Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=CheckAndRestartSMServiceOreo.this;

        if (Build.VERSION.SDK_INT >= 26)
        {
            displayFrontNotification("Processing", null);
        }


    }

    //display success Notification
    private void displayFrontNotification(String contentText,String errorMsg)
    {

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setOngoing(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("SS EnterPrise Mode")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            //.setChannelId("Test");

            assignWithNotificationChannel(builder);

            if (errorMsg == null) {
                builder.setContentText(contentText);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                    builder.setContentText(contentText);

                    NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle()
                            .bigText(errorMsg);
                    builder.setStyle(style);

                } else {
                    builder.setContentText(errorMsg);
                }
            }


            Notification notification = builder.build();


            // Sets an ID for the notification, so it can be updated
            int notifyID = 2;

            startForeground(notifyID, notification);

            //new DisplayDebugLogs(context).execute("procesing");


        }catch (Exception E)
        {
           // new DisplayDebugLogs(context).execute(E.getMessage());
        }

    }

    //create notification channel
    private void assignWithNotificationChannel(NotificationCompat.Builder builder)
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.enterprise_notify_ch_id);
            CharSequence name = getString(R.string.enterprise_notify_ch_name);
            String description = getString(R.string.enterprise_notify_ch_des);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            builder.setChannelId(channelId);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        Log.i("","check and restrat service");

        if (SignageMgrAccessModel.isSignageMgrAccessOn(context,getString(R.string.sm_access_near_by_mode)))
        {
            new ConnectingNearBySMMOdel().startDiscoveringSMService(context);
        }

        if (SignageMgrAccessModel.isSignageMgrAccessOn(context,getString(R.string.sm_access_enterprise_mode)))
        {

            //and then restart
            EnterPriseSettingsModel.startEnterPriseModel(context);
        }

        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
