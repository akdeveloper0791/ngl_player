package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

public class AskEnterpriseModePermissions extends Activity {

    private final static int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                EnterPriceSettings.permissions ,
                REQUEST_CODE_REQUIRED_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
             finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
