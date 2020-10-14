package com.david.codec.media.video;

/**
 * Created by David on 2020/9/3
 * 视频编码信息
 */
public class VideoInfo {
    //宽高要与相机预览一致
    private int width=1280;
    private int height=720;
    private int videoRate = 2048000;       //视频编码波特率
    private int frameRate = 24;           //视频编码帧率
    private int frameInterval = 1;        //视频编码关键帧，1秒一关键帧

    public VideoInfo() {
    }

    public VideoInfo(int width, int height, int frameRate) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getVideoRate() {
        return videoRate;
    }

    public void setVideoRate(int videoRate) {
        this.videoRate = videoRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getFrameInterval() {
        return frameInterval;
    }

    public void setFrameInterval(int frameInterval) {
        this.frameInterval = frameInterval;
    }
}
