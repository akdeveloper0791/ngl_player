package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignMediaTable;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DeleteUnknownCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.FetchBasicCampInfoService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.RSSModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.TickerTextModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver.DOWNLOAD_LIST_API_ERROR;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver.DOWNLOAD_LIST_CAMPAIGN_SUCCESS;


public class AutoCampDownloadListService extends IntentService
{
    private Context context;
    private StopServiceReceiver stopServiceReceiver;
    public final static String STOP_SERVICE_ACTION="com.ibetter.www.adskitedigi.adskitedigi.required_schedule_files.downloadCampaign.auto_download_campaign.STOP_SERVICE";
    private boolean isStopped = false;
    public AutoCampDownloadListService()
    {
        super(FetchBasicCampInfoService.class.getName());
    }


    private HashMap<Long,GCModel> serverCampaigns = new HashMap<>();
    private ArrayMap<Long,ScheduleCampaignModel> schedules = new ArrayMap<>();
    private HashMap<Long, RSSModel> serverRSSFeeds = new HashMap<>();
    private HashMap<Long, TickerTextModel> serverTickers = new HashMap<>();
    private HashMap<Long,ArrayList<String>> campaignMedias = new HashMap<>();


    @Override
    public void onHandleIntent(@Nullable Intent intent)
    {
        isStopped=false;

        context=AutoCampDownloadListService.this;
        registerStopServiceReceiver();
        getCampaignList();
    }

