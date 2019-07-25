package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.ActionsDBHelper;
import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class ActionModel
{
    //temp field
    public static final int ACTION_TEMPLATE_ID=0;

    public static final  boolean ACTION_FLAG=false;

    /*save and get Action Template Id received from the SM app*/
    public  boolean saveActionTempId(Context context,int templateId)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putInt(context.getString(R.string.action_template_id),templateId);

        return userDetailsSPEditor.commit();
    }

    public  int getActionTemplateId(Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.action_template_id),ACTION_TEMPLATE_ID);
    }

    /*save and get Action TemplateId and Action Form URL received from the SM app*/
    public  boolean saveActionFormUrl(Context context,int templateId,String actionFormUrl)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putInt(context.getString(R.string.action_template_id),templateId);
        userDetailsSPEditor.putString(context.getString(R.string.interactive_action_form_url),actionFormUrl);

        return userDetailsSPEditor.commit();
    }



    //get Action Form URL from  User Interactive Action form
    public  String getActionFormUrl(Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.interactive_action_form_url),null);
    }

    /*save and get Action TemplateId and Action Form URL received from the SM app*/
    public  boolean saveAppInvokePackageName(Context context,int templateId,String packageName)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putInt(context.getString(R.string.action_template_id),templateId);
        userDetailsSPEditor.putString(context.getString(R.string.interactive_action_app_invoke),packageName);

        return userDetailsSPEditor.commit();
    }

    //get Action Form URL from  User Interactive Action form
    public  String getAppInvokePackageName(Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getString(context.getString(R.string.interactive_action_app_invoke),null);
    }



    /*set adn get customer action layout saved state */
    public  boolean saveDisplayActionLayoutState(Context context,boolean flag)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putBoolean(context.getString(R.string.display_customer_interactive_action),flag);

        return userDetailsSPEditor.commit();
    }

    public  boolean getDisplayActionLayoutState (Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getBoolean(context.getString(R.string.display_customer_interactive_action),ACTION_FLAG);
    }


    /*set and get customer action layout scrolling text saved state */
    public  boolean saveDisplayActionScrollingTextState(Context context,boolean flag)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putBoolean(context.getString(R.string.display_customer_interactive_action_text),flag);

        return userDetailsSPEditor.commit();
    }

    public  boolean getDisplayActionScrollingTextState(Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getBoolean(context.getString(R.string.display_customer_interactive_action_text),ACTION_FLAG);
    }

    //get and send customer interactive action layout settings to SM
    public String getActionLayoutSettings(Context context)
    {
        try
        {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put(context.getString(R.string.display_customer_interactive_action), getDisplayActionLayoutState(context));
            jsonObject.put(context.getString(R.string.display_customer_interactive_action_text), getDisplayActionScrollingTextState(context));

            int temId=getActionTemplateId(context);
            Log.i("InteractionSettings","temId:"+temId);
            jsonObject.put(context.getString(R.string.action_template_id), temId);
            jsonObject.put(context.getString(R.string.interactive_action_form_url), getActionFormUrl(context));

            JSONArray optionalData=getInteractiveOptionalFields(context);
            if(optionalData!=null)
            {
                //Log.i("InteractionSettings","getInteractiveOptionalFields:"+optionalData.toString());
                jsonObject.put(context.getString(R.string.interactive_optional_data),optionalData);
            }

            jsonObject.put(context.getString(R.string.interactive_action_app_invoke),getAppInvokePackageName(context));

            jsonObject.put(context.getString(R.string.interactive_inactivity_timer_flag), getActionInactivityTimesStatus(context));
            jsonObject.put(context.getString(R.string.interactive_inactivity_timer), getActionInactivityTime(context));

            return jsonObject.toString();

        } catch (Exception e)
        {
            e.printStackTrace();
            return  null;
        }

    }

    private JSONArray getInteractiveOptionalFields(Context context)throws JSONException
    {
        JSONArray jsonArray=new JSONArray();

        Cursor cursor=new ActionsDBHelper(context).getInteractiveOptionalFields();
        if(cursor!=null && cursor.moveToFirst())
        {
            do {
                JSONObject obj=new JSONObject();
                obj.put(context.getString(R.string.interactive_od_id),cursor.getInt(cursor.getColumnIndex(DataBaseHelper.LOCAL_ID)));
                obj.put(context.getString(R.string.interactive_od_value),cursor.getString(cursor.getColumnIndex(ActionsDBHelper.OPTIONAL_FIELD_NAME)));
                int status=cursor.getInt(cursor.getColumnIndex(ActionsDBHelper.OPTIONAL_FIELD_FLAG));
                if(status==1)
                {
                    obj.put(context.getString(R.string.interactive_od_flag),true);
                }else
                {
                    obj.put(context.getString(R.string.interactive_od_flag),false);
                }
                jsonArray.put(obj);

            }while (cursor.moveToNext());

            return jsonArray;

        }else
        {
            return null;
        }

    }


    public String getRequestedActionsDataJson(Context context,String status)
    {
       // Log.i("ActionsDataJson","getRequestedActionsDataJson:"+status);
        try
        {
             int templateId = getActionTemplateId(context);
             Log.i("ActionsDataJson","getRequestedActionsDataJson templateId:"+templateId);

            Cursor cursor=null;
            if(status!=null && !status.equalsIgnoreCase("null"))
            {
                int actionStatus=Integer.parseInt(status);
                if(actionStatus==0)
                {
                    cursor= new ActionsDBHelper(context).getCustomerDataByStatus(templateId,status);

                }else if(actionStatus==1)
                {
                    cursor= new ActionsDBHelper(context).getCustomerDataByStatus(templateId,status);
                }else
                {
                    cursor= new ActionsDBHelper(context).getCustomerDataByTempId(templateId);
                }

            }else
            {
                cursor= new ActionsDBHelper(context).getCustomerDataByTempId(templateId);
            }
            JSONArray jsonArray=new JSONArray();

            if(cursor!=null && cursor.moveToFirst())
            {
                do {
                    JSONObject jsonObject=new JSONObject();

                    jsonObject.put(DataBaseHelper.LOCAL_ID, cursor.getString(cursor.getColumnIndex(DataBaseHelper.LOCAL_ID)));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_NAME, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_NAME)));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_NUMBER, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_NUMBER)));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_CREATED_AT, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_CREATED_AT)));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_ACTION_TEXT, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_ACTION_TEXT)));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_STATUS, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_STATUS)));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_UPDATED_AT,cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_UPDATED_AT)));

                   // jsonObject.put(ActionsDBHelper.CUSTOMER_EXTRA_INFO,cursor.getString(cursor.getColumnIndex("extra_info")));


                    jsonArray.put(jsonObject);

                }while (cursor.moveToNext());

                if(jsonArray!=null && jsonArray.length()>=1)
                {
                    JSONObject finalobject = new JSONObject();
                    finalobject.put("customers",jsonArray);

                   // Log.i("ActionsDataJson","resourcesString  jsonArray:"+finalobject.toString());

                    return finalobject.toString();

                }else
                {
                    Log.i("ActionsDataJson","resourcesString final jsonArray data null");
                    return null;
                }

            }else
            {
                Log.i("ActionsDataJson","resourcesString cursor data null");

                return null;
            }

        }catch (Exception e)
        {
            Log.i("ActionsDataJson","resourcesString Exception happen");
            e.printStackTrace();
            return null;
        }
    }


    public String getExportedActionsDataJson(Context context,String status,long startDate,long endDate)
    {

        try
        {
            int templateId = getActionTemplateId(context);
            Log.i("ActionsDataJson","getExportedActionsDataJson templateId:"+templateId);

            Cursor cursor=null;
            if(status!=null && !status.equalsIgnoreCase("null"))
            {
                int actionStatus=Integer.parseInt(status);
                if(actionStatus==0)
                {
                    cursor= new ActionsDBHelper(context).getExportDataByStatus(templateId,status,startDate,endDate);

                }else if(actionStatus==1)
                {
                    cursor= new ActionsDBHelper(context).getExportDataByStatus(templateId,status,startDate,endDate);
                }else
                {
                    cursor= new ActionsDBHelper(context).getExportDataByTempId(templateId,startDate,endDate);
                }

            }else
            {
                cursor= new ActionsDBHelper(context).getExportDataByTempId(templateId,startDate,endDate);
            }
            JSONArray jsonArray=new JSONArray();

            if(cursor!=null && cursor.moveToFirst())
            {
                Log.i("ActionsDataJson","getExportedActionsDataJson cursor:"+cursor.getCount());
                do {
                    JSONObject jsonObject=new JSONObject();
                    int localId=cursor.getInt(cursor.getColumnIndex(DataBaseHelper.LOCAL_ID));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_NAME, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_NAME)));
                    jsonObject.put(ActionsDBHelper.CUSTOMER_NUMBER, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_NUMBER)));

                    Cursor dataCursor=new ActionsDBHelper(context).getCustomerOptionalData(localId);
                    if(dataCursor!=null && dataCursor.moveToFirst())
                    {
                        JSONArray dataArray=new JSONArray();

                        do {
                            JSONObject dateObject=new JSONObject();
                            dateObject.put(ActionsDBHelper.OPTIONAL_DATA_KEY, dataCursor.getString(dataCursor.getColumnIndex(ActionsDBHelper.OPTIONAL_DATA_KEY)));
                            dateObject.put(ActionsDBHelper.OPTIONAL_DATA_VALUE, dataCursor.getString(dataCursor.getColumnIndex(ActionsDBHelper.OPTIONAL_DATA_VALUE)));
                            Log.i("ActionsDataJson","getExportedActionsDataJson dateObject:"+dateObject.toString());
                            dataArray.put(dateObject);

                        }while (dataCursor.moveToNext());

                        jsonObject.put("data",dataArray);
                    }

                    jsonArray.put(jsonObject);

                }while (cursor.moveToNext());

                if(jsonArray!=null && jsonArray.length()>=1)
                {
                    JSONObject finalobject = new JSONObject();
                    finalobject.put("customers",jsonArray);

                    Log.i("ActionsDataJson","resourcesString  jsonArray:"+finalobject.toString());

                    return finalobject.toString();

                }else
                {
                    Log.i("ActionsDataJson","resourcesString final jsonArray data null");
                    return null;
                }

            }else
            {
                Log.i("ActionsDataJson","resourcesString cursor data null");
                return null;
            }

        }catch (Exception e)
        {
            Log.i("ActionsDataJson","resourcesString Exception happen");
            e.printStackTrace();
            return null;
        }

    }



    public String getInteractiveOptionalData(Context context,int  customerId)
    {
        try
        {
            Cursor cursor= new ActionsDBHelper(context).getCustomerOptionalData(customerId);
            if(cursor!=null && cursor.moveToFirst())
            {
                JSONArray jsonArray=new JSONArray();
                do {
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put(ActionsDBHelper.OPTIONAL_DATA_KEY, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.OPTIONAL_DATA_KEY)));
                    jsonObject.put(ActionsDBHelper.OPTIONAL_DATA_VALUE, cursor.getString(cursor.getColumnIndex(ActionsDBHelper.OPTIONAL_DATA_VALUE)));
                    jsonArray.put(jsonObject);

                }while (cursor.moveToNext());

                if(jsonArray!=null && jsonArray.length()>=1)
                {
                    JSONObject finalobject = new JSONObject();
                    finalobject.put("data",jsonArray);

                  //  Log.i("ActionsDataJson","getInteractiveOptionalData:"+finalobject.toString());

                    return finalobject.toString();

                }else
                {
                    Log.i("ActionsDataJson","getInteractiveOptionalData null");
                    return null;
                }

            }else
            {
                Log.i("ActionsDataJson","getInteractiveOptionalData null");

                return null;
            }

        }catch (Exception e)
        {
            Log.i("ActionsDataJson","resourcesString Exception happen");
            e.printStackTrace();
            return null;
        }

    }




    public void setupActionSettings(Context context,String settingsJson)
    {
        try
        {
            JSONObject jsonObject=new JSONObject(settingsJson);
            //Log.i("InteractionSettings","setupActionSettings jsonObject:"+jsonObject.toString());
            String actionString=null;

            if(jsonObject.has(context.getString(R.string.interactive_inactivity_timer_flag)))
            {
                saveActionInactivityTimesStatus(context,jsonObject.getBoolean(context.getString(R.string.interactive_inactivity_timer_flag)));
            }
            if(jsonObject.has(context.getString(R.string.interactive_inactivity_timer)))
            {
                saveActionInactivityTime(context,jsonObject.getInt(context.getString(R.string.interactive_inactivity_timer)));
            }

            if(jsonObject.has(context.getString(R.string.action_template_id)))
            {
                int templateId=jsonObject.getInt(context.getString(R.string.action_template_id));
                saveActionTemplateInSP(context,templateId,jsonObject);

                //user interaction option data fields info
                if(jsonObject.has(context.getString(R.string.interactive_optional_data)))
                {
                    //create/update interaction optional fields data
                    saveInteractiveOptionalFileds(context,jsonObject.getJSONArray(context.getString(R.string.interactive_optional_data)));
                }

            }else
            {
                if(jsonObject.has(context.getString(R.string.display_customer_interactive_action)))
                {
                    actionString=context.getString(R.string.display_customer_interactive_action);
                    saveDisplayActionLayoutState(context,jsonObject.getBoolean(context.getString(R.string.display_customer_interactive_action)));
                }
                else if(jsonObject.has(context.getString(R.string.display_customer_interactive_action_text)))
                {
                    actionString=context.getString(R.string.display_customer_interactive_action_text);
                    saveDisplayActionScrollingTextState(context,jsonObject.getBoolean(context.getString(R.string.display_customer_interactive_action_text)));
                }

                Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra(context.getString(R.string.action),actionString);
                context.sendBroadcast(intent);
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    //display customized action layout in DisplayLocalFoloderAds Activity based on template ID from SM
    public void saveActionTemplateInSP(Context context,int tempId,JSONObject jsonObject)throws JSONException
    {
        switch (tempId)
        {
            case 0:
                //save Customer Queue Form template ID
                saveActionTempId(context,tempId);

                break;

            case 1:
                //save Customer Action Feedback Form template ID
                saveActionFormUrl(context,tempId,jsonObject.getString(context.getString(R.string.interactive_action_form_url)));
                break;

            case 2:
                //save Customer Action Feedback Form template ID
                saveAppInvokePackageName(context,tempId,jsonObject.getString(context.getString(R.string.interactive_action_app_invoke)));
                break;

        }

    }


    private void saveInteractiveOptionalFileds(Context context,JSONArray jsonArray)throws JSONException
    {
        Log.i("InteractiveOptional","jsonArray:"+jsonArray.toString());
        if(jsonArray!=null)
        {
            int length=jsonArray.length();
            for (int i=0;i<length;i++)
            {
                JSONObject object=(JSONObject)jsonArray.get(i);
                String name=object.getString(context.getString(R.string.interactive_od_value));
                boolean flag=object.getBoolean(context.getString(R.string.interactive_od_flag));
                ContentValues cv=new ContentValues();
                cv.put(ActionsDBHelper.OPTIONAL_FIELD_NAME,name);
                if(flag)
                {
                    cv.put(ActionsDBHelper.OPTIONAL_FIELD_FLAG,1);
                }else
                {
                    cv.put(ActionsDBHelper.OPTIONAL_FIELD_FLAG,0);
                }

                if(object.has(context.getString(R.string.interactive_od_id)))
                {
                    int id=object.getInt(context.getString(R.string.interactive_od_id));

                    boolean isExists= new ActionsDBHelper(context).checkOptionalFieldIsExists(id);
                    if(isExists)
                    {
                        //update the field name
                        new ActionsDBHelper(context).updateInteractiveOptionalField(cv,id);
                    }else
                    {
                        //insert field name
                        cv.put(DataBaseHelper.LOCAL_ID,id);
                        new ActionsDBHelper(context).insertInteractiveOptionalField(cv);
                    }

                }else
                {
                    //insert field name
                    cv.put(DataBaseHelper.LOCAL_ID,object.getInt(context.getString(R.string.interactive_od_id)));
                    new ActionsDBHelper(context).insertInteractiveOptionalField(cv);
                }

            }
        }

    }


    public static void updateCustomerActionStatus(String localId,Context context)
    {
        try
        {
            long _id=Long.parseLong(localId);
            if(_id>0)
            {
                ContentValues cv=new ContentValues();
                cv.put(ActionsDBHelper.CUSTOMER_STATUS,1);
                cv.put(ActionsDBHelper.CUSTOMER_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
                new ActionsDBHelper(context).updateCustomerActionData(cv,_id);

            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void updateCustomerActionText(Context context,String jsonData)
    {

        try
        {

            JSONObject object=new JSONObject(jsonData);
            long localId=object.getLong(DataBaseHelper.LOCAL_ID);
            String actionText=object.getString(ActionsDBHelper.CUSTOMER_ACTION_TEXT);

            ContentValues cv=new ContentValues();
            cv.put(ActionsDBHelper.CUSTOMER_ACTION_TEXT,actionText);
            cv.put(ActionsDBHelper.CUSTOMER_UPDATED_AT,Calendar.getInstance().getTimeInMillis());
            new ActionsDBHelper(context).updateCustomerActionData(cv,localId);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /*save in SP auto campaign rule setting  */
    public boolean saveAutoCampaignRuleSettingInSP(Context context,boolean flag)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putBoolean(context.getString(R.string.auto_campaign_rule_setting),flag);

        return userDetailsSPEditor.commit();
    }

    //get and prepare JSON  send auto campaign rule setting to SM
    public String getAutoCampRuleSettingsJSON(Context context) throws JSONException
    {
        JSONObject jsonObject=new JSONObject();
        try
        {
            jsonObject.put(context.getString(R.string.auto_campaign_rule_setting), getAutoCampaignRuleSetting(context));

        }catch (Exception e)
        {
            jsonObject.put(context.getString(R.string.auto_campaign_rule_setting),false);

        }
        return  jsonObject.toString();

    }

    //get and  send auto campaign rule setting to SM
    public  boolean getAutoCampaignRuleSetting (Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getBoolean(context.getString(R.string.auto_campaign_rule_setting),ACTION_FLAG);
    }


    /*save interactive inactivity time flag from the SM app*/
    public  boolean saveActionInactivityTimesStatus (Context context,boolean flag)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putBoolean(context.getString(R.string.interactive_inactivity_timer_flag),flag);
        return userDetailsSPEditor.commit();
    }

    /*save interactive inactivity time  from the SM app*/
    public  boolean saveActionInactivityTime(Context context,int seconds)
    {
        SharedPreferences.Editor userDetailsSPEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(context).edit();
        userDetailsSPEditor.putInt(context.getString(R.string.interactive_inactivity_timer),seconds);
        return userDetailsSPEditor.commit();
    }


    //get Action Form URL from  User Interactive Action form
    public  boolean getActionInactivityTimesStatus(Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getBoolean(context.getString(R.string.interactive_inactivity_timer_flag),false);
    }

    public  int getActionInactivityTime(Context context)
    {
        return  new SharedPreferenceModel().getUserDetailsSharedPreference(context).getInt(context.getString(R.string.interactive_inactivity_timer),ACTION_TEMPLATE_ID);
    }

    public boolean getInactivityTimerFlag(Context context)
    {
        if(getActionInactivityTimesStatus(context))
        {
            int inActivityTime=getActionInactivityTime(context);
            //Log.i("disconnectHandler","startInactivityTimer:time"+inActivityTime);
            if(inActivityTime>0)
            {
              return true;
            }else
            {
                return false;
            }
        }else
        {
            return false;
        }
    }

    public static boolean checkAccessibilityService()
    {
        String action=android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS;
        Log.i("OtherAppInvokeService","checkAccessibilityService action:"+action);
        if(action!=null)
        {
            return true;
        }else
        {
            return false;
        }
    }

}
