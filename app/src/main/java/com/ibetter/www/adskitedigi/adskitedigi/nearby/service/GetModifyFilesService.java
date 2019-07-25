package com.ibetter.www.adskitedigi.adskitedigi.nearby.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers.GetModifyFilesReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetModifyFilesService extends IntentService {

    public static boolean isServiceActive = false;
    private Context context;
    public static final int CHUNK_LIMIT = 100;

    public GetModifyFilesService()
    {
        super("GetModifyFilesService");
        context = GetModifyFilesService.this;
    }

    //start service
    public static void getFilesToModify(Context context, GetModifyFilesReceiver.GetModifyFilesReceiverCallBacks getModifyFilesReceiverCallBacks,int offset)
    {
        //start worker service to handle
        if(!isServiceActive)//avoid starting multiple services ,, if service is already running dont start it
        {
            //initialize the receiver to handle the call backs
            GetModifyFilesReceiver getModifyFilesReceiver = new GetModifyFilesReceiver(new Handler());
            getModifyFilesReceiver.setReceiver(getModifyFilesReceiverCallBacks);

            Intent intent = new Intent(context,GetModifyFilesService.class);
            intent.putExtra("result_receiver",getModifyFilesReceiver);
            intent.putExtra("offset",offset);
            context.startService(intent);
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
        isServiceActive = false;
    }

    public void onHandleIntent(Intent intent)
    {


        isServiceActive = true;

        ResultReceiver resultReceiver = intent.getParcelableExtra("result_receiver");
        int offset = intent.getIntExtra("offset",0);

        try {


            Cursor cursor= getMediaFilesToModify(offset,CHUNK_LIMIT);


            if (cursor != null && cursor.moveToFirst())
            {

                //prepare json
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode", GetModifyFilesReceiver.SEND_FILE);
                JSONArray modifyFilesArray = new JSONArray();

                do {
                    JSONObject fileInfoObject = getFileInfoObject(cursor.getLong(cursor.getColumnIndex(CampaignsDBModel.LOCAL_ID)));

                    if (fileInfoObject != null) {

                        if(modifyFilesArray.length()<CHUNK_LIMIT)
                        {
                            modifyFilesArray.put(fileInfoObject);
                        }else
                        {
                            //limit reached
                            break;
                        }

                    }
                }while (cursor.moveToNext());




                if(modifyFilesArray.length()>=1)
                {

                    //finished processing
                    jsonObject.put("filesArray",modifyFilesArray);
                    jsonObject.put("statusCode",GetModifyFilesReceiver.SEND_FILE);

                    Bundle bundle = new Bundle(1);
                    bundle.putString("info", jsonObject.toString());

                    resultReceiver.send(GetModifyFilesReceiver.SEND_FILE,bundle);
                }else
                {
                    //no more files to process , end
                    jsonObject = new JSONObject();
                    jsonObject.put("statusCode",GetModifyFilesReceiver.SUCCESS_SENDING);
                    Bundle bundle = new Bundle(1);
                    bundle.putString("info", jsonObject.toString());

                    resultReceiver.send(GetModifyFilesReceiver.SUCCESS_SENDING,bundle);
                }

            }
            else
            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode",GetModifyFilesReceiver.NO_FILES);
                Bundle bundle = new Bundle(1);
                bundle.putString("info", jsonObject.toString());

                resultReceiver.send(GetModifyFilesReceiver.NO_FILES,bundle);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            Bundle extras = new Bundle(1);
            extras.putString("errorMsg","Error retrieving files "+e.getMessage());

            resultReceiver.send(GetModifyFilesReceiver.ERROR_IN_PROCESSING,extras);
        }

    }

    //get media files to modify
    private Cursor getMediaFilesToModify(int offset,int CHUNK_LIMIT)
    {
        return CampaignsDBModel.getCampaignsFromOffset(context,offset,CHUNK_LIMIT);
    }

    //get file info object
    private JSONObject getFileInfoObject(long campaignId)
    {
        try {
            //if file is text type then you can set info else you dont have the properties to set skip
            Cursor  campaignInfo = CampaignsDBModel.getCampaign(context,campaignId);

            if (campaignInfo != null&&campaignInfo.moveToFirst())
            {

               JSONObject newJsonObject = new JSONObject();


                newJsonObject.put(getString(R.string.multi_region_media_name_json_key),campaignInfo.getString(campaignInfo.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME)));
                newJsonObject.put(getString(R.string.media_path_json_key),campaignId);
                newJsonObject.put(getString(R.string.media_info_json_key),campaignInfo.getString(campaignInfo.getColumnIndex(CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO)));


                boolean isSkip=false;
                if(Constants.convertToInt(campaignInfo.getString(campaignInfo.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP)))==1)
                {isSkip=true;

                }
                newJsonObject.put(getString(R.string.is_skip_json_key),isSkip);

               return newJsonObject;

            }else
            {
                return null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
