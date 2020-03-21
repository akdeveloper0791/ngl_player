package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;
import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.HOT_SPOT_ENABLE_FAILURE_UN_RECOVERABLE;
import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.HOT_SPOT_ENABLE_SUCCESS_CODE;
import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.HOT_SPOT_ENABLE_SUCCESS_UN_AVAILABLE;

/**
 * Created by vineeth_ibetter on 16/11/15.
 */
public class NetworkModel {




    //check internet connection
    public static boolean isInternet(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void turnOnHotspotOreo(final Context context) {
        WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d(TAG, "Wifi Hotspot is on now");
               // mReservation = reservation;
                Toast.makeText(context,"Wifi hot spot is on now",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopped() {
                super.onStopped();

                Toast.makeText(context,"Wifi hot spot is turned off",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Toast.makeText(context,"Wifi hot spot is turned failed",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailed: ");
            }
        }, new Handler());
    }

    private static void turnOffHotspotOreo() {
       /*if (mReservation != null) {
            mReservation.close();
        }*/
    }


    public static int changeWifiHotspotState(Context context,boolean enable) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(enable){
               turnOnHotspotOreo(context);
                return HOT_SPOT_ENABLE_SUCCESS_UN_AVAILABLE;
            }else{
                turnOffHotspotOreo();
                return HOT_SPOT_ENABLE_SUCCESS_CODE;
            }
        }else{
            try {
               /* WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                manager.setWifiEnabled(false);
                Method method = manager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class,
                        Boolean.TYPE);
                method.setAccessible(true);
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                wifiConfiguration.SSID = "NGL_PLAYER";
                boolean isSuccess = (Boolean) method.invoke(manager,wifiConfiguration, enable);*/

                enableHotSpotInBelowOreo(context);

                Toast.makeText(context,"Successfully turned on WiFi Hotspot",Toast.LENGTH_SHORT).show();
                return HOT_SPOT_ENABLE_SUCCESS_CODE;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context,"Unable to turn on hot spot- "+e.getMessage(),Toast.LENGTH_SHORT).show();
                return HOT_SPOT_ENABLE_FAILURE_UN_RECOVERABLE;

            }
        }

    }

    private static void  enableHotSpotInBelowOreo(Context context){
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiApControl apControl = WifiApControl.getApControl(manager);
        apControl.setWifiApEnabled(apControl.getWifiApConfiguration(),
                true);
    }
}
