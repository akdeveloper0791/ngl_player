package com.ibetter.www.adskitedigi.adskitedigi.settings.default_image_settings;

import android.content.Context;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by vineeth_ibetter on 1/10/18.
 */

public class DefaultImageSettingsModel {
    private  ImageButton  captureImage;
    private  ImageView profilePhoto;
    private Button updateButton;
    private Context context;




    public DefaultImageSettingsModel(ImageButton captureImage, ImageView profilePhoto, Button updateButton, Context context) {
        this.captureImage = captureImage;
        this.profilePhoto = profilePhoto;
        this.updateButton = updateButton;
        this.context = context;
    }


    public ImageButton getCaptureImage() {
        return captureImage;
    }

    public ImageView getProfilePhoto() {
        return profilePhoto;
    }

    public Button getUpdateButton() {
        return updateButton;
    }

    public Context getContext() {
        return context;
    }
}
