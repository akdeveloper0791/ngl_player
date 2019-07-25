package com.ibetter.www.adskitedigi.adskitedigi.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.ProcessRule;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * Created by ibetter-Dell on 31-03-17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private Context context;
    private final static String HANDLE_METRICS_RULE = "1";
    private final static String HANDLE_MIC_RULE = "2";


    @Override
    public void onNewToken(String mToken)
    {
        super.onNewToken(mToken);
        Log.i("TOKEN",mToken);
        Log.d(TAG, "FCM token id: " + mToken);
        MyFirebaseInstanceIDService.startService(this,mToken);
        //saveToken(mToken);
    }

    public static  void saveFCMTokenId(Context context, String FCMToken)
    {

        SharedPreferences tokenSP =context.getSharedPreferences(context.getString(R.string.fcm_sp), Context.MODE_PRIVATE);
        SharedPreferences.Editor tokenEditor=tokenSP.edit();
        tokenEditor.putString(context.getString(R.string.fcm_token),FCMToken);
        tokenEditor.putBoolean(context.getString(R.string.is_fcm_updated),false);
        tokenEditor.commit();


    }

    public static void checkAndUploadFCM(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.fcm_sp),MODE_PRIVATE);
        if(sp.getBoolean(context.getString(R.string.is_fcm_updated),false)==false)
        {
           String fcm =  sp.getString(context.getString(R.string.fcm_token),null);
           if(fcm!=null)
           {
               MyFirebaseInstanceIDService.startService(context,fcm);
           }
        }
    }

    private void saveToken(String mToken)
    {
        SharedPreferences sp = getSharedPreferences(getString(R.string.fcm_sp),MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(getString(R.string.fcm_token),mToken);
        editor.commit();

        // check and upload to server
    }

    public static String getToken(Context context)
    {
       return context.getSharedPreferences(context.getString(R.string.fcm_sp),MODE_PRIVATE).getString(context.getString(R.string.fcm_token),null);
    }

    public static void setFCMUpdateStatus(boolean flag,Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.fcm_sp),MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.is_fcm_updated),flag);
        editor.commit();
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {

        Log.i(TAG, "From: " + remoteMessage.getFrom());

        context=MyFirebaseMessagingService.this;

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null)
        {


            handleNotification(remoteMessage.getNotification().getBody());
        }

        Log.i(TAG, "Notification Bod: " + remoteMessage.getData().toString());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0)
        {
            Log.i(TAG, "Notification Inside remote messafe data size greater than zero");
            android.support.v4.util.ArrayMap<String,String> dataMessage = (ArrayMap<String, String>) remoteMessage.getData();
            handleDataMessage(dataMessage);
        }

    }

    private void handleDataMessage(ArrayMap<String,String> dataMessage )
    {
        Log.i(TAG, "Inside handle data message actions is"+dataMessage.get("action"));

        if(dataMessage.get("action").equals(HANDLE_METRICS_RULE))
        {
            if(DisplayLocalFolderAds.isServiceRunning)
            {
                //handle metrics rule
                ProcessRule.startService(context,dataMessage.get("rule"),dataMessage.get("push_time"),
                        Constants.convertToInt(dataMessage.get("delay_time")));
            }
        }else if(dataMessage.get("action").equals(HANDLE_MIC_RULE))
        {
            Log.i(TAG, "Inside handle data message Inside handle mic rule case");

            if(DisplayLocalFolderAds.isServiceRunning)
            {
                String rule = processMicRule(dataMessage.get("rule"));
                Log.i(TAG,"Inside mic rule is"+rule);
                //handle metrics rule
                ProcessRule.startService(context,rule,dataMessage.get("push_time"),
                        Constants.convertToInt(dataMessage.get("delay_time")));
            }else {
                Log.i(TAG,"Inside mic rule DisplayLocalFolderAds is not running");
            }

        }else {
            Log.i(TAG, "Inside handle data message else case");
        }

    }

    private String processMicRule(String ruleJSON)
    {
        try
        {
            JSONArray rules = new JSONArray(ruleJSON);
            StringBuilder sb = new StringBuilder();
            String prefix = "";
            for(int i=0;i<rules.length();i++)
            {
                sb.append(prefix+rules.getString(i));
                prefix=getString(R.string.rule_seperator);
            }

            if(sb.length()>=1)
            {
                return sb.toString();
            }else
            {
                return null;
            }
        }catch(JSONException e)
        {
            e.printStackTrace();
            return null;
        }

    }

    private void handleNotification(String message)
    {

       /* if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }*/

       Log.i("FireBaseMessage","Inside My firebase messaging service sms - "+message);

    }



}