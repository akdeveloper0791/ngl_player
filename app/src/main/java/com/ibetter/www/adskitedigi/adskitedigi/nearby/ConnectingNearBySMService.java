package com.ibetter.www.adskitedigi.adskitedigi.nearby;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.RestartApp;
import com.ibetter.www.adskitedigi.adskitedigi.SignageServe;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.DisplayLocalFolderAds;
import com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder.MediaInfo;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.MediaModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.multi_region.MultiRegionSupport;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.service.GetModifyFilesService;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.service.HandleMediaSettingsService;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers.HandleGetModifyFilesServiceReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings.AudioSettingsConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.ToggleSMServices;
import com.ibetter.www.adskitedigi.adskitedigi.settings.text_settings.ScrollTextSettingsModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by vineethkumar0791 on 28/03/18.
 */

public class ConnectingNearBySMService extends Service
{
    private boolean isServiceOn = false;
    private Context context;
    private final static SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final static SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();

   //public static String  CHECK_DISCOVERY_STATUS="com.ibetter.www.adskitedigi.adskitedigi.nearby.ConnectingNearBySMService.CheckDiscoveryStatusRx";

    private Timer monitorDiscoveryTimerTask,restartDiscoveryTimerTask;

   private final static long MonitorDiscoveryTime=180000;//3minutes
   private final static long RestartDiscoveryTime=20000;//20 seconds


    /** States that the UI goes through. */
    public enum State {
        UNKNOWN,
        SEARCHING,
        CONNECTED,
        FAILURE
    }

    private State mState = State.UNKNOWN;


    /** Our handler to Nearby Connections. */
    private ConnectionsClient mConnectionsClient;

    /** The devices we've discovered near us. */
    private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();

    /**
     * The devices we have pending connections to. They will stay pending until we call {@link
     */
    private final Map<String, Endpoint> mPendingConnections = new HashMap<>();

    /**
     * The devices we are currently connected to. For advertisers, this may be large. For discoverers,
     * there will only be one entry in this map.
     */
    private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();


    public final static  int RESTART_SM_SERVICE_ALARM_ACTION = 0101;


    /**
     * True if we are asking a discovered device to connect to us. While we ask, we cannot ask another
     * device.
     */

    /** Callbacks for connections to other devices. */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback()
            {

                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo)
                {
                    logD(
                            String.format(
                                    "On Connection Initiated (endpointId=%s, endpointName=%s)",
                                    endpointId, connectionInfo.getEndpointName()));
                    Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
                    mPendingConnections.put(endpointId, endpoint);

                    acceptConnection(endpoint);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result)
                {
                    logD(String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result));

                    // We're no longer connecting

                    if (!result.getStatus().isSuccess())
                    {
                        logD(("Connection failed. Received status "+ result.getStatus()));
                        onConnectionFailed(mPendingConnections.remove(endpointId));
                        return;
                    }

