package com.ibetter.www.adskitedigi.adskitedigi.nearby;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.google.android.gms.nearby.connection.Strategy;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by vineethkumar0791 on 28/03/18.
 */

public class ConnectingNearBySMMOdel implements Serializable
{

    public static final Strategy STRATEGY = Strategy.P2P_STAR;


    private static final String[] REQUIRED_PERMISSIONS =

            new String[]
            {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };

    public static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    public static  String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    public void  stopDiscoveringSMService()
    {
        try
        {
            Service service =  SignageServe.signageServeObject.getSmService();

            if(service!=null)
            {
                service.stopSelf();
                System.out.println("Stopping service - " + service);
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void  startDiscoveringSMService(Context context)
    {
        try
        {
            context.startService(new Intent(context,ConnectingNearBySMService.class));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String saveCampaignImagesTo(Context context,String fileName)
    {

        String playingDir= new User().getUserPlayingFolderModePath(context);

        File dir = new File(playingDir , context.getString(R.string.campaign_images_dir));

        if (!dir.exists()) {
            dir.mkdirs();

        }

        if(fileName==null)
        {
            fileName = Calendar.getInstance().getTimeInMillis() + ".mp3";
        }
        return  (dir.getAbsolutePath() +File.separator+fileName );
    }

    public static File[] getMediaFiles(final Context context)
    {


        String dir=  ( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getPath()+"/Nearby";


        if(dir!=null)
        {
            File dirFile=new File(dir);

            if(dirFile.exists()) {
                File[] files = dirFile.listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File file, String s) {
                                s = s.toLowerCase();

                                if ((  s.endsWith(context.getString(R.string.media_video_wmv)) ||
                                        s.endsWith(context.getString(R.string.media_video_avi)) ||
                                        s.endsWith(context.getString(R.string.media_video_mpg)) ||
                                        s.endsWith(context.getString(R.string.media_video_mpeg)) ||
                                        s.endsWith(context.getString(R.string.media_video_webm)) ||
                                        s.endsWith(context.getString(R.string.media_video_mp4))||
                                        s.endsWith(context.getString(R.string.media_video_3gp))||
                                        s.endsWith(context.getString(R.string.media_video_mkv))||
                                        s.endsWith(context.getString(R.string.media_image_jpg)) ||
                                        s.endsWith(context.getString(R.string.media_image_jpeg)) ||
                                        s.endsWith(context.getString(R.string.media_image_png)) ||
                                        s.endsWith(context.getString(R.string.media_image_bmp)) ||
                                        s.endsWith(context.getString(R.string.media_image_gif))||
                                        s.endsWith(context.getString(R.string.media_txt))
                                ) && !s.startsWith(context.getString(R.string.do_not_display_media))
                                        )
                                {
                                    return true;
                                }
                                else
                                {
                                    return false;
                                }
                            }

                        }

                );


                //ascending order
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {

                        return file2.lastModified() > file1.lastModified() ? -1 : (file2.lastModified() < file1.lastModified()) ? 1 : 0;
                    }
                });


                return files;
            }else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }



}
