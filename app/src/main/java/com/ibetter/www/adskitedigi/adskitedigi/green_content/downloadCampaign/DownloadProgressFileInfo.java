package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.DownloadMediaInfo;
import com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService.DOWNLOAD_CAMPAIGNS_PATH;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService.UPDATE_PROGRESS_RX;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.DOWNLOAD_PROGRESS;


public class DownloadProgressFileInfo {
    public   String currentDownloadingResourceFileName;
    private String campaignName,mediaDownloadPath;
    private Context context;
    public long size=0;
    public long downloadedBytes = 0;
    public int totalFiles=0,storeLocation=2;
    public long progressListenerDownloadedBytes;


    public long updateOnProgress =0;

    public int currentDownloadingResourceFilePosition =0;


    public String dummyFileName;
    public OutputStream fileOutPutStream;

    public long downloadProgressEndTime=0,downloadProgressStartTime=0;

    public static final long MIN_PROGRESS_LISTENER_DOWNLOAD_BYTES = 512*1024; //512 kb ,, half a mb

    public boolean isResourceExists()
    {
        return false;
       // return new File(DOWNLOAD_CAMPAIGNS_PATH,currentDownloadingResourceFileName).exists();
    }
    public void setCurrentDownloadingResourceFileName(String currentDownloadingResourceFileName) throws FileNotFoundException
    {
        this.currentDownloadingResourceFileName = currentDownloadingResourceFileName;
        //init output stream
        File path = new File(DOWNLOAD_CAMPAIGNS_PATH);
        this.dummyFileName=String.valueOf(Calendar.getInstance().getTimeInMillis());
        File file = new File(path, dummyFileName);

        // Make sure the Downloads directory exists.
        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw  new RuntimeException("Unable to create directory: " + path);
            }
        }
        else if (!path.isDirectory())
        {
            throw  new IllegalStateException("Download path is not a directory: " + path);
        }

        // Download the file.\
        fileOutPutStream = new FileOutputStream(file);

    }

    public void setSize(long size) {
        this.size = size;
    }

    public void onChunkDownloadSuccess()
    {
        //update downloaded bytes
        this.downloadProgressEndTime = System.currentTimeMillis();
        this.downloadedBytes +=progressListenerDownloadedBytes;

    }

    public int getCurrentUploadedProgress()
    {
        if(size>0)
        {
            long totalBytesUploaded = (downloadedBytes+progressListenerDownloadedBytes);

            return (int) ((totalBytesUploaded * 100) / size);
        }else
        {
            return 0;
        }

    }

    public void updateOnProgress(NotificationCompat.Builder builder, NotificationManager notificationManager)
    {
        if(updateOnProgress==0 || (System.currentTimeMillis() - updateOnProgress) >= 1000)
        {
            updateOnProgress = System.currentTimeMillis();

            builder.setProgress(100,getCurrentUploadedProgress(),false);
            notificationManager.notify(NotificationModelConstants.DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_NOTIFY_ID,
                    builder.build());

            sendUpdateDownloadInfo();
            }

    }
    public void setDownloadProgressInitTime(long uploadProgressInitTime)
    {
        this.downloadProgressStartTime = uploadProgressInitTime;
    }



    public void setProgressListenerDownloadedBytes(long progressListenerDownloadedBytes)
    {
        this.progressListenerDownloadedBytes = progressListenerDownloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes)
    {
        this.downloadedBytes = downloadedBytes;
    }

    public long getNextChunkToUpload()
    {
        //calculate the time taken to transfer previous chunk
        int timeTakenInSec = (int)(((downloadProgressEndTime - downloadProgressStartTime) / 1000));
        if(timeTakenInSec>=60 && timeTakenInSec<=100)
        {
            //no change return the previous bytes
            return progressListenerDownloadedBytes;
        }else
        {
            //taking more time so calculate the next chunk based on current transfer
            long nextChunk =  (progressListenerDownloadedBytes/timeTakenInSec) * 90;//90 is the seconds
            return ((nextChunk < 90*1024*1024)?nextChunk:90*1024*1024);
        }

    }

    //on downloading error
    public void onChunkDownloadError()
    { Log.i("downloadedBytes before",""+downloadedBytes);
        //update downloaded bytes
        downloadedBytes += progressListenerDownloadedBytes;

        Log.i("downloadedBytes",""+downloadedBytes);
    }

    public void reInitOutputStream() throws FileNotFoundException
    {
        //init output stream
        File path = new File(DOWNLOAD_CAMPAIGNS_PATH);
        File file = new File(path, currentDownloadingResourceFileName);

        // Make sure the Downloads directory exists.
        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw  new RuntimeException("Unable to create directory: " + path);
            }
        } else if (!path.isDirectory()) {
            throw  new IllegalStateException("Download path is not a directory: " + path);

        }

        // Download the file.\
        fileOutPutStream = new FileOutputStream(file);
    }


    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private  void sendUpdateDownloadInfo()
    {
            //name
            try {


                Intent intent = new Intent(UPDATE_PROGRESS_RX);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("action",DOWNLOAD_PROGRESS);
                intent.putExtra("progress",getCurrentUploadedProgress());
                intent.putExtra("name", campaignName);
                intent.putExtra("resource_name", currentDownloadingResourceFileName);
                intent.putExtra("total_files", totalFiles);
                intent.putExtra("position", currentDownloadingResourceFilePosition);
                context.sendBroadcast(intent);
            }catch (Exception E)
            {
                E.printStackTrace();
            }


    }

    public int getCurrentDownloadingResourceFilePosition() {
        return currentDownloadingResourceFilePosition;
    }

    public void setCurrentDownloadingResourceFilePosition(int currentDownloadingResourceFilePosition) {
        this.currentDownloadingResourceFilePosition = currentDownloadingResourceFilePosition;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public  void  renameDummyFile()
    {

        try {
            File path = new File(DOWNLOAD_CAMPAIGNS_PATH);
            File dummyFile = new File(path, dummyFileName);
            File resourceFile = new File(path, currentDownloadingResourceFileName);

            if (dummyFile.exists()) {
                if(dummyFile.renameTo(resourceFile)) {


                    if (dummyFile.exists()) {
                        Log.i("deleted", "" + dummyFile.delete());
                    } else {
                        Log.i("deleted", "dummy file not exost");
                    }



                }
            }

        }catch (Exception E)
        {
            E.printStackTrace();
        }
    }

    public static void deleteGarbageFiles()
    {
        File path = new File(DOWNLOAD_CAMPAIGNS_PATH);
        File[] files = path.listFiles(new FileFilter() {
            @Override

            public boolean accept(File pathname) {
                String s = pathname.getName();
                s = s.toLowerCase();
                return (!(s.contains(".")));

            }
        });
        for (File file:files)
        {
            file.delete();
        }
    }

    public void setDownloadMediaInfo(DownloadMediaInfo mediaInfo)
    {
        this.storeLocation = mediaInfo.getStoreLocation();
        this.mediaDownloadPath = mediaInfo.getPath();

    }

    public String getMediaDownloadPath()
    {
        return mediaDownloadPath;
    }

    public int getStoreLocation()
    {
        return storeLocation;
    }

}
