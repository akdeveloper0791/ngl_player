package com.ibetter.www.adskitedigi.adskitedigi.settings.playing_mode_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignReportsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.fcm.MyFirebaseMessagingService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignTriggerService;
import com.ibetter.www.adskitedigi.adskitedigi.login.GCRegisterDeviceService;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.MetricsModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.CampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.MediaModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.MainSettingsActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService.DOWNLOAD_CAMPAIGNS_PATH;
import static com.ibetter.www.adskitedigi.adskitedigi.settings.playing_mode_settings.PlayingModeSettingsModel.CLOUD_MODE;
import static com.ibetter.www.adskitedigi.adskitedigi.settings.playing_mode_settings.PlayingModeSettingsModel.ENTERPRISE_MODE;
import static com.ibetter.www.adskitedigi.adskitedigi.settings.playing_mode_settings.PlayingModeSettingsModel.NEAR_BY_MODE;

/**
 * Created by vineeth_ibetter on 12/31/17.
 */

public class PlayingModeSettingsActivity extends Activity
{
    private  PlayingModeSettingsModel playingModeSettingsModel;
    private  static  final int PICK_FILE_REQUEST_CODE=2;
    private RegisterServiceReceiver registerServiceReceiver;
    private ProgressDialog busyDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setActionBar();

        View layout = getLayoutInflater().inflate(R.layout.display_playing_mode_settings,null);
        setContentView(layout);


        playingModeSettingsModel=new PlayingModeSettingsModel
        (layout, PlayingModeSettingsActivity.this);

