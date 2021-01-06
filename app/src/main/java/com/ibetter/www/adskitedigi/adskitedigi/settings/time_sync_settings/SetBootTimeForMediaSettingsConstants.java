package com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings;

import android.content.Context;

import com.ibetter.www.adskitedigi.adskitedigi.R;

public class SetBootTimeForMediaSettingsConstants {
    public  static final boolean DEFAULT_TIME_SYNC_STATUS = false;
    public  static final boolean DEFAULT_AUTO_RESTART_ON_REBOOT = false;
    public  static final boolean DEFAULT_PLAY_CAMPAIGN_ON_REBOOT_ONCE = false;

    //get play offer audio settings
    public boolean getTimeSyncSettings(Context context)
    {
        return context.getSharedPreferences(context.getString(R.string.settings_sp),context.MODE_PRIVATE).
                getBoolean(context.getString(R.string.sync_time_sp),DEFAULT_TIME_SYNC_STATUS);
    }

    //get play offer audio settings
    public boolean getAutoRestartOnRebootSettings(Context context)
    {
        return context.getSharedPreferences(context.getString(R.string.settings_sp),context.MODE_PRIVATE).
                getBoolean(context.getString(R.string.auto_restart_on_reboot),DEFAULT_AUTO_RESTART_ON_REBOOT);
    }


    //get playCampaign on boot once
    public boolean getPlayCampaignOnBootOnceSettings(Context context)
    {
        return context.getSharedPreferences(context.getString(R.string.settings_sp),context.MODE_PRIVATE).
                getBoolean(context.getString(R.string.play_campaign_on_reboot_once_sp),DEFAULT_PLAY_CAMPAIGN_ON_REBOOT_ONCE);
    }

}
