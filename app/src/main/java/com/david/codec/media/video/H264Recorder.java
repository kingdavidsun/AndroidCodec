/*
 *
 * YooRecorder.java
 *
 * Created by Wuwang on 2016/12/31
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.david.codec.media.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.david.codec.media.util.CodecUtil;
import com.david.codec.media.util.FileUtils;
import com.david.codec.media.util.YUVUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by David on 2020/7/29
 * 进行h264编码
 */
public class H264Recorder {

    public static final String TAG = "H264Recorder";
    private final Object LOCK = new Object();

    private MediaCodec mVideoEnc;
    private String videoMime = "video/avc";   //视频编码格式
//    private String videoMime = "video/hevc";   //视频编码格式
    private VideoInfo mVideoInfo;//视频编码属性
    private int fpsTime;
    private Thread mVideoThread;
    private int width;
    private int height;
    private byte[] nowFeedData;//当前需要编码的数据
    private boolean hasNewData = false;//是否有新数据
    private long nanoTime;
    private boolean isRecording;//是否录制h264文件
    private boolean isEncoding;//是否编码
    private int colorFormat;
    public H264Recorder(VideoInfo videoInfo) {
        fpsTime = 1000 / videoInfo.getFrameRate();
        mVideoInfo = videoInfo;
    }

    private OnH264Listener mOnH264Listener;

    //设置h264数据回调监听
    public void setOnH264Listener(OnH264Listener onH264Listener) {
        mOnH264Listener = onH264Listener;
    }

    public int prepare() throws IOException {

        //准备Video
        this.width = mVideoInfo.getWidth();
        this.height = mVideoInfo.getHeight();
        if (CodecUtil.isH265EncoderSupport()){
            videoMime="video/hevc";
        }else{
            videoMime="video/avc";
        }
        Log.i(TAG, "videoMime: "+videoMime);
        MediaFormat videoFormat = MediaFormat.createVideoFormat(videoMime, width, height);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mVideoInfo.getVideoRate());
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mVideoInfo.getFrameRate());
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mVideoInfo.getFrameInterval());
        colorFormat = checkColorFormat(videoMime);
        Log.i(TAG, ": "+colorFormat);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mVideoEnc = MediaCodec.createEncoderByType(videoMime);
        mVideoEnc.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Bundle bundle = new Bundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, mVideoInfo.getVideoRate());
            mVideoEnc.setParameters(bundle);
        }
        return 0;
    }

    public void start() throws InterruptedException, IOException {
        //记录起始时间
        prepare();
        nanoTime = System.nanoTime();
        synchronized (LOCK) {

            if (mVideoThread != null && mVideoThread.isAlive()) {
                isEncoding = false;
                mVideoThread.join();
            }
            isEncoding=true;
            mVideoEnc.start();
            mVideoThread = new Thread(new VideoRecordThread());
            mVideoThread.start();
        }

    }

    public void startRecord(String fileName) {
        this.fileName = fileName;
        isRecording=true;
        try {
            start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class VideoRecordThread implements Runnable {
        public void run() {
            while (isEncoding) {
                long time = System.currentTimeMillis();
                if (nowFeedData != null) {
                    try {
                        videoStep(nowFeedData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                long lt = System.currentTimeMillis() - time;
                if (fpsTime > lt) {
                    try {
                        Thread.sleep(fpsTime - lt);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void stop() {
        try {
            synchronized (LOCK) {
                isRecording = false;
                isEncoding = false;
                mVideoThread.join();
                //Video Stop
                mVideoEnc.stop();
                mVideoEnc.release();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 由外部喂入一帧数据
     *
     * @param data nv21数据
     */
    public void feedData(final byte[] data) {
        hasNewData = true;
        nowFeedData = data;

    }

    private ByteBuffer getInputBuffer(MediaCodec codec, int index) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getInputBuffer(index);
        } else {
            return codec.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(MediaCodec codec, int index) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(index);
        } else {
            return codec.getOutputBuffers()[index];
        }
    }

    private String fileName;

    //录制h264文件到本地
    public void startH264Record(String name) {
        fileName = name;
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private byte[] yuv;

    // 定时调用，如果没有新数据，就用上一个数据
    private void videoStep(byte[] data) throws IOException {
        int index = mVideoEnc.dequeueInputBuffer(-1);
        if (index >= 0) {
            if (hasNewData) {
                if (yuv == null) {
                    yuv = new byte[width * height * 3 / 2];
                }
                if (colorFormat==19) {
                    YUVUtil.NV21ToPlanar(data, yuv, width, height);
                }else{
                    YUVUtil.NV21ToNV12(data, yuv, width, height);
                }
            }
            ByteBuffer buffer = getInputBuffer(mVideoEnc, index);
            buffer.clear();
            buffer.put(yuv);
            mVideoEnc.queueInputBuffer(index, 0, yuv.length, (System.nanoTime() - nanoTime) / 1000, 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex = mVideoEnc.dequeueOutputBuffer(bufferInfo, 0);
        do {
            if (outIndex >= 0) {
                ByteBuffer outBuf = getOutputBuffer(mVideoEnc, outIndex);
                // 保存文件
                byte[] outData = new byte[bufferInfo.size];
                outBuf.get(outData);
                if (mOnH264Listener != null) {
                    mOnH264Listener.onH264Data(outBuf, bufferInfo);
                }
                if (isRecording) {
                    FileUtils.writeData(outData, FileUtils.H264_FOLDER, fileName);
                }
                mVideoEnc.releaseOutputBuffer(outIndex, false);
                outIndex = mVideoEnc.dequeueOutputBuffer(bufferInfo, 0);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.e(TAG, "video end");
                }
            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mOnH264Listener != null) {
                    mOnH264Listener.onH264Format(mVideoEnc.getOutputFormat());
                }
            }
        } while (outIndex >= 0);
    }

    private int checkColorFormat(String mime) {
        if (Build.MODEL.equals("HUAWEI P6-C00")) {
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        }
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (info.isEncoder()) {
                String[] types = info.getSupportedTypes();
                for (String type : types) {
                    if (type.equals(mime)) {
                        Log.e("YUV", "type-->" + type);
                        MediaCodecInfo.CodecCapabilities c = info.getCapabilitiesForType(type);
                        Log.e("YUV", "color-->" + Arrays.toString(c.colorFormats));
                        for (int j = 0; j < c.colorFormats.length; j++) {
                            if (c.colorFormats[j] == MediaCodecInfo.CodecCapabilities
                                    .COLOR_FormatYUV420Planar) {

                                return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
                            } else if (c.colorFormats[j] == MediaCodecInfo.CodecCapabilities
                                    .COLOR_FormatYUV420SemiPlanar) {

                                return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
                            }
                        }
                    }
                }
            }
        }
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
    }

    public interface OnH264Listener {
        void onH264Data(ByteBuffer buffer, MediaCodec.BufferInfo info);

        void onH264Format(MediaFormat mediaFormat);
    }
}
