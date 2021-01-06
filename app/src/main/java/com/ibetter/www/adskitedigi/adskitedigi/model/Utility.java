package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import static android.content.Context.MODE_PRIVATE;
import static com.ibetter.www.adskitedigi.adskitedigi.model.Constants.IS_ENABLE_HOT_SPOT_ALWAYS_SETTINGS_DEFAULT;

public class Utility {

    public static boolean canEnableHotSpot(Context context){
        SharedPreferences settingsSP = context.getSharedPreferences(context.getString(R.string.settings_sp), MODE_PRIVATE);
        return settingsSP.getBoolean(context.getString(R.string.is_hot_spot_enable_always), IS_ENABLE_HOT_SPOT_ALWAYS_SETTINGS_DEFAULT);
    }

    //checks and returns the youtube id
    public static String isYouTubeUrl(String url) {
        if(url != null && (url.contains("youtu.be") || url.contains("youtube.com"))) {
            String[] urlParts = url.split("/");
            String videoId =  urlParts[(urlParts.length-1)];
            if(videoId.startsWith("?watch")) {
                return videoId.split("=")[1];
            } else {
                return videoId;
            }
        }
        return null;
    }
}
