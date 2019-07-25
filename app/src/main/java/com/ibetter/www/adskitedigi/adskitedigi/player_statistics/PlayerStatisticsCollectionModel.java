package com.ibetter.www.adskitedigi.adskitedigi.player_statistics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;
import static com.ibetter.www.adskitedigi.adskitedigi.model.AlarmConstants.PLAYER_STATISTICS_ALARM;

public class PlayerStatisticsCollectionModel
{
    public static void checkRestartUploadCampaignReportsService(Context context)
    {

        if(new User().isPlayerStatisticsCollectionOn(context)&& User.isPlayerRegistered(context)) {
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            Intent intent = new Intent(context, PlayerStatisticsCollectionService.class);

            PendingIntent reminderPI = PendingIntent.getService(context, PLAYER_STATISTICS_ALARM, intent, 0);

            am.set(AlarmManager.RTC_WAKEUP, ((Calendar.getInstance().getTimeInMillis()) + TimeUnit.MINUTES.toMillis(new User().getPlayerStatisticsCollectionDuration(context))), reminderPI);

            Log.i("alarm is on","player statistics\t"+new User().getPlayerStatisticsCollectionDuration(context));
        }
        else
        {
            Log.i("alarm is on","player statistics off");
        }

    }

    public static void stopUploadCampaignReportsService(Context context)
    {

        AlarmManager am=(AlarmManager)context.getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(context, PlayerStatisticsCollectionService.class);

        PendingIntent reminderPI = PendingIntent.getService(context, PLAYER_STATISTICS_ALARM,intent,0);

        reminderPI.cancel();
        am.cancel(reminderPI);

        if(PlayerStatisticsCollectionService.isServiceOn)
        {

            PlayerStatisticsCollectionService.playerStatisticsCollectionRx.send(PlayerStatisticsCollectionRx.STOP_SERVICE, null);

        }
    }

}
