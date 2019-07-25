package com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by vineeth_ibetter on 12/30/17.
 */

public class ScrollTextSettingsModel  {

private Context scrollTextContext;
private LinearLayout scrollingTextLayout;
private Button updateScrollText;
private EditText scrollingTextEt;
private RadioButton scrollingCustomisedRB,scrollingMediaNameRB;
private RadioGroup scrollingTextModeRadioGroup;
private RelativeLayout scrollingCustomisedTextLayout;
private boolean DEFAULT_SCROLL_TEXT_VALUE = true;





    public ScrollTextSettingsModel(Context scrollTextContext, LinearLayout scrollingTextLayout, Button updateScrollText, EditText scrollingTextEt, RadioButton scrollingCustomisedRB, RadioButton scrollingMediaNameRB, RadioGroup scrollingTextModeRadioGroup, RelativeLayout scrollingCustomisedTextLayout)
    {

        this.scrollTextContext = scrollTextContext;
        this.scrollingTextLayout = scrollingTextLayout;
        this.updateScrollText = updateScrollText;
        this.scrollingTextEt = scrollingTextEt;
        this.scrollingCustomisedRB = scrollingCustomisedRB;
        this.scrollingMediaNameRB = scrollingMediaNameRB;
        this.scrollingTextModeRadioGroup = scrollingTextModeRadioGroup;
        this.scrollingCustomisedTextLayout = scrollingCustomisedTextLayout;

       setRadioButtons();

    }

    public ScrollTextSettingsModel(Context scrollTextContext) {
        this.scrollTextContext = scrollTextContext;
    }

    public boolean isScrollTextOn()
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(scrollTextContext);

