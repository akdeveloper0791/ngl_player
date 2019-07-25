package com.ibetter.www.adskitedigi.adskitedigi.green_content;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_services.GCLoginIntentService;
import com.ibetter.www.adskitedigi.adskitedigi.green_content.gc_model.GCUtils;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ibetter.www.adskitedigi.adskitedigi.green_content.downloadCampaign.DownloadCampaigns.GC_LOGIN_ACTION;

public class GCLoginActivity extends Activity
{
    private Context context;
    private EditText emailET,passwordET;
    private LoginResultReceiver loginResultReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        context=GCLoginActivity.this;

        setContentView(R.layout.gc_login_activity);

         emailET=findViewById(R.id.email_et);
         passwordET=findViewById(R.id.password_et);

         setPreviousLoginInfo();

         logInWithGCServer();

         forgotPassword();
         newAccountSignUpPage();

    }


    private void forgotPassword()
    {
        Button forgotPwdBtn=findViewById(R.id.forgot_pwd);
        forgotPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lunchGreenContentWebView(GCUtils.GC_LOGIN_FORGET_PASSWORD_URL);
            }
        });
    }

    private void lunchGreenContentWebView(String url)
    {
        Intent intent=new Intent(context,GCContentWebView.class);
        intent.putExtra("gc_content_url",url);
        startActivity(intent);
    }

    private void newAccountSignUpPage()
    {
        Button signupBtn=findViewById(R.id.create_new_btn);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               lunchGreenContentWebView(GCUtils.GC_USER_SIGNUP_URL);
            }
        });

    }




    @Override
    public void onBackPressed()
    {
        //Toast.makeText(context,resultData.getString("status"),Toast.LENGTH_SHORT).show();
        sendResults(false);
        super.onBackPressed();
    }

    private void setPreviousLoginInfo()
    {
        SharedPreferences sp=getSharedPreferences(getString(R.string.user_details_sp), MODE_PRIVATE);

        String emailId=sp.getString(getString(R.string.gc_user_email_id),null);
        String password=sp.getString(getString(R.string.gc_user_password),null);

        if(emailId!=null && password!=null)
        {
            emailET.setText(emailId);
            passwordET.setText(password);
        }
    }

    private void logInWithGCServer()
    {
        Button loginBtn=findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                String email=emailET.getText().toString();
                String password=passwordET.getText().toString();
                if(validateUserInfo(email,password))
                {
                  emailET.setError(null);
                  passwordET.setError(null);

                startGCLoginService(email,password);
                }

            }
        });

    }

    private void startGCLoginService(String emailId,String password)
    {
        loginResultReceiver = new LoginResultReceiver(new Handler());

        Intent startIntent = new Intent(context, GCLoginIntentService.class);
        startIntent.putExtra("receiver", loginResultReceiver);
        startIntent.putExtra("email_id", emailId);
        startIntent.putExtra("password", password);
        startService(startIntent);
        enableProgressBar();
    }


    private class LoginResultReceiver extends ResultReceiver
    {
        public LoginResultReceiver(Handler handler)
        {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData)
        {
            disableProgressBar();

            switch (resultCode)
            {
                case GCLoginIntentService.LOGIN_SUCCESS:
                    if(resultData!=null)
                    {
                        if(resultData.getBoolean("flag"))
                        {
                            // Log.i("FullCampaignService","Login:"+resultData.getString("status"));
                            Toast.makeText(context,resultData.getString("status"),Toast.LENGTH_SHORT).show();
                            new User().saveGCUserPassword(context,passwordET.getText().toString());
                            sendResults(true);
                        }else
                        {
                            Toast.makeText(context,resultData.getString("status"),Toast.LENGTH_SHORT).show();
                            sendResults(false);
                        }
                    }else
                    {
                        Toast.makeText(context, "Unable to Login, Please Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }


    private void  enableProgressBar()
    {
        LinearLayout progressLayout=findViewById(R.id.progress_layout);
        progressLayout.setVisibility(View.VISIBLE);
    }



    private void  disableProgressBar()
    {
        LinearLayout progressLayout=findViewById(R.id.progress_layout);
        progressLayout.setVisibility(View.GONE);
    }


    private boolean validateUserInfo(String emailId,String password)
    {
        boolean flag=true;

      if(emailId==null||!isValidEmailId(emailId))
      {
        flag=false;
        emailET.setError(getString(R.string.gc_user_email_err_msg));
      }

      if(password==null ||password.length()<=3)
      {
          flag=false;
          passwordET.setError(getString(R.string.gc_user_password_err_msg));
      }

      return flag;
    }

   /* @param email
 * @return boolean true for valid false for invalid
 */
    public static boolean isValidEmailId(String email)
    {

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();

    }

    private void sendResults(boolean flag)
    {
        // put the String to pass back into an Intent and close this activity
        Intent intent = new Intent();
        intent.putExtra("flag",flag);

        setResult(RESULT_OK, intent);
        finish();
    }


}
