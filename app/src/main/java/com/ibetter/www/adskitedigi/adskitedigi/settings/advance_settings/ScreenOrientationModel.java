package com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings;

import android.content.Context;
import android.content.pm.ActivityInfo;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import static android.content.Context.MODE_PRIVATE;

public class ScreenOrientationModel {

    public static final String DEFAULT_SCREEN_ORIENTATION = "Landscape";

    public static int getSelectedScreenOrientation(Context context)
    {



        String selectedMode = (context.getSharedPreferences(context.getString(R.string.settings_sp), MODE_PRIVATE)).
                getString(context.getString(R.string.screen_orientation_sp),DEFAULT_SCREEN_ORIENTATION);
        if(selectedMode.equalsIgnoreCase(context.getString(R.string.portrait)))
        {
          //return  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            return ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
        }
        else
        {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }


    }
}
