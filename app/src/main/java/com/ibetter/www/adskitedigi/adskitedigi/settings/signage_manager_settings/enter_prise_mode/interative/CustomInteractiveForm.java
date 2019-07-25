package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.interative;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.accessibility.HandleKeyCommandsUpdateReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.database.ActionsDBHelper;
import com.ibetter.www.adskitedigi.adskitedigi.database.DataBaseHelper;
import com.ibetter.www.adskitedigi.adskitedigi.model.ActionModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.OptionalModel;
import com.ibetter.www.adskitedigi.adskitedigi.model.Validations;
import com.ibetter.www.adskitedigi.adskitedigi.settings.advance_settings.ScreenOrientationModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CustomInteractiveForm extends Activity
{
    private Context context;
    //private EditText nameET,numET,dataET1,dataET2,dataET3,dataET4,dataET5,dataET6,dataET7,dataET8;
    private Button submitBtn,cancelBtn;
    private int actionTempId;

    private Handler disconnectHandler = new Handler();
    private Timer endScheduleTimer;
    private boolean timerFlag;
    private long inActiveDuration=0;
    private long eventTime=0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ScreenOrientationModel.getSelectedScreenOrientation(this));

        super.onCreate(savedInstanceState);
        context= CustomInteractiveForm.this;

       // setContentView(R.layout.user_interaction_form);
        View layout =  getLayoutInflater().inflate(R.layout.user_interaction_form, null);
        layout.setBackgroundResource(R.drawable.dialog_border_round_corner);
        setContentView(layout);

        submitBtn=findViewById(R.id.submit_btn);
        cancelBtn=findViewById(R.id.cancel_btn);

        if(getIntent().getExtras()!=null)
        {
            actionTempId=getIntent().getIntExtra("actionTempId",0);
            displayInteractiveForm();

            timerFlag=new ActionModel().getActionInactivityTimesStatus(context);
            inActiveDuration=(long)(new ActionModel().getActionInactivityTime(context)*1000);
            Log.i("disconnectHandler","onCreate inActiveDuration:"+inActiveDuration);
            startInactivityTimer();

        }else
            {
            Toast.makeText(context, "Unable to display custom interactive form, please try again later.", Toast.LENGTH_SHORT).show();
            finish();
           }

    }


    private void displayInteractiveForm()
    {
        final EditText nameET=findViewById(R.id.name_et);
        final EditText numET=findViewById(R.id.num_et);
        new Validations().setMandatoryRequired(context,nameET);
        new Validations().setMandatoryRequired(context,numET);
        //final EditText countET=layout.findViewById(R.id.count_et);

        final EditText dataET1=findViewById(R.id.data_et1);
        final EditText dataET2=findViewById(R.id.data_et2);
        final EditText dataET3=findViewById(R.id.data_et3);
        final EditText dataET4=findViewById(R.id.data_et4);
        final EditText dataET5=findViewById(R.id.data_et5);
        final EditText dataET6=findViewById(R.id.data_et6);
        final EditText dataET7=findViewById(R.id.data_et7);
        final EditText dataET8=findViewById(R.id.data_et8);

        displayOptionalFieldNames();

        submitBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name=nameET.getText().toString();
                String num=numET.getText().toString();

                //String count=countET.getText().toString();
                if(dataValidation(name,num,nameET,numET))
                {
                    ContentValues cv=new ContentValues();
                    cv.put(ActionsDBHelper.CUSTOMER_NAME,name);
                    cv.put(ActionsDBHelper.CUSTOMER_NUMBER,num);
                    cv.put(ActionsDBHelper.CUSTOMER_STATUS,0);
                    cv.put(ActionsDBHelper.CUSTOMER_ACTION_TEXT,"");
                    cv.put(ActionsDBHelper.CUSTOMER_CREATED_AT, Calendar.getInstance().getTimeInMillis());
                    cv.put(ActionsDBHelper.CUSTOMER_ACTION_TEMPLATE_ID,actionTempId);

                    ArrayList<OptionalModel> dataList=new ArrayList<>();
                    String data1=dataET1.getText().toString();
                    if(data1!=null && data1.length()>=1)
                    {

                        OptionalModel model=new OptionalModel();
                        model.setValue(data1);
                        TextView dataTV1=findViewById(R.id.optional_data1);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }
                    String data2=dataET2.getText().toString();
                    if(data2!=null && data2.length()>=1)
                    {
                        OptionalModel model=new OptionalModel();
                        model.setValue(data2);
                        TextView dataTV1=findViewById(R.id.optional_data2);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }
                    String data3=dataET3.getText().toString();
                    if(data3!=null && data3.length()>=1)
                    {
                        OptionalModel model=new OptionalModel();
                        model.setValue(data2);
                        TextView dataTV1=findViewById(R.id.optional_data3);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }

                    String data4=dataET4.getText().toString();
                    if(data4!=null && data4.length()>=1)
                    {
                        OptionalModel model=new OptionalModel();
                        model.setValue(data4);
                        TextView dataTV1=findViewById(R.id.optional_data4);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }
                    String data5=dataET5.getText().toString();
                    if(data5!=null && data5.length()>=1)
                    {
                        OptionalModel model=new OptionalModel();
                        model.setValue(data5);
                        TextView dataTV1=findViewById(R.id.optional_data5);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }
                    String data6=dataET6.getText().toString();
                    if(data6!=null && data6.length()>=1)
                    {
                        OptionalModel model=new OptionalModel();
                        model.setValue(data6);
                        TextView dataTV1=findViewById(R.id.optional_data6);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }

                    String data7=dataET7.getText().toString();
                    if(data7!=null && data7.length()>=1)
                    {
                        OptionalModel model=new OptionalModel();
                        model.setValue(data7);
                        TextView dataTV1=findViewById(R.id.optional_data7);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }
                    String data8=dataET8.getText().toString();
                    if(data8!=null && data8.length()>=1)
                    {
                        OptionalModel model=new OptionalModel();
                        model.setValue(data8);
                        TextView dataTV1=findViewById(R.id.optional_data8);
                        String data=dataTV1.getText().toString();
                        data=data.replace(context.getString(R.string.optional_text_data_length),"");
                        model.setKey(data);
                        dataList.add(model);
                    }

                    boolean flag=new ActionsDBHelper(context).insertCustomerInfo(cv,dataList);
                    if(flag)
                    {
                        Toast.makeText(context, context.getString(R.string.restaurants_waiting_list_data_record_string), Toast.LENGTH_SHORT).show();
                        interactiveAppInvoke();

                    }else
                    {
                        Toast.makeText(context, "Dear Guest, We are unable to take your data. Please try again", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }

            }

        });

        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               finish();
            }

        });

    }




    private void displayOptionalFieldNames()
    {
        try
        {
            Cursor cursor=new ActionsDBHelper(context).getInteractiveOptionalFields();
            if(cursor!=null && cursor.moveToFirst())
            {
                do {

                    int status=cursor.getInt(cursor.getColumnIndex(ActionsDBHelper.OPTIONAL_FIELD_FLAG));
                    if(status==1)
                    {
                        int id=cursor.getInt(cursor.getColumnIndex(DataBaseHelper.LOCAL_ID));
                        String fieldName=cursor.getString(cursor.getColumnIndex(ActionsDBHelper.OPTIONAL_FIELD_NAME));
                        if(fieldName!=null && fieldName.length()>=2)
                        {
                            setFieldName(id,fieldName);
                        }
                    }
                }while (cursor.moveToNext());
            }

        }catch (Exception exp)
        {
            exp.printStackTrace();
        }


    }

    private void setFieldName(int id,String dataName)
    {
        switch (id)
        {
            case 1:

                LinearLayout optionalLayout1=findViewById(R.id.optional_layout1);
                optionalLayout1.setVisibility(View.VISIBLE);
                TextView dataTV1=findViewById(R.id.optional_data1);
                dataTV1.setText(dataName+context.getString(R.string.optional_text_data_length));

                break;

            case 2:
                LinearLayout optionalLayout2=findViewById(R.id.optional_layout2);
                optionalLayout2.setVisibility(View.VISIBLE);
                TextView dataTV2=findViewById(R.id.optional_data2);
                dataTV2.setText(dataName+context.getString(R.string.optional_text_data_length));

                break;

            case 3:
                LinearLayout optionalLayout3=findViewById(R.id.optional_layout3);
                optionalLayout3.setVisibility(View.VISIBLE);
                TextView dataTV3=findViewById(R.id.optional_data3);
                dataTV3.setText(dataName+context.getString(R.string.optional_text_data_length));


                break;

            case 4:

                LinearLayout optionalLayout4=findViewById(R.id.optional_layout4);
                optionalLayout4.setVisibility(View.VISIBLE);
                TextView dataTV4=findViewById(R.id.optional_data4);
                dataTV4.setText(dataName+context.getString(R.string.optional_text_data_length));

                break;

            case 5:
                LinearLayout optionalLayout5=findViewById(R.id.optional_layout5);
                optionalLayout5.setVisibility(View.VISIBLE);
                TextView dataTV5=findViewById(R.id.optional_data5);
                dataTV5.setText(dataName+context.getString(R.string.optional_text_data_length));

                break;
            case 6:

                LinearLayout optionalLayout6=findViewById(R.id.optional_layout6);
                optionalLayout6.setVisibility(View.VISIBLE);
                TextView dataTV6=findViewById(R.id.optional_data6);
                dataTV6.setText(dataName+context.getString(R.string.optional_text_data_length));


                break;

            case 7:
                LinearLayout optionalLayout7=findViewById(R.id.optional_layout7);
                optionalLayout7.setVisibility(View.VISIBLE);
                TextView dataTV7=findViewById(R.id.optional_data7);
                dataTV7.setText(dataName+context.getString(R.string.optional_text_data_length));


                break;
            case 8:
                LinearLayout optionalLayout8=findViewById(R.id.optional_layout8);
                optionalLayout8.setVisibility(View.VISIBLE);
                TextView dataTV8=findViewById(R.id.optional_data8);
                dataTV8.setText(dataName+context.getString(R.string.optional_text_data_length));

                break;

        }
    }

    private boolean dataValidation(String name, String number, EditText nameET, EditText numET)
    {
        boolean flag=true;

        if(name==null|| name.length()<=1)
        {
            flag=false;
            nameET.setError("Please enter valid name");
        }

        if(number==null|| number.length()<=1)
        {
            flag=false;
            numET.setError("Please enter valid number");
        }

        return flag;

    }

    private void startInactivityTimer()
    {
        if (timerFlag)
        {
             Log.i("OtherAppInvokeService","startInactivityTimer:time"+inActiveDuration);
            if (inActiveDuration>0)
            {
                endScheduleTimer = new Timer();
                endScheduleTimer.schedule(new InActiveTimerTask(), inActiveDuration);
            }
        }
    }

    private class InActiveTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            // run on another thread
            disconnectHandler.post(new Runnable() {
                @Override
                public void run()
                {
                    //long eventTime= HandleKeyCommands.eventTime;
                    if(eventTime>0)
                    {
                        long currentTime= Calendar.getInstance().getTimeInMillis();
                        if(currentTime>eventTime)
                        {
                            long diff=currentTime-eventTime;
                            // Log.i("OtherAppInvokeService","diff:"+diff);
                            long extraDuration=inActiveDuration-diff;
                            Log.i("OtherAppInvokeService","extraDuration:"+extraDuration);
                            if(extraDuration>0)
                            {
                                stopTimer();
                                endScheduleTimer=  new Timer();
                                endScheduleTimer.schedule(new InActiveTimerTask(), extraDuration);
                            }else
                            {
                                finish();
                            }

                        }else if(currentTime==eventTime)
                        {
                            stopTimer();
                            endScheduleTimer=  new Timer();
                            endScheduleTimer.schedule(new InActiveTimerTask(), inActiveDuration);
                            Log.i("OtherAppInvokeService","restart InActiveTimerTask:"+inActiveDuration);
                        }

                    }else
                    {
                          Log.i("OtherAppInvokeService","restartApp eventTime:"+eventTime);
                        //restart signage player
                        finish();
                    }
                }

            });

        }
    }

    private void stopTimer()
    {
        if(endScheduleTimer!=null)
        {
            eventTime=0;
            endScheduleTimer.cancel();
            endScheduleTimer.purge();
        }
    }

    @Override
    public void onUserInteraction()
    {
        if(timerFlag)
        {
            eventTime= Calendar.getInstance().getTimeInMillis();
            Log.i("OtherAppInvokeService","onUserInteraction:eventTime"+eventTime);
        }
    }


    private void interactiveAppInvoke()
    {
        try
        {
            Cursor cursor=new ActionsDBHelper(context).getAppInvokePackageName(9,1);
            if(cursor!=null && cursor.moveToFirst())
            {
                String appPackageName=cursor.getString(cursor.getColumnIndex(ActionsDBHelper.OPTIONAL_FIELD_NAME));
                launchThirdParty(appPackageName);
            }else
            {
                finish();
            }

        }catch (Exception exp)
        {
            exp.printStackTrace();
            finish();
        }
    }


    private void launchThirdParty(String appPackageName)
    {
        Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
        if (mIntent != null)
        {
            try {
               // mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               // mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
               // mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mIntent);

                thirdPartyAppInvokeService();

            } catch (ActivityNotFoundException err)
            {
                Toast.makeText(context, "No Application found with selected package name, Please download it from play store", Toast.LENGTH_SHORT).show();
                tryToOpenAppInPlayStore(appPackageName);
            }
        }else
        {
            tryToOpenAppInPlayStore(appPackageName);
        }


    }

    private void tryToOpenAppInPlayStore(String appPackageName)
    {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe)
        {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }

        thirdPartyAppInvokeService();
    }

    private void thirdPartyAppInvokeService()
    {
        if(new ActionModel().getInactivityTimerFlag(context))
        {
            saveAppInvokeStatus(true);

            if(ActionModel.checkAccessibilityService())
            {
                try
                {
                    Intent appIntent = new Intent(context, MonitorAppInvokeService.class);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    {
                        CustomInteractiveForm.this.startForegroundService(appIntent);
                    }else{
                        startService(appIntent);
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            finish();
        }
    }

    private void saveAppInvokeStatus(boolean status)
    {
        try
        {
            if(MonitorAppInvokeService.isServiceActive)
            {
                Log.i("OtherAppInvokeService","isServiceActive:"+MonitorAppInvokeService.isServiceActive);
                stopService(new Intent(context, MonitorAppInvokeService.class));
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        Intent intent = new Intent(HandleKeyCommandsUpdateReceiver.INTENT_ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("action",HandleKeyCommandsUpdateReceiver.IS_THIRD_PARTY_APP_INVOKE);
        intent.putExtra("value",status);
        sendBroadcast(intent);
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        Log.i("OtherAppInvokeService","onRestart:startInactivityTimer");
        startInactivityTimer();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.i("OtherAppInvokeService","onStop:stopDisconnectTimer");
        stopTimer();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i("OtherAppInvokeService","onDestroy:stopDisconnectTimer");
        stopTimer();

    }

}
