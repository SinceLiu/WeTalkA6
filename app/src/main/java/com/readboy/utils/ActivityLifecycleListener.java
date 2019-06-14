package com.readboy.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * 用生命周期回调区分wetalk前后台
 */
public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {
    private static int createdNum;
    private static int destroyedNum;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        createdNum++;
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        destroyedNum++;
    }

    public static boolean isBackground(Context context) {
        if (createdNum <= destroyedNum) {
            return true;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo processInfo = appProcesses.get(0);
        if (context.getPackageName().equals(processInfo.processName)) {
            return false;
        }
        return true;
    }
}
