package com.ibetter.www.adskitedigi.adskitedigi.nearby.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;

public class HandleMediaSettingsService extends IntentService {

    public HandleMediaSettingsService()
    {
        super("HandleMediaSettingsService");
    }

    public static void startHandleMediaSettingsService(Context context, String payloadMsg)
    {
        Intent intent = new Intent(context,HandleMediaSettingsService.class);
        intent.putExtra("payloadMsg",payloadMsg);
        context.startService(intent);

    }

    public void onHandleIntent(Intent intent)
    {
         String payloadString = intent.getStringExtra("payloadMsg");
         try
        {
           String[] payloadBytes = payloadString.split(getString(R.string.payload_separator));
           if(payloadBytes.length>=2)
           {
               switch (payloadBytes[1])
               {
                   case "skip_setting":
                       handleSkipSetting(HandleMediaSettingsService.this,payloadBytes[2],Boolean.parseBoolean(payloadBytes[3]));
                       break;
                   case "pause_media_setting":
                       pauseCurrentMedia();
                       break;
                   case "resume_media_setting":
                       resumeCurrentMedia();
                       break;
               }
           }
        }catch (Exception e)
        {
            e.printStackTrace();
           // Toast.makeText(context,"Invalid request in handling media settings request "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    //handle skip settings
    private void handleSkipSetting(Context context,String mediaId,boolean isSkip) throws Exception
    {

        long campaignId=Constants.convertToLong(mediaId);

        if(campaignId>0)
        {
        ContentValues cv=new ContentValues();
        cv.put(CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP,(isSkip?1:0));

        CampaignsDBModel.updateCampaignById(cv,context, campaignId);



        if(isSkip && DisplayLocalFolderAds.actionReceiver!=null)
        {


                Cursor cursor=CampaignsDBModel.getCampaign(context,campaignId);

                if(cursor!=null&&cursor.moveToFirst()) {
                    Bundle extras = new Bundle(2);
                    extras.putBoolean("isSkip", isSkip);
                    extras.putString("campaignName",cursor.getString(cursor.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME)) );

                    DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.SKIP_ACTION_CODE, extras);

                }else
                {
                    Log.d("Handle media settings","No media file found - ");

                }
            }
        }

        else
        {
            Log.d("Handle media settings","No media file found - ");
        }
    }

    //pause current playing media
    private void pauseCurrentMedia()
    {
        Log.d("Pause","Inside pause current media Handle media settings service");
        if( DisplayLocalFolderAds.actionReceiver!=null)
        {
            Log.d("Pause","Inside pause current media Handle media settings service action receiver is not null");
            DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.PAUSE_MEDIA_ACTION_CODE,null);

        }
    }

    //pause current playing media
    private void resumeCurrentMedia()
    {
        Log.d("Resume","Inside resume current media Handle media settings service");
        if( DisplayLocalFolderAds.actionReceiver!=null)
        {
            Log.d("Pause","Inside resume current media Handle media settings service action receiver is not null");
            DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.RESUME_MEDIA_ACTION_CODE,null);

        }
    }
}
