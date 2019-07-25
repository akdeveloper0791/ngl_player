package com.ibetter.www.adskitedigi.adskitedigi.model;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyFileAsync extends AsyncTask<File,Void,Boolean> {
  private Context context;private boolean isDeleteSource =false;

   public CopyFileAsync(Context context,boolean isDeleteSource)
   {
       this.context = context;
       this.isDeleteSource = isDeleteSource;
   }
    protected Boolean doInBackground(File... params)
    {


        File source = params[0];
        File dest = params[1];

        try {
            if (source.exists()) {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(dest);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                if(isDeleteSource) {
                    source.delete();
                }

                return true;

            } else {
                return false;
            }
        }catch(Exception e){
            return false;
        }

    }


    protected void onPostExecute(final Boolean isSuccess)
    {
        if(!isSuccess) {
            Handler handler = new Handler(context.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Unable to copy file " , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
