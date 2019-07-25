package com.ibetter.www.adskitedigi.adskitedigi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import static com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper.LOCAL_ID;

public class CampaignRulesDBModel
{
    private Context context;
    public final static String CAMPAIGN_RULES_TABLE="campaign_rules_table";
    public final static String RULE_ID="_id";
    public final static String RULE_NAME="name";
    public final static String RULE_CREATED_AT="created_at";
    public final static String RULE_UPDATED_AT="updated_at";
    public final static String RULE_ASSIGNED_CAMP_LIST="camp_list";
    public final static String RULE_SERVER_ID="server_id";
    public final static String RULE_DELAY_DURATION="delay_duration";


    public  final  static String CREATE_RULES_TABLE = "CREATE TABLE " + CAMPAIGN_RULES_TABLE
            + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + RULE_NAME + " TEXT,"
            + RULE_CREATED_AT + " TEXT,"
            + RULE_ASSIGNED_CAMP_LIST+" TEXT,"
            + RULE_UPDATED_AT+ " TEXT,"
            +RULE_SERVER_ID+" INTEGER DEFAULT 0," +
            RULE_DELAY_DURATION+" INTEGER DEFAULT 0 );";

    //rule campaigns table
    public final static String RULE_CAMPAIGN_TABLE = "rule_campaigns";
    public final static String RULE_CAMPAIGN_SERVER_ID = "rc_server_id";
    public final static String RULE_CAMPAIGN_RULE_NAME="rule_name";
    public final static String RULE_CAMPAIGN_CAMPAIGN_NAME ="campaign_name";

    public final static String CREATE_RULE_CAMPAIGNS_TABLE = "CREATE TABLE "+RULE_CAMPAIGN_TABLE+
            " ("+ "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            RULE_CAMPAIGN_SERVER_ID+" INTEGER DEFAULT 0," +
            RULE_CAMPAIGN_RULE_NAME+" VARCHAR(125)," +
            RULE_CAMPAIGN_CAMPAIGN_NAME+" VARCHAR(125) );";

    public final static String DELETE_RULE_CAMPAIGNS_TRIGGER =" CREATE TRIGGER IF NOT EXISTS delete_rule_campaign_trigger AFTER DELETE ON "+
            CAMPAIGN_RULES_TABLE+" FOR EACH ROW BEGIN DELETE from "+RULE_CAMPAIGN_TABLE+"  WHERE "+RULE_CAMPAIGN_RULE_NAME+" = OLD."+RULE_NAME+"; END";




    public CampaignRulesDBModel(Context context)
    {
        this.context = context;
    }

    //save customer info
    public long insertCampaignRulesInfo(ContentValues cv)
    {
        return DataBaseHelper.initializeDataBase(context).saveRecordToDBTable(cv,CAMPAIGN_RULES_TABLE);
    }