        return saveSP.getBoolean(scrollTextContext.getString(R.string.is_scrolling_text_on), DEFAULT_SCROLL_TEXT_VALUE);
    }

    public boolean setScrollTextStatus(boolean status)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(scrollTextContext);
        SharedPreferences.Editor saveSPEditor = saveSP.edit();

        Log.i("status","::"+status);
        saveSPEditor.putBoolean(scrollTextContext.getString(R.string.is_scrolling_text_on), status);

        return saveSPEditor.commit();
    }

    public String getLocalScrollText()
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(scrollTextContext);

        switch(scrollingTextModeRadioGroup.getCheckedRadioButtonId())
        {
            case R.id.scrolling_customized_text:
                return saveSP.getString(scrollTextContext.getString(R.string.local_scroll_text), scrollTextContext.getString(R.string.display_ads_layout_scrolling_text));

            case R.id.scrolling_media_name:
                return String.valueOf(saveSP.getInt(scrollTextContext.getString(R.string.local_media_scroll_text), Constants.DEFAULT_SCROLL_MEDIA_POSITION));

            default:
                return saveSP.getString(scrollTextContext.getString(R.string.local_scroll_text), scrollTextContext.getString(R.string.display_ads_layout_scrolling_text));

        }

    }

    public boolean setLocalScrollText(String text)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(scrollTextContext);
        SharedPreferences.Editor saveSPEditor = saveSP.edit();

        saveSPEditor.putString(scrollTextContext.getString(R.string.local_scroll_text), text);

        return saveSPEditor.commit();
    }

    public boolean setLocalScrollMedia(int position)
    {
        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(scrollTextContext);
        SharedPreferences.Editor saveSPEditor = saveSP.edit();

        saveSPEditor.putInt(scrollTextContext.getString(R.string.local_media_scroll_text), position);

        return saveSPEditor.commit();
    }

    protected void  displayScrollTextLayout()
    {
        scrollingTextLayout.setVisibility(View.VISIBLE);
    }

    protected void  dismissScrollTextLayout()
    {
        scrollingTextLayout.setVisibility(View.GONE);
    }

    protected void saveScrolledText()
    {
        updateScrollText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (scrollingTextModeRadioGroup.getCheckedRadioButtonId())
                {
                    case R.id.scrolling_media_name:
                        saveScrollMedia();
                        break;

                    case R.id.scrolling_customized_text:
                        saveCustomizedText();
                        break;
                }

            }
        });


    }

    private void saveCustomizedText()
    {
        String text=scrollingTextEt.getText().toString();

        if(text!=null&text.length()>0)
        {
            if(setLocalScrollText(text))
            {
                Toast.makeText(scrollTextContext,"Updated",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            scrollingTextEt.setError(scrollTextContext.getString(R.string.local_scroll_text_err_msg));
        }
    }

    private void saveScrollMedia()
    {
        int scrollMediaPosition= Constants.convertToInt(scrollingTextEt.getText().toString());

        if(scrollMediaPosition>=0)
        {
            if(setLocalScrollMedia(scrollMediaPosition))
            {
                Toast.makeText(scrollTextContext,"Updated",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            scrollingTextEt.setError(scrollTextContext.getString(R.string.invalid_scroll_media));
        }
    }


    public RadioButton getScrollingCustomisedRB() {
        return scrollingCustomisedRB;
    }

    public RadioButton getScrollingMediaNameRB() {
        return scrollingMediaNameRB;
    }

    public RadioGroup getScrollingTextModeRadioGroup() {
        return scrollingTextModeRadioGroup;
    }

    public RelativeLayout getScrollingCustomisedTextLayout() {
        return scrollingCustomisedTextLayout;
    }

    private void setRadioButtons() {


        int mode = new User().getLocalScrollTextMode(scrollTextContext);

        switch (mode)
        {
            case Constants.SCROLLING_CUSTOMISED_TEXT:
                scrollingCustomisedRB.setChecked(true);
                scrollingCustomisedTextLayout.setVisibility(View.VISIBLE);
                break;

            case Constants.SCROLLING_MEDIA_NAME:
                scrollingMediaNameRB.setChecked(true);
                scrollingCustomisedTextLayout.setVisibility(View.VISIBLE);
                break;

            default:
                    scrollingCustomisedRB.setChecked(true);
                    scrollingCustomisedTextLayout.setVisibility(View.VISIBLE);
                    break;


        }



        scrollingTextModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(radioGroup.getCheckedRadioButtonId()== R.id.scrolling_media_name)
                {
                    new User().setLocalScrollTextMode(scrollTextContext,Constants.SCROLLING_MEDIA_NAME);
                    scrollingCustomisedTextLayout.setVisibility(View.VISIBLE);
                    scrollingTextEt.setInputType(InputType.TYPE_CLASS_NUMBER);
                    scrollingTextEt.setHint(scrollTextContext.getString(R.string.media_scroll_text_hint));
                }
                else  if(radioGroup.getCheckedRadioButtonId()== R.id.scrolling_customized_text)
                {

                    new User().setLocalScrollTextMode(scrollTextContext,Constants.SCROLLING_CUSTOMISED_TEXT);

                    scrollingCustomisedTextLayout.setVisibility(View.VISIBLE);
                    scrollingTextEt.setInputType(InputType.TYPE_CLASS_TEXT);

                    scrollingTextEt.setHint(scrollTextContext.getString(R.string.custom_scroll_text_hint));
                }

                scrollingTextEt.setText(getLocalScrollText());
            }
        });


        scrollingTextEt.setText(getLocalScrollText());



    }


    //get media name to scroll
    public String getMediaNameToScroll(String filePath)
    {


        if(filePath!=null && new File(filePath).exists())
        {
            String completeName =  new Constants().removeExtension(new File(filePath).getName());

            String[] nameBuilder = completeName.split(scrollTextContext.getString(R.string.file_name_seperator));
            //get selected media scroll position
            SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(scrollTextContext);
            int selectedMediaPosition = (saveSP.getInt(scrollTextContext.getString(R.string.local_media_scroll_text),  Constants.DEFAULT_SCROLL_MEDIA_POSITION));

            if(nameBuilder.length>=selectedMediaPosition)
            {
                if(selectedMediaPosition>0) {
                    return nameBuilder[--selectedMediaPosition];
                }else
                {
                    return nameBuilder[0];
                }
            }else
            {
                return completeName;
            }
        }else
        {
            return null;
        }
    }

  //get text settings response
    public static JSONObject GetTextSettingsResponseJSON(Context context) throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put(context.getString(R.string.scroll_text), new User().getLocalScrollText(context));
        obj.put(context.getString(R.string.is_scroll_text_on),new User().isScrollTextOn(context));
        obj.put(context.getString(R.string.scroll_text_mode),new User().getLocalScrollTextMode(context));
        obj.put(context.getString(R.string.scroll_media_name_position),new User().getMediaScrollTextPosition(context));

        return obj;
    }

    public static void updateTextSettingsFromSM(Context context,String newSettings) throws JSONException,Exception
    {
            JSONObject jsonObject = new JSONObject(newSettings);
            boolean isScrollTextOn=jsonObject.getBoolean(context.getString(R.string.is_scroll_text_on));


            SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
            SharedPreferences.Editor saveSPEditor = saveSP.edit();

            if(isScrollTextOn)
            {
                int scrollTextMode = jsonObject.getInt(context.getString(R.string.scroll_text_mode));


                saveSPEditor.putInt(context.getString(R.string.local_scroll_text_mode), scrollTextMode);

                switch (scrollTextMode)
                {
                    case Constants.SCROLLING_CUSTOMISED_TEXT:
                        saveSPEditor.putString(context.getString(R.string.local_scroll_text), jsonObject.getString(context.getString(R.string.scroll_text)));
                        break;

                    case Constants.SCROLLING_MEDIA_NAME:
                        saveSPEditor.putInt(context.getString(R.string.local_media_scroll_text), jsonObject.getInt(context.getString(R.string.scroll_media_name_position)));
                        break;

                    default:
                        saveSPEditor.putString(context.getString(R.string.local_scroll_text), jsonObject.getString(context.getString(R.string.scroll_text)));
                        break;


                }

            }

            saveSPEditor.putBoolean(context.getString(R.string.is_scrolling_text_on), isScrollTextOn);

            saveSPEditor.commit();

            Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(context.getString(R.string.action),context.getString(R.string.update_scroll_text_action));

            context.sendBroadcast(intent);



    }

    public static void updateTickerValuesFromJson(Context context,String jsonText,String updatedAt)
    {
        try
        {
        if (jsonText != null)
        {
            JSONObject jsonObject = new JSONObject(jsonText);

            JSONArray jsonArray =jsonObject.getJSONArray("regions");

            JSONObject info = jsonArray.getJSONObject(0);

            //
            JSONObject properties = info.getJSONObject(context.getString(R.string.multi_region_properties_json_key));



            String bgColor = properties.getString("textBgColor");
            String textColor = properties.getString("textColor");
            int textSize = properties.getInt("textSize");

            //check and set text
            String text = info.getString(context.getString(R.string.multi_region_media_name_json_key));


            boolean isBold=properties.getBoolean("isBold");

            boolean isItalic=properties.getBoolean("isItalic");


            Log.i("bgColor",bgColor);
            Log.i("textColor",textColor);
            SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);

            SharedPreferences.Editor editor =saveSP.edit();

            //editor.putString(getString(R.string.local_scroll_text),text);
            editor.putString(context.getString(R.string.scroll_text_bg_color),bgColor);
            editor.putString(context.getString(R.string.scroll_text_text_color),textColor);
            editor.putInt(context.getString(R.string.scroll_text_text_size),textSize);
            editor.putString(context.getString(R.string.scroll_text_updated_at),updatedAt);
            editor.putString(context.getString(R.string.local_scroll_text),text);
            editor.putBoolean(context.getString(R.string.local_scroll_text_bold),isBold);
            editor.putBoolean(context.getString(R.string.local_scroll_text_italic),isItalic);

            editor.commit();

        }
     }
        catch (Exception e)
     {
        e.printStackTrace();
     }
    }
}
