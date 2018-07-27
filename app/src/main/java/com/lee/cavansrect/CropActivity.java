package com.lee.cavansrect;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @类名: ${type_name}
 * @功能描述:
 * @作者: ${user}
 * @时间: ${date}
 * @最后修改者:
 * @最后修改内容:
 */
public class CropActivity extends Activity {

    private String photoPath = "";

    private CustomSurfaceView surfce;
    private LinearLayout linear;
    private ImageView image01, image02;

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_crop);

        linear = (LinearLayout) findViewById(R.id.overlay_image);
        image01 = (ImageView) findViewById(R.id.image_01);
        image02 = (ImageView) findViewById(R.id.image_02);
        photoPath = Environment.getExternalStorageDirectory().toString() + "/test.jpg";

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
        
        init();
    }

    boolean isRun = true;
    private Bitmap  bitmap;
    private Bitmap  bitmap1;
    private Bitmap  bitmap2;
    private List<CustomSurfaceView.Postion> position;
    private void init() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    Display display = getWindowManager().getDefaultDisplay();
                    try {
                        bitmap = BitmapUtils.toBitmap(photoPath, display.getWidth(), display.getHeight());
                        if (bitmap == null){
                            isRun = true;
                        } else {
                            isRun = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        surfce = new CustomSurfaceView(CropActivity.this, photoPath, false);
        linear.addView(surfce);

        surfce.setState(0);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = new ArrayList<>();
                if (bitmap != null) {
                    position = surfce.getPosition();

                    bitmap1 = null;
                    bitmap2 = null;
                    bitmap1 = Bitmap.createBitmap(bitmap, surfce.getPosition().get(0).getLeft(),
                            surfce.getPosition().get(0).getTop(),
                            surfce.getPosition().get(0).getRight() - surfce.getPosition().get(0).getLeft(),
                            surfce.getPosition().get(0).getBottom() - surfce.getPosition().get(0).getTop());
                    bitmap2 = Bitmap.createBitmap(bitmap, surfce.getPosition().get(1).getLeft(),
                            surfce.getPosition().get(1).getTop(),
                            surfce.getPosition().get(1).getRight() - surfce.getPosition().get(1).getLeft(),
                            surfce.getPosition().get(1).getBottom() - surfce.getPosition().get(1).getTop());
                    if (bitmap == null || bitmap1 == null || bitmap2 == null || position.size() != 2) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(CropActivity.this);
                        builder.setMessage("照片裁剪失败，请重新裁剪！！").setTitle("提示").setPositiveButton("返回", null);
                    } else {
                        image01.setImageBitmap(bitmap1);
                        image02.setImageBitmap(bitmap2);
                    }
                }
            }
        });

        findViewById(R.id.canle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfce.revocation();
            }
        });
        
    }
 
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        if (bitmap1 != null) {
            bitmap1.recycle();
            bitmap1 = null;
        }
        if (bitmap2 != null) {
            bitmap2.recycle();
            bitmap2 = null;
        }
       
        System.gc();
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }
}
