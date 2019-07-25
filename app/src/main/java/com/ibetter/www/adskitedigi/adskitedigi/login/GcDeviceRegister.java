package com.ibetter.www.adskitedigi.adskitedigi.login;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.fcm.MyFirebaseMessagingService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.auto_download_campaign.AutoDownloadCampaignModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Permissions;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;
import com.ibetter.www.adskitedigi.adskitedigi.player_statistics.PlayerStatisticsCollectionModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriceSettings;

import org.json.JSONException;
import org.json.JSONObject;

public class GcDeviceRegister extends Activity implements View.OnClickListener{

    private EditText userPwdET,userEmailET,displayNameET,locationET;
    private EditText akUserPwdET;
    private CheckBox isAdsKiteUserCB;
    private Context context;

    private  static  int WRITABLE_PERMISSION_REQUEST=1;
    private final int ENTERPRISE_MODE_PERMISSIONS_REQUEST = 2;
    private final int MAC_QR_ACTIVITY=3;

    private RegisterServiceReceiver registerServiceReceiver;
    private ProgressDialog busyDialog;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gc_device_register);
        context = GcDeviceRegister.this;

        userPwdET=findViewById(R.id.register_layout_user_pwd_et);
        akUserPwdET = findViewById(R.id.register_layout_display_adskite_user_pwd);
        userEmailET=findViewById(R.id.register_layout_user_email_et);
        displayNameET=findViewById(R.id.register_layout_display_name_et);
        locationET=findViewById(R.id.register_layout_display_location_info_et);

        handleIsAdsKiteCB();
        checkForPermissions();
    }

    public void onClick(View view)
    {
      if(view.getId()== R.id.register_layout_register_button)
      {
          //register display
          checkAndRegister();
      }
    }

    private void handleIsAdsKiteCB()
    {
        isAdsKiteUserCB = findViewById(R.id.register_layout_display_is_adskite_user);
        isAdsKiteUserCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b)
                {
                    akUserPwdET.setVisibility(View.GONE);
                    akUserPwdET.setVisibility(View.VISIBLE);
                }
                else
                {
                    akUserPwdET.setVisibility(View.VISIBLE);
                    akUserPwdET.setVisibility(View.GONE);
                }
            }
        });
    }

    private void checkAndRegister()
    {
        if(checkForPermissions())
        {
            if(validateRequireLoginFields())
            {
                GCMacAddressQRCode();
            }
        }
    }
    //check for permissions
    private boolean checkForPermissions()
    {
        //check for storage permission
        if(isStoragePermissionGranted())
        {
            //check for enterprise mode permissions
            return checkEnterpriseModePermissions();

        }else
        {
            //return false
            return false;
        }
    }

    //check enterprise mode permissions
    private boolean checkEnterpriseModePermissions()
    {
        //check the permissions and start
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if (!Permissions.hasPermissions(this, EnterPriceSettings.permissions)) {

                ActivityCompat.requestPermissions(this,
                        EnterPriceSettings.permissions ,
                        ENTERPRISE_MODE_PERMISSIONS_REQUEST);
                return false;
            }else
            {
                //permissions approved
                return true;
            }
        }else
        {
            return true;// permissions added
        }
    }


    protected   boolean isStoragePermissionGranted()
    {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            //Now you have permission
            return true;
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITABLE_PERMISSION_REQUEST);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults!=null && grantResults.length>=1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //Now you have permission

            // Check file copy generated the request
            // and resume file copy
            if(requestCode == WRITABLE_PERMISSION_REQUEST && isStoragePermissionGranted())
            {
                //check for enterprise mode permissions
                if(checkEnterpriseModePermissions())
                {
                    checkAndRegister();
                }

            }else if(requestCode == ENTERPRISE_MODE_PERMISSIONS_REQUEST && checkEnterpriseModePermissions())
            {
                checkAndRegister();
            }
        }
    }



    //validate fields
    public boolean validateRequireLoginFields()
    {



        if ( !(new Validations().validateString(displayNameET.getText().toString())))
        {
            displayNameET.setError(getString(R.string.login_action_display_name_et_error_msg));
            return false;

        }

        if(!Validations.validateEmail(context,userEmailET.getText().toString()))
        {
            userEmailET.setError(getString(R.string.login_action_user_email_et_error_msg));
            return false;
        }

        if(isAdsKiteUserCB.isChecked())
        {
            ;
            if(!Validations.validateString(context,akUserPwdET.getText().toString(),3))
            {
                akUserPwdET.setError(getString(R.string.login_action_user_pwd_et_error_msg));
            }
        }else
        {

            if(!Validations.validateString(context,userPwdET.getText().toString(),3))
            {
                userPwdET.setError(getString(R.string.login_action_user_pwd_et_error_msg));
            }
        }



        return true;
    }

    //display qr
    private void GCMacAddressQRCode()
    {
        Intent intent = new Intent(context,GCMacAddressQRCode.class);
        startActivityForResult(intent,MAC_QR_ACTIVITY);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
       if(resultCode==RESULT_OK)
       {
           switch (requestCode) {
               case MAC_QR_ACTIVITY:
                   String macAddress = data.getStringExtra("macAddress");
                   if(macAddress!=null)
                   {
                       register(macAddress);
                   }else
                   {
                       Toast.makeText(context, "Unable to register.Switch on your wi-fi and try again later,", Toast.LENGTH_SHORT).show();
                   }
                   break;
           }
       }

    }

    private void register(String mac)
    {
          Log.d("Register","Device mac - "+mac);
          Log.d("Register","Access Token "+ MyFirebaseMessagingService.getToken(context));
       try {
           JSONObject array = new JSONObject();
           array.put("name", displayNameET.getText().toString());
           array.put("mac", mac);

           String desc=locationET.getText().toString();

           if(desc!=null)
           {
               array.put("location_desc", desc);
           }

           array.put("fcm_id", MyFirebaseMessagingService.getToken(context));
           String pwd = userPwdET.getText().toString();

           if(isAdsKiteUserCB.isChecked())
           {
               pwd = akUserPwdET.getText().toString();
           }

           String intentAction = "GC_DEVICE_REGISTER_ACTIVITY";

           //register receiver
           if(registerServiceReceiver==null)
           {
               IntentFilter intentFilter =  new IntentFilter(intentAction);
               intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

               registerServiceReceiver = new RegisterServiceReceiver();
               registerReceiver(registerServiceReceiver,intentFilter);

           }

           //display busy dialog
           displayBusyDialog("Registering device, please wait...");

           //start register service
           GCRegisterDeviceService.startAction(context,0,null,displayNameET.getText().toString(),userEmailET.getText().toString(),pwd,isAdsKiteUserCB.isChecked(),
                   array.toString(),intentAction);



       }catch(JSONException e)
       {
           Toast.makeText(context,"Unable to register, please try again later"+e.getMessage(),Toast.LENGTH_SHORT).show();
       }
    }

     private void unRegisterReceiver()
     {
         try {
             if (registerServiceReceiver != null) {
                 unregisterReceiver(registerServiceReceiver);
             }
         }catch (Exception e)
         {

         }finally {

             registerServiceReceiver = null;
         }
     }
    private class RegisterServiceReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context,Intent intent)
        {
            unRegisterReceiver();
            dismissBusyDialog();

            if(intent.getBooleanExtra("flag",false))
            {
                //success
                //start enterprise mode

                checkAndRestartPlayerStatisticsCollectionService();
                checkRestartAutoCampaignDownloadService();


                new User().checkExistingScheduleFiles(GcDeviceRegister.this);
            }else
            {
                Toast.makeText(context,intent.getStringExtra("status"),Toast.LENGTH_SHORT).show();
            }
        }
    }





    private void checkAndRestartPlayerStatisticsCollectionService()
    {
        PlayerStatisticsCollectionModel.checkRestartUploadCampaignReportsService(context);
    }

    private void checkRestartAutoCampaignDownloadService()
    {
        AutoDownloadCampaignModel.checkRestartAutoCampaignDownloadService(context);
    }

    private void displayBusyDialog(String msg)
    {
        busyDialog = new ProgressDialog(context);
        busyDialog.setCancelable(false);
        busyDialog.setMessage(msg);
        busyDialog.show();
    }

    private void dismissBusyDialog()
    {
        if(busyDialog!=null && busyDialog.isShowing())
        {
            busyDialog.dismiss();
        }
    }

}
