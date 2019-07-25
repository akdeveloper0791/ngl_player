package com.ibetter.www.adskitedigi.adskitedigi.fcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.ProcessRule;

import org.json.JSONArray;
import org.json.JSONException;

public class SoftIOTFCMService extends IntentService {

    Context context;

    public SoftIOTFCMService()
    {
        super("SoftIOTFCMService");
        context = SoftIOTFCMService.this;
    }
    public void onHandleIntent(Intent intent)
    {

        String rule = processMicRule(intent.getStringExtra("rule"));

        Log.i("SoftIOTFCMService","Inside mic rule is"+rule);
        //handle metrics rule
        ProcessRule.startService(context,rule,intent.getStringExtra("push_time"),
                intent.getIntExtra("delay_time",0));
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

}
