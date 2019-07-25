package com.ibetter.www.adskitedigi.adskitedigi.settings.metrics_settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.iot_devices.IOTDevice;
import com.ibetter.www.adskitedigi.adskitedigi.iot_devices.RegisterIOTDevice;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.CameraServiceResultReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.MetricsModel;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.internal.MetricsService;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

public class MetricsSettingsActivity extends AppCompatActivity{

public static final boolean DEFAULT_METRICS_STATUS = false;
private Switch metricsSwitch;
private Context context;
private Button submitButton;

private ProgressDialog busyDialog;

private GetResponseReceiver getResponseReceiver;
private RadioGroup radioGroup;
private TextView typeTextView;
private RadioButton internalCamRB,externalCamRB;
private static final  int SYSTEM_ALERT_WINDOW_PERMISSION=201;

public void onCreate(Bundle savedInstanceState)
        {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.metrics_settings_layout);

        context = MetricsSettingsActivity.this;
        setActionBar();

        metricsSwitch = findViewById(R.id.metrics_sw);
       //imageCaptDurationEt = findViewById(R.id.duration);

        radioGroup=findViewById(R.id.cam_rg);
        typeTextView=findViewById(R.id.type_tv);

        internalCamRB=findViewById(R.id.internal_cam_rb);
        externalCamRB=findViewById(R.id.external_cam_rb);

        submitButton=findViewById(R.id.update);

        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(metricsSwitch.isChecked()&&radioGroup.getCheckedRadioButtonId()==R.id.internal_cam_rb)
                {
                    MetricsModel.startMetricsService(context);
                }

                finish();

            }
        });

         setValues();

        }

