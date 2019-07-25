package com.ibetter.www.adskitedigi.adskitedigi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.model.OptionalModel;

import java.util.ArrayList;

import static com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper.LOCAL_ID;

public class ActionsDBHelper
{
    private Context context;
    public final static String CUSTOMER_TABLE="customer_table";
    public final static String CUSTOMER_NAME="name";
    public final static String CUSTOMER_NUMBER = "number";
    public final static String CUSTOMER_STATUS= "status" ;
    public final static String CUSTOMER_CREATED_AT="created_at";
    public final static String CUSTOMER_UPDATED_AT="updated_at";
    public final static String CUSTOMER_ACTION_TEXT="action_text";
    public final static String CUSTOMER_ACTION_TEMPLATE_ID="temp_id";

    /* Interactive Actions  Optional Data Field Names  DataBase Table */
    public final static String OPTIONAL_FIELDS_TABLE="optional_field";
    public final static String OPTIONAL_FIELD_NAME="name";
    public final static String OPTIONAL_FIELD_FLAG = "flag";

    /* Interactive Actions  Optional Data Table */
    public final static String OPTIONAL_DATA_TABLE="optional_data";
    public final static String OPTIONAL_DATA_CUSTOMER_ID="c_id";
    public final static String OPTIONAL_DATA_KEY = "key";
    public final static String OPTIONAL_DATA_VALUE ="value";

    public final static String DELETE_OPTIONAL_DATA_TRIGGER =" CREATE TRIGGER delete_optional_data_trigger  BEFORE DELETE ON "+
            CUSTOMER_TABLE+" FOR EACH ROW BEGIN DELETE from "+OPTIONAL_DATA_TABLE+"  WHERE "+OPTIONAL_DATA_CUSTOMER_ID+" = OLD._id"+"; END";


    public  final static String CREATE_CUSTOMER_TABLE="CREATE TABLE " + CUSTOMER_TABLE
                            + " ("
                                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                    +CUSTOMER_NAME + " TEXT,"
        + CUSTOMER_NUMBER + " TEXT,"
        +CUSTOMER_STATUS+ " INTEGER DEFAULT 0,"
        +CUSTOMER_CREATED_AT+ " INTEGER,"
        +CUSTOMER_UPDATED_AT+ " INTEGER,"
        +CUSTOMER_ACTION_TEMPLATE_ID+ " INTEGER,"
        +CUSTOMER_ACTION_TEXT + " TEXT );";


    public  final  static  String CREATE_OPTIONAL_FIELDS_TABLE="CREATE TABLE " + ActionsDBHelper.OPTIONAL_FIELDS_TABLE
            + " ("
            + "_id INTEGER PRIMARY KEY,"
            + ActionsDBHelper.OPTIONAL_FIELD_NAME + " TEXT,"
            + ActionsDBHelper.OPTIONAL_FIELD_FLAG + " INTEGER );";

    public final  static String CREATE_OPTIONAL_DATA_TABLE="CREATE TABLE " + ActionsDBHelper.OPTIONAL_DATA_TABLE
            + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ActionsDBHelper.OPTIONAL_DATA_CUSTOMER_ID + " INTEGER,"
            + ActionsDBHelper.OPTIONAL_DATA_KEY + " TEXT,"
            + ActionsDBHelper.OPTIONAL_DATA_VALUE + " TEXT );";



    public ActionsDBHelper(Context context)
    {
        this.context = context;
    }


    //save customer info
    public boolean insertCustomerInfo(ContentValues cv,ArrayList<OptionalModel> dataList)
    {
        long customerId=DataBaseHelper.initializeDataBase(context).saveRecordToDBTable(cv,CUSTOMER_TABLE);
        if(customerId>=1)
        {
            Log.i("SAVE", "DB customerId:"+customerId);
            if(dataList!=null)
            {
                int size=dataList.size();
                for (int i=0;i<size;i++)
                {
                    OptionalModel model=dataList.get(i);
                    ContentValues optionalCV=new ContentValues();
                    optionalCV.put(OPTIONAL_DATA_CUSTOMER_ID,customerId);
                    optionalCV.put(OPTIONAL_DATA_KEY,model.getKey());
                    optionalCV.put(OPTIONAL_DATA_VALUE,model.getValue());
                    DataBaseHelper.initializeDataBase(context).saveRecordToDBTable(optionalCV,OPTIONAL_DATA_TABLE);
                }

            }
            return true;

        }else
        {
            return false;
        }
    }


    //insert  OptionalField name
    public long insertInteractiveOptionalField(ContentValues cv)
    {
       return DataBaseHelper.initializeDataBase(context).saveRecordToDBTable(cv,OPTIONAL_FIELDS_TABLE);
    }


