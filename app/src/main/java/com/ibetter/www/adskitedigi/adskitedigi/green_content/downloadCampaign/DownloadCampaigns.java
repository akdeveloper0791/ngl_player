package com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.database.CampaignsDBModel;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.GCLoginActivity;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.GCProfileActivity;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.campaign_preview.PreviewIndvCampaign;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoCampDownloadListService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignTriggerService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.DownloadCampaignsService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.download_services.FetchBasicCampInfoService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.GCNotification;
import com.ibetter.www.adskitedigi.adskitedigi.model.NetworkModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import com.liulishuo.magicprogresswidget.MagicProgressCircle;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver.DOWNLOAD_LIST_API_ERROR;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignReceiver.DOWNLOAD_LIST_CAMPAIGN_SUCCESS;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.model.GCModel.DOWNLOAD_ERROR;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.GCNotification.ACTION_RETRY;
import static com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_notify.GCNotification.ACTION_SKIP;
import static com.ibetter.www.adskitedigi.adskitedigi.settings.auto_campaign_sync_settings.AutoCampaignDownloadSettings.DEFAULT_AUTO_DOWNLOAD__STATUS;

public class DownloadCampaigns extends AppCompatActivity {
    private Context context;
    private RecyclerView recyclerView;
    public static final int GC_LOGIN_ACTION = 2001;
    public static final int GC_PROFILE_ACTION = 2002;
    private ArrayList<GCModel> campList = new ArrayList<>();
    private HashMap<String, GCModel> campHashMap = new HashMap<>();
    private CampaignsAdapter campaignsAdapter;

    private CampaignResultReceiver campaignResultReceiver;
    private ProgressUpdateReceiver progressUpdateReceiver;

    private ArrayList<String> deletingFiles = new ArrayList<>();

    private ArrayList<String> skipCampsList = new ArrayList<>();
    private ProgressDialog busyDialog;
    private RelativeLayout searchLayout;
    private boolean isFilterActive = false;
    private SparseBooleanArray skippedMediasState = new SparseBooleanArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        context = DownloadCampaigns.this;
        setContentView(R.layout.download_campaigns);

        setActionBar();

        recyclerView = findViewById(R.id.campaigns_lv);

        searchLayout = findViewById(R.id.search_layout);

        initializeAdaptor();

        //download basic campaign info from GC server
        downloadBasicCampaignsList();

