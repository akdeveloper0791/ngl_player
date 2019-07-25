package com.ibetter.www.adskitedigi.adskitedigi.settings.default_image_settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

import java.io.File;

/**
 * Created by vineeth_ibetter on 1/10/18.
 */

public class DefaultImageSettings extends Activity
{
    DefaultImageSettingsModel defaultImageSettingsModel;
    public final static  int PROFILE_PIC_ACTION=1716;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.default_image_settings_layout);

        setActionBar();

        defaultImageSettingsModel=new DefaultImageSettingsModel((ImageButton)findViewById(R.id.image_capture),
                (ImageView)findViewById(R.id.profile_photo),(Button)findViewById(R.id.update),DefaultImageSettings.this);


        setImagesSelection();
        setUserProfilePhoto();
        updateMode();

    }

    private void setImagesSelection()
    {
        defaultImageSettingsModel.getCaptureImage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getDefaultPhoto();
            }
        });

        defaultImageSettingsModel.getProfilePhoto().setOnClickListener(new View.OnClickListener()
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
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PROFILE_PIC_ACTION);
    }


    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case PROFILE_PIC_ACTION:

                // When an Image is picked
                if (requestCode ==PROFILE_PIC_ACTION && resultCode == RESULT_OK && null != data)
                {
                    // Get the Image from data

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    // Get the cursor
                    Cursor cursor = defaultImageSettingsModel.getContext().getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();

                    try {

                        SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(defaultImageSettingsModel.getContext());
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(getString(R.string.playing_default_image_path),imgDecodableString);
                        editor.commit();

                        // Set the Image in ImageView after decoding the String
                        defaultImageSettingsModel.getProfilePhoto().setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                        defaultImageSettingsModel.getCaptureImage().setVisibility(View.GONE);

                    }catch (Exception e)
                    {

                    }

                } else
                {
                    Toast.makeText(defaultImageSettingsModel.getContext(), "You haven't picked Image", Toast.LENGTH_LONG).show();
                }

                break;

        }
    }


    private  void setUserProfilePhoto()
    {
        SharedPreferences sp = new SharedPreferenceModel().getUserDetailsSharedPreference(defaultImageSettingsModel.getContext());
        String imagePath= sp.getString(getString(R.string.playing_default_image_path),null);

        if(imagePath!=null)
        {

            if( new File(imagePath).exists()) {
                defaultImageSettingsModel.getCaptureImage().setVisibility(View.GONE);
                // Set the Image in ImageView after decoding the String
                defaultImageSettingsModel.getProfilePhoto().setImageBitmap(BitmapFactory.decodeFile(imagePath));
            }
        }

    }

    //set action bar
    private void setActionBar()
    {

        ActionBar actionBar=getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Set Default Image Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMode()
    {
        defaultImageSettingsModel.getUpdateButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



            }
        });
    }

}
