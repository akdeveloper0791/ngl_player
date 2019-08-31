package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.register.CheckLicenceService;

import static com.ibetter.www.adskitedigi.adskitedigi.fcm.MyFirebaseMessagingService.checkAndUploadFCM;

public class RestartEnterpriseSettingsMode extends JobService {

    private Context context;
    private String TAG="info RestartEnterpriseSettingsMode";
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        context = RestartEnterpriseSettingsMode.this;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG,"INSIDE START JOB");
        //Thread.sleep(2000);

        EnterPriseSettingsModel.startEnterPriseModel(context);

        checkAndUploadFCM(context);


        //check for licence
        ContextCompat.startForegroundService(context,new Intent(context, CheckLicenceService.class));

        jobFinished (jobParameters,false);
        onStopJob(jobParameters);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        Log.i(TAG,"INSIDE STOP JOB");
        return false;
    }
}
