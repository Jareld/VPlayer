package com.example.lyc.vrexplayer.view;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.lyc.vrexplayer.Utils.CameraUtil;
import com.example.lyc.vrexplayer.Utils.LogUtils;
import com.example.lyc.vrexplayer.service.CameraDataTrandsferService;
import com.example.lyc.vrexplayer.task.FileServerAsyncTask;

import java.io.IOException;
import java.util.List;

public class CameraPreview
        extends SurfaceView
        implements SurfaceHolder.Callback
{

    private static final String TAG = "CP";

    private SurfaceHolder       mHolder;
    private Camera              mCamera;
    private FileServerAsyncTask mFileServerAsyncTask;
    private Camera.Size         mPreviewSize;
    private int                 mFrameCount;

    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera, FileServerAsyncTask fileServerAsyncTask) {
        super(context);
        mFrameCount = 0;
        mCamera = camera;
        mFileServerAsyncTask = fileServerAsyncTask;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }


    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        try {
            mCamera.setPreviewDisplay(holder);
            // mCamera.setPreviewCallback( new PreviewCallback(){
            // @Override
            // public void onPreviewFrame(byte[] data, Camera camera) {
            // Log.d(CP, "Preview Callback in CameraPreview.java");
            // }
            // });
            //	mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Parameters param = mCamera.getParameters();
        param.setPreviewSize(CameraUtil.CAMERA_WIDTH, CameraUtil.CAMERA_HEIGHT);
        param.setPreviewFormat(ImageFormat.YV12);

        List<Integer> lislis = param.getSupportedPreviewFormats();

        List<Camera.Size> supportedPreviewSizes = param.getSupportedPreviewSizes();

        LogUtils.logInfo(TAG, "HY", "看一下这边获取到的预览的size" + lislis + "::" + supportedPreviewSizes);
        for (Camera.Size size : supportedPreviewSizes) {
            LogUtils.logInfo(TAG, "HY", "看一下支持的尺寸都有多大" + size.width + "::" + size.height);
        }

        List<int[]> supportedPreviewFpsRange = param.getSupportedPreviewFpsRange();
        for (int[] range : supportedPreviewFpsRange) {
            LogUtils.logInfo(TAG, "看一下支持的FPS", range[0] + "::" + range[1]);
        }
        int[] muqianrangr=new int[2];
        param.getPreviewFpsRange(muqianrangr);
        LogUtils.logInfo(TAG,"看一下目前的",muqianrangr[0]+"::"+muqianrangr[1]);
         param.setPreviewFpsRange(30000,30000);
     //   param.setPreviewFrameRate(30000);
        //param.setPreviewFormat(ImageFormat.JPEG);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(param);

        //int form = param.getPreviewFormat();
        mPreviewSize = mCamera.getParameters()
                              .getPreviewSize();
      //  int previewFormat = mCamera.getParameters().getPreviewFormat();
//        int size = mPreviewSize.width * mPreviewSize.height
//                * ImageFormat.getBitsPerPixel(previewFormat)
//                / 8;
//        mCamera.addCallbackBuffer(new byte[size]);
        CameraDataTrandsferService.setPreviewSize(mPreviewSize);

        // start preview with new settings
        try {

            mCamera.setPreviewDisplay(mHolder);

//            mCamera.setOneShotPreviewCallback(new PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//                    LogUtils.logInfo(TAG,"看看这个方法的回调setOneShotPreviewCallback","changdu"+data.length);
//                }
//            });

//            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//
//                    LogUtils.logInfo(TAG,"看看这个方法的回调","changdu"+data.length);
//                }
//            });

            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
//                    mCamera.addCallbackBuffer(data);
                   Log.d(TAG, "看看这Preview Callback in CameraPreview.java. mFrameCount = " + mFrameCount+"::"+data.length);
                   mFrameCount++;
                 //   mFrameCount++;

                   CameraDataTrandsferService.tranfeData(data);

                     mCamera.addCallbackBuffer(data);

                    //	CameraDataTrandsferService.tranfeData(data);
                }
            });
            //	mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

}
