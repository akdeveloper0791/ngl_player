package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.files.FileMetadata;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaignResultReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadProgressFileInfo;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.GetCampaignInfoDbx;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.GetCampaignInfoFromLocalServer;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.LocalFolderBuilder;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignTriggerService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.DownloadMediaInfo;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.drop_box.DropboxClientFactory;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.GCNotification;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.NotificationRx;
import com.ibetter.www.adskitedigi.adskitedigi.model.NetworkModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaignResultReceiver.DBX_RESOURCE_FILE_CHUNK_DOWNLOAD_SUCCESS;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaignResultReceiver.DBX_RESOURCE_FILE_DOWNLOAD_SUCCESS;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver.DOWNLOAD_CAMPAIGN_INFO_API_RESPONSE;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.DOWNLOAD_ERROR;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.DOWNLOAD_SUCCESS;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.GET_DOWNLOADING_FILES;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.INIT_DOWNLOAD;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.REMOVE_ALL_PROGRESS;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.REMOVE_PROGRESS;
import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.DOWNLOAD_CAMPAIGN_FRONT_SERVICE_NOTIFY_ID;
import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID;
import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_NOTIFY_ID;

public class DownloadCampaignsService extends Service implements DownloadCampaignResultReceiver.CallBack
{
    public static boolean isServiceOn = false;
    private Context context;
    private LinkedHashMap<String, GCModel> pendingCampaigns = new LinkedHashMap<>();
    private String inProgressCampaign;
    private ArrayList<DownloadMediaInfo> downloadingFiles = new ArrayList<>();

    public static DownloadCampaignResultReceiver downloadCampaignResultReceiver;
    private NotificationRx notificationRx;

    private DownloadProgressFileInfo downloadProgressFileInfo;
    private int currentUploadingResourceFilePosition = 0;

    private IOUtil.ProgressListener progressListener;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder downloadCampaignUploadProgressNotification;

    private long CURRENT_DOWNLOAD_CHUNK_SIZE  = 4L << 20 ;//initially it is 4MB

    public static final String DOWNLOAD_CAMPAIGNS_PATH = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getPath()+"/Nearby";


    public static final String UPDATE_PROGRESS_RX = "com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.UPDATE_PROGRESS_RX";
    public static final String IN_PROGRESS="IN_PROGRESS";
    public static final String ERROR="error";
    private   String serviceStatus=IN_PROGRESS;
    private   String errorMsg;

    public static boolean isAutoSyc;
    private int autoRetry=0;

    private DbxDownloader<FileMetadata> downloadedInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceOn = true;

        context = DownloadCampaignsService.this;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        initRx();

        registerNotificationRX();

        checkAndStartForegroundNotification();


