package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.DateTimeModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.MediaModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpletResult;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EnterPriseFTPFileObserver extends DefaultFtplet {

    private Context context;
    private WeakReference<EnterPriseSettingsService> activityWeakReference;

    public EnterPriseFTPFileObserver(Context context, EnterPriseSettingsService activity) {
        this.context = context;
        activityWeakReference = new WeakReference<>(activity);
    }


    @Override
    public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException {

        String command = request.getCommand();

        Log.d("FileDownloadObserver", "Inside on after command command- " + request.getCommand());
        Log.d("FileDownloadObserver", "Inside on after command session id - " + session.getSessionId());
        Log.d("FileDownloadObserver", "Inside on after command arguments- " + request.getArgument());
        Log.d("FileDownloadObserver", "Inside on after command received time- " + request.getReceivedTime());

        if(session.getSessionId()!=null) {

            switch (command) {
                case "STOR":
                    Log.d("FileDownloadObserver"," FIle Obser"+"Inside FileObserver command STOR");
                    checkAndProcessDownloadedFile(request);
                    break;
            }
        }

        return super.afterCommand(session, request, reply);
    }

    private void checkAndProcessDownloadedFile(FtpRequest request)
    {
        if(request!=null)
        {

            String argument = request.getArgument();
            Log.d("FTP FIle Obser","Inside FileObserver command argument -"+argument);


            if(argument!=null)
            {
                if (isFileArgument(argument))
                {

                        if (argument.startsWith(context.getString(R.string.ftp_command_file_prefix)))
                        {  if (activityWeakReference != null && activityWeakReference.get() != null) {
                            activityWeakReference.get().handleCommand(argument);}
                        }

                    }
                else if (isCampaignFile(argument))
                {
                    Log.i(" FileDownloadObserver ", "media   " + argument);

                    File mediaFile = new File(new User().getUserPlayingFolderModePath(context) + File.separator + argument);

                    if (mediaFile.exists()) {
                        String processedText = new MediaModel().readTextFile(mediaFile);

                        String extension = argument.substring(argument.lastIndexOf("."));//extension
                        extension =  extension.replace(".","");


                        String mediaName=argument.replace("."+extension,"");

                        saveCampaignInfoInDB(processedText,mediaName);
                        Log.i("FileDownloadObserver ", "processed text" + processedText);
                    }
                }
                else if (isBgAudioFile(argument))
                {
                    if (activityWeakReference != null && activityWeakReference.get() != null)
                    {
                        activityWeakReference.get().saveBGAudioFile(argument);
                    }
                }
                else if(isAnnouncementFile(argument))
                {
                    if (activityWeakReference != null && activityWeakReference.get() != null)
                    {
                        activityWeakReference.get().playAnnouncement(argument);
                    }
                }
            }
        }

    }

    private boolean isCampaignFile(String name)
    {
        String extension = name.substring(name.lastIndexOf("."));//extension
        extension =  extension.replace(".","");

        return extension != null && extension.equalsIgnoreCase(context.getString(R.string.media_txt))&&(!(name.startsWith("DNDM")));
    }
    //is File argument
    private boolean isFileArgument(String name)
    {
        if(name!=null && name.contains(".")) {

            String extension = name.substring(name.lastIndexOf("."));//extension
            extension =  extension.replace(".","");
            Log.d("FTP FIle Obser", "Inside FileObserver command argument extension " + extension);
            return (extension != null && extension.equalsIgnoreCase(context.getString(R.string.media_json_txt)));
        }else
        {
            return false;
        }

    }

    //is bg audio file
    private boolean isBgAudioFile(String name)
    {
        return (name!=null && name.startsWith(context.getString(R.string.bg_audio_file)));
    }

    private boolean isAnnouncementFile(String name)
    {
        return (name!=null && name.startsWith(context.getString(R.string.announcement_request)));

    }
    private void saveCampaignInfoInDB(String mediaText,String mediaName)
    {
        try {

            ContentValues cv = new ContentValues();
            cv.put(CampaignsDBModel.CAMPAIGN_TABLE_CAMPAIGN_INFO, mediaText);
            cv.put(CampaignsDBModel.CAMPAIGN_TABLE_IS_CAMPAIGN_DOWNLOADED, 1);
            cv.put(CampaignsDBModel.CAMPAIGNS_TABLE_IS_SKIP, 0);
            cv.put(CampaignsDBModel.CAMPAIGNS_TABLE_UPDATED_DATE, new DateTimeModel().getDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"), Calendar.getInstance().getTimeInMillis()));

            if(!CampaignsDBModel.isCampaignNameExist(context,mediaName)) {

                cv.put(CampaignsDBModel.CAMPAIGNS_TABLE_CAMPAIGN_NAME, mediaName);
                cv.put(CampaignsDBModel.CAMPAIGNS_TABLE_CREATED_DATE, new DateTimeModel().getDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"), Calendar.getInstance().getTimeInMillis()));

                CampaignsDBModel.insertCampaign(cv, context);
                Log.i("FileDownloadObserver ", "campain is inserted" + mediaName);

            }else
            {
                CampaignsDBModel.updateCampaign(cv, context,mediaName);
                Log.i("FileDownloadObserver ", "campain is updated" + mediaName);

            }


        }catch (Exception E)
        {
            E.printStackTrace();
        }

    }
}
