package com.example.surfaceviewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    /**
     * 每帧刷新一次屏幕的时间
     **/
    public static final int TIME_IN_FRAME = 30;
    // 这三个变量用于初始化 start
    // 见init()方法。
    private SurfaceHolder mHolder;
    // 使用 Canvas 来进行绘图
    private Canvas mCanvas;
    // 子线程标志位，用于控制用于绘制的子线程
    private boolean mIsDrawing;
    // 这三个变量用于初始化 end
    private Path mPath;
    private Paint mPaint;
    private int x, y;

    // constructor start
    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    // constructor end

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        // window系统的focus
        setFocusable(true);
        setFocusableInTouchMode(true);
        //
        setKeepScreenOn(true);

        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        // 设置画笔空心
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔宽度
        mPaint.setStrokeWidth(10);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    // Runnable, 用来绘制的线程
    @Override
    public void run() {
        while (mIsDrawing) {
            /**取得更新之前的时间**/
            long startTime = System.currentTimeMillis();
            // Log.i("sv-test", "startTime:" + startTime);
            /**在这里加上线程安全锁**/
            synchronized (mHolder) {
                draw();
                x += 1;
                y = (int) (100 * Math.sin(x * 2 * Math.PI / 180) + 400);
                mPath.lineTo(x, y);
            }

            /**取得更新结束的时间**/
            long endTime = System.currentTimeMillis();

            /**计算出一次更新的毫秒数**/
            int diffTime = (int) (endTime - startTime);

            /**确保每次更新时间为30帧**/
            while (diffTime <= TIME_IN_FRAME) {
                diffTime = (int) (System.currentTimeMillis() - startTime);
                /**线程等待**/
                /**
                 * Thread.yield(): 与Thread.sleep(long millis):的区别：
                 *
                 * Thread.yield(): 是暂停当前正在执行的线程对象 ，并去执行其他线程。
                 * Thread.sleep(long millis):则是使当前线程暂停参数中所指定的毫秒数然后在继续执行线程。
                 */
                // Log.i("sv-test", "diffTime:" + diffTime);
                Thread.yield();
            }
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();//获取Canvas对象进行绘制
            //SurfaceView背景
            mCanvas.drawColor(Color.WHITE);
            mCanvas.drawPath(mPath, mPaint);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);//保证绘制的画布内容提交
            }
        }
    }

    // methods of SurfaceHolder.Callback start
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.i("sv-test", "surfaceCreated");
        mIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.i("sv-test", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i("sv-test", "surfaceDestroyed");
        mIsDrawing = false;
    }
    // methods of SurfaceHolder.Callback end
}