        downloadCampaignUploadProgressNotification = new GCNotification().initCampaignResourceUploadProgress(context,
                "Downloading Campaign","Downloading campaign resource","");

    }

    private void initRx()
    {
      downloadCampaignResultReceiver = new DownloadCampaignResultReceiver(new Handler(), DownloadCampaignsService.this, this);
    }

    private void checkAndStartForegroundNotification()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            startForeground(DOWNLOAD_CAMPAIGN_FRONT_SERVICE_NOTIFY_ID, GCNotification.campaignDownloadServiceNotification(context,
                    "Downloading campaigns"));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        isAutoSyc=intent.getExtras().getBoolean("is_auto_sync",false);

        Log.i("intent campList","campList:: isAutoSyc: "+isAutoSyc);

        if(isAutoSyc)
        {
            ArrayList<GCModel> campList = (ArrayList<GCModel>) intent.getExtras().getSerializable("campList");

            if(campList!=null&&campList.size()>0) {

                for (GCModel model : campList) {
                    addCampaignToList(model);
                }

            }else
            {
                finishService();
            }
        }else
        {
            GCModel model = (GCModel) intent.getSerializableExtra("model");

            addCampaignToList(model);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        isServiceOn = false;
        unRegisterNotificationRx();
        stopForeground(true);
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //implements action receiver callbacks
    public void initDownloadCampaign(Bundle campaignValues) {

        //get the campaign name and add it to the pending list
        String campaignName = campaignValues.getString("campaignName");

        if (pendingCampaigns.containsKey(campaignName)) {
            Toast.makeText(context, "Campaign is already added to the upload list", Toast.LENGTH_SHORT).show();
        }
        else
        {
            GCModel model = (GCModel) campaignValues.getSerializable("model");
            addCampaignToList(model);
        }
    }


    private  void addCampaignToList(GCModel model) {

        if (pendingCampaigns.containsKey(model.getCampaignName())) {
            if(!isAutoSyc) {
                Toast.makeText(context, "Campaign is already in pending list", Toast.LENGTH_SHORT).show();
            }

        } else
        {
            pendingCampaigns.put(model.getCampaignName(), model);

            if (inProgressCampaign == null)
            {
                //start uploading campaign
                checkAndDownloadCampaign();
            }

            //send init download
             sendInitDownloadInfo(model.getCampaignName());
            if(!isAutoSyc) {
                Toast.makeText(context, "Campaign has been added to downloading list", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void resetCampaignVariables()
    {
        currentUploadingResourceFilePosition = 0;
        downloadingFiles.clear();
        downloadProgressFileInfo = null;

    }

    private void checkAndDownloadCampaign() {


        Iterator<Map.Entry<String, GCModel>> iterator = pendingCampaigns.entrySet().iterator();

        if (iterator.hasNext()) {

            inProgressCampaign = iterator.next().getKey();

            resetCampaignVariables();

            prepareDownloading();

        } else {
            //no campaigns are there to upload
            sendRemoveProgress();
            finishService();
        }
    }

    private void prepareDownloading() {
        //get campaign path and read file
        String infoText = pendingCampaigns.get(inProgressCampaign).getInfo();

        if (infoText != null) {
            //prepare upload files from json
            try
            {
                JSONObject infoObj = new JSONObject(infoText);
                String type = infoObj.getString("type");

                if (type.equalsIgnoreCase("multi_region")) {
                    processMultiRegFilesToUpload(infoObj.getJSONArray("regions"));
                }
                else
                {
                    addForRegion(infoObj, "Single");
                }

                //check for bg audio file
                if (infoObj.has("bg_audio")) {
                    downloadingFiles.add(new DownloadMediaInfo(pendingCampaigns.get(inProgressCampaign).getStoreLocation(),
                            pendingCampaigns.get(inProgressCampaign).getSavePath()+infoObj.getString("bg_audio")));
                    //downloadingFiles.add(infoObj.getString("bg_audio"));
                }

                //add thumb file
                String thumbFileName = getString(R.string.do_not_display_media)+"-"+getString(R.string.media_thumbnail)+"-"+inProgressCampaign+ getString(R.string.media_thumbnail_extention);

                downloadingFiles.add(new DownloadMediaInfo(pendingCampaigns.get(inProgressCampaign).getStoreLocation(),
                        pendingCampaigns.get(inProgressCampaign).getSavePath()+thumbFileName));

                initDownload();


                //start Upload campaignFile
                //initUpload(infoText);
            } catch (JSONException e) {

                //downloading info error
                handleCampaignDownloadingError("invalid campaign"+e.getMessage());

            }
        }
        else
        {
            if(isAutoSyc)
            {

                skipCampaign();

            }else {
                //downloading info error
                handleCampaignDownloadingError("invalid campaign");
            }

        }

    }


    //process multi region files
    private void processMultiRegFilesToUpload(JSONArray regions) throws JSONException {
        for (int i = 0; i < regions.length(); i++) {
            addForRegion(regions.getJSONObject(i), "multi");
        }
    }

    private void addForRegion(JSONObject region, String type) throws JSONException {
        if (!region.getString("type").equalsIgnoreCase("text") && !region.getString("type").equalsIgnoreCase("Url")
                && !region.getString("type").equalsIgnoreCase("default")) {
            if (type.equalsIgnoreCase("Single")) {
                if(region.has("is_content_path") && region.getBoolean("is_content_path"))
                {
                    downloadingFiles.add(new DownloadMediaInfo(region.getInt("content_store_location"),
                            region.getString("content_path")));
                }else
                {
                    String media = region.getString("resource");
                    if (!media.equalsIgnoreCase("default"))
                        downloadingFiles.add(new DownloadMediaInfo(pendingCampaigns.get(inProgressCampaign).getStoreLocation(),
                                pendingCampaigns.get(inProgressCampaign).getSavePath()+media));

                }

            } else {
                if(region.has("is_content_path") && region.getBoolean("is_content_path"))
                {
                    downloadingFiles.add(new DownloadMediaInfo(region.getInt("content_store_location"),
                            region.getString("content_path")));
                }else {
                    String media = region.getString("media_name");
                    if (!media.equalsIgnoreCase("default"))
                        downloadingFiles.add(new DownloadMediaInfo(pendingCampaigns.get(inProgressCampaign).getStoreLocation(),
                                pendingCampaigns.get(inProgressCampaign).getSavePath()+media));
                }
            }

        }
    }

    private void displayProgressNotification()
    {
        downloadCampaignUploadProgressNotification.setProgress(0,0,true);
        downloadCampaignUploadProgressNotification.setContentTitle(inProgressCampaign);
        downloadCampaignUploadProgressNotification.setContentText("Initializing....");
        mNotificationManager.notify(DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_NOTIFY_ID,downloadCampaignUploadProgressNotification.build());
    }

    private void initDownload()
    {

      if(pendingCampaigns.get(inProgressCampaign).getStoreLocation()==1)//local
        {
            displayProgressNotification();
            Thread downloadThread = new GetCampaignInfoFromLocalServer(context,inProgressCampaign, DownloadCampaignsService.this, downloadCampaignResultReceiver, pendingCampaigns.get(inProgressCampaign).getSavePath());
            downloadThread.start();
        }
        else if(pendingCampaigns.get(inProgressCampaign).getStoreLocation()==2)//dropbox
        {
            displayProgressNotification();
            Thread downloadThread = new GetCampaignInfoDbx(inProgressCampaign, DownloadCampaignsService.this, downloadCampaignResultReceiver, pendingCampaigns.get(inProgressCampaign).getSavePath());
            downloadThread.start();
        }else
        {

            sendRemoveAllProgress();
            finishService();

        }
    }

    private void finishService()
    {
        stopDownload();

        removeDownloadProgressNotification();
        removeNotification(DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID);



        if(isAutoSyc)
        {
            if(AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver!=null)
            {
                AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver.send(DOWNLOAD_CAMPAIGN_INFO_API_RESPONSE,null);
            }

        }
        else
        {
            AutoDownloadCampaignModel.checkRestartAutoCampaignDownloadService(context);
        }
        stopSelf();
    }

    @Override
    public void initDownloadApiError(Bundle values) {

        Log.i("inProgressCampaign",inProgressCampaign+"::init"+values.getString("campaign_name"));
        if(inProgressCampaign!=null&&inProgressCampaign.equalsIgnoreCase(values.getString("campaign_name"))) {
            handleCampaignDownloadingError(values.getString("status"));

        }
    }

    @Override
    public void initDownloadApiResponse(Bundle values)
    {

        if(inProgressCampaign!=null&&inProgressCampaign.equalsIgnoreCase(values.getString("campaign_name")))
        {

          downLoadResourceFile(downloadingFiles.get(0));

        }
    }

    private void downLoadResourceFile(DownloadMediaInfo downloadingFile) {
        //init current uploading file info
        downloadProgressFileInfo = new DownloadProgressFileInfo();

       try {

           downloadProgressFileInfo.setCurrentDownloadingResourceFileName(downloadingFile.getMediaName());
           downloadProgressFileInfo.setContext(context);
           downloadProgressFileInfo.setTotalFiles(downloadingFiles.size());
           downloadProgressFileInfo.setCurrentDownloadingResourceFilePosition(currentUploadingResourceFilePosition+1);
           downloadProgressFileInfo.setCampaignName(inProgressCampaign);
           downloadProgressFileInfo.setDownloadMediaInfo(downloadingFile);

           UpdateUploadResourceProgressNotification();

           if(downloadProgressFileInfo.isResourceExists())
           {
               //downloading success
               downloadCampaignResultReceiver.send(DBX_RESOURCE_FILE_DOWNLOAD_SUCCESS,null);
           }else
           {
               directDownloadResource();
           }


       }catch (FileNotFoundException ex)
       {
           //error in downloading resource file
           handleCampaignDownloadingError("Unable to create file, please try again");
       }catch (RuntimeException ex)
       {
           handleCampaignDownloadingError("Unable to create file, please try again");
       }

    }

    private void checkAndUploadResourceFile() {


        ++currentUploadingResourceFilePosition;

        if (downloadingFiles.size() > currentUploadingResourceFilePosition)
        {
            downLoadResourceFile(downloadingFiles.get(currentUploadingResourceFilePosition));
        }
        else
        {
            //create text file info


            if(createCampaignInfoFile())
            {

                if(inProgressCampaign.equalsIgnoreCase(getString(R.string.dndm_ss_ticker_txt)))
                {
                    processScrollTickerTextFile();
                }

                onDownloadComplete(inProgressCampaign);

                pendingCampaigns.remove(inProgressCampaign);

                if(isAutoSyc) {
                    autoRetry = 0;
                }

                checkAndDownloadCampaign();//check and download next campaign
            }

        }
    }

    //update campaign download status
    private boolean createCampaignInfoFile()
    {
        if(pendingCampaigns!=null&&pendingCampaigns.get(inProgressCampaign)!=null) {
            // Uri infoUri = new GalleryMediaModel(context).getTextToFile(pendingCampaigns.get(inProgressCampaign).getInfo(), inProgressCampaign, context);
            boolean infoUri = CampaignsDBModel.setCampaignDownloadedTrue(context, pendingCampaigns.get(inProgressCampaign).getServerId());
            Log.d("DownloadCampaign", "Inside download campaign service info uri " + infoUri);
            if (infoUri == false) {

                //handle eerror
                handleCampaignDownloadingError("Unable to set download status");

            }

            return infoUri;
        }else
        {
            finishService();
            return false;
        }
    }

    private void directDownloadResource() {


        if(downloadProgressFileInfo.getStoreLocation()==1)
        {

            Thread downloadThread = new DownloadLocalServerCampaignResourceFile(inProgressCampaign);
            downloadThread.start();

        }else  if(downloadProgressFileInfo.getStoreLocation()==2)
        {

            initProgressListener();

            Thread downloadThread = new DownloadCampaignResourceFile(inProgressCampaign);
            downloadThread.start();

        }else
        {
            sendRemoveAllProgress();
            finishService();
        }

    }


    private void initProgressListener() {

        downloadProgressFileInfo.setDownloadProgressInitTime(System.currentTimeMillis());
        //re initializing progress uploaded bytes
        downloadProgressFileInfo.setProgressListenerDownloadedBytes(0);

        progressListener = new IOUtil.ProgressListener() {
            @Override
            public void onProgress(long bytesDownloaded)
            {

                downloadProgressFileInfo.setProgressListenerDownloadedBytes(bytesDownloaded);

                downloadProgressFileInfo.updateOnProgress(downloadCampaignUploadProgressNotification,mNotificationManager);

            }
        };
    }

    private class DownloadCampaignResourceFile extends Thread
    {

        private String campaignName;

        private DownloadCampaignResourceFile(String campaignName)
        {
            this.campaignName=campaignName;
        }

        public void run()
        {

            String resourceFileName = downloadProgressFileInfo.currentDownloadingResourceFileName;

            downloadFile(resourceFileName);

        }

        private void downloadFile(String resourceFileName) {

            Exception mException = null;
            try {

                {
                    Log.d("DownloadCampaign","Current downloading path "+downloadProgressFileInfo.getMediaDownloadPath());
                     downloadedInfo = DropboxClientFactory.getClient().files().downloadBuilder(downloadProgressFileInfo.getMediaDownloadPath())
                            .range(downloadProgressFileInfo.downloadedBytes,CURRENT_DOWNLOAD_CHUNK_SIZE)
                            .start();


                    downloadProgressFileInfo.setSize(downloadedInfo.getResult().getSize());


                    FileMetadata metadata = downloadedInfo.download(downloadProgressFileInfo.fileOutPutStream, progressListener);
                    //downloaded += length;



                }

            } catch (DbxException | IOException e) {
                mException = e;

            } catch (Exception e) {
                mException = e;
            } finally {

                if(inProgressCampaign!=null&&inProgressCampaign.equalsIgnoreCase(campaignName)) {


                    if (mException != null) {

                        if (downloadCampaignResultReceiver != null) {
                            mException.printStackTrace();

                            downloadProgressFileInfo.onChunkDownloadError();

                            if (mException instanceof NetworkIOException) {
                                //handle network io exceptions
                                chunkDownloadResourceNetworkIoException();
                            }
                            else {
                                downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.DBX_RESOURCE_FILE_DOWNLOAD_FAILURE, null);
                            }
                        }
                    }
                    else
                    {
                        //success case
                        if (downloadCampaignResultReceiver != null)
                        {

                            downloadProgressFileInfo.onChunkDownloadSuccess();

                            downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.DBX_RESOURCE_FILE_CHUNK_DOWNLOAD_SUCCESS, null);

                        }
                    }
                }
            }
        }
    }

    public void resourceFileChunkDownloadSuccess()
    {
        Log.i("downloadedBytes",""+downloadProgressFileInfo.downloadedBytes);

        Log.i("downloadsize",""+downloadProgressFileInfo.size);
        if(downloadProgressFileInfo.downloadedBytes >= downloadProgressFileInfo.size)
        {
            //downloading success
            downloadCampaignResultReceiver.send(DBX_RESOURCE_FILE_DOWNLOAD_SUCCESS,null);
        }
        else
        {
            try {
                //check and upload the next chunk
                long remainingBytes = (downloadProgressFileInfo.size - downloadProgressFileInfo.downloadedBytes);

                int timeTaken = (int) (((downloadProgressFileInfo.downloadProgressEndTime - downloadProgressFileInfo.downloadProgressStartTime) / 1000));
                long bytesPerSec = (downloadProgressFileInfo.progressListenerDownloadedBytes / timeTaken);

               Log.d("DownloadCampaign", "Inside init chunk upload session uploaded bytes - " + downloadProgressFileInfo.downloadedBytes + "\n" +
                        "remaining bytes - " + remainingBytes + "  timeTaken -" + timeTaken + "Sec\n" +
                        "bytesPerSec - " + bytesPerSec + "\n start time - " + downloadProgressFileInfo.downloadProgressStartTime + "" +
                        "End time - " + downloadProgressFileInfo.downloadProgressEndTime + "time " + TimeUnit.MILLISECONDS.toSeconds((downloadProgressFileInfo.downloadProgressEndTime - downloadProgressFileInfo.downloadProgressStartTime)));

               CURRENT_DOWNLOAD_CHUNK_SIZE = downloadProgressFileInfo.getNextChunkToUpload();


                if (remainingBytes < CURRENT_DOWNLOAD_CHUNK_SIZE) {
                    Log.d("last bytes" ,""+ remainingBytes);
                    CURRENT_DOWNLOAD_CHUNK_SIZE = remainingBytes;

                }
                //start downloading next chunk
                directDownloadResource();
            }catch (Exception e)
            {
              if(downloadCampaignResultReceiver!=null)
              {
                  downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.DBX_RESOURCE_FILE_DOWNLOAD_FAILURE, null);
              }else
              {
                  finishService();
              }
            }
        }

    }



    private void removeDownloadProgressNotification()
    {

        mNotificationManager.cancel(DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_NOTIFY_ID);

    }

    @Override
    public void downloadResourceFileSuccess(Bundle values) {

        //rename resource filename

        downloadProgressFileInfo.renameDummyFile();

        removeDownloadProgressNotification();
        checkAndUploadResourceFile();

    }

    @Override
    public void downloadResourceFileFailure(Bundle values)
    {

        handleCampaignDownloadingError("Download Failed");

    }

    private void onDownloadComplete(String campaignName)
    {
        sendDownloadSuccess(campaignName);
        removeDownloadProgressNotification();
        sendRemoveProgress();
    }


    private void registerNotificationRX() {
        IntentFilter intentFilter = new IntentFilter(GCNotification.ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        //receiver to receive file sent events
        if (notificationRx == null) {
            notificationRx = new NotificationRx(new WeakReference<>(DownloadCampaignsService.this));
            registerReceiver(notificationRx, intentFilter);
        }
    }

    private void unRegisterNotificationRx() {
        try {
            if (notificationRx != null) {
                unregisterReceiver(notificationRx);
            }
        } catch (Exception e) {

        } finally {
            notificationRx = null;
        }
    }



    private void handleCampaignDownloadingError(String error) {

        if(isServiceOn) {
            if(isAutoSyc) {

                String thumbFileName = getString(R.string.do_not_display_media) + "-" + getString(R.string.media_thumbnail) + "-" + inProgressCampaign + getString(R.string.media_thumbnail_extention);

                if (downloadProgressFileInfo!=null&&(downloadingFiles.size() > 0) && (thumbFileName.equalsIgnoreCase(downloadProgressFileInfo.currentDownloadingResourceFileName)) &&
                !error.equals("Unable to set download status"))
                {

                  //delete thumb nail
                  deleteTempThumbNail(thumbFileName);
                  checkAndUploadResourceFile();
                 }
                  else
                  {

                    if(autoRetry<=3) {
                        ++autoRetry;

                        try {


                            Thread.sleep(2000);
                        }catch (Exception e)
                        {

                        }
                        retryCampaign();
                    }else
                    {
                        skipCampaign();
                        autoRetry=0;

                        Log.i("Dow autoRetry skipped ","Dow autoRetry skipped "+autoRetry);
                    }
                }

            }else {
                removeDownloadProgressNotification();

                serviceStatus = ERROR;

                errorMsg = error;


                String thumbFileName = getString(R.string.do_not_display_media) + "-" + getString(R.string.media_thumbnail) + "-" + inProgressCampaign + getString(R.string.media_thumbnail_extention);
                if (downloadProgressFileInfo == null) {

                    //error in retrieving file info
                    new GCNotification(context).displayErrorNotification(inProgressCampaign + " Downloaded Error", error);

                    sendDownloadErrorInfo(inProgressCampaign, error);
                } else if ((downloadingFiles.size() > 0) && (thumbFileName.equalsIgnoreCase(downloadProgressFileInfo.currentDownloadingResourceFileName))) {
                    //delete thumb nail
                    deleteTempThumbNail(thumbFileName);
                    checkAndUploadResourceFile();
                } else {


                    new GCNotification(context).displayErrorNotification(inProgressCampaign + " Downloaded Error", error);

                    sendDownloadErrorInfo(inProgressCampaign, error);
                }
            }
        }
        else
        {
            Log.i("Info","service is killed");
            finishService();
        }
    }

    public void retryCampaign()
    {
        removeNotification(DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID);
        serviceStatus=IN_PROGRESS;

        if(downloadingFiles.size()>0)
        {
            if(downloadProgressFileInfo==null)
            {
                initDownload();
            }
            else
            {
                if(downloadingFiles.size() > currentUploadingResourceFilePosition)
                {
                    checkAndRetryDownloadResourceFile();
                }
                else
                {
                    createCampaignInfoFile();
                }

            }
        }
        else
        {
            if(inProgressCampaign!=null)
            {
                prepareDownloading();
            }else
            {
                checkAndDownloadCampaign();
            }
        }
    }

    //check and retry campaign resource file download
    private void checkAndRetryDownloadResourceFile()
    {
        downloadProgressFileInfo.setProgressListenerDownloadedBytes(0);

        try {
            if(downloadProgressFileInfo.downloadedBytes<=0 || downloadProgressFileInfo.size <=0)
            {
                directDownloadResource();
            }
            else if (downloadProgressFileInfo.downloadedBytes >= downloadProgressFileInfo.size) {

               //file has been downloaded , check for next resource file
                //downloading success
               //Toast.makeText(context,"Downloaded test",Toast.LENGTH_SHORT).show();

                downloadCampaignResultReceiver.send(DBX_RESOURCE_FILE_DOWNLOAD_SUCCESS,null);
            }else
            {
                long remainingBytes = (downloadProgressFileInfo.size - downloadProgressFileInfo.downloadedBytes);



                if(remainingBytes < CURRENT_DOWNLOAD_CHUNK_SIZE)
                {
                    CURRENT_DOWNLOAD_CHUNK_SIZE = remainingBytes;

                }

                directDownloadResource();
            }


        }catch (Exception e)
        {
            //any exception auto start from starting
            checkAndDownloadCampaign();
        }
    }


    public void skipCampaign()
    {
        removeNotification(DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID);

        serviceStatus=IN_PROGRESS;

        downloadingFiles.add(new DownloadMediaInfo(pendingCampaigns.get(inProgressCampaign).getStoreLocation(),
                pendingCampaigns.get(inProgressCampaign).getSavePath()+inProgressCampaign+".txt"));

        if(downloadingFiles.size()>0)
        {
            for (DownloadMediaInfo mediaInfo:downloadingFiles)
            {
                File file=new File(DOWNLOAD_CAMPAIGNS_PATH +"/"+mediaInfo.getMediaName());
                if(file.exists())
                {
                    file.delete();

                }
            }
        }

        DownloadProgressFileInfo.deleteGarbageFiles();

        sendRemoveProgress();

        pendingCampaigns.remove(inProgressCampaign);

        checkAndDownloadCampaign();
    }

    private void removeNotification(int id)
    {
        mNotificationManager.cancel(id);
    }

    private void deleteTempThumbNail(String thumbPath)
    {

            File thumbFile = new File(DOWNLOAD_CAMPAIGNS_PATH,thumbPath);
            if (thumbFile.exists()) {
                thumbFile.delete();
            }
    }

    private void UpdateUploadResourceProgressNotification()
    {
        //update notification progress info
        downloadCampaignUploadProgressNotification.setContentTitle(inProgressCampaign);
        downloadCampaignUploadProgressNotification.setContentText(downloadProgressFileInfo.currentDownloadingResourceFileName+" ("+
                (currentUploadingResourceFilePosition+1)+"/"+downloadingFiles.size()+")");
        downloadCampaignUploadProgressNotification.setOngoing(false);

        downloadCampaignUploadProgressNotification.setProgress(100,downloadProgressFileInfo.getCurrentUploadedProgress(),
                false);
        mNotificationManager.notify(DOWNLOAD_CAMPAIGN_RESOURCE_PROGRESS_NOTIFY_ID,downloadCampaignUploadProgressNotification.build());
    }

    private void chunkDownloadResourceNetworkIoException()
    {
        //handleCampaignDownloadingError

        //check whether user has internet or not
        if(NetworkModel.isInternet(context))
        {
            //recalculate chunk and retry
            if(downloadProgressFileInfo.progressListenerDownloadedBytes>=downloadProgressFileInfo.MIN_PROGRESS_LISTENER_DOWNLOAD_BYTES)
            {
                Log.d("DownloadCampaign","INside slow internet error try reUploading ");
                //network time out
                //start chunk upload with minimum upload chunk size
                CURRENT_DOWNLOAD_CHUNK_SIZE = downloadProgressFileInfo.MIN_PROGRESS_LISTENER_DOWNLOAD_BYTES;

                //user has switched off his internet while uploading ask him retry after switching on
                handleCampaignDownloadingError("Network timeout error, please retry");

            }else
            {

                //no internet can not upload
                //user has switched off his internet while uploading ask him retry after switching on
                handleCampaignDownloadingError("No Internet connection, please switch on and retry");
            }
        }else
        {
            //user has switched off his internet while uploading ask him retry after switching on
            handleCampaignDownloadingError("No Internet connection, please switch on and retry");
        }
    }

    private  void sendInitDownloadInfo(String campaignName)
    {
        //name
        try {


            Intent intent = new Intent(UPDATE_PROGRESS_RX);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("action",INIT_DOWNLOAD);

            intent.putExtra("name", campaignName);
            sendBroadcast(intent);
        }catch (Exception E)
        {
            E.printStackTrace();
        }
    }

    private  void sendRemoveAllProgress()
    {
        //name
        try {


            Intent intent = new Intent(UPDATE_PROGRESS_RX);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("action",REMOVE_ALL_PROGRESS);

            sendBroadcast(intent);
        }catch (Exception E)
        {
            E.printStackTrace();
        }
    }

    private  void sendRemoveProgress()
    {
        //name
        try
        {
            Intent intent = new Intent(UPDATE_PROGRESS_RX);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("action",REMOVE_PROGRESS);
            intent.putExtra("name", inProgressCampaign);
            sendBroadcast(intent);

        }catch (Exception E)
        {
            E.printStackTrace();
        }
    }


    private  void sendDownloadErrorInfo(String campaignName,String error)
    {
        try {
            Intent intent = new Intent(UPDATE_PROGRESS_RX);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("action",DOWNLOAD_ERROR);
            intent.putExtra("name", campaignName);
            intent.putExtra("error", error);
            sendBroadcast(intent);
        }catch (Exception E)
        {
            E.printStackTrace();
        }
    }

    private  void sendDownloadSuccess(String campaignName)
    {
        //name
        try {


            Intent intent = new Intent(UPDATE_PROGRESS_RX);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("action",DOWNLOAD_SUCCESS);

            intent.putExtra("name", campaignName);
            sendBroadcast(intent);
        }catch (Exception E)
        {
            E.printStackTrace();
        }
    }

    @Override
    public void requestForDownloadingCampaigns()
    {
        try
        {
            Intent intent = new Intent(UPDATE_PROGRESS_RX);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("action", GET_DOWNLOADING_FILES);
            intent.putExtra("status", serviceStatus);
            intent.putExtra("campaign_name", inProgressCampaign);

            if (downloadProgressFileInfo != null) {
                intent.putExtra("progress", downloadProgressFileInfo.getCurrentUploadedProgress());
                intent.putExtra("resource_name", downloadProgressFileInfo.currentDownloadingResourceFileName);
                intent.putExtra("total_files", downloadingFiles.size());

            }

            if (serviceStatus.equalsIgnoreCase(ERROR)) {
                intent.putExtra("error_msg", errorMsg);
            }else
            {
                intent.putExtra("position", downloadProgressFileInfo.currentDownloadingResourceFilePosition);
            }

            intent.putExtra("status", serviceStatus);

            intent.putExtra("files_list", new ArrayList<String>(pendingCampaigns.keySet()));

            sendBroadcast(intent);
        }
        catch (Exception E)
        {
            E.printStackTrace();
        }
    }

    @Override
    public void stopService(Bundle values) {

        finishService();
    }

    @Override
    public void interruptService(Bundle values) {

        try {

            sendRemoveAllProgress();



            if(inProgressCampaign!=null) {
                downloadingFiles.add(new DownloadMediaInfo(pendingCampaigns.get(inProgressCampaign).getStoreLocation(),
                        pendingCampaigns.get(inProgressCampaign).getSavePath()+inProgressCampaign + ".txt"));

            }

            if (downloadingFiles.size() > 0) {
                for (DownloadMediaInfo mediaInfo : downloadingFiles) {
                    File file = new File(DOWNLOAD_CAMPAIGNS_PATH + "/" + mediaInfo.getMediaName());
                    if (file.exists()) {
                        file.delete();
                        Log.i("file", mediaInfo.getMediaName() + "file is deleted");
                    }
                }
            }

            DownloadProgressFileInfo.deleteGarbageFiles();
            Log.i("info", "finish service");
            stopDownload();
            removeDownloadProgressNotification();
            removeNotification(DOWNLOAD_CAMPAIGN_RESOURCE_FAIL_NOTIFY_ID);

            pendingCampaigns.clear();
        }catch (Exception E)
        {
            E.printStackTrace();

        }finally {
            stopSelf();
        }

    }

    public void stopDownload()
    {
        if (downloadedInfo != null)
        {
            try
            {
                downloadedInfo.close();
                downloadedInfo = null;
            }
            catch (Exception e)
            {
                downloadedInfo = null;
                e.printStackTrace();
            }
        }
    }

    private void processScrollTickerTextFile()
    {
        try
        {

            String fileText = pendingCampaigns.get(inProgressCampaign).getInfo();

            Log.i("fileText",""+fileText);

            if (fileText != null)
            {
                JSONObject jsonObject = new JSONObject(fileText);

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
                editor.putString(getString(R.string.scroll_text_bg_color),bgColor);
                editor.putString(getString(R.string.scroll_text_text_color),textColor);
                editor.putInt(getString(R.string.scroll_text_text_size),textSize);
                editor.putString(getString(R.string.scroll_text_updated_at),pendingCampaigns.get(inProgressCampaign).getUpdatedAt());
                editor.putString(getString(R.string.local_scroll_text),text);
                editor.putBoolean(getString(R.string.local_scroll_text_bold),isBold);
                editor.putBoolean(getString(R.string.local_scroll_text_italic),isItalic);

                editor.commit();

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private class DownloadLocalServerCampaignResourceFile extends Thread
    {

        private String campaignName;

        private DownloadLocalServerCampaignResourceFile(String campaignName)
        {
            this.campaignName=campaignName;
        }

        public void run()
        {
            String resourceFileName = downloadProgressFileInfo.currentDownloadingResourceFileName;
            downloadLocalFile(resourceFileName);
        }

        private void downloadLocalFile(String resourceFileName) {

            Exception mException = null;  boolean proceedDownload=false;
            try {

                Log.i("resourceFileName",resourceFileName);

                downloadProgressFileInfo.setDownloadProgressInitTime(System.currentTimeMillis());
                //re initializing progress uploaded bytes
                downloadProgressFileInfo.setProgressListenerDownloadedBytes(0);

                LocalFolderBuilder localFolderBuilder=new LocalFolderBuilder(downloadProgressFileInfo.getMediaDownloadPath(),context);

                localFolderBuilder.range(downloadProgressFileInfo.downloadedBytes,downloadProgressFileInfo.downloadedBytes+CURRENT_DOWNLOAD_CHUNK_SIZE);

                proceedDownload=localFolderBuilder.start();

                Log.i("size",""+localFolderBuilder.getSize());

                if(proceedDownload)
                {
                    downloadProgressFileInfo.setSize(localFolderBuilder.getSize());
                    localFolderBuilder.download(downloadProgressFileInfo.fileOutPutStream,downloadProgressFileInfo,mNotificationManager,downloadCampaignUploadProgressNotification);
                }
               else
                {
                    //need to handle
                    handleCampaignDownloadingError("Resource not found");
                }
            }
            catch (DbxException | IOException e) {
                mException = e;
                Log.d("Download local resource", "IOException in downloading file");
            }
            catch (Exception e) {
                mException = e;
            }
            finally
            {
                if(inProgressCampaign!=null&&inProgressCampaign.equalsIgnoreCase(campaignName)) {

                    if (mException != null) {

                        if (downloadCampaignResultReceiver != null) {
                            mException.printStackTrace();

                            downloadProgressFileInfo.onChunkDownloadError();

                            if (mException instanceof NetworkIOException) {
                                //handle network io exceptions
                                chunkDownloadResourceNetworkIoException();
                            } else {
                                downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.DBX_RESOURCE_FILE_DOWNLOAD_FAILURE, null);

                            }

                        }else
                        {
                            finishService();
                        }
                    }
                    else
                        {
                        //success case ,,and proceed download

                        if (downloadCampaignResultReceiver != null && proceedDownload) {

                            downloadProgressFileInfo.onChunkDownloadSuccess();

                            downloadCampaignResultReceiver.send(DBX_RESOURCE_FILE_CHUNK_DOWNLOAD_SUCCESS,null);


                        }
                    }
                }
            }
        }
    }


}
