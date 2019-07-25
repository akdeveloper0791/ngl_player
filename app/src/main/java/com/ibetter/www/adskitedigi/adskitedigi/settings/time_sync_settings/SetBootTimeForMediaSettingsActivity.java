package com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ibetter.www.adskitedigi.adskitedigi.R;

public class SetBootTimeForMediaSettingsActivity extends Activity implements CompoundButton.OnCheckedChangeListener
{
    private Context context;
    private SharedPreferences settingsSP;
    private SharedPreferences.Editor editor;
    private Switch timeSyncSW;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advance_settings);

        context = SetBootTimeForMediaSettingsActivity.this;

        setActionBar();

        settingsSP = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        editor= settingsSP.edit();

        timeSyncSW = findViewById(R.id.time_sync);
        timeSyncSW.setOnCheckedChangeListener(this);
        timeSyncSW.setChecked(settingsSP.getBoolean(getString(R.string.sync_time_sp), SetBootTimeForMediaSettingsConstants.DEFAULT_TIME_SYNC_STATUS));


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
        switch (compoundButton.getId())
        {
            case R.id.time_sync:

                timeSyncSwitchChange(isOn);

                break;
        }

    }

    private void timeSyncSwitchChange(boolean isOn)
    {

        editor.putBoolean(getString(R.string.sync_time_sp),isOn);

        editor.commit();

       // Toast.makeText(context,"Saved Successfully",Toast.LENGTH_SHORT).show();

    }

    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Advanced Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

}
