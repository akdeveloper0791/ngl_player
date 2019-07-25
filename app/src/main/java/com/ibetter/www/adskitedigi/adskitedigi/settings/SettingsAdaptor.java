package com.ibetter.www.adskitedigi.adskitedigi.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.AdvanceSettings;
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

/**
 * Created by vineeth_ibetter on 12/30/17.
 */

public class SettingsAdaptor extends ArrayAdapter {
    private ArrayList<String> settingsNames;
    private TypedArray icons;
    private int layout;
    private Context context;

    public SettingsAdaptor(Context context, int layout, ArrayList<String> settingsNames, TypedArray icons)
    {

        super(context,layout,settingsNames);
        this.settingsNames = settingsNames;
        this.icons=icons;
        this.layout=layout;
        this.context=context;
    }

    @Override
    public int getCount() {
        return settingsNames.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        if (convertView == null)
        {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(layout, null);
        }

        TextView tileTextView=convertView.findViewById(R.id.title) ;
        ImageView iconView=convertView.findViewById(R.id.icon);

        tileTextView.setText(settingsNames.get(position));
        iconView.setImageResource(icons.getResourceId(position, -1));



        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

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
                        //((Activity)context).finish();
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

        return convertView;
    }



}
