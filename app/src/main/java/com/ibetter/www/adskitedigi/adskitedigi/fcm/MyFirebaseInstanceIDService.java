package com.ibetter.www.adskitedigi.adskitedigi.fcm;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.model.NetworkModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;


/**
 * Created by ibetter-Dell on 31-03-17.
 */

public class MyFirebaseInstanceIDService extends Service
{
    private Context context;
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();
    private RegFCMIdReceiver regFCMIdReceiver;

    public void onCreate()
    {
        super.onCreate();
        context = MyFirebaseInstanceIDService.this;
        NotificationModelConstants.displayFrontNotification(this,"Synchronizing settings...",
                NotificationModelConstants.SYNCHRONIZING_FCM_ID,"Uploading FCM id");
    }
    public static void startService(Context context,String fcmId)
    {
        Intent intent = new Intent(context,MyFirebaseInstanceIDService.class);
        intent.putExtra("fcm_reg_id",fcmId);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
           context.startForegroundService(intent);
        }
        else
        {
            context.startService(intent);
        }
    }

    public IBinder onBind(Intent binder)
    {
        return null;
    }

    public int onStartCommand(Intent intent,int flags,int startId)
    {
        String fcmRegId = intent.getStringExtra("fcm_reg_id");
        saveRegIdInPref(fcmRegId);

        return START_REDELIVER_INTENT;
    }

    //save notification FCM token id in Shared Preference
    private void saveRegIdInPref(String FCMToken)
    {

        if(FCMToken!=null)
        {
            MyFirebaseMessagingService.saveFCMTokenId(context,FCMToken);
            //sending reg id to your server
            uploadFCMTokenId(FCMToken);

            Log.e(TAG, "saved token id: " + FCMToken);
        }
        else
        {
            stopSelf();
        }

     }





    private void uploadFCMTokenId(String FCMTokenId)
     {
         //check internet connection
         if (User.isPlayerRegistered(context))
         {
           if(new NetworkModel().isInternet(context))
           {
                //check FCM Token value and upload to server
                 if(FCMTokenId!=null)
                 {
                     //get the latest FCM registration token and sync with server and save it in SP
                     sendFCMRegistrationIdToServer (FCMTokenId);
                 }else
                 {
                     stopSelf();
                 }
            }else
                {
                    //display notification to user to update latest FCM to server
                    //updateFCMIdNotification();
                    stopSelf();
                }

         }else
             {
               //display notification to user to update latest FCM to server
               //updateFCMIdNotification();
                stopSelf();
             }

     }


    //sync FCM registration id to server
    private void sendFCMRegistrationIdToServer(String FCMTokenId)
    {

        String intentAction ="com.ibetter.www.adskitelite.fcm.RegFCMIdReceiver";

        if(regFCMIdReceiver == null)
        {
            regFCMIdReceiver = new RegFCMIdReceiver();
            IntentFilter intentFilter = new IntentFilter(intentAction);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(regFCMIdReceiver,intentFilter);
        }

        //start service to restore notes
        UploadFCMIdToServer.startService(context,intentAction,FCMTokenId);

    }

    //receiver to handle FCM Id Upload receiver
    private class RegFCMIdReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {

            //unregister receiver
            unRegisterUploadFCMIdReceiver();

            if(intent.getBooleanExtra("flag",false))
            {
                //refresh and update FCM registration ID in SP
                new MyFirebaseMessagingService().setFCMUpdateStatus(true,context);

            }
            else
            {
                 new MyFirebaseMessagingService().setFCMUpdateStatus(false,context);
            }

               stopSelf();

        }

    }

    //unregister restore receiver
    private void unRegisterUploadFCMIdReceiver()
    {
        try
        {
            unregisterReceiver(regFCMIdReceiver);

        }catch (Exception e)
        {

        }finally
        {
            regFCMIdReceiver = null;
        }
    }


    public void onDestroy()
    {
        super.onDestroy();
        unRegisterUploadFCMIdReceiver();
        stopForeground(true);
        Log.d("FCM","Inside MyFirebaseInstanceIDService on Destroy method");
    }


}
