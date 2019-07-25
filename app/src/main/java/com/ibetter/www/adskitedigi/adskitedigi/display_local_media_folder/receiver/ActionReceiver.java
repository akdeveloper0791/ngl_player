package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.ibetter.www.adskitedigi.adskitedigi.metrics.UploadMetricsFileService;

import java.io.File;
import java.util.ArrayList;

public class ActionReceiver extends ResultReceiver {

    public static final int SKIP_ACTION_CODE = 1;
    public static final int PAUSE_MEDIA_ACTION_CODE = 2;
    public static final int RESUME_MEDIA_ACTION_CODE = 3;
    public static final int HANDLE_CAMPAIGN_RULE_ACTION_CODE = 4;
    public static final int HANDLE_DELETE_CAMPAIGNS = 5;
    public static final int HANDLE_CAMPAIGN_RULE_NEW_ACTION_CODE = 6;

    ActionReceiverCallBacks handler;

    public ActionReceiver(Handler handler)
    {
        super(handler);
    }

    public void setHandelActionReceiverHandler(ActionReceiverCallBacks handler)
    {
        this.handler = handler;
    }

    @Override
    public void onReceiveResult(int actionCode,Bundle extras)
    {

        if(handler!=null) {

            switch (actionCode) {
                case SKIP_ACTION_CODE:
                    if(extras!=null && extras.containsKey("isSkip") && extras.getBoolean("isSkip") && extras.containsKey("campaignName"))
                    handler.onSkipCampaign(extras.getString("campaignName"));
                    break;

                case PAUSE_MEDIA_ACTION_CODE:

                    handler.onPauseCampaign();
                    break;

                case RESUME_MEDIA_ACTION_CODE:
                    handler.onResumeCampaign();
                    break;

                case HANDLE_CAMPAIGN_RULE_ACTION_CODE:
                    ArrayList<File> campaignFilesArray = (ArrayList<File>)extras.getSerializable("campaignFiles");
                    handler.handleCampaignRule(campaignFilesArray);
                    break;

                case HANDLE_CAMPAIGN_RULE_NEW_ACTION_CODE:
                    ArrayList<String> ruleCampaigns = (ArrayList<String>)extras.getSerializable("campaignFiles");

                    handler.handleCampaignRuleNew(ruleCampaigns,extras.getString("rule"));
                    break;

                case HANDLE_DELETE_CAMPAIGNS:
                    ArrayList<Long> deletedCampaigns = (ArrayList<Long>)extras.getSerializable("deleted_campaigns");
                    handler.handleDeletedCampaigns(deletedCampaigns);
                    break;

                case UploadMetricsFileService.UPLOAD_FILE_ACTION:

                    if (extras != null)
                    {
                        boolean flag = extras.getBoolean("flag", false);
                        if (flag) {
                            handler.successResponse("User metrics captured");
                            //need to delete file
                        } else {
                            handler.failureResponse("No metrics detected ");
                        }

                    } else
                    {
                        handler.failureResponse("Unable to capture user metrics");
                    }
                    break;
            }
        }

    }

    public interface ActionReceiverCallBacks
    {
         void onSkipCampaign(String campaignName);
         void onPauseCampaign();
        void onResumeCampaign();
        void handleCampaignRule(ArrayList<File> campaignFilesArray);
        void handleDeletedCampaigns(ArrayList<Long> deletedCampaigns);
        void successResponse(String msg);
        void failureResponse(String msg);
        void handleCampaignRuleNew(ArrayList<String> campaignFilesArray,String rule);

    }
}
