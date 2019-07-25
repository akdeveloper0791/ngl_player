package com.ibetter.www.adskitedigi.adskitedigi.display_ads;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.Constants;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * Created by vineeth_ibetter on 11/23/16.
 */

public class DisplayAdsMedia {

    private String media;//media name

    private long mediaId;//server media id

    private String mediaType;

    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    private long previousChunkSize= Constants.DEFAULT_DOWNLOADABLE_CHUNK_SIZE;

    private long modifiedChunkSize=0;

    public void setMedia(String media,Context context)
    {

        this.media=media;

        setMediaType(media,context);

    }

    public void setMediaId(long mediaId)
    {

        this.mediaId=mediaId;
    }

    private void setMediaType(String media,Context context)
    {
        if(media!=null) {
            String mediaTypeArray[] = media.split(context.getString(R.string.layout_media_extension_seperator));

            if (mediaTypeArray.length > 1) {
                String mediaExtension = mediaTypeArray[mediaTypeArray.length - 1];

                if (mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_wmv)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_avi)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_mpg)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_mpeg)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_webm)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_mp4))||
                mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_3gp))||
                                mediaExtension.equalsIgnoreCase(context.getString(R.string.media_video_mkv)
                ))
                {
                    mediaType = context.getString(R.string.app_default_video_name);

                } else if (mediaExtension.equalsIgnoreCase(context.getString(R.string.media_image_jpg)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_image_jpeg)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_image_png)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_image_bmp)) ||
                        mediaExtension.equalsIgnoreCase(context.getString(R.string.media_image_gif))
                        ) {
                    mediaType = context.getString(R.string.app_default_image_name);
                }

            }
            else
            {
                mediaType = null;
            }
        }else
        {
            mediaType = null;
        }

    }

    public String getMedia()
    {
        return media;
    }

    public long getMediaId()
    {
        return mediaId;
    }

    public String getMediaType()
    {
        return mediaType;
    }

    //covert to string to bit map

    public Bitmap stringToBitMap(String encodedString,Context context){

        try
        {

            String filePath = getRealPathFromURI(encodedString,context);
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();
          //  options.inJustDecodeBounds = true;
            Bitmap bmp = decodeFile(filePath,options);

            options.inSampleSize = 1;//calculateInSampleSize(options,options.outWidth ,options.outHeight );
            bmp= BitmapFactory.decodeFile(filePath,options);

            return bmp;
          // scaleUpBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.adskite_logo),imageWidth,imageHeight,context);

        }
        catch(Exception e)
        {
          e.printStackTrace();
            return null;
        }
    }


    private  Bitmap scaleUpBitmap(Bitmap photo,int imageWidth,int imageHeight,Context context) {

        final float densityMultiplier =context.getResources().getDisplayMetrics().density;

        int h= (int) (imageHeight*densityMultiplier);


        photo=Bitmap.createScaledBitmap(photo, imageWidth, h, true);

        return photo;
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

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;


        return inSampleSize;
    }

    public long getPreviousChunkSize() {
        return previousChunkSize;
    }

    public void setPreviousChunkSize(long previousChunkSize) {
        this.previousChunkSize = previousChunkSize;
    }

    public long getModifiedChunkSize() {
        return modifiedChunkSize;
    }

    public void setModifiedChunkSize(long modifiedChunkSize) {
        this.modifiedChunkSize = modifiedChunkSize;
    }
}
