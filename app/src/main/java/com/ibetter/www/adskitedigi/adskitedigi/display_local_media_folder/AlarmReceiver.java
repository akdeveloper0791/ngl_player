package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver
{
    private Context context;
    public static boolean isAppLauncherFlag=false;
    @Override
        public void onReceive(Context context, Intent intent)
        {
             this.context=context;

             Log.i("AlarmService","getAppToForeground has been triggered:");


             //call the method here
              getAppToForeground();
        }



    private void getAppToForeground()
    {
        isAppLauncherFlag=true;

        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.ibetter.www.adskitedigi.adskitedigi");
        if (intent != null)
        {
            intent.setPackage(null);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
        }
    }

}
