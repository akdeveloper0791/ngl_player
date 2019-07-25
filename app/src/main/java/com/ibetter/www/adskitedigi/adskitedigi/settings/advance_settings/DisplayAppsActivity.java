package com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import java.util.ArrayList;
import java.util.List;

public class DisplayAppsActivity extends Activity
 {
    private Context context;
    private CustomGridViewAdapter customAdapter;
    private ArrayList<AppModel> dataList=new ArrayList<>();

    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        context=DisplayAppsActivity.this;
        setContentView(R.layout.display_apps_activity);

        new GetAppsTask().execute();

    }



  private class GetAppsTask extends AsyncTask<Void,Void, Void>
  {
      @Override
      protected void onPreExecute()
      {
          enableProgressBar();
      }
      @Override
      protected Void doInBackground(Void... values)
      {
          getAllInstalledApps();
          return null;
      }

      @Override
      protected void onPostExecute(Void result)
      {
          disableProgressBar();
          if(dataList!=null && dataList.size()>0)
          {
              displayAllApps();
          }else
          {
              Toast.makeText(context, "Unable to choose app, Please try again later", Toast.LENGTH_SHORT).show();
              finish();
          }

      }
  }



    private void displayAllApps()
    {
        // get the reference of RecyclerView
        RecyclerView recyclerView =findViewById(R.id.recyclerView);
        // set a GridLayoutManager with default vertical orientation and 2 number of columns

      //  GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),3, LinearLayoutManager.HORIZONTAL,false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),3);
        recyclerView.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        //  call the constructor of CustomAdapter to send the reference and data to Adapter
        customAdapter = new CustomGridViewAdapter(context, dataList);
        recyclerView.setAdapter(customAdapter); // set the Adapter to RecyclerView
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setFocusable(true);

    }

    public class CustomGridViewAdapter extends RecyclerView.Adapter<CustomGridViewAdapter.MyViewHolder>
    {
        private Context mContext;
        private ArrayList<AppModel>appList;

        public CustomGridViewAdapter(Context context, ArrayList<AppModel>appList)
        {
            mContext = context;
            this.appList = appList;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_apps_supportview, parent, false);
            // set the view's size, margins, paddings and layout parameters
            MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
            return vh;
        }

       @Override
        public void onBindViewHolder(MyViewHolder holder, final int position)
        {
            final AppModel model=appList.get(position);
            // set the data in items
            holder.name.setText(model.getName());
            holder.image.setImageDrawable(model.getAppIcon());
            // implement setOnClickListener event on item view.
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    boolean flag=new User().updateSelectedAppPackageInSP(context,model.getPackageName());
                    if(flag)
                    {
                        Toast.makeText(context,"Selected application:"+model.getName(), Toast.LENGTH_SHORT).show();
                        sendResults(model.getPackageName());

                    }else
                    {
                        Toast.makeText(context, "Please select a valid application...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        @Override
        public int getItemCount() {
            return appList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            // init the item view's
            TextView name;
            ImageView image;
            public MyViewHolder(View itemView)
            {
                super(itemView);
                // get the reference of item view's
                name =itemView.findViewById(R.id.app_name);
                image =itemView.findViewById(R.id.app_icon);
            }

            @Override
            public void onClick(View view) {
                view.setSelected(true);
               // clickListener.onClick(view, getAdapterPosition()); // call the onClick in the OnItemClickListener
            }
        }
    }




    private void sendResults(String packageName)
    {
        // put the String to pass back into an Intent and close this activity
        Intent intent = new Intent();
        intent.putExtra("packageName",packageName);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void enableProgressBar()
    {
        LinearLayout progressLayout=findViewById(R.id.progress_bar_layout);
        progressLayout.setVisibility(View.VISIBLE);
    }
    private void disableProgressBar()
    {
        LinearLayout progressLayout=findViewById(R.id.progress_bar_layout);
        progressLayout.setVisibility(View.GONE);
    }

    private void getAllInstalledApps()
    {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages)
        {
            try {
                if(pm.getLaunchIntentForPackage(packageInfo.packageName)!= null && !pm.getLaunchIntentForPackage(packageInfo.packageName).equals(""))
                {
                    // Log.i("DisplayApps","packageInfo.Package:"+packageInfo.packageName);
                    //Log.i("DisplayApps","packageInfo.Label:"+pm.getApplicationLabel(packageInfo));
                    // Log.i("DisplayApps","packageInfo.icon:"+pm.getApplicationIcon(packageInfo.packageName).toString());
                    AppModel model=new AppModel();
                    model.setPackageName(packageInfo.packageName);
                    model.setName(String.valueOf(pm.getApplicationLabel(packageInfo)));
                    model.setAppIcon(pm.getApplicationIcon(packageInfo.packageName));
                    dataList.add(model);
                }

            }catch (PackageManager.NameNotFoundException exp)
            {
                exp.printStackTrace();
            }
        }
        Log.i("DisplayApps","Installed Apps List size:"+dataList.size());
    }

}
