package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import static android.content.Context.MODE_PRIVATE;
import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.IS_ENABLE_HOT_SPOT_ALWAYS_SETTINGS_DEFAULT;

public class Utility {

    public static boolean canEnableHotSpot(Context context){
        SharedPreferences settingsSP = context.getSharedPreferences(context.getString(R.string.settings_sp), MODE_PRIVATE);
        return settingsSP.getBoolean(context.getString(R.string.is_hot_spot_enable_always), IS_ENABLE_HOT_SPOT_ALWAYS_SETTINGS_DEFAULT);
    }
}
