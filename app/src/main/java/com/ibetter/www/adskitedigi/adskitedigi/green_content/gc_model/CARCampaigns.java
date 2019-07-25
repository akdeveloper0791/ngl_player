package com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model;

public class CARCampaigns {

    private String campaignName,ruleName;
    private long serverId;

    public CARCampaigns(String campaignName,String ruleName,long serverId)
    {
       this.campaignName = campaignName;
       this.ruleName = ruleName;
       this.serverId = serverId;
    }

    public String getCampaignName()
    {
        return campaignName;
    }

    public String getRuleName()
    {
        return ruleName;
    }

    public long getServerId()
    {
        return serverId;
    }
}
