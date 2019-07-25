package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ibetter.www.adskitedigi.adskitedigi.BuildConfig;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.RestartApp;
import com.ibetter.www.adskitedigi.adskitedigi.StopApp;
import com.ibetter.www.adskitedigi.adskitedigi.accessibility.HandleKeyCommands;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class DeviceModel {

    public String getDefaultMobileNumber(Context context)
    {
        try
        {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tMgr.getLine1Number();

        }catch(SecurityException se)
        {
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public String getMacAddress(Context context)
    {
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface nif : all)
            {

                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {

                   /* String stringValue=Integer.toHexString(b & 0xFF);
                    if(stringValue.length()<=1)
                    {
                        stringValue="0"+stringValue;
                    }*/
                    String stringValue= Integer.toHexString(b & 0xFF).length()==1 ?"0"+Integer.toHexString(b & 0xFF) :Integer.toHexString(b & 0xFF);
                    res1.append(stringValue + ":");

                }

                if (res1.length() > 0) {

                    res1.deleteCharAt(res1.length() - 1);
                }

                return res1.toString();
            }
        } catch (Exception ex)
        {
        }

        return null;
    }
    /* generate harware key */
    public  String generateHardwareKey(Context context) {


        String mac = getMacAddress();

            if (mac != null) {
                mac = mac.replaceAll(":", XmlPullParser.NO_NAMESPACE);
            }
           return context.getString(R.string.xibo_device_hard_ware_key_prefix) + mac ;

    }

    /* Returns the consumer friendly device name */
    public  String generateDeviceName(Context context) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return  context.getString(R.string.app_default_android_text)+"-"+Constants.capitalize(model)+"-"+new User().getUserMobileNumber(context);
        }
        return context.getString(R.string.app_default_android_text)+"-"+Constants.capitalize(manufacturer) + " " + model+"-"+new User().getUserMobileNumber(context);
    }


    /* Returns the consumer friendly device name */
    public  String getDeviceModelName(Context context)
    {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manufacturer)) {
            return Constants.capitalize(model);

        }
        return Constants.capitalize(manufacturer) + " " + model;
    }

    // get downloadable chunk size
    public long getDownloadableChunkSize(Context context)
    {
        SharedPreferences deviceSP = new SharedPreferenceModel().getDeviceSharedPreference(context);
        return deviceSP.getLong(context.getString(R.string.device_downloadable_sp),Constants.DEFAULT_DOWNLOADABLE_CHUNK_SIZE);
    }

    // update default downloadable chunk size
    public boolean updateDownloadableChunkSize(Context context,long chunkSize)
    {
        SharedPreferences deviceSP = new SharedPreferenceModel().getDeviceSharedPreference(context);
        SharedPreferences.Editor deviceSPEditor = deviceSP.edit();

        deviceSPEditor.putLong(context.getString(R.string.device_downloadable_sp),chunkSize);
        return deviceSPEditor.commit();
    }

    public boolean isBelowIceCreamSandWichVersion()
    {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            return  true;
        }else
        {
            return false;
        }
    }

    public boolean isMemoryLow(Context context) {

        // Before doing something that requires a lot of memory,
        // check to see whether the device is in a low memory state.

        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        Log.i("lowMemory",""+memoryInfo.lowMemory);
        Log.i("availMem",""+memoryInfo.availMem);
        Log.i("totalMem",""+memoryInfo.totalMem);
        Log.i("threshold memory",""+memoryInfo.threshold);

       return memoryInfo.lowMemory;


    }

    public long getAvlMemory(Context context)
    {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        return memoryInfo.availMem;

    }

    //get perviously saved app version code
    public int getSavedAppVersionCode(Context context)
    {
        SharedPreferences loginSP=context.getSharedPreferences(context.getString(R.string.user_details_sp), Context.MODE_PRIVATE);
        return loginSP.getInt(context.getString(R.string.app_version_code),0);

    }

    //save the current app version code
    public boolean savedAppVersionCode(Context context)
    {
        SharedPreferences loginSP=context.getSharedPreferences(context.getString(R.string.user_details_sp), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = loginSP.edit();
        int versionCode=getAppVersionCode();
        if(versionCode>0)
        {
            editor.putInt(context.getString(R.string.app_version_code),versionCode);
            editor.commit();
            return true;

        }else
        {
            return false;
        }

    }


    //get app version code
    public int getAppVersionCode()
    {
        int verCode = BuildConfig.VERSION_CODE;
        if(verCode>1)
        {
            return verCode;
        }else {
            return 0;
        }


    }

    //calculate device properties
    public HashMap<String,Integer> getDeviceProperties(Activity context)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        HashMap<String,Integer> deviceInfo = new HashMap<>(2);
        int softBtnBarHeight=getSoftButtonsBarHeight(context);

        deviceInfo.put("width",displayMetrics.widthPixels);
        deviceInfo.put("height",(displayMetrics.heightPixels+softBtnBarHeight));
        return deviceInfo;

    }

    public static String getIpAddress(Context context)
    {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo connectionInfo = wm.getConnectionInfo();
        String ip = Formatter.formatIpAddress(connectionInfo.getIpAddress());

        return ip;
    }

    @SuppressLint("NewApi")
    public int getSoftButtonsBarHeight(Activity activity)
    {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            //  Toast.makeText(activity, "usableHeight :"+usableHeight, Toast.LENGTH_SHORT).show();
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            //  Toast.makeText(activity, "realHeight :"+realHeight, Toast.LENGTH_SHORT).show();
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    //generate QR
    public Bitmap generateMACQR(String macAddress,Context context)
    {

        try {

            //generate json object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(context.getString(R.string.json_key_mac), macAddress);
            jsonObject.put(context.getString(R.string.json_key_is_encoded), true);

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

            BitMatrix bitMatrix = multiFormatWriter.encode(jsonObject.toString(), BarcodeFormat.QR_CODE,400,400);

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            return bitmap;

        }
        catch (WriterException e)
        {
            e.printStackTrace();
            return null;

        }catch(JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String encodeMacAddress(String mac)
    {
        try
        {
            byte[] data = mac.getBytes("UTF-8");
            return Base64.encodeToString(data, Base64.DEFAULT);

        }catch (Exception e)
        {

            e.printStackTrace();
            return null;
        }
    }

    public static void restartApp(Context context)
    {
        //restart app
        Intent i = new Intent(context,RestartApp.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }

    //stop app
    public static void stopApp(Context context)
    {
        Intent i = new Intent(context, StopApp.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    //determine is the device zidoo or not
    public static boolean isZidooDevice(Context context)
    {
        //redirect to zidoo if there
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getString(R.string.def_hdmi_in_package));
        if (launchIntent != null) {
            return true;
        }else
        {
            return false;
        }
    }


    //determine is the device envy or not
    public static boolean isEnvyDevice(Context context)
    {
        //redirect to zidoo if there
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getString(R.string.def_envy_hdmi_in_package));

        if (launchIntent != null)
        {
            //context.startActivity(launchIntent);
            return true;
        }
        else
        {
            return false;
        }
    }

    public static void launchEnvyHdmiIn(Context context)
    {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getString(R.string.def_envy_hdmi_in_package));
        if (launchIntent != null) {
            context.startActivity(launchIntent);

        }
    }
    public static void launchZidooHdmiIn(Context context)
    {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getString(R.string.def_hdmi_in_package));
        if (launchIntent != null) {
            context.startActivity(launchIntent);

        }
    }



    public static void processHomeCommand(Context context)
    {
        boolean flag=new User().isAppLauncherSettingOn(context);
        if(flag)
        {
            String packageName=new User().getAppLauncherPackage(context);
            // Toast.makeText(context, "packageName:"+packageName, Toast.LENGTH_SHORT).show();
            if(packageName!=null)
            {
                launchOtherApp(context,packageName);
            }else
            {
                stopApp(context);
            }
        }else
        {
            stopApp(context);
        }

    }

    private static void launchOtherApp(Context context,String packageName)
    {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null)
        {
            try {
               context.startActivity(mIntent);

            } catch (ActivityNotFoundException err)
            {

                Toast.makeText(context, err.toString(), Toast.LENGTH_SHORT).show();
                stopApp(context);
            }

        }
    }



    public static boolean isAccessibilityServiceRunning(Context context)
    {

        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + HandleKeyCommands.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v("DeviceModel", "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("DeviceModel", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v("DeviceModel", "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v("DeviceModel", "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v("DeviceModel", "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v("DeviceModel", "***ACCESSIBILITY IS DISABLED***");
        }

        return false;

    }

}