        registerProgressRx();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.download_campaign_menu, menu);

        MenuItem item = menu.findItem(R.id.skip_all);
        item.setActionView(R.layout.sync_switch_layout);
        final Switch mySwitch = item.getActionView().findViewById(R.id.switch_btn);

        mySwitch.setChecked(isSkipAllSP());
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!DownloadCampaignsService.isServiceOn) {

                    CampaignsDBModel.updateCampaignIsSkip(context, isChecked);
                    new PrepareAndDisplayCampaigns().execute();
                    setISkipALLSP(isChecked);

                } else {
                    mySwitch.setChecked(!isChecked);
                    Toast.makeText(context, "Dear user, Please wait campaigns are downloading, or this will override the skip settings", Toast.LENGTH_SHORT).show();
                }

            }
        });

        if (isFilterActive) {
            menu.findItem(R.id.filter).setIcon(R.drawable.ic_active_filter);
            item.setVisible(false);
        } else {
            menu.findItem(R.id.filter).setIcon(R.drawable.ic_filter);
            item.setVisible(true);
        }

        setAndHandleSyncSettings(menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.profile:

                if (new User().isGCUserLogin(context)) {
                    startActivityForResult(new Intent(context, GCProfileActivity.class), GC_PROFILE_ACTION);
                } else {
                    loginActivity();
                }
                return true;

            case R.id.filter:
                if (searchLayout.getVisibility() == View.VISIBLE) {
                    campaignsDataRefresh();
                    isFilterActive = false;
                    searchLayout.setVisibility(View.GONE);
                } else {
                    isFilterActive = true;
                    recyclerView.setVisibility(View.GONE);
                    searchLayout.setVisibility(View.VISIBLE);
                    campaignsFilter();
                }
                invalidateOptionsMenu();
                return true;

            case R.id.search:

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadBasicCampaignsList() {
        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp), MODE_PRIVATE);

        if (new User().isGCUserLogin(context)) {
            if (settingsSp.getBoolean(getString(R.string.is_auto_download_campaign), DEFAULT_AUTO_DOWNLOAD__STATUS)) {
                if (new NetworkModel().isInternet(context)) {
                    downloadFromServer();
                } else {
                    //display local
                    new PrepareAndDisplayCampaigns().execute();
                }
            } else {
                //display local
                new PrepareAndDisplayCampaigns().execute();
            }

        } else {
            Toast.makeText(context, "Dear user, Please login...", Toast.LENGTH_SHORT).show();
            loginActivity();
        }

    }

    private void downloadFromServer(){
        enableProgressBar();
        campaignResultReceiver = new CampaignResultReceiver(new Handler());
        Intent startIntent = new Intent(context, AutoCampDownloadListService.class);
        startIntent.putExtra("receiver", campaignResultReceiver);
        startService(startIntent);
    }

    private void loginActivity() {
        Intent intent = new Intent(context, GCLoginActivity.class);
        startActivityForResult(intent, GC_LOGIN_ACTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GC_LOGIN_ACTION:

                if (resultCode == RESULT_OK && null != data) {
                    if (data.getBooleanExtra("flag", false)) {
                        if (campaignsAdapter != null) {
                            campList.clear();
                            campHashMap.clear();
                            enableProgressBar();
                            campaignsAdapter.notifyDataSetChanged();
                        }

                        downloadBasicCampaignsList();

                    } else {
                        Toast.makeText(context, "Unable to Login, Please try again later.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;

            case GC_PROFILE_ACTION:

                if (resultCode == RESULT_OK && null != data) {
                    if (data.getBooleanExtra("is_log_out", false)) {
                        loginActivity();
                    }
                }
                break;
        }

    }


    //set ActionBar
    private void setActionBar() {
       /* */

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

       ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.gc_download_campaigns_page_title));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void enableNoCampaignsFound() {
        recyclerView.setVisibility(View.GONE);
        TextView noCampaigns = findViewById(R.id.no_camp_tv);
        noCampaigns.setVisibility(View.VISIBLE);
    }

    private void disableNoCampaignsFound() {
        recyclerView.setVisibility(View.VISIBLE);
        TextView noCampaigns = findViewById(R.id.no_camp_tv);
        noCampaigns.setVisibility(View.GONE);

    }


    private void enableProgressBar() {
        recyclerView.setVisibility(View.GONE);
        LinearLayout progressLayout = findViewById(R.id.progress_layout);
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void disableProgressBar() {
        LinearLayout progressLayout = findViewById(R.id.progress_layout);
        progressLayout.setVisibility(View.GONE);
    }


    @Override
    public void onDestroy() {
       /* if(DownloadFullCampaignService.isServiceActive)
        {
            stopCampaignService();
        }*/

        unRegisterProgressRx();
        super.onDestroy();
    }

    private class CampaignResultReceiver extends ResultReceiver {
        public CampaignResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            Log.i("onReceiveResult", "onReceiveResult");
            disableProgressBar();

            switch (resultCode) {
                case FetchBasicCampInfoService.GC_FETCH_CAMPAIGNS_ACTION:

                    if (resultData != null) {
                        if (resultData.getBoolean("flag")) {
                            campList = (ArrayList<GCModel>) resultData.getSerializable("campaign_list");

                            for (GCModel model : campList) {
                                campHashMap.put(model.getCampaignName(), model);
                            }

                            campaignsDataRefresh();


                        } else {
                            enableNoCampaignsFound();
                            Toast.makeText(context, resultData.getString("status"), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(context, "Unable to get the Campaigns info, Please Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DOWNLOAD_LIST_CAMPAIGN_SUCCESS:
                    new PrepareAndDisplayCampaigns().execute();
                    break;
                case DOWNLOAD_LIST_API_ERROR:
                    Toast.makeText(context, resultData.getString("status", "error"), Toast.LENGTH_SHORT).show();
                    break;

            }
            super.onReceiveResult(resultCode, resultData);
        }

    }


    private void campaignsDataRefresh() {
        try {
            if (campList != null && campList.size() > 0) {
                disableNoCampaignsFound();
                campaignsAdapter.notifyDataSetChanged();
                requestForCurrentDownloadingFiles();
                // campaignsAdapter.notifyDataSetChanged();

            } else {
                enableNoCampaignsFound();
            }
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    private void initializeAdaptor() {
        campaignsAdapter = new CampaignsAdapter(campList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(campaignsAdapter);
    }

    private class CampaignsAdapter extends RecyclerView.Adapter {
        private ArrayList<GCModel> campaignsList;


        public CampaignsAdapter(ArrayList<GCModel> campaignsList) {
            this.campaignsList = campaignsList;
        }

        @Override
        public int getItemCount() {
            return campaignsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView campNameTV, createdByTV, createdAtTV, errorInfoTv, progressInfoTv, downLoadingFilesInfoTV;
            public ImageButton downloadBtn, deleteButton, previewButton;
            public Button retryBtn, skipBtn;
            public AVLoadingIndicatorView downloadAVLoading;
            public MagicProgressCircle progressCircle;
            public RelativeLayout errorInfoLayout;
            public FrameLayout progressInfoLayout;
            public Switch skipButton;
            public ImageView thumbView;

            public MyViewHolder(View view) {
                super(view);
                campNameTV = view.findViewById(R.id.campaign_name);
                createdByTV = view.findViewById(R.id.created_by);
                createdAtTV = view.findViewById(R.id.created_at);
                downloadBtn = view.findViewById(R.id.download_btn);
                downloadAVLoading = view.findViewById(R.id.download_dialog_avl);
                progressCircle = view.findViewById(R.id.download_dialog_pg_dialog_magic_circles);
                skipBtn = view.findViewById(R.id.skip_btn);
                retryBtn = view.findViewById(R.id.retry_btn);
                errorInfoTv = view.findViewById(R.id.error_msg);
                errorInfoLayout = view.findViewById(R.id.error_info_layout);
                progressInfoTv = view.findViewById(R.id.progress_info);
                progressInfoLayout = view.findViewById(R.id.progress_infoo_layout);
                downLoadingFilesInfoTV = view.findViewById(R.id.downloading_info_tv);
                deleteButton = view.findViewById(R.id.delete_btn);
                previewButton = view.findViewById(R.id.preview_btn);
                skipButton = view.findViewById(R.id.skip_button);

                thumbView = view.findViewById(R.id.thumb_iv);

                skipButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isSkip) {
                        try {


                            int position = getAdapterPosition();
                            //Toast.makeText(context,"toggled out side - "+position+isSkip,Toast.LENGTH_SHORT).show();
                            GCModel gcModel = (GCModel) getItem(position);
                            if (isSkip) {

                                if (!skippedMediasState.get(position, false)) {
                                    skippedMediasState.put(position, true);
                                    gcModel.setIsSkip(1);
                                    handleSkipSetting(gcModel.getCampaignName(), isSkip, position);
                                    if(gcModel.getServerId()>=1)
                                    {
                                        checkAndIssueSyncOverrideWarn();
                                    }

                                }
                            } else {
                                if (skippedMediasState.get(position, false)) {
                                    skippedMediasState.put(position, false);
                                    gcModel.setIsSkip(0);
                                    handleSkipSetting(gcModel.getCampaignName(), isSkip, position);
                                    if(gcModel.getServerId()>=1)
                                    {
                                        checkAndIssueSyncOverrideWarn();
                                    }
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public Object getItem(int position) {
            return campaignsList.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_camp_supportview, parent, false);

            // final MyViewHolder result = new MyViewHolder(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(true);
                    notifyDataSetChanged();
                }
            });

            return new MyViewHolder(itemView);
        }

        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            try {

                final MyViewHolder myViewHolder = ((MyViewHolder) holder);
                final Switch skipButton = myViewHolder.skipButton;

                final GCModel gcModel = (GCModel) getItem(position);
                final String campaignName = gcModel.getCampaignName();
                myViewHolder.campNameTV.setText(campaignName);
                String createdAt = gcModel.getCreatedAt();
                myViewHolder.createdAtTV.setText("Created At:" + createdAt);

                //final File campaignFile=new File(new DownloadMediaHelper().getAdsKiteNearByDirectory(context)+"/"+gcModel.getCampaignName()+".txt");
                if (gcModel.getIsDownloaded()) {
                    myViewHolder.downloadBtn.setBackgroundResource(R.drawable.green_button_background);
                    //need to get is skip
                    skippedMediasState.put(position, gcModel.getIsSkip() == 1 ? true : false);

                } else {
                    myViewHolder.downloadBtn.setBackgroundResource(R.drawable.download_button_style);
                    skippedMediasState.put(position, false);
                }


                skipButton.setChecked(skippedMediasState.get(position));


                String thumbFileName = getString(R.string.do_not_display_media) + "-" + getString(R.string.media_thumbnail) + "-" + gcModel.getCampaignName() + getString(R.string.media_thumbnail_extention);

                String thumbFilePath = new DownloadMediaHelper().getAdsKiteNearByDirectory(context) + "/" + thumbFileName;

                File thumbFile = new File(thumbFilePath);

                if (thumbFile.exists()) {
                    myViewHolder.thumbView.setImageURI(Uri.fromFile(thumbFile));
                } else {
                    myViewHolder.thumbView.setImageDrawable(context.getResources().getDrawable(R.drawable.default_campaign));
                }
                if (gcModel.getServerId() > 0) {
                    //check and download campaign full info from the server
                    myViewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadDBxCampaign(gcModel);
                        }
                    });
                } else {
                    myViewHolder.downloadBtn.setVisibility(View.INVISIBLE);
                }

                GCModel.ProgressInfo progressInfo = gcModel.getProgressInfo();
                if (progressInfo == null) {

                    myViewHolder.downloadAVLoading.setVisibility(View.GONE);
                    myViewHolder.errorInfoLayout.setVisibility(View.GONE);
                    myViewHolder.progressInfoLayout.setVisibility(View.GONE);
                    myViewHolder.downLoadingFilesInfoTV.setVisibility(View.GONE);
                    myViewHolder.downloadBtn.setVisibility(View.VISIBLE);

                    if (gcModel.getIsDownloaded()) {
                        myViewHolder.previewButton.setVisibility(View.VISIBLE);
                        myViewHolder.deleteButton.setVisibility(View.VISIBLE);
                        myViewHolder.skipButton.setVisibility(View.VISIBLE);
                    } else {
                        myViewHolder.previewButton.setVisibility(View.GONE);
                        myViewHolder.deleteButton.setVisibility(View.GONE);
                        myViewHolder.skipButton.setVisibility(View.GONE);
                    }

                } else {
                    myViewHolder.downloadBtn.setVisibility(View.GONE);
                    myViewHolder.previewButton.setVisibility(View.GONE);
                    myViewHolder.deleteButton.setVisibility(View.GONE);
                    myViewHolder.skipButton.setVisibility(View.GONE);
                    String status = progressInfo.getStatus();

                    switch (status) {
                        case GCModel.INIT_DOWNLOAD:
                            myViewHolder.downloadAVLoading.setVisibility(View.VISIBLE);
                            myViewHolder.progressInfoLayout.setVisibility(View.GONE);
                            myViewHolder.downLoadingFilesInfoTV.setVisibility(View.GONE);
                            myViewHolder.errorInfoLayout.setVisibility(View.GONE);

                            break;
                        case GCModel.DOWNLOAD_PROGRESS:
                            myViewHolder.downloadAVLoading.setVisibility(View.GONE);
                            myViewHolder.progressInfoLayout.setVisibility(View.VISIBLE);
                            myViewHolder.downLoadingFilesInfoTV.setVisibility(View.VISIBLE);
                            myViewHolder.errorInfoLayout.setVisibility(View.GONE);

                            myViewHolder.progressInfoTv.setText(String.valueOf(progressInfo.getProgress() + "%"));
                            Log.i("DOWNLOAD_PROGRESS", (progressInfo.getProgress() * 1.0f) + ":::" + progressInfo.getProgress());
                            myViewHolder.progressCircle.setSmoothPercent((progressInfo.getProgress() * 1.0f) / 100, 1500);
                            myViewHolder.progressInfoTv.setVisibility(View.VISIBLE);
                            myViewHolder.downLoadingFilesInfoTV.setText("Downloading Campaign (" + progressInfo.getPosition() + "/" + progressInfo.getTotalFiles() + ")");
                            break;

                        case DOWNLOAD_ERROR:
                            myViewHolder.errorInfoLayout.setVisibility(View.VISIBLE);
                            myViewHolder.downloadAVLoading.setVisibility(View.GONE);
                            myViewHolder.progressInfoLayout.setVisibility(View.GONE);
                            myViewHolder.progressInfoTv.setVisibility(View.GONE);
                            myViewHolder.downLoadingFilesInfoTV.setVisibility(View.GONE);

                            myViewHolder.errorInfoTv.setText(progressInfo.getErrorMsg());

                            //NEED TO HANDLE
                            break;
                    }


                }

                myViewHolder.retryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent retryIntent = new Intent();
                        retryIntent.setAction(GCNotification.ACTION);
                        retryIntent.putExtra("action", ACTION_RETRY);

                        sendBroadcast(retryIntent);
                    }
                });


                myViewHolder.skipBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent retryIntent = new Intent();
                        retryIntent.setAction(GCNotification.ACTION);
                        retryIntent.putExtra("action", ACTION_SKIP);
                        sendBroadcast(retryIntent);
                    }
                });

                myViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        deleteCampaignDialog(gcModel);
                    }
                });

                myViewHolder.previewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.d("DownloadCampaigns","on preview-"+gcModel.getInfo());
                        Intent intent = new Intent(context, PreviewIndvCampaign.class);
                        intent.putExtra("info", gcModel.getInfo());
                        startActivity(intent);
                    }
                });


                holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                v.setSelected(true);
                                break;
                            case MotionEvent.ACTION_UP:
                                v.setSelected(false);
                                break;
                            case MotionEvent.ACTION_CANCEL:
                                v.setSelected(false);
                                break;
                        }
                        return true;
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void deleteCampaignDialog(final GCModel gcModel) {
        if (gcModel != null) {
            try {

                AlertDialog.Builder msgDialog = new AlertDialog.Builder(context);
                msgDialog.setMessage("Are you sure.\nYou want to delete Campaign ?");
                msgDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<GCModel> deletedCampaigns = new ArrayList<>();
                        deletedCampaigns.add(gcModel);
                        Intent intent = new Intent(context, DeleteUnknownCampaigns.class);
                        intent.putExtra("unknown_campaigns", deletedCampaigns);
                        startService(intent);
                        dialog.dismiss();

                        gcModel.setIsDownloaded(0);
                        campaignsAdapter.notifyDataSetChanged();
                        CampaignsDBModel.updateIsDownload(context,0,gcModel.getCampaignLocalId());
                    }
                });

                msgDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                msgDialog.create().show();
            } catch (Exception E) {
                E.printStackTrace();
            }

        } else {
            Toast.makeText(context, "Unable to delete Campaign ", Toast.LENGTH_SHORT).show();
        }

    }

    private synchronized void deleteCampaign(JSONObject infoObj, String campaignName) {
        //prepare upload files from json
        try {
            String type = infoObj.getString("type");
            if (type.equalsIgnoreCase("multi_region")) {
                processMultiRegFilesToUpload(infoObj.getJSONArray("regions"));
            } else {
                //single region
                addForRegion(infoObj, "Single");
            }

            //check for bg audio file
            if (infoObj.has("bg_audio")) {
                deletingFiles.add(infoObj.getString("bg_audio"));
            }

            //add thumb file
            String thumbFileName = getString(R.string.do_not_display_media) + "-" + getString(R.string.media_thumbnail) + "-" + campaignName + getString(R.string.media_thumbnail_extention);

            deletingFiles.add(thumbFileName);

            deletingFiles.add(campaignName + ".txt");

            for (String fileName : deletingFiles) {

                File file = new File(new DownloadMediaHelper().getAdsKiteNearByDirectory(context) + "/" + fileName);
                if (file.exists()) {
                    file.delete();
                    Log.d("file deleted", "campaign file is -- " + fileName);

                }
            }

        } catch (JSONException e) {

            Toast.makeText(context, "Unable to delete Campaign", Toast.LENGTH_SHORT).show();


        } finally {
            deletingFiles.clear();
            campaignsAdapter.notifyDataSetChanged();
        }

    }

    //process multi region files
    private void processMultiRegFilesToUpload(JSONArray regions) throws JSONException {
        for (int i = 0; i < regions.length(); i++) {
            addForRegion(regions.getJSONObject(i), "multi");
        }
    }

    private void addForRegion(JSONObject region, String type) throws JSONException {
        if (!region.getString("type").equalsIgnoreCase("text") && !region.getString("type").equalsIgnoreCase("Url")
                && !region.getString("type").equalsIgnoreCase("default")) {
            if (type.equalsIgnoreCase("Single")) {
                deletingFiles.add(region.getString("resource"));
            } else {
                String media = region.getString("media_name");
                if (!media.equalsIgnoreCase("default"))
                    deletingFiles.add(region.getString("media_name"));
            }

        }
    }

    private void downloadDBxCampaign(GCModel model) {
        if (new User().isGCUserLogin(context)) {
            if (new NetworkModel().isInternet(context)) {

                //stop alarm service
                AutoDownloadCampaignModel.stopAutoCampaignDownloadService(context);


                if (DownloadCampaignsService.isServiceOn) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("model", model);
                    DownloadCampaignsService.downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.INIT_DOWNLOAD_CAMPAIGN, bundle);
                } else {
                    Intent intent = new Intent(context, DownloadCampaignsService.class);
                    intent.putExtra("model", model);
                    ContextCompat.startForegroundService(context, intent);
                }
            } else {
                Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Dear user, Please login...", Toast.LENGTH_SHORT).show();
            loginActivity();
        }
    }

    private void registerProgressRx() {

        IntentFilter intentFilter = new IntentFilter(DownloadCampaignsService.UPDATE_PROGRESS_RX);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        progressUpdateReceiver = new ProgressUpdateReceiver(new WeakReference<Activity>(DownloadCampaigns.this));
        registerReceiver(progressUpdateReceiver, intentFilter);
    }

    private void unRegisterProgressRx() {
        try {
            if (progressUpdateReceiver != null) {
                unregisterReceiver(progressUpdateReceiver);
            }

        } catch (Exception E) {

        } finally {
            progressUpdateReceiver = null;
        }
    }

    public void initDownloadProgressInfo(String campaignName) {

        try {
            if (campHashMap.containsKey(campaignName)) {
                GCModel gcModel = campHashMap.get(campaignName);

                gcModel.initProgressInfo();

                campaignsAdapter.notifyDataSetChanged();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void updateDownloadProgressInfo(String campaignName, int progress, int position, int totalFiles, String resourceName) {

        try {
            if (campHashMap.containsKey(campaignName)) {
                GCModel gcModel = campHashMap.get(campaignName);
                gcModel.updateDownloadProgress(progress, position, totalFiles, resourceName);

                campaignsAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void updateDownloadProgressErrorInfo(String campaignName, String error) {

        try {
            if (campHashMap.containsKey(campaignName)) {
                GCModel gcModel = campHashMap.get(campaignName);

                gcModel.updateDownloadErrorProgressInfo(error);
                campaignsAdapter.notifyDataSetChanged();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeDownloadProgressInfo(String campaignName) {

        try {
            if (campHashMap.containsKey(campaignName)) {
                GCModel gcModel = campHashMap.get(campaignName);


                gcModel.removeProgressInfo();
                campaignsAdapter.notifyDataSetChanged();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void downloadProgressSuccess(String campaignName) {

        try {
            if (campHashMap.containsKey(campaignName)) {
                GCModel gcModel = campHashMap.get(campaignName);
                gcModel.setIsDownloaded(1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void removeDownloadAllProgressInfo() {

        try {
            for (GCModel gcModel : campList) {

                gcModel.removeProgressInfo();

            }

            campaignsAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void requestForCurrentDownloadingFiles() {
        if (DownloadCampaignsService.isServiceOn) {
            DownloadCampaignsService.downloadCampaignResultReceiver.send(DownloadCampaignResultReceiver.REQUEST_FOR_DOWNLOADING_CAMPAIGNS, null);
        }
    }

    public void getDownloadingPendingFiles(ArrayList<String> list, String status, String inProgressCampaignName, int progress, String errorMsg, int position, int totalFiles, String resourceName) {
        try {
            for (String campaignName : list) {
                if (campHashMap.containsKey(campaignName)) {
                    GCModel gcModel = campHashMap.get(campaignName);

                    gcModel.initProgressInfo();
                }
            }

            GCModel progressGcModel = campHashMap.get(inProgressCampaignName);

            if (status.equalsIgnoreCase(DownloadCampaignsService.ERROR)) {
                progressGcModel.updateDownloadErrorProgressInfo(errorMsg);

            } else {
                progressGcModel.updateDownloadProgress(progress, position, totalFiles, resourceName);
            }
            campaignsAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //handle skip settings
    private void handleSkipSetting(String campaignName, boolean isSkip, int position) throws Exception {
        CampaignsDBModel.updateSkip(context, campaignName, isSkip == true ? 1 : 0);

    }


    //display busy dialog
    private void displayBusyDialog(String busyMsg) {
        busyDialog = new ProgressDialog(context);
        busyDialog.setMessage(busyMsg);
        busyDialog.setCanceledOnTouchOutside(false);
        busyDialog.setCancelable(false);

        busyDialog.show();
    }

    //dismiss busy dialog
    private void dismissBusyDialog() {
        try {
            if (busyDialog != null && busyDialog.isShowing()) {
                busyDialog.dismiss();
            }
        } catch (Exception e) {

        }
    }

    private void campaignsFilter() {
        Button searchBtn = findViewById(R.id.search_btn);
        final EditText searchET = findViewById(R.id.search_et);
        //searchBtn.setText("Apply");
        searchBtn.setText("Apply");
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchString = searchET.getText().toString();
                if (searchString != null && searchString.length() > 0) {
                    new FilteredCampaignsTask().execute(searchString, null, null);
                } else {
                    Toast.makeText(context, "Please enter text to filter campaigns", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private class FilteredCampaignsTask extends AsyncTask<String, Void, Void> {
        private String filterString;
        private ArrayList<GCModel> filterList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            displayBusyDialog("Please wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            filterString = params[0];
            try {
                for (String campaignName : campHashMap.keySet()) {
                    if ((campaignName.toLowerCase()).contains(filterString)) {
                        filterList.add(campHashMap.get(campaignName));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("SkipAllCampaignsTask", "Exception:" + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dismissBusyDialog();
            if (filterList != null && filterList.size() > 0) {
                disableNoCampaignsFound();
                campaignsAdapter = new CampaignsAdapter(filterList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context, GridLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(campaignsAdapter);
                requestForCurrentDownloadingFiles();
            } else {
                enableNoCampaignsFound();
            }

        }

    }

    private class SkipCampaignsReceiver extends ResultReceiver {
        public SkipCampaignsReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case SkipAllCampaignsService.SKIP_ALL_CAMPAIGNS_ACTION:

                    dismissBusyDialog();
                    try {
                        if (resultData != null) {

                            if (resultData.getBoolean("flag")) {
                                boolean skipFlag = resultData.getBoolean("skipFlag", false);
                                if (skipFlag) {
                                    skipCampsList = (ArrayList<String>) resultData.getSerializable("skipCampsList");
                                } else {
                                    skipCampsList.clear();
                                }

                                setISkipALLSP(skipFlag);

                                campaignsAdapter.notifyDataSetChanged();

                            } else {
                                Toast.makeText(context, resultData.getString("status"), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Unable to get do Skip operation, please try again later", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Unable to get do Skip operation, please try again later", Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private void setISkipALLSP(boolean isSkip) {

        SharedPreferences sp = new SharedPreferenceModel().getDeviceSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putBoolean(getString(R.string.skip_all_sp), isSkip);
        editor.commit();


    }

    private boolean isSkipAllSP() {

        SharedPreferences sp = new SharedPreferenceModel().getDeviceSharedPreference(context);

        return sp.getBoolean(getString(R.string.skip_all_sp), false);
    }

    private void setAndHandleSyncSettings(Menu menu) {
        final SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp), MODE_PRIVATE);
        MenuItem item = menu.findItem(R.id.skip_all);

        final Switch mySwitch = item.getActionView().findViewById(R.id.sync_switch_btn);

        mySwitch.setChecked(settingsSp.getBoolean(getString(R.string.is_auto_download_campaign), DEFAULT_AUTO_DOWNLOAD__STATUS));
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSyncSettings(isChecked, settingsSp);
            }
        });
    }

    private void updateSyncSettings(boolean isON, SharedPreferences settingsSp) {


        SharedPreferences.Editor editor = settingsSp.edit();

        editor.putBoolean(getString(R.string.is_auto_download_campaign), isON);
        if (editor.commit()) {
            if (isON) {

                if (User.isPlayerRegistered(context)) {
                    downloadFromServer();
                    startService(new Intent(context, AutoDownloadCampaignTriggerService.class));

                } else {
                    Toast.makeText(context, "Player not registered", Toast.LENGTH_SHORT).show();
                }


            } else {

                AutoDownloadCampaignModel.stopAutoCampaignDownloadService(context);
                Toast.makeText(context, "Switched off successfully", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(context, "Unable to save settings", Toast.LENGTH_SHORT).show();
        }
    }

    private class PrepareAndDisplayCampaigns extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            clearListAndNotify();
            enableProgressBar();
        }

        public Void doInBackground(Void... params) {

            Cursor campaigns = CampaignsDBModel.getCampaigns(context);
            if (campaigns != null && campaigns.moveToNext()) {
                do {
                    GCModel gcModel = new GCModel(campaigns);
                    campList.add(gcModel);
                    campHashMap.put(gcModel.getCampaignName(), gcModel);
                } while (campaigns.moveToNext());
            }

            return null;
        }

        public void onPostExecute(Void result) {
            disableProgressBar();
            campaignsDataRefresh();
        }
    }

    private void clearListAndNotify() {
        campList.clear();
        campHashMap.clear();
        campaignsAdapter.notifyDataSetChanged();

    }

    private void checkAndIssueSyncOverrideWarn()
    {
        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp), MODE_PRIVATE);
        if(settingsSp.getBoolean(getString(R.string.is_auto_download_campaign), DEFAULT_AUTO_DOWNLOAD__STATUS))
        {
            Toast.makeText(context, "Dear user, please switch off the sync , or this will override the skip setting", Toast.LENGTH_SHORT).show();
        }

    }
}