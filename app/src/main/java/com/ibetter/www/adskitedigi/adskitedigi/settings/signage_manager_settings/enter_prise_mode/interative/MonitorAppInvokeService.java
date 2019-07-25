package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorAppInvokeService extends Service
{
        private Context context;
        public static boolean isServiceActive=false;
        private NotificationManager mNotificationManager;
        private Notification notification;
        private NotificationCompat.Builder mBuilder;
        private final int notificationId=103;

       private GlobalEventReceiver globalEventReceiver;
       public static String GLOBAL_EVENT_ACTION_INTENT="com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative";
       public static final String NOTIFICATION_CHANNEL_ID ="com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative.MonitorAppInvokeService";

        private Handler mHandler = new Handler();
        private Timer endScheduleTimer;
        private long inActiveDuration;
        private static long eventTime=0;

        public void onCreate()
        {
            super.onCreate();

            context = MonitorAppInvokeService.this;
            isServiceActive=true;

         try{
                if (Build.VERSION.SDK_INT >= 26)
                {
                    createNotification();
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        public IBinder onBind(Intent intent) {
            return null;
        }

        public int onStartCommand(Intent intent,int flags,int startId)
        {

            registerUpdatesFromReceiver();

            inActiveDuration=(new ActionModel().getActionInactivityTime(context))*1000;
            Log.i("OtherAppInvokeService","inActiveDuration:"+inActiveDuration);
            endScheduleTimer = new Timer();
            endScheduleTimer.schedule(new InActiveTimerTask(), inActiveDuration);

            return START_STICKY; //restart the service whenever resources get available
        }


    private class InActiveTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run()
                {
                    //long eventTime= HandleKeyCommands.eventTime;
                    if(eventTime>0)
                    {
                        long currentTime= Calendar.getInstance().getTimeInMillis();
                        if(currentTime>eventTime)
                        {
                            long diff=currentTime-eventTime;
                           // Log.i("OtherAppInvokeService","diff:"+diff);
                            long extraDuration=inActiveDuration-diff;
                            Log.i("OtherAppInvokeService","extraDuration:"+extraDuration);
                            stopTimer();

                            endScheduleTimer=  new Timer();
                            endScheduleTimer.schedule(new InActiveTimerTask(), extraDuration);

                        }else if(currentTime==eventTime)
                        {
                            stopTimer();
                            endScheduleTimer=  new Timer();
                            endScheduleTimer.schedule(new InActiveTimerTask(), inActiveDuration);
                            Log.i("OtherAppInvokeService","restart InActiveTimerTask:"+inActiveDuration);
                        }

                    }else
                    {
                       Log.i("OtherAppInvokeService","restartApp eventTime:"+eventTime);
                        //restart signage player
                        DeviceModel.restartApp(context);
                        stopSelf();
                        stopForeground(true);
                    }
                }

            });

        }
    }

    private void stopTimer()
    {
        if(endScheduleTimer!=null)
        {
            eventTime=0;
            endScheduleTimer.cancel();
            endScheduleTimer.purge();
        }
    }

 public void createNotification()
        {
            /**Creates an explicit intent for an Activity in your app**/

            mBuilder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
            //PendingIntent mPendingIntent = PendingIntent.getActivity(context,notificationId , intent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentTitle("Digital Signage Player")
                    //.setContentText(context.getString(R.string.auto_campaign_notification_message))
                    .setAutoCancel(true)
                    .setOngoing(false) 
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    //.setContentIntent(mPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Command Handling", importance);
                notificationChannel.setDescription("Signage Player");
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                assert mNotificationManager != null;
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                mNotificationManager.createNotificationChannel(notificationChannel);

            }

            assert  mNotificationManager != null;
            mNotificationManager.notify(notificationId /* Request Code */, mBuilder.build());
            // Build the notification.
            notification = mBuilder.build();
            startForeground(notificationId, notification);
        }

    private  void registerUpdatesFromReceiver()
    {
        IntentFilter intentFilter = new IntentFilter(GLOBAL_EVENT_ACTION_INTENT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (globalEventReceiver == null)
        {
            globalEventReceiver = new GlobalEventReceiver();
            context.registerReceiver(globalEventReceiver, intentFilter);
        }
    }


    public class GlobalEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent)
        {
          //  Log.i("OtherAppInvokeService","inside UpdatesFromReceiver:");
            try {
                if (intent != null)
                {
                    eventTime=intent.getLongExtra("eventTime",0);
                    Log.i("OtherAppInvokeService","UpdatesFromReceiver:"+eventTime);
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    //un register  receivers
    private void unRegisterGlobalEventReceiver()
    {
        try
        {
           context.unregisterReceiver(globalEventReceiver);
        } catch (Exception e)
        {

        }
        finally
        {
            globalEventReceiver = null;
        }

    }

 @Override
 public void onDestroy()
        {
            //Log.i("OtherAppInvokeService","onDestroy:");
            isServiceActive=false;
            super.onDestroy();
            stopTimer();
            unRegisterGlobalEventReceiver();

        }



}
