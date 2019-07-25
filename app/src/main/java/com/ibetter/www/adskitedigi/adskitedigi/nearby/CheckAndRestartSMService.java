package com.ibetter.www.adskitedigi.adskitedigi.nearby;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

public class CheckAndRestartSMService extends IntentService
{

    private Context context;

    public CheckAndRestartSMService() {
        super("CheckAndRestartSMService");
        context=CheckAndRestartSMService.this;
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(0, new Notification());
        }


        Log.i("","check and restrat service");

        if (SignageMgrAccessModel.isSignageMgrAccessOn(context,getString(R.string.sm_access_near_by_mode)))
        {
            new ConnectingNearBySMMOdel().startDiscoveringSMService(context);
        }

        if (SignageMgrAccessModel.isSignageMgrAccessOn(context,getString(R.string.sm_access_enterprise_mode)))
        {

            //and then restart
            EnterPriseSettingsModel.startEnterPriseModel(context);
        }

    }

}
