package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model;
import java.io.Serializable;
import java.util.ArrayList;

public class GCModel implements Serializable
{
    private String campaignName,campaignFile;
    private String createdAt,updatedAt,savePath;
    private long serverId,campaignSize,campaignUploadedBy,campaignLocalId;
    private int storeLocation,campaignType,source,isSkip,scheduleType,campaignPriority;
    private String info;
    private ArrayList<String> regionList;
    private ProgressInfo progressInfo;

    public static final String INIT_DOWNLOAD="INIT_DOWNLOAD";
    public static final String DOWNLOAD_PROGRESS="DOWNLOAD_PROGRESS";
    public static final  String DOWNLOAD_ERROR="DOWNLOAD_ERROR";
    public static final  String REMOVE_PROGRESS="REMOVE_PROGRESS";
    public static final  String GET_DOWNLOADING_FILES="GET_DOWNLOADING_FILES";
    public static final  String REMOVE_ALL_PROGRESS="REMOVE_ALL_PROGRESS";

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public ArrayList<String> getRegionList() {
        return regionList;
    }

    public void setRegionList(ArrayList<String> regionList) {
        this.regionList = regionList;
    }

    public String getCampaignFile() {
        return campaignFile;
    }

    public void setCampaignFile(String campaignFile) {
        this.campaignFile = campaignFile;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(int storeLocation) {
        this.storeLocation = storeLocation;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ProgressInfo getProgressInfo() {
        return progressInfo;
    }

    public void setProgressInfo() {

        progressInfo=new ProgressInfo();
    }

    public void removeProgressInfo()
    {
        progressInfo=null;
    }

    public void initProgressInfo()
    {
        progressInfo=new ProgressInfo();
        progressInfo.status = INIT_DOWNLOAD;

    }

    public void updateDownloadProgress(int progress,int position,int totalFiles,String resourceName)
    {
        if(progressInfo!=null)
        {
            progressInfo.status = DOWNLOAD_PROGRESS;
            progressInfo.errorMsg = null;
            progressInfo.progress = progress;
            progressInfo.totalFiles=totalFiles;
            progressInfo.resourceName=resourceName;
            progressInfo.position=position;
        }
    }

    public void updateDownloadErrorProgressInfo(String errorMsg)
    {
        if(progressInfo!=null)
        {
            progressInfo.status = DOWNLOAD_ERROR;
            progressInfo.errorMsg=errorMsg;



        }

    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public long getCampaignUploadedBy() {
        return campaignUploadedBy;
    }

    public void setCampaignUploadedBy(long campaignUploadedBy) {
        this.campaignUploadedBy = campaignUploadedBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCampaignSize() {
        return campaignSize;
    }

    public void setCampaignSize(Long campaignSize) {
        this.campaignSize = campaignSize;
    }

    public int getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(int campaignType) {
        this.campaignType = campaignType;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getIsSkip() {
        return isSkip;
    }

    public void setIsSkip(int isSkip) {
        this.isSkip = isSkip;
    }


    public class ProgressInfo
    {

        int progress,position,totalFiles;
        String errorMsg,status,resourceName;

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public ProgressInfo() {

        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getTotalFiles() {
            return totalFiles;
        }

        public void setTotalFiles(int totalFiles) {
            this.totalFiles = totalFiles;
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }
    }

    public void setSavePath(String savePath)
    {
        this.savePath = savePath;
    }

    public String getSavePath()
    {
        return savePath;
    }

    public void setScheduleType(int scheduleType)
    {
        this.scheduleType = scheduleType;
    }

    public int getScheduleType()
    {
        return scheduleType;
    }

    public void setCampaignLocalId(long campaignLocalId)
    {
        this.campaignLocalId = campaignLocalId;
    }

    public long getCampaignLocalId()
    {
        return campaignLocalId;
    }

    public void setCampaignPriority(int campaignPriority)
    {
        this.campaignPriority = campaignPriority;
    }

    public int getCampaignPriority()
    {
        return campaignPriority;
    }
}