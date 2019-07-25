package com.ibetter.www.adskitedigi.adskitedigi.green_content;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaigns;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import java.util.ArrayList;
import java.util.Arrays;

public class GreenContentMngmtActivity extends Activity
{
    private Context context;
    private ContentAdapter contentAdapter;

    private static final int CREATE_MULTI_REG_ACTION = 11;

    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        context=GreenContentMngmtActivity.this;
        setActionBar();

        setContentView(R.layout.content_mngmt_activity);

        ListView listView=findViewById(R.id.content_lv);
        TypedArray icons = getResources().obtainTypedArray(R.array.green_content_setting_icons_array);
        ArrayList<String> list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.green_content_setting_array)));
        contentAdapter = new ContentAdapter(context, R.layout.content_mngmnt_support_view, list,icons);
        listView.setAdapter(contentAdapter);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration)
    {
        super.onConfigurationChanged(newConfiguration);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        invalidateOptionsMenu();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Take appropriate action for each action item click
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //set ActionBar
    private void setActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.green_content_management_page_title));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private class ContentAdapter extends ArrayAdapter<String>
    {
        ArrayList<String> data;
        TypedArray icons;

        public ContentAdapter(Context context, int textViewResourceId, ArrayList<String> data,TypedArray icons)
        {
            super(context, textViewResourceId, data);
            this.data=data;
            this.icons=icons;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_mngmnt_support_view, parent, false);
            }
            // Lookup view for data population
            Button btn = convertView.findViewById(R.id.setting_btn);
            btn.setText(data.get(position));

            ImageView btnImgView=convertView.findViewById(R.id.setting_icon);
            btnImgView.setImageResource(icons.getResourceId(position, -1));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //respectedAction(position);
                }
            });
            // Return the completed view to render on screen
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Log.i("respectedAction","respectedAction");
                    respectedAction(position);
                }
            });
            return convertView;
        }

        private void respectedAction(int position)
        {
            switch (position)
            {

                case 0:

                   break;

                case 1:
                    startActivity(new Intent(context,DownloadCampaigns.class));

                    break;

                    default:
                        break;


            }

        }




        @Override
        public long getItemId(int position) {
            // data.get(position);
            return position;
        }
        @Override
        public int getCount() {

            int count=data.size(); //counts the total number of elements from the arrayList.
            return count;//returns the total count to adapter
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {


        }

    }








}
