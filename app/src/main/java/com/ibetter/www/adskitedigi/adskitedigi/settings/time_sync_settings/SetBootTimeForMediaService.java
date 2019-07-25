package com.ibetter.www.adskitedigi.adskitedigi.settings.time_sync_settings;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;

public class SetBootTimeForMediaService extends IntentService
{
    private Context context;
    private File[] files;
    public SetBootTimeForMediaService() {
        super("SetBootTimeForMediaService");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        context=SetBootTimeForMediaService.this;

        changeModifiedTime();
    }

    protected  void changeModifiedTime()
    {
        files = getAllFiles();

        if(files != null && files.length>=1) {

           modifyFile(0,1);

        }

    }

    private void modifyFile(int filePosition,int tryCount)
    {
        if(tryCount<=3)//if modifying fails try for 3 times
        {
            File file = files[filePosition];

            try {

                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                long length = raf.length();
                raf.setLength(length + 1);
                raf.setLength(length);
                raf.close();

               //Log.d("Boot time service","After modifying the media last updated time - "+file.lastModified());
                checkAndModifyNextFile(++filePosition);

            }catch(Exception e)
            {
                e.printStackTrace();
                modifyFile(filePosition,++tryCount);
            }
        }else
        {
            checkAndModifyNextFile(++filePosition);
        }
    }

    //check and modify next file
    private void checkAndModifyNextFile(int nextFilePosition)
    {
        if(files.length>nextFilePosition)
        {
            modifyFile(nextFilePosition,1);
        }
    }

    protected File[] getAllFiles()
    {
        String dir= new User().getUserPlayingFolderModePath(context);

        if(dir!=null)
        {

            File dirFile=new File(dir);

            File[] files =   dirFile.listFiles(
                    new FilenameFilter()
            {
                        @Override
                        public boolean accept(File file, String s)
                        {

                            s=s.toLowerCase();

                            if((s.endsWith(context.getString(R.string.media_video_wmv)) ||
                                    s.endsWith(context.getString(R.string.media_video_avi)) ||
                                    s.endsWith(context.getString(R.string.media_video_mpg)) ||
                                    s.endsWith(context.getString(R.string.media_video_mpeg)) ||
                                    s.endsWith(context.getString(R.string.media_video_webm)) ||
                                    s.endsWith(context.getString(R.string.media_video_mp4))||
                                    s.endsWith(context.getString(R.string.media_video_3gp))||
                                    s.endsWith(context.getString(R.string.media_video_mkv))||
                                    s.endsWith(context.getString(R.string.media_image_jpg)) ||
                                    s.endsWith(context.getString(R.string.media_image_jpeg)) ||
                                    s.endsWith(context.getString(R.string.media_image_png)) ||
                                    s.endsWith(context.getString(R.string.media_image_bmp)) ||
                                    s.endsWith(context.getString(R.string.media_image_gif)) ||
                                    s.endsWith(context.getString(R.string.media_txt))
                            ) &&  (!s.startsWith(context.getString(R.string.do_not_display_media))))
                            {
                                return true;
                            }
                            else
                            {
                                return false;
                            }
                        }
                    }
            );

            try {
                //ascending order
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {

                        return file1.lastModified() > file2.lastModified() ? -1 : (file1.lastModified() < file2.lastModified()) ? 1 : 0;
                    }
                });
                return files;
            }catch (NullPointerException e)
            {
                return null;
            }

        }
        else
        {
            return null;
        }
    }


}
