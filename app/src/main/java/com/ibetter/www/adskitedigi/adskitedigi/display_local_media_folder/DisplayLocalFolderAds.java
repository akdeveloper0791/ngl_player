package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.ibetter.www.adskitedigi.adskitedigi.DisplayAdsBase;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.accessibility.HandleKeyCommandsUpdateReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.bg_audio.BackGroundAudioHandler;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignReportsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.receiver.ActionReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;
import com.ibetter.www.adskitedigi.adskitedigi.fcm.SoftIotFCMReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.iot_devices.IOTDevice;
import com.ibetter.www.adskitedigi.adskitedigi.logs.DisplayDebugLogs;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.UploadMetricsFileService;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DateTimeModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.NetworkModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.model.Utility;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;
import com.ibetter.www.adskitedigi.adskitedigi.multi_region.MultiRegionSupport;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionModel;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionService;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.announcement_settings.AnnouncementSettingsConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings.AudioSettingsConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative.MonitorAppInvokeService;
import com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings.SetBootTimeForMediaSettingsConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.url_settings.URLSettingsAct;
import com.ibetter.www.adskitedigi.adskitedigi.settings.user_channel_guide.UserGuideActivity;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.CONTINUOUS_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.LOCAL_SCHEDULE_DATE_FORMAT;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.LOCAL_SCHEDULE_ONLY_DATE_FORMAT;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.LOCAL_SCHEDULE_TIME_FORMAT;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.SCHEDULE_DAILY_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.SCHEDULE_HOUR_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.SCHEDULE_MINUTE_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.SCHEDULE_MONTHLY_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.SCHEDULE_WEEKLY_PLAY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.ScheduleCampaignModel.SCHEDULE_YEARLY_PLAY;

/**
 * Created by vineeth_ibetter on 1/8/18.
*/


