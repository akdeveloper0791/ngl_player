package com.ibetter.www.adskitedigi.adskitedigi.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;

public class GCMacAddressQRCode extends Activity
{

    private String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.qr_code_generator_layout);

        displayMACAddress();

        proceed();

        displayNote();

    }

    private void displayNote()
    {
        String str = "\ud83d\udc49" + getString(R.string.note_for_qr_code_scan);

        ((TextView) findViewById(R.id.note)).setText(Html.fromHtml(str));

    }


    private void proceed()
    {
        findViewById(R.id.proceed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                success();

            }
        });
    }

    public void success() {

        Intent startRegisterActivityIntent= getIntent();
        startRegisterActivityIntent.putExtra("proceed",true);
        startRegisterActivityIntent.putExtra("macAddress",macAddress);
        this.setResult(RESULT_OK,startRegisterActivityIntent);
        finish();

    }

    private void displayMACAddress()
    {
        TextView macAddressTV=findViewById(R.id.mac_address_tv);

        try
        {
             macAddress = new DeviceModel().getMacAddress();

            if (macAddress != null && !macAddress.equalsIgnoreCase("02:00:00:00:00:00")) {

                String encodedMAC = new DeviceModel().encodeMacAddress(macAddress);

                if (encodedMAC != null) {
                    macAddressTV.setText(encodedMAC);
                    generateMACQR(encodedMAC);
                } else {
                    macAddressTV.setText("Something Went Wrong");
                }
            } else {
                disableProceed();
                macAddressTV.setText("Wifi Not Available");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void disableProceed()
    {
        findViewById(R.id.proceed).setVisibility(View.GONE);
    }


    //generate QR
    private void generateMACQR(String macAddress)
    {
        ImageView qrCodeImg = findViewById(R.id.qr_code_iv);

        try {

            Bitmap bitmap = new DeviceModel().generateMACQR(macAddress,GCMacAddressQRCode.this);


            if(bitmap!=null) {
                qrCodeImg.setVisibility(View.VISIBLE);
                qrCodeImg.setImageBitmap(bitmap);
            }
            else

            {
                Toast.makeText(GCMacAddressQRCode.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();

            }



        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
