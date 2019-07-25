package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

public class WebViewAutoScrollTask extends TimerTask {
  private  WeakReference<DisplayLocalFolderAds> activityRef;
  int webViewId;

    public WebViewAutoScrollTask(DisplayLocalFolderAds activity, int webViewId)
    {
        activityRef = new WeakReference<>(activity);
        this.webViewId = webViewId;

    }

    public void run()
    {
        Log.d("AutoScroll","Inside webview auto scroll task");
        if(activityRef!=null && activityRef.get()!=null )
        {
            activityRef.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView webView = activityRef.get().findViewById(webViewId);
                    if(webView!=null && webView.getVisibility()== View.VISIBLE && activityRef.get().isServiceRunning == true)
                    {
                        webView.pageDown(false);
                    }else
                    {
                        WebViewAutoScrollTask.this.cancel();
                    }
                }
            });

        }
    }
}
