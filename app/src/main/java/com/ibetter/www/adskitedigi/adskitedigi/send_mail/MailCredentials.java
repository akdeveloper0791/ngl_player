package com.ibetter.www.adskitedigi.adskitedigi.send_mail;

/**
 * Created by vineeth_ibetter on 10/16/15.
 */
public class MailCredentials {


    private final static String ADS_KITE_SALES_EMAIL="contact@adskite.com";



    private final static String ADS_KITE_QUERIES_EMAIL="adskitesales@gmail.com";
    private final static String ADS_KITE_QUERIES_EMAIL_PWD="adskite123";


    public static String getAdsKiteContactEmailPwd()
    {
        return ADS_KITE_QUERIES_EMAIL_PWD;
    }

     public static String getDigiContactEmail()
    {
        return ADS_KITE_SALES_EMAIL;
    }

    public static String getDigiInfoEmail()
    {
        return ADS_KITE_QUERIES_EMAIL;
    }


}
