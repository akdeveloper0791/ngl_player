package com.ibetter.www.adskitedigi.adskitedigi.test.list_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import java.util.ArrayList;
import java.util.List;

public class ListInstalledApps extends Activity {

    ListView listView;

   public void onCreate(Bundle savedInstanceState)
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.single_listview_layout);

       listView = findViewById(R.id.single_list_view);

       new LoadApplications().execute();

   }

    private class LoadApplications extends AsyncTask<Void, Void, ArrayList<String>> {
        private ProgressDialog progress = null;

        @Override
        protected ArrayList doInBackground(Void... params) {
            ArrayList<String> installedApps = new ArrayList<>();
            PackageManager packageManager = getPackageManager();
            List<ApplicationInfo> appList = (packageManager.getInstalledApplications(PackageManager.GET_META_DATA));

            for (ApplicationInfo info : appList) {
                try {
                    //if (null != packageManager.getLaunchIntentForPackage(info.packageName))
                    {
                        installedApps.add(info.packageName+"   "+ (String)packageManager.getApplicationLabel(info));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            return installedApps;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

            progress.dismiss();
            super.onPostExecute(result);
            if(result!=null)
            {
                ArrayAdapter adapter = new ArrayAdapter(ListInstalledApps.this,android.R.layout.simple_list_item_1,result);
                listView.setAdapter(adapter);
            }else
            {
                Toast.makeText(ListInstalledApps.this,"No Apps found",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ListInstalledApps.this, null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}
