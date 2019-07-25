package com.ibetter.www.adskitedigi.adskitedigi.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.SharedPreferenceModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public class LoginViewModel {
    private static EditText loginLayoutDisplayNameEditText, loginLayoutDisplayLocationDescriptionEditText, loginLayoutMobileNumberEditText;
    private static Button loginLayoutLoginButton;
    private static String userMobileNumber;
    private static Context context;
    private View layout;

    public LoginViewModel(Context context,View layout) {
        this.context=context;
        this.layout=layout;

        loginLayoutMobileNumberEditText=layout.findViewById(R.id.register_layout_mobile_number_et);
        loginLayoutDisplayNameEditText=layout.findViewById(R.id.register_layout_display_name_et);
        loginLayoutDisplayLocationDescriptionEditText=layout.findViewById(R.id.register_layout_display_location_info_et);
        loginLayoutLoginButton=layout.findViewById(R.id.register_layout_register_button);

    }


    public static EditText getLoginLayoutDisplayNameEditText() {
        return loginLayoutDisplayNameEditText;
    }



    public static EditText getLoginLayoutDisplayLocationDescriptionEditText() {
        return loginLayoutDisplayLocationDescriptionEditText;
    }

    public static Context getContext() {
        return context;
    }


    public EditText getLoginLayoutMobileNumberEditText() {
        return loginLayoutMobileNumberEditText;
    }



    public Button getLoginLayoutLoginButton() {
        return loginLayoutLoginButton;
    }

    /*set and get user mobile number*/
    public void setUserMobileNumber(String userMobileNumber) {
        this.userMobileNumber = userMobileNumber;
    }

    public String getUserMobileNumber() {
        return userMobileNumber;
    }




    /*check for valid mobile number*/
    public boolean isValidMobileNumber() {

        return new Validations().validateMobileNumber(context, userMobileNumber);

    }


    /*check for valid mobile number*/
    public boolean isValidDisplayName() {
        return new Validations().validateString(loginLayoutDisplayNameEditText.getText().toString());
    }

    public void setLoginLayoutMobileNumberETErrorMSG(String errorMSG) {
        loginLayoutMobileNumberEditText.setError(errorMSG);
    }

    public void setLoginLayoutDisplayNameETErrorMSG(String errorMSG) {
        loginLayoutDisplayNameEditText.setError(errorMSG);
    }


    public void assignLoginLayoutMobileNumberET(String text) {

        if (text != null) {
            loginLayoutMobileNumberEditText.setText(text);
        }

    }







}
