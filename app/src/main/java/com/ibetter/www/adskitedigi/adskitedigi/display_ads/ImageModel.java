package com.ibetter.www.adskitedigi.adskitedigi.display_ads;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ANIL on 06-Mar-17.
 */

public class ImageModel
{

    public Bitmap compressImage(String imageUri,Context context)
    {

        String filePath = getRealPathFromURI(imageUri,context);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath,options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        //calculate and add 45% and add to resized to dimensions
        ArrayList<Integer> values=getImgCompressValues(actualWidth,actualHeight);
        if(values.size()>0)
        {
            //actualWidth=Math.round(values.get(0));
            actualWidth=values.get(0);
            actualHeight=values.get(1);
            Log.i("BitmapImage","image after getImgCompressValues  w*h:"+actualWidth+"*"+actualHeight);
        }

        options.inSampleSize =calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16*1024];

        try{
            bmp = BitmapFactory.decodeFile(filePath,options);
        }
        catch(OutOfMemoryError exception)
        {
            exception.printStackTrace();
        }
        try{
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
          }
        catch(OutOfMemoryError exception)
        {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float)options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth()/2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);

            } else if (orientation == 3) {
                matrix.postRotate(180);

            } else if (orientation == 8) {
                matrix.postRotate(270);

            }
            return Bitmap.createBitmap(scaledBitmap, 0, 0,scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }



    }





    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap)
        {
            inSampleSize++;
        }

        return inSampleSize;
    }




    public String getRealPathFromURI(String contentURI,Context context)
    {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }


    public ArrayList<Integer> getImgCompressValues(int width, int height)
    {
        ArrayList<Integer> valuesArray=new ArrayList<Integer>();

      //  Log.i("BitmapImage","image original W*H:"+width+"*"+height);

        int actualWidth=width;
        int actualHeight=height;

        float maxHeight = 1280.0f;
        float maxWidth = 1280.0f;
        float imgRatio =(float) width / height;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth)
        {
            if (imgRatio < maxRatio)
            {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;

            } else if (imgRatio > maxRatio)
            {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;

            } else
            {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        Log.i("BitmapImage","image compressed w*h:"+actualWidth+"*"+actualHeight);
        valuesArray.add(actualWidth);
        valuesArray.add(actualHeight);
        return valuesArray;

    }


    public String getFilename(Context context)
    {

        File dir;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard + "/AdsKiteMobi/LeadImages");
        } else {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir() + "/AdsKiteMobi/LeadImages");
        }


        if (!dir.exists()) {
            dir.mkdirs();
        }

       return  (dir.getAbsolutePath() +"/"+System.currentTimeMillis() + ".jpg");

       /* File file = new File(Environment.getExternalStorageDirectory().getPath(), "/AdsKiteMobi/LeadImages");
        if (!file.exists())
        {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() +"/"+"IMG_"+ System.currentTimeMillis() + ".jpg");
        return uriSting;*/

    }

    public String getThumbFilename(Context context)
    {
        File dir;
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //sd card is present

            File sdCard = Environment.getExternalStorageDirectory();
            dir = new File(sdCard + "/AdsKiteMobi/LeadThumbs");
        } else {
            //save to internal memory(phone memory)
            dir = new File(context.getFilesDir() + "/AdsKiteMobi/LeadThumbs");
        }


        if (!dir.exists()) {
            dir.mkdirs();
        }

        return  (dir.getAbsolutePath() +"/"+ System.currentTimeMillis() + ".jpg");

       /* File file = new File(Environment.getExternalStorageDirectory().getPath(), "/AdsKiteMobi/LeadThumbs");
        if (!file.exists())
        {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() +"/"+ System.currentTimeMillis() + ".jpg");
        return uriSting;*/

    }

    public String leadFirstImage(ArrayList<String>list)
    {
        if(list.size()>0)
        {
            return list.get(0);
        }else
        {
            return null;
        }
    }

}
