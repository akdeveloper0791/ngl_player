package com.ibetter.www.adskitedigi.adskitedigi.settings.device_profile_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.fcm.MyFirebaseMessagingService;
import com.ibetter.www.adskitedigi.adskitedigi.location.SearchLocation;
import com.ibetter.www.adskitedigi.adskitedigi.login.GCRegisterDeviceService;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import org.json.JSONObject;


public class DeviceProfileActivity extends Activity implements View.OnClickListener
{
    private Context context;

    private  TextView displayNameTv,locationTv;

    private ProgressDialog busyDialog;
    private RegisterServiceReceiver registerServiceReceiver;
    private static final int PICK_LOCATION_REQUEST_CODE=3;
    private View editLayout;

    public void onCreate(Bundle savedInstanceState)
    {

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_profile_activity);

        context = DeviceProfileActivity.this;

        displayNameTv=findViewById(R.id.display_name);
        locationTv=findViewById(R.id.location);

        setActionBar();

        setValues();

    }

    private void setValues()
    {
        String location=new User().getUserDisplayLocationDesc(context);
        String displayName=new User().getUserDisplayName(context);

        if(displayName!=null)
        {
            displayNameTv.setText(displayName);
        }else
        {
            displayNameTv.setText("Not Found");
        }

        if (location!=null)
        {
            locationTv.setText(location);
        }
        else
        {
            locationTv.setText("Not Found");
        }


    }

    private void setActionBar()
    {
        ActionBar actionBar = getActionBar();

        if(actionBar!=null) {

            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_profile_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case  R.id.edit_profile:
                editDisplayProfile();
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void editDisplayProfile()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        editLayout = inflater.inflate(R.layout.edit_display_profile, null);
        alertDialog.setView(editLayout);

        final Button update=editLayout.findViewById(R.id.update);

        final EditText displayNameET=editLayout.findViewById(R.id.display_name);
        final EditText locationEt=editLayout.findViewById(R.id.location);



        String location=new User().getUserDisplayLocationDesc(context);
        String displayName=new User().getUserDisplayName(context);

        if(displayName!=null)
        {
            displayNameET.setText(displayName);
        }
        else
        {
            displayNameET.setText("");
        }


        if (location!=null)
        {
            locationEt.setText(location);
        }
        else
        {
            locationEt.setText("");
        }


        final  AlertDialog dialog=alertDialog.create();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValues(displayNameET,locationEt,dialog);
            }
        });


        dialog.show();


    }

    private void updateValues(EditText displayNameET,EditText locationEt,AlertDialog  alertDialog)
    {

        if(validateInfo(displayNameET)) {



            String displayName=displayNameET.getText().toString();

            String locationName=locationEt.getText().toString();

            if(updateValuesLocally(displayName,locationName))
            {
                int mode=new User().getUserPlayingMode(context);

               if(mode==Constants.NEAR_BY_MODE) {
                   alertDialog.dismiss();
                   Toast.makeText(context,"Updated Successfully",Toast.LENGTH_SHORT).show();

               }else {

                   //

                   try {
                       JSONObject array = new JSONObject();

                       array.put("name", displayName);
                       array.put("mac", new DeviceModel().getMacAddress());

                       if (locationName != null&&locationName.length()>0) {
                           array.put("location_desc", locationName);
                       }

                       array.put("fcm_id", MyFirebaseMessagingService.getToken(context));

                       String intentAction = "UPDATE_DISPLAY_INFO__DEVICE_REGISTER_ACTIVITY";

                       //register receiver
                       if(registerServiceReceiver==null)
                       {
                           IntentFilter intentFilter =  new IntentFilter(intentAction);
                           intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

                           registerServiceReceiver = new RegisterServiceReceiver(alertDialog);
                           registerReceiver(registerServiceReceiver,intentFilter);

                       }



                       if (mode == Constants.ENTERPRISE_MODE) {

                           GCRegisterDeviceService.startAction(context, mode, new User().getEnterPriseURL(context), displayName, new User().getEnterpriseUserMailId(context), new User().getEnterpriseUserPwd(context), false,
                                   array.toString(), intentAction);
                       } else if (mode == Constants.CLOUD_MODE) {



                           GCRegisterDeviceService.startAction(context, mode, null, displayName, new User().getGCUserMailId(context), new User().getGCUserPwd(context), false,
                                   array.toString(), intentAction);
                       }

                       displayBusyDialog("Updating...");

                   }catch (Exception E)
                   {
                       E.printStackTrace();
                   }

               }


            }
            else
            {
                Toast.makeText(context,"Unable to save",Toast.LENGTH_SHORT).show();

            }

        }




    }

    private boolean updateValuesLocally(String displayName,String locationName)
    {
        SharedPreferences settingsSp = getSharedPreferences(getString(R.string.user_details_sp), MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsSp.edit();



        editor.putString(getString(R.string.user_display_name), displayName);
        editor.putString(getString(R.string.user_display_location_desc), locationName);

        if (editor.commit())
        {
            updateValues(displayName,locationName);

            return  true;
        }else
        {

            return  false;
        }
    }


    private boolean validateInfo(EditText displayNameEt)
    {
        boolean flag=true;

        String displayName=displayNameEt.getText().toString();


        if(displayName!=null&&displayName.length()<1)

        {
            flag=false;
            displayNameEt.setError("Please Enter Display Name");
        }


        return flag;
    }


    private void updateValues(String displayName,String location)
    {
        if(displayName!=null)
        {
            displayNameTv.setText(displayName);
        }

        if (location!=null)
        {
            locationTv.setText(location);
        }
        else
        {
            locationTv.setText("Not Found");
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
        private Dialog dialog;

        public RegisterServiceReceiver(Dialog dialog) {
            this.dialog = dialog;
        }

        public void onReceive(Context context, Intent intent)
        {
            unRegisterReceiver();
            dismissBusyDialog();

            dialog.dismiss();

            if(intent.getBooleanExtra("flag",false))
            {

                Toast.makeText(context,"Updated Successfully",Toast.LENGTH_SHORT).show();

            }else
            {
                Toast.makeText(context,intent.getStringExtra("status"),Toast.LENGTH_SHORT).show();
            }
        }
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

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.edit_profile_btn:
                editDisplayProfile();
                break;
            case R.id.edit_location_btn:
                getLocation();
                break;
        }
    }

    private void getLocation()
    {
        startActivityForResult(new Intent(context, SearchLocation.class),PICK_LOCATION_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        switch (requestCode)
        {
            case PICK_LOCATION_REQUEST_CODE:
                if(resultCode==RESULT_OK)
                {
                    User.updateLocation(this,(Location) data.getParcelableExtra("location"));

                    if(data.hasExtra("address") && editLayout!=null)
                    {
                        ((EditText)editLayout.findViewById(R.id.location)).setText(data.getStringExtra("address"));
                    }

                }
             break;
        }
    }
}
