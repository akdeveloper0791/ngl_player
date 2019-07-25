package com.ibetter.www.adskitedigi.adskitedigi.metrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

public class HandleDelayRuleReceiver extends BroadcastReceiver {

    public final static String ACTION = "com.ibetter.www.adskitedigi.adskitedigi.metrics.HandleDelayRuleReceiver";
    private Context context;
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;

        Intent serviceIntent = new Intent(context, HandleDelayRuleService.class);
        serviceIntent.setAction(HandleDelayRuleReceiver.ACTION);
        serviceIntent.putExtra("delay_rule_id",intent.getLongExtra("delay_rule_id",0));
        serviceIntent.putExtra("rule",intent.getStringExtra("rule"));
        serviceIntent.putExtra("push_time",intent.getStringExtra("push_time"));
        ContextCompat.startForegroundService(context, serviceIntent);

    }


}
