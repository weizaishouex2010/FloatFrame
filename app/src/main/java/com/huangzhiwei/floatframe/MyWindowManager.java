package com.huangzhiwei.floatframe;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by huangzhiwei on 16/2/18.
 */
public class MyWindowManager {
    private static String TAG = "FloatFrameService";
    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;
    /**
     * 用于获取手机可用内存
     */
    private static ActivityManager mActivityManager;

    /**
     * 小悬浮窗View的实例
     */
    private static FloatWindowSmallView smallView;
    /**
     * 小悬浮窗View的参数
     */
    private static WindowManager.LayoutParams smallLayoutParams;
    /**
     * 大悬浮窗View的参数
     */
    private static WindowManager.LayoutParams bigLayoutParams;

    /**
     * 大悬浮窗View的实例
     */
    private static FloatWindowBigView bigView;

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context
     *            必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context)
    {
        if(mWindowManager==null)
        {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }


    /**
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
     *
     * @param context
     *            可传入应用程序上下文。
     * @return ActivityManager的实例，用于获取手机可用内存。
     */
    private static ActivityManager getActivityManager(Context context)
    {
        if(mActivityManager==null)
        {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }


    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void createSmallWindow(Context context)
    {
        WindowManager  windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if(smallView == null)
        {
            smallView = new FloatWindowSmallView(context);
            if(smallLayoutParams == null)
            {
                smallLayoutParams = new WindowManager.LayoutParams();
                //电话窗口。它用于电话交互（特别是呼入）。它置于所有应用程序之上，状态栏之下。
                smallLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                smallLayoutParams.format = PixelFormat.RGBA_8888;
                smallLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                smallLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallLayoutParams.width = FloatWindowSmallView.viewWidth;
                smallLayoutParams.height = FloatWindowSmallView.viewHeight;
                smallLayoutParams.x = screenWidth - FloatWindowSmallView.viewWidth;
                smallLayoutParams.y = screenHeight / 2;

            }
            smallView.setParams(smallLayoutParams);
            windowManager.addView(smallView,smallLayoutParams);
        }
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context)
    {
        if(smallView !=null )
        {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallView);
            smallView = null;
        }
    }

    /**
     * 创建一个大悬浮窗。位置为屏幕正中间。
     *
     * @param context
     *            必须为应用程序的Context.
     */

    public static void createBigWindow(Context context)
    {
        WindowManager  windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if(bigView == null)
        {
            bigView = new FloatWindowBigView(context);
            if(bigLayoutParams == null)
            {
                bigLayoutParams = new WindowManager.LayoutParams();
                bigLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                bigLayoutParams.format = PixelFormat.RGBA_8888;
                bigLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                bigLayoutParams.width = FloatWindowBigView.viewWidth;
                bigLayoutParams.height = FloatWindowBigView.viewHeight;
                bigLayoutParams.x = screenWidth/2 - FloatWindowBigView.viewWidth/2;
                bigLayoutParams.y = screenHeight/2 - FloatWindowBigView.viewHeight/2;
            }
            windowManager.addView(bigView,bigLayoutParams);
        }
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     *
     * @param context
     *            必须为应用程序的Context.
     */

    public static void removeBigWindow(Context context)
    {
        if(bigView != null)
        {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(bigView);
            bigView = null;
        }
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isShowing()
    {
        return bigView != null || smallView != null;
    }

    /**
     * 计算已使用内存的百分比，并返回。
     *
     * @param context
     *            可传入应用程序上下文。
     * @return 已使用内存的百分比，以字符串形式返回。
     */


    public static void updateUsedPercent(Context context) {
        if(smallView!=null)
        {
            TextView percent = (TextView) smallView.findViewById(R.id.percent);
            percent.setText(getUsedPercentValue(context));
        }
    }

    public static String getUsedPercentValue(Context context)
    {

        String dir = "/proc/meminfo";
        try {
            FileReader fileReader = new FileReader(dir);
            BufferedReader bufferedReader = new BufferedReader(fileReader,2048);
            String memoryLine = bufferedReader.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            bufferedReader.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+",""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) (( totalMemorySize - availableSize ) / (float) totalMemorySize * 100);
            Log.d(TAG,availableSize+"/"+totalMemorySize+ "= "+ percent+"%");
            Log.d(TAG,percent+"%");
            return percent+"%";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "悬浮窗";
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context
     *            可传入应用程序上下文。
     * @return 当前可用内存。
     */
    private static long getAvailableMemory(Context context)
    {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }


}
