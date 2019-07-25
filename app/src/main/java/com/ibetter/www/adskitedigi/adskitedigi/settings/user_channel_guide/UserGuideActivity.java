package com.ibetter.www.adskitedigi.adskitedigi.settings.user_channel_guide;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.MainSettingsActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import java.util.ArrayList;
import java.util.Arrays;

public class UserGuideActivity extends Activity
{
    private Context context;
    public  static final String DEFAULT_GUIDE_CHANNEL_NAME = "Green Content";

    private UserGuideAdapter userGuideAdapter;
    private GridView guideListView;

    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        context=UserGuideActivity.this;
        setActionBar();

        setContentView(R.layout.user_guide_activity);

        initializeAdapter();
    }

    private void initializeAdapter()
    {
            guideListView=findViewById(R.id.grid_view);
           String[] settings= context.getResources().getStringArray(R.array.user_guide_string_array);
            TypedArray icons=context.getResources().obtainTypedArray(R.array.user_guide_icons_array);

            userGuideAdapter=new UserGuideAdapter(context,R.layout.user_guide_supportview,new ArrayList<String>(Arrays.asList(settings)),icons,UserGuideActivity.this);
            guideListView.setAdapter(userGuideAdapter);

            guideListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:

                        startActivity(new Intent(context, DownloadCampaigns.class));
                        break;

                    case 1:

                        videoConferencing();
                        break;

                    case 2:

                        DeviceModel.restartApp(context);
                        finish();
                        break;


                    case 3:
                        startActivity(new Intent(context, MainSettingsActivity.class));
                        finish();
                        break;

                    default:

                        break;

                }
            }
        });

    }

    private void videoConferencing()
    {

        final String appPackageName = getString(R.string.zoom_packagename); // getPackageName() from Context or Activity object
        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appPackageName);
            if (launchIntent != null) {
                startActivity(launchIntent);

            }else
            {
               startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            }

        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
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
        actionBar.setTitle(getResources().getString(R.string.user_guide_string));
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
                finish();
                return true;

            default:
                return true;
        }
    }


    @Override
    public void onBackPressed()
    {
        new User().checkExistingScheduleFiles(UserGuideActivity.this);
    }


    public void displayNameEditDialog()
    {
        final EditText taskEditText = new EditText(context);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog .setTitle("Edit Channel Name Dialog !");
       // dialog .setMessage("Enter Your Channel Name!");
        dialog .setView(taskEditText);

        taskEditText.setHint("Enter Your Channel Name");
        taskEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        taskEditText.setText(new User().getUserChannelName(context));

        dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String channelName = String.valueOf(taskEditText.getText());
                        if(channelName!=null && channelName.length()>2)
                        {
                           if(new User().updateUserChannelNameInSP(context,channelName))
                           {
                              Toast.makeText(context, "Channel Name Updated", Toast.LENGTH_SHORT).show();
                               userGuideAdapter.notifyDataSetChanged();

                           }else
                           {
                               Toast.makeText(context, "Name Update Failed", Toast.LENGTH_SHORT).show();
                           }

                        }else {
                            Toast.makeText(context, "Enter Valid Name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
             dialog.setNegativeButton("Cancel", null);
             dialog .create();
             dialog.show();
    }


}
