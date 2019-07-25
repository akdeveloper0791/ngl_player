package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.CampaignModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService.DOWNLOAD_CAMPAIGNS_PATH;

public class DeleteUnknownCampaigns extends IntentService {
   private Context context;

    public DeleteUnknownCampaigns()
    {
        super("DeleteUnknownCampaigns");
        context = DeleteUnknownCampaigns.this;
    }

    protected void onHandleIntent(Intent intent)
    {

        ArrayList<GCModel> unknownCampaigns = (ArrayList)intent.getStringArrayListExtra("unknown_campaigns");
        ArrayList<Long> campaignLocalId = new ArrayList<>();
        for(GCModel campaign:unknownCampaigns)
        {
            removeCampaignResources(campaign.getCampaignName(),campaign.getInfo());
            campaignLocalId.add(campaign.getCampaignLocalId());
        }

        broadCastDeletedCampaigns(campaignLocalId);
    }


    private void removeCampaignResources(String campaignName,String info)
    {
        ArrayList<String> dataList=new ArrayList<>();


        {
            File thumbFile=new File(DOWNLOAD_CAMPAIGNS_PATH+File.separator+context.getString(R.string.do_not_display_media)+"-"+context.getString(R.string.media_thumbnail)+"-"+campaignName+".jpg");
            if(thumbFile.exists())
            {
                //  Log.d("PreviewCampaign","deleted thumbFile is :"+thumbFile.getName());
                thumbFile.delete();
            }


            // Log.d("PreviewCampaign","deleteCampaignResources:"+fileJson);
            try
            {
                JSONObject jsonObject = new JSONObject(info);
                String type = jsonObject.getString("type");

                if(type.equalsIgnoreCase(context.getString(R.string.app_default_image_name)))
                {
                    dataList.add(campaignName);
                    dataList.add(jsonObject.getString("resource"));

                }else if(type.equalsIgnoreCase(context.getString(R.string.app_default_video_name)))
                {
                    dataList.add(campaignName);
                    dataList.add(jsonObject.getString("resource"));

                }else if(type.equalsIgnoreCase(context.getString(R.string.app_default_multi_region)))
                {
                    dataList= processMultiRegionFile(jsonObject.getJSONArray("regions"));
                    dataList.add(campaignName);

                }else if(type.equalsIgnoreCase(context.getString(R.string.url_txt)))
                {
                    dataList.add(campaignName);
                }


                if(dataList!=null && dataList.size()>0)
                {
                    // Log.d("PreviewCampaign","deleted dataList:"+dataList.toString());

                    for(String fileString:dataList)
                    {
                        //  Log.d("PreviewCampaign","deleted campaign file:"+fileString);
                        File resourceFile=new File(CampaignModel.getAdsKiteNearByDirectory(context)+File.separator+fileString);

                        if(resourceFile.exists())
                        {

                            resourceFile.delete();
                        }
                    }
                }

                File infoFile=new File(DOWNLOAD_CAMPAIGNS_PATH,campaignName+".txt");
                if(infoFile.exists())
                {
                    infoFile.delete();
                }

            }catch(Exception e)
            {
                //error processing text, try play next media
                e.printStackTrace();
            }

        }


    }

    private ArrayList<String> processMultiRegionFile(JSONArray jsonArray)throws JSONException
    {
        ArrayList<String>fileList=new ArrayList<>();

        for(int i=0;i<jsonArray.length();i++)
        {

            JSONObject regionObject=jsonArray.getJSONObject(i);
            String type = regionObject.getString("type");

            if(type.equalsIgnoreCase(getString(R.string.media_image_type)) ||type.equalsIgnoreCase(getString(R.string.media_video_type)) ||type.equalsIgnoreCase(getString(R.string.app_default_file_name)))
            {
                fileList.add(regionObject.getString("media_name"));
            }

        }

        return fileList;
    }

    private void broadCastDeletedCampaigns(ArrayList<Long> campaignsFileArray)
    {
        if(DisplayLocalFolderAds.actionReceiver!=null)
        {
            Bundle extras = new Bundle(2);
            extras.putSerializable("deleted_campaigns", campaignsFileArray);
            DisplayLocalFolderAds.actionReceiver.send(ActionReceiver.HANDLE_DELETE_CAMPAIGNS,extras);
        }

    }
}
