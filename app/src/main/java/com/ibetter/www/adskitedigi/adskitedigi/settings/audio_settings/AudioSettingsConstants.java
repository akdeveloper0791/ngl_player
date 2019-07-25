package com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class AudioSettingsConstants {
    protected  static final boolean DEFAULT_PLAY_AUDIO_VAL = false;


    public String backgroundAudiosFolder(Context context)
    {
        File dir = new File(context.getFilesDir() , context.getString(R.string.bg_audio_settings_dir));

        if (!dir.exists()) {
            boolean isDirectoryCreated =  dir.mkdirs();

            Log.d("info", "inside save background audio directory created - "+isDirectoryCreated);
        }

        return dir.getAbsolutePath();
    }

    public boolean isOfferMediaExists(Context context)
    {

        File[] audioFiles = getBgAudioFiles(context);
        return (audioFiles!=null && audioFiles.length>=1);
    }


    //get play offer audio settings
    public boolean getPlayOfferAudioSettings(Context context)
    {
       return context.getSharedPreferences(context.getString(R.string.settings_sp),context.MODE_PRIVATE).
               getBoolean(context.getString(R.string.play_audio_sp),DEFAULT_PLAY_AUDIO_VAL);
    }


    public File[] getBgAudioFiles(final Context context)
    {

        String dir= backgroundAudiosFolder(context);

        if(dir!=null)
        {
            File dirFile=new File(dir);

           File[] files =   dirFile.listFiles(
                    new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s)
                        {

                            s=s.toLowerCase();

                            if((s.endsWith(context.getString(R.string.media_video_3gp))||
                                s.endsWith(context.getString(R.string.media_video_mp4))||
                                s.endsWith(context.getString(R.string.media_audio_m4a))||
                                s.endsWith(context.getString(R.string.media_audio_ts))||
                                s.endsWith(context.getString(R.string.media_audio_flac))||
                                s.endsWith(context.getString(R.string.media_video_mkv))||
                                s.endsWith(context.getString(R.string.media_audio_wav))||
                                s.endsWith(context.getString(R.string.media_audio_ogg))||
                                s.endsWith(context.getString(R.string.media_audio_xmf))||
                                s.endsWith(context.getString(R.string.media_audio_ota))||
                                s.endsWith(context.getString(R.string.media_audio_mp3))
                            ) )
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
        }
        else
        {
            return null;
        }
    }


   public JSONObject getAudioSettingsSMRequest(Context context) throws JSONException
   {
       JSONObject obj = new JSONObject();

       obj.put(context.getString(R.string.play_audio), getPlayOfferAudioSettings(context));

       File[] files = getBgAudioFiles(context);

       if (files != null)
       {
           JSONArray mJSONArray = new JSONArray(Arrays.asList(files));
           obj.put("files", mJSONArray);
       }

       return obj;
   }

   public static void updateBgAudioSettings(Context context,boolean isOn)
   {
       SharedPreferences saveSP = context.getSharedPreferences(context.getString(R.string.settings_sp),context.MODE_PRIVATE);
       SharedPreferences.Editor saveSPEditor = saveSP.edit();

       saveSPEditor.putBoolean(context.getString(R.string.play_audio_sp),isOn);
       saveSPEditor.commit();


       Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
       intent.addCategory(Intent.CATEGORY_DEFAULT);
       intent.putExtra(context.getString(R.string.action),context.getString(R.string.update_offer_audio_action));

       context.sendBroadcast(intent);
   }
}
