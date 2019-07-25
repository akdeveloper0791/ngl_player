package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;

public class DisplayDialogForLicenceApprovalActivity extends Activity
{

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {

        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        context=DisplayDialogForLicenceApprovalActivity.this;

        User.setLicenceStatus(context,Constants.DISPLAY_EXPIRED_STATUS);

        Toast.makeText(context,"licence is expired please contact la;ayn@adskite.com",Toast.LENGTH_SHORT).show();

        super.onCreate(savedInstanceState);

    }
}
