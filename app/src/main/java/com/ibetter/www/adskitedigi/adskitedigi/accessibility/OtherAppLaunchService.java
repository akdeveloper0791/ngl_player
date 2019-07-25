package com.ibetter.www.adskitedigi.adskitedigi.accessibility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

public class OtherAppLaunchService extends Service
{
    private Context context;
    private NotificationManager mNotificationManager;
    private Notification notification;
    private NotificationCompat.Builder mBuilder;
    private final int notificationId=101;
    public static final String NOTIFICATION_CHANNEL_ID ="com.ibetter.www.adskitedigi.adskitedigi.accessibility";

    public void onCreate()
    {
        super.onCreate();
        context = SignageServe.context;

        try{
            if (Build.VERSION.SDK_INT >= 26)
            {
                createNotification();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent,int flags,int startId)
    {
        if(context==null)
        {
            context = OtherAppLaunchService.this;
        }

        processHomeCommand();

        return START_STICKY; //restart the service whenever resources get available
    }

    private void processHomeCommand()
    {
        boolean flag=new User().isAppLauncherSettingOn(context);
        //Toast.makeText(context, "isAppLauncherSettingOn:"+flag, Toast.LENGTH_SHORT).show();
        if(flag)
        {
            String packageName=new User().getAppLauncherPackage(context);
           // Toast.makeText(context, "packageName:"+packageName, Toast.LENGTH_SHORT).show();
            if(packageName!=null)
            {
                launchOtherApp(packageName);
            }else
            {
                DeviceModel.stopApp(context);
                stopSelf();
                stopForeground(true);
            }
        }else
        {
            DeviceModel.stopApp(context);
            stopSelf();
            stopForeground(true);
        }
    }


    public void createNotification()
    {
        /**Creates an explicit intent for an Activity in your app**/

        mBuilder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        mBuilder.setContentTitle("Command Handling")
                //.setContentText(context.getString(R.string.auto_campaign_notification_message))
                .setAutoCancel(false)
                .setOngoing(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
               // .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Command Handling", importance);
            notificationChannel.setDescription("Processing Command...");
            //notificationChannel.enableLights(true);
            //notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);

        }

        assert  mNotificationManager != null;
        mNotificationManager.notify(notificationId /* Request Code */, mBuilder.build());
        // Build the notification.
        notification = mBuilder.build();
        startForeground(notificationId, notification);
    }


    private void launchOtherApp(String packageName)
    {
        Intent mIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null)
        {
            try {
                startActivity(mIntent);

            } catch (ActivityNotFoundException err)
            {
                Toast.makeText(context, "No application found with selected package name, Please try again later.", Toast.LENGTH_SHORT).show();
                DeviceModel.stopApp(context);
            }

        }
        stopSelf();
        stopForeground(true);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}
