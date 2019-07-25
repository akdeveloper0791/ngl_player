package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.announcement_settings.AnnouncementSettingsConstants;

import java.io.File;
import java.net.URL;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class Validations {

    public boolean validateMobileNumber(Context context,String mobileNumber)
    {
        try
        {
            if(mobileNumber!=null)
            {

                if (mobileNumber.length() == 10)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;

            }
        }
        catch(Exception e)
        {
            return false;

        }

    }

    public boolean validateFolderPath(Context context,String path)
    {
        try
        {

           if(path!=null&&path.length()>0)
            {
                return new File(path).exists();
            }
            else
           {
               return  false;
           }
        }
        catch(Exception e)
        {
            return false;

        }

    }

    public boolean validateAnnouncementText(String name)
    {

        if(name!=null && name.length()>= AnnouncementSettingsConstants.Announcement_Text_Length)
        {
            return true;
        }else
        {
            return false;
        }
    }

    public static boolean validateEmail(Context context,String email)
    {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public boolean validateEditText(EditText et)
    {
        if(et==null)
        {
            return false;
        }


        String data=et.getText().toString();

        if(data!=null && data.length()>0)
        {
            return true;
        }else
        {
            return false;
        }
    }

    public boolean validateString(String data)
    {


        if(data!=null && data.length()>0)
        {
            return true;
        }else
        {
            return false;
        }
    }

    public static boolean validateString(Context context, String data,int length)
    {


        if(data!=null && data.length()>length)
        {
            return true;
        }else
        {
            return false;
        }
    }

    //check whether to display multi layout cloud mode
    public boolean isDisplayMultiCloudMode(Context context)
    {
        //check this code with some hard work then you will rock

        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(context.getString(R.string.white_label_app_package), PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {

        }

        return false;

    }


    public static boolean isValidURL(String url)
    {
        // Try creating a valid URL
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setMandatoryRequired(Context context,EditText editText)
    {
        editText.setHint(TextUtils.concat(editText.getHint(), Html.fromHtml(context.getString(R.string.required_asterisk))));
    }


}
