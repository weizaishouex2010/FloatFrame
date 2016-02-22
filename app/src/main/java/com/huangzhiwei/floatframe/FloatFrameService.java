package com.huangzhiwei.floatframe;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FloatFrameService extends Service {

    private static String TAG = "FloatFrameService";
    /**
     * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
     */
    private Timer timer;

    /**
     * 用于在线程中创建或移除悬浮窗。
     */
    private Handler handler = new Handler();


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(timer==null)
        {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(),0,500);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    class RefreshTask extends TimerTask{
        @Override
        public void run() {
            // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
            if(isHome() && !MyWindowManager.isShowing())
            {
                Log.d(TAG,"当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.createSmallWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
            else if(!isHome() && MyWindowManager.isShowing())
            {
                Log.d(TAG,"当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.removeSmallWindow(getApplicationContext());
                        MyWindowManager.removeBigWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
            else if(isHome() && MyWindowManager.isShowing())
            {
                Log.d(TAG,"当前界面是桌面，且有悬浮窗显示，则更新内存数据。");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }
            else
            {
                Log.d(TAG,"什么都没有");
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service被终止的同时也停止定时器继续运行
        if(timer!=null)
        {
            timer.cancel();
            timer = null;
        }

    }

    /**
     * 判断当前界面是否是桌面
     */
    public boolean isHome()
    {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> infos = activityManager.getRunningTasks(1);
        for (String name :
                getHomes()) {
        }
        return getHomes().contains(infos.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    public List<String> getHomes()
    {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info :
                resolveInfo) {
            names.add(info.activityInfo.packageName);
        }
        return names;
    }
}
