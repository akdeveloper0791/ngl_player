package com.ibetter.www.adskitedigi.adskitedigi.download_media;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ibetter-Dell on 18-11-16.
 */

public class DownloadMediaHelper {

    public String mergeVideoFiles(Context context, ArrayList<File> files, String media)
    {

        /* create path */
        File dir;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED))
        {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard+ "/"+context.getString(R.string.adskite_digi_directory));
        }
        else {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir()+"/"+context.getString(R.string.adskite_digi_directory));
        }


        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outputFile = new File(dir+"/"+ Calendar.getInstance().getTimeInMillis()+"_"+media);


        if(outputFile.exists())
        {
            outputFile.delete();
        }

        FileOutputStream fos;
        FileInputStream fis;

        byte[] fileBytes;
        int bytesRead = 0;

        try {

            fos = new FileOutputStream(outputFile, true);

            for (File file : files) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0, (int) file.length());
                assert (bytesRead == fileBytes.length);
                assert (bytesRead == (int) file.length());
                fos.write(fileBytes);
                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            long length = outputFile.length();
            length = length/(1024*1024);
            Log.i("merfilesize","::::MB"+length);

            fos.close();
            fos = null;

            deleteFilesFromDirectory(files);

            return outputFile.getPath();

        }
        catch (Exception e)
        {
            return null;
        }
    }

    public File saveFileToDirectory(Context context,String fileName,byte[] data)
    {
         /* create path */
        File dir;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard+ "/"+context.getString(R.string.adskite_digi_directory));
        }
        else {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir()+"/"+context.getString(R.string.adskite_digi_directory));
        }


        if (!dir.exists())
        {
           boolean isDirectoryCreated =  dir.mkdirs();
            System.out.println("is directry created:"+isDirectoryCreated);
        }else
        {
            System.out.println("Folder already exists Adskite DIgi");
        }

        File outputFile = new File(dir+"/"+fileName);
        if(outputFile.exists())
        {
            outputFile.delete();
        }


        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            out.write(data);
            out.flush();
            out.close();

        }catch (Exception E)
        {

        }
        return outputFile;



    }

    //delete files form directory
    private   void deleteFilesFromDirectory(ArrayList<File> files)
    {
        for(File file:files) {
            Log.i("file","indide"+file.getPath());
            if (file.exists()) {
                Log.i("file","exist"+file.getPath());
                if (file.delete()) {
                    Log.i("file","delted successfully"+file.getPath());
                } else {
                    Log.i("file","unable to delte file"+file.getPath());
                }
            }
        }
    }

    public String getAdsKiteDirectory(Context context)
    {
          /* create path */
        File dir;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard+ "/"+context.getString(R.string.adskite_digi_directory));
        }
        else
        {
            //save to internal memory(phone memory)
           dir = new File(context.getFilesDir()+"/"+context.getString(R.string.adskite_digi_directory));
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


    public String getAdsKiteNearByDirectory(Context context)
    {

        /* create path */
        File dir=null;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            dir =new File(( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getPath()+"/Nearby");

        }else
        {
            dir =new File(context.getFilesDir()+File.separator+Environment.DIRECTORY_DOWNLOADS);
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

    public String getCaptureImagesDirectory(Context context)
    {

        // create path /
        File dir=null;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard+ "/"+context.getString(R.string.adskite_digi_directory)+"/CAPTURE");
        }
        else
        {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir()+"/"+context.getString(R.string.adskite_digi_directory)+"/CAPTURE");
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
