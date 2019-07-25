package com.ibetter.www.adskitedigi.adskitedigi.login;

/**
 * Created by vineeth_ibetter on 11/16/16.
 */

public interface LoginInterface {


    /*user login*/
    public void login();

    /*send Request For Register Display*/
    public void registerDisplay();

    /*save user details*/
    public void saveDetails();

    /*success*/
    public  void success();

    /*failure*/
    public  void failure();

    /* validate require fields */
    public boolean validateRequireLoginFields();


}
