package com.ibetter.www.adskitedigi.adskitedigi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

/**
 * Created by vineeth_ibetter on 1/2/18.
 */

public class MemCardReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            onMemcardMounted(context);
        }
        else if (!Environment.getExternalStorageState().equals(Environment.MEDIA_CHECKING)){
            onMemorycardUnMounted(context);
        }
    }

    private void onMemorycardUnMounted(Context context) {
       // Toast.makeText(context,"adskite media un mounted",Toast.LENGTH_LONG).show();
    }

    private void onMemcardMounted(Context context) {
        //Toast.makeText(context,"adskite media mounted",Toast.LENGTH_LONG).show();
    }
}
