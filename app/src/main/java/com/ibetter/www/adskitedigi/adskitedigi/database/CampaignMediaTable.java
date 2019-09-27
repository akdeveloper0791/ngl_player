package com.ibetter.www.adskitedigi.adskitedigi.database;

import android.content.Context;
import android.database.Cursor;

import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE_SERVER_ID;

public class CampaignMediaTable {

    public final static String CAMPAIGN_MEDIA_TABLE = "campaign_media_table";
    public final static String CAMPAIGN_MEDIA_TABLE_CAMP_ID = "cmt_campaign_server_id";
    public final static String CAMPAIGN_MEDIA_TABLE_MEDIA_NAME = "cmt_media_name";

    public final static String CREATE_CAMPAIGN_MEDIA_TABLE_QUERY = "CREATE TABLE "+CAMPAIGN_MEDIA_TABLE+"" +
            "( "+CAMPAIGN_MEDIA_TABLE_CAMP_ID+" INTEGER DEFAULT 0," +
            ""+CAMPAIGN_MEDIA_TABLE_MEDIA_NAME+" VARCHAR(125) NOT NULL )";

    public final static String CAMPAIGN_MEDIA_TABLE_DELETE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS delete_media AFTER DELETE ON "+
            CAMPAIGNS_TABLE+" FOR EACH ROW BEGIN DELETE FROM "+CAMPAIGN_MEDIA_TABLE +" WHERE OLD."+CAMPAIGNS_TABLE_SERVER_ID+" = "+CAMPAIGN_MEDIA_TABLE_CAMP_ID+"; END;";

    public static void clearTable(Context context)
    {
        DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CAMPAIGN_MEDIA_TABLE,null,null);
    }

    public static boolean canDeleteMedia(Context context,long serverId,String mediaName){
        String whereCondition = CAMPAIGN_MEDIA_TABLE_MEDIA_NAME + "=? AND "+CAMPAIGN_MEDIA_TABLE_CAMP_ID+" != ?";
        String sqlQuery = String.format("SELECT * FROM " + CAMPAIGN_MEDIA_TABLE+ " WHERE " + whereCondition + " LIMIT 1;");

      Cursor cursor =   DataBaseHelper.initializeDataBase(context).getRecord(sqlQuery,new String[]{mediaName,String.valueOf(serverId)});
      return (cursor.getCount()>=1?false:true);
    }
}
