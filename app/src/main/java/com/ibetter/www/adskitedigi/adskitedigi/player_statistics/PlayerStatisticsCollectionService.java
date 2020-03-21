package com.ibetter.www.adskitedigi.adskitedigi.player_statistics;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignReportsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.GCNotification;
import com.ibetter.www.adskitedigi.adskitedigi.model.DateTimeModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.PLAYER_STATISTICS_COLLECTION_SERVICE_NOTIFY_ID;

public class PlayerStatisticsCollectionService extends Service implements PlayerStatisticsCollectionRx.CallBack
{
   private Context context;
    public static String TAG = "Player Statistics";
    public static boolean isServiceOn = false;

    public long startingTime=0;

    public static PlayerStatisticsCollectionRx playerStatisticsCollectionRx;

    @Override
    public void onCreate() {
        context = PlayerStatisticsCollectionService.this;

        initRx();
        checkAndStartForegroundNotification();

        startingTime= Calendar.getInstance().getTimeInMillis();

        Log.i(TAG, "start service");
        isServiceOn = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //upload service

        String collectionString=preparePlayerStaticsCollection();

        if(collectionString!=null)
        {
            Log.i("collectionString",collectionString);
            uploadStatisticsCollection(collectionString);

        }else
        {
            onCompleted();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "stop report  service");

       stopForeground(true);
        isServiceOn = false;
        super.onDestroy();
    }

    //stop RequiredFiles Service
    public void stopUploadService() {
        Intent intent = new Intent(UploadPlayerStatisticsCollectionService.STOP_SERVICE_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkAndStartForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(PLAYER_STATISTICS_COLLECTION_SERVICE_NOTIFY_ID, GCNotification.playerStatisticsServiceNotification(context,
                    "Upload Statistics Collection"));
        }
    }

    private String preparePlayerStaticsCollection()
    {
        try {
            Cursor cursor = CampaignReportsDBModel.getPlayerCampaignStatics(context,startingTime);

            if (cursor != null && cursor.moveToFirst())
            {

                JSONArray jsonArray = new JSONArray();

                do {
                    String campaignName = cursor.getString(cursor.getColumnIndex(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_CAMPAIGN_NAME));
                    long duration = cursor.getLong(cursor.getColumnIndex(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_TOTAL_DURATION));
                    String timesPlayed = cursor.getString(cursor.getColumnIndex(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_NO_TIMES_PLAYED));
                    long serverId = cursor.getLong(cursor.getColumnIndex(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_MAX_SERVER_ID));
                    long createdAt = cursor.getLong(cursor.getColumnIndex(CampaignReportsDBModel.CAMPAIGNS_REPORTS_TABLE_MAX_CREATED_AT));

                    JSONObject campaignInfoJsonObj = new JSONObject();

                    campaignInfoJsonObj.put("c_name", campaignName);
                    campaignInfoJsonObj.put("duration", duration);
                    campaignInfoJsonObj.put("times_played", timesPlayed);
                    campaignInfoJsonObj.put("last_played_at", new DateTimeModel().getDate(new SimpleDateFormat(getString(R.string.schedule_layout_expiry_date_format)),createdAt));

                    if (serverId > 0) {
                        campaignInfoJsonObj.put("c_server_id", serverId);
                    }

                    jsonArray.put(campaignInfoJsonObj);


                } while (cursor.moveToNext());


                if(jsonArray.length()>0)
                {
                    return jsonArray.toString();
                }else
                {
                   return null;
                }
            }
            else
            {
                return null;
            }

        }catch (Exception E)
        {
            E.printStackTrace();
            return null;
        }
    }

    private void uploadStatisticsCollection(String collectionString)
    {

        Intent intent=new Intent(context,UploadPlayerStatisticsCollectionService.class);
        intent.putExtra("collectionInfo",collectionString);
        startService(intent);
    }
    @Override
    public void uploadPlayerStatisticsReportResponse(Bundle values) {

        if(values.getBoolean("flag",false))
        {
            CampaignReportsDBModel.deletePrevReportsCollection(startingTime,context);
        }
        onCompleted();
    }

    @Override
    public void stopService(Bundle values) {
        stopUploadService();

        stopSelf();
    }

    private void initRx() {

        playerStatisticsCollectionRx = new PlayerStatisticsCollectionRx(new Handler(), PlayerStatisticsCollectionService.this, this);

    }

    private void onCompleted()
    {
        PlayerStatisticsCollectionModel.setUploadingCampReportsLastTime(context,Calendar.getInstance().getTimeInMillis());
        //PlayerStatisticsCollectionModel.checkRestartUploadCampaignReportsService(context);
        stopSelf();
    }

}