    //delete campaign Rule info
    public boolean deleteRuleInfo(int localId)
    {
        Log.i("schedule","deleting rule");
        String whereCondition = LOCAL_ID+" = "+localId;
        return (DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGN_RULES_TABLE,whereCondition)>=1);
    }

    //update schedule details based on schedule server id
    public boolean updateCampaignRuleInfo(String localId,ContentValues cv)
    {
        Log.i("update", "updateCampaignRuleInfo:"+localId);
        String whereCondition = new DataBaseHelper(context).LOCAL_ID+" = '"+localId+"'";
        return DataBaseHelper.initializeDataBase(context).updateDBRecord(CAMPAIGN_RULES_TABLE,cv,whereCondition) >=1;
    }

    //get Campaign rules from the DB
    public Cursor getRules()
    {
        String condition = "SELECT * FROM "+CAMPAIGN_RULES_TABLE;
        return DataBaseHelper.initializeDataBase(context).getRecord(condition);
    }

    //get rule info
    public Cursor getRuleInfo(String rule)
    {
        String condition = "SELECT * FROM "+CAMPAIGN_RULES_TABLE+ " WHERE "+RULE_NAME+
                " LIKE '%"+rule+"%' LIMIT 1";
                ;
        return DataBaseHelper.initializeDataBase(context).getRecord(condition);
    }

    //get only campaign rule names
    public Cursor getRuleNames()
    {
        String condition = "SELECT "+RULE_NAME+" FROM "+CAMPAIGN_RULES_TABLE;
        return DataBaseHelper.initializeDataBase(context).getRecord(condition);
    }

    public static void clearServerRules(Context context) {
        String condition = RULE_SERVER_ID + " > 0";
        DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGN_RULES_TABLE,condition);
    }

    public static Cursor getRulesByServerIdsList(String ruleServerIds,Context context)
    {
      String query = "SELECT * FROM "+CAMPAIGN_RULES_TABLE+" WHERE "+RULE_SERVER_ID+" IN ("+ruleServerIds+")";
      return DataBaseHelper.initializeDataBase(context).getRecord(query);
    }

    public static void deleteGarbageRulesByServerId(String ruleServerIds,Context context)
    {
        String whereCondition=RULE_SERVER_ID+">0 AND "+RULE_SERVER_ID+" NOT IN("+ruleServerIds+")";

        if(ruleServerIds!=null)
        {

            DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGN_RULES_TABLE,whereCondition);

        }

    }

    public static void deleteGarbageRuleCampaignsByServerIds(String serverIds,Context context)
    {
        String whereCondition=RULE_CAMPAIGN_SERVER_ID+">0 AND "+RULE_CAMPAIGN_SERVER_ID+" NOT IN("+serverIds+")";

        if(serverIds!=null)
        {

            DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(RULE_CAMPAIGN_TABLE,whereCondition);

        }

    }

    public static  Cursor getServerCampaigns(String serverIdList,Context context)
    {

        String sqlQuery =String.format("SELECT "+RULE_CAMPAIGN_SERVER_ID+" FROM "+RULE_CAMPAIGN_TABLE+" WHERE " +RULE_CAMPAIGN_SERVER_ID+" IN ("+serverIdList+ ");");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

    }

    public static Cursor getRuleCampaignsByRuleName(String ruleName,Context context)
    {
        String[] rules = ruleName.split(context.getString(R.string.rule_seperator));
        String sql;
        if(rules.length<=1)
        {
            sql = "SELECT * FROM "+RULE_CAMPAIGN_TABLE+" WHERE "+RULE_CAMPAIGN_RULE_NAME+" = ?";
            return DataBaseHelper.initializeDataBase(context).getRecord(sql,new String[]{ruleName});
        }else
        {

            ruleName = TextUtils.join("\",\"",rules);
            sql =  "SELECT * FROM "+RULE_CAMPAIGN_TABLE+" WHERE "+RULE_CAMPAIGN_RULE_NAME+" IN (\""+ruleName+"\")";

            return DataBaseHelper.initializeDataBase(context).getRecord(sql);
        }


    }

    public static boolean isRuleCampaignsExist(String ruleName,String campaignName,Context context)
    {


        String sql = "SELECT "+LOCAL_ID+" FROM "+RULE_CAMPAIGN_TABLE+" WHERE "+RULE_CAMPAIGN_RULE_NAME+" ='"+ruleName+"' AND "+RULE_CAMPAIGN_CAMPAIGN_NAME+" ='"+campaignName+"';";
       Cursor cursor= DataBaseHelper.initializeDataBase(context).getRecord(sql);

       if(cursor!=null&&cursor.getCount()>0)
       {
           return true;
       }else
       {
           return false;
       }
    }


    //get rule info
    public Cursor getRuleCampaignInfo(String rule)
    {
        String condition = "SELECT * FROM "+RULE_CAMPAIGN_TABLE+ " WHERE "+RULE_CAMPAIGN_RULE_NAME+"='"+rule+"'";

        return DataBaseHelper.initializeDataBase(context).getRecord(condition);
    }

    public static boolean deleteUnKnownCampaigns(String CAMPAIGNS,String RULE, Context context) {


        String whereCondition = RULE_CAMPAIGN_RULE_NAME+"='"+RULE +"' AND " +RULE_CAMPAIGN_CAMPAIGN_NAME + " NOT IN(" + CAMPAIGNS + ")";

        if (CAMPAIGNS != null)
        {


            long status = DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(RULE_CAMPAIGN_TABLE, whereCondition);

            if (status > 0)
            {
                return true;
            }

            return false;
        } else {

            return false;
        }

    }


}
