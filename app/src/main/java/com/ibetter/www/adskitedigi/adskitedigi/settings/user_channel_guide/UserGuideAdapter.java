package com.ibetter.www.adskitedigi.adskitedigi.settings.user_channel_guide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.MainSettingsActivity;

import java.util.ArrayList;

public class UserGuideAdapter extends ArrayAdapter
{
    private ArrayList<String> settingsNames;
    private TypedArray icons;
    private int layout;
    private Context context;
    private UserGuideActivity activity;

    public UserGuideAdapter(Context context, int layout, ArrayList<String> settingsNames, TypedArray icons,UserGuideActivity activity)
    {

        super(context,layout,settingsNames);
        this.settingsNames = settingsNames;
        this.icons=icons;
        this.layout=layout;
        this.context=context;
        this.activity=activity;
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

        if(position==0)
        {
           tileTextView.setText(new User().getUserChannelName(context));
        }

        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                switch (position)
                {
                    case 0:

                        //context.startActivity(new Intent(context, AudioSettings.class));
                        context.startActivity(new Intent(context, DownloadCampaigns.class));
                        break;
                    case 1:

                        videoConferencing(context);
                        break;

                    case 2:

                        DeviceModel.restartApp(context);
                        activity.finish();
                        break;

                    case 3:

                        context.startActivity(new Intent(context, MainSettingsActivity.class));
                        break;

                    default:

                        break;

                }
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                if(position==0)
                {
                    activity.displayNameEditDialog();
                }
                return false;
            }
        });

        return convertView;
    }


    private void displayNameEditDialog()
    {
        final EditText taskEditText = new EditText(context);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("User Dialog")
                .setMessage("Enter Your Channel Name!")
                .setView(taskEditText)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String channelName = String.valueOf(taskEditText.getText());
                        if(channelName!=null && channelName.length()>2)
                        {
                            new User().updateUserChannelNameInSP(context,channelName);

                        }else {
                            Toast.makeText(context, "Enter Valid Name", Toast.LENGTH_SHORT).show();
                              }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
               dialog.show();
    }

    private void videoConferencing(Context context)
    {

        final String appPackageName = context.getString(R.string.zoom_packagename); // getPackageName() from Context or Activity object
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
            if (launchIntent != null) {
                context.startActivity(launchIntent);

            }else
            {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            }

        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
