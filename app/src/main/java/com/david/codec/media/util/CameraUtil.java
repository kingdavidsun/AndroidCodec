package com.david.codec.media.util;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by David on 2020/9/1
 */
public class CameraUtil {
    private static final String TAG = "CameraUtil";
    private Camera mCamera;
    private int mWidth = 1280;//预览宽
    private int mHeight = 720;//预览高
    private int mFrameRate = 15;//帧率
    //打开相机
    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCamera();
            mCamera = Camera.open(id);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFrameRate(mFrameRate);
            parameters.setPreviewSize(mWidth, mHeight);
            parameters.setPreviewFormat(ImageFormat.NV21);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(0);//根据实际情况适配
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e("CameraUtil", "failed to open Camera:"+e.getMessage());
            e.printStackTrace();
        }

        return qOpened;
    }
    //在onDestroy中调用
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    //在surfaceDestroyed中调用
    public void stopPreview(){
        if (mCamera!=null){
            mCamera.stopPreview();
        }
    }
    //surfaceCreated中调用
    public void startPreviewCreate(SurfaceHolder holder){
        boolean cameraOpen = safeCameraOpen(0);
        if (cameraOpen) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void startPreviewChange(SurfaceHolder holder,Camera.PreviewCallback callback){
// stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // start preview with new settings
        try {
//            int orientation = getResources().getConfiguration().orientation;
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallbackWithBuffer(callback);
            byte[] bytes = new byte[mWidth * mHeight * 3 / 2];
            mCamera.addCallbackBuffer(bytes);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    //通过withbuffer回调的，必须调用这个方法，否则后续不会进入回调
    public void addCallbackBuffer(byte[]data){
        if (mCamera!=null) {
            mCamera.addCallbackBuffer(data);
        }
    }
}