    public boolean checkOptionalFieldIsExists(int localId)
    {
        String sqlStatement = "SELECT * FROM " + OPTIONAL_FIELDS_TABLE + " WHERE "+LOCAL_ID+" = "+localId ;
        Cursor cursor= DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
        if(cursor!=null && cursor.moveToFirst())
        {
            return true;
        }else
        {
            return false;
        }
    }

    //update OptionalField name
    public boolean updateInteractiveOptionalField(ContentValues cv, int localId)
    {
        String whereCondition =DataBaseHelper.LOCAL_ID+" = "+localId;

        return ( DataBaseHelper.initializeDataBase(context).updateDBRecord(OPTIONAL_FIELDS_TABLE,cv,whereCondition) >=1);
    }


    //delete schedule layout based on schedule layout server id
    public boolean deleteCustomerInfo(String localId)
    {
        Log.i("schedule","deleting schedule");
        String whereCondition = LOCAL_ID+" = '"+localId+"'";

        return (DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(CUSTOMER_TABLE,whereCondition)>=1);
    }

    //update schedule details based on schedule server id
    public boolean updateCustomerActionData(ContentValues cv, long localId)
    {
        Log.i("update", "DB:"+localId);
        String whereCondition =DataBaseHelper.LOCAL_ID+" = '"+localId+"'";

        return ( DataBaseHelper.initializeDataBase(context).updateDBRecord(CUSTOMER_TABLE,cv,whereCondition) >=1);
    }


     public Cursor getCustomerActionText(int templateId)
     {
         String whereCondition = CUSTOMER_ACTION_TEMPLATE_ID+" = "+templateId+" AND "+CUSTOMER_ACTION_TEXT+" IS NOT NULL AND "+CUSTOMER_STATUS +" == 0 ";
         String sqlStatement = "SELECT * FROM " + CUSTOMER_TABLE + " WHERE " + whereCondition;
         return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
     }


    public Cursor getCustomerDataByStatus(int templateId,String status)
    {
        //extra_info,
        String sqlStatement = "SELECT * FROM " + CUSTOMER_TABLE + " WHERE "+CUSTOMER_ACTION_TEMPLATE_ID+" = "+templateId+"  AND "+CUSTOMER_STATUS +" = '"+status+"'";
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
    }

    public Cursor getCustomerDataByTempId(int templateId)
    {
        //customer interaction optional data
        String sqlStatement = "SELECT * FROM " + CUSTOMER_TABLE + " WHERE "+CUSTOMER_ACTION_TEMPLATE_ID+" = "+templateId ;
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
    }



    public Cursor getExportDataByStatus(int templateId,String status,long startDate,long endDate)
    {
        //extra_info,
        String sqlStatement = "SELECT * FROM " + CUSTOMER_TABLE + " WHERE "+CUSTOMER_ACTION_TEMPLATE_ID+" = "+templateId+" AND "+CUSTOMER_STATUS +" = '"+status+"' AND "+CUSTOMER_CREATED_AT+" >= "+startDate+" AND "+CUSTOMER_CREATED_AT+" <= "+endDate;
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
    }

    public Cursor getExportDataByTempId(int templateId,long startDate,long endDate)
    {
        //customer interaction optional data
        String sqlStatement = "SELECT * FROM " + CUSTOMER_TABLE + " WHERE "+CUSTOMER_ACTION_TEMPLATE_ID+" = "+templateId+" AND "+CUSTOMER_CREATED_AT+" >= "+startDate+" AND "+CUSTOMER_CREATED_AT+" <= "+endDate;
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
    }



    public Cursor getCustomerOptionalData(int customer_id)
    {
        //customer interaction optional data
        String sqlStatement = "SELECT * FROM " + OPTIONAL_DATA_TABLE + " WHERE "+OPTIONAL_DATA_CUSTOMER_ID+" = "+customer_id ;
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
    }

    public Cursor getInteractiveOptionalFields()
    {
        //customer interaction optional fields
        String sqlStatement = "SELECT * FROM " +OPTIONAL_FIELDS_TABLE;
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
    }


    public Cursor getAppInvokePackageName(int fieldId,int fieldStatus)
    {
        //extra_info,
        String sqlStatement = "SELECT * FROM " + OPTIONAL_FIELDS_TABLE + " WHERE "+DataBaseHelper.LOCAL_ID+" = "+fieldId+" AND "+OPTIONAL_FIELD_FLAG +" = "+fieldStatus;
        return DataBaseHelper.initializeDataBase(context).getRecord(sqlStatement);
    }


    public void deleteAllFields()
    {
        DataBaseHelper.initializeDataBase(context).deleteRecordFromDBTable(OPTIONAL_FIELDS_TABLE, null);

    }
}
