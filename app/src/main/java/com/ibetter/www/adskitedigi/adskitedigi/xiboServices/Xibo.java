package com.ibetter.www.adskitedigi.adskitedigi.xiboServices;

import android.content.Context;

import com.ibetter.www.adskitedigi.adskitedigi.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapSerializationEnvelope;

/**
 * Created by vineeth_ibetter on 11/2/16.
 */

public class Xibo
{
    public static final String XIBO_LAYOUT_TAG = "layout";
    public static final String XIBO_MEDIA_TAG = "media";
    public static final String XIBO_RESOURCE_TAG = "resource";
    public static final String XIBO_DEFAULT_TAG = "default";
    public static final String NAMESPACE = "urn:xmds";
    public static final String OPERATION_BLACKLIST = "urn:xmds#BlackList";
    public static final String OPERATION_GET_FILE = "urn:xmds#GetFile";
    public static final String OPERATION_GET_RESOURCE = "urn:xmds#GetResource";
    public static final String OPERATION_MEDIA_INVENTORY = "urn:xmds#MediaInventory";
    public static final String OPERATION_NOTIFY_STATUS = "urn:xmds#NotifyStatus";
    public static final String OPERATION_REGISTER_DISPLAY = "urn:xmds#RegisterDisplay";
    public static final String OPERATION_REQUIRED_FILES = "urn:xmds#RequiredFiles";
    public static final String OPERATION_SCHEDULE = "urn:xmds#Schedule";
    public static final String OPERATION_SUBMIT_LOG = "urn:xmds#SubmitLog";
    public static final String OPERATION_SUBMIT_SCREENSHOT = "urn:xmds#SubmitScreenShot";
    public static final String OPERATION_SUBMIT_STATS = "urn:xmds#SubmitLog";

 //public  static final String SERVER_KEY="43e3NE";
    public  static final String SERVER_KEY="krishna";
    public static final String XML_VERSION="<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->";

    public  static final int  XIBO_API_DURATION=60000;
    protected Context context;
    protected String macAddress;
    private String mainRequestUrl;
    private String atualSoapAction;

    //get xibo preference url
    public static String getXiboPrefsServerUrl(Context context)
    {

        String url = context.getString(R.string.xibo_pref_serverurl_default);
        char lastURLChar = url.charAt(url.length() - 1);
        if (lastURLChar == '\\' || lastURLChar == '/') {
            return url;
        }

        return url.concat("/");
    }

    public static SoapSerializationEnvelope getXiboEnvelop()
    {

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        return  envelope;

    }


}