    private void getCampaignList()
    {
        String url=getCampaignsDownloadURL();

        if(url!=null)
        {
            try {

                // OkHttpClient httpClient = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("secretKey",new User().getGCUserUniqueKey(context))
                        .addFormDataPart("player",String.valueOf(new User().getPlayerId(context)))
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()
                        ;


                OkHttpClient httpClient = new OkHttpClient.Builder()
                        .connectTimeout(1, TimeUnit.MINUTES)
                        .writeTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.MINUTES)
                        .build();


                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        sendFailedResponse(false,e.toString(),0);



                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString=response.body().string().trim();

                        if (response.isSuccessful()) {
                            processResponse(responseString);
                        }else
                        {
                            sendFailedResponse(false,"Unable to contact the server, please check your connections",0);
                        }
                    }


                });

            }

            catch (Exception e)
            {
                e.printStackTrace();
                sendFailedResponse(false,e.toString(),0);
            }

        }else
        {
            sendFailedResponse(false,"Invalid User Id, Please login and try again.",0);
        }
    }

    private synchronized void processResponse(String response)
    {
        Log.i("response",response);
        try
        {
            JSONObject responseObject=new JSONObject(response);
            int statusCode=responseObject.getInt("statusCode");



            if(statusCode==0)
            {


                JSONArray campArray=responseObject.getJSONArray("campaigns");
                if(campArray!=null&& campArray.length()>0)
                {
                    boolean isTickerActivate=false,isTickerTextUpdated=false;

                    for(int i=0;i<campArray.length();i++)
                    {

                        JSONObject campObject=campArray.getJSONObject(i);

                        long serverId = campObject.getLong("id");
                        int campaignType = campObject.getInt("camp_type");
                        int scheduleType = ScheduleCampaignModel.CONTINUOUS_PLAY;
                        int isSkip = 0,priority=0;

                        if(Constants.convertToLong(campObject.getString("pc_id"))>0)
                        {
                            scheduleType = campObject.getInt("schedule_type");
                            isSkip = campObject.getInt("is_skip");
                            priority = campObject.getInt("pc_priority");
                        }else if(Constants.convertToLong(campObject.getString("dgc_id"))>0)
                        {
                            scheduleType = campObject.getInt("dgc_schedule_type");
                            isSkip = campObject.getInt("dgc_is_skip");
                            priority = campObject.getInt("dgc_priority");
                        }

                        if(campaignType==2)
                        {
                           if(!serverRSSFeeds.containsKey(serverId))
                           {
                               serverRSSFeeds.put(serverId,new RSSModel(serverId,isSkip,
                                       campObject.getString("info"),scheduleType));
                           }
                        }
                        if(campaignType==3)
                        {
                            if(!serverTickers.containsKey(serverId))
                            {
                                serverTickers.put(serverId,new TickerTextModel(serverId,isSkip,
                                        campObject.getString("info"),scheduleType));
                            }
                        }else {


                            if (!serverCampaigns.containsKey(serverId)) {
                                String campaignName = campObject.getString("campaign_name");
                                GCModel gcModel = new GCModel();
                                gcModel.setCampaignFile(campObject.getString("text_file"));
                                //gcModel.setCreatedAt(campObject.getString("created_date"));
                                gcModel.setCreatedAt(campObject.getString("created_date"));
                                gcModel.setStoreLocation(campObject.getInt("stor_location"));
                                gcModel.setInfo(campObject.getString("info"));
                                gcModel.setCampaignName(campaignName);
                                gcModel.setSavePath(campObject.getString("save_path"));
                                gcModel.setServerId(serverId);
                                gcModel.setCampaignSize(campObject.getLong("campaign_size"));
                                gcModel.setCampaignType(campaignType);
                                gcModel.setUpdatedAt(campObject.getString("updated_date"));
                                gcModel.setCampaignUploadedBy(campObject.getLong("campaign_uploaded_by"));
                                gcModel.setSource(campObject.getInt("source"));
                                gcModel.setIsSkip(isSkip);
                                gcModel.setScheduleType(scheduleType);
                                gcModel.setCampaignPriority(priority);

                                serverCampaigns.put(serverId, gcModel);

                                prepareCampaignMedias(serverId,campObject.getString("info"));

                            }
                        }

                        String sIdString=campObject.getString("sc_id");
                        //check and save schedules, save schedules whose server id is not null
                        long scId=0;
                        if(sIdString!=null&&!sIdString.equalsIgnoreCase("null")) {
                             scId = Constants.convertToLong(sIdString);
                        }
                        if(scId>0)
                        {
                            //valid schedule let save this
                            ScheduleCampaignModel scModel = new ScheduleCampaignModel(scId,serverId,
                                    campObject.getString("schedule_from"),campObject.getString("schedule_to"),
                                    campObject.getInt("sc_schedule_type"),campObject.getString("additional_info"));
                            scModel.setScPriority(campObject.getInt("sc_priority"));

                            if(scModel.getScheduleFrom()!=null && scModel.getScheduleTo()!=null)
                            {
                                schedules.put(scId,scModel);
                            }
                        }
                    }

                    //check and update rss feeds

                        //process rss feeds data
                        processRSSFeedsData();

                        processTickerTextData();

                    if(serverCampaigns!=null && serverCampaigns.size()>0)
                    {
                        //check and update ticker text

                        processCampaignsData();
                    }else
                    {
                        noCampaignsFound();
                    }

                    processMediaCampaignsData();

                }else
                {
                    noCampaignsFound();
                }

            }else if(statusCode==2)
            {
                //no campaigns found
                noCampaignsFound();
            }
            else
            {
                sendFailedResponse(false,responseObject.getString("status"),statusCode);

            }

        }catch (Exception e)
        {
            e.printStackTrace();
            sendFailedResponse(false,"Unable to get the response, Please try again.",0);
        }
    }


    private String getCampaignsDownloadURL()
    {
        int mode=new User().getUserPlayingMode(context);
        String userId=new User().getGCUserUniqueKey(context);


        if(mode== Constants.CLOUD_MODE)
        {
            if(userId!=null)
            {
                return GCUtils.GET_SCHEDULE_CAMPAIGNS_URL;
            }else
            {
                return null;
            }
        }else
        if(mode== Constants.ENTERPRISE_MODE)
        {
            if(userId!=null)
            {
                return new User().getEnterPriseURL(context)+GCUtils.ENTERPRISE_SCHEDULE_CAMPAIGNS_URL;
            }else
            {
                return null;
            }
        }
        else
        {
            return null;
        }

    }

    private void sendSuccessResponse()
    {
        Log.d("Sync","Inside auto camp download list service  sendSuccessResponse"+isStopped);
        if(!isStopped) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("flag", true);

            AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver.send(DOWNLOAD_LIST_CAMPAIGN_SUCCESS, bundle);
        }
    }

    private void sendFailedResponse(boolean flag,String status,int statusCode)
    {
        if(!isStopped)
        {
            Bundle bundle = new Bundle();
            bundle.putBoolean("flag", flag);
            bundle.putString("status", status);
            bundle.putInt("statusCode", statusCode);
            AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver.send(DOWNLOAD_LIST_API_ERROR, bundle);
        }
    }

    @Override
    public void onDestroy()
    {
        unRegisterStopServiceReceiver();

        Log.d("DownloadCampaign","prepare campaign media total medias size"+campaignMedias.size());
        super.onDestroy();
    }

    //register stop service receiver
    private void registerStopServiceReceiver()
    {
        IntentFilter intentFilter=new IntentFilter(STOP_SERVICE_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        stopServiceReceiver=new StopServiceReceiver();
        registerReceiver(stopServiceReceiver, intentFilter);

    }

    //stop service receiver
    private class StopServiceReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context,Intent intent)
        {
            isStopped=true;
            //stop executing
            stopSelf();


        }
    }

    //un register StopServiceReceiver
    private void unRegisterStopServiceReceiver()
    {
        try
        {
            unregisterReceiver(stopServiceReceiver);
        }catch(Exception e)
        {

        }
    }

    private synchronized void processCampaignsData()
    {
        //prepare data to insert and update in bulk
        ArrayList<GCModel> updateData = new ArrayList<>();
        HashMap<Long,GCModel> insertData =(HashMap)serverCampaigns.clone();


        try
        {


                //check campaign record to update or insert to the local database
                Cursor campaignsCursor=CampaignsDBModel.getServerIdsList(TextUtils.join(", ",serverCampaigns.keySet()),context);

                if (campaignsCursor != null && campaignsCursor.moveToFirst())
                {
                    do {


                        long serverId = campaignsCursor.getLong(campaignsCursor.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID));
                        if (serverId >0)
                        {
                           //insert to update list and remove from
                            updateData.add(insertData.remove(serverId));
                        }

                    } while (campaignsCursor.moveToNext());

                }

        }catch (Exception e)
        {
            e.printStackTrace();

        }finally {
           //do bulk insert
            if(insertData.size()>0)
            {
                bulkCampaignsInsert(insertData);

            }

            //do bulk update
            if(updateData.size()>0)
            {
                bulkCampaignsUpdate(updateData);
            }

            deleteUnknownSchedules();

            checkAndInsertNewSchedules();


            //check and remove unknown campaigns
            deleteUnknownCampaigns();

            //send success response
            sendSuccessResponse();
        }

    }

    private synchronized void  bulkCampaignsInsert(HashMap<Long,GCModel> campHashMap)
   {
     SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

     try {

        mDb.beginTransaction();

        String insetQuary = "INSERT INTO " + CampaignsDBModel.CAMPAIGNS_TABLE + "(" +
                CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID + ","
                + CampaignsDBModel.CAMPAIGNS_TABLE_UPLOADED_BY + ","
                + CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME +"," +
                CampaignsDBModel.CAMPAIGNS_TABLE_CREATED_DATE + "," +
                CampaignsDBModel.CAMPAIGNS_TABLE_UPDATED_DATE + "," +
                CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP +"," +
                CampaignsDBModel.CAMPAIGNS_TABLE_CAMP_TYPE + "," +
                CampaignsDBModel.CAMPAIGNS_TABLE_STOR_LOCATION + "," +
                CampaignsDBModel.CAMPAIGNS_TABLE_CAMP_SIZE +"," +
                CampaignsDBModel.CAMPAIGNS_TABLE_SAVE_PATH +"," +
                CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO +"," +
                CampaignsDBModel.CAMPAIGNS_TABLE_SOURCE+","+
                CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE+"," +
                CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_PRIORITY+") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


        SQLiteStatement insert = mDb.compileStatement(insetQuary);

        Iterator it = campHashMap.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            GCModel gcModel = (GCModel) pair.getValue();


            insert.bindLong(1, gcModel.getServerId());
            insert.bindLong(2, gcModel.getCampaignUploadedBy());
            insert.bindString(3, gcModel.getCampaignName());
            insert.bindString(4, gcModel.getCreatedAt());
            insert.bindString(5, gcModel.getUpdatedAt());
            insert.bindLong(6, gcModel.getIsSkip());
            insert.bindLong(7, gcModel.getCampaignType());
            insert.bindLong(8, gcModel.getStoreLocation());
            insert.bindLong(9,gcModel.getCampaignSize());
            insert.bindString(10,gcModel.getSavePath());
            insert.bindString(11, gcModel.getInfo());
            insert.bindLong(12, gcModel.getSource());
            insert.bindLong(13, gcModel.getScheduleType());
            insert.bindLong(14, gcModel.getCampaignPriority());
            insert.execute();

        }


    }
    catch (Exception e)
    {

        Log.w("XML:",e );
    }
    finally
    {
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        campHashMap.clear();
    }

}

