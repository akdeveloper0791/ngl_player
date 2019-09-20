package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.DisplayAdsBase;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.StopApp;
import com.ibetter.www.adskitedigi.adskitedigi.database.ActionsDBHelper;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.CopyFileAsync;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.MediaModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.service.GetModifyFilesService;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.service.HandleMediaSettingsService;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers.GetModifyFilesReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings.AudioSettingsConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.DisplayDialogForLicenceApprovalActivity;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.announcement.PlayAnnouncement;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.model.HandlePlayerCommands;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.model.HandleRulesCommands;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.receiver.MonitorConnectivityChangesChanges;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.service_receivers.HandleGetModifyFilesServiceReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.services.DeleteCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.services.GetFilesService;
import com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings.ScrollTextSettingsModel;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.impl.USER;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class EnterPriseSettingsService extends Service {

    protected static FtpServer ftpServer;
    private Context context;

    private final static int PORT_NUMBER = 2020;
    private final static String USER_NAME = "android";
    private final static String PASS_WORD = "android";

    private Timer errorRestartFTPServerTimer;
    private final static int RESTART_ERROR_FTP_TIMER_PERIOD = 30000;//@ every 30 seconds

    private ArrayList<String> pendingCommands = new ArrayList<>();
    public String ipAddr;

    private static boolean isHandleCommandProcessing = false;

    private MonitorConnectivityChangesChanges monitorConnectivityChangesChanges;

    public void onCreate()
    {
        super.onCreate();
        context = EnterPriseSettingsService.this;

        ipAddr=new DeviceModel().getIpAddress(context);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            //register rx
            registerMonitorConnectivityChangesChanges();
        }

        // save service info
        SignageServe.signageServeObject.saveRunningServicesInfo(EnterPriseSettingsService.class.getName(),
                EnterPriseSettingsService.this);

        displayFrontNotification("Initializing",null);


    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent,int flags,int startId)
    {

      if(context==null)
      {
          context = EnterPriseSettingsService.this;
      }

      if (SignageMgrAccessModel.isSignageMgrAccessOn(context,getString(R.string.sm_access_enterprise_mode))) {

            stopFTPServer();

            configureAndStartFTPServer();
        }
        else
        {
            stopSelf();
        }

        return START_STICKY; //restart the service whenever resources get available
    }

    //configure and start ftp server
    private void configureAndStartFTPServer()
    {
        BasicConfigurator.configure();

        //configure setup
        configureSetUp();
    }

    private void configureSetUp()
    {
      String storagePath = EnterPriseSettingsModel.createStorageSpace(context);
      if(storagePath!=null)
      {

        File propertiesFiles = getPropertiesFile();
         if(propertiesFiles!=null)
         {
            setUpFTPServer(storagePath,propertiesFiles);
         }
      }else
      {
          //error in setting up directory ,, show error notification
          ftpSetUpError("Error in setting up directory");
      }
    }



    //get properties file
    private File getPropertiesFile()
    {
        try {
            File propertiesFile = new File(getFilesDir() + "FTP_myusers.properties");
            if (!propertiesFile.exists()) {
                if(propertiesFile.createNewFile())
                {
                    return propertiesFile;
                }
                else
                {
                    //unable to create properties file ,, display notification
                    ftpSetUpError("Unable to create properties file");
                    return null;
                }
            }else
            {
                return propertiesFile;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            ftpSetUpError("Unable to create properties file "+e.getMessage());
            //unable to create properties file ,, display notification
            return null;
        }
    }

    //set up ftp server
    private void setUpFTPServer(String storagePath,File propertiesFiles)
    {
        try
        {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setServerAddress(DeviceModel.getIpAddress(context));


        factory.setPort(PORT_NUMBER);// set the port of the listener (choose your desired port, not 1234)
        serverFactory.addListener("default", factory.createListener());


        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(propertiesFiles);//choose any. We're telling the FTP-server where to read it's user list
        userManagerFactory.setPasswordEncryptor(new PasswordEncryptor()

        {//We store clear-text passwords in this example

            @Override
            public String encrypt(String password) {
                return password;
            }

            @Override
            public boolean matches(String passwordToCheck, String storedPassword) {
                return passwordToCheck.equals(storedPassword);
            }
        });

        //Let's add a user, since our myusers.properties files is empty on our first test run
        BaseUser user = new BaseUser();
        user.setName(USER_NAME);
        user.setPassword(PASS_WORD);
        user.setHomeDirectory(storagePath);

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        authorities.add(new TransferRatePermission(Integer.MAX_VALUE, Integer.MAX_VALUE));
        user.setAuthorities(authorities);
        UserManager um = userManagerFactory.createUserManager();
        try
        {
          um.save(user);//Save the user to the user list on the filesystem

          serverFactory.setUserManager(um);

          Map<String, Ftplet> m = new HashMap<String, Ftplet>();


          m.put("miaFtplet",new EnterPriseFTPFileObserver(context,EnterPriseSettingsService.this));

          serverFactory.setFtplets(m);


          ftpServer = serverFactory.createServer();



            ftpServer.start();//Your FTP server starts listening for incoming FTP-connections, using the configuration options previously set

            //stop timer if running
            stopErrorRestartTimer();

            displayFrontNotification("Success",null);

        }
        catch (FtpException ex)
        {
            //Deal with exception as you need
            ex.printStackTrace();
            Toast.makeText(this,"FTP Error "+ex.getMessage(),Toast.LENGTH_SHORT).show();

            ftpSetUpError("FTP Error "+ex.getMessage());
            //unable to start the server,, display error notification

           //new  DisplayDebugLogs(context).execute(ex.getMessage());

        }
    }catch (Exception e)
     {
        e.printStackTrace();
        Toast.makeText(this,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();

        ftpSetUpError("Error "+e.getMessage());
         //new  DisplayDebugLogs(context).execute(e.getMessage());

         //unable to start the server,, display error notification
     }

    }

    //on destroy
    public void onDestroy()
    {
        super.onDestroy();
        // save service info
        SignageServe.signageServeObject.removeBackGroundService(EnterPriseSettingsService.class.getName());

        stopFTPServer();

        //remove front end notification
       stopForeground(true);

        stopErrorRestartTimer();
    }

    private void suspendFTPServer()
    {
        try
        {
            ftpServer.suspend();

        }catch (Exception e)
        {
                e.printStackTrace();
        }
    }
    private void stopFTPServer()
    {
        try
        {
            if(ftpServer!=null)
            {
                suspendFTPServer();
                ftpServer.stop();
            }

        }catch (Exception e)
        {

        }
    }

    //display success Notification
    private void displayFrontNotification(String contentText,String errorMsg)
    {

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setOngoing(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("SS EnterPrise Mode")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            //.setChannelId("Test");

            assignWithNotificationChannel(builder);

            if (errorMsg == null) {
                builder.setContentText(contentText);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                    builder.setContentText(contentText);

                    NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle()
                            .bigText(errorMsg);
                    builder.setStyle(style);

                } else {
                    builder.setContentText(errorMsg);
                }
            }


            Notification notification = builder.build();


            // Sets an ID for the notification, so it can be updated
            int notifyID = NotificationModelConstants.ENTER_PRISE_SETTINGS_SERVICE_NOTIFICATION;

            startForeground(notifyID, notification);

          //  new DisplayDebugLogs(context).execute("Suceess");


        }catch (Exception E)
        {
            E.printStackTrace();
           // new DisplayDebugLogs(context).execute(E.getMessage());
        }

    }


    private void ftpSetUpError(String errorMsg)
    {
        //display notification
        displayFrontNotification("Error",errorMsg);
        errorRestartFTPServerTimer();
    }

    //start ftp error restart timer
    private void errorRestartFTPServerTimer()
    {

        if(errorRestartFTPServerTimer!=null)
        {
            stopErrorRestartTimer();
        }
        errorRestartFTPServerTimer= new Timer();
        errorRestartFTPServerTimer.schedule(new CheckAndRestartFTPServer(),RESTART_ERROR_FTP_TIMER_PERIOD);

    }

    private void stopErrorRestartTimer()
    {
        try
        {
            errorRestartFTPServerTimer.cancel();
            errorRestartFTPServerTimer.purge();

        }catch (Exception e)
        {

        }finally {
            errorRestartFTPServerTimer = null;
        }
    }

    private class CheckAndRestartFTPServer extends TimerTask
    {
        public void run()
        {
           if(isFTPServerRunning())
           {
               //ftp is already running
              this.cancel();
              stopErrorRestartTimer();
           }else
           {
               //restart ftp server
               restartService();
           }
        }
    }

    //restart service
    private void restartService()
    {
        stopSelf();
        startService(new Intent(context,EnterPriseSettingsService.class));
    }

    private boolean isFTPServerRunning()
    {
        return (ftpServer!=null && (!ftpServer.isStopped() || !ftpServer.isSuspended()));
    }

    //handle command processing
    public void handleCommand(String commandFile)
    {
        Log.d("FTP FIle Obser","Inside handle command "+commandFile);
        pendingCommands.add(commandFile);
        if(!isHandleCommandProcessing)
        {
            new HandleCommandProcessing().execute(pendingCommands.get(0));
        }
    }

    private void displayToast(final String msg)
    {
        Handler handler = new Handler(getApplicationContext().getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }


    //create notification channel
    private void assignWithNotificationChannel(NotificationCompat.Builder builder)
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.enterprise_notify_ch_id);
            CharSequence name = getString(R.string.enterprise_notify_ch_name);
            String description = getString(R.string.enterprise_notify_ch_des);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            builder.setChannelId(channelId);
        }
    }

    //back ground thread to handle command processing
    private class HandleCommandProcessing extends AsyncTask<String,Void,File>
    {

        protected void onPreExecute()
        {
            isHandleCommandProcessing = true;
        }

        protected File doInBackground(String... params)
        {
            String commandFileName = params[0];
            //check whether file exists or not

            File commandFile = new File(new User().getUserPlayingFolderModePath(context)+File.separator+commandFileName);
            if(commandFile.exists())
            {
                String processedText = new MediaModel().readTextFile(commandFile);

                Log.d("FTP FIle Obser","Inside handle command processed text is----------"+processedText);

                if(processedText!=null)
                {
                    processProcessedText(processedText);
                }
            }
            return commandFile;
        }

        protected void onPostExecute(File commandFile)
        {
            removeCommandFile(commandFile);

            checkAndHandleNextCommands();
        }

        private void removeCommandFile(File commandFile)
        {
            pendingCommands.remove(commandFile.getName());
            commandFile.delete();

        }

        private void checkAndHandleNextCommands()
        {
            if(pendingCommands!=null && pendingCommands.size()>=1)
            {
                 new HandleCommandProcessing().execute(pendingCommands.get(0));
            }else
            {
                isHandleCommandProcessing = false;
            }
        }

        private void processProcessedText(String jsonText)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(jsonText);
                String request = jsonObject.getString("request");
                Log.i("processedText",jsonObject.toString());

                if(request!=null)
                {
                    if(request.equalsIgnoreCase(getString(R.string.media_request)))
                    {
                          handleMediaCommands(jsonObject);
                    }else if(request.equalsIgnoreCase(getString(R.string.media_and_player_request)))
                    {
                        handleMediaPlayerRequest(jsonObject);
                    }else if(request.equalsIgnoreCase(getString(R.string.device_settings_request)))
                    {
                        handleDeviceSettingsRequest(jsonObject);
                    }else if(request.equalsIgnoreCase(getString(R.string.player_settings_request)))
                    {
                        new HandlePlayerCommands(context).handlePlayerSettingsRequest(jsonObject);

                    }else if(request.equalsIgnoreCase(getString(R.string.interaction_settings_request)))
                    {
                        handleInteractionSettings(jsonObject);
                    }else if(request.equalsIgnoreCase(getString(R.string.campaign_rules_request)))
                    {
                        new HandleRulesCommands(context).handleCamRulesCommands(jsonObject);

                    }else if(request.equalsIgnoreCase(getString(R.string.auto_campaign_rule_setting_request)))
                    {
                        handleAutoCampaignRuleSetting(jsonObject);
                    }
                    else if(request.equalsIgnoreCase(getString(R.string.app_default_update_current_time_request)))
                    {
                        handleDisplayUpdateInfo(jsonObject);

                    }
                }
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }


    //handle media commands
    private void handleMediaCommands(JSONObject jsonObject) throws JSONException
    {
        String action = jsonObject.getString("action");
        if(action.equalsIgnoreCase(getString(R.string.modify_request)))
        {

            getFilesToModify(jsonObject.getInt(getString(R.string.offset_json_key)),jsonObject.getString(getString(R.string.save_ftp_command_response_to_json_key)));
        }else if(action.equalsIgnoreCase(getString(R.string.delete_actio_request)))
        {
            deleteCampaigns(jsonObject.getJSONArray(getString(R.string.delete_media_files_json_key)));

        }else if(action.equalsIgnoreCase(getString(R.string.get_text_settings_request)))
        {
            prepareGetTextSettingsResponse(jsonObject.getString(
                    getString(R.string.save_ftp_command_response_to_json_key)));
        }else if(action.equalsIgnoreCase(getString(R.string.add_text_ticker_prefix)))
        {
            updateTickerTextSettings(jsonObject.getJSONObject(getString(R.string.settings_json_key)));

        }else if(action.equalsIgnoreCase(getString(R.string.play_offer_audio_request)))
        {
            prepareBGAudioSettingsReq(jsonObject.getString(getString(R.string.save_ftp_command_response_to_json_key)));

        }else if(action.equalsIgnoreCase(getString(R.string.delete_offer_audio_action_request)))
        {

            deleteBgAudioFile(jsonObject.getString(getString(R.string.bg_audio_file)));
        }
        else if(action.equalsIgnoreCase(getString(R.string.all_file_key)))
        {

            getAllFiles(jsonObject.getInt(getString(R.string.offset_json_key)),jsonObject.getString(getString(R.string.save_ftp_command_response_to_json_key)));
        }
    }

    private void getFilesToModify(final int offset,final String saveResponseTo)
    {
        Handler handler = new Handler(getApplicationContext().getMainLooper());
        HandleGetModifyFilesServiceReceiver getModifyFilesReceiverCallBacks = new
                HandleGetModifyFilesServiceReceiver(EnterPriseSettingsService.this,saveResponseTo);

        //initialize the receiver to handle the call backs
        GetModifyFilesReceiver getModifyFilesReceiver = new GetModifyFilesReceiver(handler);
        getModifyFilesReceiver.setReceiver(getModifyFilesReceiverCallBacks);

        Intent intent = new Intent(context,GetModifyFilesService.class);
        intent.putExtra("result_receiver",getModifyFilesReceiver);
        intent.putExtra("offset",offset);
        context.startService(intent);

    }


    private void getAllFiles(final int offset,final String saveResponseTo)
    {
        Handler handler = new Handler(getApplicationContext().getMainLooper());
        HandleGetModifyFilesServiceReceiver getModifyFilesReceiverCallBacks = new
                HandleGetModifyFilesServiceReceiver(EnterPriseSettingsService.this,saveResponseTo);

        //initialize the receiver to handle the call backs
        GetModifyFilesReceiver getModifyFilesReceiver = new GetModifyFilesReceiver(handler);
        getModifyFilesReceiver.setReceiver(getModifyFilesReceiverCallBacks);

        Intent intent = new Intent(context, GetFilesService.class);
        intent.putExtra("result_receiver",getModifyFilesReceiver);
        intent.putExtra("offset",offset);
        context.startService(intent);

    }

    private void deleteCampaigns(JSONArray files)
    {
        Intent deleteCampaignService = new Intent(context, DeleteCampaigns.class);
        deleteCampaignService.putExtra("json_array_files_string",files.toString());
        startService(deleteCampaignService);
    }

    private void prepareGetTextSettingsResponse(String saveResponseTo)
    {
        try {
            JSONObject jsonObject = ScrollTextSettingsModel.GetTextSettingsResponseJSON(context);
            EnterPriseSettingsModel.saveSMFTPResponse(context,jsonObject.toString(),saveResponseTo);
        }catch (JSONException e)
        {
            displayToast("Unable to handle get text settings. "+e.getMessage());
        }
    }

    private void updateTickerTextSettings(JSONObject settingsJSON)
    {
        try {
            ScrollTextSettingsModel.updateTextSettingsFromSM(context,settingsJSON.toString());
        }catch (JSONException e)
        {
            displayToast("Unable to update text settings. "+e.getMessage());
        }
        catch (Exception e)
        {
            displayToast("Unable to update text settings. "+e.getMessage());
        }
    }

    private void prepareBGAudioSettingsReq(String saveResponseTo)
    {
        try {
            JSONObject jsonObject = new AudioSettingsConstants().getAudioSettingsSMRequest(context);
            EnterPriseSettingsModel.saveSMFTPResponse(context,jsonObject.toString(),saveResponseTo);
        }catch (JSONException e)
        {
            displayToast("Unable to handle get audio settings. "+e.getMessage());
        }
    }

    private void deleteBgAudioFile(String audioFile)
    {
        new File(audioFile).delete();
    }

    private void handleMediaPlayerRequest(JSONObject jsonObject)throws JSONException
    {
        HandleMediaSettingsService.startHandleMediaSettingsService(context,jsonObject.getString(getString(R.string.payload_string_json_key)));
    }

    private void handleDeviceSettingsRequest(JSONObject jsonObject)throws JSONException
    {
        String action = jsonObject.getString("action");
        if(action.equalsIgnoreCase(getString(R.string.control_panel_restart_request)))
        {
            //turn off auto restart of app, because we are forcly restarting the service
            //DisplayAdsBase.isRelaunchAppOnStop = false;
            sendResetIsRelaunchAppOnStop(false);

            DeviceModel.restartApp(context);
        }
        else if(action.equalsIgnoreCase(getString(R.string.control_panel_stop_request)))
        {
            //stop app
            Log.d("stop","stop app");
            Intent i =new Intent(context,StopApp.class);
            stopApp();
        }

    }

    private void stopApp()
    {
        DeviceModel.stopApp(context);
    }

    public void saveBGAudioFile(String bgAudioFileName)
    {
        String saveBgAudioTo = new Constants().saveBGAudioTo(context,bgAudioFileName.replace(context.getString(R.string.bg_audio_file),""));
        File sourceBGFile = new File(new User().getUserPlayingFolderModePath(context)+File.separator+bgAudioFileName);
        File toBGFile = new File(saveBgAudioTo);

        new CopyFileAsync(context,true).execute(new File[]{sourceBGFile,toBGFile});
    }

   //handle interaction settings
    private void handleInteractionSettings(JSONObject jsonObject) throws JSONException
    {
        Log.i("handleInteractionSetgs",jsonObject.toString());

        String action = jsonObject.getString("action");
        if(action.equalsIgnoreCase(getString(R.string.customer_interactive_action_settings_request)))
        {
           prepareAndSendInteractionSettingsList(jsonObject.getString(getString(R.string.save_ftp_command_response_to_json_key)));

        }else if(action.equalsIgnoreCase(getString(R.string.update_settings_json_key)))
        {
            updateInteractionSettings(jsonObject.getString(getString(R.string.settings_json_key)));

        }else if(action.equalsIgnoreCase(getString(R.string.customer_interactive_actions_list_request)))
        {
            getInteractiveActionCustomers(jsonObject.getString(
                    getString(R.string.save_ftp_command_response_to_json_key)),jsonObject.getString(
                    getString(R.string.status_json_key)));
        }else if(action.equalsIgnoreCase(getString(R.string.customer_interactive_actions_close_request)))
        {
            updateInteractiveCustomerStatusToClose(jsonObject.getString(
                    getString(R.string.c_id_json_key)));
        }
        else if(action.equalsIgnoreCase(getString(R.string.customer_interactive_update_actions_text_request)))
        {
            updateInteractiveActionsText(jsonObject.getString(
                    getString(R.string.info_json_key)));
        }
        else if(action.equalsIgnoreCase(getString(R.string.interactive_optional_data)))
        {
            getCustomerInteractiveOptionalData(jsonObject.getString(
                    getString(R.string.save_ftp_command_response_to_json_key)),jsonObject.getInt(
                    getString(R.string.interactive_od_id)));
        }else if(action.equalsIgnoreCase(getString(R.string.export_customer_interactive_actions_list_request)))
        {
            Log.i("ActionsDataJson","getExportActionsData:"+jsonObject.toString());
            getExportActionsData(jsonObject.getString(getString(R.string.save_ftp_command_response_to_json_key)),
                    jsonObject.getString(getString(R.string.status_json_key)),
                    jsonObject.getLong(getString(R.string.service_action_start_date)),
                    jsonObject.getLong(getString(R.string.service_action_end_date)));
        }
    }

    private void prepareAndSendInteractionSettingsList(String saveResponseTo)
    {
        String requestObj=new ActionModel().getActionLayoutSettings(context);
       // Log.i("InteractionSettings","InteractionSettings:"+requestObj);
        EnterPriseSettingsModel.saveSMFTPResponse(context,requestObj,saveResponseTo);
    }

    private void updateInteractionSettings(String settings)
    {
        //new ActionsDBHelper(context).deleteAllFields();
      //   Log.i("InteractionSettings","updateInteractionSettings:"+settings);
        new ActionModel().setupActionSettings(context,settings);
    }

    private void getInteractiveActionCustomers(String saveResponseTo,String status)
    {
        String requestCustomerJson=new ActionModel().getRequestedActionsDataJson(context,status);
        EnterPriseSettingsModel.saveSMFTPResponse(context,requestCustomerJson,saveResponseTo);
    }

    private void getExportActionsData(String saveResponseTo,String status,long fromDate,long toDate)
    {
        String requestCustomerJson=new ActionModel().getExportedActionsDataJson(context,status,fromDate,toDate);
       // Log.i("ActionsDataJson","getExportActionsData:"+requestCustomerJson);
        EnterPriseSettingsModel.saveSMFTPResponse(context,requestCustomerJson,saveResponseTo);
    }

    private void getCustomerInteractiveOptionalData(String saveResponseTo,int customerId)
    {
        String requestObj=new ActionModel().getInteractiveOptionalData(context,customerId);
       // Log.i("InteractionSettings","getCustomerInteractiveOptionalData:"+requestObj);
        EnterPriseSettingsModel.saveSMFTPResponse(context,requestObj,saveResponseTo);

    }

    private void updateInteractiveCustomerStatusToClose(String cId)
    {
        ActionModel.updateCustomerActionStatus(cId,context);
        updatePlayer(getString(R.string.customer_interactive_actions_close_request),cId);
    }

    private void updateInteractiveActionsText(String infoJSON)
    {
        ActionModel.updateCustomerActionText(context,infoJSON);
        updatePlayer(getString(R.string.customer_interactive_update_actions_text_request),infoJSON);
    }

    private void updatePlayer(String action,String extraInfo)
    {
        Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(getString(R.string.action),action);
        intent.putExtra(getString(R.string.action_extra_info),extraInfo);
        sendBroadcast(intent);
    }

    public void playAnnouncement(String fileName)
    {
        if(!PlayAnnouncement.addPendingAnnouncements(fileName)) {
            Intent intent = new Intent(context, PlayAnnouncement.class);
            intent.putExtra("announcement_file", fileName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //handle auto campaign rule setting
    private void handleAutoCampaignRuleSetting(JSONObject jsonObject) throws JSONException
    {
        Log.i("CampaignRuleSetting",jsonObject.toString());
        String action = jsonObject.getString("action");
        if(action.equalsIgnoreCase(context.getString(R.string.get_auto_campaign_rule_setting_request)))
        {
            getAutoCampaignRuleSetting(jsonObject.getString(context.getString(R.string.save_ftp_command_response_to_json_key)));

        }else if(action.equalsIgnoreCase(context.getString(R.string.update_auto_campaign_rule_setting)))
        {
            new ActionModel().saveAutoCampaignRuleSettingInSP(context,jsonObject.getBoolean(context.getString(R.string.auto_campaign_rule_setting)));
        }

    }

    private void getAutoCampaignRuleSetting(String saveResponseTo) throws JSONException
    {
        EnterPriseSettingsModel.saveSMFTPResponse(context,new ActionModel().getAutoCampRuleSettingsJSON(context),saveResponseTo);
    }

    private void sendResetIsRelaunchAppOnStop(boolean status)
    {
        Intent intent = new Intent(DisplayAdsBase.UPDATE_RECIVER_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("action",getString(R.string.update_is_re_launch));
        intent.putExtra("value",status);
        sendBroadcast(intent);
    }

    //register stop service receiver
    private void registerMonitorConnectivityChangesChanges()
    {

        IntentFilter intentFilter=new IntentFilter( );
        intentFilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.setPriority(2147483646);

        monitorConnectivityChangesChanges=new MonitorConnectivityChangesChanges(new WeakReference(EnterPriseSettingsService.this),ipAddr);
        registerReceiver(monitorConnectivityChangesChanges, intentFilter);

    }

    private void unRegisterMonitorConnectivityChangesChanges()
    {

        try
        {
            if (monitorConnectivityChangesChanges != null) {
                unregisterReceiver(monitorConnectivityChangesChanges);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            monitorConnectivityChangesChanges = null;
        }

    }

    private void handleDisplayUpdateInfo(JSONObject jsonObject)
    {
        if(User.getDisplayLicenceStatus(context)==Constants.DISPLAY_SUCCESS_STATUS)

        {
            Log.i("ftp display is", "active" + "");
        }
        else
        {
            try {

                long currentTime = jsonObject.getLong(context.getString(R.string.current_time_json_key));

                if (new User().getDisplayCreatedTime(context) != 0)
                {
                    long licenceTime = new User().getDisplayCreatedTime(context) + User.getTrialPeriodTime(context);
                    Log.i("ftp licenceTime time", licenceTime + "");
                    Log.i("ftp created time", new User().getDisplayCreatedTime(context) + "");

                    Log.i("ftp curent time", currentTime + "");
                    if (currentTime >= licenceTime)
                    {
                        Log.i("ftp info", "inside expired");
                        licenceExpired();
                    }
                    else
                    {
                        Log.i("ftp info", "inside device is not expired expired");
                    }
                } else {
                    Log.i("ftp info", "register time uise zero");

                }
            } catch (Exception E) {
                E.printStackTrace();
            }
        }
    }

    private void licenceExpired()
    {
        User.setLicenceStatus(context,Constants.DISPLAY_EXPIRED_STATUS);

        //restart activity
        sendResetIsRelaunchAppOnStop(false); //to safe restart

        try
        {
            Thread.sleep(500);
        }catch(InterruptedException e)
        {

        }

        DeviceModel.restartApp(context);
    }
}