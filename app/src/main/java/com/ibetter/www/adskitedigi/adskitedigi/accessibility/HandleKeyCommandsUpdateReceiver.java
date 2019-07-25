package com.ibetter.www.adskitedigi.adskitedigi.accessibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

public class HandleKeyCommandsUpdateReceiver extends BroadcastReceiver {

    private WeakReference<HandleKeyCommands> weakReference;
    public static final String IS_SIGNAGE_SCREEN_ACTIVE_KEY = "is_signage_screen_active_key";
    public static final String IS_THIRD_PARTY_APP_INVOKE="is_third_party_app_invoke";
    public static final String INTENT_ACTION = "com.ibetter.www.adskitedigi.adskitedigi.accessibility.HandleKeyCommands.HandleKeyCommandsUpdateReceiver";

    public HandleKeyCommandsUpdateReceiver(HandleKeyCommands reference)
    {
        weakReference = new WeakReference<>(reference);
    }

    public void onReceive(Context context,Intent intent)
    {
        HandleKeyCommands reference = weakReference.get();
        if(reference!=null)
        {
            String action = intent.getStringExtra("action");
            if (action.equals(IS_SIGNAGE_SCREEN_ACTIVE_KEY))
            {
                reference.isSignageScreenVisible = intent.getBooleanExtra("value",false);
            }else if(action.equals(IS_THIRD_PARTY_APP_INVOKE))
            {
                reference.isOtherAppIsInvoke = intent.getBooleanExtra("value",false);
            }

        }
    }
}
