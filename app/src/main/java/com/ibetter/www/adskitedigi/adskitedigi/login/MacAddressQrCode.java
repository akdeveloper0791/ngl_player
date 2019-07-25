package com.ibetter.www.adskitedigi.adskitedigi.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.register.RegisterActivity;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MacAddressQrCode extends Activity
{

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

        Intent startRegisterActivityIntent=new Intent(MacAddressQrCode.this, RegisterActivity.class);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startRegisterActivityIntent);
        finish();

    }

    private void displayMACAddress()
    {
        TextView macAddressTV=findViewById(R.id.mac_address_tv);

        try
        {
            String macAddress = new DeviceModel().getMacAddress();

            if (macAddress != null && !macAddress.equalsIgnoreCase("02:00:00:00:00:00")) {

                String encodedMAC = new DeviceModel().encodeMacAddress(macAddress);

                if (encodedMAC != null) {
                    macAddressTV.setText(encodedMAC);
                    generateMACQR(encodedMAC);
                } else {
                    macAddressTV.setText("Something Went Wrong");
                }
            } else {
                macAddressTV.setText("Wifi Not Available");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    //generate QR
    private void generateMACQR(String macAddress)
    {
        ImageView qrCodeImg = findViewById(R.id.qr_code_iv);

        try {

            Bitmap bitmap = new DeviceModel().generateMACQR(macAddress,MacAddressQrCode.this);


            if(bitmap!=null) {
                qrCodeImg.setVisibility(View.VISIBLE);
                qrCodeImg.setImageBitmap(bitmap);
            }
            else

            {
                Toast.makeText(MacAddressQrCode.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();

            }



        }
       catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
