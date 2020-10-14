package com.david.codec.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.david.codec.R;
import com.david.codec.media.audio.AudioInfo;
import com.david.codec.media.audio.AudioRecorder;
import com.david.codec.media.util.CameraUtil;
import com.david.codec.media.video.Mp4Recorder;
import com.david.codec.media.video.VideoInfo;

/**
 * 硬编码aac示例
 */
public class AACActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private CameraUtil mCameraUtil;
    private AudioRecorder mAudioRecorder;
    private boolean isRecording;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mCameraUtil=new CameraUtil();
        mAudioRecorder=new AudioRecorder(new AudioInfo());
    }

    private void initView() {
        mSurfaceView=findViewById(R.id.surface_view);
        mHolder =mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCameraUtil.startPreviewCreate(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCameraUtil.startPreviewChange(mHolder,this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraUtil.stopPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //必须调用该方法
        mCameraUtil.addCallbackBuffer(data);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        mCameraUtil.releaseCamera();

    }

    public void startRecord(View view) {

        Button button= (Button) view;
        if (isRecording=!isRecording){
            button.setText("停止");
            mAudioRecorder.startRecord(System.currentTimeMillis()+"test.aac");
        }else{
            mAudioRecorder.stop();
            button.setText("开始");
        }
    }


}
