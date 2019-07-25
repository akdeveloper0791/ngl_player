package com.ibetter.www.adskitedigi.adskitedigi.settings.auto_campaign_sync_settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignTriggerService;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

public class AutoCampaignDownloadSettings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener
{
    public static final long DEFAULT_AUTO_DOWNLOAD_CAMPAIGN_DURATION = 1;
    public static final boolean DEFAULT_AUTO_DOWNLOAD__STATUS = true;

    private final long MINIMUM_DURATION = 1;
    private Switch autoDownloadSwitch;
    private EditText durationEt;
    private Button submitButton;
    private Context context;

    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_campaign_download_settings);

        context = AutoCampaignDownloadSettings.this;

        setActionBar();

        autoDownloadSwitch = findViewById(R.id.auto_campaign_download_sw);

        durationEt = findViewById(R.id.duration);
        submitButton = findViewById(R.id.update);
        submitButton.setOnClickListener(this);

        setValues();

        autoDownloadSwitch.setOnCheckedChangeListener(this);

    }

    private void setActionBar()
    {
        ActionBar actionBar = getSupportActionBar();

        if(actionBar!=null)
        {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
            //actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        Toast.makeText(context,"item id - "+item.getItemId(),Toast.LENGTH_SHORT);

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

    private void  setValues()
    {
        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        boolean isON = settingsSp.getBoolean(getString(R.string.is_auto_download_campaign),DEFAULT_AUTO_DOWNLOAD__STATUS);

        autoDownloadSwitch.setChecked(isON);
        durationEt.setText(String.valueOf(settingsSp.getLong(getString(R.string.auto_campaign_download_duration), (DEFAULT_AUTO_DOWNLOAD_CAMPAIGN_DURATION))));

        if(isON)
        {
            durationEt.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        }else
        {
            durationEt.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isON) {
        switch (compoundButton.getId())
        {
            case  R.id.auto_campaign_download_sw:
                updateSettings(isON);
                break;
        }
    }

    private void updateSettings(boolean isON)
    {

        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsSp.edit();

        editor.putBoolean(getString(R.string.is_auto_download_campaign),isON);
        if(editor.commit())
        {
            if(isON)
            {
                //display submit and duration button
                durationEt.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);

                if(User.isPlayerRegistered(context))
                {
                    startService(new Intent(context,AutoDownloadCampaignTriggerService.class));

                }else
                {
                    Toast.makeText(context,"Player not registered",Toast.LENGTH_SHORT).show();
                }



            }else
            {
                durationEt.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);

                AutoDownloadCampaignModel.stopAutoCampaignDownloadService(context);



                Toast.makeText(context, "Switched off successfully", Toast.LENGTH_SHORT).show();

                finish();
            }
        }else
        {
            Toast.makeText(context,"Unable to save settings",Toast.LENGTH_SHORT).show();
        }
    }

    //update button click listeners
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.update:
                updateValues();
                break;
        }
    }

    private void updateValues()
    {
        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsSp.edit();

        long duration = Constants.convertToLong(durationEt.getText().toString());

        if(duration<MINIMUM_DURATION)
        {
            duration = MINIMUM_DURATION;
        }

        editor.putLong(getString(R.string.auto_campaign_download_duration),duration);

        if(editor.commit())
        {
            Toast.makeText(context, "Successfully updated", Toast.LENGTH_SHORT).show();
            finish();
        }else
        {
            Toast.makeText(context,"Unable to save settings",Toast.LENGTH_SHORT).show();
        }

    }




}
