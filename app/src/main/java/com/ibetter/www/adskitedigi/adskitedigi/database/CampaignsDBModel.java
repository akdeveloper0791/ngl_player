package com.ibetter.www.adskitedigi.adskitedigi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CampaignsDBModel
{
    public final static String LOCAL_ID="_id";

    /* list of campaigns table*/
    public final static String CAMPAIGNS_TABLE="campaigns_table";
    public final static String CAMPAIGNS_TABLE_UPLOADED_BY="campaign_uploaded_by";
    public final static String CAMPAIGNS_TABLE_CAMPAIGN_NAME="campaign_name";
    public final static String CAMPAIGNS_TABLE_CREATED_DATE="created_date";
    public final static String CAMPAIGNS_TABLE_UPDATED_DATE="updated_date";
    public final static String CAMPAIGNS_TABLE_IS_SKIP="is_skip";
    public final static String CAMPAIGNS_TABLE_CAMP_TYPE="camp_type";
    public final static String CAMPAIGNS_TABLE_STOR_LOCATION="stor_location";
    public final static String CAMPAIGNS_TABLE_CAMP_SIZE="campaign_size";
    public final static String CAMPAIGNS_TABLE_SOURCE="source";
    public final static String CAMPAIGNS_TABLE_SAVE_PATH="save_path";
    public final static String CAMPAIGNS_TABLE_SERVER_ID="server_id";
    public final static String CAMPAIGN_TABLE_CAMPAIGN_INFO="info";
    public final static String CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED="is_downloaded";
    public final static String CAMPAIGN_TABLE_SCHEDULE_TYPE="schedule_type";
    public final static String CAMPAIGN_TABLE_SCHEDULE_PRIORITY="campaign_priority";
    public final static String CAMPAIGN_TABLE_PLAYER_CAMPAIGN_ID = "pc_id";
    public final static String CAMPAIGN_TABLE_PLAYER_GROUP_ID = "dgc_id";



    public  final static String CREATE_CAMPAIGNS_TABLE="CREATE TABLE " + CAMPAIGNS_TABLE
            + " ("
            + LOCAL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
            +CAMPAIGNS_TABLE_UPLOADED_BY + " INTEGER,"
            + CAMPAIGNS_TABLE_CAMPAIGN_NAME + " TEXT,"
            +CAMPAIGNS_TABLE_CREATED_DATE+ " TEXT ,"
            +CAMPAIGNS_TABLE_UPDATED_DATE+ " TEXT,"
            +CAMPAIGNS_TABLE_IS_SKIP+ " TEXT,"
            +CAMPAIGNS_TABLE_CAMP_TYPE+ " INTEGER,"
            +CAMPAIGNS_TABLE_STOR_LOCATION+ " INTEGER,"
            +CAMPAIGNS_TABLE_CAMP_SIZE+ " TEXT,"
            +CAMPAIGNS_TABLE_SOURCE+ " INTEGER,"
            +CAMPAIGNS_TABLE_SAVE_PATH+ " TEXT,"
            +CAMPAIGNS_TABLE_SERVER_ID+ " INTEGER DEFAULT 0,"
            +CAMPAIGN_TABLE_CAMPAIGN_INFO + " TEXT,"
            +CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+" INTEGER DEFAULT 0,"
            +CAMPAIGN_TABLE_SCHEDULE_TYPE+" INTEGER DEFAULT 10,"
            +CAMPAIGN_TABLE_SCHEDULE_PRIORITY+" INTEGER DEFAULT 0,"
            +CAMPAIGN_TABLE_PLAYER_CAMPAIGN_ID+" INTEGER DEFAULT 0,"
            +CAMPAIGN_TABLE_PLAYER_GROUP_ID+" INTEGER DEFAULT 0);";

    /*[++] schedules campaign table [++]*/
    public final static String SCHEDULE_CAMPAIGNS_TABLE="schedule_campaigns";
    public final static String SCHEDULE_CAMPAIGNS_CS_ID = "campaign_server_id";
    public final static String SCHEDULE_CAMPAIGNS_SERVER_ID = "sc_server_id";
    public final static String SCHEDULE_CAMPAIGNS_SCHEDULE_FROM = "schedule_from";
    public final static String SCHEDULE_CAMPAIGNS_SCHEDULE_TO = "schedule_to";
    public final static String SCHEDULE_TABLE_SCHEDULE_TYPE="schedule_type";
    public final static String SCHEDULE_TABLE_SCHEDULE_PRIORITY="sc_priority";
    public final static String SCHEDULE_TABLE_NEXT_SCHEDULE_AT="next_schedule_at";
    public final static String SCHEDULE_TABLE_ADDITIONAL_INFO="additional_info";

    public  final static String CREATE_SCHEDULE_CAMPAIGNS_TABLE="CREATE TABLE " + SCHEDULE_CAMPAIGNS_TABLE
            + " ("
            + LOCAL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
            +SCHEDULE_CAMPAIGNS_CS_ID + " INTEGER DEFAULT 0,"
            + SCHEDULE_CAMPAIGNS_SERVER_ID + " INTEGER DEFAULT 0 ,"
            +SCHEDULE_CAMPAIGNS_SCHEDULE_FROM+ " DATETIME ,"
            +SCHEDULE_CAMPAIGNS_SCHEDULE_TO+ " DATETIME ,"
            +SCHEDULE_TABLE_SCHEDULE_TYPE + " INTEGER DEFAULT 100 ,"
            +SCHEDULE_TABLE_SCHEDULE_PRIORITY+" INTEGER DEFAULT 0 ,"
            +SCHEDULE_TABLE_NEXT_SCHEDULE_AT+ " DATETIME, "
            +SCHEDULE_TABLE_ADDITIONAL_INFO+" TEXT );";

    public final static String DELETE_SCHEDULE_CAMPAIGNS_TRIGGER =" CREATE TRIGGER IF NOT EXISTS delete_schedule_campaign_trigger AFTER DELETE ON "+
            CAMPAIGNS_TABLE+" FOR EACH ROW BEGIN DELETE from "+SCHEDULE_CAMPAIGNS_TABLE+"  WHERE "+SCHEDULE_CAMPAIGNS_CS_ID+" = OLD."+CAMPAIGNS_TABLE_SERVER_ID+"; END";

    public final static String RSS_FEEDS_TABLE = "rss_feeds";
    public final static String RSS_FEED_CAMPAIGN_SERVER_ID = "rss_server_id";
    public final static String RSS_FEED_IS_SKIP = "rss_feed_is_skip";
    public final static String RSS_FEED_INFO = "info";

    public final static String CREATE_RSS_FEED_TABLE = "CREATE TABLE "+RSS_FEEDS_TABLE+
            " ("+ "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            RSS_FEED_CAMPAIGN_SERVER_ID+" INTEGER DEFAULT 0," +
            RSS_FEED_IS_SKIP+" INTEGER DEFAULT 1," +
            RSS_FEED_INFO+" TEXT," +
            CAMPAIGN_TABLE_SCHEDULE_TYPE+" INTEGER DEFAULT 10 );";

    public final static String TICKER_TEXT_TABLE = "ticker_text";
    public final static String TICKER_TEXT_SERVER_ID = "ticker_text_server_id";
    public final static String TICKER_TEXT_IS_SKIP = "ticker_text_is_skip";
    public final static String TICKER_TEXT_INFO = "info";

    public final static String CREATE_TICKER_TEXT_TABLE = "CREATE TABLE "+TICKER_TEXT_TABLE+
            " ("+ "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            TICKER_TEXT_SERVER_ID+" INTEGER DEFAULT 0," +
            TICKER_TEXT_IS_SKIP+" INTEGER DEFAULT 1," +
            TICKER_TEXT_INFO+" TEXT," +
            CAMPAIGN_TABLE_SCHEDULE_TYPE+" INTEGER DEFAULT 10 );";

    public static Cursor getCampaigns(Context context)
    {
        String condition = "SELECT * FROM "+CAMPAIGNS_TABLE+" Order By "+CAMPAIGN_TABLE_PLAYER_GROUP_ID+" DESC, "+CAMPAIGN_TABLE_PLAYER_CAMPAIGN_ID+" DESC;";
        return DataBaseHelper.initializeDataBase(context).getRecord(condition);
    }


    // fetch all the  server id's from local database
    public static  Cursor getServerIdsList(String serverIdList,Context context)
    {

        String sqlQuery =String.format("SELECT "+CAMPAIGNS_TABLE_SERVER_ID+" FROM "+CAMPAIGNS_TABLE+" WHERE " +CAMPAIGNS_TABLE_SERVER_ID+" IN ("+serverIdList+ ");");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

    }

    public static  boolean deleteGarbageCampaigns(String serverIds,Context context)
    {

        String whereCondition=CAMPAIGNS_TABLE_SERVER_ID+">0 AND "+CAMPAIGNS_TABLE_SERVER_ID+" NOT IN("+serverIds+")";

        if(serverIds!=null)
        {


            long status= DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGNS_TABLE,whereCondition);

             if(status>0)
             {
                 return true;
             }
            return false;
        }
        else
        {

            return false;
        }

    }

    public static   Cursor getGarbageCampaigns(String serverIds,Context context)
    {


        String whereCondition=CAMPAIGNS_TABLE_SERVER_ID+">0 AND "+CAMPAIGNS_TABLE_SERVER_ID+" NOT IN("+serverIds+")";

        if(serverIds!=null)
        {

            String sqlQuery =String.format("SELECT * FROM "+CAMPAIGNS_TABLE+" WHERE " + whereCondition+ ";");
            return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

        }
        else
        {

            return null;
        }


    }

    public static long getServerIdByCampaignName(String campaignName, Context context)
    {

        String sqlQuery =String.format("SELECT "+CAMPAIGNS_TABLE_SERVER_ID+" FROM "+CAMPAIGNS_TABLE+" WHERE " +CAMPAIGNS_TABLE_CAMPAIGN_NAME+"='"+campaignName+"'");
        Cursor cursor= DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);


         if(cursor!=null&&cursor.moveToFirst())
         {

             return cursor.getLong(cursor.getColumnIndex(CAMPAIGNS_TABLE_SERVER_ID));
         }else
         {
             return 0;
         }
    }

    public static   Cursor getServerCampaigns(Context context)
    {


        String whereCondition=CAMPAIGNS_TABLE_SERVER_ID+">0 ";
        String sqlQuery =String.format("SELECT * FROM "+CAMPAIGNS_TABLE+" WHERE " + whereCondition+ ";");

        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

    }

    public static  boolean deleteServerCampaigns(Context context)
    {

        String whereCondition=CAMPAIGNS_TABLE_SERVER_ID+">0";

        long status= DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGNS_TABLE,whereCondition);

        if(status>0)
        {
            return true;
        }
        return false;


    }



    public static  Cursor getSavedSchedules(String serverIdList,Context context)
    {

        String sqlQuery =String.format("SELECT "+SCHEDULE_CAMPAIGNS_SERVER_ID+" FROM "+SCHEDULE_CAMPAIGNS_TABLE+" WHERE " +SCHEDULE_CAMPAIGNS_SERVER_ID+" IN ("+serverIdList+ ");");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

    }

    public static  boolean deleteGarbageSchedules(String serverIds,Context context)
    {

        String whereCondition=SCHEDULE_CAMPAIGNS_SERVER_ID+" NOT IN("+serverIds+")";

        if(serverIds!=null)
        {


            long status= DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(SCHEDULE_CAMPAIGNS_TABLE,whereCondition);

            if(status>0)
            {
                return true;
            }
            return false;
        }
        else
        {

            return false;
        }

    }

    public static Cursor getCampaignsToDownload(Context context)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(ScheduleCampaignModel.LOCAL_SCHEDULE_DATE_FORMAT);
        String currentDateTime = sdf.format(Calendar.getInstance().getTime());

        String sqlQuery =String.format("SELECT * FROM "+CAMPAIGNS_TABLE+" WHERE " +CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+" = 0 AND (" +
                CAMPAIGN_TABLE_SCHEDULE_TYPE+" = "+ ScheduleCampaignModel.CONTINUOUS_PLAY +" OR "+CAMPAIGNS_TABLE_SERVER_ID+" IN " +
                "(SELECT "+SCHEDULE_CAMPAIGNS_CS_ID+" FROM "+SCHEDULE_CAMPAIGNS_TABLE+" WHERE "+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+" > '"+currentDateTime+"' GROUP BY "+SCHEDULE_CAMPAIGNS_CS_ID+" ORDER BY "+SCHEDULE_CAMPAIGNS_SCHEDULE_FROM+" ASC )) GROUP BY "+LOCAL_ID+" LIMIT 6;");

        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }

    public static Cursor getExpiredCampaigns(Context context,String currentDateTime)
    {
        String sqlQuery =String.format("SELECT * FROM "+CAMPAIGNS_TABLE+" WHERE " +CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+" = 1 AND (" +
                CAMPAIGN_TABLE_SCHEDULE_TYPE+" != "+ ScheduleCampaignModel.CONTINUOUS_PLAY +" AND "+CAMPAIGNS_TABLE_SERVER_ID+" NOT IN " +
                "(SELECT "+SCHEDULE_CAMPAIGNS_CS_ID+" FROM "+SCHEDULE_CAMPAIGNS_TABLE+" WHERE "+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+" > '"+currentDateTime+"' GROUP BY "+SCHEDULE_CAMPAIGNS_CS_ID+" ORDER BY "+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+" DESC )) GROUP BY "+LOCAL_ID+" LIMIT 6;");

        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }

    public static boolean setCampaignDownloadedTrue(Context context,long serverId)
    {
        try
        {
            String whereCondition = CAMPAIGNS_TABLE_SERVER_ID+"=?";
            String[] args = new String[]{String.valueOf(serverId)};
            ContentValues cv = new ContentValues(1);
            cv.put(CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED,1);//set to true.. downloaded
            long updated = DataBaseHelper.initializeDataBase(context).updateDBRecord(CAMPAIGNS_TABLE,cv,whereCondition,args);
            return (updated>=1);
        }catch (Exception e)
        {
            return false;
        }

    }

    public static Cursor getRegularScheduleCampaigns(Context context,String currentDateTime)
    {
        String sqlQuery =String.format("SELECT campaigns.*, schedules."+SCHEDULE_CAMPAIGNS_SCHEDULE_FROM+",schedules."+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+",schedules."+SCHEDULE_TABLE_SCHEDULE_PRIORITY+",schedules."+LOCAL_ID+" as schedule_id FROM "+CAMPAIGNS_TABLE+" as campaigns LEFT JOIN ( SELECT * FROM "+SCHEDULE_CAMPAIGNS_TABLE+" WHERE "+SCHEDULE_TABLE_SCHEDULE_TYPE+" = 100 AND ("+SCHEDULE_CAMPAIGNS_SCHEDULE_FROM+" <= '"+currentDateTime+"' AND "+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+" > '"+currentDateTime+"' ) ) as schedules ON campaigns."+CAMPAIGNS_TABLE_SERVER_ID+" = schedules."+SCHEDULE_CAMPAIGNS_CS_ID+
                " WHERE ( (campaigns." +CAMPAIGN_TABLE_SCHEDULE_TYPE+" = 10 OR ("+ CAMPAIGNS_TABLE_SERVER_ID +" IN ( SELECT "+SCHEDULE_CAMPAIGNS_CS_ID+" FROM "+SCHEDULE_CAMPAIGNS_TABLE+" WHERE "+SCHEDULE_TABLE_SCHEDULE_TYPE+" = 100 AND ("+ SCHEDULE_CAMPAIGNS_SCHEDULE_FROM+" <= '"+currentDateTime+"' AND "+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+" > '"+currentDateTime+"' )))) AND campaigns."+CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+"=1 AND campaigns.is_skip=0 ) ORDER BY campaigns."+CAMPAIGN_TABLE_SCHEDULE_TYPE+" DESC, schedules."+SCHEDULE_TABLE_SCHEDULE_PRIORITY+" DESC");

        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }

    public static Cursor getScheduledCampaigns(Context context,String currentDateTime,String queuedList)
    {
        String sqlQuery =String.format("SELECT campaigns.*, schedules.schedule_from,schedules.schedule_to,schedules."+SCHEDULE_TABLE_SCHEDULE_PRIORITY +",schedules."+LOCAL_ID+" as schedule_id,schedules."+SCHEDULE_TABLE_SCHEDULE_TYPE+",schedules."+SCHEDULE_TABLE_ADDITIONAL_INFO+" FROM "+SCHEDULE_CAMPAIGNS_TABLE+" as schedules INNER JOIN "
                +CAMPAIGNS_TABLE+" as campaigns ON schedules."+SCHEDULE_CAMPAIGNS_CS_ID+" = campaigns."+CAMPAIGNS_TABLE_SERVER_ID+
                " WHERE ( ( schedules."+SCHEDULE_TABLE_SCHEDULE_TYPE+" != 100 AND ( ("+SCHEDULE_TABLE_NEXT_SCHEDULE_AT+" <= '"+currentDateTime+"' OR ("+SCHEDULE_TABLE_NEXT_SCHEDULE_AT+" IS NULL AND "+SCHEDULE_CAMPAIGNS_SCHEDULE_FROM+" <= '"+currentDateTime+"')) AND "+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+" > '"+currentDateTime+"' )) AND ( campaigns."+CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+"=1 AND campaigns.is_skip=0 AND campaigns."+LOCAL_ID+" NOT IN ("+queuedList+") )) ORDER BY schedules."+SCHEDULE_TABLE_SCHEDULE_PRIORITY+" DESC");



        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }

    public static  boolean updateExpiredCampaignsStatus(String localIds,Context context)
    {
        try
        {
            String whereCondition = LOCAL_ID+" IN("+localIds+")";

            ContentValues cv = new ContentValues(1);
            cv.put(CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED,0);//set to false.. downloaded
            long updated = DataBaseHelper.initializeDataBase(context).updateDBRecord(CAMPAIGNS_TABLE,cv,whereCondition);
            return (updated>=1);
        }catch (Exception e)
        {
            return false;
        }

    }

    public static Cursor getCampaignsByName(Context context,String nameList)
    {


        if(nameList!=null)
        {

            String sqlQuery =String.format("SELECT campaigns.*, schedules."+SCHEDULE_CAMPAIGNS_SCHEDULE_FROM+",schedules."+SCHEDULE_CAMPAIGNS_SCHEDULE_TO+",schedules."+SCHEDULE_TABLE_SCHEDULE_PRIORITY+",schedules."+LOCAL_ID+" as schedule_id FROM "+CAMPAIGNS_TABLE+" as campaigns LEFT JOIN "+SCHEDULE_CAMPAIGNS_TABLE+"  as schedules ON campaigns."+CAMPAIGNS_TABLE_SERVER_ID+" = schedules."+SCHEDULE_CAMPAIGNS_CS_ID+
                    " WHERE ( campaigns."+CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+" = 1 AND campaigns."+CAMPAIGNS_TABLE_CAMPAIGN_NAME+" IN ("+nameList+"))");


            return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
        }
        else
        {

            return null;
        }
    }

    public static long insertCampaign(ContentValues cv, Context context) {
        return DataBaseHelper.initializeDataBase(context).saveRecordToDBTable(cv, CAMPAIGNS_TABLE);
    }

    public static long updateCampaign(ContentValues cv, Context context,String campaignName) {
        return DataBaseHelper.initializeDataBase(context).updateDBRecord( CAMPAIGNS_TABLE,cv,CAMPAIGNS_TABLE_CAMPAIGN_NAME+"='"+campaignName+"'");
    }

    public static boolean isCampaignSkip(Context context, String campaignName)
    {
        String condition = "SELECT "+CAMPAIGNS_TABLE_IS_SKIP+" FROM " + CAMPAIGNS_TABLE + " WHERE " + CAMPAIGNS_TABLE_CAMPAIGN_NAME + "='" + campaignName + "'";
        Cursor cursor = DataBaseHelper.initializeDataBase(context).getRecord(condition);

        if (cursor != null && cursor.moveToFirst())
        {
            Log.i("isskip ", campaignName+" "+cursor.getInt(cursor.getColumnIndex(CAMPAIGNS_TABLE_IS_SKIP)));

            if(cursor.getInt(cursor.getColumnIndex(CAMPAIGNS_TABLE_IS_SKIP))==1)
            {
                return true;
            }
            else
            {
               return false;
            }
        }
        else
        {
            return false;
        }
    }

    public static boolean isCampaignNameExist(Context context, String campaignName)
    {
        String condition = "SELECT "+LOCAL_ID+" FROM " + CAMPAIGNS_TABLE + " WHERE " + CAMPAIGNS_TABLE_CAMPAIGN_NAME + "='" + campaignName + "'";
        Cursor cursor = DataBaseHelper.initializeDataBase(context).getRecord(condition);

        if (cursor != null && cursor.getCount() > 0)
        { return true;
        } else {
            return false;
        }
    }

    public static boolean isCampaignExist(Context context, String campaignName) {
        String condition = "SELECT "+LOCAL_ID+" FROM " + CAMPAIGNS_TABLE + " WHERE " + CAMPAIGNS_TABLE_CAMPAIGN_NAME + "='" + campaignName + "' AND "+CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+" = 1";
        Cursor cursor = DataBaseHelper.initializeDataBase(context).getRecord(condition);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static Cursor getCampaignsFromOffset(Context context, int offset,int CHUNK_LIMIT) {
        String sqlQuery = String.format("SELECT * FROM " + CAMPAIGNS_TABLE + " WHERE " + LOCAL_ID + " >" + offset + " ORDER BY " + LOCAL_ID + " DESC LIMIT  " + CHUNK_LIMIT + ";");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }

    public static Cursor getCampaign(Context context, long id) {
        String condition = "SELECT * FROM " + CAMPAIGNS_TABLE + " WHERE " + LOCAL_ID + "=" + id;
        return DataBaseHelper.initializeDataBase(context).getRecord(condition);
    }

    public static long updateCampaignById(ContentValues cv, Context context,long id) {
        return DataBaseHelper.initializeDataBase(context).updateDBRecord( CAMPAIGNS_TABLE,cv,LOCAL_ID+"="+id);
    }

    public static boolean deleteCampaignsByLocalId(String localIds, Context context) {

        String whereCondition =  LOCAL_ID + " IN(" + localIds + ")";

        if (localIds != null)
        {
            long status = DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGNS_TABLE, whereCondition);

            if (status > 0)
            {
                return true;
            }

            return false;
        } else {

            return false;
        }
    }


    public static Cursor getCampaignsByLocalIds(String localIds, Context context) {


        String whereCondition =  LOCAL_ID + " IN( "+ localIds +" )";

        if (localIds != null) {

            String sqlQuery = String.format("SELECT * FROM " + CAMPAIGNS_TABLE + " WHERE " + whereCondition + ";");
            return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

        } else {

            return null;
        }
    }


    public static boolean updateCampaignNextSchedule(Context context,long scheduleId,String nextSchedule)
    {
        try
        {

            String whereCondition = LOCAL_ID+"=?";
            String[] args = new String[]{String.valueOf(scheduleId)};
            ContentValues cv = new ContentValues(1);
            cv.put(SCHEDULE_TABLE_NEXT_SCHEDULE_AT,nextSchedule);
            long updated = DataBaseHelper.initializeDataBase(context).updateDBRecord(SCHEDULE_CAMPAIGNS_TABLE,cv,whereCondition,args);
            return (updated>=1);

        }catch (Exception e)
        {
            return false;
        }

    }

    public static Cursor getScheduleInfo(Context context,long scheduleId)
    {
        try {
            String whereCondition = LOCAL_ID + "=?";
            String sqlQuery = String.format("SELECT * FROM " + SCHEDULE_CAMPAIGNS_TABLE+ " WHERE " + whereCondition + ";");

            return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery, new String[]{String.valueOf(scheduleId)});
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //get saved rss feeds
    public static  Cursor getSavedFeeds(String serverIdList,Context context)
    {
        String sqlQuery =String.format("SELECT "+RSS_FEED_CAMPAIGN_SERVER_ID+" FROM "+RSS_FEEDS_TABLE+" WHERE " +RSS_FEED_CAMPAIGN_SERVER_ID+" IN ("+serverIdList+ ");");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }

    public static Cursor getRSSFeeds(Context context)
    {
        String sqlQuery = "SELECT * FROM "+RSS_FEEDS_TABLE+" WHERE "+RSS_FEED_IS_SKIP+" = 0";
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }

    public static  boolean deleteGarbageFeeds(String serverIds,Context context)
    {

        String whereCondition=RSS_FEED_CAMPAIGN_SERVER_ID+">0 AND "+RSS_FEED_CAMPAIGN_SERVER_ID+" NOT IN("+serverIds+")";

        if(serverIds!=null)
        {


            long status= DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(RSS_FEEDS_TABLE,whereCondition);

            if(status>0)
            {
                return true;
            }
            return false;
        }
        else
        {
            return false;
        }

    }

    //get saved rss feeds
    public static  Cursor getSavedTickerTexts(String serverIdList,Context context)
    {

        String sqlQuery =String.format("SELECT "+TICKER_TEXT_SERVER_ID+" FROM "+TICKER_TEXT_TABLE+" WHERE " +TICKER_TEXT_SERVER_ID+" IN ("+serverIdList+ ");");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);

    }

    public static  boolean deleteGarbageTickers(String serverIds,Context context)
    {

        String whereCondition=TICKER_TEXT_SERVER_ID+">0 AND "+TICKER_TEXT_SERVER_ID+" NOT IN("+serverIds+")";

        if(serverIds!=null)
        {

            long status= DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(TICKER_TEXT_TABLE,whereCondition);

            if(status>0)
            {
                return true;
            }
            return false;
        }
        else
        {
            return false;
        }
    }

    //get saved ticker text
    public static  Cursor getTickerTexts(Context context)
    {
        String sqlQuery =String.format("SELECT * FROM "+TICKER_TEXT_TABLE+" WHERE "+ TICKER_TEXT_IS_SKIP+"=0 ORDER BY " +LOCAL_ID +" ASC");
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery);
    }



    public static long updateCampaignByName(ContentValues cv, Context context,String name) {
        return DataBaseHelper.initializeDataBase(context).updateDBRecord( CAMPAIGNS_TABLE,cv,CAMPAIGNS_TABLE_CAMPAIGN_NAME+"='"+name+"'");
    }

    public static long updateCampaignIsSkip(Context context,boolean isSkip) {


        int skipValue=isSkip?1:0;

        ContentValues cv=new ContentValues();
        cv.put(CAMPAIGNS_TABLE_IS_SKIP,skipValue);

        return DataBaseHelper.initializeDataBase(context).updateDBRecord( CAMPAIGNS_TABLE,cv,null);

    }

    public static boolean updateIsDownload(Context context,int isDownload,long campaignLocalId){
        try
        {
            String whereCondition = LOCAL_ID+"=?";
            String[] args = new String[]{String.valueOf(campaignLocalId)};
            ContentValues cv = new ContentValues(isDownload);
            cv.put(CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED,isDownload);
            long updated = DataBaseHelper.initializeDataBase(context).updateDBRecord(CAMPAIGNS_TABLE,cv,whereCondition,args);
            return (updated>=1);
        }catch (Exception e)
        {
            return false;
        }
    }

    public static boolean updateSkip(Context context,String campaignName,int isSkip)
    {
        try
        {
            String whereCondition = CAMPAIGNS_TABLE_CAMPAIGN_NAME+"=?";
            String[] args = new String[]{campaignName};
            ContentValues cv = new ContentValues(1);
            cv.put(CAMPAIGNS_TABLE_IS_SKIP,isSkip);
            long updated = DataBaseHelper.initializeDataBase(context).updateDBRecord(CAMPAIGNS_TABLE,cv,whereCondition,args);
            return (updated>=1);
        }catch (Exception e)
        {
            return false;
        }

    }
}