package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.CARCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.ProcessRule;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DateTimeModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class HandleRulesCommands {
    private Context context;

    public HandleRulesCommands(Context context)
    {
        this.context = context;
    }

    public void handleCamRulesCommands(JSONObject jsonObject)throws JSONException
    {
        String action = jsonObject.getString("action");
        if(action.equalsIgnoreCase(context.getString(R.string.get_campaign_rules_request)))
        {
            getCampRulesReq(jsonObject.getString(context.getString(R.string.save_ftp_command_response_to_json_key)));

        }else if(action.equalsIgnoreCase(context.getString(R.string.process_rules_request)))
        {
            //process all the rules commands insert,update,delete rules request
            processCampRulesReq(jsonObject.getString(context.getString(R.string.campaign_rules_info)));
        }
        else if(action.equalsIgnoreCase(context.getString(R.string.handle_rule_request)))
        {
            Log.d("Handle rule","Inside handle rule request---"+jsonObject.getString(context.getString(R.string.campaign_rule)));
            //process all the rules commands insert,update,delete rules request
            handleRule(jsonObject.getString(context.getString(R.string.campaign_rule)));

        }else if(action.equalsIgnoreCase(context.getString(R.string.get_rule_names_request)))
        {
            Log.d("Handle rule","Inside handle rule request---");
            getCampRuleNamesReq(jsonObject.getString(context.getString(R.string.save_ftp_command_response_to_json_key)));
        }

    }

    private void getCampRulesReq(String saveResponseTo)throws JSONException
    {
        JSONObject jsonObject = getCampRulesInfo();
        EnterPriseSettingsModel.saveSMFTPResponse(context,jsonObject.toString(),saveResponseTo);


    }

    private JSONObject getCampRulesInfo() throws JSONException
    {
        JSONObject mainObject=new JSONObject();
        Cursor cursor=new CampaignRulesDBModel(context).getRules();
        if(cursor!=null && cursor.moveToFirst())
        {
            JSONArray newRulesArray=new JSONArray();

            do {
                JSONObject ruleObject=new JSONObject();
                ruleObject.put(context.getString(R.string.json_rule_id),cursor.getString(cursor.getColumnIndex(CampaignRulesDBModel.RULE_ID)));
                ruleObject.put(context.getString(R.string.json_rule_name),cursor.getString(cursor.getColumnIndex(CampaignRulesDBModel.RULE_NAME)));
                ruleObject.put(context.getString(R.string.json_rule_created_at),cursor.getString(cursor.getColumnIndex(CampaignRulesDBModel.RULE_CREATED_AT)));
                ruleObject.put(context.getString(R.string.json_rule_updated_at),cursor.getString(cursor.getColumnIndex(CampaignRulesDBModel.RULE_UPDATED_AT)));

                String campInfo=getCampInfo(cursor.getString(cursor.getColumnIndex(CampaignRulesDBModel.RULE_NAME)));
                Log.i("test info",""+campInfo);
                ruleObject.put(context.getString(R.string.json_rule_camp_info),campInfo);
                newRulesArray.put(ruleObject);

               } while (cursor.moveToNext());

            //Log.i("mainObject","newRulesArray:"+newRulesArray.toString());
            mainObject.put("rules",newRulesArray);
            Log.i("mainObject","mainObject:"+mainObject.toString());
        }else
        {
            mainObject.put("statusCode","1");
            mainObject.put("status","No Rules Found");
        }
        return mainObject;
     }


     private String getCampInfo(String ruleName) {
         ArrayList<String> campaignsFileArray = new ArrayList<>();


         Cursor ruleInfoCursor = new CampaignRulesDBModel(context).getRuleCampaignInfo(ruleName);
         if (ruleInfoCursor != null && ruleInfoCursor.moveToFirst()) {
             do {
                 //rule has info to process,, get info (campaigns to play)
                 String campaign = ruleInfoCursor.getString(ruleInfoCursor.getColumnIndex(CampaignRulesDBModel.RULE_CAMPAIGN_CAMPAIGN_NAME));

                 Log.i("test rule campaign",campaign);
                 if (campaign != null) {

                         campaignsFileArray.add(campaign);

                 }

             } while (ruleInfoCursor.moveToNext());

             Log.i("test rule campaign lis",""+campaignsFileArray);

             DataBaseHelper.closeCursor(ruleInfoCursor);





            String  info=TextUtils.join(",",campaignsFileArray);
             return info;

         }

         return null;
     }
    private void processCampRulesReq(String mainString) throws JSONException
    {
        JSONObject mainObject=new JSONObject(mainString);
        Log.i("mainObject","mainObject:"+mainObject);

        //insert new campaign rules to DB
        if(mainObject.has(context.getString(R.string.json_new_rules_list)))
        {
            JSONArray newRulesArray=mainObject.getJSONArray(context.getString(R.string.json_new_rules_list));
            for (int i = 0; i < newRulesArray.length(); i++)
            {
                ArrayList<String> campList=new ArrayList();
                JSONObject ruleObject = newRulesArray.getJSONObject(i);
                JSONArray campArray = ruleObject.getJSONArray(context.getString(R.string.json_rule_camp_info));

                for (int j=0;j<campArray.length();j++)
                {
                    campList.add(campArray.getString(j));

                }
                // Log.i("mainObject", "campList:"+campList.toString());
                ContentValues cv = new ContentValues();
                cv.put(CampaignRulesDBModel.RULE_NAME, ruleObject.getString(context.getString(R.string.json_rule_name)));
                cv.put(CampaignRulesDBModel.RULE_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
                cv.put(CampaignRulesDBModel.RULE_CREATED_AT, Calendar.getInstance().getTimeInMillis());
                new CampaignRulesDBModel(context).insertCampaignRulesInfo(cv);

                bulkInsertRuleCampaigns(campList,ruleObject.getString(context.getString(R.string.json_rule_name)));
            }
        }

        //delete rules from deleteRulesArray
        if(mainObject.has(context.getString(R.string.json_delete_rule_list)))
        {
            JSONArray deleteArray = mainObject.getJSONArray(context.getString(R.string.json_delete_rule_list));
            for(int i=0;i< deleteArray.length();i++)
            {
                int ruleId=deleteArray.getInt(i);
                new CampaignRulesDBModel(context).deleteRuleInfo(ruleId);
            }
        }


        //update campaign rules in DB
        if(mainObject.has(context.getString(R.string.json_update_rules_list)))
        {
            JSONArray updateRulesArray=mainObject.getJSONArray(context.getString(R.string.json_update_rules_list));
            Log.i("mainObject", "updateRulesArray:"+updateRulesArray.toString());

            for (int i = 0; i < updateRulesArray.length(); i++)
            {
                ArrayList<String> insertCampList=new ArrayList();
                ArrayList<String> campList=new ArrayList();

                JSONObject ruleObject = updateRulesArray.getJSONObject(i);
                JSONArray campArray = ruleObject.getJSONArray(context.getString(R.string.json_rule_camp_info));

                for (int j=0;j<campArray.length();j++)
                {
                    campList.add( campArray.getString(j));

                    if(!(CampaignRulesDBModel.isRuleCampaignsExist(ruleObject.getString(context.getString(R.string.json_rule_name)),campArray.getString(j),context)))
                    {
                        insertCampList.add( campArray.getString(j));
                    }
                }
                // Log.i("mainObject", "campList:"+campList.toString());
                ContentValues cv = new ContentValues();
                String ruleId=ruleObject.getString(context.getString(R.string.json_rule_id));
                cv.put(CampaignRulesDBModel.RULE_NAME, ruleObject.getString(context.getString(R.string.json_rule_name)));
                cv.put(CampaignRulesDBModel.RULE_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
                cv.put(CampaignRulesDBModel.RULE_CREATED_AT, Calendar.getInstance().getTimeInMillis());

                new CampaignRulesDBModel(context).updateCampaignRuleInfo(ruleId,cv);

                bulkInsertRuleCampaigns(insertCampList,ruleObject.getString(context.getString(R.string.json_rule_name)));

                StringBuilder sb=new StringBuilder();
                String pre="";
                for(String camp:campList)
                {
                    sb.append(pre+"'"+camp+"'");
                    pre=",";
                }
                CampaignRulesDBModel.deleteUnKnownCampaigns(sb.toString(),ruleObject.getString(context.getString(R.string.json_rule_name)),context);
            }
        }

    }


    private void bulkInsertRuleCampaigns(ArrayList<String> campList,String ruleName)
    {
        SQLiteDatabase mDb = DataBaseHelper.initializeDataBase(context.getApplicationContext()).getDb();

        try {

            mDb.beginTransaction();

            String insertQuery = "INSERT INTO " + CampaignRulesDBModel.RULE_CAMPAIGN_TABLE + "(" +
                    CampaignRulesDBModel.RULE_CAMPAIGN_SERVER_ID + ","
                    + CampaignRulesDBModel.RULE_CAMPAIGN_RULE_NAME + ","
                    + CampaignRulesDBModel.RULE_CAMPAIGN_CAMPAIGN_NAME +") VALUES(?,?,?)";


            SQLiteStatement insert = mDb.compileStatement(insertQuery);

            for (String camp:campList)
            {
                insert.bindLong(1, 0);
                insert.bindString(2, ruleName);
                insert.bindString(3,camp);

                insert.execute();

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

        }
    }

    //handle individual rule
    private void handleRule(String rule)
    {

         if(rule!=null) {

             ProcessRule.startService(context,rule,new DateTimeModel().getDate(new SimpleDateFormat(Constants.LOCAL_SAVE_DATE_TIME_FORMAT),Calendar.getInstance().getTimeInMillis()),
                     0);


             ArrayList<File> campaignsFileArray = new ArrayList<>();

             Cursor ruleInfoCursor = new CampaignRulesDBModel(context).getRuleCampaignInfo(rule);
            if(ruleInfoCursor!=null && ruleInfoCursor.moveToFirst())
            {
                do {
                    //rule has info to process,, get info (campaigns to play)
                    String campaign = ruleInfoCursor.getString(ruleInfoCursor.getColumnIndex(CampaignRulesDBModel.RULE_CAMPAIGN_CAMPAIGN_NAME));

                    if (campaign != null) {
                        File campaignFile = new File(new User().getUserPlayingFolderModePath(context) + File.separator + campaign);
                        if (campaignFile.exists()) {
                            campaignsFileArray.add(campaignFile);
                        }
                    }

                }while (ruleInfoCursor.moveToNext());
                DataBaseHelper.closeCursor(ruleInfoCursor);

                if (campaignsFileArray.size() >= 1) {
                    Bundle extras = new Bundle(2);
                    extras.putSerializable("campaignFiles", campaignsFileArray);
                    DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.HANDLE_CAMPAIGN_RULE_ACTION_CODE, extras);
                }
            }
        }
    }


    //get all the campaign rule names
    private void getCampRuleNamesReq(String saveResponseTo)throws JSONException
    {
        JSONObject jsonObject = getRuleNames();
        EnterPriseSettingsModel.saveSMFTPResponse(context,jsonObject.toString(),saveResponseTo);
    }


    private JSONObject getRuleNames() throws JSONException
    {
        JSONObject mainObject=new JSONObject();
        Cursor cursor=new CampaignRulesDBModel(context).getRuleNames();
        if(cursor!=null && cursor.moveToFirst())
        {
            JSONArray namesArray=new JSONArray();
            do {
                namesArray.put(cursor.getString(cursor.getColumnIndex(CampaignRulesDBModel.RULE_NAME)));
            } while (cursor.moveToNext());

            //Log.i("mainObject","newRulesArray:"+newRulesArray.toString());
            mainObject.put("rules",namesArray);
            Log.i("mainObject","mainObject:"+mainObject.toString());
        }else
        {
            mainObject.put("statusCode","1");
            mainObject.put("status","No Rules Found");
        }
        return mainObject;
    }


}
