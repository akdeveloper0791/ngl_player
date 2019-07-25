package com.ibetter.www.adskitedigi.adskitedigi.settings.overlay_image_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.GalleryMediaModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import org.json.JSONObject;

import java.io.File;

public class OverlayImageSettings extends Activity {

    private  OverlayImageSettingsModel overlayImageSettingsModel;
    private  Context context;
    private LinearLayout settingsInfoLayout;
    public final static  int IMAGE_ACTION=1716;
    private ImageView overlayImageView;
    private String imagePath;
    private Spinner positionTypeSpinner;
    private EditText widthET,heightET;
    private    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate( Bundle savedInstanceState) {

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.overlay_image_settings_layout);

        setActionBar();

        context=OverlayImageSettings.this;

        overlayImageSettingsModel=new OverlayImageSettingsModel();
        settingsInfoLayout=findViewById(R.id.settings_info_layout);
        overlayImageView=findViewById(R.id.overlaying_image);
        positionTypeSpinner=findViewById(R.id.position_type_spinner);
        widthET=findViewById(R.id.width);
        heightET=findViewById(R.id.height);

        setOverlayImageStatusSettingsSwitch();

        setImagesSelection();

        setPositionTypeSpinner();

        setSettingsInfoValues();

        saveInfo();

    }

    private void saveInfo()
    {
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validateFields())
                {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("width", widthET.getText().toString());
                        jsonObject.put("height", widthET.getText().toString());
                        jsonObject.put("position", positionTypeSpinner.getSelectedItem());

                        overlayImageSettingsModel.storeOverlayImageAndSettingsInfo(context,jsonObject.toString(),imagePath);



                    }catch (Exception E)
                    {
                        Toast.makeText(context,"Unable to save",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean  validateFields()
    {
        boolean flag=true;

        if(!new Constants().verifyTextLength(widthET.getText().toString(),1))
        {
           widthET.setError("Please enter width");
            flag=false;
        }
        if(!new Constants().verifyTextLength(heightET.toString(),1))
        {

            heightET.setError("Please enter height");
            flag=false;

        }
        if(!new Constants().verifyTextLength(imagePath,1))
        {

            Toast.makeText(context,"Please select image",Toast.LENGTH_SHORT).show();
          flag=false;

        }

        if(positionTypeSpinner.getSelectedItemId()==0)
        {

            Toast.makeText(context,"Please select Position",Toast.LENGTH_SHORT).show();
            flag=false;

        }


        return flag;

    }


    private void setSettingsInfoValues()
    {
        String settingsInfo=overlayImageSettingsModel.getOverlayingImageSettingsInfo(context);

        try
        {
            JSONObject jsonObject = new JSONObject(settingsInfo);
            widthET.setText(jsonObject.getString("width"));
            heightET.setText(jsonObject.getString("height"));
            String position=jsonObject.getString("position");

            positionTypeSpinner.setSelection(adapter.getPosition(position),true);

        }
        catch (Exception E)
        {
            E.printStackTrace();
        }

        //setImage

        imagePath=overlayImageSettingsModel.getOverlayingImagePath(context);

        if(imagePath!=null)
        {

            if( new File(imagePath).exists()) {
               findViewById(R.id.image_capture).setVisibility(View.GONE);
                // Set the Image in ImageView after decoding the String
               overlayImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            }
        }

    }

    private void setPositionTypeSpinner()
    {
        // Create an ArrayAdapter using the string array and a default spinner layout
         adapter = ArrayAdapter.createFromResource(context,
                R.array.overlay_image_position, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        positionTypeSpinner.setAdapter(adapter);


    }
    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Overlaying Image Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    private void setOverlayImageStatusSettingsSwitch()
    {

        Switch switchButton = (Switch)findViewById(R.id.setting_status);

        //POS Item price Hide status update to server and save in Shared Preferance -- pos_item_price_hide_status
        boolean status=overlayImageSettingsModel.getOverlayImageSettingStatus(context);

        switchButton.setChecked(status);

        if(status)
        {
            settingsInfoLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            settingsInfoLayout.setVisibility(View.GONE);
        }

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                overlayImageSettingsModel.setOverlayImageSettingsStatus(context,isChecked);

                if(isChecked)
                {
                    settingsInfoLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    settingsInfoLayout.setVisibility(View.GONE);
                }

            }
        });



    }

    private void setImagesSelection()
    {
        findViewById(R.id.image_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getDefaultPhoto();
            }
        });

        findViewById(R.id.overlaying_image).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getDefaultPhoto();
            }
        });
    }

    //set user profile photo
    private  void getDefaultPhoto()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pick Image"),IMAGE_ACTION);

        //Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //startActivityForResult(galleryIntent,IMAGE_ACTION);

    }


    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {

            case android.R.id.home:

                onBackPressed();

                break;

            case IMAGE_ACTION:

                // When an Image is picked
                if (resultCode == RESULT_OK && null != data)
                {
                    // Get the Image from data
                    try {

                        Uri uri = data.getData();
                        if(uri!=null)
                        {
                            Glide.with(OverlayImageSettings.this)
                                    .load(uri)
                                    .into(overlayImageView);
                            // Set the Image in ImageView after decoding the String
                            // overlayImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                            findViewById(R.id.image_capture).setVisibility(View.GONE);

                            imagePath= new GalleryMediaModel(context).getPath(uri);

                        }else
                        {
                            Toast.makeText(context, "Please Select a Valid Image", Toast.LENGTH_SHORT).show();
                        }
                    /*String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    // Get the cursor
                    Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imagePath = cursor.getString(columnIndex);
                    cursor.close();
                     Log.i("info","image Decodable String"+imagePath);*/

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                    }

                } else
                {
                    Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
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

}
