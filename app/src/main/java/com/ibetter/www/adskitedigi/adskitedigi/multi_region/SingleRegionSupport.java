package com.ibetter.www.adskitedigi.adskitedigi.multi_region;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class SingleRegionSupport
{
    RelativeLayout parentLayout;
    HashMap<String, Integer> deviceInfo = new HashMap<>();
    Activity activity;
    Context context;

    public SingleRegionSupport(Context context) {
        this.context = context;
    }

    public SingleRegionSupport(Context context, Activity activity, RelativeLayout parentLayout)
    {
        this.parentLayout = parentLayout;
        deviceInfo = new DeviceModel().getDeviceProperties(activity);
        this.activity = activity;
        this.context = context;
    }

    public synchronized void processSingleRegionJSON(JSONObject info)
    {
        try {
               String type = info.getString(context.getString(R.string.multi_region_type_json_key));
                    if (type.equalsIgnoreCase(activity.getString(R.string.app_default_image_name)))
                    {
                        addImageRegion(info);

                    } else if (type.equalsIgnoreCase(activity.getString(R.string.app_default_video_name))) {

                        addVideoRegion(info);

                    } else if (type.equalsIgnoreCase(activity.getString(R.string.app_default_url_name))) {

                        addURLRegion(info);

                    } else
                    {
                        Toast.makeText(activity, "Unable to Display Campaign PerView", Toast.LENGTH_SHORT).show();
                        // addFileRegion(info);
                    }

                } catch (Exception e)
              {
                  Toast.makeText(activity, "Unable to Display Campaign PerView", Toast.LENGTH_SHORT).show();
                  e.printStackTrace();
            }
     }

    //add image region
    private synchronized void addImageRegion(JSONObject info) throws JSONException {

        String mainDir = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getPath() + "/Nearby";

        String imagePath = mainDir + "/" + info.getString(activity.getString(R.string.media_resource_json_key));
        File dirFile = new File(imagePath);
        //if(dirFile.exists())
        {
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), 100), calculateRequiredPixel(deviceInfo.get("height"), 100));

            //RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(requiredWidth,RelativeLayout.LayoutParams.MATCH_PARENT);

            ImageView image = new ImageView(activity);
            image.setLayoutParams(params1);

           // RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_action_new);

            image.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(activity)
                    .load(Uri.fromFile(dirFile))
                    //.apply(requestOptions)
                    .into(image);

            parentLayout.addView(image);

            Log.i("parentLayout","image is added");


        }
    }


    private int calculateRequiredPixel(int orginalValue, int percentage) {
        return (int) Math.round(new Constants().getPercentageAmount((double) orginalValue, percentage));
    }


    //add video region
    private void addVideoRegion(final JSONObject info) throws JSONException {

        String dir = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getPath() + "/Nearby";

        String imagePath = dir + "/" + info.getString(activity.getString(R.string.media_resource_json_key));
        File dirFile = new File(imagePath);

        if (dirFile.exists()) {
            RelativeLayout.LayoutParams parentLayoutParams = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), 100),
                    calculateRequiredPixel(deviceInfo.get("height"), 100));

            RelativeLayout videoParentLayout = new RelativeLayout(context);
            videoParentLayout.setLayoutParams(parentLayoutParams);

            parentLayout.addView(videoParentLayout);

            final VideoView videoView = new VideoView(context);
            boolean isStretch = true;
            boolean isRequestFocus = false;

            if (isStretch) {
                RelativeLayout.LayoutParams videoViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                //adding margins

                videoViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                videoViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                videoViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                videoViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                videoView.setLayoutParams(videoViewParams);
            } else {
                RelativeLayout.LayoutParams videoViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //adding margins

                videoViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);

                videoView.setLayoutParams(videoViewParams);


                videoParentLayout.addView(videoView);
                videoView.setVideoPath(dirFile.getAbsolutePath());
                if (isRequestFocus) {
                    videoView.requestFocus();
                } else {
                    videoView.start();
                }
            }
        }
    }

    //add url region
    private void addURLRegion(JSONObject info) throws JSONException
    {
        RelativeLayout.LayoutParams parentLayoutParams = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), 100),
                calculateRequiredPixel(deviceInfo.get("height"), 100));

        final WebView web = new WebView(context);
        web.setLayoutParams(parentLayoutParams);
        parentLayout.addView(web);
        //initialize web view
        // web settings
        WebSettings webSettings = web.getSettings();

        // false
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setAppCacheEnabled(true);

        // true
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // other
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);


        web.setWebViewClient(new WebViewClient() {


            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

            }

            public void onPageFinished(WebView view, String url) {

                //once the page loading finished remove the listeners
                web.setWebViewClient(null);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);


            }

        });

        web.loadUrl(info.getString(context.getString(R.string.app_default_url_name)));
    }
}
