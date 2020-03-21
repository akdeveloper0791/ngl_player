package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;

import java.util.ArrayList;

public class SkipAllCampaignsService extends IntentService
{
    private Context context;
    private ResultReceiver receiver;
    private boolean skipFlag;

    private ArrayList<String> skipCampsList=new ArrayList<>();
    private ArrayList<GCModel> campList=new ArrayList<>();

    public static final int SKIP_ALL_CAMPAIGNS_ACTION=2012;
    public SkipAllCampaignsService()
    {
        super(SkipAllCampaignsService.class.getName());
    }

    @Override
    public void onHandleIntent(@Nullable Intent intent)
    {
        context=SkipAllCampaignsService.this;
        receiver = intent.getParcelableExtra("receiver");
        skipFlag=intent.getBooleanExtra("skipFlag",false);
        if(skipFlag)
        {
            campList=(ArrayList<GCModel>)intent.getSerializableExtra("campaignList");
        }

        skipCampsList=(ArrayList<String>)intent.getSerializableExtra("skipCampsList");

        skipAllCampaignsAction();

    }

    private void skipAllCampaignsAction()
    {
        try
        {
            if(skipCampsList==null|| skipCampsList.size()<=0)
            {
                for(GCModel gcModel:campList)
                {
                    String campaignName=gcModel.getCampaignName();
                    if(CampaignsDBModel.isCampaignExist(context,campaignName))
                    {
                        ContentValues cv=new ContentValues();
                        cv.put(CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP,1);

                        CampaignsDBModel.updateCampaignByName(cv,context, campaignName);

                        skipCampsList.add(campaignName);
                    }
                    else
                    {
                        Log.i("SkipAllCampaignsTask","No media file found - "+campaignName);
                    }
                    sendResponse(true,"All campaigns Un skipped ");
                }

            }
            else if(skipCampsList!=null && skipCampsList.size()>0)
            {
                for(String campaignName:skipCampsList)
                {
                    if(CampaignsDBModel.isCampaignExist(context,campaignName))
                    {
                        ContentValues cv=new ContentValues();
                        cv.put(CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP,0);

                        CampaignsDBModel.updateCampaignByName(cv,context, campaignName);

                    }
                    else
                    {
                        Log.i("SkipAllCampaignsTask","No media file found - "+campaignName);
                    }
                }

                sendResponse(true,"All campaigns  skipped ");

            }else
            {
                sendResponse(false,"No Campaigns downloaded to do skip operation");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            Log.i("SkipAllCampaignsTask","Exception:"+e.toString());
            sendResponse(false,e.toString());
        }

    }


    private void sendResponse(boolean flag,String status)
    {
        Bundle bundle=new Bundle();
        bundle.putBoolean("flag",flag);
        bundle.putBoolean("skipFlag",skipFlag);
        if(skipFlag)
        {
            bundle.putSerializable("skipCampsList",skipCampsList);
        }
        bundle.putString("status",status);
        receiver.send(SKIP_ALL_CAMPAIGNS_ACTION,bundle);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}
