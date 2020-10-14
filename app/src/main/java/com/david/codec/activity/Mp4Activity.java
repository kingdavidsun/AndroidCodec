package com.david.codec.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.david.codec.R;
import com.david.codec.media.audio.AudioInfo;
import com.david.codec.media.util.CameraUtil;
import com.david.codec.media.video.Mp4Recorder;
import com.david.codec.media.video.VideoInfo;

/**
 * 合成mp4示例
 */
public class Mp4Activity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private CameraUtil mCameraUtil;
    private Mp4Recorder mMp4Recorder;
    private boolean isMp4Recording;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mCameraUtil=new CameraUtil();
        mMp4Recorder=new Mp4Recorder(new VideoInfo(),new AudioInfo());
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

        if (data==null){
            return;
        }
        //必须调用该方法
        if (mCameraUtil!=null) {
            mCameraUtil.addCallbackBuffer(data);
        }
        //处理yuv数据
        if (mMp4Recorder!=null){
            mMp4Recorder.fillData(data);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        mCameraUtil.releaseCamera();

    }

    public void startRecord(View view) {

        Button button= (Button) view;
        if (isMp4Recording=!isMp4Recording){
            button.setText("停止");
            mMp4Recorder.startRecord(System.currentTimeMillis()+"test.mp4");
        }else{
            mMp4Recorder.stop();
            button.setText("开始");
        }
    }


}
