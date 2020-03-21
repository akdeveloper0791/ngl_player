package com.ibetter.www.adskitedigi.adskitedigi.login;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.fcm.MyFirebaseMessagingService;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils.GC_PLAYER_REGISTER_URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GCRegisterDeviceService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.ibetter.www.adskitedigi.adskitedigi.login.action.FOO";


    // TODO: Rename parameters
    private static final String userEmail_PARAM = "userEmail_PARAM";
    private static final String pwdPARAM = "pwdPARAM";
    private static final String isAdsKitePARAM = "isAdsKitePARAM";
    private static final String dataPARAM = "data";
    private static final String intentActionPARAM = "intentAction";
    private static final String display_name_PARAM = "display_name";
    private static final String playing_mode_PARAM = "playing_mode";
    private static final String url_PARAM = "url";


    private String intentAction ,enterpriseUrl;

    private String displayName;

    private int playingMode;

    private String pwd;

    private Context context;

    public GCRegisterDeviceService() {
        super("GCRegisterDeviceService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startAction(Context context,int mode,String url,String displayName, String userEmail, String pwd,boolean isAdsKite,String data,
                                   String intentAction) {
        Intent intent = new Intent(context, GCRegisterDeviceService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(userEmail_PARAM, userEmail);
        intent.putExtra(display_name_PARAM, displayName);
        intent.putExtra(playing_mode_PARAM, mode);
        intent.putExtra(pwdPARAM, pwd);
        intent.putExtra(isAdsKitePARAM, isAdsKite);
        intent.putExtra(dataPARAM, data);
        intent.putExtra(intentActionPARAM,intentAction);
        intent.putExtra(url_PARAM,url);
        context.startService(intent);

    }



    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            context = GCRegisterDeviceService.this;

            displayName = intent.getStringExtra(display_name_PARAM);
            intentAction = intent.getStringExtra(intentActionPARAM);
            enterpriseUrl = intent.getStringExtra(url_PARAM);
            pwd = intent.getStringExtra(pwdPARAM);
            playingMode = intent.getIntExtra(playing_mode_PARAM, Constants.CLOUD_MODE);
            //prepare data
            String registerData = intent.getStringExtra(dataPARAM);
            LatLng deviceLocation = User.getDeviceLocation(context);
            if(deviceLocation!=null)
            {
                try {
                    JSONObject registerDataJSON = new JSONObject(registerData);
                    registerDataJSON.put("player_lat", deviceLocation.latitude);
                    registerDataJSON.put("player_lng", deviceLocation.longitude);
                    Log.d("Register","Inside register service , register data "+registerDataJSON.toString());
                    registerData = registerDataJSON.toString();
                }catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
            }


            Log.d("Register","registerData - "+registerData);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_email", intent.getStringExtra(userEmail_PARAM))
                    .addFormDataPart("pwd", intent.getStringExtra(pwdPARAM))
                    .addFormDataPart("isAdskite", String.valueOf(intent.getBooleanExtra(isAdsKitePARAM, false)))
                    .addFormDataPart("data", registerData)
                    .build();

            String URL = null;
            if (playingMode == Constants.CLOUD_MODE) {
                URL = GC_PLAYER_REGISTER_URL;
            } else {


                if (enterpriseUrl != null && enterpriseUrl.length() > 1) {
                    URL = enterpriseUrl + "player/register";
                }
            }

            Log.i("url",URL+"");


            if (URL != null) {
                Request request = new Request.Builder()
                        .post(requestBody)
                        .url(URL)
                        .build();

                OkHttpClient httpClient = new OkHttpClient.Builder()
                        .connectTimeout(1, TimeUnit.MINUTES)
                        .writeTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.MINUTES)
                        .build();

                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        sendStatus(false, "Unable to register , please check your internet and try again");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseString = response.body().string().trim();
                        //Log.d("TestRestApi","response in api is - "+responseString.trim());
                        handleResponse(responseString);
                    }
                });
            } else {
                sendStatus(false, "Url is not found");
            }


        }catch (Exception E)
        {
            sendStatus(false,"Unable to login"+E.getMessage());
            E.printStackTrace();
        }
    }

    private void handleResponse(String response)
    {
        try
        {
            JSONObject info = new JSONObject(response);

            Log.i("info response",response);
            if(info.getInt("statusCode")==0)
            {
                JSONObject infoObject = info.getJSONObject("info");
                //success,, save response to cache
                SharedPreferences.Editor userInfoEditor = new SharedPreferenceModel().getUserDetailsSharedPreference(this).edit();

                userInfoEditor.putString(getString(R.string.gc_user_unique_key),infoObject.getString("user_unique_key"));
               userInfoEditor.putString(getString(R.string.gc_user_first_name),infoObject.getString("first_name"));
                userInfoEditor.putInt(getString(R.string.player_id),info.getInt("player"));
                userInfoEditor.putString(getString(R.string.player_mac),info.getString("mac"));
                userInfoEditor.putString(getString(R.string.user_display_name),displayName);
                userInfoEditor.putString(getString(R.string.user_display_name),displayName);
                userInfoEditor.putInt(getString(R.string.playing_media_mode),playingMode);

                if(playingMode==Constants.ENTERPRISE_MODE) {
                    userInfoEditor.putString(getString(R.string.enter_prise_url),enterpriseUrl);
                    userInfoEditor.putString(getString(R.string.enterprise_user_email_id),infoObject.getString("email"));
                    userInfoEditor.putString(getString(R.string.enterprise_user_password),pwd);

                }
                else
                {
                    userInfoEditor.putString(getString(R.string.gc_user_email_id),infoObject.getString("email"));
                    userInfoEditor.putString(getString(R.string.gc_user_password),pwd);

                }
                String uploadedMac = info.getString("fcm");
                String savedFCM = MyFirebaseMessagingService.getToken(this);

                if(uploadedMac!=null && savedFCM!=null && uploadedMac.equalsIgnoreCase(savedFCM))
                {

                    //update status to true
                    MyFirebaseMessagingService.setFCMUpdateStatus(true,this);
                }

                boolean isCommit = userInfoEditor.commit();

                if(isCommit)
                {
                    sendStatus(true,info.getString("status"));

                }else
                {
                    sendStatus(false,"Unable to register, please try again later");
                }


            }else
            {
                sendStatus(false,info.getString("status"));
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
            sendStatus(false,"Unable to register"+e.getMessage());
        }
    }

    private void sendStatus(boolean flag,String status)
    {
        Intent intent = new Intent(intentAction);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("flag",flag);
        intent.putExtra("status",status);
        sendBroadcast(intent);

    }


}