                    connectedToEndpoint(mPendingConnections.remove(endpointId));

                }
                @Override
                public void onDisconnected(String endpointId) {
                    if (!mEstablishedConnections.containsKey(endpointId)) {
                        logD("Unexpected disconnection from endpoint " + endpointId);
                        return;
                    }
                    disconnectedFromEndpoint(mEstablishedConnections.get(endpointId));
                }
            };

    /** Callbacks for payloads (bytes of data) sent from another device to us. */
    private final PayloadCallback mPayloadCallback =
            new PayloadCallback()
            {

                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    logD(String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
                    onReceive(mEstablishedConnections.get(endpointId), payload);
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update)
                {
                    onTransferUpdate(update.getPayloadId(),update);
                }

     };

    @Override
    public void onCreate()
    {
        super.onCreate();
        context=ConnectingNearBySMService.this;
        mConnectionsClient = Nearby.getConnectionsClient(this);

        SignageServe.signageServeObject.signageServeObject.setSmService(ConnectingNearBySMService.this);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        logD("is Service running - "+isServiceOn);

        if(mConnectionsClient==null)
        {
            mConnectionsClient = Nearby.getConnectionsClient(this);
        }

        if(!isServiceOn || mConnectionsClient==null)
        {
            isServiceOn = true;
            logD("Service is started");

            setState(State.SEARCHING);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();


        logD("SM Service is Destroyed");

        isServiceOn = false;

        stopAllEndpoints();

        SignageServe.signageServeObject.signageServeObject.setSmService(null);

        checkRestartSMService();

        //stop timer tasks
        stopDiscovering();

        stopMonitorTimerTask();

        stopRestartDiscoveryTimerTask();



        mConnectionsClient = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @CallSuper
    protected synchronized void logD(String msg)
    {
        Log.d("Signage Serv ",msg);

      //  new DisplayDebugLogs(context).execute(msg);

    }

    /** Accepts a connection request. */
    protected void acceptConnection(final Endpoint endpoint) {

        if(mConnectionsClient!=null) {
            mConnectionsClient
                    .acceptConnection(endpoint.getId(), mPayloadCallback)
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    setState(State.FAILURE);
                                    logD("acceptConnection() failed." + e);
                                    if (context != null) {
                                        Toast.makeText(context, "Unable to accept connection from SM " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
        }else
        {
            stopSelf();
        }

    }



    protected void onConnectionFailed(Endpoint endpoint) {
        // Let's try someone else.
        if (getState() == State.SEARCHING)
        {
            setState(State.FAILURE);
        }
    }

    /** @return The current state. */
    private State getState() {
        return mState;
    }


    private void connectedToEndpoint(Endpoint endpoint) {
        logD(String.format("connectedToEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.put(endpoint.getId(), endpoint);
        onEndpointConnected(endpoint);
    }

    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.signage_mgr_access_toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.CONNECTED);
    }

    /** {@see ConnectionsActivity#onReceive(Endpoint, Payload)} */

    protected void onReceive(Endpoint endpoint, Payload payload) {

        try {

            switch (payload.getType())
            {
                case Payload.Type.BYTES:

                    String payloadFilenameMessage = new String(payload.asBytes(), "UTF-8");

                    logD("result"+payloadFilenameMessage);

                    if(payloadFilenameMessage.startsWith(getString(R.string.add_action_prefix)))
                    {
                        String [] payloadsStrings=payloadFilenameMessage.split(":");

                        if(payloadsStrings.length>0)
                        {
                            String filePayloadId = payloadsStrings[payloadsStrings.length - 1];

                            String fileName=payloadsStrings[1];

                            filePayloadFilenames.put(Long.parseLong(filePayloadId),fileName);

                            logD(Long.parseLong(filePayloadId)+"incomingPayloads id\n"+filePayloadId);
                        }

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.delete_request)))
                    {
                        String [] payloadsStrings=payloadFilenameMessage.split(":");

                        getRequestedFiles(payloadsStrings[payloadsStrings.length - 1] );

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.campaign_image_add_request)))
                    {
                        String [] payloadsStrings=payloadFilenameMessage.split(":");

                        if(payloadsStrings.length>0)
                        {

                            String filePayloadId = payloadsStrings[payloadsStrings.length - 1];

                            String fileName=payloadsStrings[1];

                            filePayloadFilenames.put(Long.parseLong(filePayloadId),new ConnectingNearBySMMOdel().saveCampaignImagesTo(context,fileName));

                            logD(Long.parseLong(filePayloadId)+"incomingPayloads id\n"+filePayloadId);
                        }

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.delete_actio_request)))
                    {

                        String  payloadsString=payloadFilenameMessage.replace(getString(R.string.delete_actio_request),"");

                        if(payloadsString.length()>0) {

                            ArrayList<File> filesList=getFiles(payloadsString);

                            if(filesList!=null)
                            {
                              //  logD("delete Files Found");

                                deleteAction(filesList);

                            }else
                            {
                               // logD("No Files Found");
                            }

                        }

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.control_panel_restart_request)))
                    {
                       //restart app
                        Intent i =new Intent(context,RestartApp.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.add_text_ticker_prefix)))
                    {

                        saveTextTicker(payloadFilenameMessage.replace(getString(R.string.add_text_ticker_prefix),"") );

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.get_text_settings_request)))
                    {

                        sendGetTextSettingsResponse( );

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.bg_audio_file)))
                    {
                        String [] payloadsStrings=payloadFilenameMessage.split(":");

                        if(payloadsStrings.length>0) {

                            String fileName=(payloadsStrings.length>=2?payloadsStrings[1]:null);

                          //  logD("file name"+fileName);
                            String filePayloadId = payloadsStrings[payloadsStrings.length - 1];

                            filePayloadFilenames.put(Long.parseLong(filePayloadId),new Constants().saveBGAudioTo(context,fileName));
                        }

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.play_offer_audio_request)))
                    {

                        sendGetAudioSettingsResponse( );

                    }

                    else if(payloadFilenameMessage.startsWith(getString(R.string.play_offer_text_action)))
                    {

                        updateAudioSettings(payloadFilenameMessage.replace(getString(R.string.play_offer_text_action),"") );
                    }

                    //request received form signage manager to modify data
                    else if(payloadFilenameMessage.startsWith(getString(R.string.modify_request)))
                    {
                        String [] payloadsStrings=payloadFilenameMessage.split(":");
                        int offset = 0;
                        if(payloadsStrings.length>=2) {

                            offset= Constants.convertToInt(payloadsStrings[1]);

                       }
                        getFilesToModify(offset);
                    }

                    //request received form signage manager to modify action request with reference file name
                    else if(payloadFilenameMessage.startsWith(getString(R.string.modify_action_request)))
                    {
                        String [] payloadsStrings=payloadFilenameMessage.split(":");

                        String filePath=payloadsStrings[payloadsStrings.length - 1];
                        File file=new File(filePath);

                        new RequestedData(file).execute();

                    }

                    else if(payloadFilenameMessage.startsWith(getString(R.string.delete_offer_audio_action_request)))
                    {

                        String  payloadsString=payloadFilenameMessage.replace(getString(R.string.delete_offer_audio_action_request),"");

                        if(payloadsString.length()>0) {

                            File file=new File(payloadsString);

                            if(file.exists())
                            {
                                file.delete();



                            }else
                            {
                               // logD("No Files Found");
                            }

                        }

                    }
                    //request received form signage manager to get the customer interactive actions data list
                    else if(payloadFilenameMessage.startsWith(getString(R.string.customer_interactive_actions_list_request)))
                    {
                        String  status=payloadFilenameMessage.replace(getString(R.string.customer_interactive_actions_list_request),"");
                        sendActionsDataList(status,getString(R.string.customer_interactive_actions_list_response));
                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.actions_list_based_on_status_request)))
                    {
                        String  status=payloadFilenameMessage.replace(getString(R.string.actions_list_based_on_status_request),"");
                        sendActionsDataList(status,getString(R.string.actions_list_based_on_status_response));
                    }

                    //request received form signage manager to get the customer interactive actions settings data
                    else if(payloadFilenameMessage.startsWith(getString(R.string.customer_interactive_action_settings_request)))
                    {
                        String actionSettingsJson=new ActionModel().getActionLayoutSettings(context);
                        Log.i("onPostExecute","resourcesString actionSettingsJson:"+actionSettingsJson);

                        send(Payload.fromBytes((getString(R.string.customer_interactive_action_settings_response)+ actionSettingsJson).getBytes("UTF-8")));
                    }
                    //request received form signage manager to display action layout on screen when user do single click action
                    else if(payloadFilenameMessage.startsWith(getString(R.string.display_customer_interactive_actions_layout_request)))
                    {
                        //get the json data and update the ActionText in SS player
                        String  actionTextString=payloadFilenameMessage.replace(getString(R.string.display_customer_interactive_actions_layout_request),"");
                        Log.i("onPostExecute","resourcesString actionLayoutSetting:--------"+actionTextString);
                        new ActionModel().setupActionSettings(context,actionTextString);
                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.display_customer_interactive_actions_text_request)))//request received form signage manager to display scrolling action text on  SS screen
                    {
                        //get the json data  change action record status and refresh ActionText in SS player
                        String  actionTextString=payloadFilenameMessage.replace(getString(R.string.display_customer_interactive_actions_text_request),"");
                        Log.i("onPostExecute","resourcesString actionTextSetting:--------"+actionTextString);
                        new ActionModel().setupActionSettings(context,actionTextString);
                    }
                    //response received form signage manager to push customer interactive actions data list
                    else if(payloadFilenameMessage.startsWith(getString(R.string.customer_interactive_update_actions_text_request)))
                    {
                        interactiveActions(payloadFilenameMessage);

                    }
                    //response received form signage manager to close the customer interactive actions  form open state to close state
                    else if(payloadFilenameMessage.startsWith(getString(R.string.customer_interactive_actions_close_request)))
                    {
                        interactiveActions(payloadFilenameMessage);

                    }else if(payloadFilenameMessage.startsWith(getString(R.string.device_volume_change_request)))//for device settings form SM to control volume
                    {
                        deviceSettingsHandler(payloadFilenameMessage);

                    }

                    else if(payloadFilenameMessage.startsWith(getString(R.string.media_settings_request)))
                    {
                        handleMediaSettingsRequest(payloadFilenameMessage);

                    }
                    else if(payloadFilenameMessage.startsWith(getString(R.string.ss_mode_settings)))
                    {
                        String[] ssModeString=payloadFilenameMessage.split(":");
                        String ssModeRequest=ssModeString[ssModeString.length - 1];
                        handleSSModeSettingsRequest(ssModeRequest);
                    }
                    else
                    {
                        Toast.makeText(context,"Invalid request" +payloadFilenameMessage, Toast.LENGTH_SHORT).show();
                    }

                    //  Toast.makeText(context,"" +payloadFilenameMessage, Toast.LENGTH_SHORT).show();

                   // logD("type:/n"+"Payload.Type.BYTES");

                    break;

                case Payload.Type.FILE:

                   // logD("type:/n"+"Payload.Type.FILE");

                    incomingPayloads.put(payload.getId(), payload);
                   // logD("incomingPayloads id\n"+payload.getId());

                    // infoTv.append( saveFile(payloadFile));

                    break;

                case Payload.Type.STREAM:

                    if(payload!=null) {
                        sendUpdatesToPlayer(payload);
                      }

                    logD("type:/n"+"Payload.Type.STREAM");


                    break;



                default:
                   Toast.makeText(context,"Invalid request type "+payload.getType(),Toast.LENGTH_SHORT).show();
                    break;

            }

        }catch (Exception e)
        {
            Toast.makeText(context,"Error in processing request " +e.getMessage(), Toast.LENGTH_SHORT).show();
            logD( "Result"+e.getMessage());
        }

    }

    /** {@see ConnectionsActivity#onReceive(Endpoint, Payload)} */

    protected void onTransferUpdate(long payloadId,PayloadTransferUpdate update)
    {
       if(update!=null) {
          if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
              if (incomingPayloads.containsKey(payloadId)) {
                  Payload inComingPayload = incomingPayloads.remove(payloadId);
                  if (inComingPayload.getType() == Payload.Type.FILE) {
                      // Retrieve the filename that was received in a bytes payload.
                      String newFilename = filePayloadFilenames.remove(payloadId);

                      File payloadFile = inComingPayload.asFile().asJavaFile();

                      if (payloadFile.exists()) {
                          // Rename the file.

                          if (newFilename != null && newFilename.contains(getString(R.string.default_file_name_seperator))) {
                              try {
                                  //File newFile = new File(context.getFilesDir(), payloadFile.getAbsolutePath());
                                  new MediaModel().copyFiles(payloadFile.getAbsolutePath(), newFilename);

                                  if (payloadFile.exists()) {
                                      payloadFile.delete();
                                  }

                              } catch (Exception e) {
                                  Toast.makeText(context, "Error in receiving file " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                  logD("inside create file Error in creating file" + e.getMessage());
                              }
                          } else {
                              try {
                                  payloadFile.renameTo(new File(payloadFile.getParentFile(), newFilename));
                              } catch (Exception e) {
                                  logD("inside create file Error in creating file" + e.getMessage());
                                  Toast.makeText(context, "Error in receiving file " + e.getMessage(), Toast.LENGTH_SHORT).show();

                              }
                          }


                          //   logD("Rename\n\n" + );

                      } else {
                          //logD("not exist Rename");
                      }
                  }
              }
          } else if (update.getStatus() == PayloadTransferUpdate.Status.FAILURE || update.getStatus() == PayloadTransferUpdate.Status.CANCELED) {
              if (incomingPayloads.containsKey(payloadId)) {
                  incomingPayloads.remove(payloadId);
              }
              Toast.makeText(context, "Media receiving failure", Toast.LENGTH_SHORT).show();
          }
      }

    }


    private void disconnectedFromEndpoint(Endpoint endpoint) {
        logD(String.format("DisconnectedFromEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.remove(endpoint.getId());
        onEndpointDisconnected(endpoint);
    }


    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.signage_mgr_access_toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.SEARCHING);
    }

    protected String getName() {
        return new User().getUserDisplayName(context)+"-"+new User().getUserMobileNumber(context);
    }

    public Strategy getStrategy() {
        return ConnectingNearBySMMOdel.STRATEGY;
    }

    public String getServiceId()
    {
       return SignageMgrAccessModel.getSignageMgrAccessServiceId(context);
    }

    private void setState(State state) {

        logD("Status State set to  " + state);

        State oldState = mState;
        mState = state;
        onStateChanged(oldState, state);
    }

    /**
     * State has changed.
     *
     * @param oldState The previous state we were in. Clean up anything related to this state.
     * @param newState The new state we're now in. Prepare the UI for this state.
     */
    private void onStateChanged(State oldState, State newState) {

        // Update Nearby Connections to the new state.
        switch (newState)
        {
            case SEARCHING:
                disconnectFromAllEndpoints();
                startDiscovering();
                break;
            case CONNECTED:
                stopDiscovering();
                stopMonitorTimerTask();
                stopRestartDiscoveryTimerTask();
                break;
            case UNKNOWN:
                stopAllEndpoints();
                stopMonitorTimerTask();
                stopRestartDiscoveryTimerTask();
            case FAILURE:
                stopMonitorTimerTask();
                startRestartDiscoveryTimerTask();
                break;
            default:
                // no-op
                break;
        }



    }
    /**
     * Sets the device to discovery mode. It will now listen for devices in advertising mode. Either
    * out if we successfully entered this mode.
     */
    protected void startDiscovering() {


        stopDiscovering();
        DiscoveryOptions.Builder discoverOptionsBuilder= new DiscoveryOptions.Builder();
        discoverOptionsBuilder.setStrategy(getStrategy());

        mDiscoveredEndpoints.clear();

        mConnectionsClient
                .startDiscovery(
                        getServiceId(),
                        new EndpointDiscoveryCallback()
                        {
                            @Override
                            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                                logD(
                                        String.format(
                                                "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                                                endpointId, info.getServiceId(), info.getEndpointName()));

                                if (getServiceId().equals(info.getServiceId())) {
                                    if(!mDiscoveredEndpoints.containsKey(endpointId)) {
                                        Endpoint endpoint = new Endpoint(endpointId, info.getEndpointName());
                                        mDiscoveredEndpoints.put(endpointId, endpoint);
                                        onEndpointDiscovered(endpoint);
                                    }
                                }
                            }

                            @Override
                            public void onEndpointLost(String endpointId) {

                                setState(State.FAILURE);
                                logD(String.format("onEndpointLost(endpointId=%s)", endpointId));
                            }
                        },
                        discoverOptionsBuilder.build())

                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                onDiscoveryStarted();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logD("startDiscovering() failed."+ e);

                                if(context!=null) {
                                    Toast.makeText(context, "Unable to start discovery " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }//Toast.makeText(context,"Unable to start discovery "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                onDiscoveryFailed();
                            }
                        });

        startMonitorTimerTask();
    }


    protected void onDiscoveryStarted() {
        logD("Discovery is started");
    }

    protected void onDiscoveryFailed() {

        setState(State.FAILURE);
        logD("Discovery is failed");
    }


    protected void onEndpointDiscovered(Endpoint endpoint) {
        // We found an advertiser!
        stopDiscovering();
        connectToEndpoint(endpoint);
    }

    /**
     * * ConnectionInfo)} or {@link #onConnectionFailed(Endpoint)} will be called once we've found out
     * if we successfully reached the device.
     */
    protected void connectToEndpoint(final Endpoint endpoint) {

        // Mark ourselves as connecting so we don't connect multiple times


        // Ask to connect
        mConnectionsClient
                .requestConnection(getName(), endpoint.getId(), mConnectionLifecycleCallback)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.getCause();
                                logD(   "\nrequestConnection() failed.\n"+ e.getMessage());

                                onConnectionFailed(endpoint);
                            }
                        }
                );
    }

    /** Stops discovery. */
    protected void stopDiscovering() {

        if(mConnectionsClient!=null) {
            mConnectionsClient.stopDiscovery();
            logD("Stop Discovering");
        }

    }

    /** Disconnects from all currently connected endpoints. */
    protected void disconnectFromAllEndpoints() {
        for (Endpoint endpoint : mEstablishedConnections.values()) {
            mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
        }
        mEstablishedConnections.clear();
    }



    /** Resets and clears all state in Nearby Connections. */
    protected void stopAllEndpoints() {
        mConnectionsClient.stopAllEndpoints();
        mDiscoveredEndpoints.clear();
        mPendingConnections.clear();
        mEstablishedConnections.clear();
    }

    private void sendGetTextSettingsResponse()
    {
        try
        {

          JSONObject obj = ScrollTextSettingsModel.GetTextSettingsResponseJSON(context);

          send(Payload.fromBytes((getString(R.string.get_text_settings_response)+obj.toString()).getBytes("UTF-8")));

        }catch (Exception e)
        {
          e.printStackTrace();
        }
   }
    private void sendGetAudioSettingsResponse()
    {
        try
        {
            JSONObject obj = new AudioSettingsConstants().getAudioSettingsSMRequest(context);


            send(Payload.fromBytes((getString(R.string.play_offer_audio_response)+obj.toString()).getBytes("UTF-8")));

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private  void  updateAudioSettings(String info)
    {
        try {

            JSONObject jsonObject=new JSONObject(info);
            AudioSettingsConstants.updateBgAudioSettings(context,jsonObject.getBoolean(getString(R.string.play_audio)));


        }catch (Exception e)
        {

        }



    }
    private void getRequestedFiles(String mediaType)
    {

        File [] files=null;


        if(mediaType.equalsIgnoreCase(getString(R.string.media)))
        {
            files=ConnectingNearBySMMOdel.getMediaFiles(context);
        }

        try {

            if (files != null)
            {
                JSONObject obj = new JSONObject();
                JSONArray mJSONArray = new JSONArray(Arrays.asList(files));
                obj.put("files", mJSONArray);

                send(Payload.fromBytes((getString(R.string.delete_response)+obj.toString()).getBytes("UTF-8")));


            }
            else {
                JSONObject obj = new JSONObject();
                JSONArray mJSONArray = new JSONArray(Arrays.asList(new File[]{}));
                obj.put("files", mJSONArray);

                send(Payload.fromBytes((getString(R.string.delete_response)+obj.toString()).getBytes("UTF-8")));

            }
        }catch (Exception e)
        {
           Toast.makeText(context,"Unable to send response "+e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void send(Payload payload) {

    send(payload, mEstablishedConnections.keySet());
    }

    public void send(Payload payload, Set<String> endpoints)
    {
        try
        {


            mConnectionsClient
                    .sendPayload(new ArrayList<>(endpoints), payload)
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    e.printStackTrace();
                                    logD("sendPayload() failed."+ e.getMessage());
                                    Toast.makeText(context,"Unable to send payload "+e.getMessage(),Toast.LENGTH_SHORT).show();

                                }
                            });

        }catch (Exception e)
        {
            logD("sendPayload exception"+e.getMessage()+"");
            Toast.makeText(context,"Unable to send payload "+e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }

    }

    private  ArrayList<File> getFiles(String response)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);

            JSONArray jsonArray=jsonObject.getJSONArray("files");

            int len = jsonArray.length();

            ArrayList<File> list=new ArrayList<File>();


            if (jsonArray != null) {

                for (int i=0;i<len;i++)
                {

                    list.add(new File(jsonArray.get(i).toString()));
                }

                return list;
            }
            else
            {
                return null;
            }

        }catch (Exception e)
        {

            e.printStackTrace();
            return null;

        }
    }

    private void deleteAction(ArrayList<File> files)
    {

        for ( final File file : files )
        {

            String fileName=file.getName();

            if( fileName.endsWith(getString(R.string.media_txt)))
            {
                if(!fileName.startsWith(getString(R.string.url_txt)))
                {
                    deleteResources(file);

                }
                else
                {
                  //  logD( "not require text file " + file.getAbsolutePath() );
                }

            }

            if ( !file.delete() )
            {
               // logD( "Can't remove " + file.getAbsolutePath() );
            }

        }
    }

    private void deleteResources(File file)
    {
        try {
            String processedText = new MediaModel().readTextFile(file.getPath());

            Log.i("ProcessTxtFile", "processed text is----------" + processedText);

            if (processedText != null)
            {
                JSONObject jsonObject = new JSONObject(processedText);

                String type = jsonObject.getString("type");

                if(type.equalsIgnoreCase(getString(R.string.app_default_multi_region))) {
                    new MultiRegionSupport(ConnectingNearBySMService.this).deleteResources(jsonObject);
                }


                if(jsonObject.has(getString(R.string.media_resource_json_key)))
                {
                    String resource = jsonObject.getString(getString(R.string.media_resource_json_key));

                    if (resource != null) {
                        String filePath = new DownloadMediaHelper().getAdsKiteNearByDirectory(context) + "/" + resource;

                        if (new File(filePath).exists()) {
                            if (!new File(filePath).delete()) {
                                logD("Can't resources remove " + file.getAbsolutePath());
                            }

                        }

                    }
                }

                if(jsonObject.has(getString(R.string.bg_audio_json_key)))
                {
                    String resource = jsonObject.getString(getString(R.string.bg_audio_json_key));

                    if (resource != null) {
                        String filePath = new DownloadMediaHelper().getAdsKiteNearByDirectory(context) + "/" + resource;

                        if (new File(filePath).exists()) {
                            if (!new File(filePath).delete()) {
                                logD("Can't resources remove " + file.getAbsolutePath());
                            }

                        }

                    }
                }


            }
            else
            {
                logD( "processedText null" + file.getAbsolutePath() );

            }

        }catch (Exception e)
        {

            Log.d("Delete","Inside Multi region support delete resources inside  exception "+e.getMessage());
            e.printStackTrace();

        }


    }



    private void saveTextTicker(String  textInfo)
    {
        try {

           ScrollTextSettingsModel.updateTextSettingsFromSM(context,textInfo);

    }catch (Exception e)
    {
        Toast.makeText(context, "Unable to update the settings", Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }

    }

    private  synchronized void sendUpdatesToPlayer(Payload payload)
    {
        try
        {
            Log.d("announcement","inside send updates to player");

             SignageServe.signageServeObject.setStreamingPayload(payload);

            Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(getString(R.string.action),getString(R.string.start_streaming));

            sendBroadcast(intent);
        }catch (Exception e)
        {

            e.printStackTrace();
        }

    }


    private void getFilesToModify(int offset)
    {
        GetModifyFilesService.getFilesToModify(context,new HandleGetModifyFilesServiceReceiver(this),offset);

    }



    class RequestedData extends AsyncTask<Void,Void,String>
    {
        private File file;

        protected RequestedData(File file)
        {
            this.file=file;
        }

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(Void... values)
        {
            try
            {

                if(file!=null)
                {
                    MediaInfo mediaInfo = new MediaInfo();
                    String fileName = (file.getName()).toLowerCase();
                    mediaInfo.setMediaName(fileName);
                    String filePath = (file).getPath();

                    if (mediaInfo !=null)
                    {
                        String resourcesString = new MediaModel().readTextFile(filePath);
                        mediaInfo.setFileData(resourcesString);
                        Log.d("onPostExecute","resourcesString--------"+resourcesString);

                        String obj=getMediaJsonString(mediaInfo);

                        return obj;

                    }
                    else
                    {
                        Log.d("","outpout  media info is null");
                        return null;
                    }
                }
                else
                {
                    Log.d("","outpout  getRequestedFilesToModify file not found");
                    return null;
                }

            }catch ( Exception e)
            {
                Log.d("","outpout  getRequestedFilesToModify json no data found Exception");
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(String obj)
        {
            try {
                 if(obj!=null)
                     Log.d("onPostExecute","resourcesString--------"+obj);
                  send(Payload.fromBytes((getString(R.string.modify_action_response) + obj).getBytes("UTF-8")));
            }catch ( Exception e)
            {
                Log.d("","outpout  getRequestedFilesToModify json no data found Exception");
                e.printStackTrace();
            }

        }
    }

    private String getMediaJsonString(MediaInfo info)
    {
        try
        {
            JSONObject obj = new JSONObject();
            //obj.put(context.getString(R.string.app_default_media_type),info.getMediaType());
            obj.put(context.getString(R.string.app_default_media_name),info.getMediaName());
            obj.put(context.getString(R.string.app_default_media_file_data),info.getFileData());
            return obj.toString();
        }catch (Exception e)
        {
            return null;
        }
    }

    private void checkRestartSMService()
    {
        if (SignageMgrAccessModel.isSignageMgrAccessOn(context,getString(R.string.sm_access_near_by_mode)))
        {
            AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);

            Intent intent = new Intent(context,CheckAndRestartSMService.class);


            PendingIntent reminderPI = PendingIntent.getService(context, RESTART_SM_SERVICE_ALARM_ACTION,intent,0);

            am.set(AlarmManager.RTC_WAKEUP,((Calendar.getInstance().getTimeInMillis())+ TimeUnit.MINUTES.toMillis(1)),reminderPI);
        }
    }



    private void startMonitorTimerTask()
    {
        logD("start Monitor Timer Task after 3min");
       if(isServiceOn) {
           monitorDiscoveryTimerTask = new Timer();
           monitorDiscoveryTimerTask.schedule(new TimerTask() {
               @Override
               public void run() {
                   setState(State.FAILURE);
               }
           }, MonitorDiscoveryTime);
       }
    }

    private void stopMonitorTimerTask()
    {
        logD("stop Monitor Timer Task (3min)");

        try {
            if (monitorDiscoveryTimerTask != null) {
                monitorDiscoveryTimerTask.cancel();
                monitorDiscoveryTimerTask.purge();
            }
        }catch (Exception e)
        {

        }finally
        {
            monitorDiscoveryTimerTask = null;
        }


    }


    private void startRestartDiscoveryTimerTask()
    {
        stopDiscovering();

        logD("start Restart Discovery Timer Task after 20 seconds");

        if( isServiceOn) {

            restartDiscoveryTimerTask = new Timer();
            restartDiscoveryTimerTask.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (mState == State.FAILURE && new SignageMgrAccessModel().isSignageMgrAccessOn(context,getString(R.string.sm_access_near_by_mode))) {
                        startDiscovering();
                    }

                }
            }, RestartDiscoveryTime);
        }

    }

    private void stopRestartDiscoveryTimerTask()
    {
        logD("Stop Restart Discovery Timer Task after 20 seconds");


        try {
            if (restartDiscoveryTimerTask != null) {
                restartDiscoveryTimerTask.cancel();
                restartDiscoveryTimerTask.purge();
            }
        }catch (Exception e)
        {

        }finally {
            restartDiscoveryTimerTask = null;
        }


    }

    //push all action request to the DisplayFloderAds Activity to do multiple operations



    private void interactiveActions(String settingsJson)
    {
        try {
            String action=null;
            String extraInfo=null;

            if(settingsJson.startsWith(getString(R.string.customer_interactive_update_actions_text_request)))
            {
                action=getString(R.string.customer_interactive_update_actions_text_request);
                extraInfo=settingsJson.replace(action,"");
                ActionModel.updateCustomerActionText(context,extraInfo);

            }else if(settingsJson.startsWith(getString(R.string.customer_interactive_actions_close_request)))
            {
                action=getString(R.string.customer_interactive_actions_close_request);
                extraInfo=settingsJson.replace(action,"");
                ActionModel.updateCustomerActionStatus(extraInfo,context);

            }

            Intent intent = new Intent(DisplayLocalFolderAds.SM_UPDATES_INTENT_ACTION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(getString(R.string.action),action);
            intent.putExtra(getString(R.string.action_extra_info),extraInfo);
            sendBroadcast(intent);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void sendActionsDataList(String status,String resultReturnTag)
    {
        try
        {

            String requestCustomerJson=new ActionModel().getRequestedActionsDataJson(context,status);
            send(Payload.fromBytes((resultReturnTag+requestCustomerJson).getBytes("UTF-8")));

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    //handle media settings request
    private void handleMediaSettingsRequest(String payloadString)
    {
        HandleMediaSettingsService.startHandleMediaSettingsService(context,payloadString);
    }

    private void deviceSettingsHandler(String actionRequest)
    {
        try {
            actionRequest=actionRequest.replace(getString(R.string.device_volume_change_request),"");
            boolean flag=Boolean.parseBoolean(actionRequest);
            deviceVolumeControlAction(flag);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private void deviceVolumeControlAction(boolean flag)
    {
        try {

            final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            int i=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            //Log.i("AudioManager","AudioManager STREAM_MUSIC:"+i);

            if(flag)
            {
                ++i;
                //To increase media player volume STREAM_MUSIC
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
                audioManager.setStreamVolume(AudioManager.STREAM_RING,i,0);

            }else
            {
                --i;
                //To decrease media player volume
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
                audioManager.setStreamVolume(AudioManager.STREAM_RING,i,0);
            }

        }catch (Exception e)
        {
            e.printStackTrace();

        }

    }

    private void handleSSModeSettingsRequest(String ssModeString)
    {

        if(ssModeString.equalsIgnoreCase(getResources().getString(R.string.ss_mode_settings_request)))
        {
            String mode=SignageMgrAccessModel.getSelectedSMMode(context);

            try {

                send(Payload.fromBytes((getResources().getString(R.string.ss_mode_settings_response)+mode).getBytes("UTF-8")));
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }else if(ssModeString.contains(getResources().getString(R.string.ss_mode_change_request)))
        {
            String mode=ssModeString.replace(getResources().getString(R.string.ss_mode_change_request),"");
            changeSSConnectionMode(mode);
        }

    }

    private void changeSSConnectionMode(String mode)
    {
        Intent intent=new Intent(context,ToggleSMServices.class);
        intent.putExtra("switch_to",mode);
        startService(intent);
    }



}
