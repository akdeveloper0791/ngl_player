package com.ibetter.www.adskitedigi.adskitedigi.ftp;

public class FTPFileObserverModel {

    private long fileDownloadStartTime,fileDownloadFinishTime;

    public void setFileDownloadStartTime(long fileDownloadStartTime)
    {
        this.fileDownloadStartTime = fileDownloadStartTime;
    }

    public long getFileDownloadStartTime()
    {
        return fileDownloadStartTime;
    }

    public void setFileDownloadFinishTime(long fileDownloadFinishTime)
    {
        this.fileDownloadFinishTime = fileDownloadFinishTime;
    }

    public long getFileDownloadFinishTime()
    {
        return fileDownloadFinishTime;
    }
}
