package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.Permissions;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;
import com.ibetter.www.adskitedigi.adskitedigi.nearby.ConnectingNearBySMMOdel;

/**
 * Created by vineethkumar0791 on 27/03/18.
 */

public class SignageManagerAccessSettings extends android.app.Fragment {
    private Activity context;

    private ToggleButton switchButton;
    private RelativeLayout signageMgrInfoLayout;
    private TextView serviceIdTV;
    private ImageView updateServiceId;
    private AlertDialog dialog;

    //on create view
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState)

    {
        context = getActivity();


        View  view = inflater.inflate(R.layout.signage_manager_access_settings_layout,null);
        switchButton = view.findViewById(R.id.toggle_button);
        signageMgrInfoLayout=view.findViewById(R.id.service_id_info_layout);
        serviceIdTV = view.findViewById(R.id.service_id);
        updateServiceId= view.findViewById(R.id.edit_service_id);

        setSwitchButton();

        return view;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId())
        {
            case android.R.id.home:

                context.onBackPressed();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSwitchButton()
    {
        if (SignageMgrAccessModel.isSignageMgrAccessOn(context,getString(R.string.sm_access_near_by_mode)))
        {
            switchButton.setChecked(true);
            displayServiceIdInfo();



        } else {
            switchButton.setChecked(false);
            dismissServiceIdInfo();

        }

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    if (!Permissions.hasPermissions(context,  ConnectingNearBySMMOdel.getRequiredPermissions()))
                    {
                        context.requestPermissions(ConnectingNearBySMMOdel.getRequiredPermissions(), ConnectingNearBySMMOdel.REQUEST_CODE_REQUIRED_PERMISSIONS);
                    }
                    else
                    {
                        updateTextDialog();
                    }
                }else
                {
                    SignageMgrAccessModel.setSignageMgrAccessStatus(isChecked, context,getString(R.string.sm_access_near_by_mode));
                    dismissServiceIdInfo();
                    new ConnectingNearBySMMOdel().stopDiscoveringSMService();
                }

            }
        });
    }


    /*private void displayAlertDialog()
    {
        AlertDialog.Builder infoDialog = new DisplayDialog().displayAlertDialog(context, getString(R.string.signage_mgr_access_back_press_dialog_info_msg), getString(R.string.app_default_alert_title_info), false);

        if (!infoDialog.create().isShowing())
        {

            infoDialog.setNegativeButton(getString(R.string.app_default_alert_negative_button_no_text), new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which) {



                    dialog.dismiss();

                }
            });


            infoDialog.setPositiveButton(getString(R.string.app_default_alert_positive_button_yes_text), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switchButton.setChecked(false);

                    finish();

                    dialog.dismiss();

                }
            });

            infoDialog.create().show();

        }
    }


    */

    private void updateTextDialog()
    {

        if (dialog != null && dialog.isShowing())
        {
            Log.i("info"," A dialog is already open, wait for it to be dismissed, do nothing");
        } else {
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT);

            alertDialog.setTitle(getString(R.string.signage_mgr_access__update_alert));
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.display_signage_mgr_service_id_info_dialog_layout, null);
            alertDialog.setCancelable(false);
            alertDialog.setView(layout);

            EditText serviceIdET = (EditText) layout.findViewById(R.id.service_id);
            Button updateButton=(Button)layout.findViewById(R.id.update_service_id);
            Button cancelButton=(Button)layout.findViewById(R.id.cancel);

            dialog= alertDialog.create();


            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if(!(signageMgrInfoLayout.getVisibility()==View.VISIBLE)) {
                        switchButton.setChecked(false);
                        dismissServiceIdInfo();
                    }
                    dialog.dismiss();
                }

            });

            setServiceId(serviceIdET);

            setUpdateButton(updateButton,serviceIdET,dialog);

            dialog.show();
        }

    }

    private void setServiceId(EditText serviceIdET)
    {
        String serviceID=SignageMgrAccessModel.getSignageMgrAccessServiceId(context);

        if(new Validations().validateString(serviceID))
        {
            serviceIdET.append(serviceID);
        }
    }

    private void setUpdateButton(Button updateButton,final EditText serviceIdET,final  Dialog dialog)
    {

        updateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(new Validations().validateEditText(serviceIdET))
                {
                       String serviceId = serviceIdET.getText().toString();

                        SignageMgrAccessModel.setSignageMgrAccessStatus(true, context,getString(R.string.sm_access_near_by_mode));

                        SignageMgrAccessModel.setSignageMgrAccessServiceId(serviceId, context);
                        new ConnectingNearBySMMOdel().startDiscoveringSMService(context);

                        new User().updateUserPlayingMode(context, Constants.NEAR_BY_MODE,null,null,null);

                       displayServiceIdInfo();

                       Toast.makeText(context, getString(R.string.app_default_update_success_mssg), Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
                else
                {
                    serviceIdET.setError(getString(R.string.signage_mgr_service_id_validations_err));
                }
            }
        });
    }


    private void displayServiceIdInfo()
    {
        serviceIdTV.setText( Html.fromHtml("<u>"+SignageMgrAccessModel.getSignageMgrAccessServiceId(context)+"</u>"));

        signageMgrInfoLayout.setVisibility(View.VISIBLE);

        updateServiceId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTextDialog();
            }
        });
    }

    private void dismissServiceIdInfo()
    {
        signageMgrInfoLayout.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConnectingNearBySMMOdel.REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED)
                {
                    Toast.makeText(context, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();

                    switchButton.setChecked(false);

                    dismissServiceIdInfo();


                }else if(grantResult == PackageManager.PERMISSION_GRANTED)
                {
                    updateTextDialog();
                }


            }

            context.recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
