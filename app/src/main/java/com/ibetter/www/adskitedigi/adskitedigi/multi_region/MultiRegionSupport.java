package com.ibetter.www.adskitedigi.adskitedigi.multi_region;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class MultiRegionSupport
{
    RelativeLayout parentLayout;
    HashMap<String,Integer> deviceInfo = new HashMap<>();
    Activity activity;
    Context context;
    private Random random;

    public MultiRegionSupport(Context context)
    {
        this.context = context;
        this.random = new Random();
    }

    public MultiRegionSupport(Context context,Activity activity, RelativeLayout parentLayout)
    {
        this.parentLayout = parentLayout;
        deviceInfo = new DeviceModel().getDeviceProperties(activity);
        this.activity = activity;
        this.context = context;
        this.random = new Random();
    }

    public synchronized HashMap<String,Object> processMultiRegionJSON(JSONArray regions)
    {
       HashMap<String,Object> multiRegProperties = new HashMap<>(2);

        for(int i=0;i<regions.length();i++)
        {
            try {

                JSONObject info = regions.getJSONObject(i);
                String type = info.getString(activity.getString(R.string.multi_region_type_json_key));
                 if(type.equalsIgnoreCase(
                         activity.getString(R.string.app_default_image_name)))
                 {
                     addImageRegion(info);
                 }else if(type.equalsIgnoreCase(
                         activity.getString(R.string.app_default_txt_name)))
                 {
                     addTextRegion(info);
                 }
                 else if(type.equalsIgnoreCase(
                         activity.getString(R.string.app_default_video_name)))
                 {
                     multiRegProperties =  addVideoRegion(info);
                     multiRegProperties.put(context.getString(R.string.has_video),true);

                 }else if(type.equalsIgnoreCase(
                         activity.getString(R.string.app_default_url_name)))
                 {
                     addURLRegion(info);
                 } else if(type.equalsIgnoreCase(activity.getString(R.string.app_default_file_name)))
                 {
                     addFileRegion(info);
                 }

            }catch (JSONException e)
            {
               e.printStackTrace();
            }
        }

        return multiRegProperties;
    }

    //add image region
    private synchronized void addImageRegion(JSONObject info) throws JSONException
    {

        String dir= new User().getUserPlayingFolderModePath(activity)+activity.getString(R.string.campaign_images_dir);
        if(info.has(context.getString(R.string.multi_region_is_self_path_json_key)) &&
                info.getBoolean(context.getString(R.string.multi_region_is_self_path_json_key)))
        {

            dir = new User().getUserPlayingFolderModePath(activity);
        }

        String imagePath = dir+"/"+info.getString(activity.getString(R.string.multi_region_media_name_json_key));
        File dirFile=new File(imagePath);
        //if(dirFile.exists())
        {


          RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_width_json_key))),
                    calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_height_json_key))));

          //RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(requiredWidth,RelativeLayout.LayoutParams.MATCH_PARENT);

            ImageView image = new ImageView(activity);
            image.setLayoutParams(params1);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_action_new);

            //check and set properties
            if(info.has(context.getString(R.string.multi_region_properties_json_key)))
            {
               JSONObject properties = info.getJSONObject(context.getString(R.string.multi_region_properties_json_key));
               {
                   //set scale type
                    switch ((String)properties.get("scaleType")) {

                        default:
                            image.setScaleType(ImageView.ScaleType.FIT_XY);
                            break;

                        case "optionalCenterCrop":
                            requestOptions.optionalCenterCrop();
                            break;
                        case "optionalCenterInside":
                            requestOptions.optionalCenterInside();
                            break;
                        case "optionalFitCenter":
                            requestOptions.optionalFitCenter();
                            break;
                        case "optionalCircleCrop":
                            requestOptions.optionalCircleCrop();
                            break;
                        case "centerCrop":
                            requestOptions.centerCrop();
                            break;
                        case "centerInside":
                            requestOptions.centerInside();
                            break;
                        case "fitCenter":
                            requestOptions.fitCenter();
                            break;
                        case "circleCrop":
                            requestOptions.circleCrop();
                            break;


                    }
                }
            }else
            {
                //default
                //scale
                image.setScaleType(ImageView.ScaleType.FIT_XY);
            }




           Glide.with(activity)
                    .load(Uri.fromFile(dirFile))
                    .apply(requestOptions)
                    .into(image);

          //adding margins
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) image.getLayoutParams();
            marginLayoutParams.leftMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_left_margin_json_key)));
            marginLayoutParams.rightMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_right_margin_json_key)));
            marginLayoutParams.topMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_top_margin_json_key)));
            marginLayoutParams.bottomMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_bottom_margin_json_key)));

            parentLayout.addView(image);


        }
    }


    //add text region
    //add text region
    private synchronized void addTextRegion(JSONObject info) throws JSONException
    {
        //properties
        String bgColor = context.getResources().getString(R.string.display_ad_scrolling_text_bg_color),
                textColor = context.getResources().getString(R.string.display_ad_scrolling_text_color);
        int textSize = 40,textAlignment=0; boolean isDisplayScrollAnim = true;
        boolean isBold=false,isItalic=false,isUnderLine=false;

        if(info.has(context.getString(R.string.multi_region_properties_json_key))) {
            JSONObject properties = info.getJSONObject(context.getString(R.string.multi_region_properties_json_key));
            bgColor = properties.getString("textBgColor");
            textColor = properties.getString("textColor");
            textSize = properties.getInt("textSize");
            isDisplayScrollAnim = properties.getBoolean("isScrollAnim");

            if(properties.has("textAlignment"))
            {
                textAlignment=properties.getInt("textAlignment");
            }

            if(properties.has("isBold"))
            {
                isBold=properties.getBoolean("isBold");
            }

            if(properties.has("isItalic"))
            {
                isItalic=properties.getBoolean("isItalic");
            }

            if(properties.has("isUnderLine"))
            {
                isUnderLine=properties.getBoolean("isUnderLine");
            }
        }


        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_width_json_key))),
                calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_height_json_key))));

        RelativeLayout textViewParent = new RelativeLayout(context);
        textViewParent.setLayoutParams(params1);
        textViewParent.setBackgroundColor(Color.parseColor(bgColor));

        //adding margins
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) textViewParent.getLayoutParams();
        marginLayoutParams.leftMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_left_margin_json_key)));
        marginLayoutParams.rightMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_right_margin_json_key)));
        marginLayoutParams.topMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_top_margin_json_key)));
        marginLayoutParams.bottomMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_bottom_margin_json_key)));

        parentLayout.addView(textViewParent);

        params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView image = new TextView(context);
        image.setLayoutParams(params1);

        //check and set text
        String text = info.getString(context.getString(R.string.multi_region_media_name_json_key));
        if(text.equalsIgnoreCase(context.getString(R.string.multi_region_default_scroll_text_value)))
        {
            //assign gloabl text
            SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
            text = saveSP.getString(context.getString(R.string.local_scroll_text),context.getString(R.string.display_ads_layout_scrolling_text));
        }

        image.setText(text);

        if(isDisplayScrollAnim) {
            image.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            image.setSingleLine(true);
            image.setMarqueeRepeatLimit(-1);
            image.setSelected(true);
        }

        image.setTextSize(textSize);
        image.setTextColor(Color.parseColor(textColor));

        if(textAlignment>0)
        {
            //textViewParent.setGravity(Gravity.CENTER);
            textViewParent.setGravity(textAlignment);
            image.setGravity(textAlignment);
        }
        if(isBold|| isItalic)
        {
            if(isBold&&isItalic)
            {
                image.setTypeface(image.getTypeface(), Typeface.BOLD_ITALIC);
            }else if(isBold)
            {
                image.setTypeface(image.getTypeface(), Typeface.BOLD);
            }else if(isItalic)
            {
                image.setTypeface(image.getTypeface(), Typeface.ITALIC);
            }
        }
        if(isUnderLine)
        {
            image.setPaintFlags(image.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        }

        textViewParent.addView(image);

    }


    public final static int calculateRequiredPixel(int orginalValue,int percentage)
    {
        return (int)Math.round(new Constants().getPercentageAmount((double)orginalValue,percentage));
    }

    public void deleteResources(JSONObject jsonObject) throws Exception
    {


        JSONArray regions = jsonObject.getJSONArray("regions");


        for(int i=0;i<regions.length();i++)
        {

            try {

                JSONObject info = regions.getJSONObject(i);
                String type = info.getString(context.getString(R.string.multi_region_type_json_key));

                if(type.equalsIgnoreCase(
                        context.getString(R.string.app_default_image_name)))
                {
                    boolean isSelfPath = false;
                    if(info.has(context.getString(R.string.multi_region_is_self_path_json_key)))
                    {
                        isSelfPath = info.getBoolean(context.getString(R.string.multi_region_is_self_path_json_key));
                    }

                    deleteImageResource(info.getString(context.getString(R.string.multi_region_media_name_json_key)),isSelfPath);
                }else if(type.equalsIgnoreCase(context.getString(R.string.app_default_video_name)))
                {
                    deleteVideoResource(info.getString(context.getString(R.string.multi_region_media_name_json_key)));
                }
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void deleteImageResource(String mediaName,boolean isSelfPath) throws Exception
    {

        String dir= new User().getUserPlayingFolderModePath(context)+context.getString(R.string.campaign_images_dir);
        if(isSelfPath)
        {
            dir= new User().getUserPlayingFolderModePath(context);
        }

        String imagePath = dir+File.separator+mediaName;
        File dirFile=new File(imagePath);
        if(dirFile.exists())
        {

            dirFile.delete();
        }
    }

    //delete video resource
    private void deleteVideoResource(String mediaName) throws Exception
    {

        String dir= new User().getUserPlayingFolderModePath(context);
        String imagePath = dir+File.separator+mediaName;
        File dirFile=new File(imagePath);
        if(dirFile.exists())
        {

            dirFile.delete();
        }
    }


    //add video region
    private HashMap<String,Object> addVideoRegion(final JSONObject info) throws JSONException
    {
        HashMap<String,Object> multiRegProperties = new HashMap<>(1);

        String dir= new User().getUserPlayingFolderModePath(activity);
        String imagePath = dir+"/"+info.getString(activity.getString(R.string.multi_region_media_name_json_key));
        File dirFile=new File(imagePath);

        if(dirFile.exists())
        {
            RelativeLayout.LayoutParams parentLayoutParams = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_width_json_key))),
                    calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_height_json_key))));


            RelativeLayout videoParentLayout = new RelativeLayout(context);
            videoParentLayout.setLayoutParams(parentLayoutParams);

            //adding margins
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) videoParentLayout.getLayoutParams();
            marginLayoutParams.leftMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_left_margin_json_key)));
            marginLayoutParams.rightMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_right_margin_json_key)));
            marginLayoutParams.topMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_top_margin_json_key)));
            marginLayoutParams.bottomMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_bottom_margin_json_key)));
            parentLayout.addView(videoParentLayout);

            final VideoView videoView = new VideoView(context);
             boolean isStretch = true;
           boolean isRequestFocus = false;

            if(info.has(context.getString(R.string.multi_region_properties_json_key))) {
               final JSONObject properties = info.getJSONObject(context.getString(R.string.multi_region_properties_json_key));
                isStretch = properties.getBoolean("isStretch");
                if(properties.getInt("volume")>=1)
                {
                    multiRegProperties.put(context.getString(R.string.has_video_with_sound_multi_reg_prop),true);
                }
                isRequestFocus = true;

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                        try {
                            mediaPlayer.start();
                            mediaPlayer.setLooping(true);
                            mediaPlayer.setVolume(properties.getInt("volume"),properties.getInt("volume"));
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });


            }

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

            }


            videoParentLayout.addView(videoView);
            videoView.setVideoPath(dirFile.getAbsolutePath());
            if(isRequestFocus) {
                videoView.requestFocus();
            }else
            {
                videoView.start();
            }





        }

        return multiRegProperties;

    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(activity instanceof DisplayLocalFolderAds)
            {
                DisplayLocalFolderAds ads = (DisplayLocalFolderAds)activity;
                return ads.mDetector.onTouchEvent(motionEvent);
            }else
            {
                return false;
            }

        }
    };

    //add url region
    private void addURLRegion(JSONObject info) throws JSONException
    {
        String url = info.getString(context.getString(R.string.multi_region_media_name_json_key));
        boolean isopenWithThirdPartyApp = false;
        String[] browserURLs = context.getResources().getStringArray(R.array.def_browser_url);
        for (String browserURL : browserURLs) {
            if (url.contains(browserURL)) {
                isopenWithThirdPartyApp = true;
                break;
            }
        }

        if (isopenWithThirdPartyApp) {
            if(activity instanceof DisplayLocalFolderAds)
            {
                DisplayLocalFolderAds act = (DisplayLocalFolderAds)activity;
                act.openWithThirdPartyApp(url);
            }

        }else {
            RelativeLayout.LayoutParams parentLayoutParams = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_width_json_key))),
                    calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_height_json_key))));

            final WebView web = new WebView(context);
            web.setOnTouchListener(touchListener);
            web.setLayoutParams(parentLayoutParams);

            //adding margins
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) web.getLayoutParams();
            marginLayoutParams.leftMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_left_margin_json_key)));
            marginLayoutParams.rightMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_right_margin_json_key)));
            marginLayoutParams.topMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_top_margin_json_key)));
            marginLayoutParams.bottomMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_bottom_margin_json_key)));
            parentLayout.addView(web);
            //initialize web view
            // web settings
            initWebView(web);

            web.loadUrl(url);
        }
    }



    private void initWebView(WebView web)
    {
        // web settings
        web.setId(random.nextInt((100 - 1) + 1) + 1);
        WebSettings webSettings = web.getSettings();

        // false
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setAppCacheEnabled(true);

        // true
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowContentAccess(true);

        // other
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setPluginState(WebSettings.PluginState.ON);

        //allow third pary cookies
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptFileSchemeCookies(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            //setAllowFileAccessFromFileURLs
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);

        }

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setDomStorageEnabled(true);


        web.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {

                //updateWebSiteLoadingPercentage(progress);

            }

        });

        web.setWebViewClient(new WebViewClient() {

            boolean isLoadingError = false;

            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                try {
                    //checkAndDismissWebViewProgressBar();

                    isLoadingError = true;
                    if(view!=null && description.equalsIgnoreCase("net::err_address_unreachable") ||
                            description.equalsIgnoreCase("net::err_name_not_resolved")||
                            description.equalsIgnoreCase("net::err_connection_reset")||
                            description.equalsIgnoreCase("net::err_timed_out"))
                    {
                        view.loadUrl(failingUrl);
                    }
                    //if there’s an error loading the page, make a toast
                     Log.d("MultiReg","Inside display web view failure errorcode"+errorCode+"description - "+description);
                    // Toast.makeText(mContext, description + ".", Toast.LENGTH_SHORT.show();

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Validations.isValidURL(url)) {
                    view.loadUrl(url);
                    return true;
                } else {
                    return false;
                }

            }

            public void onPageFinished(WebView view, String url) {

                try {
                    //once the page loading finished remove the listeners
                    //web.setWebViewClient(null);
                    //web.setWebChromeClient(null);

                    // Log.i("Process files", "onPageFinished -- "+url);
                    //checkAndDismissWebViewProgressBar();

                    //check and start auto scroll settings
                    if (view != null) {
                        if(activity instanceof  DisplayLocalFolderAds)
                        {
                            DisplayLocalFolderAds displayLocalFolderAds = (DisplayLocalFolderAds)activity;
                            displayLocalFolderAds.checkAndAutoScrollSettings(view.getId(), url);
                        }

                    }

                /*    if (isLoadingError) {

                        //webViewDisplayListeners(Constants.PLAY_LOAD_URL_FAIL_DURATION);
                    } else {

                       // webViewDisplayListeners(getMediaDuration(mediaInfo));
                    }*/

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);

                //check and remove auto scrolls for webview
                if(activity instanceof  DisplayLocalFolderAds)
                {
                    DisplayLocalFolderAds displayLocalFolderAds = (DisplayLocalFolderAds)activity;
                    displayLocalFolderAds.removeAutoScrollSettingsForWebView(view.getId());
                }


                //startWebSideLoading();

            }

        });
    }

    //add file multi region
    private synchronized void addFileRegion(final JSONObject info) throws JSONException
    {
        String dir= new User().getUserPlayingFolderModePath(activity);
        String imagePath = dir+"/"+info.getString(activity.getString(R.string.multi_region_media_name_json_key));
        final File dirFile=new File(imagePath);

        if(dirFile.exists())
        {
            final Uri uri = Uri.fromFile(dirFile);

            // Check what kind of file you are trying to open, by comparing the url with extensions.
            // When the if condition is matched, plugin sets the correct intent (mime) type,
            // so Android knew what application to use to open the file

            if(uri.toString().contains(context.getString(R.string.media_file_pdf)))//For PDF file
            {
                Log.i("Process Files","file  Found- pdf");

                // Do something for lollipop and above versions
                //PDF file

                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_width_json_key))),
                        calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_height_json_key))));

                // RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


                final PDFView pdfView = new PDFView(activity,null);
                pdfView.setLayoutParams(params1);




                //SCROLLBAR TO ENABLE SCROLLING
                //final ScrollBar scrollBar = new ScrollBar(activity);
                //scrollBar.setLayoutParams(params1);

                //adding margins
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) pdfView.getLayoutParams();
                marginLayoutParams.leftMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_left_margin_json_key)));
                marginLayoutParams.rightMargin = calculateRequiredPixel(deviceInfo.get("width"), info.getInt(activity.getString(R.string.multi_region_right_margin_json_key)));
                marginLayoutParams.topMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_top_margin_json_key)));
                marginLayoutParams.bottomMargin = calculateRequiredPixel(deviceInfo.get("height"), info.getInt(activity.getString(R.string.multi_region_bottom_margin_json_key)));


                if(dirFile.canRead())
                {
                    pdfView.recycle();
                    pdfView.invalidate();

                    //LOAD IT
                    pdfView

                            .fromFile(dirFile)
                            .onError(new OnErrorListener() {
                                @Override
                                public void onError(Throwable t) {

                                }
                            })
                            .autoSpacing(true)
                            .pageFitPolicy(FitPolicy.WIDTH)
                            .enableAnnotationRendering(true)
                            .defaultPage(0)
                            .onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int nbPages)
                                {
                                    if(pdfView!=null && pdfView.getVisibility() == View.VISIBLE)
                                    {

                                        //pdfView.setSwipeVertical(true);
                                        //pdfView.documentFitsView();
                                        //pdfView.zoomTo(PDFView.DEFAULT_MIN_SCALE);
                                        //pdfView.fitToWidth();
                                        //setPDFZoomLevel(info,pdfView);
                                        //pdfView.fitToWidth(pdfView.getCurrentPage());

                                        pdfView.loadPages();


                                    }

                                }
                            })
                            .onRender(new OnRenderListener() {
                                @Override
                                public void onInitiallyRendered(int nbPages) {
                                    //pdfView.enableAntialiasing(true);

                                    pdfView.setKeepScreenOn(true);
                                    pdfView.setVerticalScrollBarEnabled(true);
                                    setPDFZoomLevel(info,pdfView);
                                    setPdfScrollMode(info,pdfView);
                                    //pdfScrollMode(info,pdfView);
                                }
                            })
                            .enableSwipe(false)
                            .enableDoubletap(false)
                            .onPageChange(new OnPageChangeListener() {
                                @Override
                                public void onPageChanged(int page, int pageCount)
                                {
                                    //pdfView.fitToWidth();
                                    //pdfView.fitToWidth(page);
                                }
                            })
                            .load();





                }else
                {
                    Toast.makeText(context, "Unable to load the file", Toast.LENGTH_SHORT).show();
                }

                pdfView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {
                        //gesture detector to detect swipe.
                        ((DisplayLocalFolderAds)activity).mDetector.onTouchEvent(arg1);
                        return true;//always return true to consume event
                    }
                });

                parentLayout.addView(pdfView);

            }else {

                Toast.makeText(context, "Unsupported file format.", Toast.LENGTH_SHORT).show();

            }
        }else
        {
            //invalid file , try play next media
            //resource not found
            Log.i("Process Files","File not found -"+dirFile.getPath());
        }

    }

    private void setPDFZoomLevel(final JSONObject jsonObject,PDFView pdfView)
    {
        try
        {
            if(jsonObject.has("properties"))
            {
                // PointF pointF = new PointF(parentLayout.getPivotX()/2, parentLayout.getPivotY()/2);

                JSONObject propertiesJson=jsonObject.getJSONObject("properties");


                if(propertiesJson.has("isFitToScreen"))
                {
                    boolean isFitToScreen=propertiesJson.getBoolean("isFitToScreen");

                    if(isFitToScreen)
                    {
                        pdfView.fitToWidth(pdfView.getCurrentPage());

                    }else
                    {
                        //pdfView.resetZoom();
                        //pdfView.zoomCenteredTo(Float.parseFloat(propertiesJson.getString("zoomLevel")),pointF);
                        pdfView.zoomTo(Float.parseFloat(propertiesJson.getString("zoomLevel")));
                    }

                }else
                {
                    // pdfView.resetZoom();
                    // pdfView.zoomCenteredTo(Float.parseFloat(propertiesJson.getString("zoomLevel")),pointF);
                    pdfView.zoomTo(Float.parseFloat(propertiesJson.getString("zoomLevel")));
                }

            }else
            {
                //pdfView.zoomTo(PDFView.DEFAULT_MID_SCALE);
                pdfView.fitToWidth(pdfView.getCurrentPage());
            }

        }catch (JSONException e)
        {
            // pdfView.zoomTo(PDFView.DEFAULT_MID_SCALE);
            pdfView.fitToWidth(pdfView.getCurrentPage());
            e.printStackTrace();
        }

    }

    private void setPdfScrollMode(final JSONObject jsonObject,final PDFView pdfView)
    {
        ((DisplayLocalFolderAds)activity).pdfScrollHandler = new Handler();
        ((DisplayLocalFolderAds)activity).pdfScrollHandlerRunnable = new Runnable() {
            public void run()
            {

                if (pdfView != null && pdfView.getVisibility() == View.VISIBLE)
                {

                    long scrollSpeed;

                    try
                    {
                        if(jsonObject.has("properties"))
                        {
                            JSONObject propertiesJson=jsonObject.getJSONObject("properties");
                            int scrollTime=propertiesJson.getInt("scrollingSpeed");

                            if(scrollTime>0)
                            {
                                scrollSpeed=scrollTime*1000;
                            }else
                            {
                                scrollSpeed=DisplayLocalFolderAds.DEFAULT_PDF_SCROLL_SPEED;
                            }
                        }else
                        {
                            scrollSpeed=DisplayLocalFolderAds.DEFAULT_PDF_SCROLL_SPEED;

                        }


                    }catch (JSONException e)
                    {
                        e.printStackTrace();
                        scrollSpeed=DisplayLocalFolderAds.DEFAULT_PDF_SCROLL_SPEED;
                    }

                    int page = pdfView.getCurrentPage();
                    int count = pdfView.getPageCount();

                    if (page == (count - 1))
                    {
                        pdfView.jumpTo(0, true);
                        // pdfView.fitToWidth(page);

                    } else {
                        pdfView.jumpTo(page + 1, true);
                        // pdfView.fitToWidth(page);
                    }
                    ((DisplayLocalFolderAds) activity).pdfScrollHandler.postDelayed(this, scrollSpeed);
                }
                else
                {
                    ((DisplayLocalFolderAds)activity).pdfScrollHandler.removeCallbacks(this);
                }

            }
        };

        ((DisplayLocalFolderAds)activity).pdfScrollHandlerRunnable.run();
    }


}
