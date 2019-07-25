package com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import static com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings.AudioSettingsConstants.DEFAULT_PLAY_AUDIO_VAL;

public class AudioSettings extends Activity implements CompoundButton.OnCheckedChangeListener{

    private Switch playAudioSW;
    private Context context;
    private SharedPreferences settingsSP;
    private SharedPreferences.Editor editor;

    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_settings_layout);

        context = AudioSettings.this;

        setActionBar();

        settingsSP = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        editor= settingsSP.edit();


        playAudioSW = findViewById(R.id.play_offer_audio);
        playAudioSW.setOnCheckedChangeListener(this);
        playAudioSW.setChecked(settingsSP.getBoolean(getString(R.string.play_audio_sp),DEFAULT_PLAY_AUDIO_VAL));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
        switch (compoundButton.getId())
        {
            case R.id.play_offer_audio:

                offerAudioSwitchChange(isOn);

                break;
        }

    }

    private void offerAudioSwitchChange(boolean isOn)
    {
        TextView errorInfo = findViewById(R.id.play_offer_audio_info);

        if(isOn)
        {
            editor.putBoolean(getString(R.string.play_audio_sp),isOn);
            //check for play audio file
             if(!new AudioSettingsConstants().isOfferMediaExists(context))
             {
                 //display error info

                 errorInfo.setVisibility(View.VISIBLE);
                 errorInfo.setText(getString(R.string.play_offer_audio_no_media_info));
                 errorInfo.setTextColor(Color.parseColor("red"));
             }else
             {
                 errorInfo.setVisibility(View.GONE);
             }
        }else
        {
            editor.putBoolean(getString(R.string.play_audio_sp),isOn);
            errorInfo.setVisibility(View.GONE);
        }

        editor.commit();
    }

    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Audio Settings");
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