private synchronized void bulkCampaignsUpdate(ArrayList<GCModel> updateData)
{

    SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

    try {

        mDb.beginTransaction();

        String updateQuary="UPDATE " + CampaignsDBModel.CAMPAIGNS_TABLE + " SET " +
                CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID + " =?, " +
                CampaignsDBModel.CAMPAIGNS_TABLE_UPLOADED_BY +" =?,"+
                CampaignsDBModel.CAMPAIGNS_TABLE_UPDATED_DATE +" =?,"+
                CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP  +" =? ," +
                CampaignsDBModel.CAMPAIGNS_TABLE_CAMP_TYPE + " =?, " +
                CampaignsDBModel.CAMPAIGNS_TABLE_STOR_LOCATION +" =?,"+
                CampaignsDBModel.CAMPAIGNS_TABLE_CAMP_SIZE  +" =? ," +
                CampaignsDBModel.CAMPAIGNS_TABLE_SOURCE + " =?, " +
                CampaignsDBModel.CAMPAIGNS_TABLE_SAVE_PATH +" =?,"+
                CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO  +" =?," +
                CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE  +" =?, " +
                CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_PRIORITY +"=?"+
                "WHERE " + CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID + " =?";

        SQLiteStatement update = mDb.compileStatement(updateQuary);

       for(GCModel gcModel:updateData)
        {

            update.bindLong(1, gcModel.getServerId());
            update.bindLong(2, gcModel.getCampaignUploadedBy());
            update.bindString(3, gcModel.getUpdatedAt());
            update.bindLong(4, gcModel.getIsSkip());
            update.bindLong(5, gcModel.getCampaignType());
            update.bindLong(6, gcModel.getStoreLocation());
            update.bindLong(7,gcModel.getCampaignSize());
            update.bindLong(8,gcModel.getSource());
            update.bindString(9, gcModel.getSavePath());
            update.bindString(10, gcModel.getInfo());
            update.bindLong(11, gcModel.getScheduleType());
            update.bindLong(12, gcModel.getCampaignPriority());
            update.bindLong(13, gcModel.getServerId());


            update.execute();


        }
    }
    catch (Exception e)
    {
        Log.w("XML:",e );
    }
    finally
    {
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        updateData.clear();
    }

}

    private synchronized void  checkAndInsertNewSchedules()
    {
        try {
            //check campaign record to update or insert to the local database
            Cursor campaignsCursor = CampaignsDBModel.getSavedSchedules(TextUtils.join(", ", schedules.keySet()), context);

            if (campaignsCursor != null && campaignsCursor.moveToFirst()) {
                do {


                    long serverId = campaignsCursor.getLong(campaignsCursor.getColumnIndex(CampaignsDBModel.SCHEDULE_CAMPAIGNS_SERVER_ID));
                    if (serverId > 0) {
                        //insert to update list and remove from
                        schedules.remove(serverId);
                    }

                } while (campaignsCursor.moveToNext());

            }

            //after removing duplicate items do bulk insert schedules
            bulkInsertSchedules();
        }catch(Exception e)
        {

        }finally
        {

        }

    }

    private void bulkInsertSchedules()
    {
        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {

            mDb.beginTransaction();

            String insertQuery = "INSERT INTO " + CampaignsDBModel.SCHEDULE_CAMPAIGNS_TABLE + "(" +
                    CampaignsDBModel.SCHEDULE_CAMPAIGNS_CS_ID + ","
                    + CampaignsDBModel.SCHEDULE_CAMPAIGNS_SERVER_ID + ","
                    + CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_FROM +"," +
                    CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_TO+"," +
                    CampaignsDBModel.SCHEDULE_TABLE_SCHEDULE_TYPE+","+
                    CampaignsDBModel.SCHEDULE_TABLE_SCHEDULE_PRIORITY+","+
                    CampaignsDBModel.SCHEDULE_TABLE_ADDITIONAL_INFO+") VALUES(?,?,?,?,?,?,?)";


            SQLiteStatement insert = mDb.compileStatement(insertQuery);

            Iterator it = schedules.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                ScheduleCampaignModel scModel = (ScheduleCampaignModel) pair.getValue();




                insert.bindLong(1, scModel.getCampaignServerId());
                insert.bindLong(2, scModel.getScServerId());
                insert.bindString(3, scModel.getScheduleFrom());
                insert.bindString(4, scModel.getScheduleTo());
                insert.bindLong(5, scModel.getScheduleType());
                insert.bindLong(6, scModel.getScPriority());
                insert.bindString(7, scModel.getAdditionalInfo());
                insert.execute();

                //it.remove(); // avoids a ConcurrentModificationException
            }


        }
        catch (Exception e)
        {

            Log.w("XML:",e );
        }
        finally
        {
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            schedules.clear();
        }
    }



    private void deleteUnknownSchedules()
    {
        try
        {
           CampaignsDBModel.deleteGarbageSchedules(TextUtils.join(", ", schedules.keySet()), context);

        }catch (Exception e)
        {
            Log.d("auto download campaigns", "Error in deleteUnknownSchedules" + e.getMessage());
        }
    }

    private void deleteUnknownCampaigns()
    {
        try {
            ArrayList<GCModel> deletedCampaigns = new ArrayList<>();
            Cursor garbageCampaigns = CampaignsDBModel.getGarbageCampaigns(TextUtils.join(", ", serverCampaigns.keySet()), context);

            if (garbageCampaigns != null && garbageCampaigns.moveToFirst()) {
                do {
                    GCModel gcModel = new GCModel();
                    gcModel.setCampaignName(garbageCampaigns.getString(garbageCampaigns.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME)));
                    gcModel.setInfo(garbageCampaigns.getString(garbageCampaigns.getColumnIndex(CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO)));
                    gcModel.setCampaignLocalId(garbageCampaigns.getLong(garbageCampaigns.getColumnIndex(CampaignsDBModel.LOCAL_ID)));
                    gcModel.setServerId(garbageCampaigns.getLong(garbageCampaigns.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID)));
                    deletedCampaigns.add(gcModel);

                    if (deletedCampaigns.size() >= 100) {
                        //start service
                        Intent intent = new Intent(context, DeleteUnknownCampaigns.class);
                        intent.putExtra("unknown_campaigns", deletedCampaigns);
                        startService(intent);

                        deletedCampaigns.clear();
                    }

                } while (garbageCampaigns.moveToNext());


                if (deletedCampaigns.size() >= 1) {

                    //start service
                    Intent intent = new Intent(context, DeleteUnknownCampaigns.class);
                    intent.putExtra("unknown_campaigns", deletedCampaigns);
                    startService(intent);

                    deletedCampaigns.clear();
                }

            }

            CampaignsDBModel.deleteGarbageCampaigns(TextUtils.join(", ", serverCampaigns.keySet()), context);

        }catch(Exception e)
        {
            Log.d("auto download campaigns","Error in deleting unknown campaigns"+e.getMessage());
        }
    }



    private void checkAndUpdateTickerText() {

        startService(new Intent(context, UpdateTickerTextService.class));
    }



    //handle no campaigns found
    private void noCampaignsFound()
    {

        //delete unknown campaigns
        deleteUnknownCampaigns();

        //delete unknown rss feeds
        deleteUnknownFeeds();

        deleteUnknownTickers();

        //update ticker texe
        checkAndUpdateTickerText();

        //send success response
        sendSuccessResponse();

        processMediaCampaignsData();
    }

    //process rss feeds data
    private void processRSSFeedsData()
    {
        deleteUnknownFeeds();
        if(serverRSSFeeds.size()>=1)
        {
            checkAndInsertNewFeeds();
        }
    }


    //process rss feeds data
    private void processTickerTextData()
    {
        deleteUnknownTickers();
        if(serverTickers.size()>=1)
        {

            checkAndInsertNewTickerTexts();

        }else
        {
            checkAndUpdateTickerText();
        }
    }

    private synchronized void  checkAndInsertNewTickerTexts()
    {
        ArrayList<TickerTextModel> updateData = new ArrayList<>();
        try {
            //check campaign record to update or insert to the local database
            Cursor campaignsCursor = CampaignsDBModel.getSavedTickerTexts(TextUtils.join(", ", serverTickers.keySet()), context);

            if (campaignsCursor != null && campaignsCursor.moveToFirst()) {
                do {
                    long serverId = campaignsCursor.getLong(campaignsCursor.getColumnIndex(CampaignsDBModel.TICKER_TEXT_SERVER_ID));
                    if (serverId > 0) {
                        //insert to update list and remove from
                        updateData.add(serverTickers.remove(serverId));
                    }

                } while (campaignsCursor.moveToNext());

            }

            //after removing duplicate items do bulk insert schedules
            if(serverTickers.size()>=1)
            {
                bulkInsertTickerTexts();
            }

            if(updateData.size()>=1)
            {
                bulkTickerTextsUpdate(updateData);
            }

        }catch(Exception e)
        {

        }finally
        {
            checkAndUpdateTickerText();

        }

    }
    private void bulkInsertTickerTexts()
    {
        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {

            mDb.beginTransaction();

            String insertQuery = "INSERT INTO " + CampaignsDBModel.TICKER_TEXT_TABLE + "(" +
                    CampaignsDBModel.TICKER_TEXT_SERVER_ID + ","
                    + CampaignsDBModel.TICKER_TEXT_IS_SKIP + ","
                    + CampaignsDBModel.TICKER_TEXT_INFO+","
                    + CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE + ") VALUES(?,?,?,?)";


            SQLiteStatement insert = mDb.compileStatement(insertQuery);

            Iterator it = serverTickers.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                TickerTextModel tickerTextModel = (TickerTextModel) pair.getValue();

                insert.bindLong(1, tickerTextModel.getServerId());
                insert.bindLong(2, tickerTextModel.getIsSkip());
                insert.bindString(3, tickerTextModel.getInfo());
                insert.bindLong(4,tickerTextModel.getScheduleType());

                insert.execute();

                //it.remove(); // avoids a ConcurrentModificationException
            }


        }
        catch (Exception e)
        {

            Log.w("XML:",e );
        }
        finally
        {
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            serverTickers.clear();
        }
    }

    private synchronized void bulkTickerTextsUpdate(ArrayList<TickerTextModel> updateData)
    {

        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {
            mDb.beginTransaction();

            String updateQuary="UPDATE " + CampaignsDBModel.TICKER_TEXT_TABLE + " SET " +
                    CampaignsDBModel.TICKER_TEXT_IS_SKIP + " =?, " +
                    CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE +" =?"+
                    "WHERE " + CampaignsDBModel.TICKER_TEXT_SERVER_ID + " =?";

            SQLiteStatement update = mDb.compileStatement(updateQuary);

            for(TickerTextModel tickerTextModel:updateData)
            {

                update.bindLong(1, tickerTextModel.getIsSkip());
                update.bindLong(2, tickerTextModel.getScheduleType());
                update.bindLong(3, tickerTextModel.getServerId());


                update.execute();


            }
        }
        catch (Exception e)
        {
            Log.w("XML:",e );
        }
        finally
        {
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            updateData.clear();
        }

    }


    private void deleteUnknownTickers()
    {
        try {
            CampaignsDBModel.deleteGarbageTickers(TextUtils.join(", ", serverTickers.keySet()), context);
        }catch(Exception e)
        {
            Log.d("auto download campaigns","Error in deleting unknown TICKERS"+e.getMessage());
        }
    }






    private synchronized void  checkAndInsertNewFeeds()
    {
        ArrayList<RSSModel> updateData = new ArrayList<>();
        try {
            //check campaign record to update or insert to the local database
            Cursor campaignsCursor = CampaignsDBModel.getSavedFeeds(TextUtils.join(", ", serverRSSFeeds.keySet()), context);

            if (campaignsCursor != null && campaignsCursor.moveToFirst()) {
                do {


                    long serverId = campaignsCursor.getLong(campaignsCursor.getColumnIndex(CampaignsDBModel.RSS_FEED_CAMPAIGN_SERVER_ID));
                    if (serverId > 0) {
                        //insert to update list and remove from
                        updateData.add(serverRSSFeeds.remove(serverId));
                    }

                } while (campaignsCursor.moveToNext());

            }

            //after removing duplicate items do bulk insert schedules
            if(serverRSSFeeds.size()>=1)
            {
                bulkInsertFeeds();
            }

            if(updateData.size()>=1)
            {
                bulkFeedsUpdate(updateData);
            }

        }catch(Exception e)
        {

        }finally
        {

        }

    }

    private void bulkInsertFeeds()
    {
        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {

            mDb.beginTransaction();

            String insertQuery = "INSERT INTO " + CampaignsDBModel.RSS_FEEDS_TABLE + "(" +
                    CampaignsDBModel.RSS_FEED_CAMPAIGN_SERVER_ID + ","
                    + CampaignsDBModel.RSS_FEED_IS_SKIP + ","
                    + CampaignsDBModel.RSS_FEED_INFO+","
                    + CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE + ") VALUES(?,?,?,?)";


            SQLiteStatement insert = mDb.compileStatement(insertQuery);

            Iterator it = serverRSSFeeds.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                RSSModel rssModel = (RSSModel) pair.getValue();

                insert.bindLong(1, rssModel.getServerId());
                insert.bindLong(2, rssModel.getIsSkip());
                insert.bindString(3, rssModel.getInfo());
                insert.bindLong(4,rssModel.getScheduleType());

                insert.execute();

                //it.remove(); // avoids a ConcurrentModificationException
            }


        }
        catch (Exception e)
        {

            Log.w("XML:",e );
        }
        finally
        {
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            serverRSSFeeds.clear();
        }
    }

    private synchronized void bulkFeedsUpdate(ArrayList<RSSModel> updateData)
    {

        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {
            mDb.beginTransaction();

            String updateQuary="UPDATE " + CampaignsDBModel.RSS_FEEDS_TABLE + " SET " +
                    CampaignsDBModel.RSS_FEED_IS_SKIP + " =?, " +
                    CampaignsDBModel.SCHEDULE_TABLE_SCHEDULE_TYPE +" =?"+
                    "WHERE " + CampaignsDBModel.RSS_FEED_CAMPAIGN_SERVER_ID + " =?";

            SQLiteStatement update = mDb.compileStatement(updateQuary);

            for(RSSModel rssModel:updateData)
            {

                update.bindLong(1, rssModel.getIsSkip());
                update.bindLong(2, rssModel.getScheduleType());
                update.bindLong(3, rssModel.getServerId());


                update.execute();


            }
        }
        catch (Exception e)
        {
            Log.w("XML:",e );
        }
        finally
        {
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            updateData.clear();
        }

    }


    private void deleteUnknownFeeds()
    {
        try {
            CampaignsDBModel.deleteGarbageFeeds(TextUtils.join(", ", serverRSSFeeds.keySet()), context);
        }catch(Exception e)
        {
            Log.d("auto download campaigns","Error in deleting unknown Feeds"+e.getMessage());
        }
    }

    private void prepareCampaignMedias(long serverId,String infoText) {

        Log.d("DownloadCampaign","info text is "+infoText);

        if (infoText != null) {
            //prepare upload files from json
            try {
                JSONObject infoObj = new JSONObject(infoText);
                String type = infoObj.getString("type");
                ArrayList<String> medias = new ArrayList<>();

                if (type.equalsIgnoreCase("multi_region")) {
                    processMultiRegFilesToUpload(infoObj.getJSONArray("regions"),medias);
                } else {
                    addForRegion(infoObj, "Single",medias);
                }

                //check for bg audio file
                if (infoObj.has("bg_audio")) {
                    medias.add(infoObj.getString("bg_audio"));

                }

                campaignMedias.put(serverId,medias);
                Log.d("DownloadCampaign","prepare campaign media total medias"+campaignMedias);

                Log.d("DownloadCampaign","prepare campaign media total medias size"+campaignMedias.size());
                //start Upload campaignFile
                //initUpload(infoText);
            } catch (JSONException e) {
                Log.d("DownloadCampaign","prepare campaign media error "+e.getMessage());
              e.printStackTrace();

            }
        }
    }

    //process multi region files
    private void processMultiRegFilesToUpload(JSONArray regions,ArrayList medias) throws JSONException {
        for (int i = 0; i < regions.length(); i++) {
            addForRegion(regions.getJSONObject(i), "multi",medias);
        }
    }

    private void addForRegion(JSONObject region, String type,ArrayList medias) throws JSONException {
        if (!region.getString("type").equalsIgnoreCase("text") && !region.getString("type").equalsIgnoreCase("Url")
                && !region.getString("type").equalsIgnoreCase("default")) {
            if (type.equalsIgnoreCase("Single")) {
                if(region.has("is_content_path") && region.getBoolean("is_content_path"))
                {
                    medias.add(region.getString("media_name"));

                }else
                {
                    medias.add(region.getString("resource"));

                }

            } else {
                medias.add(region.getString("media_name"));
            }

        }
    }

    private void processMediaCampaignsData(){
        CampaignMediaTable.clearTable(context);

        //check and insert  bulk data
        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {

            mDb.beginTransaction();

            String insertQuery = "INSERT INTO " + CampaignMediaTable.CAMPAIGN_MEDIA_TABLE + "(" +
                    CampaignMediaTable.CAMPAIGN_MEDIA_TABLE_CAMP_ID + ","
                    + CampaignMediaTable.CAMPAIGN_MEDIA_TABLE_MEDIA_NAME +") VALUES(?,?)";


            SQLiteStatement insert = mDb.compileStatement(insertQuery);

            Iterator it = campaignMedias.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                long campaignServerId = (long)pair.getKey();
                ArrayList<String> medias = (ArrayList)pair.getValue();
                for(String mediaName:medias)
                {
                    insert.bindLong(1,campaignServerId);
                    insert.bindString(2,mediaName);
                    insert.execute();
                }

            }


        }
        catch (Exception e)
        {

            Log.w("XML:",e );
        }
        finally
        {
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            campaignMedias.clear();
        }
    }
}
