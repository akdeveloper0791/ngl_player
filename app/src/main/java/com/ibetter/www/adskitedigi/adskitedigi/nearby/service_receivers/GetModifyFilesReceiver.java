package com.ibetter.www.adskitedigi.adskitedigi.nearby.service_receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class GetModifyFilesReceiver extends ResultReceiver {

    public final static int SEND_FILE = 1;
    public final static int NO_FILES = 2;
    public final static int ERROR_IN_PROCESSING = 3;
    public final static int SUCCESS_SENDING = 4;

    GetModifyFilesReceiverCallBacks mReceiver;

    public GetModifyFilesReceiver(Handler handler)
    {
        super(handler);
    }

    public void setReceiver(GetModifyFilesReceiverCallBacks receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

          if (mReceiver != null) {

              if(resultCode==ERROR_IN_PROCESSING)
              {
                  mReceiver.onError(resultData.getString("errorMsg"));
              }else
              {
                  mReceiver.onReceive(resultData.getString("info"));
              }


            }else
            {
                  Log.d("HandleModifyReceiver","Inside HandleModifyReceiver mReceiver is null");

            }

    }


    public interface GetModifyFilesReceiverCallBacks
   {
      public void onReceive(String resultData);
       public void onError(String errorMsg);
   }
}
