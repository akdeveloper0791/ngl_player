package com.ibetter.www.adskitedigi.adskitedigi.settings.overlay_image_settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OverlayImageSettingsModel
{
    private  static final  int DEFAULT_WIDTH=10;
    private  static final  int DEFAULT_HEIGHT=10;

    private static  final  String DEFAULT_POSITION="RIGHT_TOP";

    private static  final  boolean DEFAULT_SETTINGS_STATUS=false;

    public static  final  String RIGHT_TOP="Right-Top";
    public static  final  String RIGHT_BOTTOM="Right-Bottom";
    public static  final  String LEFT_TOP="Left-Top";
    public static  final  String LEFT_BOTTOM="Left-Bottom";


    protected String getOverlayDefaultSettings()
    {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("width", DEFAULT_WIDTH);
            jsonObject.put("height", DEFAULT_HEIGHT);
            jsonObject.put("position", RIGHT_TOP);

            return jsonObject.toString();

        }catch (Exception e)
        {
            return null;
        }

    }

    protected void setOverlayImageSettingsStatus(Context context,boolean status)
    {
        SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.is_overlaying_image_on),status);
        editor.commit();


    }

    public boolean getOverlayImageSettingStatus(Context context)
    {  SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(context);

         return sp.getBoolean(context.getString(R.string.is_overlaying_image_on),DEFAULT_SETTINGS_STATUS);
    }


    public   String getOverlayingImageSettingsInfo(Context context)
    {
        SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(context);


       return sp.getString(context.getString(R.string.overlaying_image_setting_info),getOverlayDefaultSettings());


    }



    protected void storeOverlayImageAndSettingsInfo(Context context,String settingsInfo,String imagePath)
    {

        SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.overlaying_image_setting_info),settingsInfo);
        editor.commit();


        new CopingFile(context,imagePath).execute();

    }

    public String getOverlayingImagePath(Context context)
    {
        File dir;
        String state = Environment.getExternalStorageState();


        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard + "/" + context.getString(R.string.adskite_digi_directory)+"/.OverlayImage");
        } else {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir() + "/" + context.getString(R.string.adskite_digi_directory)+"/.OverlayImage");
        }

        File file=  new File(dir + "/" +"overlay_image.png");

        if(file.exists())
        {
            return file.toString();

        }else
        {
           return  null;
        }

    }


    class CopingFile extends AsyncTask<Void,Long,Boolean>
    {
        ProgressDialog progress;
        File targetLocation;
        Context context;
        String filePath;

        public CopingFile(Context context, String filePath)
        {
            Log.i("info filepath","filePath");

            this.context = context;
            this.filePath = filePath;
        }


        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(context,"","Please Wait...",true);

            progress.setCancelable(false);
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {

                    if(targetLocation.exists())
                    {
                        targetLocation.delete();
                    }
                    dialogInterface.dismiss();

                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... values)
        {
            FileInputStream inStream = null;
            OutputStream outStream = null;
            Log.i("info filepath",filePath+"file name");
            //
            // Check permission has been granted
            //
            File dir;
            String state = Environment.getExternalStorageState();

            if (state.equals(Environment.MEDIA_MOUNTED)) {
                //sd card is present

                File sdCard = Environment.getExternalStorageDirectory();
                dir = new File(sdCard + "/" + context.getString(R.string.adskite_digi_directory)+"/.OverlayImage");
            } else {
                //save to internal memory(phone memory)
                dir = new File(context.getFilesDir() + "/" + context.getString(R.string.adskite_digi_directory)+"/.OverlayImage");
            }



            if (!dir.exists())
            {
                boolean isDirectoryCreated = dir.mkdirs();
                System.out.println("is directry created:" + isDirectoryCreated);
            }
            else
            {
                System.out.println("Folder already exists Adskite DIgi");
            }

            Log.i("", "Directory location: " + dir.toString());

            // Copy the file to the AppData folder
            // File name remains the same as the source file name

            File sourceLocation = new File(filePath);


            targetLocation = new File(dir + "/" +"overlay_image.png");
            if(filePath.equalsIgnoreCase(targetLocation.toString()))
            {
                return true;
            }
            try
            {
                inStream = new FileInputStream(sourceLocation);
                outStream = new FileOutputStream(targetLocation);

                int copingChunkSize=30000;
                int copingBytes=0;

                byte[] buffer = new byte[copingChunkSize];
                int bytesRead;

                while ((bytesRead = inStream.read(buffer)) != -1)
                {

                    outStream.write(buffer, 0, bytesRead);

                    copingBytes=copingBytes+copingChunkSize;

                    long per=(long) (((double)copingBytes/sourceLocation.length())*100);

                    Log.i("coping status","sosourceLocation"+sourceLocation.length()+"\n"+
                            "copingBytes"+copingBytes+"\n"+((double)copingBytes/sourceLocation.length())*100);

                    publishProgress(per);

                }

                inStream.close();

                outStream.close();

                return true;

            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return false;
            } catch (IOException e)
            {
                e.printStackTrace();

                return false;
            }
            finally
            {
                Log.d("", "Target location: " + targetLocation.toString());

            }

        }

        @Override
        protected void onPostExecute(Boolean resultPath) {
            progress.dismiss();
            // Show dialog with result
            if(resultPath)
            {
               Toast.makeText(context,"Saved Successfully",Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(context,"Something went to wrong",Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        protected void onProgressUpdate(Long... values) {
            Log.i("info","Coping " + values[0] + "/ 100");
            progress.setMessage("Coping " + values[0] + "/ 100");
        }

    }

}
