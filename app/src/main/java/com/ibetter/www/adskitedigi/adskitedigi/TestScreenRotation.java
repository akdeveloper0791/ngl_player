package com.ibetter.www.adskitedigi.adskitedigi;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

public class TestScreenRotation extends Activity {

    private int[] orientations = new int[]{ActivityInfo.SCREEN_ORIENTATION_BEHIND,
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,ActivityInfo.SCREEN_ORIENTATION_FULL_USER,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_LOCKED,
            ActivityInfo.SCREEN_ORIENTATION_NOSENSOR,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            ActivityInfo.SCREEN_ORIENTATION_SENSOR,ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            ActivityInfo.SCREEN_ORIENTATION_USER,ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE,ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT};

    private int currentPosition = 0;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

      /* new Timer().schedule(new TimerTask() {
           @Override
           public void run() {
              if(orientations.length>currentPosition)
              {
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          Toast.makeText(TestScreenRotation.this,"changing orientation to "+orientations[currentPosition],
                                  Toast.LENGTH_SHORT).show();
                          setRequestedOrientation(orientations[currentPosition]);
                          ++currentPosition;
                      }
                  });
              }else
              {
                  currentPosition = 0;//start from 0;
              }
           }
       },15000,15000);*/

    }

    public void onConfigurationChanged(Configuration newConfiguration)
    {
        super.onConfigurationChanged(newConfiguration);
        Toast.makeText(this,"New configuration changed -"+newConfiguration.orientation,Toast.LENGTH_SHORT).show();
    }
}
