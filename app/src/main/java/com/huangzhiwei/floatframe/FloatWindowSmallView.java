package com.huangzhiwei.floatframe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * TODO: document your custom view class.
 */
public class FloatWindowSmallView extends LinearLayout {

    /**
     * 记录小悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int viewHeight;

    /**
     * 用于更新小悬浮窗的位置
     */
    private WindowManager windowManager;

    /**
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;


    /**
     * 记录手指位置在屏幕上的移动过程的横坐标值
     */
    private float xMoveInScreen;

    /**
     * 记录手指位置在屏幕上的移动过程的纵坐标值
     */
    private float yMoveInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值，即移动时初始位置的横坐标值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值，即移动时初始位置的纵坐标值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值,即相对于自身控件的横坐标值
     */
    private float xDownInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值，即相对于自身控件的纵坐标值
     */
    private float yDownInView;
    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;

    public FloatWindowSmallView(Context context) {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_frame_small,this);
        View view = findViewById(R.id.small_window_layout);
        viewHeight = view.getLayoutParams().height;
        viewWidth = view.getLayoutParams().width;
        TextView percent = (TextView) findViewById(R.id.percent);
        percent.setText(MyWindowManager.getUsedPercentValue(context));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY()-getStatusBarHeight();
                xMoveInScreen = event.getRawX();
                yMoveInScreen = event.getRawY()-getStatusBarHeight();
                xDownInView = event.getX();
                yDownInView = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                xMoveInScreen = event.getRawX();
                yMoveInScreen = event.getRawY()-getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if(xMoveInScreen == xDownInScreen && yDownInScreen == yMoveInScreen)
                    openBigWindow();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 打开大悬浮窗，同时关闭小悬浮窗。
     */
    public void openBigWindow()
    {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params
     *            小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xMoveInScreen-xDownInView);
        mParams.y = (int) (yMoveInScreen-yDownInView);
        windowManager.updateViewLayout(this,mParams);
    }


    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if(statusBarHeight==0)
        {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (int) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }
}
