package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.announcement_settings.AnnouncementSettingsConstants;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;
import java.util.Vector;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.CONTINUOUS_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.LOCAL_SCHEDULE_DATE_FORMAT;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.SCHEDULE_CONTINUOUS_PLAY;

public class RefreshCampaignsData extends TimerTask {
    WeakReference<DisplayLocalFolderAds> activityRef;

    public RefreshCampaignsData(DisplayLocalFolderAds activity)
    {
        activityRef= new WeakReference<>(activity);
    }

    public void run() {

     if(activityRef!=null && activityRef.get()!=null)
     {
         DisplayLocalFolderAds context = activityRef.get();
         try
         {
             SimpleDateFormat sdf = new SimpleDateFormat(LOCAL_SCHEDULE_DATE_FORMAT);
             Cursor schedules =  CampaignsDBModel.getRegularScheduleCampaigns(context, sdf.format(Calendar.getInstance().getTime()));
             SimpleArrayMap<Long,MediaInfo> tempNewSchedules = new SimpleArrayMap<>(schedules.getCount());
             if(schedules!=null && schedules.moveToFirst())
             {

                 do {
                     MediaInfo mediaInfo = new MediaInfo();
                     mediaInfo.prepareInfoFromCursor(context,schedules);
                     tempNewSchedules.put(schedules.getLong(schedules.getColumnIndex(CampaignsDBModel.LOCAL_ID)),mediaInfo);
                 }while(schedules.moveToNext());


                 //check and update existing campaigns if any change,,skip campaigns which are not in schedule phase
                 Vector<MediaInfo> tempProcessingSchedules = new Vector<>();

                 int index=0;
                 for(MediaInfo runningMedia:context.processingFiles)
                 {
                     if(tempNewSchedules.containsKey(runningMedia.getCampaignLocalId()))
                     {
                         MediaInfo tempScheduleInfo = tempNewSchedules.remove(runningMedia.getCampaignLocalId());
                         context.processingFiles.set(index,tempScheduleInfo);//update with new schedule

                     }else
                     {
                         //if the campaign is not found add it  to skip list
                         tempProcessingSchedules.add(runningMedia);
                     }

                     ++index;
                 }

                 int tempScheduleSize = tempNewSchedules.size();
                 //process new regular schedules base on selected mode
                 int cmsMode = new User().getUserPlayingMode(context);
                 boolean isAnnouncementON = AnnouncementSettingsConstants.getAnnouncementSettings(context);
                 int previousProcessingFilesSize = context.processingFiles.size();
                 for(int i=0;i<tempScheduleSize;i++)
                 {
                     MediaInfo info = tempNewSchedules.get(tempNewSchedules.keyAt(i));
                     if(cmsMode== Constants.NEAR_BY_MODE && isAnnouncementON)
                     {
                         //add to priority list
                         context.priorityList.add(info);
                     }else
                     {
                         if(info.getScheduleType()== ScheduleCampaignModel.SCHEDULE_CONTINUOUS_PLAY )
                         {
                             //we have found schedule campaign to make space for schedule campaign we will temporarily skip continuous play
                             context.isSkipContinuousPlay = true;
                         }
                         context.processingFiles.add(info);
                     }

                 }

                 if(context.isSkipContinuousPlay && context.mediaInfo!=null && context.mediaInfo.getScheduleType()==CONTINUOUS_PLAY && context.mediaInfo.getIsForcePlay()==false)
                 {
                     context.skipCurrentPlayingMedia();
                 }

                 //skip unwanted campaigns
                 skipModifiedCampaigns(tempProcessingSchedules);

                 if(cmsMode== Constants.NEAR_BY_MODE && isAnnouncementON && context.priorityList.size()>=1)
                 {
                     context.initPriorityAd();
                 }else
                 {


                     if(previousProcessingFilesSize<=0)
                     {

                         //if nothing is playing initiate play
                         if(context.mediaInfo==null)
                         {
                             context.prevPosition = -1;//
                             context.playAds(false);
                         }
                         else if(context.mediaInfo.getScheduleCampaignPriority()<context.processingFiles.get(0).getScheduleCampaignPriority() && context.mediaInfo.getIsForcePlay()==false)
                         {
                             //check for priority of running media and stop playing
                             context.skipCurrentPlayingMedia();
                         }

                     }
                 }




             }else
             {
                 skipModifiedCampaigns(context.processingFiles);
             }

             //check for scheduled campaigns
             //don't include the already queued campaigns
             ArrayList<Long> queuedSchedules = new ArrayList<>(context.prioritySchedules.size());
             for(MediaInfo info:context.prioritySchedules)
             {
                 queuedSchedules.add(info.getCampaignLocalId());
             }
             //reset priority schedules,, to get new list
             //context.prioritySchedules.clear();
             if(context.mediaInfo!=null)
             {
                 if(!(context.mediaInfo.getScheduleType()==CONTINUOUS_PLAY || context.mediaInfo.getScheduleType()==SCHEDULE_CONTINUOUS_PLAY))
                 {
                     queuedSchedules.add(context.mediaInfo.getCampaignLocalId());
                 }
             }

             Cursor scheduledCampaigns =  CampaignsDBModel.getScheduledCampaigns(context, sdf.format(Calendar.getInstance().getTime()),
                     TextUtils.join(",",queuedSchedules));
             if(scheduledCampaigns!=null && scheduledCampaigns.moveToNext())
             {

                 do {
                     MediaInfo mediaInfo = new MediaInfo();
                     mediaInfo.prepareInfoFromCursor(context,scheduledCampaigns);
                     mediaInfo.setScheduleAdditionalInfo(scheduledCampaigns.getString(scheduledCampaigns.getColumnIndex(CampaignsDBModel.SCHEDULE_TABLE_ADDITIONAL_INFO)));

                     boolean isAdded = false;
                     for(int priorityIndex=0;priorityIndex<context.prioritySchedules.size();priorityIndex++)
                     {
                       if(mediaInfo.getScheduleCampaignPriority()>context.prioritySchedules.get(priorityIndex).getScheduleCampaignPriority())
                       {
                           isAdded=true;
                           context.prioritySchedules.add(priorityIndex,mediaInfo);
                           break;
                       }
                     }
                     if(isAdded==false)
                     {
                         context.prioritySchedules.add(mediaInfo);
                     }

                 }while(scheduledCampaigns.moveToNext());

                context.scheduleCheckForInterruption();

             }

             //start check and clean expired schedules
             context.startService(new Intent(context,DeleteExpiredCampaigns.class));

         }catch (Exception e) {

           e.printStackTrace();
         }finally {
             try {


                 if (activityRef!=null && activityRef.get()!=null && activityRef.get().isServiceRunning) {

                     //start schedule again
                     activityRef.get().callFileObserver();
                     //fileObsereverTimer.schedule(new CustomisedFileObserver(), Constants.FILE_OBSERVER_DURATION);

                 }
             } catch (Exception e) {
                 //restart activity
                 e.printStackTrace();
                 if(activityRef.get()!=null)
                 {
                     activityRef.get().restartActivity();
                 }


             }
         }


     }
    }

    private void skipModifiedCampaigns(Vector<MediaInfo> skippedCampaigns)
    {
        ArrayList<Long> modifiedCampaigns = new ArrayList<>();
        for(MediaInfo model:skippedCampaigns)
        {
            modifiedCampaigns.add(model.getCampaignLocalId());
        }

        if(modifiedCampaigns.size()>=1)
        {
            if(DisplayLocalFolderAds.actionReceiver!=null)
            {
                Bundle extras = new Bundle(2);
                extras.putSerializable("deleted_campaigns", modifiedCampaigns);
                DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.HANDLE_DELETE_CAMPAIGNS,extras);
            }
        }


    }

}
