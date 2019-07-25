package com.ibetter.www.adskitedigi.adskitedigi.logs;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DateTimeModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.MediaModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//display logs   in background
public class DisplayDebugLogs extends AsyncTask<String, Void, Void>
{

    private Context context;
    private String  DEBUG_TAG="SS Debug: ";

    public DisplayDebugLogs(Context context) {
        this.context=context;
    }

    protected Void doInBackground(String... params)
    {

        saveLog(new DateTimeModel().getDate(new SimpleDateFormat(context.getString(R.string.schedule_layout_expiry_date_format)),Calendar.getInstance().getTimeInMillis())+" :: "+DEBUG_TAG+params[0]+"\n\n");


        return null;
    }

    private  void  saveLog(String log)
    {

        String filePath;

        long currentTime= Calendar.getInstance().getTimeInMillis();

        String currentTimeString=new DateTimeModel().getDate(new SimpleDateFormat(context.getString(R.string.date_format_hours)),currentTime);


        filePath=getLogDirectory()+"/"+currentTimeString+".txt";


        if(new File(filePath).exists())
        {
            new MediaModel().appendingTextToFile(log,filePath);
        }
        else
        {
            saveLogTextToFile(currentTimeString,log,context);
        }

    }

    private String getLogDirectory()
    {
        /* create path */
        File dir;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED))
        {
            //sd card is present
            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard+ "/"+context.getString(R.string.signage_serv_logs_directory_name));

        }
        else
        {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir()+"/"+context.getString(R.string.signage_serv_logs_directory_name));

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

    public Uri saveLogTextToFile(String lastLogTime, String data, Context context)
    {

        File dir;
        String state = Environment.getExternalStorageState();

        // Get the directory for the user's public pictures directory.
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard + "/" + context.getString(R.string.signage_serv_logs_directory_name) + "/");
        } else {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir() + "/" + context.getString(R.string.signage_serv_logs_directory_name) + "/");
        }

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            dir.mkdirs();
        }

        final File file = new File(dir, lastLogTime + ".txt");

        // Save your stream, don't forget to flush() it before closing it.

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();

            return Uri.fromFile(file);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());

            return null;
        }
    }

}
