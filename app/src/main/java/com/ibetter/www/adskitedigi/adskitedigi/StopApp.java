package com.ibetter.www.adskitedigi.adskitedigi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;

public class StopApp extends Activity
{
    private Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        context=StopApp.this;

        finish();

        if(DeviceModel.isZidooDevice(context))
        {
            DeviceModel.launchZidooHdmiIn(context);

        }
        else
        if(DeviceModel.isEnvyDevice(context))
        {

            DeviceModel.launchEnvyHdmiIn(context);

        }

    }
}
