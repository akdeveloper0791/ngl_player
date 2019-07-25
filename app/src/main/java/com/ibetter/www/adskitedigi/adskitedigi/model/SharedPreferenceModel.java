package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class SharedPreferenceModel {

    public SharedPreferences getUserDetailsSharedPreference(Context context)
    {
      return context.getSharedPreferences(context.getString(R.string.user_details_sp), MODE_PRIVATE);

    }

    public SharedPreferences getIOTDevicesSharedPreference(Context context)
    {
        return context.getSharedPreferences(context.getString(R.string.iot_device_details_sp), MODE_PRIVATE);
    }

    public SharedPreferences getDeviceSharedPreference(Context context)
    {

        return context.getSharedPreferences(context.getString(R.string.sp_device_name), MODE_PRIVATE);

    }

    public SharedPreferences getDisplayDetailsSharedPreference(Context context)
    {

        return context.getSharedPreferences(context.getString(R.string.sp_display_details), MODE_PRIVATE);

    }

    public SharedPreferences getLocalScrollTextSharedPreference(Context context)
    {

        return context.getSharedPreferences(context.getString(R.string.sp_local_scroll_text), MODE_PRIVATE);

    }


    public SharedPreferences getSignageMgrAccessSharedPreference(Context context)
    {

        return context.getSharedPreferences(context.getString(R.string.sp_signage_mgr_access), MODE_PRIVATE);

    }

    public SharedPreferences getSignageMgrSettingSharedPreference(Context context)
    {
        return context.getSharedPreferences(context.getString(R.string.settings_sp), MODE_PRIVATE);
    }
}
