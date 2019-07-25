package com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model;

public class ContextualAdRule {
    private String name;
    private long serverId;
    private int delayDuration;

    public ContextualAdRule(String name,long serverId,int delayDuration)
    {
        this.name = name;
        this.serverId = serverId;
        this.delayDuration = delayDuration;
    }

    public String getName()
    {
        return name;
    }

    public long getServerId()
    {
        return serverId;
    }

    public int getDelayDuration()
    {
        return delayDuration;
    }
}
