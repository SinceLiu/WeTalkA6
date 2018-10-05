package com.readboy.utils;

import android.util.Log;

import com.readboy.wetalk.BaseActivity;

/**
 * Created by hwwjian on 2016/11/17.
 */

public class LogInfo {

    //调试模式
    private static final boolean IS_DEBUG = true;

    public static void i(String msg) {
        if (IS_DEBUG) {
            Log.i(BaseActivity.TAG, msg);
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
            Log.e(BaseActivity.TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (IS_DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void d(String msg) {
        if (IS_DEBUG) {
            Log.d(BaseActivity.TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(tag, msg);
        }
    }
}
