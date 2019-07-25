package com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

/**
 * Created by vineeth_ibetter on 12/30/17.
 */

public class ScrollingTextSettings extends Activity {
    private ScrollTextSettingsModel scrollTextSettingsModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setActionBar();

        if(new DeviceModel().isBelowIceCreamSandWichVersion())
        {

            setContentView(R.layout.scroll_text_settings_layout);


            scrollTextSettingsModel=new ScrollTextSettingsModel(ScrollingTextSettings.this,
                    (LinearLayout)findViewById(R.id.scrolling_text_layout),(Button)findViewById(R.id.update_scroll_text),
                    (EditText)findViewById(R.id.text),(RadioButton)findViewById(R.id.scrolling_customized_text),(RadioButton)findViewById(R.id.scrolling_media_name),(RadioGroup) findViewById(R.id.scrolling_text_mode_rg),(RelativeLayout) findViewById(R.id.scrolling_customized_text_layout));

            setScrollTextHideSettings();
        }
        else
        {

            setContentView(R.layout.scroll_text_settings_layout_switch);

            scrollTextSettingsModel=new ScrollTextSettingsModel(ScrollingTextSettings.this,
                    (LinearLayout)findViewById(R.id.scrolling_text_layout),(Button)findViewById(R.id.update_scroll_text),
                    (EditText)findViewById(R.id.text),(RadioButton)findViewById(R.id.scrolling_customized_text),(RadioButton)findViewById(R.id.scrolling_media_name),(RadioGroup) findViewById(R.id.scrolling_text_mode_rg),(RelativeLayout) findViewById(R.id.scrolling_customized_text_layout));

            setScrollTextHideSettingsSwitch();

        }

        scrollTextSettingsModel.saveScrolledText();

    }


    //POS Item price Hide status settings
    private void setScrollTextHideSettings()
    {
        ToggleButton toggleButton = (ToggleButton)findViewById(R.id.toggle_button);

        //POS Item price Hide status update to server and save in Shared Preferance -- pos_item_price_hide_status
        boolean status=scrollTextSettingsModel.isScrollTextOn();

        toggleButton.setChecked(status);

        if(status)
        {
            scrollTextSettingsModel.displayScrollTextLayout();
        }
        else
        {
            scrollTextSettingsModel.dismissScrollTextLayout();
        }

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {


                scrollTextSettingsModel.setScrollTextStatus(isChecked);

                if(isChecked)
                {
                    scrollTextSettingsModel.displayScrollTextLayout();
                }
                else
                {
                    scrollTextSettingsModel.dismissScrollTextLayout();
                }

            }
        });

    }


    //POS Item price Hide status settings
    private void setScrollTextHideSettingsSwitch()
    {

        Switch switchButton = (Switch)findViewById(R.id.switch_button);

        //POS Item price Hide status update to server and save in Shared Preferance -- pos_item_price_hide_status
        boolean status=scrollTextSettingsModel.isScrollTextOn();

        switchButton.setChecked(status);

        if(status)
        {
            scrollTextSettingsModel.displayScrollTextLayout();
        }
        else
        {
            scrollTextSettingsModel.dismissScrollTextLayout();
        }

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
               scrollTextSettingsModel.setScrollTextStatus(isChecked);

                if(isChecked)
                {
                    scrollTextSettingsModel.displayScrollTextLayout();
                }
                else
                {
                    scrollTextSettingsModel.dismissScrollTextLayout();
                }

            }
        });



    }


    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Text Settings");
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

}
