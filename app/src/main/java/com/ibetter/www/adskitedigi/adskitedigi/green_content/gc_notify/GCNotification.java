package com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.os.Build;

import android.support.v4.app.NotificationCompat;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants;

import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID;
import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.createChannel;

public class GCNotification {

    private Context context;

    public final static String DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_ID = "DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL";
    public final static String DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_NAME = "DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_NAME";
    public final static String DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_DES = "Update campaign resource file DOWNload progress";

    private final static String DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_ID = "DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_ID";
    private final static String DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_NAME = "DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_NAME";
    private final static String DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_DES = "Update campaign download status";

    private final static String DOWNLOAD_CAMPAIGN_DOWNLOAD_FAIL_CHANNEL_ID = "DOWNLOAD_CAMPAIGN_DOWNLOAD_FAIL_CHANNEL_ID";
    private final static String DOWNLOAD_CAMPAIGN_DOWNLOAD_FAIL_CHANNEL_NAME = "DOWNLOAD_CAMPAIGN_DOWNLOAD_FAIL_CHANNEL_NAME";

    private final static String DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID = "DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID";
    private final static String DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_NAME = "DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_NAME";

    private final static String AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID = "AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID";
    private final static String AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_NAME = "AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_NAME";
    private final static String AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_DES = "Update campaign auto download status";


    private final static String PLAYER_STATISTICS_SERVICE_CHANNEL_ID = "PLAYER_STATISTICS_SERVICE_CHANNEL_ID";
    private final static String APLAYER_STATISTICS_SERVICE_CHANNEL_NAME = "APLAYER_STATISTICS_SERVICE_CHANNEL_NAME";
    private final static String PLAYER_STATISTICS_SERVICE_CHANNEL_DES = "Player Statistics Collection";

    private final static String HANDLE_RULE_SERVICE_CHANNEL_ID = "HANDLE_RULE_SERVICE_CHANNEL_ID";
    private final static String HANDLE_RULE_SERVICE_CHANNEL_NAME = "HANDLE_RULE_SERVICE_CHANNEL_NAME";
    private final static String HANDLE_RULE_DES = "Handle rule in background";


    public static  final String ACTION="com.ibetter.www.adskitedigi.adskitedigi.green_content.ACTION_RETRY";
    public static  final int ACTION_SKIP=1;
    public static  final int ACTION_RETRY=2;

    public GCNotification(Context context) {
        this.context = context;
    }

    public GCNotification()
    {

    }

    public void displayNotification(String title,String contextText)
    {
        NotificationCompat.Builder mBuilder;
        int notificationId=NotificationModelConstants.getNotificationIdForDOWNLOADSUCCESSCAMPAIGN();

        Intent intent = new Intent(context, DownloadCampaigns.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context,notificationId , intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager  mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            mBuilder = new NotificationCompat.Builder(context,DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_ID);
            mBuilder.setContentTitle(title)
                    .setContentText(contextText)
                    .setAutoCancel(false)
                    .setContentIntent(mPendingIntent)
                    // .setSmallIcon(R.drawable.ic_app_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_ID, DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_NAME, importance);
            notificationChannel.setDescription(DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_DES);
            notificationChannel.setLightColor(Color.WHITE);


            notificationChannel.enableVibration(true);
            mBuilder.setChannelId(DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);

        }else
        {
            mBuilder = new NotificationCompat.Builder(context,DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_ID);
            mBuilder.setContentTitle(title)
                    .setContentText(contextText)
                    .setAutoCancel(false)
                    .setContentIntent(mPendingIntent)
                    .setLights(Color.WHITE, Color.RED, Color.GREEN)
                    //.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    //.setSmallIcon(R.drawable.ic_app_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
            mBuilder.setColor(context.getResources().getColor(R.color.blue_color));
        } else {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        }

        mNotificationManager.notify(notificationId /* Request Code */, mBuilder.build());
    }


