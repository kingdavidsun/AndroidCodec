package com.david.codec.media.util;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by David on 2020/7/29
 */
public class FileUtils {
    public static final String H264_FOLDER= Environment.getExternalStorageDirectory()+"/david/h264/";
    public static final String AAC_FOLDER= Environment.getExternalStorageDirectory()+"/david/aac/";
    public static final String MP4_FOLDER= Environment.getExternalStorageDirectory()+"/david/mp4/";
    public static void writeData(byte[]data,String parentPath,String fileName){
        File parent=new File(parentPath);
        if (!parent.exists()){
            parent.mkdirs();
        }
        File file=new File(parent,fileName);
        try {
            FileOutputStream fos=new FileOutputStream(file,true);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
