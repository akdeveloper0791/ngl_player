package com.ibetter.www.adskitedigi.adskitedigi.settings.announcement_settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.ibetter.www.adskitedigi.adskitedigi.R;

/**
 * Created by ibetter-Dell on 20-02-18.
 */

public class AnnouncementSettingsConstants  {

    public static final int Announcement_Text_Length = 10;
    public static final int Announcement_Text_Min_Times = 3;
    public static final boolean DEFAULT_announcement_settings_announcement_status = false;
    public static final long Announcement_Text_Duration= 3000;//3 seconds minimum

    public static boolean getAnnouncementSettings(Context context)
    {
        SharedPreferences settingsModel = context.getSharedPreferences(context.getString(R.string.announcement_settings_sp),context.MODE_PRIVATE);
        return settingsModel.getBoolean(context.getString(R.string.announcement_settings_announcement_status),AnnouncementSettingsConstants.DEFAULT_announcement_settings_announcement_status);
    }


}