    public void displayErrorNotification(String title,String contextText)
    {
        NotificationCompat.Builder mBuilder;

        Intent intent = new Intent(context, DownloadCampaigns.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context,0 , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int importance = NotificationManager.IMPORTANCE_LOW;


        NotificationManager mErrNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(DOWNLOAD_CAMPAIGN_DOWNLOAD_FAIL_CHANNEL_ID, DOWNLOAD_CAMPAIGN_DOWNLOAD_FAIL_CHANNEL_NAME, importance);
            notificationChannel.setDescription(DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_DES);
            notificationChannel.setLightColor(Color.WHITE);


            mErrNotificationManager.createNotificationChannel(notificationChannel);

        }

            mBuilder = new NotificationCompat.Builder(context,DOWNLOAD_CAMPAIGN_DOWNLOAD_FAIL_CHANNEL_ID);

            mBuilder.setContentTitle(title)
                    .setContentText(contextText)
                    .setAutoCancel(false)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setOngoing(true)
                    .setContentIntent(mPendingIntent)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {

            setSkipAction(mBuilder);
            setRetryAction(mBuilder);
            mErrNotificationManager.notify(DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID /* Request Code */, mBuilder.build());



        } else {
            //direct skip
            Intent skipIntent = new Intent();
            skipIntent.setAction(ACTION);
            skipIntent.putExtra("action", ACTION_SKIP);
            context.sendBroadcast(skipIntent);

        }


    }


    private void setSkipAction( NotificationCompat.Builder mBuilder)
    {

        Intent skipIntent = new Intent();
        skipIntent.setAction(ACTION);
        skipIntent.putExtra("action", ACTION_SKIP);

        PendingIntent skipPendingIntent =
                PendingIntent.getBroadcast(context, 1, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.retry_notification_ic, "Skip", skipPendingIntent);
        mBuilder .addAction(action);

    }


    private void setRetryAction( NotificationCompat.Builder mBuilder)
    {

        Intent retryIntent = new Intent();
        retryIntent.setAction(ACTION);
        retryIntent.putExtra("action", ACTION_RETRY);

        PendingIntent retryPendingIntent =
                PendingIntent.getBroadcast(context, 2, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action retryAction = new NotificationCompat.Action(R.drawable.retry_notification_ic, "Retry", retryPendingIntent);
        mBuilder.addAction(retryAction);
    }

    //create campaign upload success notification
    public static Notification campaignDownloadServiceNotification(Context context, String title)
    {
        //create notification channel
        createChannel(context,DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID,DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_NAME,
                DOWNLOAD_CAMPAIGN_DOWNLOAD_SUCCESS_CHANNEL_DES,NotificationManager.IMPORTANCE_LOW);


        return  new NotificationCompat.Builder(context,DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

    }

    //create campaign upload success notification
    public static Notification handleRuleNotification(Context context, String title)
    {
        //create notification channel
        createChannel(context,HANDLE_RULE_SERVICE_CHANNEL_ID,HANDLE_RULE_SERVICE_CHANNEL_NAME,
                HANDLE_RULE_DES,NotificationManager.IMPORTANCE_LOW);


        return  new NotificationCompat.Builder(context,HANDLE_RULE_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

    }

    public  NotificationCompat.Builder initCampaignResourceUploadProgress(Context context,
                                                                          String title,String contentText,String bigText)
    {
        //check and create notification channel first

        NotificationModelConstants.createChannel(context,DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_ID,
                DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_NAME,DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_DES,
                NotificationManager.IMPORTANCE_LOW);

        //build notification and return
        return new NotificationCompat.Builder(context, DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_LOW);


    }

    //create campaign upload success notification
    public static Notification campaignAutoDownloadServiceNotification(Context context, String title)
    {
        //create notification channel
        createChannel(context,AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID,AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_NAME,
                AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_DES,NotificationManager.IMPORTANCE_LOW);


        return  new NotificationCompat.Builder(context,AUTO_DOWNLOAD_CAMPAIGN_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }


    //create campaign upload success notification
    public static Notification playerStatisticsServiceNotification(Context context, String title)
    {
        //create notification channel
        createChannel(context,PLAYER_STATISTICS_SERVICE_CHANNEL_ID,APLAYER_STATISTICS_SERVICE_CHANNEL_NAME,
                PLAYER_STATISTICS_SERVICE_CHANNEL_DES,NotificationManager.IMPORTANCE_LOW);


        return  new NotificationCompat.Builder(context,PLAYER_STATISTICS_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();

    }



}
