package com.ibetter.www.adskitedigi.adskitedigi.iot_devices;

import android.content.Context;

import com.ibetter.www.adskitedigi.adskitedigi.R;

public class IOTDevice {

    public static long getDeviceId(Context context)
    {
        return context.getSharedPreferences(context.getString(R.string.iot_device_details_sp),Context.MODE_PRIVATE).getLong(
                context.getString(R.string.iot),0);
    }

    public static String getIOTDeviceKey(Context context)
    {
        return context.getSharedPreferences(context.getString(R.string.iot_device_details_sp),Context.MODE_PRIVATE).getString(
                context.getString(R.string.iot_key),null);
    }

    //get green content CMS user login info from SP
    public static boolean isIOTDeviceRegistered(Context context)
    {
        return (context.getSharedPreferences(context.getString(R.string.iot_device_details_sp),Context.MODE_PRIVATE).getLong(context.getString(R.string.iot),0) >=1 ? true:false);

    }
}
