package com.david.codec.media.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by David on 2020/7/29
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    public static final String H264_FOLDER= Environment.getExternalStorageDirectory()+"/david/h264/";
    public static final String AAC_FOLDER= Environment.getExternalStorageDirectory()+"/david/aac/";
    public static final String MP4_FOLDER= Environment.getExternalStorageDirectory()+"/david/mp4/";
    public static final String YUV_FOLDER= Environment.getExternalStorageDirectory()+"/david/yuv/";
    public static void writeData(byte[]data,String parentPath,String fileName){
        File parent=new File(parentPath);
        if (!parent.exists()){
            boolean mkdirs = parent.mkdirs();
            Log.i(TAG, "writeData: 创建文件夹是否成功："+mkdirs);
        }
        File file=new File(parent,fileName);
        try {
            FileOutputStream fos=new FileOutputStream(file,true);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
