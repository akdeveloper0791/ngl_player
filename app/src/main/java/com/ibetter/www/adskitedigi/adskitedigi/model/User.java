package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.AdvanceSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.auto_campaign_sync_settings.AutoCampaignDownloadSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.display_report_image_duration.DisplayReportImageDurationConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.metrics_settings.MetricsSettingsActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.player_statistics_settings.PlayerStatisticsSettings;
import com.ibetter.www.adskitedigi.adskitedigi.settings.playing_mode_settings.PlayingModeSettingsActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.user_channel_guide.UserGuideActivity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by vineeth_ibetter on 11/2/16.
 */

public class User
{

    public boolean isInternalCamType(Context context)
    {
        int typeId=new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getInt((context.getString(R.string.metrics_camera_type)),0);

        if(typeId>0)
        {
            if(typeId==1)
            {
                return true;
            }else
            {
                return false;
            }

        }else
        {
            return false;
        }
    }


    public boolean isExternalCamType(Context context)
    {
        int typeId=new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getInt((context.getString(R.string.metrics_camera_type)),0);

        if(typeId>0)
        {
            if(typeId==2)
            {
                return true;
            }else
            {
                return false;
            }

        }else
        {
            return false;
        }
    }

    /*set user Mobile number*/
    public static boolean setUserDetails(Context context,String userMobileNumber,int mode,String displayName,String locationInfo,
                                         String email)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putString(context.getString(R.string.user_mobile_number),userMobileNumber);
        userDetailsSPEditor.putInt(context.getString(R.string.playing_media_mode),mode);
        userDetailsSPEditor.putString(context.getString(R.string.user_display_name),displayName);
        userDetailsSPEditor.putString(context.getString(R.string.user_display_location_desc),locationInfo);
        userDetailsSPEditor.putString(context.getString(R.string.user_display_org_email),email);

