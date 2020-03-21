package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.text.TextUtils;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class Constants
{
    //default downloadable chunk size
    //32768
    public static final int NEAR_BY_MODE =1;
    public static final int ENTERPRISE_MODE =2;
    public static final int CLOUD_MODE =3;



    public  static  final  int DISPLAY_TRIAL_PERIOD_STATUS=0;
    public  static  final  int DISPLAY_EXPIRED_STATUS=-1;
    public  static  final  int DISPLAY_SUCCESS_STATUS=1;


    //public static final int LOCAL_AND_SERVER_PLAYING_MODE =3;

    public static final int LOCAL_FOLDER_SEQUENTIAL_PLAYING_MODE =21;

    public static final int SCROLLING_MEDIA_NAME =1;
    public static final int SCROLLING_CUSTOMISED_TEXT =2;

    public final static int DEFAULT_SCROLL_MEDIA_POSITION = 1;
    public static final long DEFAULT_DOWNLOADABLE_CHUNK_SIZE =524288;


    public static final long DISPLAY_IMAGE_VIEW_DURATION = 10000;


    public static final long REFRESH_CAMPAIGNS_DURATION = 30000;//30 seconds

    public final static String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String LOCAL_SAVE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static int FCM_MAX_DELAY = 1; //in minutes

    public final static String GC_SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public final static long LICENCE_AUTO_REFRESH_INTERVAL = 5*60*1000;//every five minutes

    public final static boolean IS_ENABLE_HOT_SPOT_ALWAYS_SETTINGS_DEFAULT = false;

    public final static int HOT_SPOT_ENABLE_SUCCESS_CODE = 200;

    public final static int HOT_SPOT_ENABLE_FAILURE_UN_RECOVERABLE = 1;

    public final static int REQUEST_LOCATION_CHECK_SETTINGS_GLOBAL = 99999;

    public final static int HOT_SPOT_ENABLE_SUCCESS_UN_AVAILABLE = 2;

    public final static int SINGLE_VIDEO_REGION_VIDEO_VIEW_ID = 11;

    public final static String LAST_MEDIA_PLAYED_SP_KEY = "last_media_played_sp_key";
    public final static String LAST_MEDIA_PLAYED_POSITION_SP_KEY = "LAST_MEDIA_PLAYED_POSITION_SP_KEY";

    //convert to long
    public static Long convertToLong(String value)
    {
        try {
            return Long.parseLong(value);

        } catch (Exception e)
        {
            e.printStackTrace();
            return (long)0;
        }
    }
    //convert to int
    public static int convertToInt(String value)
    {
        try
        {
            return Integer.parseInt(value);
        }catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    //convert to long
    public static Double convertToDouble(String value)
    {
        try {
            return Double.parseDouble(value);

        } catch (Exception e)
        {
            e.printStackTrace();
            return (double)0;
        }
    }

    public static String capitalize(String str)
    {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr)
        {
            if (capitalizeNext && Character.isLetter(c))
            {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            }
            else if (Character.isWhitespace(c))
            {
                capitalizeNext = true;
            }

            phrase.append(c);

        }

        return phrase.toString();
    }

    //verify text length
    public boolean verifyTextLength(String text,int length)
    {
        if(text!=null&&text.length()>=length)
        {
            return true;
        }else
        {
            return false;
        }
    }

    // Remove extension from file name

    public static String removeExtension(String fileName) {

        if (fileName.indexOf(".") > 0)
        {
            return fileName.substring(0, fileName.lastIndexOf("."));

        }
        else
        {
            return fileName;

        }

    }

    public static int convertToSec(long millisec)
    {
        return (int)(millisec/1000);
    }

    public static long convertToMS(long sec)
    {
        return sec*1000;
    }


    public String saveBGAudioTo(Context context,String fileName)
    {


        // save to internal memory
        File dir = new File(context.getFilesDir() , context.getString(R.string.bg_audio_settings_dir));

        if (!dir.exists()) {
           dir.mkdirs();
        }

        if(fileName==null)
        {
            fileName = Calendar.getInstance().getTimeInMillis() + ".mp3";
        }
        return  (dir.getAbsolutePath() +File.separator+fileName );


    }


    public double getPercentageAmount(double amount,float percentage)
    {
        double percentageAmount = (percentage*amount)/100;

        return convertAmountToTwoDecimal(percentageAmount);
    }

    public double convertAmountToTwoDecimal(double amount)
    {
        return (Math.round(amount * 100.00) / 100.00);
    }

    //replace msg special characters
    public static  String replaceSpecialCharacters(String msg) throws UnsupportedEncodingException
    {
        msg = msg.replaceAll(" ", "%20");
        //msg = URLEncoder.encode(msg, "utf-8");

        // msg = msg.replaceAll("\\+", "%20");

        return msg;

    }
}
