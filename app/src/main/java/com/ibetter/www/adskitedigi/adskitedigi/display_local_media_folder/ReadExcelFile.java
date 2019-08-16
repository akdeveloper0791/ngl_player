package com.ibetter.www.adskitedigi.adskitedigi.display_local_media_folder;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.model.CustomGridLayoutManager;
import com.ibetter.www.adskitedigi.adskitedigi.model.DeviceModel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;


import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ReadExcelFile implements Runnable
{
    private String path;
    private Context context;
    private WeakReference<DisplayLocalFolderAds> actReference;
    private HashMap<String,Integer> deviceInfo;
    private RecyclerView parentLayout;
    private Handler handler;



    public ReadExcelFile(String path,Context context,DisplayLocalFolderAds activity,
                         Handler handler,RecyclerView parentLayout) {
        this.path = path;
        this.context = context;
        actReference = new WeakReference(activity);
        deviceInfo  = new DeviceModel().getDeviceProperties(activity);
        this.parentLayout = parentLayout;
        this.handler = handler;
    }

    @Override
    public void run() {
        readExcelFile(path);
    }

    private  void readExcelFile( String filename)
    {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.e("EXCEL", "Storage not available or read only");
            return;
        }

        ArrayList<String> list=new ArrayList();

        int noOfColms=0;
        try
        {
            // Creating Input Stream
            File file = new File( filename);

            if(file.exists()) {
                Log.i("info start","excel");
                FileInputStream myInput = new FileInputStream(file);

                // Create a POIFSFileSystem object
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

                // Create a workbook using the File System
                HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

                // Get the first sheet from workbook
                HSSFSheet mySheet = myWorkBook.getSheetAt(0);

                /** We now need something to iterate through the cells.**/
                Iterator rowIter = mySheet.rowIterator();


                int rowNO=0;



                while (rowIter.hasNext()) {
                    HSSFRow row = (HSSFRow) rowIter.next();
                    int patchColumn = -1;
                    Log.d("excel row",++rowNO+"\n");

                    noOfColms=row.getLastCellNum();

                    for (int cn=0; cn<row.getLastCellNum(); cn++) {
                        Log.i(rowNO+"EXCEL cell NO",cn+"");

                        Cell cell = row.getCell(cn);

                        //CellStyle cellStyle= cell.getCellStyle();

                        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                            // Can't be this cell - it's empty
                            Log.i("EXCEL cell value:",cn+" :"+"EMPTY");
                            list.add("");
                            continue;
                        }

                        String text=null;
                        switch (cell.getCellType()) {

                            case HSSFCell.CELL_TYPE_FORMULA:
                                // Get the type of Formula
                                switch (cell.getCachedFormulaResultType()){
                                    case HSSFCell.CELL_TYPE_STRING:
                                        text = cell.getStringCellValue();
                                        break;
                                    case HSSFCell.CELL_TYPE_NUMERIC:
                                        text = cell.getNumericCellValue()+"";
                                        break;
                                    default:
                                }
                                break;

                            case HSSFCell.CELL_TYPE_NUMERIC:
                                Double dval = cell.getNumericCellValue();
                                text = dval + "";
                                break;

                            case HSSFCell.CELL_TYPE_STRING:
                                text = cell.getStringCellValue();
                                break;

                            case HSSFCell.CELL_TYPE_BLANK:
                                text = "";
                                break;

                            case HSSFCell.CELL_TYPE_BOOLEAN:
                                text = cell.getStringCellValue();
                                break;

                            default:
                        }
                        Log.i("EXCEL cell value:",cn+" :"+text);
                        list.add(text);

                        if ("Patch".equals(text)) {
                            patchColumn = cn;
                            Log.d("EXCEL", " patchColumn" + patchColumn);
                            break;
                        }


                    }



                    Log.d("EXCEL", "no of cells " + row.getLastCellNum());



                }




            }else
            {
                Log.d("EXCEL", "File is not exist " );
            }

        }catch (Exception e){e.printStackTrace(); }
        finally {


         if(list.size()>0)
            {
              handler.post(new CreateExcelFileRegion(list,noOfColms));



            }else
            {
                Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
            }



         // DisplayLocalFolderAds.excelResultReceiver.send(ExcelResultReceiver.RESPONSE,bundle);

           // stopSelf();
        }

        return;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<String> list = new ArrayList<>();
        private Context context;

        public Adapter(ArrayList<String> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.support_excel_layout, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(list.get(position));
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;
            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textview);
            }
        }
    }

    private class CreateExcelFileRegion implements Runnable
    {
        private   ArrayList<String> list;
        private int noOfCol;


        public CreateExcelFileRegion(ArrayList<String> list,int noOfCol)
        {
        this.list=list;
        this.noOfCol=noOfCol;
        }

        public void run()
        {

            try {
                CustomGridLayoutManager manager = new CustomGridLayoutManager(context, noOfCol, GridLayoutManager.VERTICAL, false);
                parentLayout.setLayoutManager(manager);
                //initialize your list in this method
               final Adapter adapter = new Adapter(list, context);
                parentLayout.setAdapter(adapter);

                parentLayout.smoothScrollToPosition((list.size()-1));



                parentLayout.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if(newState==RecyclerView.SCROLL_STATE_IDLE) {


                            recyclerView.scrollToPosition(0);

                            adapter.notifyDataSetChanged();

                            recyclerView.smoothScrollToPosition(list.size()-1);
                        }
                    }
                });

            }catch (Exception E)
            {
                E.printStackTrace();
            }



        }
    }


}
