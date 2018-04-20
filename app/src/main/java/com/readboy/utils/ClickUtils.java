package com.readboy.utils;


/**
 * @author oubin
 * @date 2018/4/11
 */

public final class ClickUtils {

    /**
     * 多次点击时间差，无阀值，多次点击，会多次返回
     */
    private static final long FAST_CLICK_OFFSET = 500L;
    /**
     * 记录多次点击时间差，有阀值。
     * 多次点击等于阀值，就返回。
     */
    private static final long FAST_MULTI_CLICK_OFFSET = 300L;
    private static long lastClickTime;
    private static long lastMultiClickTime;
    private static long lastMultiClickThresholdTime;
    private static int multiClickThreshold = 0;
    private static int multiClickTime = 0;

    /**
     * @see #FAST_CLICK_OFFSET 500毫秒内返回false.
     */
    public static boolean isFastDoubleClick() {
        long var0 = System.currentTimeMillis();
        if (var0 - lastClickTime < FAST_CLICK_OFFSET) {
            return true;
        } else {
            lastClickTime = var0;
            return false;
        }
    }

    /**
     * 两次点击时间间隔内都返回false
     */
    public static boolean isFastMultiClick() {
        long current = System.currentTimeMillis();
        if (current - lastMultiClickTime < FAST_MULTI_CLICK_OFFSET) {
            lastMultiClickTime = current;
            return true;
        } else {
            lastMultiClickTime = current;
            return false;
        }
    }

    /**
     * 达到一定点击数就返回true。
     * @param threshold 阀值
     */
    public static boolean isFastMultiClick(int threshold) {
        if (threshold != multiClickThreshold) {
            multiClickThreshold = threshold;
            multiClickTime = 0;
        }
        long current = System.currentTimeMillis();
        if (current - lastMultiClickThresholdTime < FAST_MULTI_CLICK_OFFSET) {
            multiClickTime++;
            lastMultiClickThresholdTime = current;
            if (multiClickTime == threshold) {
                return true;
            } else {
                return false;
            }
        } else {
            multiClickTime = 1;
            lastMultiClickThresholdTime = current;
            return false;
        }
    }

}