private void setActionBar()
   {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
        {
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        //actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        }

   }


@Override
public boolean onOptionsItemSelected(MenuItem item)
  {
        Toast.makeText(context,"item id - "+item.getItemId(),Toast.LENGTH_SHORT);
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

private void setValues()
 {
        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
        boolean isMetricsON = settingsSp.getBoolean(getString(R.string.is_metrics_on),DEFAULT_METRICS_STATUS);
        int cameraID=settingsSp.getInt(getString(R.string.metrics_camera_type),0);

        metricsSwitch.setChecked(isMetricsON);
        if(isMetricsON)
        {
            radioGroup.setVisibility(View.VISIBLE);
            typeTextView.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        }
        else
        {
            radioGroup.setVisibility(View.GONE);
            typeTextView.setVisibility(View.GONE);
        }

        if(cameraID>0)
            {
                if(cameraID==1)
                {
                    internalCamRB.setChecked(true);

                }else
                {
                    externalCamRB.setChecked(true);
                }
            }

        handleMetricsSettings();
        handleCameraSetting();
  }

  private void handleCameraSetting()
  {
      radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(RadioGroup group, int checkedId)
          {
              SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
              SharedPreferences.Editor editor = settingsSp.edit();
              int cameraId;

              switch (checkedId)
              {
                  case R.id.internal_cam_rb:
                      cameraId=1;
                      try
                      {
                          if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                          {
                              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                              {
                                  if (Settings.canDrawOverlays(context))
                                  {
                                      if(new MetricsModel().getNumberOfCameras(context)>0)
                                      {
                                          editor.putInt(getString(R.string.metrics_camera_type),cameraId);

                                          if( editor.commit())
                                          {

                                              MetricsModel.startMetricsService(context);

                                              finish();

                                          }

                                      }else
                                      {
                                          Toast.makeText(context, "You device does not have internal camera, Please try with external camera.", Toast.LENGTH_LONG).show();
                                      }

                                  }else
                                  {
                                      internalCamRB.setChecked(false);
                                      askPermission();
                                      Toast.makeText(context, "You need System Alert Window Permission to get AD metrics by using internal camera.", Toast.LENGTH_LONG).show();
                                  }
                              }
                              else
                              {
                                  if(new MetricsModel().getNumberOfCameras(context)>0)
                                  {
                                      editor.putInt(getString(R.string.metrics_camera_type),cameraId);
                                      if(editor.commit())
                                      {
                                          MetricsModel.startMetricsService(context);
                                          finish();
                                      }
                                  }else
                                  {
                                      Toast.makeText(context, "You device does not have internal camera, Please try with external camera.", Toast.LENGTH_LONG).show();
                                  }
                              }
                          }else
                          {
                              Toast.makeText(context, "You device OS is not supported for user metrics, Please try with device OS version 5.0 and above.", Toast.LENGTH_LONG).show();
                              finish();
                          }

                      }catch (Exception e)
                      {
                          Toast.makeText(context, "You device is not supported with internal camera, Please try with external camera.", Toast.LENGTH_LONG).show();
                          finish();
                      }

                      break;

                  case R.id.external_cam_rb:
                     cameraId=2;
                     editor.putInt(getString(R.string.metrics_camera_type),cameraId);
                     if(editor.commit())
                     {
                   //Toast.makeText(context, "cameraId:"+cameraId, Toast.LENGTH_SHORT).show();
                      notifyService();
                      finish();
                     }
                    break;
              }
          }
      });

  }

    private void handleMetricsSettings()
    {
        metricsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                try
                {
                    SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
                    SharedPreferences.Editor editor = settingsSp.edit();
                    if(isChecked)
                    {
                        if(User.isPlayerRegistered(context))
                        {

                            if(IOTDevice.isIOTDeviceRegistered(context))
                            {
                                radioGroup.setVisibility(View.VISIBLE);
                                typeTextView.setVisibility(View.VISIBLE);
                                submitButton.setVisibility(View.VISIBLE);

                                editor.putBoolean(getString(R.string.is_metrics_on), isChecked);

                                //display submit and duration button
                                Toast.makeText(context, "AD view metrics is ON.", Toast.LENGTH_SHORT).show();

                                editor.commit();

                            }
                            else
                            {
                                //need to call register service


                                registerIotDeviceService();

                            }

                        }else
                        {
                            //display submit and duration button
                            metricsSwitch.setChecked(false);
                            Toast.makeText(context,"Player is not registered, Please check in CMS Settings.",Toast.LENGTH_SHORT).show();
                        }

                    }else
                    {
                        editor.putBoolean(getString(R.string.is_metrics_on),isChecked);
                        if(editor.commit())
                        {
                            notifyService();
                            finish();
                        }
                    }

                }catch (Exception e)
                {
                    Toast.makeText(context,"Unable to save settings, Please try again later.",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    private void askPermission()
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case SYSTEM_ALERT_WINDOW_PERMISSION:
               // Toast.makeText(this, "App overlay permission is granted", Toast.LENGTH_SHORT).show();
                break;
        }
    }



private void notifyService()
{
  if(MetricsService.isServiceOn)
  {
      MetricsService.cameraServiceResultReceiver.send(CameraServiceResultReceiver.STOP_SERVICE, null);
  }
}

private void registerIotDeviceService()
{
    getResponseReceiver =new GetResponseReceiver(new Handler());

    Intent startIntent = new Intent(context, RegisterIOTDevice.class);
    startIntent.putExtra("receiver", getResponseReceiver);
    startService(startIntent);

    displayBusyDialog("Registering IOT Device");

}


    public class GetResponseReceiver extends ResultReceiver
    {

        com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers.GetModifyFilesReceiver.GetModifyFilesReceiverCallBacks mReceiver;

        public GetResponseReceiver(Handler handler)
        {

            super(handler);
        }

        public void setReceiver(com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers.GetModifyFilesReceiver.GetModifyFilesReceiverCallBacks receiver)
        {
            mReceiver = receiver;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {



                switch (resultCode)
                {
                    case RegisterIOTDevice.REGISTER_IOT_DEVICE_ACTION:

                        dismissBusyDialog();

                        if (resultData != null)
                        {
                            boolean flag = resultData.getBoolean("flag", false);
                            if (flag)
                            {
                                SharedPreferences settingsSp = getSharedPreferences(getString(R.string.settings_sp),MODE_PRIVATE);
                                SharedPreferences.Editor editor = settingsSp.edit();

                                radioGroup.setVisibility(View.VISIBLE);
                                typeTextView.setVisibility(View.VISIBLE);
                                submitButton.setVisibility(View.VISIBLE);

                                editor.putBoolean(getString(R.string.is_metrics_on), true);

                                //display submit and duration button
                                Toast.makeText(context, "AD view metrics is ON.", Toast.LENGTH_SHORT).show();

                                editor.commit();

                                 //need to delete file
                            }
                            else
                            {
                                metricsSwitch.setChecked(false);
                                //display submit and duration button
                                Toast.makeText(context, resultData.getString("status"), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            metricsSwitch.setChecked(false);
                            Toast.makeText(context, "Unable to switch on AD view metrics is ON.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
        }
    }


    //display busy dialog
    private void displayBusyDialog(String busyMsg)
    {
        busyDialog = new ProgressDialog(context);
        busyDialog.setMessage(busyMsg);
        busyDialog.setCanceledOnTouchOutside(false);
        busyDialog.setCancelable(false);

        busyDialog.show();
    }

    //dismiss busy dialog
    private void dismissBusyDialog()
    {
        try {
            if (busyDialog != null && busyDialog.isShowing()) {
                busyDialog.dismiss();
            }
        } catch (Exception e) {

        }
    }

}

