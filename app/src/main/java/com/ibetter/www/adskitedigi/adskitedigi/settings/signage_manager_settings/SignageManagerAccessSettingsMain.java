package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.ConnectingNearBySMMOdel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriceSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

import java.util.Arrays;

public class SignageManagerAccessSettingsMain extends AppCompatActivity {

   public final static String SM_ACCESS_MODE ="Enterprise";
   private SharedPreferences settingsSP;

    protected void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.sigange_manager_access_settings_main);

        settingsSP = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);

        setCustomizedActionBar();

        checkAndDisplaySelectedMode();

    }

    private void setCustomizedActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setTitle(getString(R.string.signage_mgr_access_settings_text));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void checkAndDisplaySelectedMode()
    {

        String selectedMode = settingsSP.getString(getString(R.string.sm_access_settings_mode_sp),SM_ACCESS_MODE);
        displaySelectedMode(selectedMode);

    }

    private void displaySelectedMode(String selectedMode)
    {
        if(selectedMode.equalsIgnoreCase(getString(R.string.sm_access_near_by_mode)))
        {
            displayNearBySettings();
        }
        else if(selectedMode.equalsIgnoreCase(getString(R.string.sm_access_enterprise_mode)))
        {
            displayEnterPriseSettings();
        }
    }

    private void displayNearBySettings()
    {
        Fragment nearByFragment = new SignageManagerAccessSettings();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, nearByFragment).commit();
    }

    private void displayEnterPriseSettings()
    {

        Fragment enterpriseFragment = new EnterPriceSettings();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, enterpriseFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sm_access_settings, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();

                return true;

            case R.id.sm_access_modes:
                 displaySMAccessModesSpinner();
                return true;

                default:
                    return false;
        }
    }
    //display modes spinner
    private void displaySMAccessModesSpinner()
    {
        final String previousSelectedMode =  settingsSP.getString(getString(R.string.sm_access_settings_mode_sp),SM_ACCESS_MODE);
        AlertDialog.Builder itemsBuilder = new AlertDialog.Builder(this);
        final String[] options = getResources().getStringArray(R.array.sm_access_modes);

        int selectedOptionPosition = Arrays.asList(options).indexOf(previousSelectedMode);

        itemsBuilder.setSingleChoiceItems(options, selectedOptionPosition, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                String chosenOption = options[i];

                //switch modes
                switchModes(previousSelectedMode,chosenOption);

                displaySelectedMode(chosenOption);


            }
        });




        Dialog overFlowWindow = itemsBuilder.create();
        overFlowWindow.setCanceledOnTouchOutside(true);
        overFlowWindow.requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams wmlp = overFlowWindow.getWindow().getAttributes();
        wmlp.gravity = Gravity.RIGHT;
        wmlp.height = LinearLayout.LayoutParams.MATCH_PARENT;
        wmlp.width = LinearLayout.LayoutParams.MATCH_PARENT;;


        wmlp.gravity = Gravity.TOP | Gravity.RIGHT;
       wmlp.x = LinearLayout.LayoutParams.WRAP_CONTENT;   //x position
        wmlp.y =  70;   //y position


       overFlowWindow.getWindow().setAttributes(wmlp);
        overFlowWindow.show();
    }

    //check and switch off the mode
    private void switchModes(String previousMode,String currentMode)
    {
        if(!previousMode.equalsIgnoreCase(currentMode))
        {
           switchOffPreviousMode(previousMode);
           switchToNewMode(currentMode);
        }

    }

    //switch off previous mode
    private void switchOffPreviousMode(String mode)
    {

        if(mode.equalsIgnoreCase(getString(R.string.sm_access_near_by_mode)))
        {
            //switchOffNearByMode
            switchOffNearByMode();
        }else if(mode.equalsIgnoreCase(getString(R.string.sm_access_enterprise_mode)))
        {
            EnterPriseSettingsModel.switchOffEnterPriseSettings(SignageManagerAccessSettingsMain.this);
        }
    }

    //switch off neer by mode
    private void switchOffNearByMode()
    {
        SignageMgrAccessModel.setSignageMgrAccessStatus(false, this,getString(R.string.sm_access_near_by_mode));
        new ConnectingNearBySMMOdel().stopDiscoveringSMService();
    }

    private void switchToNewMode(String newMode)
    {
        SharedPreferences.Editor editor = settingsSP.edit();
        editor.putString(getString(R.string.sm_access_settings_mode_sp),newMode);
        editor.commit();
    }
}


