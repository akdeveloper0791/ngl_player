package com.ibetter.www.adskitedigi.adskitedigi.metrics.internal;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;
import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.download_media.DownloadMediaHelper;

import com.ibetter.www.adskitedigi.adskitedigi.iot_devices.IOTDevice;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.CameraServiceResultReceiver;
import com.ibetter.www.adskitedigi.adskitedigi.metrics.UploadMetricsFileService;
import com.ibetter.www.adskitedigi.adskitedigi.model.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.CAPTURE_IMAGE_NOTIFY_ID;
import static com.ibetter.www.adskitedigi.adskitedigi.model.NotificationModelConstants.createChannel;

public class MetricsService extends Service implements CameraServiceResultReceiver.CallBack {

    private final static String NOTIFY_CHANNEL_ID = "NOTIFY_CHANNEL_ID";
    private final static String NOTIFY_CHANNEL_NAME = "NOTIFY_CHANNEL_NAME";
    private final static String NOTIFY_CHANNEL_DES = "Image Capturing";
    private String TAG="MetricsService";
    private static Context context;
    public static CameraServiceResultReceiver cameraServiceResultReceiver;
    public static boolean isServiceOn = false;
    public static boolean isActiveCaptureImg=true;

    private File imageFile;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    @Override
    public void onCreate()
    {
        super.onCreate();
        context= MetricsService.this;
        isServiceOn=true;

        initRx();
        checkAndStartForegroundNotification();
        Log.i(TAG, "MetricsService onCreate:");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfiguration)
    {
        super.onConfigurationChanged(newConfiguration);
        adjustCameraRotation();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try
        {
            if(IOTDevice.isIOTDeviceRegistered(context))
            {
                mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                setLayoutParams();
                faceRegInit();
            }else
            {
                Toast.makeText(context, "Player not registered, Please register and try again.", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"Player not registered");
                finishService();
            }

        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            finishService();
        }

        return START_STICKY;
    }

