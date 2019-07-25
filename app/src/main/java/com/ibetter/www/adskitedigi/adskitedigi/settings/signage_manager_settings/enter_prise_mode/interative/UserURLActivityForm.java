package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Permissions;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class UserURLActivityForm extends Activity
{
    private Context context;
    private WebView webView;
    private ProgressBar webLoadingProgressbar;

    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private Uri photoURI;// mCameraPhotoPath;

    private static final int INPUT_FILE_REQUEST_CODE=2000;
    private static final int FILE_CHOOSER_RESULT_CODE =2001;
    private static final int PERMISSIONS_REQUEST_ACTION=2002;
    private static final int EXTERNAL_STORAGE_PERMISSIONS_REQUEST=2003;

     private Handler disconnectHandler = new Handler();
     private Timer endScheduleTimer;
     private boolean timerFlag;
     private long inActiveDuration=0;
     private long eventTime=0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));
        super.onCreate(savedInstanceState);
        context= UserURLActivityForm.this;

        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_feedback_form_dialog);

        webView=findViewById(R.id.form_web_view);
        webLoadingProgressbar=findViewById(R.id.url_progress_bar);

        if(getIntent().getExtras()!=null)
        {
            String userFormURL=getIntent().getStringExtra("form_url");

            timerFlag=new ActionModel().getActionInactivityTimesStatus(context);
            inActiveDuration=(long)(new ActionModel().getActionInactivityTime(context)*1000);
            Log.i("OtherAppInvokeService","onCreate inActiveDuration:"+inActiveDuration);

            displayURLActivityForm (userFormURL);
            displayDoneBtnInfo();

            startInactivityTimer();

        }else
            {
            Toast.makeText(context, "Unable to display URL Activity Form, please set valid form to the screen.", Toast.LENGTH_SHORT).show();
            finish();
            }
     }


     private void startInactivityTimer()
     {
         if (timerFlag)
         {
             if (inActiveDuration > 0)
             {
                 endScheduleTimer = new Timer();
                 endScheduleTimer.schedule(new InActiveTimerTask(), inActiveDuration);
             }
         }
     }


    private class InActiveTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            // run on another thread
            disconnectHandler.post(new Runnable() {
                @Override
                public void run()
                {
                    //long eventTime= HandleKeyCommands.eventTime;
                    if(eventTime>0)
                    {
                        long currentTime= Calendar.getInstance().getTimeInMillis();
                        if(currentTime>eventTime)
                        {
                            long diff=currentTime-eventTime;
                            // Log.i("OtherAppInvokeService","diff:"+diff);
                            long extraDuration=inActiveDuration-diff;

                            Log.i("OtherAppInvokeService","extraDuration:"+extraDuration);
                            if(extraDuration>0)
                            {
                                stopTimer();
                                endScheduleTimer=  new Timer();
                                endScheduleTimer.schedule(new InActiveTimerTask(), extraDuration);
                            }else
                            {
                                finish();
                            }

                        }else if(currentTime==eventTime)
                        {
                            stopTimer();
                            endScheduleTimer=  new Timer();
                            endScheduleTimer.schedule(new InActiveTimerTask(), inActiveDuration);
                            Log.i("OtherAppInvokeService","restart InActiveTimerTask:"+inActiveDuration);
                        }

                    }else
                    {
                          Log.i("OtherAppInvokeService","restartApp eventTime:"+eventTime);
                          //restart signage player
                          finish();
                    }
                }

            });

        }
    }

    private void stopTimer()
    {
        if(endScheduleTimer!=null)
        {
            eventTime=0;
            endScheduleTimer.cancel();
            endScheduleTimer.purge();
        }
    }

    @Override
    public void onUserInteraction()
    {
        if(timerFlag)
        {
            eventTime= Calendar.getInstance().getTimeInMillis();
            Log.i("disconnectHandler","onUserInteraction:eventTime"+eventTime);
        }
    }


    private void displayURLActivityForm(String feedbackFormUrl)
    {
        WebSettings webSettings = webView.getSettings();
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        //false
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);


        // true
       //webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setSupportZoom(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        // webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowContentAccess(true);

        //webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setPluginState(WebSettings.PluginState.ON);

        //allow third pary cookies
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptFileSchemeCookies(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            //setAllowFileAccessFromFileURLs
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
            webSettings.setAllowFileAccessFromFileURLs(true);
           // webSettings.setBuiltInZoomControls(true);
        }

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient()
        {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                try
                {
                   dismissWebViewProgressBar();
                    Toast.makeText(context, description, Toast.LENGTH_SHORT).show();

                }catch(IllegalArgumentException e)
                {
                    e.printStackTrace();
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.d("url","Inside should overriding url -"+url);
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {

                try {

                  //  Log.i("Process files", "onPageFinished -- "+url);
                    dismissWebViewProgressBar();

                    //enableDoneButton();

                }catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                    dismissWebViewProgressBar();
                }

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
                startWebViewProgressBar();

            }



        });


        webView.setWebChromeClient(new WebChromeClient()
        {
            public void onProgressChanged(WebView view, int progress)
            {
                updateWebSiteLoadingPercentage(progress);
            }


            // For Android 5.0
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, FileChooserParams fileChooserParams) {

                if (Permissions.checkMultiplePermission(context, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    // Double check that we don't have any existing callbacks
                    if (mFilePathCallback != null) {
                        mFilePathCallback.onReceiveValue(null);
                    }
                    mFilePathCallback = filePath;

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager())!= null)
                    {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();

                        } catch (Exception ex) {
                            // Error occurred while creating the File
                            Log.e("Exception", "Unable to create Image File", ex);
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null)
                        {
                            photoURI = FileProvider.getUriForFile(context, "www.signagemanager.fileProvider", photoFile);
                            //mCameraPhotoPath = photoURI.getPath();
                            takePictureIntent.putExtra("PhotoPath", photoURI.getPath());
                            Log.i("ACTION_IMAGE_CAPTURE","ACTION_IMAGE_CAPTURE path:"+photoURI.getPath());
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        } else {
                            takePictureIntent = null;
                        }
                    }

                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("*/*");
                    String[] mimetypes = {"image/*", "video/*","application/pdf","audio/*"};
                    contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    //contentSelectionIntent.setType("image/*");

                    Intent[] intentArray;
                    if (takePictureIntent != null)
                    {
                        intentArray = new Intent[]{takePictureIntent};

                    } else
                    {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
                    return true;
                }else
                {
                    Permissions.requestMultiplePermission(UserURLActivityForm.this,Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,PERMISSIONS_REQUEST_ACTION);
                    return false;
                }
            }

        });


        webView.setDownloadListener(new DownloadListener()
        {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength)
            {
                if(Permissions.checkSinglePermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType);

                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//Notify client once download is completed!
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));

                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();//To notify the Client that the file is being downloaded

                }else
                {
                    Permissions.requestSinglePermission(UserURLActivityForm.this,Manifest.permission.WRITE_EXTERNAL_STORAGE,EXTERNAL_STORAGE_PERMISSIONS_REQUEST);
                }

            }});


        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if(timerFlag)
                {
                    eventTime= Calendar.getInstance().getTimeInMillis();
                    Log.i("OtherAppInvokeService","setOnTouchListener:eventTime"+eventTime);
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    showDoneBtn();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    showDoneBtn();
                }

               return false;
            }
        });


        webView.loadUrl(feedbackFormUrl);

    }

    private void updateWebSiteLoadingPercentage(int percentage)
    {
        try
        {
            if(webLoadingProgressbar!=null && webLoadingProgressbar.getVisibility()==View.VISIBLE)
            {
                webLoadingProgressbar.setIndeterminate(false);

                String colorCode = "#33691E";
                if(percentage<=10)
                {
                    colorCode = "#DCEDC8";
                }else if(percentage<=20)
                {
                    colorCode = "#C5E1A5";
                }else if(percentage<=30)
                {
                    colorCode = "#AED581";
                }else if(percentage<=40)
                {
                    colorCode = "#9CCC65";
                }else if(percentage<=50)
                {
                    colorCode = "#8BC34A";
                }else if(percentage<=60)
                {
                    colorCode = "#7CB342";
                }else if(percentage<=80)
                {
                    colorCode = "#689F38";
                }else if(percentage<=90)
                {
                    colorCode = "#558B2F";
                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webLoadingProgressbar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(colorCode)));
                }else
                {
                    Drawable progressDrawable = webLoadingProgressbar.getProgressDrawable().mutate();
                    progressDrawable.setColorFilter(Color.parseColor(colorCode), android.graphics.PorterDuff.Mode.MULTIPLY);
                    webLoadingProgressbar.setProgressDrawable(progressDrawable);

                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    webLoadingProgressbar.setProgress(percentage, true);
                }else
                {
                    webLoadingProgressbar.setProgress(percentage);
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private File createImageFile()
    {
        String state = Environment.getExternalStorageState();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        File directory;
        if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            directory =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
        else {
            directory = context.getDir("Pictures", Context.MODE_PRIVATE);
        }
        if(!directory.exists() && !directory.mkdirs()){
            Log.e("ImageSaver","Error creating directory " + directory);
        }

        return new File(directory, imageFileName);
    }


    // Return here when file selected from camera or from SDcard

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (photoURI != null)
                    {
                        results = new Uri[]{photoURI};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            if (requestCode != FILE_CHOOSER_RESULT_CODE || mUploadMessage == null) {

                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILE_CHOOSER_RESULT_CODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode)
        {
            case EXTERNAL_STORAGE_PERMISSIONS_REQUEST:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(context, " Permission Granted...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(context,permissions[0]+" Permission Denied",Toast.LENGTH_SHORT).show();
                }

                break;


            case PERMISSIONS_REQUEST_ACTION:

                if (grantResults.length > 0)
                {
                    boolean permission1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permission2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permission1 && permission2)
                    {
                        Toast.makeText(context, "Permissions Granted...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context,permissions[0]+","+permissions[1]+"Permissions Denied",Toast.LENGTH_SHORT).show();
                    }
                }

                break;

        }
    }


    private void showDoneBtn()
    {
       CardView doneLayout=findViewById(R.id.done_layout);
       doneLayout.setVisibility(View.VISIBLE);

        Button doneBtn=findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

    }

    private void displayDoneBtnInfo()
    {
        String str = "\ud83d\udc49" + getString(R.string.customer_feed_back_form_alert_string);
        ((TextView) findViewById(R.id.done_tv)).setText(Html.fromHtml(str));
    }


    private void dismissWebViewProgressBar()
    {
        if(webLoadingProgressbar!=null)
        {
            webLoadingProgressbar.setIndeterminate(false);
            webLoadingProgressbar.setVisibility(View.GONE);
            (findViewById(R.id.progress_tv)).setVisibility(View.GONE);
        }

    }

    private void startWebViewProgressBar()
    {
        if(webLoadingProgressbar!=null)
        {
            webLoadingProgressbar.setVisibility(View.VISIBLE);
            webLoadingProgressbar.setIndeterminate(true);
            (findViewById(R.id.progress_tv)).setVisibility(View.VISIBLE);

        }
    }


    @Override
    public void onRestart()
    {
        super.onRestart();
        Log.i("OtherAppInvokeService","onRestart:startInactivityTimer");
        //stopTimer();
        startInactivityTimer();
    }

    @Override
    public void onStop()
    {
        super.onStop();
       Log.i("OtherAppInvokeService","onStop:stopDisconnectTimer");
        stopTimer();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i("OtherAppInvokeService","onDestroy:stopDisconnectTimer");
        stopTimer();
    }

}
