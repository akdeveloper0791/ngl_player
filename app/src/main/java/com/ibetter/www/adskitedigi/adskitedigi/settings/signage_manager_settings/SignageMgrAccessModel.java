package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.ConnectingNearBySMMOdel;

/**
 * Created by vineethkumar0791 on 27/03/18.
 */

public class SignageMgrAccessModel {

    public static boolean isSignageMgrAccessOn(Context context,String mode)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getSignageMgrAccessSharedPreference(context);

        String key = context.getString(R.string.is_signage_mgr_access_on);//default
        if(mode.equalsIgnoreCase(context.getString(R.string.sm_access_enterprise_mode)))
        {
            key = context.getString(R.string.is_enterprise_signage_mgr_access_on);
        }

        return saveSP.getBoolean(key, false);
    }


    public static  boolean setSignageMgrAccessStatus(boolean status,Context context,String mode)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getSignageMgrAccessSharedPreference(context);
        SharedPreferences.Editor saveSPEditor = saveSP.edit();

        Log.i(" signage mgr status","::"+status);
        String key = context.getString(R.string.is_signage_mgr_access_on);//default
        if(mode.equalsIgnoreCase(context.getString(R.string.sm_access_enterprise_mode)))
        {
            key = context.getString(R.string.is_enterprise_signage_mgr_access_on);
        }
        saveSPEditor.putBoolean(key, status);

        return saveSPEditor.commit();
    }

    public static String getSignageMgrAccessServiceId(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getSignageMgrAccessSharedPreference(context);

        return saveSP.getString(context.getString(R.string.signage_mgr_access_service_id), null);
    }


    public static  boolean setSignageMgrAccessServiceId(String  serviceId,Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getSignageMgrAccessSharedPreference(context);
        SharedPreferences.Editor saveSPEditor = saveSP.edit();

        Log.i(" signage mgr serviceId","::"+serviceId);
        saveSPEditor.putString(context.getString(R.string.signage_mgr_access_service_id), serviceId);

        return saveSPEditor.commit();

    }

    //switch off neer by mode
    public static void switchOffNearByMode(Context context)
    {
        setSignageMgrAccessStatus(false, context,context.getString(R.string.sm_access_near_by_mode));
        new ConnectingNearBySMMOdel().stopDiscoveringSMService();
    }

    public static  String getSelectedSMMode(Context context)
    {
        SharedPreferences settingsSP = context.getSharedPreferences(context.getString(R.string.settings_sp),Context.MODE_PRIVATE);
        return settingsSP.getString(context.getString(R.string.sm_access_settings_mode_sp), SignageManagerAccessSettingsMain.SM_ACCESS_MODE);
    }

    public static void switchOnNearByModeServices(Context context,String serviceId)
    {
        SignageMgrAccessModel.setSignageMgrAccessStatus(true, context,context.getString(R.string.sm_access_near_by_mode));

        SignageMgrAccessModel.setSignageMgrAccessServiceId(serviceId, context);
        new ConnectingNearBySMMOdel().startDiscoveringSMService(context);

        new User().updateUserPlayingMode(context, Constants.NEAR_BY_MODE,null,null,null);

    }

}
