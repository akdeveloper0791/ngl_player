package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.DisplayAppsActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

public class InteractiveSettings extends Activity
{
    private Context context;
    private SharedPreferences settingsSP;
    private SharedPreferences.Editor editor;

    private ToggleButton actionBtn,timerBtn;

    private TextView packageNameTV;
    private EditText urlEditText,timerET;

    private static final int GET_APP_ACTION_INTENT=1001;

    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.interactive_settings);

        context = InteractiveSettings.this;
        setActionBar();

        actionBtn=findViewById(R.id.action_btn);
        timerBtn= findViewById(R.id.timer_switch);

        urlEditText=findViewById(R.id.url_et);
        new Validations().setMandatoryRequired(context,urlEditText);

        timerET=findViewById(R.id.duration_et);
        packageNameTV=findViewById(R.id.name_tv);

        settingsSP = getSharedPreferences(getString(R.string.user_details_sp), MODE_PRIVATE);
        editor = settingsSP.edit();

        //set previous  Interactive Action Form
        setActions();

        //set previous  Interactive Action Form
        setInteractiveActionForm();

        //Set Interactive InActivity Timer
        actionInactivityTimerSettings();

        //Display App Launcher Action
        displayAppLauncherAction();

        //show Previous Action Settings
          setPreviousSettings();

    }

    //set action bar
    private void setActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.action_settings_string));
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

    private void setPreviousSettings()
    {
        actionBtn.setChecked(settingsSP.getBoolean(context.getString(R.string.display_customer_interactive_action),ActionModel.ACTION_FLAG));

        boolean timerFlag=settingsSP.getBoolean(context.getString(R.string.interactive_inactivity_timer_flag),false);
        timerBtn.setChecked(timerFlag);
          if(timerFlag)
          {
              enableTimerLayout();

          }else
          {
              disableTimerLayout();
          }


        int timer=settingsSP.getInt(getString(R.string.interactive_inactivity_timer),0);
        if(timer>0)
        {
            timerET.setText(String.valueOf(timer));
        }

        int templateId= settingsSP.getInt(context.getString(R.string.action_template_id),ActionModel.ACTION_TEMPLATE_ID);
        switch (templateId)
        {
            case 1:
                RadioButton feedbackRB=findViewById(R.id.feedback_rb);
                feedbackRB.setChecked(true);
               //disableChooseLauncherApp();
                break;

            case 2:
                RadioButton appLaunchRB=findViewById(R.id.app_invoke);
                appLaunchRB.setChecked(true);
               //enableChooseLauncherApp();
                break;
        }

        String url=settingsSP.getString(context.getString(R.string.interactive_action_form_url),null);
        if(url!=null)
        {
            urlEditText.setText(url);
        }

        String packageName=settingsSP.getString(getString(R.string.interactive_action_app_invoke), "");
        packageNameTV.setText(packageName);

    }


    private void setActions()
    {
        RadioGroup actionRadioGroup=findViewById(R.id.interactive_rg);
        actionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.feedback_rb:
                        disableChooseLauncherApp();
                        break;

                    case R.id.app_invoke:
                        enableChooseLauncherApp();
                        break;

                    default:
                        break;
                }
            }
        });
    }

    private void setInteractiveActionForm()
    {
        actionBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                editor.putBoolean(getString(R.string.display_customer_interactive_action), isChecked);
                editor.commit();
            }
        });

        Button saveUrlBtn=findViewById(R.id.save_btn);
        saveUrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                boolean flag=true;
                String  formUrl =urlEditText.getText().toString();
                if (!(new Validations().validateEditText(urlEditText) && URLUtil.isValidUrl(formUrl)))
                {
                    flag = false;
                }

                if(flag)
                {
                    editor.putString(context.getString(R.string.interactive_action_form_url),formUrl);
                    editor.putInt(getString(R.string.action_template_id),1);
                    editor.commit();
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show();

                }else
                {
                    urlEditText.setError(getString(R.string.signage_mgr_url_err_msg));
                    Toast.makeText(context, "Please enter valid form URL", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void displayAppLauncherAction()
    {
        Button chooseAppBtn=findViewById(R.id.choose_btn);
        chooseAppBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(context, DisplayAppsActivity.class),GET_APP_ACTION_INTENT);
            }
        });
    }

    private void actionInactivityTimerSettings()
    {
        timerBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    enableTimerLayout();

                }else
                {
                    disableTimerLayout();
                }
            }
        });

        Button setTimerBtn=findViewById(R.id.set_btn);
        setTimerBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String timeInSec=timerET.getText().toString();
                if(timeInSec!=null && timeInSec.length()>0)
                {
                    //display customer action layout yes/no switch
                    editor.putBoolean(getString(R.string.interactive_inactivity_timer_flag),timerBtn.isChecked());
                    editor.putInt(getString(R.string.interactive_inactivity_timer), Integer.parseInt(timeInSec));
                    editor.commit();
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(context, "Please enter valid inactivity duration", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    private void enableTimerLayout()
    {
        RelativeLayout durationLayout=findViewById(R.id.duration_layout);
        durationLayout.setVisibility(View.VISIBLE);

    }
    private void disableTimerLayout()
    {
        RelativeLayout durationLayout=findViewById(R.id.duration_layout);
        durationLayout.setVisibility(View.GONE);

    }

    private void enableChooseLauncherApp()
    {
        RelativeLayout launcherLayout=findViewById(R.id.app_launcher_layout);
        launcherLayout.setVisibility(View.VISIBLE);
        LinearLayout urlLayout=findViewById(R.id.url_layout);
        urlLayout.setVisibility(View.GONE);

    }

    private void disableChooseLauncherApp()
    {
        RelativeLayout launcherLayout=findViewById(R.id.app_launcher_layout);
        launcherLayout.setVisibility(View.GONE);
        LinearLayout urlLayout=findViewById(R.id.url_layout);
        urlLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {

            case GET_APP_ACTION_INTENT:
                if (resultCode == RESULT_OK && null != data)
                {
                    String packageName=data.getStringExtra("packageName");
                    if(packageName!=null)
                    {
                        editor.putString(context.getString(R.string.interactive_action_app_invoke),packageName);
                        editor.putInt(getString(R.string.action_template_id),2);
                        editor.commit();
                        packageNameTV.setText(packageName);
                    }
                }else
                {
                    Toast.makeText(context, "No Application is Selected to Launch", Toast.LENGTH_SHORT).show();
                }
        }
    }

}
