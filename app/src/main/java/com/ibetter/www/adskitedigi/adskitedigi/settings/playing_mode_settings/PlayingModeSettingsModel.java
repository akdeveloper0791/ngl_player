package com.ibetter.www.adskitedigi.adskitedigi.settings.playing_mode_settings;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;

/**
 * Created by vineeth_ibetter on 12/31/17.
 */

public class PlayingModeSettingsModel {

    private RadioButton nearByModeRB,cloudModeRB,enterpriseModeRB;
    private RadioGroup CMSModeRadioGroup;
    private Button updateButton;
    private EditText urlET,userNameET,userPwdET;
    private LinearLayout cmsInfoLayout;
    private Context context;
    private View layout;

    public static final int NEAR_BY_MODE=R.id.near_by_mode;
    public static  final int ENTERPRISE_MODE=R.id.enterprise_mode;
    public static  final int CLOUD_MODE=R.id.cloud_mode;


    public PlayingModeSettingsModel(View layout,
                                    Context context)

    {
        this.layout=layout;
        this.nearByModeRB = layout.findViewById(R.id.near_by_mode);
        this.enterpriseModeRB = layout.findViewById(R.id.enterprise_mode);
        this.cloudModeRB = layout.findViewById(R.id.cloud_mode);
        this.CMSModeRadioGroup=  layout. findViewById(R.id.cms_mode_layout);
        this.updateButton=layout. findViewById(R.id.update);
        this.urlET= layout.findViewById(R.id.url);
        this.userNameET=layout.findViewById(R.id.user_name);
        this.userPwdET= layout.findViewById(R.id.user_pwd);
        this.cmsInfoLayout= layout.findViewById(R.id.mode_details);
        this.context=context;


        setRadioButtons();

    }


    private void setRadioButtons() {

        int mode = new User().getUserPlayingMode(context);

        switch (mode)
        {

            case Constants.NEAR_BY_MODE:

                nearByModeRB.setChecked(true);
                cmsInfoLayout.setVisibility(View.GONE);

                break;

            case Constants.ENTERPRISE_MODE:
                enterpriseModeRB.setChecked(true);
                cmsInfoLayout.setVisibility(View.VISIBLE);
                urlET.setVisibility(View.VISIBLE);

                displayModeInfo(Constants.ENTERPRISE_MODE);
                break;

            case Constants.CLOUD_MODE:
                cloudModeRB.setChecked(true);
                cmsInfoLayout.setVisibility(View.VISIBLE);
                urlET.setVisibility(View.GONE);
                displayModeInfo(Constants.CLOUD_MODE);
                break;


        }



        CMSModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                int mode=radioGroup.getCheckedRadioButtonId();

                switch (mode)
                {

                    case NEAR_BY_MODE:

                        nearByModeRB.setChecked(true);
                        cmsInfoLayout.setVisibility(View.GONE);


                        break;

                    case ENTERPRISE_MODE:
                        enterpriseModeRB.setChecked(true);
                        cmsInfoLayout.setVisibility(View.VISIBLE);
                        urlET.setVisibility(View.VISIBLE);
                        displayModeInfo(Constants.ENTERPRISE_MODE);
                        break;

                    case CLOUD_MODE:
                        cloudModeRB.setChecked(true);
                        cmsInfoLayout.setVisibility(View.VISIBLE);
                        urlET.setVisibility(View.GONE);
                        displayModeInfo(Constants.CLOUD_MODE);
                        break;


                }
            }
        });

    }

    private void clearEts()
    {
        urlET.setText("");
        userPwdET.setText("");
        userNameET.setText("");
    }

    private void displayModeInfo(int mode)
    {
        String GCUserMailId=null,GCUserPwd=null;
        if(mode==Constants.CLOUD_MODE) {
            GCUserMailId = new User().getGCUserMailId(context);
            GCUserPwd = new User().getGCUserPwd(context);

        }else if(mode==Constants.ENTERPRISE_MODE)
        {
            GCUserMailId = new User().getEnterpriseUserMailId(context);
            GCUserPwd = new User().getEnterpriseUserPwd(context);
            String enterPriseURL = new User().getEnterPriseURL(context);

            urlET.setText(enterPriseURL);
        }



        if(GCUserMailId!=null)
        {
            userNameET.setText(GCUserMailId);
        }else
        {
            userNameET.setText("");
        }
        if(GCUserPwd!=null)
        {
            userPwdET.setText(GCUserPwd);
        }else
        {
            userPwdET.setText("");

        }

    }

    public Button getUpdateButton() {
        return updateButton;
    }



    public Context getContext() {
        return context;
    }


    public RadioGroup getCMSModeRadioGroup() {
        return CMSModeRadioGroup;
    }

    public EditText getUrlET() {
        return urlET;
    }

    public EditText getUserNameET() {
        return userNameET;
    }

    public EditText getUserPwdET() {
        return userPwdET;
    }
}
