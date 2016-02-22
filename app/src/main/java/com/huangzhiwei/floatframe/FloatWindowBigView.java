package com.huangzhiwei.floatframe;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * TODO: document your custom view class.
 */
public class FloatWindowBigView extends LinearLayout {

    /**
     * 记录大悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录大悬浮窗的高度
     */
    public static int viewHeight;
    public FloatWindowBigView(final Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.float_frame_big,this);
        View view = findViewById(R.id.big_window_layout);
        viewHeight = view.getLayoutParams().height;
        viewWidth = view.getLayoutParams().width;

        Button closeBtn = (Button)findViewById(R.id.close);
        Button backBtn = (Button)findViewById(R.id.back);

        // 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.removeSmallWindow(context);
                context.stopService(new Intent(context,FloatFrameService.class));
            }
        });

        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回的时候，移除大悬浮窗，创建小悬浮窗
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
            }
        });
    }

}
