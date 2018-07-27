package com.lee.cavansrect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @类名: ${type_name}
 * @功能描述:
 * @作者: ${user}
 * @时间: ${date}
 * @最后修改者:
 * @最后修改内容:
 */
public class BitmapUtils {

    /*********** 
     * @获得原始流*
     ***********/
    public static InputStream toInputStream(String path) throws Exception{
        FileInputStream is = new FileInputStream(path.replace("file://", ""));
        return is;
    }
    
    
    /************ 
     * 返回压缩后的流* 
     ************/
    public static Bitmap toBitmap(String path, int reqWidth, int reqHeight) throws Exception{
        Bitmap bm = null;
        InputStream inputStream = toInputStream(path);  // 远程读取输入流  
        if (inputStream == null){
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 当inJustDecodeBounds设为true时,不会加载图片仅获取图片尺寸信息  
        options.inJustDecodeBounds = true;
        // 获取图片大小  
        BitmapFactory.decodeStream(inputStream, null, options);
        // 获取图片压缩比  
        int size = calculateInSampleSize(options, reqWidth, reqHeight);
        if ( size < 0 ){
            return null;
        }
        Log.e("size", String.valueOf(size));
        options.inSampleSize = size;   // 找到合适的倍率  
        // 当inJustDecodeBounds设为false,加载图片到内存  
        options.inJustDecodeBounds = false;
        inputStream = toInputStream(path);  // 远程读取输入流,要再读一次，否则之前的inputStream已无效了  
        bm = BitmapFactory.decodeStream(inputStream, null, options);
        return bm;
    }
    
    
    /********************************* 
     * @function: 计算出合适的图片倍率
     * @options: 图片bitmapFactory选项
     * @reqWidth: 需要的图片宽
     * @reqHeight: 需要的图片长
     * @return: 成功返回倍率, 异常-1  
     ********************************/
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                       int reqHeight) {
        // 设置初始压缩率为1  
        int inSampleSize = 1;
        try{
            // 获取原图片长宽  
            int width = options.outWidth;
            int height = options.outHeight;
            // reqWidth/width,reqHeight/height两者中最大值作为压缩比  
            int w_size = width/reqWidth;
            int h_size = height/reqHeight;
            inSampleSize = w_size>h_size?w_size:h_size;  // 取w_size和h_size两者中最大值作为压缩比  
            Log.e("inSampleSize", String.valueOf(inSampleSize));
        }catch(Exception e){
            return -1;
        }
        return inSampleSize;
    }



    /********************************* 
     * @function:保存图片到指定路径
     * @param bitmap
     * @param imagePath
     * @param format
     * @throws IOException
     *********************************/
    public void saveBitmap(Bitmap bitmap, String imagePath, int format)
            throws IOException {
        File file;
        file = new File(imagePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        if (format == 2) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } else if (format == 1) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }
        fos.flush();
        fos.close();
    }


}
