package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Permissions;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.AskEnterpriseModePermissions;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriceSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

public class ToggleSMServices extends IntentService {

    private Context context;
    private String serviceId;

    public ToggleSMServices()
    {
        super(ToggleSMServices.class.getName());
        context = ToggleSMServices.this;
    }

    protected void onHandleIntent(Intent intent)
    {
        String mode = intent.getStringExtra("switch_to");
        if(intent.hasExtra("service_id"))
        {
            serviceId = intent.getStringExtra("service_id");
        }

        if(mode!=null)
        {
           //check permissions
           if(checkRespectiveServicePermissions(mode)) {

               switchModes(mode);
           }



        }
    }

    private boolean checkRespectiveServicePermissions(String mode)
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {

            if (mode.equalsIgnoreCase(context.getString(R.string.sm_access_enterprise_mode))) {

                if(Permissions.hasPermissions(context, EnterPriceSettings.permissions)) {

                    return true;
                }
                else {

                    //start activity to take the enterprise settings permissions
                    askForEnterPriseModePermissions();

                    return false;
                }
            }else
            {
                return true;
            }


        }else {
            //no need to check permission run time
            return true;
        }
    }

    private void switchModes(String newMode)
    {
        switchOffPreviousMode();
        switchToNewMode(newMode);

        //switch on the respective services
        if(newMode.equalsIgnoreCase(getString(R.string.sm_access_enterprise_mode)))
        {
            //check for permissions

            EnterPriseSettingsModel.startEnterPriseModel(ToggleSMServices.this);
        }else if(newMode.equalsIgnoreCase(getString(R.string.sm_access_near_by_mode)))
        {
            //switch On near by mode services
            switchOnNearByModeServices();
        }
    }

    private void switchToNewMode(String newMode)
    {
        SharedPreferences settingsSP = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsSP.edit();
        editor.putString(getString(R.string.sm_access_settings_mode_sp),newMode);
        editor.commit();
    }



    //switch off previous mode
    private void switchOffPreviousMode()
    {
        SharedPreferences settingsSP = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        String selectedMode = settingsSP.getString(getString(R.string.sm_access_settings_mode_sp),SignageManagerAccessSettingsMain.SM_ACCESS_MODE);
        if(selectedMode.equalsIgnoreCase(getString(R.string.sm_access_near_by_mode)))
        {
            //switchOffNearByMode
          SignageMgrAccessModel.switchOffNearByMode(ToggleSMServices.this);

        }else if(selectedMode.equalsIgnoreCase(getString(R.string.sm_access_enterprise_mode)))
        {
            EnterPriseSettingsModel.switchOffEnterPriseSettings(ToggleSMServices.this);
        }
    }

    private void askForEnterPriseModePermissions()
    {
        Intent intent = new Intent(context, AskEnterpriseModePermissions.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void switchOnNearByModeServices()
    {
       SignageMgrAccessModel.switchOnNearByModeServices(context,serviceId);
    }
}
