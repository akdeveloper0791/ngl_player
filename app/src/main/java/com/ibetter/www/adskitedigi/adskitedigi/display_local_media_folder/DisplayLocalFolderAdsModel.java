package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.ActionsDBHelper;
import com.ibetter.www.adskitedigi.adskitedigi.display_ads.ImageModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.DisplayDialog;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.overlay_image_settings.OverlayImageSettingsModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative.CustomInteractiveForm;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative.MonitorAppInvokeService;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative.UserURLActivityForm;
import com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings.ScrollTextSettingsModel;

import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vineeth_ibetter on 1/8/18.
 */

public class DisplayLocalFolderAdsModel
{
    private VideoView displayVideoView;
    private ImageView displayImageView,mediaPlayingStatusIc;
    private TextView scrollingTextTV,actionTextTV;
    private Context context;
    private DisplayDialog dialogModel;
    private ImageModel imageModel;
    private String scrollText;
    private Activity activity;
    private String actionText;
    protected static WebView displayURLView;

    private FrameLayout singleRegionParent;
    protected RelativeLayout multiRegionParent,overlayLayout;

    public VideoView getDisplayVideoView() {
        return displayVideoView;
    }

    public ImageView getDisplayImageView() {
        return displayImageView;
    }

    public void setDisplayImageView(ImageView displayImageView)
    {
        this.displayImageView = displayImageView;
    }

    public TextView getScrollingTextTV() {
        return scrollingTextTV;
    }

    public Context getContext() {
        return context;
    }


    public DisplayLocalFolderAdsModel(VideoView displayVideoView, ImageView displayImageView, TextView scrollingTextTV, Context context,Activity activity)
    {
        this.displayVideoView = displayVideoView;
        this.displayImageView = displayImageView;
        this.scrollingTextTV = scrollingTextTV;
        this.context = context;
        this.activity = activity;

        singleRegionParent = activity.findViewById(R.id.display_media_view_layout);
        multiRegionParent = activity.findViewById(R.id.dynamic_view_parent);
        displayURLView =  activity.findViewById(R.id.display_media_web_view);
        actionTextTV = activity.findViewById(R.id.action_scrolling_tv);
        mediaPlayingStatusIc = activity.findViewById(R.id.media_paying_status_ic);

        setScrollText(null);

        setOverlayImageToLayout();

    }

