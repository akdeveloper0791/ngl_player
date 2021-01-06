package com.ibetter.www.adskitedigi.adskitedigi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.receiver.ScreenOnOffChangeReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION;

public class DisplayAdsBase extends YouTubeBaseActivity {

   Context context;
   public boolean isRelaunchAppOnStop = true;
   private static boolean isPlayPauseClicked;
   public static boolean isPauseMediaFlag=false;
   private UpdateReceiver updateReceiver;
   public static final String UPDATE_RECIVER_ACTION = "com.ibetter.www.adskitedigi.adskitedigi.DisplayAdsBase.UpdateReceiver";
   ScreenOnOffChangeReceiver screenOnOffChangeReceiver = new ScreenOnOffChangeReceiver();

    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        context = this;

        setUpdateReceiver();
        registerReceiver(screenOnOffChangeReceiver, screenOnOffChangeReceiver.getFilter());
       //isRelaunchAppOnStop = true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        Log.d("Dispatch key event","Inside dispatch key event - "+event.getKeyCode());
       // Toast.makeText(context, "ACTION_UP:"+event.getKeyCode(), Toast.LENGTH_SHORT).show();
       return handleKeyEvent(event.getKeyCode());

    }

    public void onResume()
    {
        super.onResume();
        //check and play auto campaign
        checkAndPlayAutoCampaignRule();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER)
        {
            // Do something here...
            event.startTracking(); // Needed to track long presses
            DeviceModel.stopApp(context);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean handleKeyEvent(int eventCode)
    {
        switch (eventCode)
        {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:

                if(!isPlayPauseClicked)
                {
                    isPlayPauseClicked = true;
                    if(!isPauseMediaFlag)
                    {
                        isPauseMediaFlag=true;
                    }else
                    {
                        isPauseMediaFlag=false;
                    }
                    processPlayAndPauseCommands();
                }else
                {
                    isPlayPauseClicked = false;
                }
                return true;


            case KeyEvent.KEYCODE_MENU:
                displayAppSettings();
                //playMenuAutoCampaigns();
                return true;

            case KeyEvent.KEYCODE_HOME:
                DeviceModel.processHomeCommand(context);
                return true;

            case KeyEvent.KEYCODE_BACK:
                //launch tv
                finish();
                return true;

            default:
                //handleInvalidKey();
                return false;
        }
    }



    private void displayAppSettings()
    {
        Intent intent = new Intent(SM_UPDATES_INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //remote menu key
        intent.putExtra(getString(R.string.action),"KEYCODE_MENU");
        sendBroadcast(intent);
    }

    private void processPlayAndPauseCommands()
    {
        if(isPauseMediaFlag)
        {
            pauseCurrentMedia();
        }else {
            resumeCurrentMedia();
        }

    }

    //pause current playing media
    private void pauseCurrentMedia()
    {
        Log.d("Pause","Inside pause current media Handle media settings service");
        if( DisplayLocalFolderAds.actionReceiver!=null)
        {
            Log.d("Pause","Inside pause current media Handle media settings service action receiver is not null");
            DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.PAUSE_MEDIA_ACTION_CODE,null);

        }
    }

    //pause current playing media
    private void resumeCurrentMedia()
    {
        //Log.d("Resume","Inside resume current media Handle media settings service");
        if( DisplayLocalFolderAds.actionReceiver!=null)
        {
            Log.d("Pause","Inside resume current media Handle media settings service action receiver is not null");
            DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.RESUME_MEDIA_ACTION_CODE,null);

        }
    }


    private void checkAndPlayAutoCampaignRule()
    {
        if(getIntent()!=null && getIntent().hasExtra("campaignFiles")) {


            final ArrayList<File> campaignFilesArray = (ArrayList<File>) getIntent().getSerializableExtra("campaignFiles");
            if (campaignFilesArray != null && campaignFilesArray.size() >= 1) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendAutoCampaignBroadCast(campaignFilesArray);
                    }
                },1500);
            }
        }
    }

    private void sendAutoCampaignBroadCast(ArrayList<File> campaignsFileArray)
    {
        Intent intent = new Intent(SM_UPDATES_INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("action", getString(R.string.handle_rule_request));
        intent.putExtra("campaignFiles", campaignsFileArray);
        sendBroadcast(intent);

    }

    //update receiver
    private void setUpdateReceiver()
    {
        IntentFilter intentFilter = new IntentFilter(UPDATE_RECIVER_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if(updateReceiver==null)
        {
            updateReceiver = new UpdateReceiver();
            registerReceiver(updateReceiver,intentFilter);
        }
    }

    public void unRegisterUpdateReceiver()
    {
        try
        {
            unregisterReceiver(updateReceiver);
        }catch (Exception e)
        {

        }finally {
            updateReceiver = null;
        }
    }

    private class UpdateReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context,Intent intent)
        {
            if (intent != null && intent.hasExtra(getString(R.string.action)))
            {
                String actionString=intent.getStringExtra(getString(R.string.action));

                if(actionString.equals(getString(R.string.update_is_re_launch)))
                {
                    isRelaunchAppOnStop = intent.getBooleanExtra("value",false);
                }
            }
        }
    }


    private void unRegisterScreenOnOffChangeReceiver()
    {
        try {

           unregisterReceiver(screenOnOffChangeReceiver);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    public void onDestroy()
    {
        super.onDestroy();
        unRegisterUpdateReceiver();
        unRegisterScreenOnOffChangeReceiver();

    }

}
