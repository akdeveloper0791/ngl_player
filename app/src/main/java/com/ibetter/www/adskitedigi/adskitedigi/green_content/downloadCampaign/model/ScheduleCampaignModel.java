package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ScheduleCampaignModel {
    private long scServerId,campaignServerId;
    private String scheduleFrom,scheduleTo,additionalInfo;
    private final static String SERVER_SCHEDULE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public final static String LOCAL_SCHEDULE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String LOCAL_SCHEDULE_TIME_FORMAT = "HH:mm:ss.SSS";
    public final static String LOCAL_SCHEDULE_ONLY_DATE_FORMAT = "yyyy-MM-dd";
    private int scheduleType,scPriority;
    public final static int CONTINUOUS_PLAY = 10;
    public final static int SCHEDULE_CONTINUOUS_PLAY = 100;
    public final static int SCHEDULE_MINUTE_PLAY = 110;
    public final static int SCHEDULE_HOUR_PLAY = 120;
    public final static int SCHEDULE_DAILY_PLAY = 200;
    public final static int SCHEDULE_MONTHLY_PLAY = 250;
    public final static int SCHEDULE_YEARLY_PLAY = 300;
    public final static int SCHEDULE_WEEKLY_PLAY = 350;


    public ScheduleCampaignModel(long scServerId,long campaignServerId,String scheduleFrom,String scheduleTo,int scheduleType,
                                 String additionalInfo)
    {
        this.scServerId = scServerId;
        this.campaignServerId = campaignServerId;
        this.scheduleType = scheduleType;
        this.additionalInfo = additionalInfo;

        SimpleDateFormat serverSDF = new SimpleDateFormat(SERVER_SCHEDULE_DATE_FORMAT, Locale.getDefault());
        serverSDF.setTimeZone(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat localSDF = new SimpleDateFormat(LOCAL_SCHEDULE_DATE_FORMAT);

        try
        {
            Date scheduleToDate = serverSDF.parse(scheduleTo);
            if(scheduleToDate.compareTo(Calendar.getInstance().getTime())>=1)
            {
                this.scheduleFrom = localSDF.format(serverSDF.parse(scheduleFrom));
                this.scheduleTo = localSDF.format(scheduleToDate);

            }
            // Log.d("auto download campaigns","after parsing schedule from "+this.scheduleFrom+", scheduleTo "+this.scheduleTo);
        }catch(ParseException exception)
        {

        }

    }

    public long getScServerId()
    {
        return scServerId;
    }

    public long getCampaignServerId()
    {
        return campaignServerId;
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

    public void setScPriority(int scPriority)
    {
        this.scPriority = scPriority;
    }

    public int getScPriority()
    {
        return scPriority;
    }

    public String getAdditionalInfo()
    {
        return additionalInfo;
    }
}
