package com.ibetter.www.adskitedigi.adskitedigi.send_mail;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;

/**
 * Created by vineeth_ibetter on 5/4/17.
 */

public class SendMailToDigiContact extends IntentService {

    public SendMailToDigiContact()
    {
        super("SendMailToDigiContact");
    }

    protected void onHandleIntent(Intent intent)
    {
        String frommail=MailCredentials.getDigiContactEmail();
        String pwd=MailCredentials.getAdsKiteContactEmailPwd();
        String subjectmsg="info";
        String msg=getString(R.string.mail_info_1)+new DeviceModel().generateHardwareKey(SendMailToDigiContact.this)+getString(R.string.mail_info_2);

        String[] numbers={MailCredentials.getDigiContactEmail(),MailCredentials.getDigiInfoEmail()};

        Mail mail = new Mail(frommail,pwd);

        if (subjectmsg != null && subjectmsg.length() > 0) {
            mail.setSubject(subjectmsg);
        } else {
            mail.setSubject("");
        }
        String newemails = frommail.split("@")[1].toString();


        if(frommail.split("@")[1].contains("gmail"))
        {
            mail.setHost("smtp.gmail.com");
            mail.setPort("465");
            mail.setSPort("465");

        }
        else if(frommail.split("@")[1].contains("yahoo"))
        {
            mail.setHost("smtp.mail.yahoo.com");
            mail.setPort("465");
            mail.setSPort("465");
        }
        else
        {
            mail.setHost("mail.adskite.com");
            mail.setPort("587");
            mail.setSPort("587");
        }
        if (msg != null && msg.length() > 0) {
            mail.setBody(msg);
        } else {
            mail.setBody("");
        }
        mail.setFrom(frommail);
        mail.setTo(numbers);

        try
        {
            if( mail.send()) {

                Log.i("info","Successfully informed");

            }else
            {
                Log.i("info","Unable to send mail");
            }

        }
        catch (Exception e)
        {

            Log.i("info","Unable to send mail");
            e.printStackTrace();

        }
    }




}
