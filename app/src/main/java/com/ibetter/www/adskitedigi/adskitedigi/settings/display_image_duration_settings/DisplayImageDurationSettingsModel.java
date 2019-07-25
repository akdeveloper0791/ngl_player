package com.ibetter.www.adskitedigi.adskitedigi.settings.display_image_duration_settings;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.util.concurrent.TimeUnit;

/**
 * Created by vineeth_ibetter on 1/24/18.
 */

public class DisplayImageDurationSettingsModel
{
   private  Context context;
   private EditText imageDurationEt;
   private Button updateButton;

    public DisplayImageDurationSettingsModel(Context context, EditText imageDurationEt, Button updateButton) {
        this.context = context;
        this.imageDurationEt = imageDurationEt;
        this.updateButton = updateButton;

        displayPreviousDuration();
    }

    public Context getContext() {
        return context;
    }

    public EditText getImageDurationEt() {
        return imageDurationEt;
    }

    public Button getUpdateButton() {
        return updateButton;
    }

    private void displayPreviousDuration()
    {

        long duration=TimeUnit.MILLISECONDS.toSeconds(new User().getImageDuration(context));

        User.appendStringToEditTextAtCursorPosition(imageDurationEt," "+String.valueOf(duration));

    }

    public void updateDuration()
    {
        updateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                long duration= Constants.convertToLong(imageDurationEt.getText().toString());
                long durationInMs=TimeUnit.SECONDS.toMillis(duration);
                if(durationInMs>0)
                {
                    if(new User().updateImageDuration(context,durationInMs))
                    {
                        Toast.makeText(context,"Updated",Toast.LENGTH_SHORT).show();
                    }else
                    {
                        Toast.makeText(context,"Unable to update",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    imageDurationEt.setError("Please Enter Duration");
                }

            }

        });

    }

}
