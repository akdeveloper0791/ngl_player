package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.logs.DisplayDebugLogs;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.CheckAndRestartSMServiceOreo;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.service_receivers.DeleteResponseFileTimerClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;

public class EnterPriseSettingsModel {

    public static void startEnterPriseModel(Context context)
    {

        try {
            SignageMgrAccessModel.setSignageMgrAccessStatus(true, context, context.getString(R.string.sm_access_enterprise_mode));

            startEnterPriseService(context);
        }catch (Exception E)
        {
            E.printStackTrace();
        }

    }

    public static String createStorageSpace(Context context)
    {
        File dir ;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            dir =new File(( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))+File.separator+
                    context.getString(R.string.enterprise_mode_files_dir));

        }else
        {
            dir =new File(context.getFilesDir()+File.separator+Environment.DIRECTORY_DOWNLOADS+File.separator+
                    context.getString(R.string.enterprise_mode_files_dir));
        }

        if (dir.exists())
        {
            User.setUserPlayingFolderModePath(context,dir.getPath());
            return  dir.getPath();
        }

        //create new directory
        if(dir.mkdirs())
        {
            User.setUserPlayingFolderModePath(context,dir.getPath());
            return dir.getPath();
        }else
        {
            return null;
        }
    }

    //start enterprise service
    private static void startEnterPriseService(Context context)
    {
        try {
            ContextCompat.startForegroundService(context, new Intent(context, EnterPriseSettingsService.class) );

             }catch (Exception e)
        {
            e.printStackTrace();
          //  new DisplayDebugLogs(context).execute("info"+e.getMessage());

        }
    }

    //switch off enter prise mode settings
    public static void switchOffEnterPriseSettings(Context context)
    {
        SignageMgrAccessModel.setSignageMgrAccessStatus(false, context,context.getString(R.string.sm_access_enterprise_mode));
        stopEnterPriseService();
    }

    public static void stopEnterPriseService()
    {
        SignageServe.signageServeObject.stopRunningService(EnterPriseSettingsService.class.getName());
    }


    public static void saveSMFTPResponse(Context context,String response,String saveResponseTo)
    {
        File dir = new File(new User().getUserPlayingFolderModePath(context));


        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            dir.mkdirs();
        }

        if(dir.exists())
        {

            final File file = new File(dir, saveResponseTo);

            // Save your stream, don't forget to flush() it before closing it.

            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(response);

                myOutWriter.close();

                fOut.flush();
                fOut.close();

                startDeleteResponseFile(context,file.getName());

            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
                displayToast(context,"Error in processing modify, File write failed: " + e.toString());
            }

        }else
        {
            displayToast(context,"Error in processing modify, no folder found");
        }

    }

    //display toast
    private static void displayToast(Context context,String msg)
    {
        if(context!=null)
        {
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
        }
    }

    private static void startDeleteResponseFile(Context context,String responseFileName)
    {
        new Timer().schedule(new DeleteResponseFileTimerClass(context,responseFileName),60000);
    }

}
