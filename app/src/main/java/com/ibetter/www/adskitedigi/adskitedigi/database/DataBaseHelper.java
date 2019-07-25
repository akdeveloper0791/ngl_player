package com.ibetter.www.adskitedigi.adskitedigi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel.CAMPAIGN_RULES_TABLE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel.CREATE_RULE_CAMPAIGNS_TABLE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel.DELETE_RULE_CAMPAIGNS_TRIGGER;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel.RULE_DELAY_DURATION;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignRulesDBModel.RULE_SERVER_ID;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGNS_TABLE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_PRIORITY;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CAMPAIGN_TABLE_SCHEDULE_TYPE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.CREATE_SCHEDULE_CAMPAIGNS_TABLE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.DELETE_SCHEDULE_CAMPAIGNS_TRIGGER;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.SCHEDULE_CAMPAIGNS_TABLE;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.SCHEDULE_TABLE_ADDITIONAL_INFO;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.SCHEDULE_TABLE_NEXT_SCHEDULE_AT;
import static com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel.SCHEDULE_TABLE_SCHEDULE_PRIORITY;
import static com.ibetter.www.adskitedigi.adskitedigi.metrics.HandleDelayRulesDB.CREATE_HANDLE_DELAY_RULE_TABLE;

/**
 * Created by ibetter-Dell on 17-11-16.
*/


public class DataBaseHelper extends SQLiteOpenHelper {

    public static boolean isDBUpdated = false;
    private static final int DATABASE_VERSION =25;//15

    //private static SQLiteDatabase mDb;
    private static final String TAG = "AdsKite Digi";
    static Context context1;
    public static SQLiteDatabase database = null;
    private static DataBaseHelper mInstance = null;
    public final static String LOCAL_ID="_id";

    public DataBaseHelper(Context context)
    {
        super(context, "adskite_digi", null, DATABASE_VERSION);
        context1 = context;

    }

    //close cursor
    public static synchronized void closeCursor(Cursor cursor) {
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {

        }
    }

    public static synchronized DataBaseHelper initializeDataBase(Context context)
    {
        if (database == null || mInstance == null)
        {
            mInstance = new DataBaseHelper(context);
        }
        return mInstance;
    }

