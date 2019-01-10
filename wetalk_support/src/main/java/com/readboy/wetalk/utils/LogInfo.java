package com.readboy.wetalk.utils;

import android.util.Log;

/**
 * Created by hwwjian on 2016/11/17.
 */

public class LogInfo {
    private static final String TAG = "wetalk";


    //调试模式
    private static final boolean IS_DEBUG = true;

    public static void i(String msg) {
        if (IS_DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (IS_DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (IS_DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String msg) {
        if (IS_DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (IS_DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void d(String msg) {
        if (IS_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(tag, msg);
        }
    }
}
