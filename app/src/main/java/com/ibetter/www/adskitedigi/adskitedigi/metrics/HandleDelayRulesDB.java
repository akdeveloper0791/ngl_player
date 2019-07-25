package com.ibetter.www.adskitedigi.adskitedigi.metrics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.model.AlarmConstants;

import java.util.ArrayList;
import java.util.Calendar;

public class HandleDelayRulesDB {

    public final static String HANDLE_DELAY_RULE_TABLE="handle_delay_rules";
    public final static String HANDLE_DELAY_RULE_RULE_NAME = "rule_name";
    public final static String HANDLE_DELAY_DELAY_TIME = "delay_time";

    public  final static String CREATE_HANDLE_DELAY_RULE_TABLE="CREATE TABLE " + HANDLE_DELAY_RULE_TABLE
            + " ("
            + DataBaseHelper.LOCAL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
            +HANDLE_DELAY_RULE_RULE_NAME + " TEXT,"
            +HANDLE_DELAY_DELAY_TIME+ " DATETIME );";


    public static String getCurrentActiveAlarmTime(Context context)
    {
       return context.getSharedPreferences(context.getString(R.string.settings_sp),Context.MODE_PRIVATE).getString(
                context.getString(R.string.handle_delay_rule_current_active_alarm_time_sp),null
        );

    }

    public static void setCurrentActiveAlarmTime(Context context,String alarmTime)
    {
        context.getSharedPreferences(context.getString(R.string.settings_sp),Context.MODE_PRIVATE).edit().
        putString(
                context.getString(R.string.handle_delay_rule_current_active_alarm_time_sp),alarmTime
        ).apply();

    }

    public static void cancelDelayAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, HandleDelayRuleReceiver.class);
        alarmIntent.setAction(HandleDelayRuleReceiver.ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmConstants.HANDLE_DELAY_RULE, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public static void startDelayAlarm(Context context,long delayId, String rule, Calendar delayCal, String delayTime)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(context, HandleDelayRuleReceiver.class);
        alarmIntent.setAction(HandleDelayRuleReceiver.ACTION);
        alarmIntent.putExtra("delay_rule_id",delayId);
        alarmIntent.putExtra("rule",rule);
        alarmIntent.putExtra("push_time",delayTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmConstants.HANDLE_DELAY_RULE, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayCal.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, delayCal.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, delayCal.getTimeInMillis(), pendingIntent);
        }

        //save current active time
        setCurrentActiveAlarmTime(context,delayTime);

    }

    public static void deleteById(Context context,long id)
    {
        String whereCondition = DataBaseHelper.LOCAL_ID+" = ?";
        DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(HandleDelayRulesDB.HANDLE_DELAY_RULE_TABLE,whereCondition,
                new String[]{String.valueOf(id)});
    }

    public static Cursor getNextDelayRule(Context context)
    {
        String condition = "SELECT * FROM "+HANDLE_DELAY_RULE_TABLE+" ORDER BY "+HANDLE_DELAY_DELAY_TIME+" ASC LIMIT 1";
        return DataBaseHelper.initializeDataBase(context).getRecord(condition);
    }

    public static void deleteInActiveRules(Context context,String currentTime)
    {
       String whereCondition = HANDLE_DELAY_DELAY_TIME+" <= ?";
       DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(HANDLE_DELAY_RULE_TABLE,whereCondition,
               new String[]{currentTime});
    }

    //handle individual rule
    public static void handleRule(Context context,String rule)
    {

        if(rule!=null) {
            Cursor ruleInfoCursor = new CampaignRulesDBModel(context).getRuleCampaignsByRuleName(rule,context);

            if(ruleInfoCursor!=null && ruleInfoCursor.moveToFirst())
            {


                //prepare the campaigns file array

                {

                    ArrayList<String> campaignsFileArray = new ArrayList<>(ruleInfoCursor.getCount());
                    do
                    {

                        campaignsFileArray.add(ruleInfoCursor.getString(ruleInfoCursor.getColumnIndex(CampaignRulesDBModel.RULE_CAMPAIGN_CAMPAIGN_NAME)));

                    }while(ruleInfoCursor.moveToNext());

                    if(campaignsFileArray.size()>=1 && DisplayLocalFolderAds.actionReceiver!=null)
                    {

                        Bundle extras = new Bundle(2);
                        extras.putSerializable("campaignFiles", campaignsFileArray);
                        extras.putString("rule",rule);
                        DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.HANDLE_CAMPAIGN_RULE_NEW_ACTION_CODE,extras);
                    }
                }
            }
        }

    }

}
