package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.content.Context;
import android.database.Cursor;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by vineeth_ibetter on 1/8/18.
 */

public class MediaInfo {
    private String pathname,mediaType,mediaName,bgAudioFileName,fileData,info,scheduleFrom,scheduleTo,scheduleAdditionalInfo,associatedRule;
    private boolean isMediaRepeating=false,canPlayBgAudio=false,isSkip=false,isPlaying=true,isForcePlay = false;
    private long duration,mediaResumedAt,campaignLocalId; //duration is in seconds
    private HashMap<String,Object> multiRegProperties;
    private JSONObject infoJson;
    private int scheduleType,campaignPriority,scheduleCampaignPriority;
    private long campaignStartTime,campaignEndTime,scheduleLocalId;

    private long singleVideoRegId;
    private int singleVideoRegPausedAt;

    public MediaInfo()
    {
        mediaResumedAt = Calendar.getInstance().getTimeInMillis();
    }

    public String getPathname() {
        return pathname;
    }


    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public boolean isMediaRepeating() {
        return isMediaRepeating;
    }

    public void setMediaRepeating(boolean mediaRepeating) {
        isMediaRepeating = mediaRepeating;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getMediaName()
    {
        return mediaName;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setBgAudioFileName(String bgAudioFileName)
    {
        this.bgAudioFileName = bgAudioFileName;
    }

    public String getBgAudioFileName()
    {
        return bgAudioFileName;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public void setCanPlayBgAudio(boolean canPlayBgAudio)
    {
        this.canPlayBgAudio = canPlayBgAudio;
    }

    public boolean getCanPlayBgAudio()
    {
        return canPlayBgAudio;
    }

    public void setMultiRegProperties(HashMap<String,Object> multiRegProperties)
    {
        this.multiRegProperties = multiRegProperties;
    }


    public boolean isMultiRegPlayingVideoWithSound(Context context)
    {
        if(multiRegProperties!=null && multiRegProperties.containsKey(context.getString(R.string.has_video_with_sound_multi_reg_prop)))
        {
            return (Boolean) multiRegProperties.get(context.getString(R.string.has_video_with_sound_multi_reg_prop));
        }else {
            return false;
        }
    }



    public boolean getIsSkip()
    {
        return isSkip;
    }

    public void setMediaResumedAt(long timeInMs)
    {
        mediaResumedAt = timeInMs;
    }

    public void setIsPlaying(boolean isPlaying)
    {
        this.isPlaying = isPlaying;
    }

    public boolean getIsPlaying()
    {
        return isPlaying;
    }

    public void resetDuration(long mediaDurationInMs)
    {

        if(mediaDurationInMs!=-1)//infinity display
        {

            long currentTimeInMs = Calendar.getInstance().getTimeInMillis();
            if (mediaResumedAt > 0 && mediaResumedAt <= currentTimeInMs) {


                long mediaPlayedDurationInMs = (currentTimeInMs - mediaResumedAt);

                //update remaining duration
                if (mediaDurationInMs >= mediaPlayedDurationInMs) {
                    duration = TimeUnit.MILLISECONDS.toSeconds((mediaDurationInMs - mediaPlayedDurationInMs));//in seconds
                } else {
                    duration = 0;//seconds
                }
            } else {

                duration = 0;//seconds
            }
        }
    }

    public void setInfoJson(JSONObject infoJson)
    {
        this.infoJson = infoJson;
    }

    public JSONObject getInfoJson()
    {
        return infoJson;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }

    public String getInfo()
    {
        return info;
    }

    public String getScheduleFrom()
    {
        return scheduleFrom;
    }



    public String getScheduleTo()
    {
        return scheduleTo;
    }



    public int getScheduleType()
    {
        return scheduleType;
    }


    public long getCampaignLocalId()
    {
        return campaignLocalId;
    }

    public boolean getIsForcePlay()
    {
        return isForcePlay;
    }

    public void setForcePlay(boolean isForcePlay)
    {
        this.isForcePlay = isForcePlay;
    }

    public void prepareInfoFromCursor(Context context,Cursor campaigns)
    {

        mediaType = context.getString(R.string.app_default_txt_name);
        mediaName = campaigns.getString(campaigns.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME));
        info = campaigns.getString(campaigns.getColumnIndex(CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO));
        scheduleType = campaigns.getInt(campaigns.getColumnIndex(CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE));
        scheduleFrom = campaigns.getString(campaigns.getColumnIndex(CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_FROM));
        scheduleTo = campaigns.getString(campaigns.getColumnIndex(CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_TO));
        campaignLocalId = campaigns.getLong(campaigns.getColumnIndex(CampaignsDBModel.LOCAL_ID));
        isSkip = (campaigns.getInt(campaigns.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP))==1?true:false);
        campaignPriority = campaigns.getInt(campaigns.getColumnIndex(CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_PRIORITY));
        scheduleCampaignPriority = campaigns.getInt(campaigns.getColumnIndex(CampaignsDBModel.SCHEDULE_TABLE_SCHEDULE_PRIORITY));
        scheduleLocalId = campaigns.getLong(campaigns.getColumnIndex("schedule_id"));
    }

    public void setScheduleAdditionalInfo(String scheduleAdditionalInfo)
    {
        this.scheduleAdditionalInfo = scheduleAdditionalInfo;
    }

    public String getScheduleAdditionalInfo()
    {
        return scheduleAdditionalInfo;
    }

    public void initCampaignStartTime()
    {
        this.campaignStartTime = Calendar.getInstance().getTimeInMillis();

    }

    public long calculateCampaignPlayedDuration()
    {
        if(campaignStartTime>0)
        {
            campaignEndTime = Calendar.getInstance().getTimeInMillis();
            return TimeUnit.MILLISECONDS.toSeconds(campaignEndTime-campaignStartTime);
        }else
        {
            return 0;
        }

    }


    public int getCampaignPriority()
    {
        return campaignPriority;
    }

    public int getScheduleCampaignPriority()
    {
        return scheduleCampaignPriority;
    }

    public long getScheduleLocalId()
    {
        return scheduleLocalId;
    }

    public void setAssociatedRule(String rule)
    {
        this.associatedRule = rule;
    }

    public String getAssociatedRule()
    {
        return associatedRule;
    }

    public long getSingleVideoRegId() {
        return singleVideoRegId;
    }

    public void setSingleVideoRegId(long singleVideoRegId) {
        this.singleVideoRegId = singleVideoRegId;
    }

    public int getSingleVideoRegPausedAt() {
        return singleVideoRegPausedAt;
    }

    public void setSingleVideoRegPausedAt(int singleVideoRegPausedAt) {
        this.singleVideoRegPausedAt = singleVideoRegPausedAt;
    }
}
