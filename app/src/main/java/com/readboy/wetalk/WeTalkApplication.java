package com.readboy.wetalk;

import com.readboy.utils.ActivityLifecycleListener;
import com.readboy.utils.LogInfo;

import android.app.Application;
import android.os.AsyncTask;

public class WeTalkApplication extends Application {
    private static final String TAG = "WeTalkApplication";

    //测试流量用
    public static final boolean IS_TEST_MODE = false;

    @Override
    public void onCreate() {
        LogInfo.i(" WeTalkApplication --- onCreate()");
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleListener());
    }
}
