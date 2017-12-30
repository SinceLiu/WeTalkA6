package com.readboy.wetalk;

import com.readboy.utils.LogInfo;
import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;

public class WeTalkApplication extends Application {
    private static final String TAG = "WeTalkApplication";

    //测试流量用
    public static final boolean IS_TEST_MODE = false;

    @Override
    public void onCreate() {
        LogInfo.i(" WeTalkApplication --- onCreate()");
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "61c2c66a3a", false);
    }
}
