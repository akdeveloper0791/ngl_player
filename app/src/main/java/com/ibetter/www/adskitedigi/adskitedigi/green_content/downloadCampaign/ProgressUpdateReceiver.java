package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class ProgressUpdateReceiver extends BroadcastReceiver {
    WeakReference<Activity> weakReference;
    public ProgressUpdateReceiver(WeakReference<Activity> weakReference) {
        this. weakReference=weakReference;

    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            if(weakReference!=null)
            {
            String action=intent.getStringExtra("action");
            DownloadCampaigns activity=(DownloadCampaigns) weakReference.get();

            switch (action) {
                case GCModel.INIT_DOWNLOAD:
                    activity.initDownloadProgressInfo(intent.getStringExtra("name"));

                    break;
                case GCModel.DOWNLOAD_PROGRESS:

                    activity.updateDownloadProgressInfo(intent.getStringExtra("name"), intent.getIntExtra("progress", 0),intent.getIntExtra("position",0),intent.getIntExtra("total_files",0),intent.getStringExtra("resource_name"));

                    break;

                case GCModel.DOWNLOAD_SUCCESS:
                    activity.downloadProgressSuccess(intent.getStringExtra("name"));
                    break;

                case GCModel.REMOVE_PROGRESS:
                    activity.removeDownloadProgressInfo(intent.getStringExtra("name"));

                    break;
                case GCModel.REMOVE_ALL_PROGRESS:
                    activity.removeDownloadAllProgressInfo();

                    break;
                case GCModel.DOWNLOAD_ERROR:

                    activity.updateDownloadProgressErrorInfo(intent.getStringExtra("name"), intent.getStringExtra("error"));
                    //NEED TO HANDLE
                    break;

                case GCModel.GET_DOWNLOADING_FILES:


                    String status=intent.getStringExtra("status");
                    String campaignName=intent.getStringExtra("campaign_name");

                    String errorMsg=null;

                    if(intent.hasExtra("error_msg")) {
                         errorMsg = intent.getStringExtra("error_msg");
                    }

                    int progress=0;
                    int totalFiles=0;
                    String resourceName=null;
                    if(intent.hasExtra("total_files")) {
                        progress = intent.getIntExtra("progress", 0);
                        totalFiles = intent.getIntExtra("resource_name", 0);

                        resourceName = intent.getStringExtra("resource_name");
                    }

                    int position=   intent.getIntExtra("position",0);
                    activity.getDownloadingPendingFiles((ArrayList)intent.getSerializableExtra("files_list"),status,campaignName,progress,errorMsg,position,totalFiles,resourceName);

                    break;
             }
            }

             }catch (Exception E)
        {
          E.printStackTrace();
        }

    }


}
