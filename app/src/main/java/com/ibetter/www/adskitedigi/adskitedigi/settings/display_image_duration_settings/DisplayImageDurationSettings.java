package com.ibetter.www.adskitedigi.adskitedigi.settings.display_image_duration_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

/**
 * Created by vineeth_ibetter on 1/24/18.
 */

public class DisplayImageDurationSettings extends Activity
{
    private DisplayImageDurationSettingsModel displayImageDurationSettingsModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.display_image_duration_settings);

        displayImageDurationSettingsModel=new DisplayImageDurationSettingsModel(DisplayImageDurationSettings.this,(EditText)findViewById(R.id.duration_et),(Button)findViewById(R.id.update));

        displayImageDurationSettingsModel.updateDuration();

        setActionBar();

    }

    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Display Image Duration Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
