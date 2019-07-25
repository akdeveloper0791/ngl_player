package com.ibetter.www.adskitedigi.adskitedigi.metrics;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.iot_devices.IOTDevice;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.internal.MetricsService;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
public class MetricsModel {

//start enterprise service
 public static void startMetricsService(Context context)
    {
        try {
            if(!MetricsService.isServiceOn)
            {
                if(new User().isMetricsOn(context)&&new User().isInternalCamType(context))
                {
                   if(User.isPlayerRegistered(context)&& IOTDevice.isIOTDeviceRegistered(context))
                   {
                       if(new MetricsModel().getNumberOfCameras(context)>0)
                       {

                           Log.i("MetricsService","startMetricsService:started");
                           ContextCompat.startForegroundService(context, new Intent(context, MetricsService.class));
                       }
                   }else
                   {
                       Log.i("MetricsService","startMetricsService:Player is not registered");
                       Toast.makeText(context, "Player is not registered...", Toast.LENGTH_SHORT).show();
                   }


                }
            }else
            {
                Log.i("MetricsService","MetricsService is already running...");
            }

        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            //new DisplayDebugLogs(context).execute("info"+e.getMessage());
        }
    }

    public static void stopMetricsService()
    {
        if(MetricsService.isServiceOn)
        {
            try
            {
                Log.i("MetricsService","stopMetricsService");
                //SignageServe.signageServeObject.stopRunningService(MetricsService.class.getName());
                Bundle bundle=new Bundle();
                MetricsService.cameraServiceResultReceiver.send(CameraServiceResultReceiver.STOP_SERVICE,bundle);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public int getNumberOfCameras(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                return ((CameraManager) context.getSystemService(Context.CAMERA_SERVICE)).getCameraIdList().length;
            } catch (CameraAccessException e) {
                Log.e("", "", e);
            }
        }
        return Camera.getNumberOfCameras();
    }
}
