package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.ibetter.www.adskitedigi.adskitedigi.R;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class DisplayDialog {

    private ProgressDialog busyDialog;

    public AlertDialog.Builder displayAlertDialog(Context context,String alertMSG,String alertTitle,boolean isCancellable)
    {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT);

        if(alertMSG.length()>2)
        {
            alertDialog.setMessage(alertMSG);
        }

        alertDialog.setTitle(alertTitle);
        alertDialog.setNegativeButton(context.getString(R.string.app_default_alert_negative_button_ok_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alertDialog.setCancelable(isCancellable);

        return alertDialog;
    }



    /*display busy dialog*/
    public ProgressDialog displayBusyDialog(Context context,String message,boolean isCancellable)
    {
        busyDialog=new ProgressDialog(context);
        busyDialog.setMessage(message);
        busyDialog.setCancelable(isCancellable);

        return busyDialog;

    }

    /*dismiss busy dialog*/
    public void dismissBusyDialog()
    {
        if(busyDialog!=null&&busyDialog.isShowing())
        {
                busyDialog.dismiss();

        }
    }



}