    private void setLayoutParams()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);

        } else
            {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);
        }

        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.width =150;
        params.height = 150;
        // params.x = left;
        // params.y = top;
    }

    private void faceRegInit()
    {
        textureView=new TextureView(getApplicationContext());

        mWindowManager.addView(textureView, params);
        textureView.setSurfaceTextureListener(textureListener);

    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here

            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };


    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        if(mBackgroundThread!=null)
        {
            mBackgroundThread.quitSafely();
            // mBackgroundThread.quit();
            try {

                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(context, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera()
    {
       CameraManager  manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            try
            {
                cameraId = manager.getCameraIdList()[1];

            }catch (Exception e)
            {
                e.printStackTrace();
              //  cameraId = manager.getCameraIdList()[0];
            }

            if(cameraId==null)
            {
                cameraId = manager.getCameraIdList()[0];
            }

            adjustCameraRotation();

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);


            /*Rational controlAECompensationStep = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
            if (controlAECompensationStep != null) {
               double compensationStep = controlAECompensationStep.doubleValue();
            }

            Range<Integer> controlAECompensationRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            if (controlAECompensationRange != null) {
                minCompensationRange = controlAECompensationRange.getLower();
                maxCompensationRange = controlAECompensationRange.getUpper();
            }*/



            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // Check for the camera permission before accessing the camera.  If the
            // permission is not granted yet, request permission.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                manager.openCamera(cameraId, stateCallback, null);
            }else
            {
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(context, "Failed to start camera preview because it couldn't access camera permission", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(context, "Failed to start camera preview because it couldn't access read external storage permission", Toast.LENGTH_SHORT).show();
                }
                finishService();
            }
        } catch (CameraAccessException e)
        {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "openCamera X");
    }


    protected void updatePreview()
    {
        if (null == cameraDevice)
        {
            Log.e(TAG, "updatePreview error, return");
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        // Auto focus should be continuous for camera preview.
        captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,getRange());
         captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 6);

       /* try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), captureCallbackListener, mBackgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to start camera preview because it couldn't access camera", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to start camera preview.", e);
        }

       */ new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            try {
                cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), captureCallbackListener, mBackgroundHandler);

            } catch (CameraAccessException e)
            {
                e.printStackTrace();
                Toast.makeText(context, "Failed to start camera preview because it couldn't access camera.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to start camera preview because it couldn't access camera", e);
            } catch (IllegalStateException e)
            {
                e.printStackTrace();
                Toast.makeText(context, "Failed to start camera preview.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to start camera preview.", e);
            }
        }
    }, 500);

    }


    private void adjustCameraRotation()
    {
        if (textureView == null)
        {
            return;

        } else  {
            try {

                Matrix matrix = new Matrix();
                int rotation = mWindowManager.getDefaultDisplay().getRotation();
                RectF textureRectF = new RectF(0, 0, 150, 150);
                RectF previewRectF = new RectF(0, 0, textureView.getHeight(), textureView.getWidth());
                float centerX = textureRectF.centerX();
                float centerY = textureRectF.centerY();
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                    previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
                    matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
                    float scale = Math.max((float) 150 / 150, (float) 150 / 150);
                    matrix.postScale(scale, scale, centerX, centerY);
                    matrix.postRotate(90 * (rotation - 2), centerX, centerY);
                }
                textureView.setTransform(matrix);
            }
         catch (Exception e)
         {
            e.printStackTrace();
         }
        }
    }


    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback()
    {
        private void process(CaptureResult result)
        {
            Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
            Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
            if (faces.length>0 && mode != null)
            {
                try
                {
                    if(isActiveCaptureImg)
                    {
                        //Toast.makeText(MainActivity.this, "faces detected:"+faces.length, Toast.LENGTH_SHORT).show();
                        //cameraCaptureSessions.stopRepeating();
                        isActiveCaptureImg=false;
                        takePicture();
                    }

                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MetricsService.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
                // Log.e(TAG, "faces : " + faces.length + " , isActiveCaptureImg : " + isActiveCaptureImg);
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            // default empty implementation
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result)
        {
            process(result);
        }

    };


    protected void takePicture()
    {
        Log.i(TAG, "takePicture:"+isActiveCaptureImg);
        if (null == cameraDevice)
        {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;

            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 480;//640
            int height = 320;//480
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.JPEG_QUALITY,(byte)40);
            captureBuilder.set(CaptureRequest.JPEG_THUMBNAIL_QUALITY, (byte) 40);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            //captureBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 6);
           captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,getRange());
            // Orientation
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int deviceRotation =mWindowManager.getDefaultDisplay().getRotation();
            int surfaceRotation = ORIENTATIONS.get(deviceRotation);
            int jpegOrientation =getJpegOrientation(characteristics,deviceRotation);
            //(surfaceRotation + sensorOrientation + 270) % 360;

            Log.i(TAG,"sensorOrientation:"+sensorOrientation);
            Log.i(TAG,"deviceRotation:"+deviceRotation);
            Log.i(TAG,"surfaceRotation:"+surfaceRotation);
            Log.i(TAG,"jpegOrientation:"+jpegOrientation);

            //captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

            String path =new DownloadMediaHelper().getCaptureImagesDirectory(context) + "/Imag" + Calendar.getInstance().getTimeInMillis() + ".jpg";
            imageFile = new File(path);
            if (!imageFile.exists())
            {
                try
                {
                    imageFile.createNewFile();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }


            };


            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)
                {
                    super.onCaptureCompleted(session, request, result);

                    Log.i(TAG,"Image Saved:"+imageFile.getPath());
                   // Toast.makeText(context, "Captured:" + imageFile.getPath(), Toast.LENGTH_SHORT).show();

                    if(imageFile!=null && imageFile.exists())
                    {
                        uploadMetricsFile();
                    }else
                    {
                        deleteCapturedImgFile();
                        reCaptureFrame();
                    }
                    if(mBackgroundThread==null)
                    {
                        startBackgroundThread();
                    }
                    if (textureView.isAvailable())
                    {
                        openCamera();
                    } else
                    {
                        textureView.setSurfaceTextureListener(textureListener);
                    }


                    //createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {

                    if (null == cameraDevice)
                    {
                        return;
                    }
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void reCaptureFrame()
    {
        Handler handler=new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run() {

                try {
                    isActiveCaptureImg=true;

                } catch (Exception e) {
                    Toast.makeText(MetricsService.this, e.toString(), Toast.LENGTH_SHORT).show();

                }

            }
        }, 4000);


    }

    private void save(byte[] bytes) throws IOException
    {
        OutputStream output = null;
        try {
            output = new FileOutputStream(imageFile);
            output.write(bytes);

        } finally {
            if (null != output)
            {
                output.close();
                //setCameraPicOrientation();
            }
        }
    }

    private void closeCamera()
    {
        try
        {
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader)
            {
                imageReader.close();
                imageReader = null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private Range<Integer> getRange()
    {
        CameraCharacteristics chars = null;
        try {
            CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            chars = mCameraManager.getCameraCharacteristics(cameraId);
            Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Range<Integer> result = null;
            for (Range<Integer> range : ranges) {
                int upper = range.getUpper();
                // 10 - min range upper for my needs
                if (upper >= 10) {
                    if (result == null || upper < result.getUpper().intValue()) {
                        result = range;
                    }
                }
            }
            if (result == null) {
                result = ranges[0];
            }
            return result;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation)
    {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }


    private void setCameraPicOrientation()
    {
        String mCurrentPhotoPath= imageFile.getAbsolutePath();
        int rotate = 0;
        try {
            // File imageFile = new File(mCurrentPhotoPath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);

        /* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getPath(), bmOptions);
        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        /* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private void checkAndStartForegroundNotification()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            startForeground(CAPTURE_IMAGE_NOTIFY_ID, setNotification("Capturing image"));
        }
    }

    //create campaign upload success notification
    public static Notification setNotification(String title)
    {
        //create notification channel
        createChannel(context, NOTIFY_CHANNEL_ID, NOTIFY_CHANNEL_NAME,
                NOTIFY_CHANNEL_DES, NotificationManager.IMPORTANCE_MIN);

        return new NotificationCompat.Builder(context, NOTIFY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private  void uploadMetricsFile()
    {
        isActiveCaptureImg=false;
        //start service to download campaign supported files
        Intent intent = new Intent(context, UploadMetricsFileService.class);
        intent.putExtra("file_path",imageFile.getPath());
        startService(intent);
    }


    private void initRx()
    {
        cameraServiceResultReceiver = new CameraServiceResultReceiver(new Handler(), MetricsService.this, this);
    }

    @Override
    public void stopService(Bundle values)
    {
        finishService();
    }

    @Override
    public void uploadMetricsFileServiceResponse(Bundle resultData) {

        if(resultData!=null)
        {
            boolean flag=resultData.getBoolean("flag",false);

            if(flag) {

                successResponse("User metrics captured");
                //need to delete file

            }else
            {
                failureResponse("No metrics captured");
            }

        }else
        {
            failureResponse("Unable to capture user metrics");

        }

    }


    private void successResponse(String successMsG)
    {
        // Toast.makeText(getApplicationContext(), successMsG, Toast.LENGTH_SHORT).show();
        Log.i(TAG,"successResponse"+"/n"+successMsG);
        deleteCapturedImgFile();
        reCaptureFrame();
    }

    private void failureResponse(String errMsg)
    {
        //Toast.makeText(context,errMsg,Toast.LENGTH_LONG).show();
        Log.i(TAG,"failureResponse"+errMsg);
        deleteCapturedImgFile();
        reCaptureFrame();
    }


    private void finishService()
    {
        stopSelf();
        stopForeground(true);
    }

    private void deleteCapturedImgFile()
    {
        //delete the file
        try
        {
            File file=new File(imageFile.getPath());
            if(file.exists())
            {
                 file.delete();
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy()
    {
        //mCamera.release();
        super.onDestroy();
        //stopForeground(true);
        isServiceOn=false;
        isActiveCaptureImg=false;

        closeCamera();

        stopBackgroundThread();

        if(textureView!=null)
        {
            mWindowManager.removeView(textureView);
            mWindowManager=null;
        }

        Log.i(TAG,"CameraService:onDestroy");

    }

}
