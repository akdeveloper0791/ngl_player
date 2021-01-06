package com.ibetter.www.adskitedigi.adskitedigi.green_content.campaign_preview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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

    //open with def browser
    public void openWithThirdPartyApp(String url) {
        try {
            if (url.contains(getString(R.string.def_zoom_us))) {
                //check and open zoom app
                checkAndOpenZoomApp(url);
            } else if (url.contains(getString(R.string.app_youtube_url)) || url.contains(
                    getString(R.string.youtube_url))) {
                checkAndOpenYouTubeApp(url);
            } else if (url.contains(getString(R.string.def_google_meet_us))) {
                checkAndOpenGoogleMeet(url);
            } else {
                //not supported url
                playURL(url);
            }

            finish();

        } catch (ActivityNotFoundException e) {
            playURL(url);
        } catch (ArrayIndexOutOfBoundsException e) {
            //Invalid request ,, open with ss
            // Toast.makeText(context, "Invalid request", Toast.LENGTH_SHORT).show();
            playURL(url);
        } catch (Exception e) {
            e.printStackTrace();
            //error in playing display media , try play next media
            playURL(url);
        }
    }

    //check and open zoom url
    private void checkAndOpenZoomApp(String zoomURL) throws ArrayIndexOutOfBoundsException {
        String[] splittedZoomUrl = zoomURL.split("/");
        if (splittedZoomUrl[splittedZoomUrl.length - 2].equalsIgnoreCase("j")) {
            //meeting join request,,open app
            Intent yt_play = new Intent(Intent.ACTION_VIEW, Uri.parse("zoomus://zoom.us/join?action=join&confno=" + splittedZoomUrl[splittedZoomUrl.length - 1]));
            Intent chooser = Intent.createChooser(yt_play, "Open With");

            if (yt_play.resolveActivity(getPackageManager()) != null) {
                startActivity(yt_play);
            } else {
                //No App found ,, open with ss
                openWithDefBrowser(zoomURL);
            }

        } else {
            //open with ss,, Invalid request
            openWithDefBrowser(zoomURL);
        }
    }

    private void checkAndOpenGoogleMeet(String googleMeet) throws ArrayIndexOutOfBoundsException {
        //meeting join request,,open app
        Intent yt_play = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMeet));
        Intent chooser = Intent.createChooser(yt_play, "Open With");

        if (yt_play.resolveActivity(getPackageManager()) != null) {
            startActivity(yt_play);
        } else {
            //No App found ,, open with ss
            openWithDefBrowser(googleMeet);
        }

    }

    //check and open youtube app
    private void checkAndOpenYouTubeApp(String url) throws Exception, ActivityNotFoundException {
        String videoID = null;
        if (url.contains(getString(R.string.app_youtube_url))) {
            String[] splitUrls = url.split("/");
            videoID = splitUrls[splitUrls.length - 1];
        } else {
            Uri uri = Uri.parse(url);
            videoID = uri.getQueryParameter("v");
        }

        if (videoID != null) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoID));
            startActivity(appIntent);
        } else {
            //play with ss
            playURL(url);
        }
    }

    private void openWithDefBrowser(String url) throws ActivityNotFoundException {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setPackage("com.android.chrome");
        browserIntent.setData(Uri.parse(url));
        startActivity(browserIntent);
    }

    private void playURL(String url) {
        Toast.makeText(this,"Unable to play the URL "+url,Toast.LENGTH_SHORT).show();
        finish();
    }

}
