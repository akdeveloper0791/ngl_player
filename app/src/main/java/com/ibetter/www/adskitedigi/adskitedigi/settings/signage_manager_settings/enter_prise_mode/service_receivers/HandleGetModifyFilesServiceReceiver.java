package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.service_receivers;

import android.content.Context;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers.GetModifyFilesReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

import org.json.JSONObject;

public class HandleGetModifyFilesServiceReceiver implements GetModifyFilesReceiver.GetModifyFilesReceiverCallBacks
{

    private Context context;
    private String saveResponseTo;


    public HandleGetModifyFilesServiceReceiver(Context context,String saveResponseTo){
        this.context = context;
        this.saveResponseTo = saveResponseTo;
    }

    public void onReceive(String response)
    {

        // save response to specific file
        EnterPriseSettingsModel.saveSMFTPResponse(context,response,saveResponseTo);
    }

    public void onError(String errorMsg) {

        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("statusCode", GetModifyFilesReceiver.ERROR_IN_PROCESSING);
            jsonObject.put("errorMsg", errorMsg);

            EnterPriseSettingsModel.saveSMFTPResponse(context,jsonObject.toString(),saveResponseTo);

        } catch (Exception e) {
            if (context != null) {
                Toast.makeText(context, "Unable to send modify response " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
         }
    }





}