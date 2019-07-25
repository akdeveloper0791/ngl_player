package com.ibetter.www.adskitedigi.adskitedigi.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.AdvanceSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.announcement_settings.AnnouncementSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings.AudioSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.auto_campaign_sync_settings.AutoCampaignDownloadSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.default_image_settings.DefaultImageSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.device_profile_settings.DeviceProfileActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.display_image_duration_settings.DisplayImageDurationSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.display_report_image_duration.DisplayReportImageDurationSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.metrics_settings.MetricsSettingsActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.overlay_image_settings.OverlayImageSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.player_statistics_settings.PlayerStatisticsSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.playing_mode_settings.PlayingModeSettingsActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.show_mac_settings.ShowSSMACSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageManagerAccessSettingsMain;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative.InteractiveSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings.ScrollingTextSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.url_settings.URLSettingsAct;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by vineeth_ibetter on 12/30/17.
 */

public class MainSettingsActivity extends Activity
{
    private Context context;
    private ListView settingsListView;
    private SettingsAdaptor settingsAdaptor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context=MainSettingsActivity.this;

        setContentView(R.layout.single_listview_layout);
        settingsListView=findViewById(R.id.single_list_view);

        setActionBar();

        setValuesListView();
    }

    protected void  setValuesListView()
    {
        String[]  settings = getResources().getStringArray(R.array.settings);
        TypedArray icons=getResources().obtainTypedArray(R.array.settings_icons);

        settingsAdaptor=new SettingsAdaptor(context,R.layout.global_settings_support_view,new ArrayList<String>(Arrays.asList(settings)),icons);
        settingsListView.setAdapter(settingsAdaptor);

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        context.startActivity(new Intent(context, DeviceProfileActivity.class));
                        break;
                    case 1:

                        context.startActivity(new Intent(context, PlayingModeSettingsActivity.class));
                        break;

                    case 2:
                        context.startActivity(new Intent(context, ShowSSMACSettings.class));
                        break;
                    case 3:

                        context.startActivity(new Intent(context, DefaultImageSettings.class));
                        break;
                    case 4:

                        context.startActivity(new Intent(context, OverlayImageSettings.class));

                        break;

                    case 5:
                        context.startActivity(new Intent(context, AutoCampaignDownloadSettings.class));
                        break;
                    case 6:
                        context.startActivity(new Intent(context, PlayerStatisticsSettings.class));
                        break;
                    case 7:
                        //finish();
                        context.startActivity(new Intent(context, MetricsSettingsActivity.class));
                        break;
                    case 8:

                        context.startActivity(new Intent(context, AdvanceSettings.class));

                        break;
                    case 9:

                        context.startActivity(new Intent(context, ScrollingTextSettings.class));
                        break;


                    case 10:
                        context.startActivity(new Intent(context, AudioSettings.class));
                        break;


                    case 11:

                        context.startActivity(new Intent(context, DisplayImageDurationSettings.class));

                        break;


                    case 12:

                        context.startActivity(new Intent(context, DisplayReportImageDurationSettings.class));

                        break;

                    case 13:

                        context.startActivity(new Intent(context, AnnouncementSettings.class));

                        break;
                    case 14:
                        context.startActivity(new Intent(context, InteractiveSettings.class));
                        break;
                    case 15:
                        context.startActivity(new Intent(context, URLSettingsAct.class));
                        break;

                    case 16:

                        context.startActivity(new Intent(context, SignageManagerAccessSettingsMain.class));

                        break;
                    default:

                        break;

                }
            }
        });
    }

    public void onResume()
    {
        super.onResume();
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));
    }

    //set ActionBar
    private void setActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_default_action_settings));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // handle item selection
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return true;
        }
    }

    @Override
    public void onBackPressed()
    {
        new User().checkExistingScheduleFiles(MainSettingsActivity.this);
    }


}
