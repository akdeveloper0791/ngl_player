package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.service_receivers;

import android.content.Context;

import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.File;
import java.util.TimerTask;

public class DeleteResponseFileTimerClass extends TimerTask {

    private String responseFileName;
    private Context context;

    public DeleteResponseFileTimerClass(Context context,String responseFileName)
    {
        this.responseFileName = responseFileName;
        this.context = context;
    }

    public void run()
    {
        File responseFile = new File(new User().getUserPlayingFolderModePath(context)+
                File.separator+responseFileName);
        if(responseFile!=null && responseFile.exists())
        {
            responseFile.delete();
        }
    }
}
