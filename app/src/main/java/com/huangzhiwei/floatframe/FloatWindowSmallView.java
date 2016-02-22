package com.huangzhiwei.floatframe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

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
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;

    public FloatWindowSmallView(Context context) {
        super(context);
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
                break;
            case MotionEvent.ACTION_UP:
                openBigWindow();
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
}
