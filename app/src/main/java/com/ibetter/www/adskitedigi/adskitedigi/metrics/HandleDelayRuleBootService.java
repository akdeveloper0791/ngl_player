package com.ibetter.www.adskitedigi.adskitedigi.metrics;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;

import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.GCNotification;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.HANDLE_DELAY_RULE;

public class HandleDelayRuleBootService extends Service {

    private Context context;

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        context= HandleDelayRuleBootService.this;
        checkAndStartForegroundNotification();

    }

    private void checkAndStartForegroundNotification()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            startForeground(HANDLE_DELAY_RULE, GCNotification.handleRuleNotification(context,
                    "Handling rule"));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try
        {
           //delete in active rules
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.LOCAL_SAVE_DATE_TIME_FORMAT);
            HandleDelayRulesDB.deleteInActiveRules(context,sdf.format(Calendar.getInstance().getTime()));

            //check and set next alarm
            checkAndSetNextAlarm();

        }catch (Exception e)
        {
            e.printStackTrace();

            //no more records ,, set current active to null
            HandleDelayRulesDB.setCurrentActiveAlarmTime(context,null);

            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void checkAndSetNextAlarm()
    {

        Cursor nextDelayRule = HandleDelayRulesDB.getNextDelayRule(context);
        if(nextDelayRule!=null && nextDelayRule.moveToFirst())
        {
            try
            {


                String delayTime =nextDelayRule.getString(nextDelayRule.getColumnIndex(HandleDelayRulesDB.HANDLE_DELAY_DELAY_TIME));
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.LOCAL_SAVE_DATE_TIME_FORMAT);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(sdf.parse(delayTime));

                HandleDelayRulesDB.startDelayAlarm(context,nextDelayRule.getLong(nextDelayRule.getColumnIndex(DataBaseHelper.LOCAL_ID)),
                        nextDelayRule.getString(nextDelayRule.getColumnIndex(HandleDelayRulesDB.HANDLE_DELAY_RULE_RULE_NAME)),calendar
                        ,delayTime);


            }catch(Exception e)
            {
                //no more records ,, set current active to null
                HandleDelayRulesDB.setCurrentActiveAlarmTime(context,null);
            }
        }else
        {
            //no more records ,, set current active to null
            HandleDelayRulesDB.setCurrentActiveAlarmTime(context,null);
        }

        //stop service
        stopSelf();
    }
}
