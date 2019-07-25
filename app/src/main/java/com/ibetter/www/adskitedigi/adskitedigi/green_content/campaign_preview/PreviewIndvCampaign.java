package com.ibetter.www.adskitedigi.adskitedigi.green_content.campaign_preview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.multi_region.MultiRegionSupport;
import com.ibetter.www.adskitedigi.adskitedigi.multi_region.SingleRegionSupport;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PreviewIndvCampaign extends Activity
{
    HashMap<String,Object> multiRegionProp;
    private Context context;
    private RelativeLayout multiRegionParent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        context=PreviewIndvCampaign.this;
        setContentView(R.layout.preview_indv_campaign_layout);

        multiRegionParent =findViewById(R.id.dynamic_view_parent);

        processAndDisplayResponse(getIntent().getStringExtra("info"));

    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration)
    {
        super.onConfigurationChanged(newConfiguration);
    }

    private void processAndDisplayResponse(String info)
    {
        try
        {
                JSONObject jsonObject = new JSONObject(info);
                String type = jsonObject.getString("type");

                if(type.equalsIgnoreCase(context.getString(R.string.app_default_image_name))||type.equalsIgnoreCase(context.getString(R.string.app_default_video_name))||type.equalsIgnoreCase(context.getString(R.string.url_txt)))
                {
                 playSingleRegion(jsonObject);

                }
                else if(type.equalsIgnoreCase(context.getString(R.string.app_default_multi_region)))
                {
                    playMultiRegion(jsonObject);
                }else
                {
                    Toast.makeText(context,"Unable to Preview the Campaign",Toast.LENGTH_SHORT).show();
                }

            }catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(context,"Unable to Preview the Campaign"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
    }

    //process single region
    private void playSingleRegion(JSONObject jsonObject) throws JSONException
    {
        new SingleRegionSupport(context,PreviewIndvCampaign.this, multiRegionParent).processSingleRegionJSON(jsonObject);
    }

    //process multi region
    private void playMultiRegion(JSONObject jsonObject) throws JSONException
    {
           multiRegionProp = new MultiRegionSupport(context, PreviewIndvCampaign.this, multiRegionParent).processMultiRegionJSON(jsonObject.getJSONArray("regions"));
    }
}
