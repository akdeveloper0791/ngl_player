package com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers;

import android.widget.Toast;

import com.google.android.gms.nearby.connection.Payload;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.ConnectingNearBySMService;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class HandleGetModifyFilesServiceReceiver implements GetModifyFilesReceiver.GetModifyFilesReceiverCallBacks
{

    private WeakReference<ConnectingNearBySMService> activityRef;


    public HandleGetModifyFilesServiceReceiver(ConnectingNearBySMService activity){
        activityRef = new WeakReference<ConnectingNearBySMService>(activity);
    }

    public void onReceive(String response)
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            ConnectingNearBySMService activity = activityRef.get();
            try {

                activity.send(Payload.fromBytes((activityRef.get().getString(R.string.modify_response) + response.toString()).getBytes("UTF-8")));
            }catch (Exception e)
            {
             Toast.makeText(activity,"Unable to send modify response "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onError(String errorMsg)
    {
        if(activityRef!=null && activityRef.get()!=null)
        {
            ConnectingNearBySMService activity = activityRef.get();
            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("statusCode", GetModifyFilesReceiver.ERROR_IN_PROCESSING);
                jsonObject.put("errorMsg", errorMsg);

                activity.send(Payload.fromBytes((activityRef.get().getString(R.string.modify_response) + jsonObject.toString()).getBytes("UTF-8")));

            }catch (Exception e)
            {
                Toast.makeText(activity,"Unable to send modify response "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }

        }
    }
}