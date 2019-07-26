package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.contextual_ads.SyncRulesService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaignResultReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.GCNotification;
import com.ibetter.www.adskitedigi.adskitedigi.model.CampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.MediaModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.NetworkModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings.ScrollTextSettingsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_CAMP_SIZE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_CAMP_TYPE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_CREATED_DATE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_SAVE_PATH;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_SOURCE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_STOR_LOCATION;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_UPDATED_DATE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_UPLOADED_BY;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService.DOWNLOAD_CAMPAIGNS_PATH;
import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.AUTO_DOWNLOAD_CAMPAIGN_SERVICE_NOTIFY_ID;

public class AutoDownloadCampaignTriggerService extends Service implements AutoDownloadCampaignReceiver.CallBack {

    public static AutoDownloadCampaignReceiver autoDownloadCampaignReceiver;

    private ArrayList<String> restoringServerIds = new ArrayList<>();
    private Context context;

    public AutoDownloadCampaignTriggerService() {
        super();
    }

    public static boolean isServiceOn = false;



    @Override
    public void onCreate() {
        context = AutoDownloadCampaignTriggerService.this;

        initRx();
        checkAndStartForegroundNotification();
        isServiceOn = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        downloadBasicCampaignsList();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        isServiceOn = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void checkAndStartForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(AUTO_DOWNLOAD_CAMPAIGN_SERVICE_NOTIFY_ID, GCNotification.campaignAutoDownloadServiceNotification(context,
                    "Auto Downloading campaigns"));
        }
    }

    private void downloadBasicCampaignsList() {
        if (new User().isGCUserLogin(context)) {
            if (new NetworkModel().isInternet(context)) {
                Intent startIntent = new Intent(context, AutoCampDownloadListService.class);
                startService(startIntent);
            } else {
                Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

                onCompleted();
            }

        } else {

            onCompleted();

        }

    }

    private void initRx() {

        autoDownloadCampaignReceiver = new AutoDownloadCampaignReceiver(new Handler(), AutoDownloadCampaignTriggerService.this, this);

    }

    @Override
    public void initDownloadListApiError(Bundle resultData) {

        if(resultData.getInt("statusCode",0)==2)//no campaigns found
        {
            deleteUnknownCampaigns();
            new ScrollTextSettingsModel(context).setScrollTextStatus(false);
            updateTickerTextSettings();
        }


        onCompletedAutoSyncCampaignsList();

    }

    @Override
    public void initDownloadListApiResponse(Bundle resultData)
    {
        try
        {
            if (resultData.getBoolean("flag")) {

               prepareListForDownload();

            }
            else
            {
                onCompletedAutoSyncCampaignsList();
            }

        }catch (Exception E)
        {
            onCompletedAutoSyncCampaignsList();
        }
    }


    @Override
    public void stopService(Bundle values) {

        stopGetCampaignListService();

        if (DownloadCampaignsService.isServiceOn) {

            DownloadCampaignsService.downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.INTERRUPT_SERVICE, null);
        }

        stopSelf();
    }

    @Override
    public void downloadCampaignsInfoResponse(Bundle values) {

        onCompletedAutoSyncCampaignsList();
    }


    private void onCompleted()
    {
        AutoDownloadCampaignModel.checkRestartAutoCampaignDownloadService(context);
        stopSelf();
    }

    //stop RequiredFiles Service
    public void stopGetCampaignListService() {
        Intent intent = new Intent(AutoCampDownloadListService.STOP_SERVICE_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(intent);
    }

    private void deleteUnknownCampaigns()
    {

        Cursor garbageCampaigns=CampaignsDBModel.getGarbageCampaigns(TextUtils.join(", ", restoringServerIds),context);

        if (garbageCampaigns != null && garbageCampaigns.moveToFirst()) {
            do
            {

                String campaignName=garbageCampaigns.getString(garbageCampaigns.getColumnIndex(CAMPAIGNS_TABLE_CAMPAIGN_NAME));



                File garbageCampaignFile=new File(DOWNLOAD_CAMPAIGNS_PATH,campaignName+".txt");

                removeCampaignResources(campaignName,garbageCampaignFile);


            } while (garbageCampaigns.moveToNext());


        }


      CampaignsDBModel.deleteGarbageCampaigns(TextUtils.join(", ", restoringServerIds),context);

    }

    private void removeCampaignResources(String fileName,File file)
    {
        ArrayList<String> dataList=new ArrayList<>();

        if(file.exists())
        {
            File thumbFile=new File(DOWNLOAD_CAMPAIGNS_PATH+File.separator+context.getString(R.string.do_not_display_media)+"-"+context.getString(R.string.media_thumbnail)+"-"+fileName+".jpg");

            if(thumbFile.exists())
            {
                //  Log.d("PreviewCampaign","deleted thumbFile is :"+thumbFile.getName());
                thumbFile.delete();
            }

            String fileJson = new MediaModel().readTextFile(file.getPath());

            try
            {
                JSONObject jsonObject = new JSONObject(fileJson);
                String type = jsonObject.getString("type");

                if(type.equalsIgnoreCase(context.getString(R.string.app_default_image_name)))
                {
                    dataList.add(file.getName());
                    dataList.add(jsonObject.getString("resource"));

                }else if(type.equalsIgnoreCase(context.getString(R.string.app_default_video_name)))
                {
                    dataList.add(file.getName());
                    dataList.add(jsonObject.getString("resource"));

                }else if(type.equalsIgnoreCase(context.getString(R.string.app_default_multi_region)))
                {
                    dataList= processMultiRegionFile(jsonObject.getJSONArray("regions"));
                    dataList.add(file.getName());

                }else if(type.equalsIgnoreCase(context.getString(R.string.url_txt)))
                {
                    dataList.add(file.getName());
                }

                if(dataList!=null && dataList.size()>0)
                {
                    // Log.d("PreviewCampaign","deleted dataList:"+dataList.toString());

                    for(String fileString:dataList)
                    {
                        //  Log.d("PreviewCampaign","deleted campaign file:"+fileString);
                        File resourceFile=new File(CampaignModel.getAdsKiteNearByDirectory(context)+File.separator+fileString);
                        if(resourceFile.exists())
                        {

                            resourceFile.delete();
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



    private void updateTickerTextSettings()
    {
        Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(context.getString(R.string.action),context.getString(R.string.update_scroll_text_action));

        context.sendBroadcast(intent);
    }

    private void prepareListForDownload() throws Exception
    {

            Cursor cursor = CampaignsDBModel.getCampaignsToDownload(context);
            ArrayList<GCModel> pendingDownloadCampaigns = new ArrayList<>();
            if(cursor!=null && cursor.moveToFirst())
            {
                do {
                    String campaignName = cursor.getString(cursor.getColumnIndex(CAMPAIGNS_TABLE_CAMPAIGN_NAME));
                    GCModel gcModel=new GCModel();

                    //gcModel.setCreatedAt(campObject.getString("created_date"));
                    gcModel.setCreatedAt(cursor.getString(cursor.getColumnIndex(CAMPAIGNS_TABLE_CREATED_DATE)));
                    gcModel.setUpdatedAt(cursor.getString(cursor.getColumnIndex(CAMPAIGNS_TABLE_UPDATED_DATE)));
                    gcModel.setStoreLocation(cursor.getInt(cursor.getColumnIndex(CAMPAIGNS_TABLE_STOR_LOCATION)));
                    gcModel.setInfo(cursor.getString(cursor.getColumnIndex(CAMPAIGN_TABLE_CAMPAIGN_INFO)));
                    gcModel.setCampaignName(campaignName);
                    gcModel.setSavePath(cursor.getString(cursor.getColumnIndex(CAMPAIGNS_TABLE_SAVE_PATH)));
                    gcModel.setServerId(cursor.getLong(cursor.getColumnIndex(CAMPAIGNS_TABLE_SERVER_ID)));
                    gcModel.setCampaignSize(cursor.getLong(cursor.getColumnIndex(CAMPAIGNS_TABLE_CAMP_SIZE)));
                    gcModel.setCampaignType(cursor.getInt(cursor.getColumnIndex(CAMPAIGNS_TABLE_CAMP_TYPE)));

                    gcModel.setCampaignUploadedBy(cursor.getLong(cursor.getColumnIndex(CAMPAIGNS_TABLE_UPLOADED_BY)));
                    gcModel.setSource(cursor.getInt(cursor.getColumnIndex(CAMPAIGNS_TABLE_SOURCE)));
                    gcModel.setIsSkip(cursor.getInt(cursor.getColumnIndex(CAMPAIGNS_TABLE_IS_SKIP)));
                    gcModel.setScheduleType(cursor.getInt(cursor.getColumnIndex(CAMPAIGN_TABLE_SCHEDULE_TYPE)));
                    pendingDownloadCampaigns.add(gcModel);

                }while(cursor.moveToNext());
            }

        if (pendingDownloadCampaigns.size() > 0)
        {
            Intent intent = new Intent(context, DownloadCampaignsService.class);
            intent.putExtra("campList", pendingDownloadCampaigns);
            intent.putExtra("is_auto_sync", true);
            ContextCompat.startForegroundService(context, intent);
        }
        else
        {
            onCompletedAutoSyncCampaignsList();
        }


    }

    //auto campaign list has been downloaded
    private void onCompletedAutoSyncCampaignsList()
    {
      startService(new Intent(context, SyncRulesService.class));
    }

    @Override
    public void syncRulesApiError(Bundle resultData) {


        onCompletedSyncRulesList();

    }

    @Override
    public void syncRulesApiSuccess(Bundle resultData)
    {
        Log.d("SyncRulesService","Inside SyncRules syncRulesApiSuccess ");
        onCompletedSyncRulesList();
    }

    private void onCompletedSyncRulesList()
    {
        //stop service
        onCompleted();
    }
}
