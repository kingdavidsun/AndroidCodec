package com.david.codec.media.util;

/**
 * Created by David on 2020/7/29
 * 进行yuv数据转换
 */
public class YUVUtil {
    public static void NV21ToPlanar(byte[] nv21, byte[] planar, int width, int height) {
        if (nv21 == null || planar == null) return;
        int framesize = width * height;
        int offset = framesize / 4;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, planar, 0, framesize);
        for (i = 0; i < framesize; i++) {
            planar[i] = nv21[i];
        }
        for (j = 0; j < framesize / 4; j++) {
            planar[framesize + j] = nv21[j * 2 + framesize + 1];
            planar[framesize + offset + j] = nv21[j * 2 + framesize];
        }
    }

    public static void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

}
