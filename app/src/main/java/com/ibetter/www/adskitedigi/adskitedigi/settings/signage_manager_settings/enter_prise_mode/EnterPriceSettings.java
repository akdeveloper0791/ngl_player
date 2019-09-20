package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Permissions;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;

public class EnterPriceSettings extends Fragment implements CompoundButton.OnCheckedChangeListener{

    private ToggleButton settingsSwitch;
    private Context context;

    public final static String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.CAMERA};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstance)
    {

        View layout = inflater.inflate(R.layout.signage_manager_enterprice_layout,null);

        settingsSwitch = layout.findViewById(R.id.enterprise_toggle);

        context = getActivity();

        setSettingsSwitch();

        settingsSwitch.setOnCheckedChangeListener(this);

        displayMACAddress(layout);

        return layout;

    }


    private void displayMACAddress(View layout)
    {
        TextView macAddressTV=layout.findViewById(R.id.mac_address_tv);


        String macAddress=new DeviceModel().getMacAddress();

        if(macAddress!=null&&!macAddress.equalsIgnoreCase("02:00:00:00:00:00"))
        {

            String encodedMAC=new DeviceModel().encodeMacAddress(macAddress);

            if(encodedMAC!=null)
            {
                macAddressTV.setText(encodedMAC);
                generateMACQR(encodedMAC,layout);
            }
            else
            {
                macAddressTV.setText("Something Went Wrong");
            }
        }
        else
        {
            macAddressTV.setText("Wifi Not Available");
        }

    }


    //generate QR
    private void generateMACQR(String macAddress,View layout)
    {
        ImageView qrCodeImg = layout.findViewById(R.id.qr_code_iv);

        try {

            Bitmap bitmap = new DeviceModel().generateMACQR(macAddress,context);


            if(bitmap!=null) {
                qrCodeImg.setVisibility(View.VISIBLE);
                qrCodeImg.setImageBitmap(bitmap);
            }
            else

            {
                Toast.makeText(context,"Something Went Wrong",Toast.LENGTH_SHORT).show();
            }



        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }





    //set settings switch
    private void setSettingsSwitch()
    {
        settingsSwitch.setChecked (SignageMgrAccessModel.isSignageMgrAccessOn(context,context.getString(R.string.sm_access_enterprise_mode)));
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {

     switch (compoundButton.getId())
     {
            case R.id.enterprise_toggle:
                handleEnterpriseToggle(b);
                break;
     }

    }

    private void handleEnterpriseToggle(boolean b)
    {
       if(b)
       {
           //check the permissions and start
           if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
           {
               if (!Permissions.hasPermissions(getActivity(), permissions)) {
                   settingsSwitch.setChecked(false);
                   ActivityCompat.requestPermissions(getActivity(),
                           permissions ,
                           1);
               }else
               {
                   EnterPriseSettingsModel.startEnterPriseModel(getActivity());
               }
           }else
           {
               EnterPriseSettingsModel.startEnterPriseModel(getActivity());
           }
       }
       else
       {
           EnterPriseSettingsModel.switchOffEnterPriseSettings(context);
       }
    }

    //generate QR
 /*   private void generateMACQR(String macAddress)
    {
       /// ImageView qrCode = (ImageView)layfindViewById(R.id.qr_code_iv);

        try {

            //generate json object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mac", macAddress);

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

            BitMatrix bitMatrix = multiFormatWriter.encode(jsonObject.toString(), BarcodeFormat.QR_CODE,400,400);

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }catch(JSONException e)
        {
            e.printStackTrace();
        }
    }*/
}
