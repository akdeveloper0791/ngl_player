package com.ibetter.www.adskitedigi.adskitedigi.green_content;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaignResultReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignTriggerService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;


public class  GCProfileActivity extends Activity
{
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
            Log.i("inside1","GCProfileActivity");
            super.onCreate(savedInstanceState);

            setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));
            context = GCProfileActivity.this;

             setActionBar();

            setContentView(R.layout.gc_profile_activity);
            Log.i("inside", "GCProfileActivity");
            setUserProfileInfo();
            logoutGCAccount();
    }


    //set ActionBar
    private void setActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.campaign_user_profile));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Take appropriate action for each action item click
        switch (item.getItemId())
        {
            case android.R.id.home:
                 super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUserProfileInfo()
    {
        try {
            SharedPreferences sp = getSharedPreferences(getString(R.string.user_details_sp), MODE_PRIVATE);
            String emailId = sp.getString(getString(R.string.gc_user_email_id), null);
            String userName = sp.getString(getString(R.string.gc_user_first_name), null);

            TextView userNameTV = findViewById(R.id.name_tv);
            TextView emailTV = findViewById(R.id.mail_tv);

            if (userName != null) {
                userNameTV.setText(userName);
            }
            if (emailId != null) {
                emailTV.setText(emailId);
            }
        }catch (Exception E)
        {
            E.printStackTrace();
        }
    }

    private void logoutGCAccount()
    {
        Button logoutBtn=findViewById(R.id.logout_btn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                logOutDialog();
            }
        });


    }


    private void logOutDialog()
    {
        AlertDialog.Builder msgDialog = new AlertDialog.Builder(context);

        msgDialog.setMessage(getString(R.string.gc_user_logout_string));

        msgDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        msgDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(resetGCUserInfoInSP())
                {
                    invalidateOptionsMenu();
                    notifyDownloadService();
                    Toast.makeText(context, "Logout is Successful", Toast.LENGTH_SHORT).show();
                    sendResult();


                    dialog.dismiss();
                }
            }
        });

        msgDialog.create().show();
    }


    private boolean resetGCUserInfoInSP()
    {
        SharedPreferences.Editor userInfoEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userInfoEditor.putInt(getString(R.string.gc_user_id),0);
        userInfoEditor.putString(getString(R.string.gc_user_unique_key),null);
        userInfoEditor.putString(getString(R.string.gc_user_first_name),null);
        return userInfoEditor.commit();
    }

    private void sendResult()
    {
        Intent intent = new Intent();
        intent.putExtra("is_log_out",true);

        setResult(RESULT_OK, intent);

        finish();
    }


    private void notifyDownloadService()
    {

        if(DownloadCampaignsService.isServiceOn) {

            DownloadCampaignsService.downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.STOP_SERVICE, null);

        }
        if(AutoDownloadCampaignTriggerService.isServiceOn)
        {
            AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver.send(AutoDownloadCampaignReceiver.STOP_SERVICE, null);

        }
    }

}