        return userDetailsSPEditor.commit();

    }

    public static boolean updateLocation(Context context, Location location)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putString(context.getString(R.string.user_display_location_lat),String.valueOf(location.getLatitude()));
        userDetailsSPEditor.putString(context.getString(R.string.user_display_location_lng),String.valueOf(location.getLongitude()));
        return userDetailsSPEditor.commit();

    }

    public static LatLng getDeviceLocation(Context context)
    {
        SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(context);
        String lat  = sp.getString(context.getString(R.string.user_display_location_lat),null);
        String lng = sp.getString(context.getString(R.string.user_display_location_lng),null);
        if(lat!=null && lng!=null)
        {

            return new LatLng(Constants.convertToDouble(lat),Constants.convertToDouble(lng));
        }else
        {
            return null;
        }
    }


    public static boolean setCanUpdateBGLocation(Context context, boolean flag)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putBoolean(context.getString(R.string.can_update_location_bg_sp),flag);
        return userDetailsSPEditor.commit();

    }

    public static boolean getCanUpdateBGLocation(Context context)
    {
       return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getBoolean(context.getString(R.string.can_update_location_bg_sp),false);
    }

    public static boolean initNewDevice(Context context)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putLong(context.getString(R.string.user_display_created_time),getCurrentDate());
        userDetailsSPEditor.putInt(context.getString(R.string.user_display_status),Constants.DISPLAY_TRIAL_PERIOD_STATUS);

        return userDetailsSPEditor.commit();
    }

    /*set user Mobile number*/
    public static boolean setLicenceStatus(Context context,int licenceStatus)
    {

        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putInt(context.getString(R.string.user_display_status), licenceStatus);

        return userDetailsSPEditor.commit();

    }

    public static boolean saveDeviceLicenceDetails(Context context,int licenceStatus,String expiryDate,String expiryCheckedAt)
    {

        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putInt(context.getString(R.string.user_display_status), licenceStatus);
        userDetailsSPEditor.putString(context.getString(R.string.device_expiry_date_sp), expiryDate);
        userDetailsSPEditor.putString(context.getString(R.string.device_expiry_checked_at_sp), expiryCheckedAt);

        return userDetailsSPEditor.commit();

    }

    public static String getDeviceExpiryDate(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.user_details_sp),Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.device_expiry_date_sp),null);
    }

        public  long getDisplayCreatedTime(Context context)
    {
        SharedPreferences userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context);

      return   userDetailsSPEditor.getLong(context.getString(R.string.user_display_created_time),Constants.DISPLAY_EXPIRED_STATUS);

    }

    public static int getDisplayLicenceStatus(Context context)
    {
        SharedPreferences userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context);

        return   userDetailsSPEditor.getInt(context.getString(R.string.user_display_status),Constants.DISPLAY_EXPIRED_STATUS);

    }


    public static long getTrialPeriodTime(Context context)
    {

        return TimeUnit.DAYS.toMillis(15);
    }


    public static long getCurrentDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();

    }
    /*get user Mobile number*/
    public  String getUserMobileNumber(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.user_mobile_number),null);

    }

    public static String getUserEmailAddress(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.user_display_org_email),null);

    }

    /*get user Mobile number*/
    public static String getUserDisplayName(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.user_display_name),null);

    }


    /*get user Mobile number*/
    public  String getUserDisplayLocationDesc(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.user_display_location_desc),null);

    }
    /*get and save HardWareKey*/
    public  void   saveHardWareKey(Context context,String hardWareKey)
    {

        SharedPreferences.Editor userDetailsEditor=new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsEditor.putString(context.getString(R.string.sp_hard_ware_key), hardWareKey);
        userDetailsEditor.commit();

    }

    public  String   getHardWareKey(Context context)
    {

        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.sp_hard_ware_key),null);

    }


    /*get and device name*/
    public  void   saveDeviceName(Context context,String deviceName)
    {

        SharedPreferences.Editor userDetailsEditor=new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsEditor.putString(context.getString(R.string.sp_device_name), deviceName);
        userDetailsEditor.commit();

    }

    public static String  getDeviceName(Context context)
    {

        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.user_display_name),null);

    }


    /*delete Temporary Register Details */
    public void deleteRegisterDetails(Context context)
    {

        saveHardWareKey(context,null);
        saveDeviceName(context,null);

    }


    /*save register details */
    public void saveRegisterDetails(Context context)
    {

        saveHardWareKey(context,new DeviceModel().generateHardwareKey(context));

        saveDeviceName(context,new DeviceModel().generateDeviceName(context));

    }

    /*get and save register status*/
    public  void   saveRegisterStatus(Context context,String status)
    {

        SharedPreferences.Editor userDetailsEditor=new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsEditor.putString(context.getString(R.string.sp_register_status), status);
        userDetailsEditor.commit();

    }

    public  String   getRegisterStatus(Context context)
    {

        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.sp_register_status),"-1");

    }
    /*get and save  Refreshing display time stamp*/
    public  void   saveLastRefreshingRequiredFilesTimeStamp(Context context,long timeStamp)
    {

        SharedPreferences.Editor userDetailsEditor=new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsEditor.putLong(context.getString(R.string.sp_display_time_stamp), timeStamp);
        userDetailsEditor.commit();

    }

    public  long   getLastRefreshingRequiredFilesTimeStamp(Context context)
    {

        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getLong(context.getString(R.string.sp_display_time_stamp),0);

    }
    /*get user Mobile number*/
    public  int getUserPlayingMode(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.playing_media_mode),Constants.NEAR_BY_MODE);

    }
    /*get user Mobile number*/
    public  int getUserLocalPlayingMode(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.playing_local_content_mode),Constants.LOCAL_FOLDER_SEQUENTIAL_PLAYING_MODE);

    }


    /*get user Mobile number*/
    public  boolean updateUserPlayingMode(Context context,int mode,String url,String userName,String userPwd)
    {

        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putInt(context.getString(R.string.playing_media_mode),mode);

        if(mode==Constants.CLOUD_MODE||mode==Constants.ENTERPRISE_MODE)
       {

           userDetailsSPEditor.putString(context.getString(R.string.gc_user_email_id), userName);
           userDetailsSPEditor.putString(context.getString(R.string.gc_user_password), userPwd);

           if(mode==Constants.ENTERPRISE_MODE)
           {
               userDetailsSPEditor.putString(context.getString(R.string.enter_prise_url), url);

           }


        }

        return userDetailsSPEditor.commit();

    }

    /*get user Mobile number*/
    public  boolean isPlayingLocalOnlyMode(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.playing_media_mode),Constants.NEAR_BY_MODE)==Constants.NEAR_BY_MODE;

    }


    /*get user folder path*/
    public  String getUserPlayingFolderModePath(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.playing_folder_path),new DownloadMediaHelper().getAdsKiteDirectory(context));

    }

    public static void setUserPlayingFolderModePath(Context context,String path)
    {

        new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit().putString(context.getString(R.string.playing_folder_path),path).commit();

    }


    public  long getImageDuration(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getLong(context.getString(R.string.default_image_duration),Constants.DISPLAY_IMAGE_VIEW_DURATION);

    }

    /*get user Mobile number*/
    public  boolean updateImageDuration(Context context,long duration)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putLong(context.getString(R.string.default_image_duration),duration);

        return userDetailsSPEditor.commit();
    }


    public static  void appendStringToEditTextAtCursorPosition(EditText editText, String appendedString)
    {
        int start =editText.getSelectionStart();
        //this is to get the the cursor position
        editText.getText().insert(start, appendedString);
    }

    public int getLocalScrollTextMode(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);

        return saveSP.getInt(context.getString(R.string.local_scroll_text_mode), Constants.SCROLLING_CUSTOMISED_TEXT);
    }

    public boolean setLocalScrollTextMode(Context context,int mode)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        SharedPreferences.Editor saveSPEditor = saveSP.edit();

        saveSPEditor.putInt(context.getString(R.string.local_scroll_text_mode), mode);

        return saveSPEditor.commit();
    }


    public  long getReportImageDuration(Context context)
    {

        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getLong(context.getString(R.string.default_report_image_duration), DisplayReportImageDurationConstants.DISPLAY_REPORT_IMAGE_VIEW_DURATION);

    }

    /*get user Mobile number*/
    public  boolean updateReportImageDuration(Context context,long duration)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putLong(context.getString(R.string.default_report_image_duration),duration);

        return userDetailsSPEditor.commit();
    }

    //check the existing schedule files
    public void checkExistingScheduleFiles(Activity context)
    {
        invokeLocalFolderAds(context);

       /* int mode=new User().getUserPlayingMode(context);
        int localContentMode=new User().getUserLocalPlayingMode(context);

        switch (mode)
        {
            case Constants.NEAR_BY_MODE:

                switch(localContentMode)
                {
                    case Constants.LOCAL_FOLDER_SEQUENTIAL_PLAYING_MODE:

                        invokeLocalFolderAds(context);
                        break;

                    case Constants.LOCAL_FOLDER_SCHEDULE_PLAYING_MODE:
                        //invokeDisplayAdsActivity(context);
                        break;
                }
gc_user_unique_key
                break;

            case Constants.CLOUD_MODE:

              /*  if (new ScheduleFilesDBModel(context).getNextScheduleLayout(0).getCount() != 0)
                {
                    invokeDisplayAdsActivity(context);
                }
                else
                {
                    startGetRequiredDetailsAndScheduleDetailsActivity(context);
                }
                break;


    }*/



    }

    //invoke display ads service
    private void invokeLocalFolderAds(Activity activity)
    {
        activity.startActivity(new Intent(activity, DisplayLocalFolderAds.class));
        activity.finish();
    }






    //check and redirect to xibo white labelled app
    private void redirectToXibo(Activity context)
    {
        if(new Validations().isDisplayMultiCloudMode(context))
        {
            Intent launchFacebookApplication = context.getPackageManager().getLaunchIntentForPackage(context.getString(R.string.white_label_app_package));
            context.startActivity(launchFacebookApplication);
            // finish();
        }else
        {
            Toast.makeText(context,context.getString(R.string.error_msg_un_support_playing_mode),Toast.LENGTH_LONG).show();

            //if app not found redirect user to settings page ask user to change the settings or contact customer care
            context.startActivity(new Intent(context, PlayingModeSettingsActivity.class));
            context.finish();
        }


    }

    public String getLocalScrollText(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getString(context.getString(R.string.local_scroll_text), context.getString(R.string.display_ads_layout_scrolling_text));

    }

    public boolean isLocalScrollTextBold(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getBoolean(context.getString(R.string.local_scroll_text_bold), true);

    }

    public boolean isLocalScrollTextItalic(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getBoolean(context.getString(R.string.local_scroll_text_italic), true);

    }

    public String getScrollTextUpdatedAt(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getString(context.getString(R.string.scroll_text_updated_at), null);

    }

    public String getScrollTextBgColor(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getString(context.getString(R.string.scroll_text_bg_color),  context.getResources().getString(R.string.display_ad_scrolling_text_bg_color));

    }

    public String getScrollTextTextColor(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getString(context.getString(R.string.scroll_text_text_color),   context.getResources().getString(R.string.display_ad_scrolling_text_color));

    }

    public int getScrollTextSize(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getInt(context.getString(R.string.scroll_text_text_size),  40);

    }


    public int getMediaScrollTextPosition(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getInt(context.getString(R.string.local_media_scroll_text), Constants.DEFAULT_SCROLL_MEDIA_POSITION);

    }

    public boolean isScrollTextOn(Context context)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
        return saveSP.getBoolean(context.getString(R.string.is_scrolling_text_on), false);
    }

    public boolean isAppLauncherSettingOn(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getBoolean(context.getString(R.string.other_app_launch_setting), AdvanceSettings.DEFAULT_OTHER_APP_LAUNCHER_STATUS);

    }
    public String getAppLauncherPackage(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getString((context.getString(R.string.other_app_package_name)),null);

    }

    public boolean updateSelectedAppPackageInSP(Context context,String packageName)
    {
        SharedPreferences settingSP = new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context);
        SharedPreferences.Editor editor = settingSP.edit();
        editor.putString(context.getString(R.string.other_app_package_name),packageName);
        return editor.commit();
    }


    //get green content CMS user login info from SP
    public static boolean isPlayerRegistered(Context context)
    {
      return (new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.player_id),0) >=1 ? true:false);

    }




    public static int getPlayerId(Context context)
    {
       return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.player_id),0);
    }

    public static void resetPlayerId(Context context)
    {
        SharedPreferences settingSP = new SharedPreferenceModel().getUserDetailsSharedPreference(context);
        SharedPreferences.Editor editor = settingSP.edit();
        editor.putInt(context.getString(R.string.player_id),0);
        editor.commit();
    }
    public static String getPlayerMac(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.player_mac),null);
    }


    public boolean isGCUserLogin(Context context)
    {
        boolean flag=false;
        String userId=new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.gc_user_unique_key),null);
        if(userId!=null)
        {
            flag=true;
        }
        return flag;
    }

    public String getGCUserUniqueKey(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.gc_user_unique_key),null);
    }


    public int getGCUserId(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.gc_user_id),0);
    }

    public String getGCUserMailId(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.gc_user_email_id),null);
    }

    public String getGCUserPwd(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.gc_user_password),null);
    }


    public String getEnterpriseUserMailId(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.enterprise_user_email_id),null);
    }

    public String getEnterpriseUserPwd(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.enterprise_user_password),null);
    }


    public String getEnterPriseURL(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.enter_prise_url),null);
    }

    public String getGCUserFirstName(Context context)
    {
        return new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.gc_user_first_name),null);
    }


    public  boolean saveGCUserPassword(Context context,String password)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putString(context.getString(R.string.gc_user_password),password);
        return userDetailsSPEditor.commit();
    }


    public String getUserChannelName(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getString((context.getString(R.string.user_guide_channel_name)), UserGuideActivity.DEFAULT_GUIDE_CHANNEL_NAME);
    }

    public boolean updateUserChannelNameInSP(Context context,String channelName)
    {
        SharedPreferences settingSP = new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context);
        SharedPreferences.Editor editor = settingSP.edit();
        editor.putString(context.getString(R.string.user_guide_channel_name),channelName);
        return editor.commit();
    }

    public long getImageCaptureDuration(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getLong((context.getString(R.string.image_capture_duration)),30);
    }


    public boolean isMetricsOn(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getBoolean((context.getString(R.string.is_metrics_on)), MetricsSettingsActivity.DEFAULT_METRICS_STATUS);
    }


    public long getAutoCampaignDownloadDuration(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getLong((context.getString(R.string.auto_campaign_download_duration)), AutoCampaignDownloadSettings.DEFAULT_AUTO_DOWNLOAD_CAMPAIGN_DURATION);
    }


    public boolean isAutoDownloadCampaignOn(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getBoolean((context.getString(R.string.is_auto_download_campaign)), AutoCampaignDownloadSettings.DEFAULT_AUTO_DOWNLOAD__STATUS);
    }

    public long getPlayerStatisticsCollectionDuration(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getLong((context.getString(R.string.player_statistics_collection_duration)), PlayerStatisticsSettings.DEFAULT_PLAYER_COLLECTION_DURATION);
    }


    public boolean isPlayerStatisticsCollectionOn(Context context)
    {
        return  new SharedPreferenceModel().getSignageMgrSettingSharedPreference(context).getBoolean((context.getString(R.string.player_statistics_collection)), PlayerStatisticsSettings.DEFAULT_PLAYER_STATISTICS_COLLECTION_STATUS);
    }


}
