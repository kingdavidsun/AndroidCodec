package com.david.codec.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.david.codec.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        requestPermission();
    }
    //简单申请权限，未做合理处理
    private void requestPermission() {

        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int filePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED || filePermission != PackageManager.PERMISSION_GRANTED || audioPermission != PackageManager.PERMISSION_GRANTED) {
            //没有权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 10086);
        } else {
            //权限通过了
        }
    }
    public void startH264Record(View view) {
        Intent intent=new Intent(this,H264Activity.class);
        startActivity(intent);
    }

    public void startAACRecord(View view) {
        Intent intent=new Intent(this,AACActivity.class);
        startActivity(intent);
    }

    public void startMP4Record(View view) {
        Intent intent=new Intent(this,Mp4Activity.class);
        startActivity(intent);
    }
}
