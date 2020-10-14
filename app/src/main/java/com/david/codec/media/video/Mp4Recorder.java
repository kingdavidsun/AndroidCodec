/*
 *
 * YooRecorder.java
 *
 * Created by Wuwang on 2016/12/31
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.david.codec.media.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import com.david.codec.media.audio.AudioInfo;
import com.david.codec.media.audio.AudioRecorder;
import com.david.codec.media.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by David on 2020/7/29
 * 录制mp4
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Mp4Recorder implements H264Recorder.OnH264Listener, AudioRecorder.OnAACListener {

    public static final String TAG = "Mp4Recorder";
    private final Object LOCK = new Object();

    private MediaMuxer mMuxer;  //音视频混合器
    private H264Recorder mH264Recorder;//h264数据获取
    private AudioRecorder mAudioRecorder;//aac数据获取
    private String mp4Name;//文件名
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;

    public Mp4Recorder(VideoInfo videoInfo, AudioInfo audioInfo) {

        mH264Recorder = new H264Recorder(videoInfo);
        mAudioRecorder = new AudioRecorder(audioInfo);
        File file = new File(FileUtils.MP4_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
            Log.i(TAG, "Mp4Recorder: 生成文件夹成功");
        }

    }

    public void prepare() {
        mH264Recorder.setOnH264Listener(this);
        mAudioRecorder.setOnAACListener(this);
    }

    public void fillData(byte[] data) {
        mH264Recorder.feedData(data);
    }

    public int startRecord(String fileName) {
        mp4Name = fileName;

        try {
            File mp4=new File(FileUtils.MP4_FOLDER+mp4Name);
            if (!mp4.exists()){
                mp4.createNewFile();
            }
            mMuxer = new MediaMuxer(FileUtils.MP4_FOLDER+mp4Name, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            prepare();
            mH264Recorder.start();
            mAudioRecorder.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //取消保存
    public void cancel() {
        stop();
        File file = new File(FileUtils.MP4_FOLDER + mp4Name);
        if (file.exists()) {
            file.delete();
        }
    }

    //停止录制
    public void stop() {
        try {
            synchronized (LOCK) {
                mAudioRecorder.stop();
                mH264Recorder.stop();
                //Muxer Stop
                mVideoTrack = -1;
                mAudioTrack = -1;
                mMuxer.stop();
                mMuxer.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onH264Data(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (mAudioTrack >= 0 && mVideoTrack >= 0) {
            mMuxer.writeSampleData(mVideoTrack, buffer, info);
        }
    }

    @Override
    public void onH264Format(MediaFormat mediaFormat) {
        if (mMuxer != null) {
            //添加视频轨道
            mVideoTrack = mMuxer.addTrack(mediaFormat);
        }
        if (mAudioTrack >= 0 && mVideoTrack >= 0) {
            mMuxer.start();
        }
    }

    @Override
    public void onAudioData(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        //写入音频
        if (mAudioTrack >= 0 && mVideoTrack >= 0) {
            mMuxer.writeSampleData(mAudioTrack, buffer, info);
        }
    }

    @Override
    public void onAudioFormat(MediaFormat mediaFormat) {
        if (mMuxer != null) {
            //添加音频轨道
            mAudioTrack = mMuxer.addTrack(mediaFormat);
        }
        if (mAudioTrack >= 0 && mVideoTrack >= 0) {
            mMuxer.start();
        }
    }
}
