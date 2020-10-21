package com.david.codec.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.david.codec.media.util.YUVUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.david.codec.media.util.YUVUtil.COLOR_FormatNV21;

/**
 * Created by David on 2020/10/19
 */
public class Camera2Util implements SurfaceHolder.Callback {
    private static final String TAG = "Camera2Util";
    // camera相关对象
    private int mCameraId = CameraCharacteristics.LENS_FACING_FRONT; // 要打开的摄像头ID
    private Size mPreviewSize = new Size(1280, 720); // 固定640*480演示
    CameraManager mCameraManager;
    private CameraDevice mCameraDevice; // 相机对象
    private CameraCaptureSession mCaptureSession;
    // handler
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    // output
    private Surface mPreviewSurface; // 输出到屏幕的预览
    private ImageReader mImageReader; // 预览回调的接收者
    private Context mContext;
    private SurfaceView mSurfaceView;
    //预览回调
    private OnPreviewListener mOnPreviewListener;
    public Camera2Util(Context context, SurfaceView surfaceView) {
        mContext = context;
        mSurfaceView = surfaceView;
        SurfaceHolder holder = mSurfaceView.getHolder();
        mPreviewSurface=holder.getSurface();
        holder.addCallback(this);
    }
    @SuppressLint("MissingPermission")
    private void openCamera(){

        try {
            //初始化handler
            if (mBackgroundThread == null || mBackgroundHandler == null) {
                Log.v(TAG, "startBackgroundThread");
                mBackgroundThread = new HandlerThread("CameraBackground");
                mBackgroundThread.start();
                mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
            }
            mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            Log.d(TAG, "preview size: " + mPreviewSize.getWidth() + "*" + mPreviewSize.getHeight());
            //创建ImageReader获取YUV数据
            mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                    ImageFormat.YUV_420_888, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
            // 打开摄像头
            mCameraManager.openCamera(Integer.toString(mCameraId),mStateCallback,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated: ");
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: ");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed: ");
        releaseCamera();
    }

    /**
     * 打开摄像头的回调
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened");
            mCameraDevice = camera;
            initPreviewRequest();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "onDisconnected");
            releaseCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "Camera Open failed, error: " + error);
            releaseCamera();
        }
    };
    //预览回调
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener=new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            byte[] dataByte = YUVUtil.getDataFromImage(image,COLOR_FormatNV21);
//            Log.i(TAG, "onImageAvailable: "+dataByte.length);
            //旋转90度
            byte[] rotateData=new byte[dataByte.length];
            YUVUtil.rotateSP90(dataByte,rotateData,1280,720);
            if (mOnPreviewListener!=null){

                mOnPreviewListener.onPreView(rotateData);
            }
            // 一定不能忘记close
            image.close();
        }
    };


    //当相机打开后，开启预览
    private void initPreviewRequest() {
        try {
            final CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 添加输出到屏幕的surface
            builder.addTarget(mPreviewSurface);
//            // 添加输出到ImageReader的surface。然后我们就可以从ImageReader中获取预览数据了
            builder.addTarget(mImageReader.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            // 设置连续自动对焦和自动曝光
                            builder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            builder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                            CaptureRequest captureRequest = builder.build();
                            try {
                                // 一直发送预览请求
                                Log.i(TAG, "onConfigured: 开始连续预览");
                                mCaptureSession.setRepeatingRequest(captureRequest, null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "ConfigureFailed. session: mCaptureSession");
                        }
                    }, mBackgroundHandler); // handle 传入 null 表示使用当前线程的 Looper
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    public void releaseCamera() {
        Log.v(TAG, "releaseCamera");
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        Log.v(TAG, "stopBackgroundThread");
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //设置预览数据回调，转为nv21
    public void setOnPreviewListener(OnPreviewListener onPreviewListener) {
        mOnPreviewListener = onPreviewListener;
    }

    public interface OnPreviewListener{
        void onPreView(byte[]data);
    }
}
