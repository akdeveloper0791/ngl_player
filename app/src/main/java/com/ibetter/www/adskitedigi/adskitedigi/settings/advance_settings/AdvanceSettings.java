package com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings.SetBootTimeForMediaSettingsConstants;

public class AdvanceSettings extends Activity implements CompoundButton.OnCheckedChangeListener {

    private Context context;
    private SharedPreferences settingsSP;
    private SharedPreferences.Editor editor;
    private Switch timeSyncSW,autoRestartSW,playCampaignRebootOnceSW,otherAppLauncherSW;
    private Spinner screenOrientationSpinner;
    private Button chooseAppBtn;
    private TextView appNameTV;
    private static final int GET_APP_ACTION_INTENT=1001;

    public  static final boolean DEFAULT_OTHER_APP_LAUNCHER_STATUS = false;

    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.advance_settings);

        context = AdvanceSettings.this;
        setActionBar();

        settingsSP = getSharedPreferences(getString(R.string.settings_sp), MODE_PRIVATE);
        editor = settingsSP.edit();

        timeSyncSW = findViewById(R.id.time_sync);
        timeSyncSW.setOnCheckedChangeListener(this);
        timeSyncSW.setChecked(settingsSP.getBoolean(getString(R.string.sync_time_sp), SetBootTimeForMediaSettingsConstants.DEFAULT_TIME_SYNC_STATUS));

        screenOrientationSpinner = findViewById(R.id.screen_orientation_spinner);
        initailizeScreenOrientationSpinner();

        setAutoRestartOnReboot();

        setPlayCampaignOnRebootOnce();

        setOtherAppLauncher();

        setAccessibilitySetting();

    }


    private void setAccessibilitySetting()
    {
        TextView settingsTV=findViewById(R.id.accessibility_sett);
        settingsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    startActivity(intent);

                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
    }


    private void setAutoRestartOnReboot()
    {
        autoRestartSW = findViewById(R.id.auto_restart_on_reboot);
        autoRestartSW.setOnCheckedChangeListener(this);
        autoRestartSW.setChecked(settingsSP.getBoolean(getString(R.string.auto_restart_on_reboot), SetBootTimeForMediaSettingsConstants.DEFAULT_AUTO_RESTART_ON_REBOOT));
    }


    private void setPlayCampaignOnRebootOnce()
    {
        playCampaignRebootOnceSW = findViewById(R.id.play_campaign_on_reboot_only_once);
        playCampaignRebootOnceSW.setOnCheckedChangeListener(this);
        playCampaignRebootOnceSW.setChecked(new SetBootTimeForMediaSettingsConstants().getPlayCampaignOnBootOnceSettings(context));
    }

    private void setOtherAppLauncher()
    {
        appNameTV=findViewById(R.id.name_tv);
        otherAppLauncherSW = findViewById(R.id.app_launcher);
        otherAppLauncherSW.setOnCheckedChangeListener(this);
        otherAppLauncherSW.setChecked(settingsSP.getBoolean(getString(R.string.other_app_launch_setting), DEFAULT_OTHER_APP_LAUNCHER_STATUS));

        String packageName=settingsSP.getString(getString(R.string.other_app_package_name), "");
       // Toast.makeText(context, packageName, Toast.LENGTH_SHORT).show();
        appNameTV.setText(packageName);

        chooseAppBtn=findViewById(R.id.choose_btn);
        chooseAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(context,DisplayAppsActivity.class),GET_APP_ACTION_INTENT);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isOn)
    {
        switch (compoundButton.getId())
        {
            case R.id.time_sync:

                timeSyncSwitchChange(isOn);

                break;
            case R.id.auto_restart_on_reboot:

                autoRestartSwitchChange(isOn);

                break;
            case R.id.play_campaign_on_reboot_only_once:

                playCampaignOnRebootOnceSwitchChange(isOn);

                break;

            case  R.id.app_launcher:

                otherAppLaunchSwitchChange(isOn);

                break;
        }

    }

    private void timeSyncSwitchChange(boolean isOn) {

        editor.putBoolean(getString(R.string.sync_time_sp), isOn);

        editor.commit();

        // Toast.makeText(context,"Saved Successfully",Toast.LENGTH_SHORT).show();

    }


    private void autoRestartSwitchChange(boolean isOn)
    {

        editor.putBoolean(getString(R.string.auto_restart_on_reboot), isOn);

        editor.commit();
        // Toast.makeText(context,"Saved Successfully",Toast.LENGTH_SHORT).show();
    }

    private void playCampaignOnRebootOnceSwitchChange(boolean isOn)
    {

        editor.putBoolean(getString(R.string.play_campaign_on_reboot_once_sp), isOn);

        if(isOn) {
            autoRestartSW.setChecked(true);
            editor.putBoolean(getString(R.string.auto_restart_on_reboot), isOn);
        }

        editor.commit();
        // Toast.makeText(context,"Saved Successfully",Toast.LENGTH_SHORT).show();
    }



    private void otherAppLaunchSwitchChange(boolean isOn)
    {

        editor.putBoolean(getString(R.string.other_app_launch_setting), isOn);

        if(isOn)
        {
          enableChooseLauncherApp();
        }else
        {
            disableChooseLauncherApp();
        }

        editor.commit();
    }

    private void enableChooseLauncherApp()
    {
        RelativeLayout launcherLayout=findViewById(R.id.app_launcher_layout);
        launcherLayout.setVisibility(View.VISIBLE);

    }

    private void disableChooseLauncherApp()
    {
        RelativeLayout launcherLayout=findViewById(R.id.app_launcher_layout);
        launcherLayout.setVisibility(View.GONE);
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
        switch (item.getItemId()) {
            case android.R.id.home:

                onBackPressed();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //initialize screen orientation Spinner
    private void initailizeScreenOrientationSpinner()
    {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.screen_orientation, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        screenOrientationSpinner.setAdapter(adapter);

        screenOrientationSpinner.setSelection(adapter.getPosition(getSelectedOrientation()),true);

        screenOrientationSpinner.setOnItemSelectedListener(screenOrientationSpinnerListener);
    }

    //get selected screen orientation
    private String getSelectedOrientation()
    {
      return settingsSP.getString(getString(R.string.screen_orientation_sp),ScreenOrientationModel.DEFAULT_SCREEN_ORIENTATION);
    }

    private boolean updateScreenOrientation(String orientation)
    {
       return editor.putString(getString(R.string.screen_orientation_sp),orientation).commit();
    }

    AdapterView.OnItemSelectedListener screenOrientationSpinnerListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

            String[] values = getResources().getStringArray(R.array.screen_orientation);


            if (!getSelectedOrientation().equalsIgnoreCase(values[position])) {

                if (updateScreenOrientation(values[position]))
                {
                    setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(context));
                }else
                {
                    Toast.makeText(context,getString(R.string.unable_to_save_settings),Toast.LENGTH_SHORT).show();
                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GET_APP_ACTION_INTENT:
                if (resultCode == RESULT_OK && null != data)
                {
                    String packageName=data.getStringExtra("packageName");
                    appNameTV.setText(packageName);
                }else
                {
                    Toast.makeText(context, "No Application is Selected to Launch", Toast.LENGTH_SHORT).show();
                }
        }
    }


}