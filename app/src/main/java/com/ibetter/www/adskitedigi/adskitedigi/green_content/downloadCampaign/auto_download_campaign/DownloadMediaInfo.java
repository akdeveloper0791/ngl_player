package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import java.io.File;

public class DownloadMediaInfo {

    private int storeLocation=2;//default is drop box
    private String path;//complete path
    private String mediaName;

    public DownloadMediaInfo(int storeLocation,String path)
    {
        this.storeLocation = storeLocation;
        this.path = path;
        String pathArray[] = path.split(File.separator);
        mediaName = pathArray[pathArray.length-1];

    }

    public int getStoreLocation()
    {
        return storeLocation;
    }

    public String getPath()
    {
        return path;
    }

    public String getMediaName()
    {
        return mediaName;
    }
}
