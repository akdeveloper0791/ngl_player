package com.ibetter.www.adskitedigi.adskitedigi;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.login.GcDeviceRegister;
import com.ibetter.www.adskitedigi.adskitedigi.login.LoginActivity;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.MetricsModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.DisplayDialog;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.CheckAndRestartSMServiceOreo;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionModel;
import com.ibetter.www.adskitedigi.adskitedigi.register.CheckLicenceService;
import com.ibetter.www.adskitedigi.adskitedigi.register.RegisterActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.user_channel_guide.UserGuideActivity;

/**
 * Created by vineeth_ibetter on 11/17/16.
 */

public class AppEntryClass extends Activity
{
    private Context context;

    private GestureDetector mDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_entry_screen);

        context = AppEntryClass.this;

        mDetector = new GestureDetector(this,new MyGestureListener());


         /*check db updataions */
         DataBaseHelper db = new DataBaseHelper(context);

         db.getWritableDatabase();

         checkAndRestartServices();

         checkUserLoginDetails();//app release

    }

    //check UserLogin xibo
    private void checkUserLoginDetails()
    {

        if (new Validations().validateMobileNumber(context, new User().getUserMobileNumber(context))) {
            checkUserRegisterDetails();
        }
        else
            {
            invokeLoginActivity();
        }

    }

   /*private void checkUserLoginDetails()
   {
       if(User.isPlayerRegistered(context))
       {
           new User().checkExistingScheduleFiles(AppEntryClass.this);
       }else
       {
           //invokeLoginActivity();
           invokeGCLoginActivity();
       }
   }
*/

   private void invokeGCLoginActivity()
   {
       Intent startRegisterActivityIntent = new Intent(context, GcDeviceRegister.class);
       startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(startRegisterActivityIntent);
       finish();
   }

    //invoke login Activity
    private void invokeLoginActivity() {

        Intent startRegisterActivityIntent = new Intent(context, LoginActivity.class);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startRegisterActivityIntent);
        finish();

    }

    //invoke register Activity
    private void invokeRegisterActivity()
    {
        Intent startRegisterActivityIntent = new Intent(context, RegisterActivity.class);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startRegisterActivityIntent);
        finish();
    }

    //check User Register
    private void checkUserRegisterDetails()
    {
       /*   int licenceStatus= new User().getDisplayLicenceStatus(context);
        if(licenceStatus==)*/


        int statusCode = (new User().getDisplayLicenceStatus(context));
        Log.i("successCodeString",""+statusCode);
        switch (statusCode)
        {
            case 0:
                checkForTrailPeriod();
                //start service to check licence
                startLicenceCheck();
                break;

            case 1:
                new User().checkExistingScheduleFiles(AppEntryClass.this);
                //start service to check licence
                startLicenceCheck();
                break;

            case 2:

                //displayRegisterInfo(getString(R.string.waiting_message)+getString(R.string.app_default_contact_info));
                checkForTrailPeriod();
                break;

            case Constants.DISPLAY_EXPIRED_STATUS:
                displayExpiredInfo();
                break;

            default:


                invokeRegisterActivity();

                break;

        }


    }

    //display register info
    private void displayRegisterInfo(String msg)
    {

        AlertDialog.Builder registerInfoDialog = new DisplayDialog().displayAlertDialog(context, msg, getString(R.string.app_default_alert_title_info), false);

        registerInfoDialog.setPositiveButton(getString(R.string.app_default_alert_refresh_button_text), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                invokeRegisterActivity();
                dialog.dismiss();
            }
        });

        registerInfoDialog.setNegativeButton(getString(R.string.app_default_alert_negative_button_exit_text), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        registerInfoDialog.create().show();

    }

    //check the app version and restart the services
    private void checkAndRestartServices()
    {
        try
        {
            int preVerCode=new  DeviceModel().getSavedAppVersionCode(context);

            Log.i("preVerCode ",""+preVerCode);

            if(preVerCode==0)
            {
                //save the app versions code in Device model class
                if(new  DeviceModel().savedAppVersionCode(context))
                {
                    //call restart services
                    callRestartServices();
                }

            }else
            {
                int currentVerCode=new DeviceModel().getAppVersionCode();

                Log.i("currentVerCode ",""+currentVerCode);

                if(preVerCode!=0 && currentVerCode!=preVerCode)
                {

                    //save the app versions code in Device model class
                    if(new  DeviceModel().savedAppVersionCode(context))
                    {
                        //call restart services
                        callRestartServices();
                    }
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void callRestartServices()
    {


            int mode=new User().getUserPlayingMode(context);

            switch (mode)
            {
                case Constants.NEAR_BY_MODE:
                    ContextCompat.startForegroundService(context, new Intent(context, CheckAndRestartSMServiceOreo.class) );
                    break;
                case Constants.CLOUD_MODE:
                    checkAndRestartImageCaptureService();
                    //checkAndRestartPlayerStatisticsCollectionService();
                    checkRestartAutoCampaignDownloadService();
                    break;
                case Constants.ENTERPRISE_MODE:
                    checkAndRestartImageCaptureService();
                    //checkAndRestartPlayerStatisticsCollectionService();
                    checkRestartAutoCampaignDownloadService();
                    break;
            }
            //context.startService(new Intent(context, CheckAndRestartSMService.class));
         }

    private void checkAndRestartImageCaptureService()
    {
        MetricsModel.startMetricsService(context);
    }


    private void checkAndRestartPlayerStatisticsCollectionService()
    {
        PlayerStatisticsCollectionModel.checkRestartUploadCampaignReportsService(context);
    }

    private void checkRestartAutoCampaignDownloadService()
    {
        AutoDownloadCampaignModel.checkRestartAutoCampaignDownloadService(context);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){

        this.mDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            //check and display add local schedule layout
            startActivity(new Intent(context, UserGuideActivity.class));
            finish();

            return true;
        }

    }


    protected boolean isStoragePermissionGranted()
    {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SET_TIME)
                == PackageManager.PERMISSION_GRANTED) {
            //Now you have permission
            return true;
        }
        else
        {
            ActivityCompat.requestPermissions(AppEntryClass.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults!=null && grantResults.length>=1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if(requestCode == 1)
            {

                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                am.setTime(1529212301);

                }

        }
    }

   private void checkForTrailPeriod()
   {
      int status = User.getDisplayLicenceStatus(context);
      if(status == Constants.DISPLAY_TRIAL_PERIOD_STATUS)
      {
          //allow the user to play the campaigns
          new User().checkExistingScheduleFiles(AppEntryClass.this);
      }else
      {
          displayRegisterInfo("Your Display Licence period has been expired please contact our support team 8105303245");
      }
   }

   private void startLicenceCheck()
   {
      ContextCompat.startForegroundService(context,new Intent(this, CheckLicenceService.class));

   }

   private void displayExpiredInfo()
   {
       invokeRegisterActivity();

   }





}
