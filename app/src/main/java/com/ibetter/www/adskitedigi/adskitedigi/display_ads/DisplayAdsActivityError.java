package com.ibetter.www.adskitedigi.adskitedigi.display_ads;

import android.content.Context;
import android.content.Intent;

import com.ibetter.www.adskitedigi.adskitedigi.R;

/**
 * Created by ibetter-Dell on 18-11-16.
 */

public class DisplayAdsActivityError {

    private int errorCode;
    private String errorMsg;

    protected void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    protected int getErrorCode()
    {
        return errorCode;
    }

    protected void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    protected String getErrorMsg()
    {
        return errorMsg;
    }




}
