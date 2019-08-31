package com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model;

import android.content.Context;
import android.net.Uri;

import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class GCUtils
{
     public static final String GC_LOGIN_API_URI_STRING="https://www.signageserv.ai/gc_login_api/";
     //public static final String GC_LOGIN_API_URI_STRING="http://vineeth0791.pythonanywhere.com/api/first/";

    public static final String GC_DEFAULT_CAMPAIGN_PATH="https://www.signageserv.ai/media/campaigns/";

    public static final String GET_GC_ALL_CAMPAIGNS_URL="https://www.signageserv.ai/campaign_downloads_api/";
    public static final String GET_GC_ALL_DROP_BOX_CAMPAIGNS_URL="https://www.signageserv.ai/player/getPlayerCampaigns/";
    public static final String GET_SCHEDULE_CAMPAIGNS_URL="https://www.signageserv.ai/player/getDSPCampaigns/";
    public static final String GET_PLAYER_CA_RULE_URL="https://www.signageserv.ai/player/getCARules/";

    public static final String UPLOAD_CAMPAIGN_SUPPORT_FILE_URL= "https://www.signageserv.ai/campaign_upload_files_api/";

    public static final String GC_LOGIN_FORGET_PASSWORD_URL="https://www.signageserv.ai/forget_password/";
    public static final String GC_USER_SIGNUP_URL="https://www.signageserv.ai/accounts/signup/";
    public static final String GC_PLAYER_REGISTER_URL="https://www.signageserv.ai/player/register";
    public static final String GC_PLAYER_REFRESH_FCM_URL="https://www.signageserv.ai/player/refresh_fcm_api";
    public static final String GC_IOT_DEVICE_REGISTER_URL="https://www.signageserv.ai/iot_device/register";

    public static final String LICENCE_REGISTER="https://www.signageserv.ai/license/register";
    public static final String SEND_OTP_REGISTER="https://www.signageserv.ai/license/sendOTP";

    public static final String UPLOAD_METRICS_FILE_URL= "https://www.signageserv.ai/iot_device/metrics";


    public static final String UPLOAD_PLAYER_STATISTICS_COLLECTION_URL= "https://www.signageserv.ai/player/saveCampaignReports/";

    public static final String GET_ALL_DROP_BOX_CAMPAIGNS_URL_END_POINT="player/getPlayerCampaigns/";
    public static final String ENTERPRISE_SCHEDULE_CAMPAIGNS_URL="player/getDSPCampaigns/";
    public static final String UPLOAD_METRICS_FILE_URL_END_POINT= "iot_device/metrics";
    public static final String UPLOAD_PLAYER_STATISTICS_COLLECTION_URL_END_POINT= "player/saveCampaignReports/";
    public static final String GET_PLAYER_CA_RULE_URL_ENTERPRISE="player/getCARules/";
    public static final String ENTERPRISE_PLAYER_REFRESH_FCM_URL="player/refresh_fcm_api";


    public static URLConnection makePostRequest(String method, String apiAddress,String email,String password,String playerId) throws IOException {

        URL url = new URL(apiAddress);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestMethod(method);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        Uri.Builder builder = new Uri.Builder().appendQueryParameter("email", email)
                .appendQueryParameter("player",playerId)
                .appendQueryParameter("password", password);

        String query = builder.build().getEncodedQuery();
        OutputStream os = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(query);
        writer.flush();
        writer.close();
        os.close();

        urlConnection.connect();
        return urlConnection;
    }

    public static String prepareCampaignFileURL(Context context,String CampaignName,String fileName)
    {
        String uniqueKey=new User().getGCUserUniqueKey(context);
        if(uniqueKey!=null)
        {
            return (GCUtils.GC_DEFAULT_CAMPAIGN_PATH + uniqueKey + File.separator+CampaignName+ File.separator+fileName);

        }else
        {
            return null;
        }
    }
}
