package com.ibetter.www.adskitedigi.adskitedigi.register;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ibetter.www.adskitedigi.adskitedigi.model.DisplayDialog;

/**
 * Created by vineeth_ibetter on 11/17/16.
 */

public class RegisterActivityModel {

    private DisplayDialog dialogModel;

    protected String REGISTER_INTENT_ACTION="com.ibetter.www.adskitedigi.adskitedigi.register";

    private RegisterActivity.RegisterDisplayReceiver registerDisplayReceiver;
    private Context registerActivityContext;


    /* SET AND GET REGISTER ACTIVITY CONTEXT*/
    public void setRegisterActivityContext(Context registerActivityContext)
    {
        this.registerActivityContext=registerActivityContext;

    }
    public Context getRegisterActivityContext()
    {
       return  registerActivityContext;

    }

    /* SET AND GET REGISTER DISPLAY Dialog model*/
    public void setRegisterDialogModel(DisplayDialog dialogModel)
    {
        this.dialogModel=dialogModel;

    }

    public DisplayDialog getRegisterDialogModel()
    {
        return  dialogModel;

    }


    /* SET AND GET REGISTER DISPLAY RECIVER*/
    public void setRegisterDisplayServiceReceiver(RegisterActivity.RegisterDisplayReceiver registerDisplayReceiver)
    {
        this.registerDisplayReceiver=registerDisplayReceiver;

    }

    public RegisterActivity.RegisterDisplayReceiver getRegisterDisplayServiceReceiver()
    {
        return  registerDisplayReceiver;

    }

    /*register the register display receiver*/
    public  boolean registerRegisterDisplayReceiver()
    {

        if (registerDisplayReceiver != null) {

            IntentFilter intentFilter = new IntentFilter(REGISTER_INTENT_ACTION);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

            registerActivityContext.registerReceiver(registerDisplayReceiver, intentFilter);

            return true;
        }else
        {
            return false;

        }


    }

    /*un register  receiver*/
    public void unRegisterRegisterDisplayReceiver()
    {
        try
        {

         registerActivityContext.unregisterReceiver(registerDisplayReceiver);

        }
        catch (Exception e)
        {

        }finally
        {
           setRegisterDisplayServiceReceiver(null);
        }
    }

}
