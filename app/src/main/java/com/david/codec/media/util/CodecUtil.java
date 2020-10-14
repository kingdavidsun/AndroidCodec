package com.david.codec.media.util;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

/**
 * Created by David on 2020/9/3
 */
public class CodecUtil {
    /**
     * 检测是否支持H265硬编码
     * @return 检测结果
     */
    public static boolean isH265EncoderSupport(){
        boolean result = false;
        int count = MediaCodecList.getCodecCount();
        for(int i=0;i<count;i++){
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            String name = info.getName();
            boolean b = info.isEncoder();
            if(b && name.contains("hevc")){
                return true;
            }
        }
        return false;
    }
    /**
     * 检测是否支持H265硬解码
     * @return 检测结果
     */
    public static boolean isH265DecoderSupport(){
        int count = MediaCodecList.getCodecCount();
        for(int i=0;i<count;i++){
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            String name = info.getName();
            if(name.contains("decoder") && name.contains("hevc")){
                return true;
            }
        }
        return false;
    }
}
