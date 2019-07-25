package com.ibetter.www.adskitedigi.adskitedigi.settings.show_mac_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.login.MacAddressQrCode;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class ShowSSMACSettings extends Activity
{

    ImageView qrCodeImg;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.show_ss_mac_layout);


         qrCodeImg =findViewById(R.id.qr_code_iv);


        setActionBar();

        displayMACAddress();

        displayNote();


    }

    private void displayNote()
    {
        String str = "\ud83d\udc49" + getString(R.string.note_for_qr_code_scan);

        ((TextView) findViewById(R.id.note)).setText(Html.fromHtml(str));

    }


    //generate QR
    private void generateMACQR(String macAddress)
    {
        ImageView qrCodeImg = findViewById(R.id.qr_code_iv);

        try {

            Bitmap bitmap = new DeviceModel().generateMACQR(macAddress,ShowSSMACSettings.this);


            if(bitmap!=null) {
                qrCodeImg.setVisibility(View.VISIBLE);
                qrCodeImg.setImageBitmap(bitmap);
            }
            else

            {
                Toast.makeText(ShowSSMACSettings.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
            }



        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    private void displayMACAddress()
    {
        TextView macAddressTV=findViewById(R.id.mac_address_tv);


        String macAddress=new DeviceModel().getMacAddress();

        if(macAddress!=null&&!macAddress.equalsIgnoreCase("02:00:00:00:00:00"))
        {

            String encodedMAC=new DeviceModel().encodeMacAddress(macAddress);

            if(encodedMAC!=null)
            {
                macAddressTV.setText(encodedMAC);
                generateMACQR(encodedMAC);
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

    public void onResume()
    {
        super.onResume();
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));
    }

    //set ActionBar
    private void setActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Show SS Player QR Code");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

}
