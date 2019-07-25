package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by vineethkumar0791 on 28/03/18.
 */

public class Permissions
{
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {

            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
        }
        return true;
    }


    public static boolean checkMultiplePermission(Context context,String permission1,String permission2)
    {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(context, permission1);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(context, permission2);
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED && SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }


    public  static void requestMultiplePermission(Activity activity, String permission1, String permission2, final int RequestPermissionCode) {

        ActivityCompat.requestPermissions(activity, new String[]
                {
                        permission1,
                        permission2
                }, RequestPermissionCode);
    }

    public static boolean checkSinglePermission(Context context,String permission)
    {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(context, permission);
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestSinglePermission(Activity activity,String permission,final int RequestPermissionCode)
    {
        ActivityCompat.requestPermissions(activity, new String[]{ permission}, RequestPermissionCode);
    }
}
