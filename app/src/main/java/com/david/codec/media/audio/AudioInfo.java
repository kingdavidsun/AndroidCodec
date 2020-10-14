package com.david.codec.media.audio;

import android.media.AudioFormat;

/**
 * Created by David on 2020/7/29
 * 音频编码信息
 */
public class AudioInfo {
    public enum Channel {
        SINGLE,//单声道
        DOUBLE//双声道
    }

    private Channel channel=Channel.DOUBLE;//声道类型
    private int sampleRate=48000;//采样率
    private int sampleFormat=AudioFormat.ENCODING_PCM_16BIT;//位深度
    private int bitrate=128000;//码率,高品质可以320k
    private int channelCount=2;//声道数，为1或2
    private int channelConfig=AudioFormat.CHANNEL_IN_STEREO;

    public AudioInfo() {
    }

    public AudioInfo(Channel channe, int sampleRate, int sampleFormat, int bitrate) {
        this.channel = channel;
        this.sampleRate = sampleRate;
        this.sampleFormat = sampleFormat;
        this.bitrate = bitrate;
    }

    public Channel getChannel() {
        return channel;
    }

    public int getChannelConfig() {
        return channel == Channel.SINGLE ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getAudioRate() {
        return bitrate;
    }

    public int getChannelCount() {
        return channel == Channel.SINGLE ? 1 : 2;
    }


    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSampleFormat() {
        return sampleFormat;
    }

    public void setSampleFormat(int sampleFormat) {
        this.sampleFormat = sampleFormat;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
}