    public synchronized SQLiteDatabase getDb() {

        if(database == null) {
            database = getWritableDatabase();
        }
        return database;
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
            database = null;
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

        db.execSQL(ActionsDBHelper.CREATE_CUSTOMER_TABLE);
        db.execSQL(ActionsDBHelper.CREATE_OPTIONAL_DATA_TABLE);
        db.execSQL(ActionsDBHelper.CREATE_OPTIONAL_FIELDS_TABLE);
        db.execSQL(CampaignRulesDBModel.CREATE_RULES_TABLE);
        db.execSQL(ActionsDBHelper.DELETE_OPTIONAL_DATA_TRIGGER);
        db.execSQL(CampaignsDBModel.CREATE_CAMPAIGNS_TABLE);
        db.execSQL(CampaignReportsDBModel.CREATE_CAMPAIGNS_REPORTS_TABLE);
        db.execSQL(CREATE_SCHEDULE_CAMPAIGNS_TABLE);
        db.execSQL(DELETE_SCHEDULE_CAMPAIGNS_TRIGGER);
        db.execSQL(CampaignRulesDBModel.CREATE_RULE_CAMPAIGNS_TABLE);
        db.execSQL(DELETE_RULE_CAMPAIGNS_TRIGGER);
        db.execSQL(CREATE_HANDLE_DELAY_RULE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        isDBUpdated = true;

        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        dataBaseUpation(newVersion, oldVersion, db);

    }

    //updating the database from older version to new version
    private void dataBaseUpation(int newVersion, int oldVersion, SQLiteDatabase database)
    {

        //check And restart
        new DoDataBaseUpdationChanges(database).execute(oldVersion, newVersion);
    }

    //do db changes in background
    private class DoDataBaseUpdationChanges extends AsyncTask<Integer, Void, Void>
    {
        SQLiteDatabase writableDb;

        public DoDataBaseUpdationChanges(SQLiteDatabase writableDb) {
            this.writableDb = writableDb;
        }

        protected Void doInBackground(Integer... params) {
            int oldVersion = params[0];
            int newVersion = params[1];

            while (oldVersion < newVersion)
            {
                oldVersion++;
                try
                {
                    dataBaseChanges(oldVersion);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result)
        {
            isDBUpdated = false;
        }

        //change the db
        private void dataBaseChanges(int oldVersion) throws Exception
        {

            switch (oldVersion) {
                case 15:

                    try {
                        writableDb.execSQL(ActionsDBHelper.CREATE_CUSTOMER_TABLE);
                    }catch (Exception E)
                    {
                        E.printStackTrace();
                    }
                    break;


                case 16:

                    try
                    {
                        writableDb.execSQL(ActionsDBHelper.CREATE_OPTIONAL_DATA_TABLE);
                        writableDb.execSQL(ActionsDBHelper.CREATE_OPTIONAL_FIELDS_TABLE);

                     }catch (Exception E)
                    {
                        E.printStackTrace();
                    }
                    break;

                case 17:

                    try
                    {
                        writableDb.execSQL(CampaignRulesDBModel.CREATE_RULES_TABLE);
                    }
                    catch (Exception E)
                    {
                        E.printStackTrace();
                    }
                    break;

                case 18:
                    try
                    {
                     writableDb.execSQL(ActionsDBHelper.DELETE_OPTIONAL_DATA_TRIGGER);
                    }
                    catch (Exception E)
                    {
                      E.printStackTrace();
                    }
                    break;

                case 19:
                    writableDb.execSQL(CampaignsDBModel.CREATE_CAMPAIGNS_TABLE);
                   break;
                case 20:
                    writableDb.execSQL(CampaignReportsDBModel.CREATE_CAMPAIGNS_REPORTS_TABLE);
                    break;
                case 21:
                    writableDb.execSQL("ALTER TABLE "+CAMPAIGNS_TABLE+" ADD COLUMN "+CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED+" INTEGER DEFAULT 0");
                    writableDb.execSQL("ALTER TABLE "+CAMPAIGNS_TABLE+" ADD COLUMN "+CAMPAIGN_TABLE_SCHEDULE_TYPE+" INTEGER DEFAULT 10");
                    writableDb.execSQL(CREATE_SCHEDULE_CAMPAIGNS_TABLE);
                    writableDb.execSQL(DELETE_SCHEDULE_CAMPAIGNS_TRIGGER);
                    break;
                case 22:
                    writableDb.execSQL("ALTER TABLE "+CAMPAIGNS_TABLE+" ADD COLUMN "+CAMPAIGN_TABLE_SCHEDULE_PRIORITY+" INTEGER DEFAULT 0");
                    writableDb.execSQL("ALTER TABLE "+SCHEDULE_CAMPAIGNS_TABLE+" ADD COLUMN "+SCHEDULE_TABLE_SCHEDULE_PRIORITY+" INTEGER DEFAULT 0");
                    writableDb.execSQL("ALTER TABLE "+SCHEDULE_CAMPAIGNS_TABLE+" ADD COLUMN "+SCHEDULE_TABLE_NEXT_SCHEDULE_AT+" DATETIME ");
                    break;
                case 23:
                    writableDb.execSQL("ALTER TABLE "+SCHEDULE_CAMPAIGNS_TABLE+" ADD COLUMN "+SCHEDULE_TABLE_ADDITIONAL_INFO+" TEXT ");
                    break;
                case 24:
                    writableDb.execSQL("ALTER TABLE "+CAMPAIGN_RULES_TABLE+" ADD COLUMN "+RULE_SERVER_ID+" INTEGER DEFAULT 0");
                    writableDb.execSQL("ALTER TABLE "+CAMPAIGN_RULES_TABLE+" ADD COLUMN "+RULE_DELAY_DURATION+" INTEGER DEFAULT 0");
                    writableDb.execSQL(CREATE_RULE_CAMPAIGNS_TABLE);
                    writableDb.execSQL(DELETE_RULE_CAMPAIGNS_TRIGGER);
                    break;
                case 25:
                    writableDb.execSQL(CREATE_HANDLE_DELAY_RULE_TABLE);
                    break;


            }
        }

    }


    // required files table operations
    public long saveRecordToDBTable(ContentValues values,String tableName)
    {
        SQLiteDatabase readDB=getReadableDatabase();
        return readDB.insert(tableName,null,values);

    }

    //flush db table
    public long deleteRecordFromDBTable(String tableName,String whereCondition)
    {
        try {
            if (database == null) {
                database = getWritableDatabase();
            }
            Log.i("delete table", "tableName" + "  " + whereCondition);

            return database.delete(tableName, whereCondition, null);
        }catch (Exception e)
        {

            e.printStackTrace();
            return 0;
        }

    }

    public long deleteRecordFromDBTable(String tableName,String whereCondition,String[] args)
    {
        if(database==null)
        {
            database=getWritableDatabase();
        }
        Log.i("delete table","tableName"+"  "+whereCondition);

        return database.delete(tableName,whereCondition,args);

    }

    //GET records
    public Cursor getRecord(String sqlStatement)
    {
        SQLiteDatabase readDB=getReadableDatabase();
        return readDB.rawQuery(sqlStatement,null);
    }

    public Cursor getRecord(String sql,String[] args)
    {
        SQLiteDatabase readDB=getReadableDatabase();
        return readDB.rawQuery(sql,args);
    }

    //Update db record
    public long updateDBRecord(String tableName,ContentValues cv,String whereCondition)
    {
       return getDb().update(tableName,cv,whereCondition,null);
    }

    public long updateDBRecord(String tableName,ContentValues cv,String whereCondition,String[] whereValues)
    {
        return getDb().update(tableName,cv,whereCondition,whereValues);
    }

}