public class DisplayLocalFolderAds extends DisplayAdsBase implements View.OnClickListener,
       TextToSpeech.OnInitListener, MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener, CameraDialog.CameraDialogParent, CameraViewInterface.Callback{

    private Context context;
    private DisplayLocalFolderAdsModel displayLocalFolderAdsModel;
    private Timer imageScheduleTimer, fileObsereverTimer;
    protected int prevPosition = 0;
    private int stopPosition;
    public GestureDetector mDetector;
    private RelativeLayout addLocalScheduleLayout;
    public MediaInfo mediaInfo;
    public static boolean isServiceRunning = false;
    private boolean isPriorityTaskPlaying = false;


    private TextToSpeech announcement;
    private int announcementTimes;
    private long announcementDuration;
    private String announcementString;

    protected Vector<MediaInfo> processingFiles = new Vector<>();
    protected Vector<Long> tempDeletedCampaigns = new Vector<>();
    protected Vector<MediaInfo> priorityList = new Vector<>();
    protected Vector<MediaInfo> prioritySchedules = new Vector();

    private UpdatesFromReceiver updatesFromReceiver;
    public static String SM_UPDATES_INTENT_ACTION = "com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds.UpdatesFromReceiver";
    private static final int SM_ANNOUNCEMENT = 1;
    protected static final int INTERACTIVE_FEED_BACK_FORM = 2;
    private static final int TTS_ACTION = 3;
    private final static int REQUEST_CHECK_SETTINGS = 4;

    private static boolean isFromAnnouncement = false;
    private MediaPlayer mediaPlayer;
    //init on oncreate method
    private BackGroundAudioHandler backGroundAudioHandler;

    private ProgressBar webLoadingProgressbar;

    private SimpleArrayMap<Integer, Timer> webViewAutoScrollTimers = new SimpleArrayMap<>();

    public static ActionReceiver actionReceiver;

    public Handler pdfScrollHandler;

    public Runnable pdfScrollHandlerRunnable;
    public static final long DEFAULT_PDF_SCROLL_SPEED = 10000;

    protected boolean isAllMediasAreSkipped = true;

    // @BindView(R.id.camera_view)
    public View mTextureView;
    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private boolean isRequest;
    private boolean isPreview;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 200;
    private FaceDetector detector;
    private String imgPath;
    private Handler disconnectHandler = new Handler();

    private SoftIotFCMReceiver softIotFCMReceiver;

    public static boolean isSkipContinuousPlay = false;

    protected Timer rssFeedsTimer;
    protected Vector<Long> runningFeeds = new Vector<>(5);

    private boolean isActivityRestarted = false;
    //flag indicates whether the campaign playing is taken from application context or not
    public boolean isResumePlaying = false;



    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener()
    {
        @Override
        public void onAttachDev(UsbDevice device)
        {
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0)
            {
                // showShortMsg("No usb camera is connected...");
                return;
            }
            // request open permission
            if (!isRequest)
            {
                isRequest = true;
                if (mCameraHelper != null)
                {
                    mCameraHelper.requestPermission(0);
                    // new DisplayDebugLogs(context).execute("\nonAttachDev:requestPermission");
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest)
            {
                // new DisplayDebugLogs(context).execute("\nonDettachDev:isRequest"+isRequest);
                isRequest = false;
                if ( mCameraHelper != null)
                {
                    mCameraHelper.closeCamera();
                    // new DisplayDebugLogs(context).execute("\nonDettachDev:closeCamera"+isRequest);
                }

                showShortMsg("USB device is detached...");

            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected)
        {
            if (!isConnected)
            {
                showShortMsg("USB connection failed...");
                isPreview = false;

            } else {

                isPreview = true;

                //new DisplayDebugLogs(context).execute("\nonConnectDev:isPreview"+isPreview);
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(new Runnable()
                {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if (mCameraHelper != null && mCameraHelper.isCameraOpened())
                        {
                            // new DisplayDebugLogs(context).execute("\nonConnectDev:MODE_BRIGHTNESS:2500sleep");

                            mCameraHelper.setModelValue(UVCCameraHelper.MODE_BRIGHTNESS, 60);
                            mCameraHelper.setModelValue(UVCCameraHelper.MODE_CONTRAST, 60);
                            // capturingFrame();
                            reCapturingFrame();
                            //mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                            //mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                        }
                        Looper.loop();
                    }
                }).start();
            }

        }

        @Override
        public void onDisConnectDev(UsbDevice device)
        {
            showShortMsg("disconnecting");
            if (new User().isMetricsOn(context)&& new User().isExternalCamType(context)&& User.isPlayerRegistered(context))
            {
                try{
                    if (mCameraHelper != null)
                    {
                        DisplayLocalFolderAds.this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mTextureView.setVisibility(View.GONE);
                            }
                        });
                        //mCameraHelper.unregisterUSB();
                        mCameraHelper.closeCamera();
                        mCameraHelper.release();
                        imgPath = null;
                        //new DisplayDebugLogs(context).execute("\nonDisConnectDev:clear mCameraHelper");
                    }

                }catch (Exception e)
                {
                    new DisplayDebugLogs(context).execute("\nonDisConnectDev:Exception"+e.toString());
                }

            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        isServiceRunning = true;
        isActivityRestarted = true;

        context = DisplayLocalFolderAds.this;

        backGroundAudioHandler = new BackGroundAudioHandler(DisplayLocalFolderAds.this, false);

        setContentView(R.layout.display_local_folder_media);

        displayLocalFolderAdsModel = new DisplayLocalFolderAdsModel((VideoView) findViewById(R.id.display_media_video_view), (ImageView) findViewById(R.id.display_media_image_view), (TextView) findViewById(R.id.display_ad_scrolling_tv), DisplayLocalFolderAds.this, DisplayLocalFolderAds.this);

        addLocalScheduleLayout = findViewById(R.id.add_local_schedules_layout);
        webLoadingProgressbar = findViewById(R.id.url_progress_loading);

        mTextureView=findViewById(R.id.camera_view);

        mDetector = new GestureDetector(this, new MyGestureListener());

        registerUpdatesFromReceiver();

        setVideoViewListeners(displayLocalFolderAdsModel.getDisplayVideoView());

        playAds(true);

        callFileObserver();

        setAnnouncementSettings();

        userMetricsTask();

        Log.d("DisplaAds","Inside oncreate");
      //  checkAndEnableHotSpot();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        int userSelectedMode = ScreenOrientationModel.getSelectedScreenOrientation(this);
        if (userSelectedMode != newConfiguration.orientation) {
            setRequestedOrientation(userSelectedMode);
        }
        //Toast.makeText(context,"Inside configuration changed "+newConfiguration.orientation,Toast.LENGTH_SHORT).show();
    }

    private void setAnnouncementSettings() {
        SharedPreferences settingsModel = getSharedPreferences(getString(R.string.announcement_settings_sp), MODE_PRIVATE);

        if (settingsModel.getBoolean(getString(R.string.announcement_settings_announcement_status), AnnouncementSettingsConstants.DEFAULT_announcement_settings_announcement_status)) {

            //initialize text to speech engine
            initAnnouncementEngine();

            //init announcement settings
            initAnnouncementSettings();

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        try {
            if (event != null) {
                this.mDetector.onTouchEvent(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return super.onTouchEvent(event);
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //check and display Customer interactive action dialog
            if (new ActionModel().getDisplayActionLayoutState(context)) {
                int tempId = new ActionModel().getActionTemplateId(context);
                displayLocalFolderAdsModel.displayCustomerActionDialog(tempId);
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            //check and display add local schedule layout
            //displayAddLocalScheduleLayout();
            redirectToSettings();
            return true;
        }


    }

    private void redirectToSettings()
    {
        startActivity(new Intent(displayLocalFolderAdsModel.getContext(), UserGuideActivity.class));
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_ads_menu, menu);

        menu.findItem(R.id.schedules).setVisible(false);

        return true;

    }


    /**
     * On selecting action bar icons
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId())
        {

            case R.id.settings:
                isRelaunchAppOnStop = false;

                startActivity(new Intent(displayLocalFolderAdsModel.getContext(), UserGuideActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);


        }

    }


    protected void playAds(boolean isFirst) {

        if (!isPriorityTaskPlaying) {

            new PlayAds(DisplayLocalFolderAds.this).execute(isFirst);
        }

    }




    public void setVideoViewListeners(VideoView videoView)
    {

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                Toast.makeText(context, "Error in playing video " + what, Toast.LENGTH_SHORT).show();
                playNextAd();
                return true;

            }
        });


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //check and pause any background audio

                checkAndPauseMediaPlayer();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if (isServiceRunning) {
                    // Log.i("Videoview","inside on completion listener service running");
                    //check and resume background audio play
                    //isForceStart will be true only in video complete listener
                    checkAndResumeMediaPlayer(true);

                    if (isPriorityTaskPlaying) {
                        // Log.i("Videoview","inside on completion listener playing priority task ");
                        checkForNextPriorityTask(false);

                    } else {
                        // Log.i("Videoview","inside on completion listener play ads");
                        playAds(false);
                    }
                }

            }
        });
    }




    protected void displayAd(MediaInfo mediaInfo) {
        try {
            Log.i("mediaIn getMediaName()", mediaInfo.getMediaName());
            //check whehter is there any previous stopped campaign or not if exist play that
            if(checkAndResumePlayCampaign()) {
                return;
            }
            String mediaType = mediaInfo.getMediaType();

            if (mediaType != null) {
                if (mediaType.equalsIgnoreCase(getString(R.string.app_default_video_name))) {
                    setScrollTextMediaName(mediaInfo);
                    displayVideoView(mediaInfo.getPathname());
                } else if (mediaType.equalsIgnoreCase(getString(R.string.app_default_image_name))) {
                    setScrollTextMediaName(mediaInfo);
                    displayImageView(mediaInfo.getPathname());
                } else if (mediaType.equalsIgnoreCase(getString(R.string.app_default_txt_name))) {
                    // Log.i("processFiles","inside else if going to txt file");
                    processTxtFile(mediaInfo);
                }
            } else {
                displayImageView(mediaInfo.getPathname());
            }

            SignageServe.lasMediaPlayedPosition = prevPosition;
            SignageServe.saveLastMediaPlayedToSP(mediaInfo, prevPosition);


        } catch (Exception e) {
            e.printStackTrace();
            restartActivity();

        }

    }

    private void setScrollTextMediaName(MediaInfo mediaInfo) {
        if (mediaInfo != null) {
            if (displayLocalFolderAdsModel.isReport(mediaInfo.getMediaName()) || displayLocalFolderAdsModel.isHideName(mediaInfo.getMediaName())) {
                displayLocalFolderAdsModel.setScrollText(null);
            } else {
                displayLocalFolderAdsModel.setScrollText(mediaInfo.getPathname());
            }
        }
    }


    //display ImageView
    private void displayImageView(String mediaPath) {
        try {
            long imageDuration = getMediaDuration(mediaInfo);


          /*  if(displayLocalFolderAdsModel.isReport(new File(mediaPath).getName()))
            {
                imageDuration= new User().getReportImageDuration(displayLocalFolderAdsModel.getContext());
            }else
            {
                imageDuration= new User().getImageDuration(displayLocalFolderAdsModel.getContext());
            }*/


            if (displayLocalFolderAdsModel.getDisplayImageView() == null) {
                ImageView displayImageView = findViewById(R.id.display_media_image_view);

                displayLocalFolderAdsModel.setDisplayImageView(displayImageView);
            }

            displayLocalFolderAdsModel.getDisplayImageView().setImageBitmap(displayLocalFolderAdsModel.getImageModel().compressImage(mediaPath, displayLocalFolderAdsModel.getContext()));
            displayLocalFolderAdsModel.displayImageView();

            //set duration for timer
            if (isServiceRunning) {
                imageScheduleTimer = new Timer();

                imageScheduleTimer.schedule(new settingDurationForImageView(), imageDuration);


            }
        } catch (OutOfMemoryError exception) {
            //restart activity
            exception.printStackTrace();
            restartActivity();
        } catch (Exception e) {
            //restart activity
            e.printStackTrace();

            playNextAd();


        }


    }

    //display videoView
    private void displayVideoView(String mediaPath) {
        try {


            Uri uri = Uri.parse(mediaPath);
            VideoView mediaView = displayLocalFolderAdsModel.getDisplayVideoView();
            mediaView.setVideoURI(uri);
            mediaView.start();

            displayLocalFolderAdsModel.displayVideoView();

        } catch (OutOfMemoryError exception) {
            //restart activity
            exception.printStackTrace();
            restartActivity();
        } catch (Exception e) {
            e.printStackTrace();
            playNextAd();
        }
    }

    public boolean isOpenWithThirdPartyApp(String url) {
        String[] browserURLs = getResources().getStringArray(R.array.def_browser_url);
        for (String browserURL : browserURLs) {
            return (url.contains(browserURL));
        }

        return false;
    }
    //check whether to open url with default browser
    private void checkAndPlayURL(String url) {
        boolean isopenWithThirdPartyApp = isOpenWithThirdPartyApp(url);
        if (isopenWithThirdPartyApp) {
            openWithThirdPartyApp(url);
        } else {
            playURL(url);
        }
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


        } catch (ActivityNotFoundException e) {
            playURL(url);
        } catch (ArrayIndexOutOfBoundsException e) {
            //Invalid request ,, open with ss
            // Toast.makeText(context, "Invalid request", Toast.LENGTH_SHORT).show();
            playURL(url);
        } catch (Exception e) {
            e.printStackTrace();
            //error in playing display media , try play next media
            playNextAd();
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


    //play url
    private void playURL(String url) {
        try {
            //for test stop sm mode
            // testStopSMMode(url);

            displayLocalFolderAdsModel.displayWebView();


            //initialize web view listeners
            displayLocalFolderAdsModel.displayURLView = initializeWebView();

            // displayLocalFolderAdsModel.displayURLView.setOnTouchListener(this);

            // specify the url we want to load
            displayLocalFolderAdsModel.displayURLView.loadUrl(url);


        } catch (OutOfMemoryError exception) {
            //restart activity
            exception.printStackTrace();
            restartActivity();
        } catch (Exception e) {
            e.printStackTrace();
            //error in playing display media , try play next media
            playNextAd();
        }
    }


    private class settingDurationForImageView extends TimerTask {

        public void run() {
            try {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            //destroy the web view
                            destroyWebView();

                            playNextAd();


                        } catch (Exception e) {

                            e.printStackTrace();

                        }
                    }
                });


            } catch (Exception e) {

            }
        }
    }

    private synchronized void checkForNextPriorityTask(boolean isFirst) {
        priorityList.remove(0);//remove previously played video

        //play priority tasks
        playPriorityTask();

    }

    //setting default image
    protected void setDefaultImageView() {
        try {
            displayLocalFolderAdsModel.displayImageView();

            SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(displayLocalFolderAdsModel.getContext());
            String imagePath = sp.getString(getString(R.string.playing_default_image_path), null);

            if (imagePath != null) {
                File dirFile = new File(imagePath);
                if (dirFile.exists()) {

                    displayLocalFolderAdsModel.getDisplayImageView().setImageBitmap(displayLocalFolderAdsModel.getImageModel().compressImage(dirFile.getPath(), displayLocalFolderAdsModel.getContext()));

                }
            } else {
                displayLocalFolderAdsModel.getDisplayImageView().setImageResource(R.drawable.default_display_ad);

            }
        } catch (OutOfMemoryError e) {

        } catch (Exception e) {
            e.printStackTrace();
        }

        //set scrolling text to null
        displayLocalFolderAdsModel.setScrollText(null);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.close_add_local_schedules:
                dismissAddLocalScheduleLayout();
                break;

            case R.id.settings:
                isRelaunchAppOnStop = false;
                startActivity(new Intent(displayLocalFolderAdsModel.getContext(), UserGuideActivity.class));
                finish();
                break;


        }

    }

    private void dismissAddLocalScheduleLayout()
    {
        addLocalScheduleLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        startRenderRSSFeed();

        try
        {
            if (new User().isMetricsOn(context)&& new User().isExternalCamType(context)&& User.isPlayerRegistered(context))
            {
                if (mCameraHelper != null)
                {
                    // new DisplayDebugLogs(context).execute("\nonStart:registerUSB");
                    mCameraHelper.registerUSB();
                }
            }

        }catch (Exception e)
        {
            new DisplayDebugLogs(context).execute("\nonStart:Exception"+e.toString());
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();

        //initialize action receiver
        initActionReceiver();

        //initialize file obserserver
        setBgAudioFileObservers();

        checkAndResumeMediaPlayer(false);

        //save app status
        saveAppVisibleStatus(true);

        saveAppInvokeStatus(false);

        //register soft iot receiver
        registerSoftIotReceiver();

        try
        {
            if (new User().isMetricsOn(context)&& new User().isExternalCamType(context)&& User.isPlayerRegistered(context))
            {
                userMetricsTask();
                if (mCameraHelper != null)
                {
                    mCameraHelper.registerUSB();
                    // new DisplayDebugLogs(context).execute("\nonResume:registerUSB");
                }
            }

        }catch (Exception e)
        {
            new DisplayDebugLogs(context).execute("\nonResume:Exception"+e.toString());
        }

    }

    @Override
    public void onPause()
    {

        if(mediaInfo != null && mediaInfo.getSingleVideoRegId() == Constants.SINGLE_VIDEO_REGION_VIDEO_VIEW_ID) {
            VideoView currentVideoView = findViewById(Constants.SINGLE_VIDEO_REGION_VIDEO_VIEW_ID);
            if(currentVideoView != null && currentVideoView.getVisibility() == View.VISIBLE) {
                mediaInfo.setSingleVideoRegPausedAt(currentVideoView.getCurrentPosition());
            }
        }

        SignageServe.lasMediaPlayedPosition = prevPosition;
        SignageServe.saveLastMediaPlayedToSP(mediaInfo, prevPosition);




        removeActionReceiver();

        try {
            VideoView videoView = displayLocalFolderAdsModel.getDisplayVideoView();

            stopPosition = videoView.getCurrentPosition();

            //   Log.i(" onPause","in on Pause stopPosition::"+stopPosition +"  prevPosition  "+prevPosition);

            videoView.pause();

            //remove file observers
            removeBgAudioFileObservers();

            //check and pause media player
            checkAndPauseMediaPlayer();

            //save app status
            saveAppVisibleStatus(false);

            unregisterSoftIotReceiver();

            if (new User().isMetricsOn(context)&& new User().isExternalCamType(context)&& User.isPlayerRegistered(context))
            {

                if (mCameraHelper != null)
                {
                    if(disconnectHandler!=null)
                    {
                        disconnectHandler=null;
                    }
                    // mCameraHelper.unregisterUSB();
                    mCameraHelper.closeCamera();
                    mCameraHelper.release();
                    // new DisplayDebugLogs(context).execute("\nonPause:closeCamera&release");
                    imgPath = null;
                }
            }


        } catch (Exception e)
        {
          //  logError("onPause"+e.toString());
        }

        super.onPause();
    }


    public void onStop()
    {
        // Log.d("Info", "onStop ");
        checkAndReleaseMediaPlayer();
        //  Toast.makeText(displayLocalFolderAdsModel.getContext(),"Inside on stop method",Toast.LENGTH_SHORT).show();

        if (new User().isMetricsOn(context)&& new User().isExternalCamType(context)&& User.isPlayerRegistered(context))
        {

            if (mCameraHelper != null)
            {
                if(disconnectHandler!=null)
                {
                    disconnectHandler=null;
                }
                //mCameraHelper.unregisterUSB();
                mCameraHelper.closeCamera();
                mCameraHelper.release();
                new DisplayDebugLogs(context).execute("\nonStop:closeCamera&release");
                imgPath = null;
            }
        }

        if(rssFeedsTimer!=null)
        {
            rssFeedsTimer.cancel();
            rssFeedsTimer.purge();
            rssFeedsTimer=null;
        }

        super.onStop();
    }


    public void onRestart()
    {
        super.onRestart();
        saveAppInvokeStatus(false);

      try {
            if (!isFromAnnouncement)
            {
                if (imageScheduleTimer != null)
                {
                    imageScheduleTimer.purge();
                    imageScheduleTimer.cancel();
                }

                //Log.i(" onRestart", "stopPosition" + stopPosition + "::::in on onRestart" + " prevPosition " + prevPosition);

                //showInfoWhileRestarting();
                continuePlaying();

            } else {
                isFromAnnouncement = false;
            }

          if (new User().isMetricsOn(context)&& new User().isExternalCamType(context)&& User.isPlayerRegistered(context))
          {
              if (mCameraHelper != null)
              {
                  mCameraHelper.registerUSB();
                  new DisplayDebugLogs(context).execute("\nonRestart:registerUSB");
              }else
              {
                  mCameraHelper = UVCCameraHelper.getInstance();
                  new DisplayDebugLogs(context).execute("\nonRestart:mCameraHelper null getInstance");
              }
          }
        } catch (Exception e)
        {
            //logError("onRestart"+e.toString());
        }

    }




    private void continuePlaying() {
        if (mediaInfo != null && !mediaInfo.isMediaRepeating() && (new File(mediaInfo.getPathname())).exists()) {

            if (mediaInfo.getMediaType().equalsIgnoreCase(getString(R.string.app_default_image_name))) {
                imageScheduleTimer = new Timer();
                imageScheduleTimer.schedule(new settingDurationForImageView(), new User().getImageDuration(displayLocalFolderAdsModel.getContext()));
            } else if (mediaInfo.getMediaType().equalsIgnoreCase(getString(R.string.app_default_video_name))) {

                VideoView videoView = displayLocalFolderAdsModel.getDisplayVideoView();
                videoView.seekTo(stopPosition);
                videoView.start();

                /// check and pause audio player
                checkAndPauseMediaPlayer();
            } else if (mediaInfo.getMediaType().equalsIgnoreCase(getString(R.string.app_default_multi_region))) {

                processTxtFile(mediaInfo);
            } else if (mediaInfo.getMediaType().equalsIgnoreCase(getString(R.string.app_default_url_name))) {
                playNextAd();
            } else {
                processTxtFile(mediaInfo);
            }

        } else {
            prevPosition = 0;

            playAds(true);

        }

    }


    @Override
    public void onBackPressed() {
        VideoView videoView = displayLocalFolderAdsModel.getDisplayVideoView();
        videoView.pause();
        finish();
    }


    @Override
    public void onDestroy()
    {



        isServiceRunning = false;

        stopImageDisplayFinishTimer();
        stopFileObserverTimer();

        stopAnnouncement();
        unRegisterUpdatesFromReceiver();

        stopRunningTimers();

        if (new User().isMetricsOn(context)&& new User().isExternalCamType(context)&& User.isPlayerRegistered(context))
        {
            FileUtils.releaseFile();
            // step.4 release uvc camera resources
            if (mCameraHelper != null)
            {
                mCameraHelper.closeCamera();
                mCameraHelper.release();
                mCameraHelper.unregisterUSB();
                isRequest=false;
                isPreview=false;
                //new DisplayDebugLogs(context).execute("\nonDestroy:clear all mCameraHelper stuff");
            }
        }
        super.onDestroy();
    }

    //cancel timers or handlers
    private void stopRunningTimers() {
        stopAutoWebScrollTimers();
        stopPdfRunnableTask();
    }


    private void stopImageDisplayFinishTimer() {
        try {
            if (imageScheduleTimer != null) {
                imageScheduleTimer.cancel();
                imageScheduleTimer.purge();
            }
        } catch (Exception e) {

        }
    }


    protected void callFileObserver() {


        if (fileObsereverTimer == null) {
            fileObsereverTimer = new Timer();
        }

        fileObsereverTimer.schedule(new RefreshCampaignsData(DisplayLocalFolderAds.this), Constants.REFRESH_CAMPAIGNS_DURATION);


    }



    private void stopFileObserverTimer() {
        try {

            if (fileObsereverTimer != null) {
                fileObsereverTimer.cancel();
                fileObsereverTimer.purge();
            }

        } catch (Exception e) {

        } finally {
            fileObsereverTimer = null;
        }

    }


    class PlayPriorityDisplayAds extends AsyncTask<Boolean, Void, MediaInfo> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected MediaInfo doInBackground(Boolean... values) {


            if (priorityList != null && priorityList.size() > 0) {
                mediaInfo  = (priorityList.get(0));
                return mediaInfo;
            } else {
                return null;
            }


        }

        @Override
        protected void onPostExecute(MediaInfo mediaInfo) {

            if (isServiceRunning) {


                if (mediaInfo == null) {
                    isPriorityTaskPlaying = false;
                    stopPlayingAnnouncement();
                    playAds(false);
                } else {

                    isPriorityTaskPlaying = true;

                    if (displayLocalFolderAdsModel.isReport(mediaInfo.getMediaName())) {
                        stopPlayingAnnouncement();
                    } else {
                        playAnnouncementText(mediaInfo);
                    }


                    displayAd(mediaInfo);

                }

            }

        }


    }

    private void stopRegularDisplay() {
        stopImageDisplayFinishTimer();
        stopRunningTimers();
        displayLocalFolderAdsModel.getDisplayVideoView().stopPlayback();

        //check and stop current campaign audio
        checkAndStopCurrentCampaingBgAudio();
        //dismiss any campaing loading progress bar
        checkAndDismissWebViewProgressBar();

    }


    private void playPriorityTask() {

        new PlayPriorityDisplayAds().execute();
    }


    @Override
    public void onInit(int status) {
        try {

            if (status == TextToSpeech.SUCCESS) {


            } else if (status == TextToSpeech.LANG_MISSING_DATA || status == TextToSpeech.LANG_NOT_SUPPORTED) {

                Toast.makeText(displayLocalFolderAdsModel.getContext(), getString(R.string.install_tts_alert), Toast.LENGTH_LONG).show();
                isRelaunchAppOnStop = false;

                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivityForResult(installIntent, TTS_ACTION);

            } else {
                Toast.makeText(displayLocalFolderAdsModel.getContext(), getString(R.string.tts_error_alert), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    //initialize text to speech engine
    private void initAnnouncementEngine() {
        announcement = new TextToSpeech(this, this);
        announcement.setLanguage(Locale.US);
    }

    //init announcemenet settings
    private void initAnnouncementSettings() {
        SharedPreferences settingsModel = getSharedPreferences(getString(R.string.announcement_settings_sp), MODE_PRIVATE);

        announcementTimes = settingsModel.getInt(getString(R.string.announcement_settings_announcement_times), AnnouncementSettingsConstants.Announcement_Text_Min_Times);
        announcementDuration = settingsModel.getLong(getString(R.string.announcement_settings_announcement_gap), AnnouncementSettingsConstants.Announcement_Text_Duration);
        ;
        announcementString = settingsModel.getString(getString(R.string.announcement_settings_announcement_text), getString(R.string.default_announcement_settings_announcement_text));
    }

    //check and play announcements
    private void playAnnouncementText(MediaInfo mediaInfo) {
        if (announcement != null && announcementString != null) {
            String announcementText = displayLocalFolderAdsModel.getAnnounceText(mediaInfo.getMediaName(), announcementString);

            if (announcementText != null) {
                //start play announcement
                long individualAnnouncementTimes = announcementTimes;

                announcement.speak(announcementText, TextToSpeech.QUEUE_FLUSH, null);

                --individualAnnouncementTimes;

                while (individualAnnouncementTimes > 0) {
                    announcement.playSilence(announcementDuration, TextToSpeech.QUEUE_ADD, null);
                    announcement.speak(announcementText, TextToSpeech.QUEUE_ADD, null);
                    --individualAnnouncementTimes;
                }

            }
        }
    }

    private void stopPlayingAnnouncement() {
        if (announcement != null) {
            announcement.stop();
        }
    }

    //stop announcement
    private void stopAnnouncement() {
        if (announcement != null) {
            announcement.shutdown();
        }
    }



    protected void restartActivity() {
        isRelaunchAppOnStop = false;
        finish();
        startActivity(new Intent(displayLocalFolderAdsModel.getContext(), DisplayLocalFolderAds.class));
    }

    //process text file
    private void processTxtFile(MediaInfo mediaInfo) {
        //String processedText = new MediaModel().readTextFile(mediaInfo.getPathname());
        String processedText = mediaInfo.getInfo();

        if (processedText != null) {
            processProcessedText(processedText);
        } else {
            //invalid file, display next file
            playNextAd();
        }

    }

    private void processJSONObject(JSONObject jsonObject) throws Exception {
        if (jsonObject.has(getString(R.string.media_duration_json_key))) {
            mediaInfo.setDuration(jsonObject.getLong(getString(R.string.media_duration_json_key)));
        }

        //check for offer audio
        if (jsonObject.has(getString(R.string.bg_audio_json_key))) {
            mediaInfo.setBgAudioFileName(jsonObject.getString(getString(R.string.bg_audio_json_key)));

        }

        //check for offer audio setting
        if (jsonObject.has(getString(R.string.can_play_bg_audio_json_key))) {
            mediaInfo.setCanPlayBgAudio(jsonObject.getBoolean(getString(R.string.can_play_bg_audio_json_key)));
        }


    }

    //process processed text
    private void processProcessedText(String processedText) {
        try {
            JSONObject jsonObject = new JSONObject(processedText);
            processJSONObject(jsonObject);
            mediaInfo.setInfoJson(jsonObject);
            if ((!mediaInfo.getIsSkip() && checkCampaignScheduleDuration()) || mediaInfo.getIsForcePlay())   {
                if(tempDeletedCampaigns.contains(mediaInfo.getCampaignLocalId()) && mediaInfo.getIsForcePlay()==false)
                {

                    //campaign has been deleted or unassigned so dont play this
                    tempDeletedCampaigns.remove(mediaInfo.getCampaignLocalId());
                    playNextAd();

                }else
                {
                    //set isAllMediasAreSkipped to false
                    isAllMediasAreSkipped = false;

                    //check and play bg audio file for campaign
                    checkAndPlayCampaignBgAudio();

                    String type = jsonObject.getString("type");
                    mediaInfo.setMediaType(type);
                    mediaInfo.initCampaignStartTime();


                    if (type.equalsIgnoreCase(getString(R.string.app_default_multi_region))) {
                        //check and play scorll text
                        checkAndScrollTextFromJSON(jsonObject);
                        //multi region play
                        playMultiRegion(jsonObject);

                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_url_name))) {
                        Log.i("processFiles", "Play url");

                        //check and play scorll text
                        checkAndScrollTextFromJSON(jsonObject);

                        // playURL(jsonObject.getString("url"));
                        checkAndPlayURL(jsonObject.getString("url"));

                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_image_name))) {
                        Log.i("processFiles", "Play Img");

                        //check and play scorll text
                        checkAndScrollTextFromJSON(jsonObject);

                        checkAndPlayImgFileFromTextFile(jsonObject);

                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_video_name))) {
                        Log.i("processFiles", "Play video");

                        //check and play scorll text
                        checkAndScrollTextFromJSON(jsonObject);

                        checkAndPlayVideoFileFromTextFile(jsonObject);

                    } else {
                        //invalid file , try play next media
                        playNextAd();
                    }
                }

            } else {
                //dismiss single and multi regions

                //skip the media and play next ad
                playNextAd();


            }
        } catch (Exception e) {
            //error processing text, try play next media
            e.printStackTrace();

            playNextAd();
        }

    }


    private void checkAndDismissWebViewProgressBar() {
        if (webLoadingProgressbar != null) {
            webLoadingProgressbar.setIndeterminate(false);
            webLoadingProgressbar.setVisibility(View.GONE);

        }

    }

    private void checkAndClearWebViewContent() {
        WebView web = displayLocalFolderAdsModel.displayURLView;
        if (web != null && web.getVisibility() == View.VISIBLE) {
            web.loadUrl("about:blank");
        }
    }

    private void startWebSideLoading() {
        if (webLoadingProgressbar != null) {
            webLoadingProgressbar.setVisibility(View.VISIBLE);
            webLoadingProgressbar.setIndeterminate(true);

        }
    }

    private void updateWebSiteLoadingPercentage(int percentage) {


        if (webLoadingProgressbar != null && webLoadingProgressbar.getVisibility() == View.VISIBLE) {
            webLoadingProgressbar.setIndeterminate(false);

            String colorCode = "#33691E";
            if (percentage <= 10) {
                colorCode = "#DCEDC8";
            } else if (percentage <= 20) {
                colorCode = "#C5E1A5";
            } else if (percentage <= 30) {
                colorCode = "#AED581";
            } else if (percentage <= 40) {
                colorCode = "#9CCC65";
            } else if (percentage <= 50) {
                colorCode = "#8BC34A";
            } else if (percentage <= 60) {
                colorCode = "#7CB342";
            } else if (percentage <= 80) {
                colorCode = "#689F38";
            } else if (percentage <= 90) {
                colorCode = "#558B2F";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webLoadingProgressbar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(colorCode)));
            } else {
                Drawable progressDrawable = webLoadingProgressbar.getProgressDrawable().mutate();
                progressDrawable.setColorFilter(Color.parseColor(colorCode), android.graphics.PorterDuff.Mode.MULTIPLY);
                webLoadingProgressbar.setProgressDrawable(progressDrawable);

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                webLoadingProgressbar.setProgress(percentage, true);
            } else {
                webLoadingProgressbar.setProgress(percentage);
            }
        }
    }


    //set web view properties
    private WebView initializeWebView() {


        //check and dismiss progress bar
        checkAndDismissWebViewProgressBar();


        // new webview
        final WebView web = DisplayLocalFolderAdsModel.displayURLView;
        //web.clearCache(true);
        //web.clearHistory();

        //web.destroyDrawingCache();


        // web settings
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

                updateWebSiteLoadingPercentage(progress);

            }

        });

        web.setWebViewClient(new WebViewClient() {

            boolean isLoadingError = false;

            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                try {
                    checkAndDismissWebViewProgressBar();

                    isLoadingError = true;
                    //if there’s an error loading the page, make a toast
                    // Log.i("Process files","description - "+description);
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
                    checkAndDismissWebViewProgressBar();

                    //check and start auto scroll settings
                    if (view != null) {
                        checkAndAutoScrollSettings(view.getId(), url);
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
                removeAutoScrollSettingsForWebView(view.getId());

                startWebSideLoading();

            }

        });

        webViewDisplayListeners(getMediaDuration(mediaInfo));


        // return the web view
        return web;
    }

    private boolean isMediaPlaying() {
        return mediaInfo != null && mediaInfo.getIsPlaying();
    }

    //play next ad
    private void playNextAd() {

        //savaing previous campaign

        saveCampaignToReportsDB();

        //check and update next schedule
        checkAndUpdateNextSchedule();

        //check and stop current campaign audio
        checkAndStopCurrentCampaingBgAudio();

        if (isServiceRunning && isMediaPlaying()) {

            if (isPriorityTaskPlaying) {

                checkForNextPriorityTask(false);

            } else {

                playAds(false);
            }
        }
    }

    protected void initPriorityAd()
    {
        if (!isPriorityTaskPlaying) {

            isPriorityTaskPlaying = true;

            //stop regular display and start playing priority task
            stopRegularDisplay();

            saveCampaignToReportsDB();

            playPriorityTask();
        }
    }

    //set listeners to display next media after displaying url
    private void webViewDisplayListeners(long duration) {

        //set duration for timer
        if (isServiceRunning) {
            if (duration > 0) {
                imageScheduleTimer = new Timer();
                imageScheduleTimer.schedule(new settingDurationForWebView(), duration);
            }
        }
    }


    private class settingDurationForWebView extends TimerTask {
        public void run() {
            try {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            if (isServiceRunning) {
                                stopRunningTimers();

                                checkAndDismissWebViewProgressBar();

                                //check and clear webview content
                                checkAndClearWebViewContent();


                                if (mediaInfo != null && mediaInfo.isMultiRegPlayingVideoWithSound(displayLocalFolderAdsModel.getContext())) {
                                    //check and resume media player (Back ground play)
                                    checkAndResumeMediaPlayer(true);
                                }


                                playNextAd();

                            }


                        } catch (Exception e) {

                            e.printStackTrace();

                        }
                    }
                });


            } catch (Exception e) {

            }
        }
    }

    //get media duration
    private long getMediaDuration(MediaInfo mediaInfo) {


        if (displayLocalFolderAdsModel.isReport(mediaInfo.getMediaName())) {

            return new User().getReportImageDuration(displayLocalFolderAdsModel.getContext());
        } else {
            if (mediaInfo.getDuration() == 0) {
                //no duration has been initialized , set default duration from settings
                return new User().getImageDuration(displayLocalFolderAdsModel.getContext());
            } else if (mediaInfo.getDuration() == -1) {
                //infinity display
                //return mediaInfo.getDuration();
                return new User().getImageDuration(displayLocalFolderAdsModel.getContext());
            } else {
                return (mediaInfo.getDuration() * 1000);
            }

        }
    }

    //distroy web view
    private void destroyWebView() {
        try {
            displayLocalFolderAdsModel.displayURLView.loadUrl(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndScrollTextFromJSON(JSONObject jsonObject) throws JSONException,NullPointerException {
        //check and get ticker text scroll mode options (media name/customized text)
        switch (new User().getLocalScrollTextMode(displayLocalFolderAdsModel.getContext())) {
            //customized ticker text mode
            case Constants.SCROLLING_CUSTOMISED_TEXT:

                if (jsonObject.has(getString(R.string.hide_ticker_txt))) {
                    boolean hideTickerTxtFlag = jsonObject.getBoolean(getString(R.string.hide_ticker_txt));
                    if (hideTickerTxtFlag) {
                        TextView scrollingTextTV = displayLocalFolderAdsModel.getScrollingTextTV();
                        scrollingTextTV.setVisibility(View.GONE);
                    } else {
                        //scrolling global ticker text
                        SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(displayLocalFolderAdsModel.getContext());
                        String tickerText = saveSP.getString(getString(R.string.local_scroll_text), getString(R.string.display_ads_layout_scrolling_text));
                        displayLocalFolderAdsModel.setMediaDefaultScrollText(tickerText);

                    }
                } else if (jsonObject.has(getString(R.string.offer_txt_json_key))) {
                    String offerText = jsonObject.getString(getString(R.string.offer_txt_json_key));
                    if (jsonObject.has(getString(R.string.is_display_scroll_txt_json_key))) {
                        if (jsonObject.getBoolean(getString(R.string.is_display_scroll_txt_json_key))) {
                            //based on user mode get scroll text
                            SharedPreferences saveSP = new SharedPreferenceModel().getLocalScrollTextSharedPreference(displayLocalFolderAdsModel.getContext());
                            String scrollText = saveSP.getString(getString(R.string.local_scroll_text), getString(R.string.display_ads_layout_scrolling_text));

                            //if the  offer text is not null then concatenate offer with scrollText and set it to scrollingTextTV TextView
                            if (offerText != null && offerText.length() >= 1) {
                                offerText = offerText + "\t\t\t\t\t\t\t\t\t\t" + scrollText;
                            } else {
                                offerText = scrollText;
                            }

                            displayLocalFolderAdsModel.setMediaDefaultScrollText(offerText);

                        } else {
                            //if the  offer text is not null then concatenate offer with scrollText and set it to scrollingTextTV TextView
                            if (offerText != null && offerText.length() >= 1) {
                                displayLocalFolderAdsModel.setMediaDefaultScrollText(offerText);
                            }
                        }

                    }

                } else {
                    setScrollTextMediaName(mediaInfo);
                }

                break;

            case Constants.SCROLLING_MEDIA_NAME://ticker text(display media name)

                setScrollTextMediaName(mediaInfo);

                break;

            default:

                setScrollTextMediaName(mediaInfo);//default ticker text(display media name)
                break;

        }

    }

    //check and play image file
    private void checkAndPlayImgFileFromTextFile(JSONObject jsonObject) throws Exception {
        //get resource file
        String imageName = jsonObject.getString(getString(R.string.media_resource_json_key));

        String dir = new User().getUserPlayingFolderModePath(displayLocalFolderAdsModel.getContext());

        String imagePath = dir + "/" + imageName;

        File dirFile = new File(imagePath);

        if (dirFile.exists()) {


            displayImageView(dirFile.getPath());

        } else {

            //resource not found

            //invalid file , try play next media
            playNextAd();

        }
    }

    //check and play image file
    public void checkAndPlayVideoFileFromTextFile(JSONObject jsonObject) throws Exception {
        //get resource file
        String imageName = jsonObject.getString(getString(R.string.media_resource_json_key));
        String dir = new User().getUserPlayingFolderModePath(displayLocalFolderAdsModel.getContext());

        String imagePath = dir + "/" + imageName;
        File dirFile = new File(imagePath);

        if (dirFile.exists()) {


            displayVideoView(dirFile.getPath());
        } else {
            //resource not found

            //invalid file , try play next media
            playNextAd();
        }
    }

    public class UpdatesFromReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (intent != null) {
                    if (intent.hasExtra(getString(R.string.action))) {
                        String actionString = intent.getStringExtra(getString(R.string.action));

                        if (actionString.equalsIgnoreCase(getString(R.string.start_streaming))) {
                            startPlayingSMAnnouncement();

                        } else if (actionString.equalsIgnoreCase(getString(R.string.update_scroll_text_action))) {

                            setScrollText();

                        } else if (actionString.equalsIgnoreCase(getString(R.string.update_offer_audio_action))) {
                            updateOfferAudio();

                        }
                        //no need to do any thing for this case just need to save action layout setting in SP
                        else if (actionString.equalsIgnoreCase(getString(R.string.display_customer_interactive_action))) {
                            //noting need to do in this case

                        } else if (actionString.equalsIgnoreCase(getString(R.string.display_customer_interactive_action_text))) {
                            displayLocalFolderAdsModel.displayActionScrollingText( null);
                        }

                        //update action record Action Scrolling text
                        else if (actionString.equalsIgnoreCase(getString(R.string.customer_interactive_update_actions_text_request))) //update action record Action Scrolling text
                        {
                            JSONObject jsonObject=new JSONObject(intent.getStringExtra(getString(R.string.action_extra_info)));

                            Log.i("JSONObject",""+jsonObject.getString("action_text"));
                            displayLocalFolderAdsModel.updateCustomerActionText(jsonObject.getString("action_text"));
                        } else if (actionString.equalsIgnoreCase(getString(R.string.customer_interactive_actions_close_request)))//close/delete action record from the table in  D
                        {
                            displayLocalFolderAdsModel.updateCustomerActionStatus();
                        } else if (actionString.equals(getString(R.string.handle_rule_request))) {
                            ArrayList<File> campaignFilesArray = (ArrayList<File>) intent.getSerializableExtra("campaignFiles");
                            handleCampaignRule(campaignFilesArray);
                        } else if (actionString.equalsIgnoreCase("PAUSE_MEDIA_ACTION_CODE")) {
                            checkAndPausePlayingMedia();
                        } else if (actionString.equalsIgnoreCase("RESUME_MEDIA_ACTION_CODE")) {
                            checkAndResumePlayingMedia();

                        } else if (actionString.equalsIgnoreCase("KEYCODE_MENU")) {
                            isRelaunchAppOnStop = false;
                            startActivity(new Intent(displayLocalFolderAdsModel.getContext(), UserGuideActivity.class));
                            finish();
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void startPlayingSMAnnouncement() {
        Intent intent = new Intent(displayLocalFolderAdsModel.getContext(), PlayAnnouncementActivity.class);
        startActivityForResult(intent, SM_ANNOUNCEMENT);

    }

    private void registerUpdatesFromReceiver() {
        IntentFilter intentFilter = new IntentFilter(SM_UPDATES_INTENT_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (updatesFromReceiver == null) {
            // Log.i("stream ","registerd");
            updatesFromReceiver = new UpdatesFromReceiver();
            registerReceiver(updatesFromReceiver, intentFilter);
        }
    }

    //un register  receivers
    private void unRegisterUpdatesFromReceiver() {
        try {
            unregisterReceiver(updatesFromReceiver);
        } catch (Exception e) {

        } finally {
            updatesFromReceiver = null;
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case SM_ANNOUNCEMENT:
                isFromAnnouncement = true;
                if (mediaInfo != null && !mediaInfo.isMediaRepeating() && (new File(mediaInfo.getPathname())).exists()) {

                    if (mediaInfo.getMediaType().equalsIgnoreCase(getString(R.string.app_default_image_name))) {
                        imageScheduleTimer = new Timer();
                        imageScheduleTimer.schedule(new settingDurationForImageView(), new User().getImageDuration(displayLocalFolderAdsModel.getContext()));

                    } else {

                        VideoView videoView = displayLocalFolderAdsModel.getDisplayVideoView();
                        videoView.seekTo(stopPosition);
                        videoView.start();
                    }

                } else {
                    if (mediaInfo.isMediaRepeating()) {

                        if (new SetBootTimeForMediaSettingsConstants().getPlayCampaignOnBootOnceSettings(context)) {

                            closeOnPlayOnceSettings();

                        } else {
                            prevPosition = 0;

                            playAds(true);
                        }

                    }
                }
                break;

            case INTERACTIVE_FEED_BACK_FORM:
                isRelaunchAppOnStop = true;
                break;


            case TTS_ACTION:
                isRelaunchAppOnStop = true;
                break;

            case REQUEST_CHECK_SETTINGS:
                if(resultCode == RESULT_OK) {
                    NetworkModel.changeWifiHotspotState(context,true);
                }
                break;
        }
    }

    private void setScrollText() {
        try {
            if(mediaInfo!=null)
                checkAndScrollTextFromJSON(mediaInfo.getInfoJson());
                if(displayLocalFolderAdsModel!=null)
                    displayLocalFolderAdsModel.setProperties();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    //check and play offer audio
    private void checkAndInitOfferAudio(boolean forcePlayGlobalAudio) {
        //based on settings and audio availability
        AudioSettingsConstants audioSettingsConstants = new AudioSettingsConstants();

        //check whether media is associated with its own bg audio , if yes then play that
        if (canPlayCampaignAudio() && forcePlayGlobalAudio == false) {
            //play media associated audio
            checkAndPlayCampaignBgAudio();

        } else if (audioSettingsConstants.getPlayOfferAudioSettings(displayLocalFolderAdsModel.getContext())) {
            try {

                //initialize media player
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnErrorListener(this);
                mediaPlayer.setLooping(false);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        //play next audio
                        mediaPlayer.reset();

                        boolean isSuccess = backGroundAudioHandler.playNextAudio(mediaPlayer);

                        if (!isSuccess) {
                            //release media player
                            checkAndReleaseMediaPlayer();
                        }
                    }
                });

                boolean isSuccess = backGroundAudioHandler.playAudio(mediaPlayer);
                if (!isSuccess) {
                    //release media player
                    checkAndReleaseMediaPlayer();
                }

            } catch (Exception e) {
                Toast.makeText(displayLocalFolderAdsModel.getContext(), getString(R.string.media_player_init_error) + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

        }

    }


    //implement on prepared listeners
    public void onPrepared(MediaPlayer mediaPlayer) {
        checkAndResumeMediaPlayer(false);
    }

    public boolean onError(MediaPlayer mediaPlayer, int error, int errorCode) {
        Toast.makeText(displayLocalFolderAdsModel.getContext(), "Error in media player" + error, Toast.LENGTH_SHORT).show();
        return false;
    }

    //check and pause media player
    private synchronized void checkAndPauseMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    //check and resume media player
    private void checkAndResumeMediaPlayer(boolean isForceStart) {
        if (mediaPlayer != null) {
            //isForceStart will be true only in video complete listener
            if (canPlayMediaPlayer() || isForceStart) {
                //dont play video if video is gettting played
                mediaPlayer.start();
            }

        } else {
            checkAndInitOfferAudio(false);
        }
    }

    //check and release media player
    private void checkAndReleaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    //check and play campaign based back ground audio
    private void checkAndPlayCampaignBgAudio() {
        if (canPlayCampaignAudio()) {
            String dir = new User().getUserPlayingFolderModePath(displayLocalFolderAdsModel.getContext());
            String imagePath = dir + "/" + mediaInfo.getBgAudioFileName();
            File dirFile = new File(imagePath);
            if (dirFile.exists()) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }

                    mediaPlayer.reset();
                }

                try {

                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(dirFile.getAbsolutePath());
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnErrorListener(this);
                    mediaPlayer.setLooping(true);

                    //nullify the back ground audio playing audio path
                    backGroundAudioHandler.playingFileName = dirFile.getAbsolutePath();

                } catch (Exception e) {
                    Toast.makeText(displayLocalFolderAdsModel.getContext(), getString(R.string.media_player_init_error) + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //check and stop current campaign bg audio and restart
    private void checkAndStopCurrentCampaingBgAudio() {
        //to check whether the media player is playing media attached audio
        if (canPlayCampaignAudio() && mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.reset();

            //restart playing default audio,, play default audio by default,, the current media info played is stopped
            checkAndInitOfferAudio(true);
        }
    }

    private void updateOfferAudio() {
        if (new AudioSettingsConstants().getPlayOfferAudioSettings(displayLocalFolderAdsModel.getContext())) {
            if (canPlayMediaPlayer()) {
                checkAndInitOfferAudio(false);
            }

        } else {
            checkAndReleaseMediaPlayer();
        }

    }

    //method to play audio
    private boolean canPlayMediaPlayer() {

        if ((!(mediaInfo != null && mediaInfo.getMediaType() != null && mediaInfo.getMediaType().equalsIgnoreCase(getString(R.string.app_default_video_name))))
                && !(mediaInfo != null && mediaInfo.isMultiRegPlayingVideoWithSound(DisplayLocalFolderAds.this))) {

            return true;
        } else {
            //if playing type is video then don't play or start the media player
            return false;
        }
    }


    private void setBgAudioFileObservers() {
        //before init refresh files
        backGroundAudioHandler.refreshFiles();

        FileObserver audioObserver = new FileObserver(new AudioSettingsConstants().backgroundAudiosFolder(displayLocalFolderAdsModel.getContext())) {
            @Override
            public void onEvent(int i, @Nullable String filePath) {

                if (i == FileObserver.CLOSE_WRITE) {

                    int newLength = backGroundAudioHandler.checkAndAddNewFile(filePath);
                    if (newLength == 1 && canPlayMediaPlayer() && !(mediaPlayer != null && mediaPlayer.isPlaying())) {
                        //if the new file is added is the first song in the list
                        //and it can play next media i.e.. media player is still not yet initialized then check and start playing
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkAndInitOfferAudio(false);
                            }
                        });
                    }
                } else if (i == FileObserver.DELETE) {
                    //Log.i("audio observer", "Inside audio observer delete- " + i + ", string - " + filePath);
                    try {
                        if (filePath != null && (mediaPlayer != null && mediaPlayer.isPlaying()) && backGroundAudioHandler.playingFileName != null && backGroundAudioHandler.playingFileName.equalsIgnoreCase(filePath)) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkAndInitOfferAudio(false);
                                }
                            });
                        }
                    } catch (Exception e) {
                        //don't do nothing
                    }
                }
            }
        };
        audioObserver.startWatching();

        backGroundAudioHandler.newAudioListener = audioObserver;


    }

    //remove back ground audio observers
    private void removeBgAudioFileObservers() {
        if (backGroundAudioHandler.newAudioListener != null) {
            backGroundAudioHandler.newAudioListener.stopWatching();
            backGroundAudioHandler.newAudioListener = null;
        }


    }


    //can play campaign audio
    private boolean canPlayCampaignAudio() {
        //media info should not be null and should has some audio attached to it and play check box should be checked
        return (mediaInfo != null && mediaInfo.getBgAudioFileName() != null && mediaInfo.getCanPlayBgAudio());
    }

    //process multi region
    private void playMultiRegion(JSONObject jsonObject) throws JSONException {

        if (displayLocalFolderAdsModel.displayMultiRegion()) {
            HashMap<String, Object> multiRegionProp = new MultiRegionSupport(displayLocalFolderAdsModel.getContext(), DisplayLocalFolderAds.this, displayLocalFolderAdsModel.multiRegionParent).processMultiRegionJSON(jsonObject.getJSONArray("regions"));
            mediaInfo.setMultiRegProperties(multiRegionProp);
            if (mediaInfo.isMultiRegPlayingVideoWithSound(displayLocalFolderAdsModel.getContext())) {
                //check and pause playing media player(Back ground)
                checkAndPauseMediaPlayer();
            }

            //check and force ticker text view to scroll.. for video regions
            if (multiRegionProp != null && multiRegionProp.containsKey(getString(R.string.has_video))) {
                if ((Boolean) multiRegionProp.get(getString(R.string.has_video))) {
                    displayLocalFolderAdsModel.forceScrollTickerTV();
                    displayLocalFolderAdsModel.forceScrollActionTV();
                }
            }

            if((multiRegionProp!=null && multiRegionProp.containsKey(getString(R.string.set_timer)))) {
                boolean isSetTimer = (boolean) multiRegionProp.get(getString(R.string.set_timer));
                if(isSetTimer==false){
                    return;
                }
            }

            webViewDisplayListeners(getMediaDuration(mediaInfo));



        }
    }

    //remove auto scroll for webview
    public void removeAutoScrollSettingsForWebView(int webViewId) {
        if (webViewAutoScrollTimers != null && webViewAutoScrollTimers.containsKey(webViewId)) {
            //timer already exist
            //stop timer and restart
            Timer timer = webViewAutoScrollTimers.remove(webViewId);
            stopTimer(timer);


        }
    }

    //check and add auto scroll webviews
    public void checkAndAutoScrollSettings(int webViewId, String url) {

        if (displayLocalFolderAdsModel != null && displayLocalFolderAdsModel.canScrollURL(url)) {

            SharedPreferences settingsSP = getSharedPreferences(getString(R.string.settings_sp), MODE_PRIVATE);

            if ( webViewId > 0 && settingsSP.getBoolean(getString(R.string.auto_scroll_web_view_sp), URLSettingsAct.DEFAULT_AUTO_SCROLL_SETTING))
            {
               // Log.d("AutoScroll","Inside webview auto scroll task settings true");
                Timer timer;
                //check for timer task existance
                if (webViewAutoScrollTimers != null && webViewAutoScrollTimers.containsKey(webViewId)) {
                    //timer already exist
                    //stop timer and restart
                    timer = webViewAutoScrollTimers.remove(webViewId);
                    stopTimer(timer);


                }

                timer = new Timer();



                timer.scheduleAtFixedRate(new WebViewAutoScrollTask(DisplayLocalFolderAds.this, webViewId), settingsSP.getLong(getString(R.string.auto_scroll_web_view_duration_sp), URLSettingsAct.DEFAULT_AUTO_SCROLL_DURATION),
                        settingsSP.getLong(getString(R.string.auto_scroll_web_view_duration_sp), URLSettingsAct.DEFAULT_AUTO_SCROLL_DURATION));

                webViewAutoScrollTimers.put(webViewId, timer);

            }
        } else {

            Log.d("AutoScroll","Inside webview auto scroll task canplay false");
            if (webViewAutoScrollTimers != null && webViewAutoScrollTimers.containsKey(webViewId)) {
                //timer already exist
                //stop timer and restart
                Timer timer = webViewAutoScrollTimers.remove(webViewId);
                stopTimer(timer);
            }
        }
    }

    //stop timer
    private void stopTimer(Timer timer) {
        try {
            timer.cancel();
            timer.purge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAutoWebScrollTimers() {
        if (webViewAutoScrollTimers != null) {
            while (webViewAutoScrollTimers.size() >= 1) {
                stopTimer(webViewAutoScrollTimers.removeAt(0));
            }

        }
    }

    private void pauseWebViewScrolls() {
        if (webViewAutoScrollTimers != null) {

            for (int i = 0; i < webViewAutoScrollTimers.size(); i++) {
                stopTimer(webViewAutoScrollTimers.valueAt(i));
            }
        }
    }

    private void resumeWebViewScrolls() {
        if (webViewAutoScrollTimers != null) {
            SharedPreferences settingsSP = getSharedPreferences(getString(R.string.settings_sp), MODE_PRIVATE);

            for (int i = 0; i < webViewAutoScrollTimers.size(); i++) {

                Timer timer = new Timer();
                {
                    //step wise scroll
                    timer.schedule(new WebViewAutoScrollTask(DisplayLocalFolderAds.this, webViewAutoScrollTimers.keyAt(i)), settingsSP.getLong(getString(R.string.auto_scroll_web_view_duration_sp), URLSettingsAct.DEFAULT_AUTO_SCROLL_DURATION),
                            settingsSP.getLong(getString(R.string.auto_scroll_web_view_duration_sp), URLSettingsAct.DEFAULT_AUTO_SCROLL_DURATION));

                }
                webViewAutoScrollTimers.put(webViewAutoScrollTimers.keyAt(i), timer);
            }
        }
    }

    //init action recevier
    private void initActionReceiver() {
        if (actionReceiver == null) {
            actionReceiver = new ActionReceiver(new Handler());
            actionReceiver.setHandelActionReceiverHandler(new ActionReceiverHandler(this));
        }
    }

    //removeActionReceiver
    private void removeActionReceiver() {
        actionReceiver.setHandelActionReceiverHandler(null);
        actionReceiver = null;
    }

    //check and skip media
    public void checkAndSkipPlayingCampaign(String campaignNameToStop) {
        if (mediaInfo != null && mediaInfo.getMediaName() != null && mediaInfo.getMediaName().equalsIgnoreCase(campaignNameToStop)) {
            //skip media
            skipCurrentPlayingMedia();
        }
    }

    //skip current playing media
    protected void skipCurrentPlayingMedia() {
        //stop timers and related audios of medias
        stopRegularDisplay();
        playNextAd();
    }

    ////check and pause current playing media
    protected void checkAndPausePlayingMedia() {
        String mediaType = mediaInfo.getMediaType();

        if (mediaInfo != null && mediaType != null && isMediaPlaying()) {
            mediaInfo.setIsPlaying(false);
            displayLocalFolderAdsModel.toggleDisplayMediaPlayingStatusIc(false);
            stopFileObserverTimer();

            //remove file observers
            removeBgAudioFileObservers();

            //check and pause media player
            checkAndPauseMediaPlayer();

            String[] availableMediaTypes = getResources().getStringArray(R.array.available_media_types);

            //check and pause respective media
            for (String type : availableMediaTypes) {
                if (mediaType.equalsIgnoreCase(type)) {

                    if (type.equalsIgnoreCase(getString(R.string.app_default_video_name))) {
                        //pause playing video
                        displayLocalFolderAdsModel.pauseVideoPlaying();
                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_image_name))) {
                        //stop timer
                        stopImageDisplayFinishTimer();
                        mediaInfo.resetDuration(getMediaDuration(mediaInfo));

                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_url_name))) {
                        stopImageDisplayFinishTimer();
                        mediaInfo.resetDuration(getMediaDuration(mediaInfo));

                        displayLocalFolderAdsModel.pauseWebView();
                        pauseWebViewScrolls();
                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_multi_region))) {
                        stopImageDisplayFinishTimer();
                        mediaInfo.resetDuration(getMediaDuration(mediaInfo));
                        displayLocalFolderAdsModel.pauseMultiRegionUI();
                        pauseWebViewScrolls();
                    } else {
                        stopImageDisplayFinishTimer();
                        mediaInfo.resetDuration(getMediaDuration(mediaInfo));
                    }

                    break;
                }
            }
        }
    }

    //check and resume media
    public void checkAndResumePlayingMedia() {
        String mediaType = mediaInfo.getMediaType();

        if (mediaInfo != null && mediaType != null && !isMediaPlaying()) {
            mediaInfo.setIsPlaying(true);
            displayLocalFolderAdsModel.toggleDisplayMediaPlayingStatusIc(true);
            callFileObserver();
            //initialize file obserserver
            setBgAudioFileObservers();

            checkAndResumeMediaPlayer(false);

            //update media resume at
            mediaInfo.setMediaResumedAt(Calendar.getInstance().getTimeInMillis());

            String[] availableMediaTypes = getResources().getStringArray(R.array.available_media_types);

            //check and pause respective media
            for (String type : availableMediaTypes) {
                if (mediaType.equalsIgnoreCase(type)) {

                    if (type.equalsIgnoreCase(getString(R.string.app_default_video_name))) {
                        //pause playing video
                        displayLocalFolderAdsModel.resumeVideoPlaying();

                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_image_name))) {
                        //stop timer
                        resumeImageDisplayFinishTimer(type);

                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_url_name))) {
                        resumeImageDisplayFinishTimer(type);
                        displayLocalFolderAdsModel.resumeWebView();
                        resumeWebViewScrolls();

                    } else if (type.equalsIgnoreCase(getString(R.string.app_default_multi_region))) {
                        resumeImageDisplayFinishTimer(type);
                        displayLocalFolderAdsModel.resumeMultiRegionUI();
                        resumeWebViewScrolls();
                    } else {
                        //stop timer
                        resumeImageDisplayFinishTimer(type);
                    }

                    break;
                }
            }
        }
    }

    //resume image timer
    private void resumeImageDisplayFinishTimer(String mediaType) {
        if (mediaInfo.getDuration() != -1) { //if it is not infinite

            imageScheduleTimer = new Timer();

            long durationInMs = mediaInfo.getDuration() * 1000;

            if (mediaType.equalsIgnoreCase(getString(R.string.app_default_image_name))) {
                imageScheduleTimer.schedule(new settingDurationForImageView(), durationInMs);
            } else {
                imageScheduleTimer.schedule(new settingDurationForWebView(), durationInMs);
            }
        }
    }


    private void stopPdfRunnableTask() {
        if (pdfScrollHandler != null && pdfScrollHandlerRunnable != null) {

            pdfScrollHandler.removeCallbacks(pdfScrollHandlerRunnable);
        }
    }


    protected void closeOnPlayOnceSettings() {
        try {
            finish();
            DeviceModel.processHomeCommand(context);

        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }


    }

    protected void handleCampaignRule(ArrayList<File> campaignFilesArray) {
        try
        {
            Cursor priorityCampaigns = CampaignsDBModel.getCampaignsByName(context,TextUtils.join(",",campaignFilesArray));
            if(priorityCampaigns!=null && priorityCampaigns.moveToNext())
            {
               do {
                   MediaInfo mediaInfo = new MediaInfo();
                   mediaInfo.prepareInfoFromCursor(context,priorityCampaigns);
                   mediaInfo.setForcePlay(true);
                   priorityList.add(mediaInfo);

               }while(priorityCampaigns.moveToNext());

                if (!isPriorityTaskPlaying) {

                    isPriorityTaskPlaying = true;

                    //stop regular display and start playing priority task
                    stopRegularDisplay();

                    playPriorityTask();
                }
            }


        } catch (Exception e) {
            Log.d("handleCampaignRule", "Inside handleCampaignRule error -" + e.getMessage());
        }

    }

    protected void handleCampaignRuleNew(ArrayList<String> campaignFilesArray,String currentRule) {

        try
        {
            boolean isSkipCurrentPlayingMedia = false;
            ArrayList<Long> existingPriorityAds = new ArrayList<>(priorityList.size());
            for(int i=0;i<priorityList.size();i++)
            {
               MediaInfo priorityInfo = priorityList.get(i);
               if(priorityInfo.getAssociatedRule()!=null && !priorityInfo.getAssociatedRule().equalsIgnoreCase(currentRule))
               {
                   //rule has been changed, check and remove , if current playing media then skip ,, if pending then remove from list
                   if(mediaInfo.getMediaName().equalsIgnoreCase(priorityInfo.getMediaName()))
                   {
                       isSkipCurrentPlayingMedia=true;
                   }else
                   {
                       priorityList.remove(i);
                   }
               }else
               {
                   //add to existing list
                   existingPriorityAds.add(mediaInfo.getCampaignLocalId());
               }

            }




            Cursor priorityCampaigns = CampaignsDBModel.getCampaignsByName(context,"'"+TextUtils.join("','",campaignFilesArray)+"'");
            Log.d("Rules","Inside handle get rule priorityCampaigns count "+priorityCampaigns.getCount());
            if(priorityCampaigns!=null && priorityCampaigns.moveToNext())
            {
                do {
                    MediaInfo mediaInfo = new MediaInfo();
                    mediaInfo.prepareInfoFromCursor(context,priorityCampaigns);
                    mediaInfo.setForcePlay(true);
                    mediaInfo.setAssociatedRule(currentRule);

                    if(!existingPriorityAds.contains(mediaInfo.getCampaignLocalId()))
                    {
                        priorityList.add(mediaInfo);
                    }


                }while(priorityCampaigns.moveToNext());

                if (!isPriorityTaskPlaying && priorityList.size()>=1) {

                    isPriorityTaskPlaying = true;

                    //stop regular display and start playing priority task
                    stopRegularDisplay();

                    playPriorityTask();
                }

                if(isPriorityTaskPlaying && isSkipCurrentPlayingMedia)
                {
                    skipCurrentPlayingMedia();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("handleCampaignRule", "Inside handle rule error -" + e.getMessage());
        }

    }


    public void saveAppVisibleStatus(boolean status) {
        Intent intent = new Intent(HandleKeyCommandsUpdateReceiver.INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("action", HandleKeyCommandsUpdateReceiver.IS_SIGNAGE_SCREEN_ACTIVE_KEY);
        intent.putExtra("value", status);
        sendBroadcast(intent);
    }


    public void saveAppInvokeStatus(boolean status) {
        try {
            if (MonitorAppInvokeService.isServiceActive) {
                stopService(new Intent(context, MonitorAppInvokeService.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(HandleKeyCommandsUpdateReceiver.INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("action", HandleKeyCommandsUpdateReceiver.IS_THIRD_PARTY_APP_INVOKE);
        intent.putExtra("value", status);
        sendBroadcast(intent);
    }

    public void MonitorAppInvokeService() {
        try {
            Intent appIntent = new Intent(context, MonitorAppInvokeService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DisplayLocalFolderAds.this.startForegroundService(appIntent);
            } else {
                startService(appIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        switch (keyCode) {
            case KeyEvent.KEYCODE_A: {
                //your Action code
                return true;
            }
        }
        return true;
    }

    private void saveCampaignToReportsDB() {

        if (new User().isPlayerStatisticsCollectionOn(context)) {
            try {
                if (mediaInfo != null) {
                    String campaignName = Constants.removeExtension(mediaInfo.getMediaName());

                    long serverId = CampaignsDBModel.getServerIdByCampaignName(campaignName, context);
                      //insert report
                    ContentValues cv = new ContentValues();
                    cv.put(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME, campaignName);
                    cv.put(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_SERVER_ID, serverId);
                    cv.put(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_DURATION, mediaInfo.calculateCampaignPlayedDuration());
                    cv.put(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_TIMES_PLAYED, 1);
                    cv.put(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_CREATED_AT, Calendar.getInstance().getTimeInMillis());

                    CampaignReportsDBModel.insertCampaignReportsInfo(cv, context);

                    if(new User().isPlayerStatisticsCollectionOn(context)&& User.isPlayerRegistered(context))
                    {
                        long currentTime = Calendar.getInstance().getTimeInMillis() - TimeUnit.MINUTES.toMillis(new User().getPlayerStatisticsCollectionDuration(context));
                        if (PlayerStatisticsCollectionModel.getUploadingCampReportsLastTime(context) <= currentTime) {

                            startService(new Intent(context, PlayerStatisticsCollectionService.class));
                        }
                    }
                }

            } catch (Exception E) {
                E.printStackTrace();
            }
        }
    }


    private void userMetricsTask()
    {
        if(new User().isMetricsOn(context) && new User().isExternalCamType(context)&& IOTDevice.isIOTDeviceRegistered(context))
        {
            try {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(DisplayLocalFolderAds.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
                } else
                {

                    ButterKnife.bind(this);
                    // step.1 initialize UVCCameraHelper
                    mTextureView.setVisibility(View.VISIBLE);
                    mUVCCameraView = (CameraViewInterface) mTextureView;
                    mUVCCameraView.setCallback(this);
                    mCameraHelper = UVCCameraHelper.getInstance();
                    try {
                        if (mCameraHelper.getUSBMonitor() == null) {
                            mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
                        }
                    } catch (Exception e)
                    {
                        new DisplayDebugLogs(context).execute("\nuserMetricsTask setDefaultFrameFormat Exception:"+e.toString());
                    }
                    mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);

                    detector = new FaceDetector.Builder(getApplicationContext())
                            .setTrackingEnabled(false)
                            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                            //.setMode(FaceDetector.ACCURATE_MODE)
                            // .setMode(FaceDetector.FAST_MODE)
                            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                            .build();

                }

            } catch (Exception e)
            {
                e.printStackTrace();
                mTextureView.setVisibility(View.GONE);
                new DisplayDebugLogs(context).execute("\nuserMetricsTask Exception:"+e.toString());
            }
        } else
        {
            mTextureView.setVisibility(View.GONE);

        }
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled)
    {
        if (canceled)
        {
            showShortMsg("Dear user without USB camera permission you are not able to get AD metrics, Please try again.");
        }
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        try {
            if (!isPreview && mCameraHelper.isCameraOpened())
            {
                if (!((surface instanceof SurfaceHolder) || (surface instanceof Surface)))
                {
                     new DisplayDebugLogs(context).execute("\nonSurfaceCreated:surface should be one of SurfaceHolder, Surface or SurfaceTexture: " + surface);
                    // throw new IllegalArgumentException("surface should be one of SurfaceHolder, Surface or SurfaceTexture: " + surface);
                }else
                {
                    mCameraHelper.startPreview(mUVCCameraView);
                    isPreview = true;
                    // new DisplayDebugLogs(context).execute("\nonSurfaceCreated: isPreview:"+isPreview);
                    reCapturingFrame();
                }

            }
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            new DisplayDebugLogs(context).execute("\nonSurfaceCreated IllegalArgumentException:"+e.toString());
        }

    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        try {
            if (isPreview && mCameraHelper.isCameraOpened())
            {
                //  new DisplayDebugLogs(context).execute("\nonSurfaceDestroy:stopPreview");
                mCameraHelper.stopPreview();
                isPreview = false;
            }

        } catch (Exception e)
        {
            isPreview = false;
            new DisplayDebugLogs(context).execute("\nonSurfaceDestroy Exception:"+e.toString());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                // close the app
                Toast.makeText(DisplayLocalFolderAds.this, "Sorry!!!, you can't get user metrics without granting external storage permission", Toast.LENGTH_LONG).show();
            } else
            {
                //  new DisplayDebugLogs(context).execute("\nonRequestPermissionsResult:userMetricsTask");
                userMetricsTask();
            }
        }
    }


    private void reCapturingFrame()
    {
        disconnectHandler.postDelayed(new Runnable()
        {
            @Override
            public void run() {

                try {

                    //recapture the frame for detection the face after 3sec
                    if (mCameraHelper == null || !mCameraHelper.isCameraOpened())
                    {
                        mTextureView.setVisibility(View.GONE);
                        //  new DisplayDebugLogs(context).execute("\nreCaptureFrame:mCameraHelper null");

                    } else {

                        imgPath = new DownloadMediaHelper().getCaptureImagesDirectory(context) + "/Imag" + Calendar.getInstance().getTimeInMillis() + ".jpg";
                        // String picPath = UVCCameraHelper.ROOT_PATH + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;
                        mCameraHelper.capturePicture(imgPath, new AbstractUVCCameraHandler.OnCaptureListener() {
                            @Override
                            public void onCaptureResult(String path)
                            {
                                // new DisplayDebugLogs(context).execute("\nreCaptureFrame:capturePicture onCaptureResult");
                                try
                                {
                                    if(imgPath!=null)
                                    {
                                        Looper.prepare();
                                        // new DisplayDebugLogs(context).execute("\nreCaptureFrame:scanFaces"+imgPath);
                                        scanFaces(imgPath);
                                        Looper.loop();

                                    }else
                                    {
                                        // new DisplayDebugLogs(context).execute("\nreCaptureFrame:path null"+imgPath);
                                        deleteCapturedImgFile();
                                        reCapturingFrame();
                                    }

                                }catch (Exception e)
                                {
                                    deleteCapturedImgFile();
                                    reCapturingFrame();
                                    new DisplayDebugLogs(context).execute("\nreCaptureFrame:Exception"+e.toString());
                                }
                            }
                        });

                    }

                }
                catch (Exception e)
                {
                    new DisplayDebugLogs(context).execute("\nreCaptureFrame:InterruptedException:"+e.toString());
                }

            }
        }, 5000);
    }

    private void scanFaces(String imagePath) throws Exception
    {
        Uri imageUri = Uri.fromFile(new File(imagePath));
        if(imageUri!=null)
        {
            Bitmap bitmap = decodeBitmapUri(this, imageUri);
            if (detector.isOperational() && bitmap != null)
            {
                Bitmap editedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                float scale = getResources().getDisplayMetrics().density;
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.rgb(255, 61, 61));
                paint.setTextSize((int) (14 * scale));
                paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3f);
                Canvas canvas = new Canvas(editedBitmap);
                canvas.drawBitmap(bitmap, 0, 0, paint);
                Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
                SparseArray<Face> faces = detector.detect(frame);

                if (faces.size() == 0)
                {
                    // new DisplayDebugLogs(context).execute("\nscanFaces:no faces"+faces.size());
                    deleteCapturedImgFile();
                    reCapturingFrame();

                } else
                {
                    // new DisplayDebugLogs(context).execute("\nscanFaces:detected faces"+faces.size());
                    uploadMetricsFile();
                }
                faces.clear();
                paint.clearShadowLayer();
                paint.reset();
                editedBitmap.recycle();

                System.gc();
                // new DisplayDebugLogs(context).execute("\nscanFaces:editedBitmap recycle");

            } else {

                // new DisplayDebugLogs(context).execute("\nscanFaces:bitmap is null");
                deleteCapturedImgFile();
                detector = new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        //.setMode(FaceDetector.FAST_MODE)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                        .build();
                reCapturingFrame();
            }
            bitmap.recycle();
            System.gc();
            // new DisplayDebugLogs(context).execute("\nscanFaces:bitmap recycle");

        }else
        {
            //  new DisplayDebugLogs(context).execute("\nscanFaces:imageUri is null");
            deleteCapturedImgFile();
            reCapturingFrame();
        }

    }

    private void uploadMetricsFile()
    {
        if (new NetworkModel().isInternet(context))
        {

            Intent startIntent = new Intent(context, UploadMetricsFileService.class);
            startIntent.putExtra("receiver", actionReceiver);
            startIntent.putExtra("file_path", imgPath);
            startService(startIntent);
            //  new DisplayDebugLogs(context).execute("\nuploadMetricsFile:");
            //showShortMsg("Uploading Metrics...");
        } else {
            //Toast.makeText(context,getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
            failureResponse(getString(R.string.no_internet));
            new DisplayDebugLogs(context).execute("\nuploadMetricsFile:no internet connection");
        }
    }



    protected void successResponse(String msg)
    {

        deleteCapturedImgFile();
        reCapturingFrame();
    }

    protected void failureResponse(String msg)
    {

        deleteCapturedImgFile();
        reCapturingFrame();
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 640;
        int targetH = 480;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
    }


    private void deleteCapturedImgFile() {
        //delete the file
        try {
            File file = new File(imgPath);
            if (file.exists()) {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private boolean checkCampaignScheduleDuration()
    {

        if(mediaInfo.getScheduleType()==CONTINUOUS_PLAY)
        {
            //if continuos play
            return true;
        }else
        {
            String scheduleFrom = mediaInfo.getScheduleFrom();
            String scheduleTo = mediaInfo.getScheduleTo();
            SimpleDateFormat sdf = new SimpleDateFormat(LOCAL_SCHEDULE_DATE_FORMAT);

            if(scheduleFrom!=null && scheduleTo!=null)
            {
                try
                {
                    long currentTimeInMs = Calendar.getInstance().getTimeInMillis();
                    long scheduleFromTimeInMs = sdf.parse(scheduleFrom).getTime();
                    long scheduleToInMs = sdf.parse(scheduleTo).getTime();
                    if(scheduleFromTimeInMs<= currentTimeInMs && scheduleToInMs>currentTimeInMs)
                    {
                      //check how much duration can the campaign be played
                        long campaignPlayRemainDuration = (scheduleToInMs-currentTimeInMs);
                        if(campaignPlayRemainDuration < (mediaInfo.getDuration()*1000))
                        {
                           //campaign play duration is less than available time so reduce the duration
                            mediaInfo.setDuration(TimeUnit.MILLISECONDS.toSeconds(campaignPlayRemainDuration));
                        }

                        //if scheduling is weekly schedule check for day
                        if(mediaInfo.getScheduleType()== SCHEDULE_WEEKLY_PLAY )
                        {

                            return canPlayWeeklySchedule();

                        }else
                        {
                            return true;
                        }


                    }else
                    {
                        //camapaign schedule time has been expired
                        return false;
                    }
                }catch(ParseException e)
                {
                    return true;
                }

            }else
            {
                return true;
            }

        }


    }

    //check and interrupt to play high priority schedule campaign
    public void scheduleCheckForInterruption()
    {
        if(prioritySchedules.size()>=1 && isPriorityTaskPlaying==false) {
            MediaInfo priorityScheduleInfo = prioritySchedules.get(0);

            if (mediaInfo != null ) {


                //if currĕntly playing campaign priority is less than the priority schedule then stop the play
                if(((mediaInfo.getScheduleType() == CONTINUOUS_PLAY && mediaInfo.getCampaignPriority() < priorityScheduleInfo.getScheduleCampaignPriority()) ||
                        (mediaInfo.getScheduleType() != CONTINUOUS_PLAY && mediaInfo.getScheduleCampaignPriority()< priorityScheduleInfo.getScheduleCampaignPriority())) && mediaInfo.getIsForcePlay()==false)
                {
                    skipCurrentPlayingMedia();
                }

            } else if (prioritySchedules.size() >= 1) {
                playAds(true);

            }
        }
    }

    private void checkAndUpdateNextSchedule()
    {
        try {

            if (mediaInfo != null) {


                String nextScheduleAt = null;Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat(LOCAL_SCHEDULE_DATE_FORMAT);
                SimpleDateFormat timeFormat = new SimpleDateFormat(LOCAL_SCHEDULE_TIME_FORMAT);
                SimpleDateFormat dateFormat = new SimpleDateFormat(LOCAL_SCHEDULE_ONLY_DATE_FORMAT);

                switch (mediaInfo.getScheduleType()) {
                    case SCHEDULE_MINUTE_PLAY:
                        calendar.add(Calendar.MINUTE, 1);
                        nextScheduleAt = sdf.format(calendar.getTime());
                        break;
                    case SCHEDULE_HOUR_PLAY:
                        calendar.add(Calendar.HOUR, 1);
                        nextScheduleAt = sdf.format(calendar.getTime());
                        break;
                    case SCHEDULE_DAILY_PLAY:
                        Cursor cursor = CampaignsDBModel.getScheduleInfo(context,mediaInfo.getScheduleLocalId());
                        if(cursor!=null && cursor.moveToFirst())
                        {
                           String scheduleFrom = cursor.getString(cursor.getColumnIndex(CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_FROM));
                           Calendar scheduleFromDateTimeCalendar = Calendar.getInstance();
                           scheduleFromDateTimeCalendar.setTime(sdf.parse(scheduleFrom));
                           calendar.add(Calendar.HOUR,24);//add day
                           nextScheduleAt = dateFormat.format(calendar.getTime())+" "+timeFormat.format(scheduleFromDateTimeCalendar.getTime());

                        }
                        break;
                    case SCHEDULE_MONTHLY_PLAY:
                        cursor = CampaignsDBModel.getScheduleInfo(context,mediaInfo.getScheduleLocalId());
                        if(cursor!=null && cursor.moveToFirst())
                        {
                            String scheduleFrom = cursor.getString(cursor.getColumnIndex(CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_FROM));
                            Calendar scheduleFromDateTimeCalendar = Calendar.getInstance();
                            scheduleFromDateTimeCalendar.setTime(sdf.parse(scheduleFrom));
                            //scheduleFromDateTimeCalendar.add(Calendar.MONTH,1);//add one month

                            int scheduleFromMonth = (scheduleFromDateTimeCalendar.get(Calendar.MONTH)+1);
                            String modifiedScheduleFromMonth = String.valueOf(scheduleFromMonth);
                            if(scheduleFromMonth<10)
                            {
                                modifiedScheduleFromMonth = "0"+scheduleFromMonth;
                            }
                            String currentOccurredDateTime = calendar.get(Calendar.YEAR)+"-"+modifiedScheduleFromMonth+"-"+scheduleFromDateTimeCalendar.get(Calendar.DAY_OF_MONTH)+
                                    " "+timeFormat.format(scheduleFromDateTimeCalendar.getTime());

                            scheduleFromDateTimeCalendar.setTime(sdf.parse(currentOccurredDateTime));


                            scheduleFromDateTimeCalendar.add(Calendar.MONTH,1);

                            while(scheduleFromDateTimeCalendar.getTime().compareTo(calendar.getTime())<=0)
                            {
                                scheduleFromDateTimeCalendar.add(Calendar.MONTH,1);//add one more month,,untill the next schedule is greater than current time
                            }

                            nextScheduleAt =sdf.format(scheduleFromDateTimeCalendar.getTime());

                        }
                        break;

                    case SCHEDULE_YEARLY_PLAY:
                        cursor = CampaignsDBModel.getScheduleInfo(context,mediaInfo.getScheduleLocalId());
                        if(cursor!=null && cursor.moveToFirst())
                        {
                            String scheduleFrom = cursor.getString(cursor.getColumnIndex(CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_FROM));
                            Calendar scheduleFromDateTimeCalendar = Calendar.getInstance();
                            scheduleFromDateTimeCalendar.setTime(sdf.parse(scheduleFrom));
                            //scheduleFromDateTimeCalendar.add(Calendar.MONTH,1);//add one month

                            int scheduleFromMonth = (scheduleFromDateTimeCalendar.get(Calendar.MONTH)+1);
                            String modifiedScheduleFromMonth = String.valueOf(scheduleFromMonth);
                            if(scheduleFromMonth<10)
                            {
                                modifiedScheduleFromMonth = "0"+scheduleFromMonth;
                            }
                            String currentOccurredDateTime = calendar.get(Calendar.YEAR)+"-"+modifiedScheduleFromMonth+"-"+scheduleFromDateTimeCalendar.get(Calendar.DAY_OF_MONTH)+
                                    " "+timeFormat.format(scheduleFromDateTimeCalendar.getTime());

                            scheduleFromDateTimeCalendar.setTime(sdf.parse(currentOccurredDateTime));


                            scheduleFromDateTimeCalendar.add(Calendar.YEAR,1);

                            while(scheduleFromDateTimeCalendar.getTime().compareTo(calendar.getTime())<=0)
                            {
                                scheduleFromDateTimeCalendar.add(Calendar.YEAR,1);//add one more month,,untill the next schedule is greater than current time
                            }

                            nextScheduleAt =sdf.format(scheduleFromDateTimeCalendar.getTime());

                        }
                        break;

                    case SCHEDULE_WEEKLY_PLAY:
                        cursor = CampaignsDBModel.getScheduleInfo(context,mediaInfo.getScheduleLocalId());
                        if(cursor!=null && cursor.moveToFirst()) {
                            String scheduleFrom = cursor.getString(cursor.getColumnIndex(CampaignsDBModel.SCHEDULE_CAMPAIGNS_SCHEDULE_FROM));
                            Calendar scheduleFromDateTimeCalendar = Calendar.getInstance();
                            scheduleFromDateTimeCalendar.setTime(sdf.parse(scheduleFrom));
                            calendar.setTime(sdf.parse((dateFormat.format(calendar.getTime())+" "+timeFormat.format(scheduleFromDateTimeCalendar.getTime()))));
                            //get schedule week days
                            JSONObject infoJson = new JSONObject(cursor.getString(cursor.getColumnIndex(CampaignsDBModel.SCHEDULE_TABLE_ADDITIONAL_INFO)));
                            JSONArray weekDaysArray = infoJson.getJSONArray("weekDays");
                            ArrayList<Integer> checkWeekDaysArray = new ArrayList(weekDaysArray.length());
                            for(int arrayIndex=0;weekDaysArray.length()>arrayIndex;arrayIndex++)
                            {
                                checkWeekDaysArray.add(weekDaysArray.getInt(arrayIndex));
                            }
                            int checkedDays=0;
                            do
                            {
                                calendar.add(Calendar.HOUR,24);
                                int nextCheckWeekDay = DateTimeModel.getDayOfWeek(calendar);
                                if(checkWeekDaysArray.contains(nextCheckWeekDay))
                                {
                                    nextScheduleAt = sdf.format(calendar.getTime());
                                    break;
                                }
                             }
                            while(checkedDays<=7);
                        }
                }



                if (nextScheduleAt != null) {

                    CampaignsDBModel.updateCampaignNextSchedule(context, mediaInfo.getScheduleLocalId(), nextScheduleAt);
                }
            }
        }catch(Exception e)
        {

            e.printStackTrace();
        }
    }

    private boolean canPlayWeeklySchedule()
    {
        String additionalInfo = mediaInfo.getScheduleAdditionalInfo();
        if(additionalInfo!=null)
        {
            try
            {
                JSONObject infoJson = new JSONObject(additionalInfo);
                JSONArray weekDaysArray = infoJson.getJSONArray("weekDays");
                 int today = DateTimeModel.getDayOfWeek(Calendar.getInstance());

                for(int arrayIndex=0;weekDaysArray.length()>arrayIndex;arrayIndex++)
                {
                   if(weekDaysArray.getInt(arrayIndex)==today)
                   {
                       return true;
                   }
                }

                return false;
            }catch(JSONException e)
            {
                return false;
            }

        }else
        {
            return false;
        }
    }

    private void registerSoftIotReceiver()
    {
        if(softIotFCMReceiver==null)
        {
            softIotFCMReceiver = new SoftIotFCMReceiver();
            IntentFilter intentFilter = new IntentFilter(SoftIotFCMReceiver.ACTION);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(softIotFCMReceiver,intentFilter);

        }

    }

    private void unregisterSoftIotReceiver()
    {
        if(softIotFCMReceiver!=null)
        {
            try
            {
                unregisterReceiver(softIotFCMReceiver);
            }catch (Exception e)
            {

            }finally {
                softIotFCMReceiver=null;
            }
        }
    }

    private void startRenderRSSFeed()
    {
        rssFeedsTimer = new Timer();
        RenderRSSFeeds renderRSSFeeds = new RenderRSSFeeds(this,this,new Handler());
        Thread thread = new Thread(renderRSSFeeds);
        thread.start();
    }

    private void checkAndEnableHotSpot(){
        if(Utility.canEnableHotSpot(context)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                checkForLocationSettingsAndSaveHotSpot();
            }else{
                NetworkModel.changeWifiHotspotState(context,true);
            }
        }
    }

    private void checkForLocationSettingsAndSaveHotSpot(){
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);


        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                NetworkModel.changeWifiHotspotState(context,true);

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(DisplayLocalFolderAds.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }else
                {
                    Toast.makeText(context,"Unable to enable location"+e.getMessage(),Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    //return is resume from last played position
    private boolean checkAndResumePlayCampaign() {
        if(isActivityRestarted) {
            SignageServe.initLastMediaPlayed();
            //make it false
            isActivityRestarted = false;
            //check for last played campaign if it is not zero set the previous position to last played position and
            //call playNext add
            if(SignageServe.lasMediaPlayedPosition >= 1 && processingFiles!=null  && processingFiles.size() >= SignageServe.lasMediaPlayedPosition) {
                //call play add
                prevPosition = (SignageServe.lasMediaPlayedPosition-1);
                playAds(false);
                isResumePlaying = true;
                return true;
            }
        }
        return false;
    }

}
