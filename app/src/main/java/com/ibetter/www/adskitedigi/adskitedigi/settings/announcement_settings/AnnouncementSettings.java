package com.ibetter.www.adskitedigi.adskitedigi.settings.announcement_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

/**
 * Created by ibetter-Dell on 19-02-18.
 */

public class AnnouncementSettings extends Activity implements View.OnClickListener{

    private ToggleButton announceToggleButton; private EditText announcementET,announcementTimesET,announcementGapET;
    private Context context;

    public void onCreate(Bundle savedInstanceState){

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.announcement_settings_toggle);

        setActionBar();

        context = AnnouncementSettings.this;
        announceToggleButton = (ToggleButton)findViewById(R.id.announce_on_btn);
        announcementET = (EditText)findViewById(R.id.announcement_text);
        announcementTimesET = (EditText)findViewById(R.id.announcement_times);
        announcementGapET = (EditText)findViewById(R.id.announcement_gap);
        setSavedValues();

         setToggleButtonFunctions();
    }

    //set action bar
    private void setActionBar()
    {
        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.announcement_settings_action_bar));
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Take appropriate action for each action item click
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setToggleButtonFunctions()
    {

        final LinearLayout announceLinearLayout = findViewById(R.id.text_to_announce_layout);

         announceToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {

                 if(isOn)
                 {
                     announceLinearLayout.setVisibility(View.VISIBLE);
                     checkAndSaveSettings(false);
                 }else
                 {
                     announceLinearLayout.setVisibility(View.GONE);
                     if(saveSettings(null,0,isOn,0))
                     {
                         Toast.makeText(context,getString(R.string.save_settings_success),Toast.LENGTH_SHORT).show();
                     }else
                     {
                         Toast.makeText(context,getString(R.string.unable_to_save_settings),Toast.LENGTH_SHORT).show();
                     }
                 }
             }
         });
    }

    public void onClick(View view)
    {
      switch (view.getId())
      {
          case R.id.save_settings:
              checkAndSaveSettings(true);
              break;
      }
    }

    //check and save settings
    private void checkAndSaveSettings(boolean isClose)
    {
        //get text
       String announcementText = announcementET.getText().toString();
      /* if(new Validations().validateAnnouncementText(announcementText))*/


           int announcementTimes = Constants.convertToInt(announcementTimesET.getText().toString());
           if(announcementTimes<=0)
           {
               announcementTimes = AnnouncementSettingsConstants.Announcement_Text_Min_Times;
           }

          long announcementGap =  Constants.convertToMS(Constants.convertToInt(announcementGapET.getText().toString()));
         if(saveSettings(announcementText,announcementTimes,announceToggleButton.isChecked(),announcementGap))
         {
             Toast.makeText(context,getString(R.string.save_settings_success),Toast.LENGTH_SHORT).show();

             if(isClose)
             {
                onBackPressed();
             }

         }else
         {
             Toast.makeText(context,getString(R.string.unable_to_save_settings),Toast.LENGTH_SHORT).show();
         }

      /* else
       {
           announcementET.setError(getString(R.string.announcement_text_error));
       }*/

    }

    private boolean saveSettings(String announcementText,int announcementTimes,boolean status,long gapSec)
    {
        SharedPreferences settingsModel = getSharedPreferences(getString(R.string.announcement_settings_sp),MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settingsModel.edit();

        if(status)
        {
            settingsEditor.putString(getString(R.string.announcement_settings_announcement_text),announcementText);
            settingsEditor.putInt(getString(R.string.announcement_settings_announcement_times),announcementTimes);
            settingsEditor.putLong(getString(R.string.announcement_settings_announcement_gap),gapSec);
        }

        settingsEditor.putBoolean(getString(R.string.announcement_settings_announcement_status),status);
        return settingsEditor.commit();
    }

    //set saved values
    private void setSavedValues()
    {
        SharedPreferences settingsModel = getSharedPreferences(getString(R.string.announcement_settings_sp),MODE_PRIVATE);
        final LinearLayout announceLinearLayout = findViewById(R.id.text_to_announce_layout);
        if(settingsModel.getBoolean(getString(R.string.announcement_settings_announcement_status),AnnouncementSettingsConstants.DEFAULT_announcement_settings_announcement_status))
        {
           announceLinearLayout.setVisibility(View.VISIBLE);

           announceToggleButton.setChecked(true);
           announcementET.setText(settingsModel.getString(getString(R.string.announcement_settings_announcement_text),getString(R.string.default_announcement_settings_announcement_text)));
           announcementTimesET.setText(String.valueOf(settingsModel.getInt(getString(R.string.announcement_settings_announcement_times),AnnouncementSettingsConstants.Announcement_Text_Min_Times)));
           announcementGapET.setText(String.valueOf(Constants.convertToSec(settingsModel.getLong(getString(R.string.announcement_settings_announcement_gap),AnnouncementSettingsConstants.Announcement_Text_Duration))));
        }else
        {
            announceToggleButton.setChecked(false);
            announceLinearLayout.setVisibility(View.GONE);
        }
    }
}
