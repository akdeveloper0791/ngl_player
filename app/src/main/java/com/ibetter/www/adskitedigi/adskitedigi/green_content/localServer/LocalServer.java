package com.ibetter.www.adskitedigi.adskitedigi.green_content.localServer;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadProgressFileInfo;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocalServer
{


    public long size;

    private static final int TIME_OUT=120000;//2min

    public void downloadFile(Context context,String savePath, String campaignName,OutputStream fileOutPutStream, DownloadProgressFileInfo downloadProgressFileInfo, NotificationManager mNotificationManager, NotificationCompat.Builder downloadCampaignUploadProgressNotification) throws Exception {
        URL url = new URL(new User().getEnterPriseURL(context) + "media" + Constants.replaceSpecialCharacters(savePath + campaignName));

        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();

        urlCon.setReadTimeout(60000);

        urlCon.setConnectTimeout(TIME_OUT);

        urlCon.setDefaultUseCaches(false);


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            size = urlCon.getContentLengthLong();
        }else
        {
            size = urlCon.getContentLength();
        }

         downloadProgressFileInfo.setSize(size);

            InputStream inputStream = urlCon.getInputStream();

            long total = 0;
            byte data[] = new byte[1024 * 4];
            int count;

            while ((count = inputStream.read(data)) != -1) {
                total += count;

                fileOutPutStream.write(data, 0, count);

                downloadProgressFileInfo.setProgressListenerDownloadedBytes(total);

                downloadProgressFileInfo.updateOnProgress(downloadCampaignUploadProgressNotification, mNotificationManager);

                Log.i("downlaodedbytes", "" + total);
                Log.i("downlaodlr.size", "" + size);
            }

            fileOutPutStream.close();

            inputStream.close();

            urlCon.disconnect();


    }
}
