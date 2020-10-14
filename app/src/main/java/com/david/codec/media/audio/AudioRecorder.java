/*
 *
 * YooRecorder.java
 *
 * Created by Wuwang on 2016/12/31
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.david.codec.media.audio;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.david.codec.media.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by David on 2020/7/29
 * 进行AAC编码
 */
public class AudioRecorder {

    public static final String TAG = "AudioRecorder";
    private final Object LOCK = new Object();
    private String audioMime = "audio/mp4a-latm";   //音频编码的Mime
    private AudioRecord mRecorder;   //录音器
    private MediaCodec mAudioEnc;   //编码器，用于音频编码
    private AudioInfo mAudioInfo;   //录制参数
    private boolean isEncoding;
    private int bufferSize;
    private Thread mAudioThread;
    private long nanoTime;
    private OnAACListener mOnAACListener;
    private String fileName;//录音保存位置
    private boolean isRecording;//是否录制文件

    public void setOnAACListener(OnAACListener onAACListener) {
        mOnAACListener = onAACListener;
    }

    public AudioRecorder(AudioInfo info) {
        mAudioInfo=info;
    }

    //进行编解码器的准备
    public void prepare() throws IOException {
        //准备Audio
        initAudioCodec();
        initAudioRecorder();
    }

    private void initAudioRecorder() {
        bufferSize = AudioRecord.getMinBufferSize(mAudioInfo.getSampleRate(), mAudioInfo.getChannelConfig(), mAudioInfo.getSampleFormat()) * 2;
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, mAudioInfo.getSampleRate(),  mAudioInfo.getChannelConfig(),
                mAudioInfo.getSampleFormat(), bufferSize);
    }

    private void initAudioCodec() throws IOException {
        MediaFormat format = MediaFormat.createAudioFormat(audioMime, mAudioInfo.getSampleRate(), mAudioInfo.getChannelCount());
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mAudioInfo.getBitrate());
        mAudioEnc = MediaCodec.createEncoderByType(audioMime);
        mAudioEnc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public int start() throws InterruptedException, IOException {
        //记录起始时间
        prepare();
        nanoTime = System.nanoTime();
        synchronized (LOCK) {
            //Audio Start
            if (mAudioThread != null && mAudioThread.isAlive()) {
                isEncoding = false;
                mAudioThread.join();
            }

            mAudioEnc.start();
            mRecorder.startRecording();
            isEncoding = true;
            mAudioThread = new Thread(new AudioRecordTask());
            mAudioThread.start();
        }
        return 0;
    }

    private class AudioRecordTask implements Runnable {

        @Override
        public void run() {
            while (isEncoding) {
                try {
                    audioStep();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void cancel() {
        stop();
        File file=new File(FileUtils.AAC_FOLDER+fileName);
        if (file.exists()){
            file.delete();
        }
    }

    public void stop() {
        try {
            synchronized (LOCK) {
                isRecording = false;
                isEncoding=false;
                mAudioThread.join();
                //Audio Stop
                mRecorder.stop();
                mAudioEnc.stop();
                mAudioEnc.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    //硬编码音频数据
    private void audioStep() throws IOException {
        int index = mAudioEnc.dequeueInputBuffer(-1);
        if (index >= 0) {
            final ByteBuffer buffer = getInputBuffer(mAudioEnc, index);
            buffer.clear();
            int length = mRecorder.read(buffer, bufferSize);
            if (length > 0) {
                mAudioEnc.queueInputBuffer(index, 0, length, (System.nanoTime() - nanoTime) / 1000, 0);
            } else {
                Log.i(TAG, "audioStep: 音频数据有误");
            }
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex;
        do {
            outIndex = mAudioEnc.dequeueOutputBuffer(bufferInfo, 1000);

            if (outIndex >= 0) {
                ByteBuffer buffer = getOutputBuffer(mAudioEnc, outIndex);
                buffer.position(bufferInfo.offset);
                if (mOnAACListener != null) {
                    mOnAACListener.onAudioData(buffer, bufferInfo);
                }
                //添加AAC头部
                byte[] temp = new byte[bufferInfo.size + 7];
                buffer.get(temp, 7, bufferInfo.size);
                addADTStoPacket(temp, temp.length);
                if (isRecording) {
                    FileUtils.writeData(temp, FileUtils.AAC_FOLDER, fileName);
                }
                mAudioEnc.releaseOutputBuffer(outIndex, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.e(TAG, "audio end");
                }
            } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //可以添加音频轨和视频轨
                Log.i(TAG, "audioStep: 开始编解码");
                if (mOnAACListener != null) {
                    mOnAACListener.onAudioFormat(mAudioEnc.getOutputFormat());
                }
            }
        } while (outIndex >= 0);
    }

    public void startRecord(String name) {
        isRecording=true;
        fileName = name;
        try {
            start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给编码出的aac裸流添加adts头字段
     *
     * @param packet    要空出前7个字节，否则会搞乱数据
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public interface OnAACListener {
        void onAudioData(ByteBuffer buffer, MediaCodec.BufferInfo info);

        void onAudioFormat(MediaFormat mediaFormat);
    }
}
