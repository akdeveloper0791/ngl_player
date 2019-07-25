package com.ibetter.www.adskitedigi.adskitedigi.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import java.io.File;

/**
 * Created by vineeth_ibetter on 1/12/18.
 */

public class TestActivity extends Activity
{

VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_layout);


    }

    private class RecursiveFileObserverBack extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {

            ( new  DirectoryFileObserver(new File("/storage/sdcard0/WhatsApp/Media").getAbsolutePath())).startWatching();

            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public class DirectoryFileObserver extends FileObserver
    {

        String aboslutePath = "path to your directory";

        public DirectoryFileObserver(String path)
        {
            super(path);
            aboslutePath = path;

            Log.i("info file ","inside");

        }

        @Override
        public void onEvent(int event, String path)
        {

            //if(event == FileObserver.CREATE && !file.equals(".probe")){ // check if its a "create" and not equal to .probe because thats created every time camera is launched
            Log.d("file", "File created [" + aboslutePath + path + "]");

            Toast.makeText(getBaseContext(), path + " was saved!", Toast.LENGTH_LONG).show();


            if(path != null)
            {

                if ((FileObserver.CREATE & event)!=0) {
                   Log.d("info file ","CREATE");
                }

                //a file or directory was opened
                if ((FileObserver.OPEN & event)!=0) {
                    Log.d("info file ","open");
                    //TODO Nothing... yet
                }

                //data was read from a file
                if ((FileObserver.ACCESS & event)!=0) {
                    //TODO Nothing... yet
                    Log.d("info file ","access");
                }

                //[todo: consider combine this one with one below]
                //a file was deleted from the monitored directory
                if ((FileObserver.DELETE & event)!=0) {
                    //TODO Remove file from the server
                    Log.d("info file ","delete");
                }

                //the monitored file or directory was deleted, monitoring effectively stops
                if ((FileObserver.DELETE_SELF & event)!=0) {
                    Log.d("info file ","DELETE_SELF");
                    //TODO Toast an error, recreate the folder, resync and restart monitoring
                }

                //a file or subdirectory was moved from the monitored directory
                if ((FileObserver.MOVED_FROM & event)!=0) {
                    Log.d("info file ","MOVED_FROM");
                    //TODO Delete from the server
                }

                //the monitored file or directory was moved; monitoring continues
                if ((FileObserver.MOVE_SELF & event)!=0) {
                    Log.d("info file ","MOVE_SELF");
                    //TODO Recreate the folder and show toast
                }
                //Metadata (permissions, owner, timestamp) was changed explicitly
                if ((FileObserver.ATTRIB & event)!=0) {
                    Log.d("info file ","ATTRIB");
                    //TODO Nothing... Yet
                }

                Log.d("FileObserver: ","File effetcted");

            }
            else
            {
                Log.d("fail FileObserver: ","File effetcted");
            }

        }

    }


    public  void setticker(String text, Context contx) {
        if (text != "") {
            LinearLayout parent_layout = (LinearLayout) ((Activity) contx)
                    .findViewById(R.id.ticker_area);

            TextView view = new TextView(contx);
            view.setText(text);

            view.setTextColor(Color.BLACK);
            view.setTextSize(25.0F);
            Context context = view.getContext(); // gets the context of the view

            // measures the unconstrained size of the view
            // before it is drawn in the layout
            view.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);

            // takes the unconstrained width of the view
            float width = view.getMeasuredWidth();
            float height = view.getMeasuredHeight();

            // gets the screen width
            float screenWidth = ((Activity) context).getWindowManager()
                    .getDefaultDisplay().getWidth();

            view.setLayoutParams(new LinearLayout.LayoutParams((int) width,
                    (int) height, 1f));

            System.out.println("width and screen width are" + width + "/"
                    + screenWidth + "///" + view.getMeasuredWidth());

            // performs the calculation
            float toXDelta = width - (screenWidth - 0);

            // sets toXDelta to -300 if the text width is smaller that the
            // screen size
            if (toXDelta < 0) {
                toXDelta = 0 - screenWidth;// -300;
            } else {
                toXDelta = 0 - screenWidth - toXDelta;// -300 - toXDelta;
            }

            // Animation parameters
            Animation mAnimation = new TranslateAnimation(screenWidth,
            toXDelta, 0, 0);
            mAnimation.setDuration(15000);
            mAnimation.setRepeatMode(Animation.RESTART);
            mAnimation.setRepeatCount(Animation.INFINITE);
            view.setAnimation(mAnimation);
            parent_layout.addView(view);

        }
    }

    //on completion video listener for video view
    private void videoViewListener()
    {

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra)
            {
                switch (what)
                {

                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        Log.i("error", "media error unknown");

                        break;

                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        Log.i("error", "media error server died");

                        break;

                }

                return true;

            }
        });

    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                Toast.makeText(TestActivity.this,"Video completed",Toast.LENGTH_SHORT).show();
            }

        });
    }




}
