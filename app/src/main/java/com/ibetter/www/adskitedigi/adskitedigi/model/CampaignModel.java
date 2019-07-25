package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class CampaignModel
{

    public static String getFileNameFromUrl(String urlPath)
    {
        return urlPath.substring(urlPath.lastIndexOf('/') + 1);
    }

    public static String getFileNameWithOutExt(String fileName)
    {
      return fileName.replace(".txt","");
    }

    public static String getAdsKiteNearByDirectory(Context context)
    {
        /* create path */
        File dir=null;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED))
        {
            //sd card is present
            dir =new File(( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getPath()+"/Nearby");

        }else
        {
            dir =new File(context.getFilesDir()+File.separator+Environment.DIRECTORY_DOWNLOADS+"/Nearby");
        }
        if (dir.exists())
        {
            return  dir.getPath();
        }
        else
        {
            boolean isDirectoryCreated =  dir.mkdirs();
            if (isDirectoryCreated)
            {
                return dir.getPath();
            }

            return  null;
        }

    }

}
