package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.announcement;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.AudioBuffer;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.os.Process.THREAD_PRIORITY_AUDIO;
import static android.os.Process.setThreadPriority;

public class PlayAnnouncement extends AppCompatActivity {

    private Context context;
    private static ArrayList<String> pendingAnnouncements = new ArrayList<>();

    /** The audio stream we're reading from. */
    private InputStream mInputStream;

    /**
     * If true, the background thread will continue to loop and play audio. Once false, the thread
     * will shut down.
     */
    public static  boolean mAlive;

    /** The background thread recording audio for us. */
    private Thread mThread;

    public PlayAnnouncement() {
    }

    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.play_announcement_layout);

        context = PlayAnnouncement.this;

        String announcementFile = getIntent().getStringExtra("announcement_file");
        playAnnouncementFile(announcementFile);
    }

    private void playAnnouncementFile(String announcementFileName)
    {
        mAlive = true;

        try {

            String parentFolder = new User().getUserPlayingFolderModePath(context);
            File announcementFile = new File(parentFolder + File.separator + announcementFileName);

            if (announcementFile.exists()) {
                mInputStream = new FileInputStream(announcementFile);

                //start thread to play announcement
                 mThread = new Thread(new PlayAudioThread());
                 mThread.start();

            }else
            {
                onFinish();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            onFinish();
        }


    }


    private class PlayAudioThread extends Thread
    {
        @Override
        public void run() {


            setThreadPriority(THREAD_PRIORITY_AUDIO);

            Buffer buffer = new Buffer();
            AudioTrack audioTrack =
                    new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            buffer.sampleRate,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            buffer.size,
                            AudioTrack.MODE_STREAM);
            audioTrack.play();

            int len;
            try {
                while ((len = mInputStream.read(buffer.data)) > 0) {
                    Log.d("Play audio","Inside check and play next file , Inside while");
                    audioTrack.write(buffer.data, 0, len);
                }
            } catch (IOException e) {
                Log.e("Play audio", "Exception with playing stream", e);
            } finally {
                stopInternal();
                audioTrack.release();
                onFinish();
            }
        }
    }




    /** @return True if currently playing. */
    public boolean isPlaying() {
        return mAlive;
    }

    private void stopInternal() {
        mAlive = false;
        try {
          if(mInputStream!=null) {
              mInputStream.close();
          }
        } catch (IOException e) {
            Log.e("Play audio", "Failed to close input stream", e);
        }catch(Exception e)
        {
             e.printStackTrace();
        }
    }

    private static class Buffer extends AudioBuffer {
        @Override
        protected boolean validSize(int size) {
            return size != AudioTrack.ERROR && size != AudioTrack.ERROR_BAD_VALUE;
        }

        @Override
        protected int getMinBufferSize(int sampleRate) {
            return AudioTrack.getMinBufferSize(
                    sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        }
    }

    private void onFinish()
    {
        Log.d("Play audio","Inside check and play next file , onFinish");

        //check and play next file
        checkAndPlayNextFile();

    }

    private void checkAndPlayNextFile()
    {
        if(pendingAnnouncements!=null && pendingAnnouncements.size()>=1)
        {
            playAnnouncementFile(pendingAnnouncements.remove(0));
            Log.d("Play audio","Inside check and play next file , files exist");
        }else
        {
            Log.d("Play audio","Inside check and play next file , on finish");
            finish();
        }
    }

    /** Stops all currently streaming audio tracks. */
    public void onDestroy()
    {
        super.onDestroy();
         stop();

    }

    /** Stops playing the stream. */
    public void stop() {

        try {
               if(mThread!=null) {
                   Log.d("Play audio","Inside check and play next file , stopped thread");
                   mThread.join();
                   Log.d("Play audio","Inside check and play next file , stopped thread");
               }
        } catch (InterruptedException e) {

            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static boolean addPendingAnnouncements(String fileName)
    {
        if(mAlive) {
            return pendingAnnouncements.add(fileName);
        }else
        {
            return false;
        }
    }

}
