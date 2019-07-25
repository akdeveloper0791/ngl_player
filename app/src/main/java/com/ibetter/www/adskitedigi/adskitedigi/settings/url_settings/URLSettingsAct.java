package com.ibetter.www.adskitedigi.adskitedigi.settings.url_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

public class URLSettingsAct extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener{

    public static final long DEFAULT_AUTO_SCROLL_DURATION = 10000;
    public static final boolean DEFAULT_AUTO_SCROLL_SETTING = true;
    private final long MINIMUM_SCROLL_DURATION = 1000;

    private Switch autoScrollSwitch;
    private EditText autoScrollDurationEt;
    private Button submitButton;

    private Context context;


    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_settings_act);

        context = URLSettingsAct.this;

        setActionBar();

        autoScrollSwitch = findViewById(R.id.auto_scroll);
        autoScrollSwitch.setOnCheckedChangeListener(this);

        autoScrollDurationEt = findViewById(R.id.auto_scroll_duration);
        submitButton = findViewById(R.id.update);
        submitButton.setOnClickListener(this);

        setValues();

    }

    private void setActionBar()
    {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
       if(actionBar!=null) {
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
        boolean isScrollON = settingsSp.getBoolean(getString(R.string.auto_scroll_web_view_sp),DEFAULT_AUTO_SCROLL_SETTING);

        autoScrollSwitch.setChecked(isScrollON);
        autoScrollDurationEt.setText(String.valueOf(Constants.convertToSec(settingsSp.getLong(getString(R.string.auto_scroll_web_view_duration_sp), (DEFAULT_AUTO_SCROLL_DURATION)))));

        if(isScrollON)
        {
            autoScrollDurationEt.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        }else
        {
            autoScrollDurationEt.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isON) {
        switch (compoundButton.getId())
        {
            case  R.id.auto_scroll:
                handleAutoScrollChanges(isON);
                break;
        }
    }

    private void handleAutoScrollChanges(boolean isON)
    {
        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsSp.edit();

        editor.putBoolean(getString(R.string.auto_scroll_web_view_sp),isON);
        if(editor.commit())
        {
           if(isON)
           {
             //display submit and duration button
               autoScrollDurationEt.setVisibility(View.VISIBLE);
               submitButton.setVisibility(View.VISIBLE);

           }else
           {
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

        long durationInMs = Constants.convertToMS(Constants.convertToInt(autoScrollDurationEt.getText().toString()));

        if(durationInMs<MINIMUM_SCROLL_DURATION)
        {
            durationInMs = MINIMUM_SCROLL_DURATION;
        }

        editor.putLong(getString(R.string.auto_scroll_web_view_duration_sp),durationInMs);
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
