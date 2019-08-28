package com.ibetter.www.adskitedigi.adskitedigi.login;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.location.SearchLocation;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DisplayDialog;
import com.ibetter.www.adskitedigi.adskitedigi.model.NetworkModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Permissions;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriceSettings;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class LoginActivity extends Activity implements LoginInterface
{
    LoginViewModel loginViewModel;
    private  static  int WRITABLE_PERMISSION_REQUEST=1;
    private final int ENTERPRISE_MODE_PERMISSIONS_REQUEST = 2;
    private  static  final int PICK_FILE_REQUEST_CODE=2;
    private static final int PICK_LOCATION_REQUEST_CODE=3;
    private boolean is_from_login=false;
    private static final  int SYSTEM_ALERT_WINDOW_PERMISSION=200;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
       // setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);


        View layout = getLayoutInflater().inflate(R.layout.login_layout,null);
        setContentView(layout);

        loginViewModel=new LoginViewModel(LoginActivity.this,layout);

        setLoginAction();

        checkForPermissions();

        getLocation();

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



    /*for login*/
    @Override
    public  void login()
    {

        if(validateRequireLoginFields())
        {

            AlertDialog.Builder alertDialog=new DisplayDialog().displayAlertDialog(loginViewModel.getContext(),
                    getString(R.string.login_action_mobile_confirmation_alert_msg)
                            +" "+loginViewModel.getUserMobileNumber(), getString(R.string.app_default_alert_title_info),false);

            alertDialog.setPositiveButton(getString(R.string.app_default_alert_positive_button_confirm_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    saveDetails();
                    dialogInterface.dismiss();
                }
            });


            alertDialog.setNegativeButton(getString(R.string.app_default_alert_negative_button_cancel_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();

                }
            });


            alertDialog.create().show();

        }
    }

    //validate fields
    public boolean validateRequireLoginFields()
    {

        if (!loginViewModel.isValidMobileNumber())
        {
            loginViewModel.setLoginLayoutMobileNumberETErrorMSG(getString(R.string.login_action_mobile_number_et_error_msg));
            return false;

        }


        if (!loginViewModel.isValidDisplayName())
        {
            loginViewModel.setLoginLayoutDisplayNameETErrorMSG(getString(R.string.login_action_display_name_et_error_msg));
            return false;

        }




        return true;
    }

    /*register display*/
    @Override
   public void registerDisplay()
    {

    }

    /*saving details*/
    @Override
    public void saveDetails() {

        if( new User().setUserDetails(loginViewModel.getContext(),loginViewModel.getUserMobileNumber(), Constants.NEAR_BY_MODE,
        loginViewModel.getLoginLayoutDisplayNameEditText().getText().toString(),loginViewModel.getLoginLayoutDisplayLocationDescriptionEditText().getText().toString()))
        {
            success();

        }else
        {
            //unable to login right now
            displayToast(getString(R.string.login_action_login_failure_text));
        }
    }

    /*success*/
    @Override
    public void success() {

        Intent startRegisterActivityIntent=new Intent(loginViewModel.getContext(), MacAddressQrCode.class);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startRegisterActivityIntent);
        finish();

    }

    /*failure*/
    @Override
    public void failure() {

    }



    /*initiate mobile number */
    private String  initiateUserMobileNumber()
    {

        String mobileNumber = loginViewModel.getLoginLayoutMobileNumberEditText().getText().toString();
        loginViewModel.setUserMobileNumber(mobileNumber);

        return mobileNumber;

    }

    /*set Login Action*/
    private void setLoginAction()
    {

        //login button onclick listener
        loginViewModel.getLoginLayoutLoginButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                is_from_login=true;
                if(checkForPermissions())
                {


                    is_from_login=false;

                    if(new NetworkModel().isInternet(loginViewModel.getContext())) {

                        initiateUserMobileNumber();

                        login();
                    }else
                    {
                        Toast.makeText(loginViewModel.getContext(),getString(R.string.no_internet_connection_error_msg),Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
    }

    protected   boolean isStoragePermissionGranted()
    {

        if (ContextCompat.checkSelfPermission(loginViewModel.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            //Now you have permission
            return true;
        }
        else
        {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITABLE_PERMISSION_REQUEST);
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
                    checkAndLogin();
                }

            }else if(requestCode == ENTERPRISE_MODE_PERMISSIONS_REQUEST && checkEnterpriseModePermissions())
            {
                checkAndLogin();
            }
        }
    }

    private void checkAndLogin()
    {
        if(is_from_login) {

            is_from_login=false;

            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M)
            {
                if (!Settings.canDrawOverlays(this))
                {
                    askPermission();
                }
            }

            if(new NetworkModel().isInternet(loginViewModel.getContext()))
            {

                initiateUserMobileNumber();

                login();

            }else
            {
                Toast.makeText(loginViewModel.getContext(),getString(R.string.no_internet_connection_error_msg),Toast.LENGTH_SHORT).show();
            }


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

    /*display toast*/
    private void displayToast(String msg)
    {
        Toast.makeText(loginViewModel.getContext(),msg,Toast.LENGTH_LONG).show();

    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {

            case SYSTEM_ALERT_WINDOW_PERMISSION:
                Toast.makeText(this, "App overlay permission is granted", Toast.LENGTH_SHORT).show();
                break;
            case PICK_LOCATION_REQUEST_CODE:
                if(resultCode==RESULT_OK)
                {
                    User.updateLocation(this,(Location) data.getParcelableExtra("location"));

                    if(data.hasExtra("address"))
                    {
                        ((EditText)findViewById(R.id.register_layout_display_location_info_et)).setText(data.getStringExtra("address"));
                    }


                }
                break;
        }

    }

    private void askPermission()
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    private void getLocation()
    {
        findViewById(R.id.search_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, SearchLocation.class),PICK_LOCATION_REQUEST_CODE);
            }
        });
    }



}
