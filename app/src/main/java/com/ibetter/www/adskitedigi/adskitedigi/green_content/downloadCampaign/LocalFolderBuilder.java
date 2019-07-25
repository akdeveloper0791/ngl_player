


package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocalFolderBuilder {

    private String filePath;
    private long fromRage,toRange;
    private long size;
    private int responseCode;
    private Context context;
    private static final int INFO_TIME_OUT=30000;//30 sec

    private static final int TIME_OUT=120000;//2min

    public LocalFolderBuilder(String filePath,Context context)
    {
        this.filePath=filePath;
        this.context=context;
    }

    public void range(long from,long to)
    {
        this.fromRage=from;
        this.toRange=to;

    }

    public long getSize()
    {
        return size;
    }

    public boolean start() throws Exception
    {

        URL url = new URL(new User().getEnterPriseURL(context)+"media"+ Constants.replaceSpecialCharacters(filePath));

        HttpURLConnection  urlCon = (HttpURLConnection) url.openConnection();


        urlCon.setConnectTimeout(INFO_TIME_OUT);

        urlCon.setDefaultUseCaches( false );


        Log.i("URL Respnse Code: " ,""+ urlCon.getResponseCode());

        if(urlCon.getResponseCode()==200)
        {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                size = urlCon.getContentLengthLong();
            }else
            {
                size = urlCon.getContentLength();
            }

            Log.i("URL Content-Length: " ,""+ size);

            urlCon.disconnect();

            if(size>0)
            {

                return true;
            }
            return false;
        }else
        {
            return false;
        }


    }


    public void download(OutputStream fileOutPutStream, DownloadProgressFileInfo downloadProgressFileInfo, NotificationManager mNotificationManager, NotificationCompat.Builder downloadCampaignUploadProgressNotification) throws Exception
    {

        URL url = new URL(new User().getEnterPriseURL(context) + "media" + Constants.replaceSpecialCharacters(filePath));

        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();

        urlCon.setReadTimeout(60000);

        urlCon.setConnectTimeout(TIME_OUT);

        urlCon.setDefaultUseCaches(false);

        Log.i("from range",""+fromRage);
        Log.i("to range",""+toRange);
        //range
        urlCon.setRequestProperty("Range", "bytes=" + fromRage + "-"+toRange);


        responseCode=urlCon.getResponseCode();
        BufferedInputStream inputStream = new BufferedInputStream( urlCon.getInputStream(), 1024 * 8);

        long total = 0;
        byte data[] = new byte[1024 * 4];
        int count;

        while ((count = inputStream.read(data)) != -1) {
            total += count;

            fileOutPutStream.write(data, 0, count);

            downloadProgressFileInfo.setProgressListenerDownloadedBytes(total);

            downloadProgressFileInfo.updateOnProgress(downloadCampaignUploadProgressNotification, mNotificationManager);

            //Log.i("downlaodedbytes", "" + total);
           // Log.i("downlaodlr.size", "" + size);
        }
        inputStream.close();

        urlCon.disconnect();
    }


}
