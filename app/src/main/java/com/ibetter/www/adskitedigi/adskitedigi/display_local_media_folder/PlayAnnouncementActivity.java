package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.nearby.connection.Payload;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.AudioPlayer;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import java.util.HashSet;
import java.util.Set;

public class PlayAnnouncementActivity extends Activity
{
    /**
     * A Handler that allows us to post back on to the UI thread. We use this to resume discovery
     * after an uneventful bout of advertising.
     */
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());


    /** {@see Handler#post()} */
    protected void post(Runnable r) {
        mUiHandler.post(r);
    }

    /** For playing audio from other users nearby. */
    private final Set<AudioPlayer> mAudioPlayers = new HashSet<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        Log.d("stream","inside PlayAnnouncementActivity");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.play_announcement_layout);

        Payload payload= SignageServe.signageServeObject.getStreamingPayload();

        this.setFinishOnTouchOutside(false);

        if(payload!=null)
       {
           AudioPlayer player =
                   new AudioPlayer(payload.asStream().asInputStream()) {
                       @WorkerThread
                       @Override

                       protected void onFinish() {
                           final AudioPlayer audioPlayer = this;
                           post(
                                   new Runnable() {
                                       @UiThread
                                       @Override
                                       public void run() {
                                           mAudioPlayers.remove(audioPlayer);
                                           SignageServe.signageServeObject.setStreamingPayload(null);


                                           finish();
                                       }
                                      });
                       }
                   };

           mAudioPlayers.add(player);
           player.start();
       }
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
        stopPlaying();

    }

    /** Stops all currently streaming audio tracks. */
    private void stopPlaying()
    {


        for (AudioPlayer player : mAudioPlayers) {
            player.stop();
        }

        mAudioPlayers.clear();


    }

    public void onBackPressed()
    {

    }

}
