package com.ibetter.www.adskitedigi.adskitedigi;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class DemoTouchEvents extends Activity implements KeyEvent.Callback, View.OnKeyListener
{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private String lightSensorReading;
    private String name = "";
    private String vendor;
    private String version;
    private String maxRange;
    private String minRange;
    private String resolution;
    private String power;
   // private HardwareAdapter hardwareAdapter;
    private ListView lightSensorListView;
    //private List<HardwareObject> lSensor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
        MediaButtonReceiver1 mediaButtonReceiver1 = new MediaButtonReceiver1();
        registerReceiver(mediaButtonReceiver1,intentFilter);


        MediaSessionCompat  mMediaSession = new MediaSessionCompat(this, "LOG_TAG");
        Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");


        mMediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast (this,
        0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT));

        mMediaSession.setCallback(new MediaSessionCompat.Callback()
        {
           public boolean  onMediaButtonEvent(Intent mediaButtonEvent)
           {
               Toast.makeText(DemoTouchEvents.this,"Inside mMediaSession set call back",Toast.LENGTH_SHORT).show();
               return true;
           }

           public void onCommand(String command, Bundle extras, ResultReceiver cb)
           {
               Toast.makeText(DemoTouchEvents.this,"Inside MediaSession onCommand",Toast.LENGTH_SHORT).show();
           }

           public void onCustomAction(String action, Bundle extras)
           {
               Toast.makeText(DemoTouchEvents.this,"Inside MediaSession onCustomAction",Toast.LENGTH_SHORT).show();
           }

        });

    }
    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop()
    {
        super.onStop();

       // startActivity(new Intent(this,DemoTouchEvents.class));

    }





    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_UP){

            Log.d("Dispatch key event","Inside dispatch key event - "+event.getKeyCode());

        }else
        {
            Log.d("Dispatch key event","Inside dispatch key event else- "+event.getKeyCode());
        }
        //Toast.makeText(this,"dispatch key event",Toast.LENGTH_SHORT).show();


        return true;
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        Toast.makeText(this,"onKeyDown",Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onKeyUp (int keyCode, KeyEvent event)
    {
        Toast.makeText(this,"onKeyUp",Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public  boolean onKey (View v, int keyCode, KeyEvent event)
    {
        Toast.makeText(this,"onKey",Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean 	onKeyLongPress(int keyCode, KeyEvent event)
    {
        Toast.makeText(this,"onKeyLongPress",Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event)
    {
        Toast.makeText(this,"onKeyMultiple",Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent (MotionEvent ev)
    {
        Toast.makeText(this,"dispatchGenericMotionEvent",Toast.LENGTH_SHORT).show();
        return true;
    }



    public class MediaButtonReceiver1 extends MediaButtonReceiver
    {

        public void onReceive(Context context,Intent intent)
        {
            Toast.makeText(context,"Inside MediaButtonReceiver1",Toast.LENGTH_SHORT).show();
        }
    }



}