        updateMode();

    }


    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("CMS Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMode()
    {
        playingModeSettingsModel.getUpdateButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validations())
                {

                    int selectedId = playingModeSettingsModel.getCMSModeRadioGroup().getCheckedRadioButtonId();

                    int selectedMode;

                    switch (selectedId)
                    {
                        case NEAR_BY_MODE:
                            saveDetails(Constants.NEAR_BY_MODE);
                            break;
                        case ENTERPRISE_MODE:
                            selectedMode = Constants.ENTERPRISE_MODE;
                            checkFCM(selectedMode);
                            break;
                        case CLOUD_MODE:
                            selectedMode = Constants.CLOUD_MODE;
                            checkFCM(selectedMode);
                            break;
                        default:
                            selectedMode = selectedId;
                            break;

                    }
                }
            }
        });
    }

    private void checkFCM(final int mode)
    {
        String fcm_id=MyFirebaseMessagingService.getToken(playingModeSettingsModel.getContext());

       // if(fcm_id==null)
        {
            final ProgressDialog busyDialog = new ProgressDialog(this);
            busyDialog.setMessage("Retrieving fcm token");
            busyDialog.setCancelable(false);
            busyDialog.show();
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            busyDialog.dismiss();
                            if (!task.isSuccessful()) {
                                Log.d("FCM", "getInstanceId failed", task.getException());

                            }else
                            {
                                // Get new Instance ID token
                                String token = task.getResult().getToken();
                                // Log and toast
                                MyFirebaseMessagingService.saveFCMTokenId(PlayingModeSettingsActivity.this,token);


                            }

                          login(mode);
                        }
                    });
        }

    }

    private void login(int mode)
    {
            try {
                JSONObject array = new JSONObject();
                array.put("name", new User().getUserDisplayName(playingModeSettingsModel.getContext()));
                array.put("mac", new DeviceModel().getMacAddress());

                String desc=new User().getUserDisplayLocationDesc(playingModeSettingsModel.getContext());

                if(desc!=null)
                {
                    array.put("location_desc", desc);
                }

                String fcm_id=MyFirebaseMessagingService.getToken(playingModeSettingsModel.getContext());
                if(fcm_id==null)
                {
                    fcm_id="null";
                }

                array.put("fcm_id",fcm_id);

                //array.put("fcm_id", MyFirebaseMessagingService.getToken(playingModeSettingsModel.getContext()));
                String pwd = playingModeSettingsModel.getUserPwdET().getText().toString();


                String intentAction = "PLAYING_MODE_GC_DEVICE_REGISTER_ACTIVITY";

                //register receiver
                if(registerServiceReceiver==null)
                {
                    IntentFilter intentFilter =  new IntentFilter(intentAction);
                    intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

                    registerServiceReceiver = new RegisterServiceReceiver();
                    registerReceiver(registerServiceReceiver,intentFilter);

                }

                //display busy dialog
                displayBusyDialog("Registering device, please wait...");

                //start register service
                GCRegisterDeviceService.startAction(playingModeSettingsModel.getContext(),mode,playingModeSettingsModel.getUrlET().getText().toString(),new User().getUserDisplayName(playingModeSettingsModel.getContext()),playingModeSettingsModel.getUserNameET().getText().toString(),pwd,false, array.toString(),intentAction);

            }catch(JSONException e)
            {
                e.printStackTrace();
                Toast.makeText(playingModeSettingsModel.getContext(),"Unable to register, please try again later"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }

    }

    private  void  saveDetails(int selectedMode)
    {
        if (new User().updateUserPlayingMode(playingModeSettingsModel.getContext(),
                selectedMode, playingModeSettingsModel.getUrlET().getText().toString(),playingModeSettingsModel.getUserNameET().getText().toString(),playingModeSettingsModel.getUserPwdET().getText().toString()))
        {
            Toast.makeText(playingModeSettingsModel.getContext(), "Updated", Toast.LENGTH_SHORT).show();
            //redirect to app entry activity
            redirectToMainSettingsActivity();
           // EnterPriseSettingsModel.startEnterPriseModel(playingModeSettingsModel.getContext());

            deleteGarbageFiles();

            new User().resetPlayerId(playingModeSettingsModel.getContext());

            resetIOTDevice();

            AutoDownloadCampaignModel.stopAutoCampaignDownloadService(playingModeSettingsModel.getContext());
            PlayerStatisticsCollectionModel.stopUploadCampaignReportsService(playingModeSettingsModel.getContext());
            MetricsModel.stopMetricsService();//playingModeSettingsModel.getContext()
        }
        else
        {
            Toast.makeText(playingModeSettingsModel.getContext(), "Unable to Update", Toast.LENGTH_SHORT).show();
        }
    }
    protected boolean validations()
    {
        int selectedId = playingModeSettingsModel.getCMSModeRadioGroup().getCheckedRadioButtonId();

        if(selectedId== ENTERPRISE_MODE||selectedId==CLOUD_MODE)
        {
            String userPwd=playingModeSettingsModel.getUserPwdET().getText().toString();
            String userName=playingModeSettingsModel.getUserNameET().getText().toString();
            String enterpriseModeUrl=playingModeSettingsModel.getUrlET().getText().toString();

            if(selectedId==ENTERPRISE_MODE)
            {
                if(enterpriseModeUrl!=null&&enterpriseModeUrl.length()<1)
                {
                    playingModeSettingsModel.getUrlET().setError("Please Enter Valid Url");
                    return false;
                }
            }

            if(userName!=null&&userName.length()<1)
            {
                playingModeSettingsModel.getUserNameET().setError("Please Enter User Name");
                return false;
            }

            if(userPwd!=null&&userPwd.length()<1)
            {
                playingModeSettingsModel.getUserPwdET().setError("Please Enter User Password");
                return false;
            }


        }
        return true;
    }



    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {



        }

    }


    /*display toast*/
    private void displayToast(String msg)
    {
        Toast.makeText(playingModeSettingsModel.getContext(),msg,Toast.LENGTH_LONG).show();

    }


    //redirect to main settings activity
    private void  redirectToMainSettingsActivity()
    {
        Intent intent = new Intent(playingModeSettingsModel.getContext(), MainSettingsActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
       //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void unRegisterReceiver()
    {
        try {
            if (registerServiceReceiver != null) {
                unregisterReceiver(registerServiceReceiver);
            }
        }catch (Exception e)
        {

        }finally {

            registerServiceReceiver = null;
        }
    }
    private class RegisterServiceReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            unRegisterReceiver();
            dismissBusyDialog();

            if(intent.getBooleanExtra("flag",false))
            {
                resetTickerTextSettings();
                resetIOTDevice();
               // checkAndRestartPlayerStatisticsCollectionService();
                checkRestartAutoCampaignDownloadService(playingModeSettingsModel.getContext());
                checkAndRestartImageCaptureService();
                //EnterPriseSettingsModel.switchOffEnterPriseSettings(playingModeSettingsModel.getContext());

                deleteGarbageFiles();

                Toast.makeText(playingModeSettingsModel.getContext(), "Updated", Toast.LENGTH_SHORT).show();
                //redirect to app entry activity
                redirectToMainSettingsActivity();
            }else
            {
                Toast.makeText(context,intent.getStringExtra("status"),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayBusyDialog(String msg)
    {
        busyDialog = new ProgressDialog(playingModeSettingsModel.getContext());
        busyDialog.setCancelable(false);
        busyDialog.setMessage(msg);
        busyDialog.show();
    }

    private void dismissBusyDialog()
    {
        if(busyDialog!=null && busyDialog.isShowing())
        {
            busyDialog.dismiss();
        }
    }

    private void checkAndRestartPlayerStatisticsCollectionService()
    {
        PlayerStatisticsCollectionModel.checkRestartUploadCampaignReportsService(playingModeSettingsModel.getContext());
    }
    private void checkAndRestartImageCaptureService()
    {

            MetricsModel.startMetricsService(playingModeSettingsModel.getContext());

    }

    private void checkRestartAutoCampaignDownloadService(Context context)
    {
        if(new User().isAutoDownloadCampaignOn(context)&& User.isPlayerRegistered(context)) {
            startService(new Intent(playingModeSettingsModel.getContext(), AutoDownloadCampaignTriggerService.class));
        }
    }

    private void deleteGarbageFiles()
    {
        Cursor garbageCampaigns= CampaignsDBModel.getServerCampaigns(playingModeSettingsModel.getContext());

        if (garbageCampaigns != null && garbageCampaigns.moveToFirst()) {
            do {
                String campaignName = garbageCampaigns.getString(garbageCampaigns.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME));


                File garbageCampaignFile = new File(DOWNLOAD_CAMPAIGNS_PATH, campaignName + ".txt");

                removeCampaignResources(campaignName, garbageCampaignFile);

            }while (garbageCampaigns.moveToNext());

            CampaignsDBModel.deleteServerCampaigns(playingModeSettingsModel.getContext());

            CampaignReportsDBModel.deleteAllServerReportsCollection(playingModeSettingsModel.getContext());

        }

    }

    private void removeCampaignResources(String fileName,File file)
    {
        ArrayList<String> dataList=new ArrayList<>();

        if(file.exists())
        {
            File thumbFile=new File(DOWNLOAD_CAMPAIGNS_PATH+File.separator+playingModeSettingsModel.getContext().getString(R.string.do_not_display_media)+"-"+playingModeSettingsModel.getContext().getString(R.string.media_thumbnail)+"-"+fileName+".jpg");
            Log.i("thumbFile","delete"+thumbFile);
            if(thumbFile.exists())
            {
                //  Log.i("PreviewCampaign","deleted thumbFile is :"+thumbFile.getName());
                thumbFile.delete();
            }

            String fileJson = new MediaModel().readTextFile(file.getPath());
            // Log.i("PreviewCampaign","deleteCampaignResources:"+fileJson);
            try
            {
                JSONObject jsonObject = new JSONObject(fileJson);
                String type = jsonObject.getString("type");

                if(type.equalsIgnoreCase(playingModeSettingsModel.getContext().getString(R.string.app_default_image_name)))
                {
                    dataList.add(file.getName());
                    dataList.add(jsonObject.getString("resource"));

                }else if(type.equalsIgnoreCase(playingModeSettingsModel.getContext().getString(R.string.app_default_video_name)))
                {
                    dataList.add(file.getName());
                    dataList.add(jsonObject.getString("resource"));

                }else if(type.equalsIgnoreCase(playingModeSettingsModel.getContext().getString(R.string.app_default_multi_region)))
                {
                    dataList= processMultiRegionFile(jsonObject.getJSONArray("regions"));
                    dataList.add(file.getName());

                }else if(type.equalsIgnoreCase(playingModeSettingsModel.getContext().getString(R.string.url_txt)))
                {
                    dataList.add(file.getName());
                }

                if(dataList!=null && dataList.size()>0)
                {
                    // Log.i("PreviewCampaign","deleted dataList:"+dataList.toString());

                    for(String fileString:dataList)
                    {
                        //  Log.i("PreviewCampaign","deleted campaign file:"+fileString);
                        File resourceFile=new File(CampaignModel.getAdsKiteNearByDirectory(playingModeSettingsModel.getContext())+File.separator+fileString);
                        if(resourceFile.exists())
                        {
                            Log.i("PreviewCampaign","file is deleted:"+resourceFile.getAbsolutePath());
                            resourceFile.delete();
                        }else
                        {
                            Log.i("PreviewCampaign","file not found");
                        }
                    }
                }
            }catch(Exception e)
            {
                //error processing text, try play next media
                e.printStackTrace();
            }

        }


    }

    private ArrayList<String> processMultiRegionFile(JSONArray jsonArray)throws JSONException
    {
        ArrayList<String>fileList=new ArrayList<>();

        for(int i=0;i<jsonArray.length();i++)
        {
            JSONObject regionObject=jsonArray.getJSONObject(i);
            String type = regionObject.getString("type");
            if(type.equalsIgnoreCase(getString(R.string.media_image_type)) ||type.equalsIgnoreCase(getString(R.string.media_video_type)) ||type.equalsIgnoreCase(getString(R.string.app_default_file_name)))
            {
                fileList.add(regionObject.getString("media_name"));
            }
        }

        return fileList;
    }

    private void resetTickerTextSettings()
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(this);

        SharedPreferences.Editor saveSPEditor = saveSP.edit();

        saveSPEditor.putBoolean(getString(R.string.is_scrolling_text_on), false);
        saveSPEditor.putString(getString(R.string.scroll_text_updated_at), null);

        saveSPEditor.commit();
    }


    private void resetIOTDevice()
    {
        //success,, save response to cache
        SharedPreferences.Editor userInfoEditor = new SharedPreferenceModel().getIOTDevicesSharedPreference(this).edit();

        userInfoEditor.putLong(getString(R.string.iot),0);
        userInfoEditor.putString(getString(R.string.iot_key),null);

        boolean isCommit = userInfoEditor.commit();

        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsSp.edit();


        editor.putBoolean(getString(R.string.is_metrics_on), false);

        editor.commit();

        new MetricsModel().stopMetricsService();

    }


}
