package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import java.util.Random;

public class NotificationModelConstants {

    public final static int ENTER_PRISE_SETTINGS_SERVICE_NOTIFICATION = 1;
    public static final int DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID = 8021912;
    public static final int DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_NOTIFY_ID = 8021911;
    public static final int DOWNLOAD_CAMPAIGN_FRONT_SERVICE_NOTIFY_ID = 8021914;
    public static final int AUTO_DOWNLOAD_CAMPAIGN_SERVICE_NOTIFY_ID = 8021915;
    public static final int PLAYER_STATISTICS_COLLECTION_SERVICE_NOTIFY_ID = 8021916;

    /* 101 to 200 don't use for any other notifications */
    public static final int SYNCHRONIZING_FCM_ID = 8021917;
    public static final int CAPTURE_IMAGE_NOTIFY_ID = 8021918;
    public static final int HANDLE_DELAY_RULE = 8021919;

    public static int getNotificationIdForDOWNLOADSUCCESSCAMPAIGN()
    {
        final int min = 101;
        final int max = 200;
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static void  createChannel(Context context,String chID,String chName,String chDescription,int priority)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(chID,chName,priority);
            channel.setDescription(chDescription);
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void displayFrontNotification(Service service, String notifyMsg, int notificationID,String nChannelDesc)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Notification notification =getNotificationBuilder(service,notifyMsg,null,
                    NotificationManager.IMPORTANCE_LOW,nChannelDesc).build();
            service.startForeground(notificationID, notification);
        }
    }

    //display success Notification
    //display success Notification
    private static NotificationCompat.Builder getNotificationBuilder(Context context,String contentText,String msg,int importance,String description)
    {

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder
                    .setPriority(android.app.Notification.PRIORITY_HIGH)
                    .setOngoing(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("BizMaxer")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
            //.setChannelId("Test");

            assignWithNotificationChannel(context, builder, importance,context.getString(R.string.enterprise_notify_ch_id),
                    context.getString(R.string.enterprise_notify_ch_name),description);

            if (msg == null) {
                builder.setContentText(contentText);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                    builder.setContentText(contentText);

                    NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle()
                            .bigText(msg);
                    builder.setStyle(style);

                } else {
                    builder.setContentText(msg);
                }
            }
            return builder;

            //new DisplayDebugLogs(context).execute("procesing");
        }catch (Exception E)
        {
            // new DisplayDebugLogs(context).execute(E.getMessage());
        }

        return null;
    }

    private static void assignWithNotificationChannel(Context context,NotificationCompat.Builder builder,int importance,String notifyChId,
                                                     String notifyChName,String notifyChDescription)
    {
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(notifyChId,notifyChName, importance);
            channel.setDescription(notifyChDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            builder.setChannelId(notifyChId);
        }
    }
}
