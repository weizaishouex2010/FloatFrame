package com.huangzhiwei.floatframe;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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
     * 小悬浮窗的布局
     */
    private LinearLayout smallWindowLayout;
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

    /**
     * 小火箭控件
     */
    private ImageView rocketImg;

    /**
     * 记录小火箭的宽度
     */
    private int rocketWidth;

    /**
     * 记录小火箭的高度
     */
    private int rocketHeight;

    /**
     * 记录当前手指是否按下
     */
    private boolean isPressed;

    public FloatWindowSmallView(Context context) {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_frame_small,this);
        smallWindowLayout = (LinearLayout) findViewById(R.id.small_window_layout);
        viewHeight = smallWindowLayout.getLayoutParams().height;
        viewWidth = smallWindowLayout.getLayoutParams().width;

        rocketImg = (ImageView) findViewById(R.id.rocket_img);
        rocketHeight = rocketImg.getLayoutParams().height;
        rocketWidth = rocketImg.getLayoutParams().width;
        TextView percent = (TextView) findViewById(R.id.percent);
        percent.setText(MyWindowManager.getUsedPercentValue(context));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
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

                updateViewStatus();
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                isPressed = false;
                if(MyWindowManager.isReadyToLaunch())
                {
                    launchRocket();
                }
                else
                {
                    updateViewStatus();
                    // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                    if(xMoveInScreen == xDownInScreen && yDownInScreen == yMoveInScreen)
                        openBigWindow();
                }
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
        MyWindowManager.updateLauncher();
    }

    /**
     * 用于发射小火箭。
     */
    private void launchRocket()
    {
        MyWindowManager.removeLauncher(getContext());
        new LaunchTask().execute();
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

    private void updateViewStatus()
    {
        if(isPressed && rocketImg.getVisibility()!=View.VISIBLE)
        {
            mParams.width = rocketWidth;
            mParams.height = rocketHeight;
            windowManager.updateViewLayout(this,mParams);
            smallWindowLayout.setVisibility(View.GONE);
            rocketImg.setVisibility(View.VISIBLE);
            MyWindowManager.createLauncher(getContext());
        }
        else if(!isPressed)
        {
            mParams.width = viewWidth;
            mParams.height = viewHeight;
            windowManager.updateViewLayout(this,mParams);
            smallWindowLayout.setVisibility(View.VISIBLE);
            rocketImg.setVisibility(View.GONE);
            MyWindowManager.removeLauncher(getContext());
        }

    }

    class LaunchTask extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
            while (mParams.y>0)
            {
                mParams.y -= 10;
                publishProgress();
                try {
                    Thread.sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            windowManager.updateViewLayout(FloatWindowSmallView.this,mParams);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateViewStatus();
            mParams.x = (int) (xDownInScreen-xDownInView);
            mParams.y = (int) (yDownInScreen-yDownInView);
            windowManager.updateViewLayout(FloatWindowSmallView.this,mParams);
        }
    }
}
