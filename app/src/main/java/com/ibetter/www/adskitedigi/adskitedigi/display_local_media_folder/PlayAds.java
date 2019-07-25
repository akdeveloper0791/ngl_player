package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.database.Cursor;
import android.os.AsyncTask;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings.SetBootTimeForMediaSettingsConstants;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.CONTINUOUS_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.LOCAL_SCHEDULE_DATE_FORMAT;

public class PlayAds extends AsyncTask<Boolean, Void, MediaInfo> {

    WeakReference<DisplayLocalFolderAds> displayAds;
    private DisplayLocalFolderAds context;

    public PlayAds(DisplayLocalFolderAds activity)
    {
        displayAds = new WeakReference<>(activity);
    }

    @Override
    protected MediaInfo doInBackground(Boolean... values) {

        context =displayAds.get();
        if(context!=null) {
            int currentPosition = 0;

            if (!values[0]) {

                currentPosition = ++(context.prevPosition);
            } else {
                //re initialize the processing files
                context.processingFiles = getRegularSchedules();

                //clear temporary deleted campaigns
                context.tempDeletedCampaigns.clear();

                context.isSkipContinuousPlay = false;

                checkAndSetMediaSkipDisplay();
            }

            if (!(context.processingFiles != null && context.processingFiles.size() > 0)) {
                context.processingFiles =  getRegularSchedules();
                //clear temporary deleted campaigns
                context.tempDeletedCampaigns.clear();

                context.isSkipContinuousPlay = false;
            }

            if (context.processingFiles != null && context.processingFiles.size() > 0) {

                context.mediaInfo = new MediaInfo();

                if (context.processingFiles.size() > currentPosition) {
                    context.mediaInfo = context.processingFiles.get(currentPosition);

                    return context.mediaInfo;
                } else {

                    context.mediaInfo.setMediaRepeating(true);
                    return context.mediaInfo;

                }

            }


        }

        return null;
    }

    @Override
    protected void onPostExecute(MediaInfo mediaInfo) {

        if (displayAds.get()!=null && displayAds.get().isServiceRunning) {

            if (mediaInfo == null) {

               //check for priority schedules and play
                if(context.prioritySchedules.size()>=1)
                {
                    mediaInfo = context.mediaInfo = context.prioritySchedules.remove(0);
                    context.displayAd(mediaInfo);

                }else
                {
                    context.setDefaultImageView();
                    context.mediaInfo = null;
                }

            } else {

                if (mediaInfo.isMediaRepeating()) {

                    //check for priority schedules and play
                    if(context.prioritySchedules.size()>=1)
                    {
                        mediaInfo = context.mediaInfo = context.prioritySchedules.remove(0);
                        context.displayAd(mediaInfo);
                    }else
                    {
                        if (new SetBootTimeForMediaSettingsConstants().getPlayCampaignOnBootOnceSettings(context)) {
                            context.closeOnPlayOnceSettings();


                        } else {
                            context.prevPosition = 0;
                            context.playAds(true);

                        }
                    }


                } else {

                    ///check for priority
                    if(context.prioritySchedules.size()>=1)
                    {
                      MediaInfo prioritySchedule = context.prioritySchedules.get(0);
                      //check for priority
                      if(((mediaInfo.getScheduleType()==CONTINUOUS_PLAY && mediaInfo.getCampaignPriority()>prioritySchedule.getScheduleCampaignPriority()) ||
                              (mediaInfo.getScheduleType()!=CONTINUOUS_PLAY && mediaInfo.getScheduleCampaignPriority() > prioritySchedule.getScheduleCampaignPriority())) ||
                              mediaInfo.getIsForcePlay()==true)
                      {
                          //continue play the normal ad
                          context.displayAd(mediaInfo);
                      }else
                      {
                          //priority schedule has the more priority so play the schedule campaign first
                          //we are interrupting the regular schedule, not allowing it to play so we need to reduce the previous position so that is can be fetched in next loop
                          --context.prevPosition;
                          mediaInfo = context.mediaInfo = context.prioritySchedules.remove(0);
                          context.displayAd(mediaInfo);
                      }
                    }else
                    {
                        context.displayAd(mediaInfo);
                    }

                }

            }
        }

    }

    private Vector<MediaInfo> getRegularSchedules()
    {
        Vector<MediaInfo> playSchedules = new Vector<>();

        SimpleDateFormat sdf = new SimpleDateFormat(LOCAL_SCHEDULE_DATE_FORMAT);
        try {
            Cursor campaigns = CampaignsDBModel.getRegularScheduleCampaigns(context, sdf.format(Calendar.getInstance().getTime()));
            if (campaigns != null && campaigns.moveToFirst()) {
                //prepare playable campaigns
                do {
                    MediaInfo mediaInfo = new MediaInfo();
                    mediaInfo.prepareInfoFromCursor(context,campaigns);
                    playSchedules.add(mediaInfo);
                } while (campaigns.moveToNext());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            return playSchedules;
        }
    }

    private void checkAndSetMediaSkipDisplay() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (context.isAllMediasAreSkipped) {
                    context.setDefaultImageView();
                }

                context.isAllMediasAreSkipped = true;

            }
        });

    }

}