    private void setOverlayImageToLayout()
    {
        overlayLayout = activity.findViewById(R.id.overlaying_image_layout);


        OverlayImageSettingsModel model=new OverlayImageSettingsModel();

        if(model.getOverlayImageSettingStatus(context)) {//overLayImage=activity.findViewById(R.id.overlaying_image);

            int width=10,height=10;

            String POSITION=OverlayImageSettingsModel.RIGHT_TOP;


            String settingsInfo=model.getOverlayingImageSettingsInfo(context);

            try
            {
                JSONObject jsonObject=new JSONObject(settingsInfo);

                width=Constants.convertToInt( jsonObject.getString("width"));

                height=Constants.convertToInt( jsonObject.getString("height"));

                POSITION=jsonObject.getString("position");

            }catch (Exception E)
            {

            }

            HashMap<String, Integer> deviceInfo = new DeviceModel().getDeviceProperties(activity);

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(calculateRequiredPixel(deviceInfo.get("width"), width),
                    calculateRequiredPixel(deviceInfo.get("height"), height));

            //RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(requiredWidth,RelativeLayout.LayoutParams.MATCH_PARENT);

            switch (POSITION){
                case OverlayImageSettingsModel.RIGHT_TOP:
                    params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
                case OverlayImageSettingsModel.LEFT_BOTTOM:
                    params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    break;
                case OverlayImageSettingsModel.RIGHT_BOTTOM:
                    params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
                case OverlayImageSettingsModel.LEFT_TOP:
                    params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    break;
                default:
                    params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
            }

            ImageView image = new ImageView(activity);
            image.setLayoutParams(params1);
            image.setScaleType(ImageView.ScaleType.FIT_XY);

            image.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            String imagePath=model.getOverlayingImagePath(context);

            Log.i("imagePath",""+imagePath);
            File dirFile=null;

            try
            {
                dirFile = new File(imagePath);
            }catch (Exception E)
            {
                E.printStackTrace();
            }

            if(dirFile!=null)
            {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher);

                try {

                    requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true);

                    Glide.with(activity)
                            .load(Uri.fromFile(dirFile))
                            .apply(requestOptions)
                            .into(image);

                } catch (Exception E) {
                    E.printStackTrace();
                }

                image.setAdjustViewBounds(true);

                overlayLayout.addView(image);

                overlayLayout.setVisibility(View.VISIBLE);
            }

        }else
        {
            overlayLayout.setVisibility(View.GONE);
        }
    }


    private int calculateRequiredPixel(int orginalValue,int percentage)
    {
        return (int)Math.round(new Constants().getPercentageAmount((double)orginalValue,percentage));
    }

    public  void setScrollText(String text)
    {
        try
        {
            Log.i("scroll text",""+text);
            scrollText = text;
            if (new ScrollTextSettingsModel(context).isScrollTextOn())
            {
                //Log.i("isScrollTextOn","on ");
                switch (new User().getLocalScrollTextMode(context))
                {
                    case Constants.SCROLLING_CUSTOMISED_TEXT:

                        setProperties();
                        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(context);
                        String previousText = scrollingTextTV.getText().toString();
                        String newText = saveSP.getString(context.getString(R.string.local_scroll_text), context.getString(R.string.display_ads_layout_scrolling_text));

                        if (scrollingTextTV.getVisibility() != View.VISIBLE)
                        {
                            scrollingTextTV.setVisibility(View.VISIBLE);
                        }

                        if (!(previousText != null && previousText.equals(newText)))
                        {
                            scrollingTextTV.setText(newText);
                            scrollingTextTV.setSelected(true);
                            scrollingTextTV.setHorizontallyScrolling(true);
                            checkAndSetScrollTextAnim();
                        }

                        break;

                    case Constants.SCROLLING_MEDIA_NAME:

                        previousText = scrollingTextTV.getText().toString();
                        text = new ScrollTextSettingsModel(context).getMediaNameToScroll(text);

                        if (text != null) {

                            if (scrollingTextTV.getVisibility() != View.VISIBLE) {
                                scrollingTextTV.setVisibility(View.VISIBLE);
                            }

                            if (!previousText.equals(text)) {
                                scrollingTextTV.setText(new File(text).getName());
                                // checkAndSetScrollTextAnim();
                            }
                        } else {
                            scrollingTextTV.setText("");
                            scrollingTextTV.setVisibility(View.GONE);
                        }

                        break;

                    default:
                        scrollingTextTV.setVisibility(View.GONE);
                        break;
                }
            } else {
                scrollingTextTV.setVisibility(View.GONE);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            scrollingTextTV.setVisibility(View.GONE);
        }

    }

    protected void setProperties()
    {
        Log.i("text BG CLR",""+new User().getScrollTextBgColor(context));
        Log.i("text CLR",""+new User().getScrollTextTextColor(context));

        scrollingTextTV.setBackgroundColor(Color.parseColor(new User().getScrollTextBgColor(context)));
        //scrollingTextTV.setTextSize(new User().getScrollTextSize(context));
        scrollingTextTV.setTextColor(Color.parseColor(new User().getScrollTextTextColor(context)));

        boolean isBold=new User().isLocalScrollTextBold(context);
        boolean isItalic=new User().isLocalScrollTextItalic(context);


        if(isBold|| isItalic)
        {
            if(isBold&&isItalic)
            {
                scrollingTextTV.setTypeface(scrollingTextTV.getTypeface(), Typeface.BOLD_ITALIC);
            }else if(isBold)
            {
                scrollingTextTV.setTypeface(scrollingTextTV.getTypeface(), Typeface.BOLD);
            }else if(isItalic)
            {
                scrollingTextTV.setTypeface(scrollingTextTV.getTypeface(), Typeface.ITALIC);
            }
        }


    }

    public  void  setMediaDefaultScrollText(String text)
    {
        try {
            scrollText = text;

            if (new ScrollTextSettingsModel(context).isScrollTextOn())
            {

                if (scrollingTextTV.getVisibility() != View.VISIBLE) {
                    scrollingTextTV.setVisibility(View.VISIBLE);
                }

                //if the previous scrolling text is not equal to latest pushed scrolling text then only need to set new text to the  scrollingTextTV
                String previousText = scrollingTextTV.getText().toString();

                if (!(previousText != null && previousText.equals(scrollText)))
                {
                    scrollingTextTV.setText(scrollText);
                }
            }
            else
            {
                scrollingTextTV.setVisibility(View.GONE);
            }
        }
        catch(Exception e)
        {
            scrollingTextTV.setVisibility(View.GONE);
        }

    }

    protected File[] getLatestFiles(final  long lastUpdatedTime,final long currentTime)
    {

        String dir= new User().getUserPlayingFolderModePath(context);

        if(dir!=null)
        {
            File dirFile=new File(dir);

            File[] files = dirFile.listFiles(new FileFilter() {
                @Override

                public boolean accept(File pathname)
                {
                    String s=pathname.getName();
                    s=s.toLowerCase();

                    return  ((pathname.lastModified()>lastUpdatedTime && pathname.lastModified()<=currentTime) && (
                            s.endsWith(context.getString(R.string.media_video_wmv)) ||
                            s.endsWith(context.getString(R.string.media_video_avi)) ||
                            s.endsWith(context.getString(R.string.media_video_mpg)) ||
                            s.endsWith(context.getString(R.string.media_video_mpeg)) ||
                            s.endsWith(context.getString(R.string.media_video_webm)) ||
                            s.endsWith(context.getString(R.string.media_video_mp4))||
                            s.endsWith(context.getString(R.string.media_video_3gp))||
                            s.endsWith(context.getString(R.string.media_video_mkv))||
                            s.endsWith(context.getString(R.string.media_image_jpg)) ||
                            s.endsWith(context.getString(R.string.media_image_jpeg)) ||
                            s.endsWith(context.getString(R.string.media_image_png)) ||
                            s.endsWith(context.getString(R.string.media_image_bmp)) ||
                            s.endsWith(context.getString(R.string.media_image_gif))||
                            s.endsWith(context.getString(R.string.media_txt))
                    ) && !s.startsWith(context.getString(R.string.do_not_display_media)));

                }
            });


            //ascending order
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {

                    return file2.lastModified() > file1.lastModified() ? -1 : (file2.lastModified() < file1.lastModified()) ? 1 : 0;
                }
            });


            return files;
        }
        else
        {
            return null;
        }
    }

    protected File[] getAllFiles()
    {

        String dir= new User().getUserPlayingFolderModePath(context);

        if(dir!=null)
        {
            File dirFile=new File(dir);

            File[] files =   dirFile.listFiles(
                    new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s)
                        {

                            s=s.toLowerCase();

                            if((s.endsWith(context.getString(R.string.media_video_wmv)) ||
                                    s.endsWith(context.getString(R.string.media_video_avi)) ||
                                    s.endsWith(context.getString(R.string.media_video_mpg)) ||
                                    s.endsWith(context.getString(R.string.media_video_mpeg)) ||
                                    s.endsWith(context.getString(R.string.media_video_webm)) ||
                                    s.endsWith(context.getString(R.string.media_video_mp4))||
                                    s.endsWith(context.getString(R.string.media_video_3gp))||
                                    s.endsWith(context.getString(R.string.media_video_mkv))||
                                    s.endsWith(context.getString(R.string.media_image_jpg)) ||
                                    s.endsWith(context.getString(R.string.media_image_jpeg)) ||
                                    s.endsWith(context.getString(R.string.media_image_png)) ||
                                    s.endsWith(context.getString(R.string.media_image_bmp)) ||
                                    s.endsWith(context.getString(R.string.media_image_gif)) ||
                                    s.endsWith(context.getString(R.string.media_txt))
                            ) &&  (!s.startsWith(context.getString(R.string.do_not_display_media))))
                            {
                                return true;
                            }
                            else
                            {
                                return false;
                            }
                        }
                    }
            );

            try {
                //ascending order
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {

                        return file2.lastModified() > file1.lastModified() ? -1 : (file2.lastModified() < file1.lastModified()) ? 1 : 0;
                    }
                });
                return files;

            }catch (NullPointerException e)
            {
                return null;
            }

        }
        else
        {
            return null;
        }
    }


    //display video view
    public void displayVideoView()
    {

        dismissMultiRegion();

        if(displayURLView!=null) {
            dismissWebView();
        }

        if(singleRegionParent!=null && singleRegionParent.getVisibility() != (View.VISIBLE))
        {
            singleRegionParent.setVisibility(View.VISIBLE);
        }

        if(displayVideoView!=null&&displayImageView!=null)
        {
            displayVideoView.setVisibility(View.VISIBLE);
            displayImageView.setVisibility(View.GONE);
        }

    }


   protected void dismissSingleAndMultipleViews()
   {
       dismissMultiRegion();

       if(singleRegionParent!=null)
       {
           singleRegionParent.setVisibility(View.GONE);
       }
   }

    //dismiss web view
    private void dismissWebView()
    {
        displayURLView.setVisibility(View.GONE);

        activity.runOnUiThread(new Runnable() {
           @Override
           public void run() {
               displayURLView.setWebViewClient(null);
               displayURLView.setWebChromeClient(null);
               //displayURLView.setOnTouchListener(null);
           }
       });



    }

    //display image view
    public void displayImageView()
    {
        dismissMultiRegion();

        if(displayURLView!=null) {
            dismissWebView();
        }

        if(singleRegionParent!=null && singleRegionParent.getVisibility() != (View.VISIBLE))
        {
            singleRegionParent.setVisibility(View.VISIBLE);
        }


        if(displayVideoView!=null&&displayImageView!=null&&displayURLView!=null)
        {
            displayImageView.setVisibility(View.VISIBLE);
            displayVideoView.setVisibility(View.GONE);

        }
    }

    public void displayWebView()
    {
        dismissMultiRegion();

        if(displayURLView!=null) {
            dismissWebView();
        }

        if(singleRegionParent!=null && singleRegionParent.getVisibility() != (View.VISIBLE))
        {
            singleRegionParent.setVisibility(View.VISIBLE);
        }


        if(displayVideoView!=null&&displayImageView!=null&&displayURLView!=null)
        {

            displayURLView.setVisibility(View.VISIBLE);
            displayImageView.setVisibility(View.GONE);
            displayVideoView.setVisibility(View.GONE);

        }
    }

    protected boolean displayMultiRegion()
    {
        dismissMultiRegion();

        if(displayURLView!=null) {
            dismissWebView();
        }

        if(displayVideoView!=null&&displayImageView!=null&&displayURLView!=null&&multiRegionParent!=null
                )
        {


            displayImageView.setVisibility(View.GONE);
            displayVideoView.setVisibility(View.GONE);
            multiRegionParent.setVisibility(View.VISIBLE);

            return true;

        }else
        {
            return false;
        }


    }


    //dismiss multi region
    private void dismissMultiRegion()
    {
        if(multiRegionParent!=null && multiRegionParent.getVisibility()==View.VISIBLE )
        {
            multiRegionParent.removeAllViews();
            multiRegionParent.setVisibility(View.GONE);
        }
    }





    /* GET ImageModel model*/
    public ImageModel getImageModel()
    {
        if(imageModel==null)
        {
            imageModel=new ImageModel();
        }
        return  imageModel;

    }

    /* GET display ads Dialog model*/
    public DisplayDialog getDisplayAdsDialogModel()
    {
        if(dialogModel==null)
        {
            dialogModel=new DisplayDialog();
        }
        return  dialogModel;

    }

    //check and set scroll text animations
    protected void checkAndSetScrollTextAnim()
    {
        /*scrollingTextTV.measure(0, 0);       //must call measure!

        Display display = activity.getWindowManager().getDefaultDisplay();

        if(scrollingTextTV.getMeasuredWidth()<display.getWidth())
        {
            Animation scrollAnim = AnimationUtils.loadAnimation(context,R.anim.scroll_text_anim);
            scrollingTextTV.startAnimation(scrollAnim);

        }else
        {

            scrollingTextTV.clearAnimation();
        }*/


    }

    protected  String getAnnounceText(String mediaName,String announcementText)
    {


            if(mediaName!=null) {
                mediaName = new Constants().removeExtension(mediaName);
                String[] mediaInfo = mediaName.split(context.getString(R.string.file_name_seperator));

                announcementText = announcementText.toLowerCase();

                Pattern pattern = Pattern.compile("f\\w+");


                Matcher matcher = pattern.matcher(announcementText);

                while (matcher.find())
                {

                    String matcherString = matcher.group().toString();

                    matcherString = matcherString.substring(1, matcherString.length());


                    int stringPositionMatcher = Constants.convertToInt(matcherString);

                    if (stringPositionMatcher > 0)
                    {

                        if (mediaInfo.length >= stringPositionMatcher)
                        {
                            announcementText = announcementText.replace(matcher.group().toString(), mediaInfo[stringPositionMatcher - 1]);
                        }

                    }

                }

                return announcementText;

            }else
            {
                return announcementText;
            }


    }

    protected boolean isReport(String imgName)
    {
       if(imgName!=null) {

            imgName = imgName.toLowerCase();

            String report_prefix = (context.getString(R.string.reports_prefix).toLowerCase());


            return (imgName.startsWith(report_prefix));
        }else
        {
            return false;
        }
    }


    protected boolean isHideName(String imgName)
    {

        if(imgName!=null)
        {
            imgName = imgName.toLowerCase();

            String report_prefix = (context.getString(R.string.hide_file_name).toLowerCase());

            return (imgName.startsWith(report_prefix));
        }
        else
        {
            return false;
        }

    }


    //update customer interactive action scrolling text in layout
    public void updateCustomerActionText(String actionText)
    {

        try
        {
            displayActionScrollingText(actionText);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    //display  interactive actions layout latest scrolling text info
    public void displayActionScrollingText(String actionText)
    {

        try {

                if (new ActionModel().getDisplayActionScrollingTextState(context)) {
                    int tempId = new ActionModel().getActionTemplateId(context);
                    Cursor cursor = new ActionsDBHelper(context).getCustomerActionText(tempId);

                    StringBuilder stringBuilder = new StringBuilder();

                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                             actionText = cursor.getString(cursor.getColumnIndex(ActionsDBHelper.CUSTOMER_ACTION_TEXT));
                            if (actionText != null && actionText.length() > 1) {
                                if (stringBuilder != null && stringBuilder.length() > 0) {
                                    stringBuilder.append("\t\t\t\t\t\t");
                                    stringBuilder.append(actionText);

                                } else {
                                    stringBuilder.append(actionText);
                                }
                            }
                        } while (cursor.moveToNext());

                        if (stringBuilder != null && stringBuilder.length() > 0) {
                            if (actionTextTV.getVisibility() != View.VISIBLE) {
                                actionTextTV.setVisibility(View.VISIBLE);
                                actionTextTV.setSelected(true);
                            }

                            if (actionText != null) {
                                if (stringBuilder.toString() != null ) {
                                    actionTextTV.setText(stringBuilder.toString());
                                    actionText = actionTextTV.getText().toString();
                                }

                            } else {
                                actionTextTV.setText(stringBuilder.toString());
                                actionText = actionTextTV.getText().toString();
                            }

                        } else {
                            actionTextTV.setVisibility(View.GONE);
                        }

                    } else {

                        if(actionText!=null) {
                            if (actionTextTV.getVisibility() != View.VISIBLE) {
                                actionTextTV.setVisibility(View.VISIBLE);
                                actionTextTV.setSelected(true);
                            }
                            actionTextTV.setText(actionText);
                        }else
                        {
                            actionTextTV.setVisibility(View.GONE);
                        }
                    }
                } else {
                    actionTextTV.setVisibility(View.GONE);
                }


        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //display customized action layout in DisplayLocalFoloderAds Activity based on template ID from SM
    public void displayCustomerActionDialog(int actionTempId)
    {
        DisplayLocalFolderAds displayLocalFolderAds = (DisplayLocalFolderAds)activity;

        Log.i("ActionsDataJson","setupActionSettings displayCustomerActionDialog tempId:"+actionTempId);
        switch (actionTempId)
        {

            case 0:
               // displayCustomerQueueActionDialog(tempId);
                displayLocalFolderAds.isRelaunchAppOnStop = false;

                Intent startIntent=new Intent(context, CustomInteractiveForm.class);
                startIntent.putExtra("actionTempId",actionTempId);
                activity.startActivityForResult(startIntent,DisplayLocalFolderAds.INTERACTIVE_FEED_BACK_FORM);

                break;

            case 1:
                //display customer interactive Feedback Form
                //DisplayLocalFolderAds displayLocalFolderAds = (DisplayLocalFolderAds)activity;
                displayLocalFolderAds.isRelaunchAppOnStop = false;

                Intent intent=new Intent(context, UserURLActivityForm.class);
                //Log.i("ActionsDataJson","setupActionSettings getActionFormUrl:"+new ActionModel().getActionFormUrl(context));
                intent.putExtra("form_url",new ActionModel().getActionFormUrl(context));
                activity.startActivityForResult(intent,DisplayLocalFolderAds.INTERACTIVE_FEED_BACK_FORM);

                break;

            case 2:

                launchOtherApp(new ActionModel().getAppInvokePackageName(context));

                if(new ActionModel().getInactivityTimerFlag(context))
                {
                    DisplayLocalFolderAds activity1 = (DisplayLocalFolderAds) activity;
                    activity1.saveAppInvokeStatus(true);

                    if(ActionModel.checkAccessibilityService())
                    {
                        activity1.MonitorAppInvokeService();
                    }
                }
                break;
        }

    }
    protected void forceScrollTickerTV()
    {
        if(scrollingTextTV!=null && scrollingTextTV.getVisibility() == View.VISIBLE && scrollingTextTV.getText()!=null)
        {
            scrollingTextTV.requestFocus();
        }
    }

    protected void forceScrollActionTV()
    {
        if(actionTextTV!=null && actionTextTV.getVisibility() == View.VISIBLE && actionTextTV.getText()!=null)
        {
           //actionTextTV.requestFocus();
        }
    }

    //delete the customer info record from the local DB
    public void  updateCustomerActionStatus()
    {
        try
        {
            displayActionScrollingText(null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //can scroll urls
    public boolean canScrollURL(String url)
    {
        if(url!=null)
        {
            url = url.toLowerCase();

            String[] dontScrollURL = context.getResources().getStringArray(R.array.dont_scroll_url);
            for(String listedURL : dontScrollURL)
            {
                return !(url.contains(listedURL));
            }

            return true;

        }else
        {
            return false;
        }
    }

    //pause video playing
    protected void pauseVideoPlaying()
    {
        if(displayVideoView!=null && displayVideoView.isPlaying())
        {
            displayVideoView.pause();
        }
    }

    //pause video playing
    protected void resumeVideoPlaying()
    {
        if(displayVideoView!=null && displayVideoView.getVisibility()==View.VISIBLE)
        {
            displayVideoView.start();
        }
    }

    //check and pause webview
    protected void pauseWebView()
    {
        if(displayURLView!=null&& displayURLView.getVisibility()==View.VISIBLE)
        {
            displayURLView.onPause();
        }
    }

    //check and resume webview
    protected void resumeWebView()
    {
        if(displayURLView!=null && displayURLView.getVisibility() == View.VISIBLE)
        {
            displayURLView.onResume();
        }
    }

    protected void pauseMultiRegionUI()
    {
        if(multiRegionParent!=null && multiRegionParent.getVisibility()==View.VISIBLE )
        {
           for(int i=0;i<multiRegionParent.getChildCount();i++)
           {
               View view = multiRegionParent.getChildAt(i);
               if(view instanceof WebView)
               {
                   ((WebView) view).onPause();

               }else if(view instanceof RelativeLayout)
               {
                   RelativeLayout relativeLayout =(RelativeLayout) view;

                   for (int j=0;j<relativeLayout.getChildCount();j++)
                   {
                       View view1 = relativeLayout.getChildAt(j);

                       if(view1 instanceof VideoView)
                       {
                           ((VideoView) view1).pause();
                       }

                   }

               }
           }
        }
    }


    protected void resumeMultiRegionUI()
    {
        if(multiRegionParent!=null && multiRegionParent.getVisibility()==View.VISIBLE )
        {
            for(int i=0;i<multiRegionParent.getChildCount();i++)
            {
                View view = multiRegionParent.getChildAt(i);
                 if(view instanceof WebView)
                {
                    ((WebView) view).onResume();

                }else if(view instanceof RelativeLayout)
                 {
                     RelativeLayout relativeLayout =(RelativeLayout) view;
                     for (int j=0;j<relativeLayout.getChildCount();j++)
                     {
                         View view1 = relativeLayout.getChildAt(j);
                         if(view1 instanceof VideoView)
                         {
                             ((VideoView) view1).start();
                         }
                     }
                 }
            }
        }
    }

    //display media playing status icon
    protected void toggleDisplayMediaPlayingStatusIc(boolean isPlay)
    {
        if(mediaPlayingStatusIc!=null) {
            mediaPlayingStatusIc.setVisibility(View.VISIBLE);

            if (isPlay) {
                mediaPlayingStatusIc.setImageResource(R.drawable.ic_pause);
                Animation animation = AnimationUtils.loadAnimation(context,R.anim.media_play_anim);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mediaPlayingStatusIc.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mediaPlayingStatusIc.startAnimation(AnimationUtils.loadAnimation(context,R.anim.media_play_anim));

            }else
            {
                mediaPlayingStatusIc.setImageResource(R.drawable.ic_play);
                mediaPlayingStatusIc.startAnimation(AnimationUtils.loadAnimation(context,R.anim.media_pause_anim));
            }
        }
    }


    private void launchOtherApp(String appPackageName)
    {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
        if (mIntent != null)
        {
            try {
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(mIntent);
            } catch (ActivityNotFoundException err)
            {
                Toast.makeText(context, "No Application found with selected package name, Please download it from play store", Toast.LENGTH_SHORT).show();
                tryToOpenPlayStore(appPackageName);
            }
        }else
        {
            tryToOpenPlayStore(appPackageName);
        }
    }

    private void tryToOpenPlayStore(String appPackageName)
    {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe)
        {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

}
