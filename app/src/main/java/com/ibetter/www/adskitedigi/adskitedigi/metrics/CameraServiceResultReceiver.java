package com.ibetter.www.adskitedigi.adskitedigi.metrics;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.ibetter.www.adskitedigi.adskitedigi.metrics.internal.MetricsService;

import java.lang.ref.WeakReference;

public class CameraServiceResultReceiver extends ResultReceiver
{
    private CallBack callBack;
    private WeakReference serviceContext;

    public static final int STOP_SERVICE=1;
    public static final int UPLOAD_METRICS_FILE_SERVICE=2;

    public CameraServiceResultReceiver(Handler handler, MetricsService context, CallBack callBacks)
    {
        super(handler);
        serviceContext = new WeakReference<>(context);
        this.callBack = callBacks;
    }

    public interface CallBack {

        void stopService(Bundle values);
        void uploadMetricsFileServiceResponse(Bundle values);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle values) {

        switch (resultCode) {

            case STOP_SERVICE:
                callBack.stopService(values);
                break;
            case UPLOAD_METRICS_FILE_SERVICE:
                callBack.uploadMetricsFileServiceResponse(values);
                break;
        }


    }
}
