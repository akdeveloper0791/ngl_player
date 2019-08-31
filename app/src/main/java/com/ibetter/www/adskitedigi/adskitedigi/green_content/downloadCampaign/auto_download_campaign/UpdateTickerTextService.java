package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings.ScrollTextSettingsModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class UpdateTickerTextService extends IntentService {
    private Context context;

    public UpdateTickerTextService()
    {
        super("UpdateTickerText");
        context = UpdateTickerTextService.this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Cursor cursor = CampaignsDBModel.getTickerTexts(context);

        StringBuilder sb = new StringBuilder();

        String bgColor="#fbf5da0b",textColor="#cc2507";
        boolean isBold=false,isItalic=false;
        long serverID=0;
        int textSize=40;

        boolean isUpdate=false;

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {


                    String info = cursor.getString(cursor.getColumnIndex(CampaignsDBModel.TICKER_TEXT_INFO));
                     serverID = cursor.getInt(cursor.getColumnIndex(CampaignsDBModel.TICKER_TEXT_SERVER_ID));

                    JSONObject jsonObject = new JSONObject(info);

                    JSONArray jsonArray = jsonObject.getJSONArray("regions");


                    JSONObject infoJSON = (JSONObject) jsonArray.get(0);
                    String tickerText = infoJSON.getString("media_name");



                        sb.append(tickerText);
                        sb.append("      ");


                    if(cursor.isLast())
                    {
                        JSONObject propJSON = infoJSON.getJSONObject("properties");
                         bgColor=propJSON.getString("textBgColor");
                         textColor=propJSON.getString("textColor");

                         isBold = propJSON.getBoolean("isBold");
                         isItalic = propJSON.getBoolean("isItalic");
                         textSize = propJSON.getInt("textSize");

                    }

                }
                while (cursor.moveToNext());



                long prevStyleID=new ScrollTextSettingsModel(context).getLocalScrollTextStyleId();

                if(prevStyleID!=serverID)
                {
                    SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);

                    SharedPreferences.Editor editor = saveSP.edit();

                    //editor.putString(getString(R.string.local_scroll_text),text);
                    editor.putString(context.getString(R.string.scroll_text_bg_color), bgColor);
                    editor.putString(context.getString(R.string.scroll_text_text_color), textColor);
                    editor.putInt(context.getString(R.string.scroll_text_text_size), textSize);
                    editor.putLong(context.getString(R.string.local_scroll_text_style_id), serverID);
                    editor.putBoolean(context.getString(R.string.local_scroll_text_bold), isBold);
                    editor.putBoolean(context.getString(R.string.local_scroll_text_italic), isItalic);

                    editor.commit();

                    isUpdate=true;
                }

                String scrollText=sb.toString();



                String previousText = new ScrollTextSettingsModel(context).getLocalScrollLocalText();
                if (!(previousText != null && previousText.equals(scrollText))) {

                    new ScrollTextSettingsModel(context).setLocalScrollText(scrollText);

                    isUpdate=true;
                }

              //IF TICKER TEXT IS OFF
                if( !new ScrollTextSettingsModel(context).isScrollTextOn())
                {
                    new ScrollTextSettingsModel(context).setScrollTextStatus(true);

                    isUpdate=true;
                }


            }else
            {
                if(new ScrollTextSettingsModel(context).isScrollTextOn()) {
                    new ScrollTextSettingsModel(context).setScrollTextStatus(false);
                    new ScrollTextSettingsModel(context).setLocalScrollText("");

                    isUpdate=true;
                }
            }


        } catch (Exception E)
        {
            E.printStackTrace();
        }finally {
            if(isUpdate)
            {
                updateTickerTextSettings();
            }
        }

    }

    private void updateTickerTextSettings()
    {
        Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(context.getString(R.string.action),context.getString(R.string.update_scroll_text_action));

        context.sendBroadcast(intent);

    }




}
