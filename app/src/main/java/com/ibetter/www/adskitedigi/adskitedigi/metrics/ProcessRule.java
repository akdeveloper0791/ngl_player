package com.ibetter.www.adskitedigi.adskitedigi.metrics;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.FCM_MAX_DELAY;
import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.LOCAL_SAVE_DATE_TIME_FORMAT;

public class ProcessRule extends IntentService {
   private Context context;

    public ProcessRule()
    {
        super("ProcessRule");
        context=ProcessRule.this;
    }

    public static void startService(Context context,String rule,String pushTime,int delayTime)
    {
        Intent intent = new Intent(context,ProcessRule.class);
        intent.putExtra("rule",rule);
        intent.putExtra("push_time",pushTime);
        intent.putExtra("delay_time",delayTime);
        context.startService(intent);
    }

    public void onHandleIntent(Intent intent)
    {
        String rule=intent.getStringExtra("rule");
        String pushTime = intent.getStringExtra("push_time");
        int delayTime = intent.getIntExtra("delay_time",0);
        //get push time
        try
        {

            SimpleDateFormat sdf = new SimpleDateFormat(Constants.SERVER_DATE_TIME_FORMAT);
            Calendar pushTimeCal = Calendar.getInstance();
            Calendar currentTime = Calendar.getInstance();

            pushTimeCal.setTime(sdf.parse(pushTime));
            Calendar maxDelayCal = (Calendar) pushTimeCal.clone();
            maxDelayCal.add(Calendar.MINUTE,FCM_MAX_DELAY);
            //add delay time
            pushTimeCal.add(Calendar.SECOND,delayTime);

            if(maxDelayCal.getTime().compareTo(currentTime.getTime())>=0 ||
               pushTimeCal.getTime().compareTo(currentTime.getTime())>=0)
            {

                processRule(rule,currentTime,pushTimeCal);
            }else
            {
                Log.d("UploadMetrics","Inside handle rule, push time is"+sdf.format(pushTimeCal.getTime())+" TIme out");

            }

        }catch(ParseException e)
        {
            e.printStackTrace();
            processRule(rule,null,null);
        }
    }

    private void processRule(String rule,Calendar currentTime,Calendar delayTimeCal)
    {
        if(currentTime!=null && delayTimeCal!=null)
        {
           //check if delayTime is less than current time then fire the rule
            if(delayTimeCal.compareTo(currentTime)<=0)
            {
                HandleDelayRulesDB.handleRule(context,rule);
            }else
            {
                //handle delay rule
                handleDelayRule(rule,delayTimeCal);
            }
        }else
        {
            HandleDelayRulesDB.handleRule(context,rule);
        }
    }



    private void handleDelayRule(String rule,Calendar delayTimeCal)
    {
        try
        {
            //get delay rime
            SimpleDateFormat sdf = new SimpleDateFormat(LOCAL_SAVE_DATE_TIME_FORMAT);
            String delayTime = sdf.format(delayTimeCal.getTime());
            //save record
            ContentValues cv= new ContentValues(2);
            cv.put(HandleDelayRulesDB.HANDLE_DELAY_RULE_RULE_NAME,rule);
            cv.put(HandleDelayRulesDB.HANDLE_DELAY_DELAY_TIME,delayTime);
            long inserted = DataBaseHelper.initializeDataBase(context).saveRecordToDBTable(cv,HandleDelayRulesDB.HANDLE_DELAY_RULE_TABLE);

            String currentActiveAlarmTime = HandleDelayRulesDB.getCurrentActiveAlarmTime(context);
            if(currentActiveAlarmTime==null)
            {

                //no alarms set,, start alarm
                HandleDelayRulesDB.startDelayAlarm(context,inserted,rule,delayTimeCal,delayTime);


            }else
            {
                //check whether active alarm time is less than triggered time else reset alarm with less time
                if(sdf.parse(currentActiveAlarmTime).compareTo(delayTimeCal.getTime())>0)
                {

                  //current active time is greater than triggered time so update alarm with triggered time
                   HandleDelayRulesDB.cancelDelayAlarm(context);

                    //set new alarm
                    HandleDelayRulesDB.startDelayAlarm(context,inserted,rule,delayTimeCal,delayTime);
                }



            }


        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }


}
