package com.ibetter.www.adskitedigi.adskitedigi.settings.display_report_image_duration;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import java.util.concurrent.TimeUnit;

/**
 * Created by vineethkumar0791 on 28/02/18.
 */

public class DisplayReportImageDurationSettings extends Activity
{

        private Context context;
        private EditText imageDurationEt;
        private Button updateButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.display_image_duration_settings);

        context=DisplayReportImageDurationSettings.this;
        imageDurationEt=(EditText)findViewById(R.id.duration_et);
        updateButton=(Button)findViewById(R.id.update);

         setLabels();
         displayPreviousDuration();

         updateDuration();

         setActionBar();

    }


    private  void setLabels()
    {
        TextView imageDurationTv=(TextView)findViewById(R.id.delivery_date_info);
        imageDurationTv.setText("Display Report Image Duration(Sec's)");

    }
    public void updateDuration()
    {
        updateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                long duration= Constants.convertToLong(imageDurationEt.getText().toString());
                long durationInMs= TimeUnit.SECONDS.toMillis(duration);
                if(durationInMs>0)
                {
                    if(new User().updateReportImageDuration(context,durationInMs))
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

    private void displayPreviousDuration()
    {

        long duration=TimeUnit.MILLISECONDS.toSeconds(new User().getReportImageDuration(context));

        User.appendStringToEditTextAtCursorPosition(imageDurationEt," "+String.valueOf(duration));

    }


    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Display Image Report Duration Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
