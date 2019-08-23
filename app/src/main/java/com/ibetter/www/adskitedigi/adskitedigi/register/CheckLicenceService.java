package com.ibetter.www.adskitedigi.adskitedigi.register;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.DisplayAdsBase;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.DISPLAY_EXPIRED_STATUS;

public class CheckLicenceService extends Service {
    private Context context ;

    public CheckLicenceService()
    {
        super();
    }

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onCreate()
    {
        context=CheckLicenceService.this;
        startFrontEndNotification();
    }

    private void startFrontEndNotification()
    {
        NotificationModelConstants.displayFrontNotification(this,"Checking licence",
                NotificationModelConstants.LICENCE_SERVICE_ID,"Checking licence");
    }

    public int onStartCommand(Intent intent,int flags,int startId)
    {
        Log.d("Licence","Inside on start command startId"+startId+", flags"+flags);
        checkForLicence();
       return flags;
    }

    private void checkForLicence()
    {
        SharedPreferences licenceSp = getSharedPreferences(getString(R.string.user_details_sp),MODE_PRIVATE);
        String lastExpiredCheckedAt = licenceSp.getString(getString(R.string.device_expiry_checked_at_sp),null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(Calendar.getInstance().getTime());
        if(!(lastExpiredCheckedAt!=null && lastExpiredCheckedAt.equals(today)))
        {
          //check in server
            IntentFilter intentFilter = new IntentFilter(RegisterServiceReceiver.INTENT_ACTION);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

            registerReceiver(new RegisterServiceReceiver(),intentFilter);

            //start service to check licence
            //start register service
            Intent intent = new Intent(context, RegisterDisplayService.class);
            intent.putExtra(getString(R.string.app_default_intent_action_text), RegisterServiceReceiver.INTENT_ACTION);
            startService(intent);
        }
        else
        {
          Log.d("Licence","Inside register licence already checked for "+lastExpiredCheckedAt);
          stopSelf();


        }
    }

    private class RegisterServiceReceiver extends BroadcastReceiver
    {
        private final static String INTENT_ACTION = "com.ibetter.www.adskitedigi.adskitedigi.register.CheckLicenceService.RegisterServiceReceiver";
        public void onReceive(Context context,Intent intent)
        {
           unregisterReceiver(this);

            if (intent.getBooleanExtra(getString(R.string.app_default_flag_text), false)) {

                onSuccess(Constants.convertToInt(intent.getStringExtra(getString(R.string.app_default_success_code_text))));
            } else {
                String errorMessage = intent.getStringExtra(getString(R.string.app_default_error_msg_text));
                Log.d("Licence","Inside RegisterServiceReceiver "+errorMessage);
                onFailure();
            }
        }
    }

    private void onSuccess(int deviceStatus)
    {
        if(deviceStatus==DISPLAY_EXPIRED_STATUS)
        {
            //device expired
            licenceExpired();
        }else
        {
            //licence is still there
            stopSelf();
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
        stopForeground(true);
    }

    private void licenceExpired()
    {
        //restart activity
        sendResetIsRelaunchAppOnStop(false); //to safe restart

        try
        {
            Thread.sleep(500);
        }catch(InterruptedException e)
        {

        }

        DeviceModel.restartApp(context);
    }

    private void sendResetIsRelaunchAppOnStop(boolean status)
    {
        Intent intent = new Intent(DisplayAdsBase.UPDATE_RECIVER_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("action",getString(R.string.update_is_re_launch));
        intent.putExtra("value",status);
        sendBroadcast(intent);
    }

    private void onFailure()
    {
        //check with off line dates, when unable to connect to internet

        String serverExpiryDateString = User.getDeviceExpiryDate(context);
        boolean isExpired = true;
        try {
            if (serverExpiryDateString != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.GC_SERVER_DATE_TIME_FORMAT);
                Date expiredDate = sdf.parse(serverExpiryDateString);
                Date todayDate = Calendar.getInstance().getTime();
                Log.d("Licence","expiryDate "+expiredDate.getTime());
                Log.d("Licence","today date "+todayDate.getTime());
                if(expiredDate.getTime()>todayDate.getTime())
                {
                    //expired
                    isExpired = false;
                }

            }
        }catch(Exception e)
        {
            Log.d("Licence","Error in verifying license on failure "+e.getMessage());
            e.printStackTrace();
        }

        Log.d("Licence","isExpired "+isExpired);
        if(isExpired)
        {
            User.setLicenceStatus(context,Constants.DISPLAY_EXPIRED_STATUS);
            licenceExpired();
        }
    }
}
