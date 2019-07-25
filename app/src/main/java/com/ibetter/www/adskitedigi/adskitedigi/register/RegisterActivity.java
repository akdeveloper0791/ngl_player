package com.ibetter.www.adskitedigi.adskitedigi.register;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.logs.DisplayDebugLogs;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DisplayDialog;
import com.ibetter.www.adskitedigi.adskitedigi.model.NetworkModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionModel;
import com.ibetter.www.adskitedigi.adskitedigi.send_mail.SendMailToDigiContact;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

/**
 * Created by vineeth_ibetter on 11/17/16.
 */

public class RegisterActivity extends Activity {

    private RegisterActivityModel registerActivityModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        initializeRegisterActivityModel();

        saveRegisterDetails();

        invokeRegisterDisplayService();


    }

    /*initialize register activity Model*/
    private void initializeRegisterActivityModel()
    {

        registerActivityModel=new RegisterActivityModel();

        registerActivityModel.setRegisterActivityContext(RegisterActivity.this);

        registerActivityModel.setRegisterDialogModel(new DisplayDialog());

    }


    /*register Display Receiver*/
    protected class RegisterDisplayReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            try {

                registerActivityModel.unRegisterRegisterDisplayReceiver();

                registerActivityModel.getRegisterDialogModel().dismissBusyDialog();

                /*get response parameters*/

                if (intent.getBooleanExtra(getString(R.string.app_default_flag_text), false)) {
                    String successMessage = intent.getStringExtra(getString(R.string.app_default_success_msg_text));
                    String successCode = intent.getStringExtra(getString(R.string.app_default_success_code_text));
                    onSuccess(successMessage, successCode);
                } else {
                    String errorMessage = intent.getStringExtra(getString(R.string.app_default_error_msg_text));
                    onFailure(errorMessage);
                }
            }catch (Exception e)
            {
               //new  DisplayDebugLogs(context).execute("error rx"+e.getMessage());
            }
        }

    }

    //register Register Display reciever
    private boolean registerRegisterDeviceReceiver() {

        registerActivityModel.setRegisterDisplayServiceReceiver(new RegisterDisplayReceiver());
        return registerActivityModel.registerRegisterDisplayReceiver();
    }


    //start register display service
    private void invokeRegisterDisplayService()
    {
        if(new NetworkModel().isInternet(registerActivityModel.getRegisterActivityContext()))
        {

            registerRegisterDeviceReceiver();

            displayRegisterDisplayBusyDialog();

            //start register service
            Intent intent = new Intent(registerActivityModel.getRegisterActivityContext(), RegisterDisplayService.class);
            intent.putExtra(getString(R.string.app_default_intent_action_text), registerActivityModel.REGISTER_INTENT_ACTION);
            startService(intent);

        }
        else
        {

            onFailure(getString(R.string.no_internet_connection_error_msg));

        }

    }

    /*save Temporary Register Details*/
    private void saveRegisterDetails()
    {
       new User().saveRegisterDetails(registerActivityModel.getRegisterActivityContext());

    }

    /*display Register Display Dialog*/

    private void displayRegisterDisplayBusyDialog()
    {
        registerActivityModel.getRegisterDialogModel().displayBusyDialog(registerActivityModel.getRegisterActivityContext(),getString(R.string.register_action_progress_dialog_text),false).show();
        registerActivityModel.getRegisterDialogModel().displayBusyDialog(registerActivityModel.getRegisterActivityContext(),getString(R.string.register_action_progress_dialog_text),false).show();
    }

    /*success response*/
    private void  onSuccess(String successMessage,String successCodeString)
    {

        Log.i("successCodeString ser",successCodeString);
        new User().saveRegisterStatus(registerActivityModel.getRegisterActivityContext(),successCodeString);
        int successCode=Constants.convertToInt(successCodeString);

        if(successCode==0)
        {
            User.setLicenceStatus(RegisterActivity.this,Constants.DISPLAY_SUCCESS_STATUS);

            new User().updateUserPlayingMode(registerActivityModel.getRegisterActivityContext(), Constants.NEAR_BY_MODE, null,null,null);
            //start enterprise mode
            EnterPriseSettingsModel.startEnterPriseModel(this);

            checkAndRestartPlayerStatisticsCollectionService();
            checkRestartAutoCampaignDownloadService();

            new User().checkExistingScheduleFiles(RegisterActivity.this);
        }
        else
        {

            if(successCode==1||successCode==2)
            {
                sendMail();
            }

            if(User.getDisplayLicenceStatus(this)==Constants.DISPLAY_EXPIRED_STATUS)
            {
                AlertDialog.Builder successInfoDialog = registerActivityModel.getRegisterDialogModel().displayAlertDialog(registerActivityModel.getRegisterActivityContext(), successMessage+getString(R.string.app_default_contact_info), getString(R.string.app_default_alert_title_info), false);
                successInfoDialog.setNegativeButton(registerActivityModel.getRegisterActivityContext().getString(R.string.app_default_alert_negative_button_ok_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        finish();
                    }
                });


                successInfoDialog.create().show();
            }else
            {
                //for new install extend trail period and proceed
                //start enterprise mode

                User.initNewDevice(this);
                new User().updateUserPlayingMode(registerActivityModel.getRegisterActivityContext(), Constants.NEAR_BY_MODE, null,null,null);
                //start enterprise mode
                EnterPriseSettingsModel.startEnterPriseModel(this);
                checkAndRestartPlayerStatisticsCollectionService();
                checkRestartAutoCampaignDownloadService();

                new User().checkExistingScheduleFiles(RegisterActivity.this);

            }


            /* */


        }

    }



    private void checkAndRestartPlayerStatisticsCollectionService()
    {
        PlayerStatisticsCollectionModel.checkRestartUploadCampaignReportsService(RegisterActivity.this);
    }

    private void checkRestartAutoCampaignDownloadService()
    {
        AutoDownloadCampaignModel.checkRestartAutoCampaignDownloadService(RegisterActivity.this);
    }

    private void sendMail() {

        try
        {
            startService(new Intent(registerActivityModel.getRegisterActivityContext(), SendMailToDigiContact.class));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }
    /* failure respose */
    private void onFailure(String errorMessage)
    {

        new User().deleteRegisterDetails(registerActivityModel.getRegisterActivityContext());

        AlertDialog.Builder failureInfoDialog= registerActivityModel.getRegisterDialogModel().displayAlertDialog(registerActivityModel.getRegisterActivityContext(),errorMessage,getString(R.string.app_default_alert_title_info),false);

        failureInfoDialog.setNegativeButton(registerActivityModel.getRegisterActivityContext().getString(R.string.app_default_alert_negative_button_ok_text), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                finish();

            }
        });

        failureInfoDialog.create().show();
    }

    public void onDestroy()
    {

        super.onDestroy();

        registerActivityModel.unRegisterRegisterDisplayReceiver();

    }



}
