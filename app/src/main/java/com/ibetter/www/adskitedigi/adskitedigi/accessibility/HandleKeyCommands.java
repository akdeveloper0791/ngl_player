package com.ibetter.www.adskitedigi.adskitedigi.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.DisplayAdsBase;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.receiver.ScreenOnOffChangeReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative.MonitorAppInvokeService;
import com.ibetter.www.adskitedigi.adskitedigi.settings.user_channel_guide.UserGuideActivity;

import java.util.Calendar;

import static com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION;

public class HandleKeyCommands extends AccessibilityService
{
    private Context context;

    private static boolean isIsOptionMenuClicked=false,
    isBackButtonClicked = false, isHomeButtonClicked = false, isPlayPauseClicked=false;

    protected static boolean isSignageScreenVisible = false;

    HandleKeyCommandsUpdateReceiver handleKeyCommandsUpdateReceiver;

    private ScreenOnOffChangeReceiver screenOnOffChangeReceiver = new ScreenOnOffChangeReceiver();

    public static boolean isOtherAppIsInvoke=false;

    public void onCreate()
    {
        super.onCreate();
        context = HandleKeyCommands.this;

        SignageServe.context.registerReceiver(screenOnOffChangeReceiver, screenOnOffChangeReceiver.getFilter());
    }

    @Override
    protected void onServiceConnected()
    {
       //register receiver
        registerUpdatesReceiver();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        boolean eventFlag=false;
        final int eventType = event.getEventType();
        switch(eventType)
        {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventFlag=true;
                break;

            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                eventFlag=true;
                break;
        }

        if(eventFlag)
        {
            if(isOtherAppIsInvoke)
            {
                long eventTime= Calendar.getInstance().getTimeInMillis();
                updateEventTime(eventTime);
                //Log.i("OtherAppInvokeService","HandleKeyCommands eventTime:"+eventTime);
            }
        }

    }


    private void updateEventTime(long eventTime)
    {
        try
        {
            Intent intent = new Intent(MonitorAppInvokeService.GLOBAL_EVENT_ACTION_INTENT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("eventTime",eventTime);
            SignageServe.context.sendBroadcast(intent);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private void  registerUpdatesReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HandleKeyCommandsUpdateReceiver.INTENT_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        if(handleKeyCommandsUpdateReceiver==null)
        {
            handleKeyCommandsUpdateReceiver = new HandleKeyCommandsUpdateReceiver(this);
            registerReceiver(handleKeyCommandsUpdateReceiver,intentFilter);
        }
    }

    private void unRegisterUpdateReceiver()
    {
        try {
            if (handleKeyCommandsUpdateReceiver != null) {
                unregisterReceiver(handleKeyCommandsUpdateReceiver);
            }
        }catch(Exception e)
        {

        }finally {
            handleKeyCommandsUpdateReceiver = null;
        }
    }


    @Override
    public void onInterrupt()
    {
        //Toast.makeText(context,"onInterrupt",Toast.LENGTH_SHORT).show();
    }


    @Override
    protected boolean onKeyEvent(KeyEvent event)
    {
        Log.d("HandleKeyComm","Handle key commands, onInterrupt - "+event.getKeyCode());
        //new DisplayDebugLogs(context).doInBackground(String.valueOf(event.getKeyCode()));
        //Toast.makeText( SignageServe.context,"Handle key commands-"+event.getKeyCode(),Toast.LENGTH_SHORT).show();

          return handleKeyEvent(event.getKeyCode());

    }

    private boolean handleKeyEvent(int eventCode)
    {
        switch (eventCode)
        {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:

                 if(!isPlayPauseClicked)
                 {
                     isPlayPauseClicked = true;
                     if(!DisplayAdsBase.isPauseMediaFlag)
                     {
                         DisplayAdsBase.isPauseMediaFlag=true;
                     }else
                     {
                         DisplayAdsBase.isPauseMediaFlag=false;
                     }
                     processPlayAndPauseCommands();

                 }else
                 {
                     isPlayPauseClicked = false;
                 }

                 return true;

            case KeyEvent.KEYCODE_HOME:
                //relaunch ss
                homeKeyPressed();
                return true;

            case KeyEvent.KEYCODE_MENU:
                //remote menu key
                if(!isIsOptionMenuClicked)
                {
                    isIsOptionMenuClicked=true;
                    displayAppSettings();
                }else
                {
                    isIsOptionMenuClicked=false;
                }
                return true;

            case KeyEvent.KEYCODE_BACK:

                //launch tv
                relaunchApp();
                return true;

                default:
                     //handleInvalidKey();
                    return false;
        }
    }


    private void displayAppSettings()
    {
        try
        {
            Intent i = new Intent(SignageServe.context, UserGuideActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SignageServe.context.startActivity(i);
        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(context, e+toString(), Toast.LENGTH_SHORT).show();
        }

    }

    synchronized  private void relaunchApp()
    {
        if(!isBackButtonClicked)
        {
            isBackButtonClicked = true;
            DeviceModel.restartApp(context);
            //showInvalidKeyToast();
        }else
        {
            isBackButtonClicked = false;
        }

    }

    private void homeKeyPressed()
    {
        if(!isHomeButtonClicked)
        {
            isHomeButtonClicked = true;
            try
            {
                Intent intent = new Intent(SignageServe.context,OtherAppLaunchService.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                {
                    startForegroundService(intent);
                }else{
                    startService(intent);
                }

            }catch (Exception e)
            {
                e.printStackTrace();
                DeviceModel.stopApp(context);
            }
        }else
        {
            isHomeButtonClicked = false;
        }
    }


    private void processPlayAndPauseCommands()
    {
        if(DisplayAdsBase.isPauseMediaFlag)
        {
            processAppCommands("PAUSE_MEDIA_ACTION_CODE");
        }else {
            processAppCommands("RESUME_MEDIA_ACTION_CODE");
        }

    }

    private void processAppCommands(String action)
    {
        Intent intent = new Intent(SM_UPDATES_INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //remote center OK button Key
        intent.putExtra("action",action);
        SignageServe.context.sendBroadcast(intent);
    }

    private void unRegisterScreenOnOffChangeReceiver()
    {
        try {

            SignageServe.context.unregisterReceiver(screenOnOffChangeReceiver);
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
