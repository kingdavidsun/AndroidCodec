package com.david.codec.camera2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.david.codec.R;
import com.david.codec.media.video.H264Recorder;
import com.david.codec.media.video.VideoInfo;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Camera2RecordActivity extends AppCompatActivity implements Camera2Util.OnPreviewListener {
    private static final String TAG = "david";
    private SurfaceView mSurfaceView;
    private Button mBtnRecord;
    private Camera2Util mCamera2Util;
    private H264Recorder mH264Recorder;
    private boolean isRecording;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_record);
        initView();
    }
    private void initView() {
        mSurfaceView=findViewById(R.id.surface_view);
        //自定义view的高度
        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                int orientation = Camera2RecordActivity.this.getResources().getConfiguration().orientation;
                if (orientation== Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏宽高为：3:4
                    int measuredWidth = mSurfaceView.getMeasuredWidth();
                    ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
                    layoutParams.height = measuredWidth * 4 / 3;
                    mSurfaceView.setLayoutParams(layoutParams);
                }else{

                }
            }
        });
        mBtnRecord=findViewById(R.id.btn_record);
        mCamera2Util=new Camera2Util(this,mSurfaceView);
        mH264Recorder=new H264Recorder(new VideoInfo(720,1280,25));
        mCamera2Util.setOnPreviewListener(this);
        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording=!isRecording){
                    mBtnRecord.setText("停止");
                    mH264Recorder.startRecord(System.currentTimeMillis()+"test.h264");
                }else{
                    mH264Recorder.stop();
                    mBtnRecord.setText("开始");
                }
            }
        });
    }

    @Override
    public void onPreView(byte[] data) {
        //处理yuv数据
        if (mH264Recorder!=null){
            mH264Recorder.feedData(data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera2Util.releaseCamera();
    }
}
