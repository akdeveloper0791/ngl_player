package com.ibetter.www.adskitedigi.adskitedigi.model;

public class TickerTextModel {

    private long serverId;
    private int isSkip,scheduleType;
    private String info;

    public TickerTextModel(long serverId,int isSkip,String info,int scheduleType)
    {
        this.serverId=serverId;
        this.isSkip=isSkip;
        this.info = info;
        this.scheduleType = scheduleType;
    }

    public long getServerId()
    {
        return serverId;
    }

    public int getIsSkip()
    {
        return isSkip;
    }

    public String getInfo()
    {
        return info;
    }

    public int getScheduleType()
    {
        return scheduleType;
    }
}
