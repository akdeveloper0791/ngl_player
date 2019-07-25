package com.ibetter.www.adskitedigi.adskitedigi.bg_audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.FileObserver;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings.AudioSettingsConstants;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class BackGroundAudioHandler {

    //file observer
    public FileObserver newAudioListener;
    private Context context;
    private  File[] playFiles;
    private int playAudioPosition = 0;
    public String playingFileName;



    public BackGroundAudioHandler(Context context,boolean isRefreshFiles)
    {
        this.context = context;

       if(isRefreshFiles)
       {
           refreshFiles();
       }
    }


    public void refreshFiles()
    {
        playFiles = new AudioSettingsConstants().getBgAudioFiles(context);
    }

    //play audio
    public boolean playAudio(MediaPlayer mediaPlayer)
    {
        //get file to play
        if(playFiles.length>=1)
        {
           if(playAudioPosition<playFiles.length)
           {
               File playFile = playFiles[playAudioPosition];
               if(playFile.exists())
               {
                   //save playing file name
                   playingFileName = playFile.getName();

                   //start playing
                   try {
                       mediaPlayer.setDataSource(playFile.getAbsolutePath());
                       mediaPlayer.prepareAsync();
                       return true;
                   }catch (IOException exception)
                   {
                       Toast.makeText(context,context.getString(R.string.media_player_init_error) + exception.getMessage(),
                               Toast.LENGTH_SHORT).show();
                       return false;
                   }
               }else
               {

                   //play next audio
                  return playNextAudio(mediaPlayer);
               }
           }else
           {
               //refresh files list
                 refreshFiles();
              return restartPlayLoop(mediaPlayer);
           }
        }//else no audio files to play
        else
        {
            return false;
        }
    }

    //restart play loop
    private boolean restartPlayLoop(MediaPlayer mediaPlayer)
    {
        playAudioPosition = 0;
       return  playAudio(mediaPlayer);
    }

    //play next audio
    public boolean playNextAudio(MediaPlayer mediaPlayer)
    {
        //increment audio position and play
        playAudioPosition +=1;
        return playAudio(mediaPlayer);
    }

    //check and add new file
    public int checkAndAddNewFile(String newPath)
    {
        if(newPath!=null)
        {
            File newFile = new File(new AudioSettingsConstants().backgroundAudiosFolder(context)+File.separator+newPath);

            if(newFile.exists()) {
              int previousFilesSize = playFiles.length;
              int newLength = previousFilesSize + 1;
              playFiles = Arrays.copyOf(playFiles, newLength);
              playFiles[previousFilesSize] = newFile;
              return newLength;
            }else
             {
                return 0;//file adding failed
             }
        }else
        {
           return 0;//file adding failed
        }
    }

    public boolean isPlayingSong(String fileName)
    {
        return (fileName!=null && playingFileName!=null && fileName.equals(playingFileName));
    }

}
