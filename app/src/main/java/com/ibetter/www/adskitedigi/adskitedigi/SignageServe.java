package com.ibetter.www.adskitedigi.adskitedigi;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.support.v4.util.SimpleArrayMap;

import com.google.android.gms.nearby.connection.Payload;
import com.google.gson.Gson;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.MediaInfo;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;

/**
 * Created by vineethkumar0791 on 28/03/18.
 */

public class SignageServe extends MultiDexApplication
{

    public static SignageServe signageServeObject ;

    private static Service smService ;
    public static Context context;


    private SimpleArrayMap<String,Service> runningServices = new SimpleArrayMap<>();

    public static Service getSmService()
    {
        return smService;
    }

    public static void setSmService(Service smService)
    {
        SignageServe.smService = smService;
    }

    public static  Payload streamingPayload;

    public static int lasMediaPlayedPosition = 0;
    public static MediaInfo lastMediaPlayed = null;

    @Override public void onCreate() {
        super.onCreate();
        signageServeObject = new SignageServe();
        context=getApplicationContext();
    }


    public Payload getStreamingPayload() {
        return streamingPayload;
    }

    public static void  setStreamingPayload(Payload streamingPayload) {
        SignageServe.streamingPayload = streamingPayload;
    }

    public synchronized void saveRunningServicesInfo(String serviceName,Service service)
    {
        runningServices.put(serviceName,service);
    }

    public synchronized void removeBackGroundService(String serviceName)
    {
        if(runningServices!=null && runningServices.containsKey(serviceName))
        {
            runningServices.remove(serviceName);
        }

    }
    //stop service
    public synchronized void stopRunningService(String serviceName)
    {
        if(runningServices!=null && runningServices.containsKey(serviceName))
        {
            runningServices.remove(serviceName).stopSelf();
        }
    }

    public static void initLastMediaPlayed() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.settings_sp),Context.MODE_PRIVATE);
        String mediaInfoJson = sharedPreferences.getString(Constants.LAST_MEDIA_PLAYED_SP_KEY,null);
        if(mediaInfoJson != null){
            try{
                lastMediaPlayed = new Gson().fromJson(mediaInfoJson,MediaInfo.class);
                lasMediaPlayedPosition = sharedPreferences.getInt(Constants.LAST_MEDIA_PLAYED_POSITION_SP_KEY,0);
            }catch (Exception e) {

            }
        }
    }

    public static void saveLastMediaPlayedToSP(MediaInfo mediaInfo, int lasMediaPlayedPosition) {
        lastMediaPlayed = mediaInfo;
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.settings_sp),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String mediaJson = new Gson().toJson(mediaInfo);
        if(mediaInfo != null){
            editor.putString(Constants.LAST_MEDIA_PLAYED_SP_KEY, mediaJson);
            editor.putInt(Constants.LAST_MEDIA_PLAYED_POSITION_SP_KEY,lasMediaPlayedPosition);
            editor.apply();
        }
    }


}
