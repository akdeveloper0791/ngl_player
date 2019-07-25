package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TriggerAlarmService extends Service
{
    private Context context;
    public static boolean isServiceActive=false;

    @Override
    public void onCreate()
    {
         context = TriggerAlarmService.this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
             isServiceActive=true;
             AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);

             Intent alarmIntent = new Intent(context, AlarmReceiver.class);
             long duration=intent.getLongExtra("duration",0);
             PendingIntent launchPI = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

            /* Schedule Alarm with and authorize to WakeUp the device during sleep */
            am.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis()+(duration),launchPI);
            Log.i("AlarmService","AlarmManager has been triggered:");


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        isServiceActive=false;

    }

}
