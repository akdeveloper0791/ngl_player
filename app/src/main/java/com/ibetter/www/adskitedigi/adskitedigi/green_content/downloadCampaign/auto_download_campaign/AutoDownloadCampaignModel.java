package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import static android.content.Context.ALARM_SERVICE;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService.DOWNLOAD_CAMPAIGNS_PATH;
import static com.ibetter.www.adskitedigi.adskitedigi.model.AlarmConstants.AUTO_SYN_ALARM;
import static com.ibetter.www.adskitedigi.adskitedigi.model.AlarmConstants.CAPTURE_IMAGE_ALARM;

public class AutoDownloadCampaignModel
{
    public static void checkRestartAutoCampaignDownloadService(Context context)
    {

        if(new User().isAutoDownloadCampaignOn(context)&& User.isPlayerRegistered(context))
        {
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            Intent intent = new Intent(context, AutoDownloadCampaignTriggerService.class);

            PendingIntent reminderPI = PendingIntent.getService(context, AUTO_SYN_ALARM, intent, 0);

            am.set(AlarmManager.RTC_WAKEUP, ((Calendar.getInstance().getTimeInMillis()) + TimeUnit.MINUTES.toMillis(new User().getAutoCampaignDownloadDuration(context))), reminderPI);
        }

    }

    public static void stopAutoCampaignDownloadService(Context context)
    {

        AlarmManager am=(AlarmManager)context.getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(context, AutoDownloadCampaignTriggerService.class);

        PendingIntent reminderPI = PendingIntent.getService(context, AUTO_SYN_ALARM,intent,0);

        reminderPI.cancel();

        am.cancel(reminderPI);


        if(AutoDownloadCampaignTriggerService.isServiceOn)
        {

            AutoDownloadCampaignTriggerService.autoDownloadCampaignReceiver.send(AutoDownloadCampaignReceiver.STOP_SERVICE, null);

        }


    }


    //get media files to modify
    public static File[] getCampaignFiles(final Context context)
    {
        String dir=DOWNLOAD_CAMPAIGNS_PATH;
        if(dir!=null)
        {
            File dirFile=new File(dir);

            if(dirFile.exists()) {
                File[] files = dirFile.listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File file, String s) {
                                s = s.toLowerCase();

                                if ((
                                        s.endsWith(context.getString(R.string.media_txt))
                                ) && !s.startsWith(context.getString(R.string.do_not_display_media))
                                )
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

                //ascending order
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {

                        return file2.lastModified() > file1.lastModified() ? -1 : (file2.lastModified() < file1.lastModified()) ? 1 : 0;
                    }
                });


                return files;
            }else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

}
