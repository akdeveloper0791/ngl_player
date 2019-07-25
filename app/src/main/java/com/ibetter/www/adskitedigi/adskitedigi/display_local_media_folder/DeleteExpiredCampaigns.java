package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DeleteUnknownCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DeleteExpiredCampaigns extends IntentService {

    private Context context;

    public DeleteExpiredCampaigns()
    {
        super("DeleteExpiredCampaigns");
        context = DeleteExpiredCampaigns.this;
    }

    public void onHandleIntent(Intent intent)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(ScheduleCampaignModel.LOCAL_SCHEDULE_DATE_FORMAT);
        String currentDateTime = sdf.format(Calendar.getInstance().getTime());
        Cursor expiredSchedules = CampaignsDBModel.getExpiredCampaigns(context,currentDateTime);
        ArrayList<GCModel> expiredCampaignList = new ArrayList<>(expiredSchedules.getCount());
        ArrayList<String> expiredCampaignId = new ArrayList<>(expiredSchedules.getCount());
        Log.d("PlayAds","Inside PlayAds DeleteExpiredCampaigns found - "+expiredSchedules.getCount());
        if(expiredSchedules!=null && expiredSchedules.moveToFirst())
        {
            do {
                GCModel gcModel = new GCModel();
                gcModel.setCampaignName(expiredSchedules.getString(expiredSchedules.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME)));
                gcModel.setInfo(expiredSchedules.getString(expiredSchedules.getColumnIndex(CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO)));
                long localId = expiredSchedules.getLong(expiredSchedules.getColumnIndex(CampaignsDBModel.LOCAL_ID));
                gcModel.setCampaignLocalId(localId);
                expiredCampaignList.add(gcModel);
                expiredCampaignId.add(String.valueOf(localId));
            }while(expiredSchedules.moveToNext());
        }

        //update campaigns download status
        CampaignsDBModel.updateExpiredCampaignsStatus(TextUtils.join(", ", expiredCampaignId),context);

        //start service to delete unknown campaigns
        Intent deletedCampaignsIntent = new Intent(context, DeleteUnknownCampaigns.class);
        deletedCampaignsIntent.putExtra("unknown_campaigns", expiredCampaignList);
        startService(deletedCampaignsIntent);

    }
}
