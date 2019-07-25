package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DeleteUnknownCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class DeleteCampaigns extends IntentService{

    private String campaignFolder;
    private Context context;


    public DeleteCampaigns()
    {
        super(DeleteCampaigns.class.getName());
    }

    protected void onHandleIntent(Intent intent)
    {
        context=DeleteCampaigns.this;

        try {
            campaignFolder = new User().getUserPlayingFolderModePath(this);

            String jsonArrayFilesString = intent.getStringExtra("json_array_files_string");

            JSONArray jsonArray=new JSONArray(jsonArrayFilesString);

            List<String> list = new ArrayList<String>();
            for(int i = 0; i < jsonArray.length(); i++){
                list.add(jsonArray.getString(i));
            }

            Log.i("deleting files list",""+list);

            Cursor garbageCampaigns = CampaignsDBModel.getCampaignsByLocalIds(TextUtils.join(", ", list), context);

            ArrayList<GCModel> deletedCampaigns = new ArrayList<>();

            if (garbageCampaigns != null && garbageCampaigns.moveToFirst()) {
                do {
                    GCModel gcModel = new GCModel();
                    gcModel.setCampaignName(garbageCampaigns.getString(garbageCampaigns.getColumnIndex(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME)));
                    gcModel.setInfo(garbageCampaigns.getString(garbageCampaigns.getColumnIndex(CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO)));
                    gcModel.setCampaignLocalId(garbageCampaigns.getLong(garbageCampaigns.getColumnIndex(CampaignsDBModel.LOCAL_ID)));
                    deletedCampaigns.add(gcModel);
                } while (garbageCampaigns.moveToNext());

            }

            Intent newIntent = new Intent(context, DeleteUnknownCampaigns.class);
            newIntent.putExtra("unknown_campaigns", deletedCampaigns);
            startService(newIntent);

            //delete in data base

            CampaignsDBModel.deleteCampaignsByLocalId(TextUtils.join(", ", list), context);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }




}
