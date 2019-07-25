package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.receiver;

import android.app.Activity;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Process;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.receiver.ConnectionDetector;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsService;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.RestartEnterpriseSettingsMode;

import java.lang.ref.WeakReference;


/**
 * Created by vineeth_ibetter on 5/25/15.
 */
public class MonitorConnectivityChangesChanges extends BroadcastReceiver {

    private Context context;
    private String ip;
    private boolean isFirsttime = true;
    private WeakReference<EnterPriseSettingsService> weakReference;

    private boolean is_init_internet_action=false;
    private boolean is_init_wifi_action=false;

    public MonitorConnectivityChangesChanges(WeakReference weakReference,String ip) {
        this.weakReference = weakReference;
        this.ip=ip;
    }


    public void onReceive(Context context, Intent intent) {

            this.context = context;
            //intent.g

            Log.i("Info", "internet changes in entrprise -"+intent.getAction());

            String intentAction=intent.getAction();

        EnterPriseSettingsService service = weakReference.get();

            if(intentAction.equalsIgnoreCase(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
            {
                if(is_init_internet_action){
                    //restart
                    checkAndRestartSmEnterPriceMode(service);
                }else
                {
                    is_init_internet_action=true;

                }

             }else if(intentAction.equalsIgnoreCase("android.net.wifi.WIFI_STATE_CHANGED"))
            {
                if(is_init_wifi_action){
                    //restart
                    checkAndRestartSmEnterPriceMode(service);
                }else
                {
                    is_init_wifi_action=true;

                }

            }

            //Toast.makeText(context,"Inside internet changes receiver",Toast.LENGTH_SHORT).show();
            Log.i("internet changes", "Inside internet changes receiver");


           /* if(!isFirsttime)
            {
                Log.i("info"," before stoping ");
               // EnterPriseSettingsModel.stopEnterPriseService();
                EnterPriseSettingsModel.switchOffEnterPriseSettings(context);

                Log.i("info"," after stoping ");

            }
            else
            {
                Log.i("info"," else isFirsttime");

                isFirsttime=false;
            }*/
           // service.connectivityChanged();

        //and then restart
        //EnterPriseSettingsModel.startEnterPriseModel(context);


      //  EnterPriseSettingsModel.switchOffEnterPriseSettings(context);

    }

    private  void killProcesses()
    {
       Process.killProcess(Process.myPid());
        System.exit(1);
    }

    private void checkAndRestartSmEnterPriceMode(EnterPriseSettingsService service)
    {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String currentIP = new DeviceModel().getIpAddress(context);

         if (SignageMgrAccessModel.isSignageMgrAccessOn(context, context.getString(R.string.sm_access_enterprise_mode)))
         {

            Log.i("info internet", "previp" + service.ipAddr);
            Log.i("info internet", "currentIP" + currentIP);

             if ((wifi.isWifiEnabled() || isInternet(context)))
             {
                if ((!currentIP.equalsIgnoreCase("0.0.0.0")) && (!service.ipAddr.equalsIgnoreCase(currentIP))) {

                    //context.startActivity(new Intent(context,RestartEnterPriseModeServiceActivity.class));
                    Log.i("info internet", "ip changed need to restart service" + currentIP);

                    restartService();

                    Log.i("info","before killing services");

                    killProcesses();

                    Log.i("info","after killing services");
              }

            }else
             {
                 restartService();

                 Log.i("info","before killing services");

                 killProcesses();
             }
        }
    }

    private void restartService()
    {

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo myJob = new JobInfo.Builder(1, new ComponentName(context, RestartEnterpriseSettingsMode.class))
                .setRequiresCharging(true)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setRequiresCharging(false)
                .setRequiresBatteryNotLow(false)
                .build();

        jobScheduler.schedule(myJob);


    }

    private boolean isInternet(Context context)
    {
        ConnectionDetector cd = new ConnectionDetector(context);

        return cd.isConnectingToInternet();

    }


}