package com.ibetter.www.adskitedigi.adskitedigi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class CampaignReportsDBModel
{
    public final static String LOCAL_ID="_id";


    /* list of campaigns table*/
    public final static String CAMPAIGNS_REPORTS_TABLE="campaigns_report_table";
    public final static String CAMPAIGNS_REPORTS_TABLE_SERVER_ID="server_id";
    public final static String CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME="campaign_name";
    public final static String CAMPAIGNS_REPORTS_TABLE_TIMES_PLAYED="times_played";
    public final static String CAMPAIGNS_REPORTS_TABLE_DURATION="duration";
    public final static String CAMPAIGNS_REPORTS_TABLE_CREATED_AT="created_at";

    public final static String CAMPAIGNS_REPORTS_TABLE_MAX_CREATED_AT="max_created_at";

    public final static String CAMPAIGNS_REPORTS_TABLE_MAX_SERVER_ID="max_server_id";

    public final static String CAMPAIGNS_REPORTS_TABLE_TOTAL_DURATION="total_duration";

    public final static String CAMPAIGNS_REPORTS_TABLE_NO_TIMES_PLAYED="no_times_played";

    public  final static String CREATE_CAMPAIGNS_REPORTS_TABLE="CREATE TABLE " + CAMPAIGNS_REPORTS_TABLE
            + " ("
            + LOCAL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
            +CAMPAIGNS_REPORTS_TABLE_SERVER_ID + " INTEGER DEFAULT 0,"
            + CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME + " TEXT ,"
            +CAMPAIGNS_REPORTS_TABLE_DURATION+ " INTEGER ,"
            +CAMPAIGNS_REPORTS_TABLE_CREATED_AT+ " INTEGER ,"
            +CAMPAIGNS_REPORTS_TABLE_TIMES_PLAYED + " INTEGER );";




    public static long insertCampaignReportsInfo(ContentValues cv, Context context)
    {
        return DataBaseHelper.initializeDataBase(context).saveRecordToDBTable(cv,CAMPAIGNS_REPORTS_TABLE);
    }

    public static long updateCampaignReportsInfo(String campaignName,ContentValues cv, Context context)
    {
        String whereCondition = CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME+" = '"+campaignName+"'";

        return DataBaseHelper.initializeDataBase(context).updateDBRecord(CAMPAIGNS_REPORTS_TABLE,cv,whereCondition);
    }

    public static Cursor getCampaignReport(String campaignName, Context context)
    {

        String sqlQuery =String.format("SELECT * FROM "+CAMPAIGNS_REPORTS_TABLE+" WHERE " +CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME+"='"+campaignName+"'");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

    }



    public static   Cursor getPlayerCampaignStatics(Context context,long syncTime)
    {

       String whereCondition=" WHERE "+CAMPAIGNS_REPORTS_TABLE_CREATED_AT+" <= "+syncTime;

       String sqlQuery =String.format("SELECT "+
                    "MAX("+CAMPAIGNS_REPORTS_TABLE_SERVER_ID+") AS "+ CAMPAIGNS_REPORTS_TABLE_MAX_SERVER_ID+","+
                    "MAX("+CAMPAIGNS_REPORTS_TABLE_CREATED_AT+") AS "+ CAMPAIGNS_REPORTS_TABLE_MAX_CREATED_AT+","+
                    CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME+","+
                   "sum("+ CAMPAIGNS_REPORTS_TABLE_DURATION +") AS "+ CAMPAIGNS_REPORTS_TABLE_TOTAL_DURATION+","+
                    "sum("+ CAMPAIGNS_REPORTS_TABLE_TIMES_PLAYED +") AS "+ CAMPAIGNS_REPORTS_TABLE_NO_TIMES_PLAYED+
                    " FROM "+CAMPAIGNS_REPORTS_TABLE+whereCondition+" GROUP BY "+ CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME+" ;");

            return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }


    public static  boolean deletePrevReportsCollection(long syncTime,Context context)
    {

        String whereCondition=CAMPAIGNS_REPORTS_TABLE_CREATED_AT+" <= "+syncTime;

            long status= DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGNS_REPORTS_TABLE,whereCondition);

            if(status>0)
            {
                return true;
            }
            return false;

    }

    public static  boolean deleteAllServerReportsCollection(Context context)
    {

        long status= DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGNS_REPORTS_TABLE,null);

        if(status>0)
        {
            return true;
        }
        return false;
    }

}
