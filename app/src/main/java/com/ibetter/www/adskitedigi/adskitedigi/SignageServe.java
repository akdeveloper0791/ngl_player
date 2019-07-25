package com.ibetter.www.adskitedigi.adskitedigi;

import android.app.Service;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.v4.util.SimpleArrayMap;

import com.google.android.gms.nearby.connection.Payload;

import java.util.HashSet;

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

    public static long eventTime=0;


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


}
