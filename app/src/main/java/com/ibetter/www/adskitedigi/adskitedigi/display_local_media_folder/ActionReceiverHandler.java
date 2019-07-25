package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ActionReceiverHandler implements ActionReceiver.ActionReceiverCallBacks {

    private WeakReference<DisplayLocalFolderAds> activityRef;

    public ActionReceiverHandler(DisplayLocalFolderAds activity)
    {
      activityRef = new WeakReference<>(activity);
    }

    public void onSkipCampaign(String campaignName)
    {
        //handle on skip
        if(activityRef!=null && activityRef.get()!=null && campaignName!=null)
        {

            activityRef.get().checkAndSkipPlayingCampaign(campaignName);
        }
    }

    //on pause campaign
    public void onPauseCampaign()
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            activityRef.get().checkAndPausePlayingMedia();
        }
    }

    //on resume campaign
    public void onResumeCampaign()
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            activityRef.get().checkAndResumePlayingMedia();
        }
    }

    public void handleCampaignRule(ArrayList<File> campaignFilesArray)
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            activityRef.get().handleCampaignRule(campaignFilesArray);
        }
    }

    public void handleCampaignRuleNew(ArrayList<String> campaigns,String rule)
    {

        if(activityRef!=null && activityRef.get()!=null)
        {

            activityRef.get().handleCampaignRuleNew(campaigns,rule);
        }
    }

    public void handleDeletedCampaigns(ArrayList<Long> deletedCampaigns)
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            Log.d("PlayAds","Inside PlayAds inside handle deleted campaings total deleted are"+deletedCampaigns.size());
            DisplayLocalFolderAds context = activityRef.get();
            for(long deletedCampaignId: deletedCampaigns)
            {
                Log.d("PlayAds","Inside PlayAds inside handle deleted campaings deletedCampaignId"+deletedCampaignId);
                //save to temp deleted list
                context.tempDeletedCampaigns.add(deletedCampaignId);
                //check and skip current campaign
                if(context.mediaInfo!=null && context.mediaInfo.getCampaignLocalId()== deletedCampaignId)
                {
                    Log.d("PlayAds","Inside PlayAds inside handle deleted campaings skipcurrentplaying media");

                    context.skipCurrentPlayingMedia();
                }
            }
        }
    }

    //on resume campaign
    public void successResponse(String msg)
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            activityRef.get().successResponse(msg);
        }
    }

    //on resume campaign
    public void failureResponse(String msg)
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            activityRef.get().failureResponse(msg);
        }
    }

}
