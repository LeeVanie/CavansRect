package com.lee.cavansrect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Lee  on 2018/7/27.
 */

public class CustomSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback, Runnable, Handler.Callback {
    // SurfaceHolder
    private SurfaceHolder mSurfaceHolder;

    /**
     * 屏幕尺寸
     */
    private int viewWidth;
    private int viewHeight;

    // 线宽
    private int StrokeWidth = 5;
    
    private boolean startDraw;
    //半径
    private int radius;
    // Path
    private Path mPath = new Path();
    // 画笔
    private Paint mpaint = new Paint();

    private Canvas canvas;
    //滑板背景（保存绘制的图片）
    private Bitmap saveBitmap;
    //图像
    Bitmap bitmap;

    // 图片路径
    private String urlPath;
    private List<DrawPath> drawPathList = new ArrayList<>();

    /**
     * X 、 Y 方向的图片和屏幕比例
     */
    private float scaleX, scaleY;
    
    /**
     * 0矩形
     * 1撤回
     */
    private static int state = 0;

    public void setState(int state) {
        this.state = state;
    }



    public CustomSurfaceView(Context context, String url, boolean s) {
       
        this(context, null);
        this.urlPath = url;
        saveBitmap = Bitmap.createBitmap(720, 1000, Bitmap.Config.ARGB_8888);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(); // 初始化

    }

    private void initView() {
        setMeasuredDimension(720, 1000);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    private Handler handler = new Handler(this);
    @Override
    public void run() {
        while (startDraw) {
            
            if (urlPath != null) {
                    try {
                        bitmap = BitmapUtils.toBitmap(urlPath, getWidth(), getHeight());
                        if (bitmap == null) {
                            startDraw = true;
                        } else {
                            startDraw = false; 
                        }
                    } catch (Exception e) {
                        Log.d("CustomSurfaceView", "CustomSurfaceView ------- " + e.toString());
                    }
                }
            
            handler.sendEmptyMessage(1);
        }
    }

    /*
         * 创建
         */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startDraw = true;

        canvas = mSurfaceHolder.lockCanvas();
        canvas.setBitmap(saveBitmap);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        new Thread(this).start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getWidth();
        viewHeight = getHeight();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    /*
     * 销毁
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        startDraw = false;
    }

    int startX;
    int startY;
    int stopX;
    int stopY;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                mpaint = new Paint();
                startX = 0;
                startY = 0;
                startX = (int) event.getX();
                startY = (int) event.getY();
                mPath.moveTo(startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                stopX = (int) event.getX();
                stopY = (int) event.getY();
                if (state == 0) {
                    draws();

                }
                break;
            case MotionEvent.ACTION_UP:
                if (drawPathList.size() + 1 <= 1){
                    if (state == 0) {

                        mPath.moveTo(startX, startY);
                        mPath.lineTo(startX, stopY);
                        mPath.lineTo(stopX, stopY);
                        mPath.lineTo(stopX, startY);
                        mPath.lineTo(startX, startY);
                        mPath.close();

                    }

                }
                position.add(union((int) (stopX / scaleX), (int) (stopY / scaleY),
                        (int) (startX / scaleX), (int) (startY / scaleY),
                        (int) (startX / scaleX), (int) (startY / scaleY)));

                setPosition(position);
                
                drawPathList.add(new DrawPath(mpaint, mPath));
                
                //  限制绘制矩形个数
                if (drawPathList.size() == 3){  
                    drawPathList.remove(drawPathList.size() - 2);
//                    drawPathList.add(new DrawPath(mpaint, mPath));
                }
                if (position.size() == 3){
                    position.remove(position.size() - 2);
//                    position.add(union((int) (stopX / scaleX), (int) (stopY / scaleY),
//                            (int) (startX / scaleX), (int) (startY / scaleY),
//                            (int) (startX / scaleX), (int) (startY / scaleY)));
                    setPosition(position);
                }
                break;
        }
        return true;
    }

    /**
     * 判断四个顶点的位置，绘制矩形
     * @param x
     * @param y
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    public Postion union(int x, int y, int left, int top, int right, int bottom) {
        int temp = 0;
        if (x < left) {
            temp = left;
            left = x;
            right = temp;
        } else if (x > right) {
            temp = right;
            right = x;
            left = temp;
        }
        if (y < top) {
            temp = top;
            top = y;
            bottom = temp;
        } else if (y > bottom) {
            temp = bottom;
            bottom = y;
            top = temp;
        }
        
        return new Postion(left, top, right, bottom);
        
    }

    /**
     * 获取绘制的四个点在原图的位置集合
     */
    private List<Postion> position = new ArrayList<>();

    public List<Postion> getPosition() {
        return position;
    }

    public void setPosition(List<Postion> position1) {
        this.position = position1;
    }
    
    public void draws() {
        if (bitmap == null) {
            Toast.makeText(getContext(), "加载图片失败", Toast.LENGTH_SHORT).show();
            Log.e("msg", "加载图片失败");
            return;
        }
        
        canvas = mSurfaceHolder.lockCanvas();
        Rect rectF = new Rect(0, 0, getWidth(), getHeight());   //w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高

        scaleX = (float) getWidth() / bitmap.getWidth();
        scaleY = (float) getHeight() / bitmap.getHeight();
        
        canvas.drawBitmap(bitmap, null, rectF, null);
        mpaint.setStyle(Paint.Style.STROKE);
        mpaint.setAntiAlias(true);
        for (int i = 0; i < drawPathList.size(); i++) {
            //把path中的路线绘制出来
            canvas.drawPath(drawPathList.get(i).path, drawPathList.get(i).paint);
        }
        
        mpaint.setColor(Color.RED);
        if (state == 0) {
            mpaint.setColor(Color.RED);
            mpaint.setStyle(Paint.Style.STROKE);
            mpaint.setStrokeWidth(StrokeWidth);
            canvas.drawRect(startX, startY, stopX, stopY, mpaint);

        }
        
        mSurfaceHolder.unlockCanvasAndPost(canvas);

    }

    

    @Override
    public boolean handleMessage(Message msg) {
        canvas = mSurfaceHolder.lockCanvas();
        //这里相当于是一个预览图
        Rect rectF = new Rect(0, 0, viewWidth, viewHeight);   //w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
        if (bitmap!= null&& canvas!= null)
        canvas.drawBitmap(bitmap, null, rectF, null);
        if (canvas!= null)
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        if (bitmap != null) {
            startDraw = false;
        }

        return false;
    }
    
    public class DrawPath {

        public Paint paint;
        public Path path;

        public DrawPath(Paint paint, Path path) {
            this.paint = paint;
            this.path = path;
        }
    }

    /**
     * 撤销上一个矩形
     */
    public void revocation() {
        if (drawPathList.size() > 0) {
            drawPathList.remove(drawPathList.size() - 1);
            position.remove(position.size() - 1);
            if (drawPathList.size() == 0){
                
                position = new ArrayList<>();

            }
            startX = 0; startY = 0; stopX = 0; stopY = 0;

            draws();
        }
        
    }


    /**
     * 位置 Bean
     */
    public class Postion{
        public int left;
        public int top;
        public int right;
        public int bottom;

        public int getLeft() {
            return left;
        }

        public int getTop() {
            return top;
        }

        public int getRight() {
            return right;
        }

        public int getBottom() {
            return bottom;
        }

        public Postion(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }
}
