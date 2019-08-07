package com.ibetter.www.adskitedigi.adskitedigi.green_content.contextual_ads;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignTriggerService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.CARCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.ContextualAdRule;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
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

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver.SYNC_RULES_API_ERROR;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver.SYNC_RULES_API_SUCCESS;

public class SyncRulesService extends IntentService {
   public static String STOP_SERVICE = "com.ibetter.www.adskitedigi.adskitedigi.green_content.contextual_ads.SyncRulesService." +
           "StopSyncRulesReceiver";
   private Context context;
   private StopSyncRulesReceiver stopSyncRulesReceiver;
   private boolean isStopped = false;
   private HashMap<Long,ContextualAdRule> serverRules = new HashMap<>();
    private ArrayMap<Long, CARCampaigns> serverCampaigns = new ArrayMap<>();

    public SyncRulesService()
    {
        super("SyncRulesService");
        context = SyncRulesService.this;
    }

    private void registerStopServiceReceiver()
    {
        IntentFilter intentFilter = new IntentFilter(STOP_SERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if(stopSyncRulesReceiver==null)
        {
            stopSyncRulesReceiver = new StopSyncRulesReceiver();
            registerReceiver(stopSyncRulesReceiver,intentFilter);
        }
    }

    protected void onHandleIntent(Intent intent)
    {
        if(!isStopped)
        {

            registerStopServiceReceiver();

            String url=getURL();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("accessToken",new User().getGCUserUniqueKey(context))
                    .addFormDataPart("p_mac",new User().getPlayerMac(context))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if(!isStopped)
                    {
                        e.printStackTrace();
                        sendFailedResponse(false,e.toString(),0);
                    }
                }

                @Override
                public void onResponse(Call call, Response response)throws IOException {
                    String responseString=response.body().string().trim();

                    if (response.isSuccessful()) {
                       // processResponse(responseString);
                        processResponse(responseString);
                        //sendSuccessResponse();
                    }else
                    {
                        sendFailedResponse(false,"Unable to contact the server, please check your connections",0);
                    }

                }
            });

        }
    }

    private void sendFailedResponse(boolean flag,String status,int statusCode)
    {
        if(!isStopped&& AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver!=null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("flag", flag);
            bundle.putString("status", status);
            bundle.putInt("statusCode", statusCode);
            AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver.send(SYNC_RULES_API_ERROR, bundle);
        }
    }


    private String getURL()
    {
        int mode=new User().getUserPlayingMode(context);
        String userId=new User().getGCUserUniqueKey(context);


        if(mode== Constants.CLOUD_MODE)
        {
            if(userId!=null)
            {
                return GCUtils.GET_PLAYER_CA_RULE_URL;
            }else
            {
                return null;
            }
        }else
        if(mode== Constants.ENTERPRISE_MODE)
        {
            if(userId!=null)
            {
                return new User().getEnterPriseURL(context)+GCUtils.GET_PLAYER_CA_RULE_URL_ENTERPRISE;
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

    private class StopSyncRulesReceiver extends BroadcastReceiver
    {
      public void onReceive(Context contex,Intent intent)
      {
          isStopped = true;
          stopSelf();
      }
    }

    public void onDestroy()
    {

        super.onDestroy();

        unRegisterStopServiceReceiver();
    }

    private void sendSuccessResponse()
    {


        if(!isStopped) {

            Bundle bundle = new Bundle();
            bundle.putBoolean("flag", true);
            AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver.send(SYNC_RULES_API_SUCCESS, bundle);
        }
    }

    //un register StopServiceReceiver
    private void unRegisterStopServiceReceiver()
    {
        try
        {
            unregisterReceiver(stopSyncRulesReceiver);
        }catch(Exception e)
        {

        }finally {
            stopSyncRulesReceiver=null;
        }
    }

    //process response
    private void processResponse(String response)
    {

        try
        {
            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject.getInt("statusCode")==0)
            {
                processRules(jsonObject.getJSONArray("rules"));
                //sendSuccessResponse();
            }else if(jsonObject.getInt("statusCode")==2)
            {
                handleNoRulesFound();
            }else
            {
                sendFailedResponse(false,jsonObject.getString("status"),jsonObject.getInt("statusCode"));
            }
        }catch(JSONException e)
        {
            e.printStackTrace();
            sendFailedResponse(false,"Unable to parse the server response",6);

        }catch(Exception e)
        {
            e.printStackTrace();
            sendFailedResponse(false,"Unable to parse the server response",6);
        }
    }

    private void handleNoRulesFound()
    {
        //delete unknown campaigns
        CampaignRulesDBModel.clearServerRules(context);

        //send success response
        sendSuccessResponse();
    }

    private void processRules(JSONArray rulesArray) throws JSONException, Exception
    {

      for(int i=0;i<rulesArray.length();i++)
      {
          JSONObject ruleObj = rulesArray.getJSONObject(i);
          long ruleServerId = ruleObj.getLong("rule_id");
          if(!serverRules.containsKey(ruleServerId))
          {
              ContextualAdRule ruleModel = new ContextualAdRule(ruleObj.getString("classifier"),
                      ruleServerId,ruleObj.getInt("delay_time"));
              serverRules.put(ruleServerId,ruleModel);
          }

          long rcId = ruleObj.getLong("rc_id");
          if(rcId>0)
          {
              serverCampaigns.put(rcId, new CARCampaigns(ruleObj.getString("campaign_name"),ruleObj.getString("classifier"),
                      rcId));
          }

      }



        if(serverRules!=null && serverRules.size()>0)
        {
            //check and update ticker text
            saveRules();

        }else
        {
            handleNoRulesFound();
        }

    }

    private void saveRules()
    {
        //prepare data to insert and update in bulk

        HashMap<Long,ContextualAdRule> insertData =(HashMap)serverRules.clone();


        try
        {


            //check campaign record to update or insert to the local database
            Cursor campaignsCursor= CampaignRulesDBModel.getRulesByServerIdsList(TextUtils.join(",",serverRules.keySet()),context);

            if (campaignsCursor != null && campaignsCursor.moveToFirst())
            {
                do {


                    long serverId = campaignsCursor.getLong(campaignsCursor.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID));
                    if (serverId >0)
                    {
                        insertData.remove(serverId);
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
                bulkRulesInsert(insertData);

            }



            deleteUnknownRules();

            deleteUnknownRuleCampaigns();

            checkAndInsertRuleCampaigns();

            //send success response
            sendSuccessResponse();
        }


    }

    private void bulkRulesInsert(HashMap<Long,ContextualAdRule> rules)
    {
        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {

            mDb.beginTransaction();

            String insetQuary = "INSERT INTO " + CampaignRulesDBModel.CAMPAIGN_RULES_TABLE + "(" +
                    CampaignRulesDBModel.RULE_NAME + ","
                    + CampaignRulesDBModel.RULE_SERVER_ID +"," +
                    CampaignRulesDBModel.RULE_CREATED_AT + ","
                    + CampaignRulesDBModel.RULE_UPDATED_AT +"," +
                    CampaignRulesDBModel.RULE_DELAY_DURATION +") VALUES(?,?,?,?,?)";


            SQLiteStatement insert = mDb.compileStatement(insetQuary);

            Iterator it = rules.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                ContextualAdRule ruleModel = (ContextualAdRule) pair.getValue();


                insert.bindString(1, ruleModel.getName());
                insert.bindLong(2, ruleModel.getServerId());
                insert.bindLong(3, Calendar.getInstance().getTimeInMillis());
                insert.bindLong(4, Calendar.getInstance().getTimeInMillis());
                insert.bindLong(5, ruleModel.getDelayDuration());

                insert.execute();

            }

            mDb.setTransactionSuccessful();


        }
        catch (Exception e)
        {
            e.printStackTrace();

            Log.w("XML:",e );
        }
        finally
        {

            mDb.endTransaction();
            rules.clear();
        }
    }

    private void deleteUnknownRules()
    {

            try
            {
                CampaignRulesDBModel.deleteGarbageRulesByServerId(TextUtils.join(", ", serverRules.keySet()), context);

            }catch (Exception e)
            {
                Log.d("auto download campaigns", "Error in deleteUnknownSchedules" + e.getMessage());
            }

    }

    private void deleteUnknownRuleCampaigns()
    {
        try
        {
            CampaignRulesDBModel.deleteGarbageRuleCampaignsByServerIds(TextUtils.join(", ", serverCampaigns.keySet()), context);

        }catch (Exception e)
        {
            Log.d("auto download campaigns", "Error in deleteUnknownSchedules" + e.getMessage());
        }
    }

    private synchronized void  checkAndInsertRuleCampaigns()
    {
        try {
            //check campaign record to update or insert to the local database
            Cursor campaignsCursor = CampaignRulesDBModel.getServerCampaigns(TextUtils.join(", ", serverCampaigns.keySet()), context);

            if (campaignsCursor != null && campaignsCursor.moveToFirst()) {
                do {


                    long serverId = campaignsCursor.getLong(campaignsCursor.getColumnIndex(CampaignRulesDBModel.RULE_CAMPAIGN_SERVER_ID));
                    if (serverId > 0) {
                        //insert to update list and remove from
                        serverCampaigns.remove(serverId);
                    }

                } while (campaignsCursor.moveToNext());

            }


            //after removing duplicate items do bulk insert schedules
            bulkInsertRuleCampaigns();
        }catch(Exception e)
        {

        }finally
        {

        }

    }

    private void bulkInsertRuleCampaigns()
    {
        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {

            mDb.beginTransaction();

            String insertQuery = "INSERT INTO " + CampaignRulesDBModel.RULE_CAMPAIGN_TABLE + "(" +
                    CampaignRulesDBModel.RULE_CAMPAIGN_SERVER_ID + ","
                    + CampaignRulesDBModel.RULE_CAMPAIGN_RULE_NAME + ","
                    + CampaignRulesDBModel.RULE_CAMPAIGN_CAMPAIGN_NAME +") VALUES(?,?,?)";


            SQLiteStatement insert = mDb.compileStatement(insertQuery);

            Iterator it = serverCampaigns.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                CARCampaigns serverCampaignModel = (CARCampaigns) pair.getValue();




                insert.bindLong(1, serverCampaignModel.getServerId());
                insert.bindString(2, serverCampaignModel.getRuleName());
                insert.bindString(3, serverCampaignModel.getCampaignName());

                insert.execute();

                //it.remove(); // avoids a ConcurrentModificationException
            }

            mDb.setTransactionSuccessful();

        }
        catch (Exception e)
        {

            Log.w("XML:",e );
        }
        finally
        {

            mDb.endTransaction();
            serverCampaigns.clear();
        }
    }

}
