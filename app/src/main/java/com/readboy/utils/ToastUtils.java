package com.readboy.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * @author ouibn
 * @date 2016/8/30
 */

public class ToastUtils {

    private static Toast toast;
    private static String oldMsg = "";
    private static long oneTime = 0;
    private static long twoTime = 0;


    /**
     * backgroundID = 0 代表使用默认背景 backgroundID = 1
     */
    private static void initToast(Context context, int backgroundID) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT);
        }
        ViewGroup viewGroup = (ViewGroup) toast.getView();
        if (backgroundID != 0) {
            viewGroup.setBackgroundResource(backgroundID);
        }
        viewGroup.getBackground().setAlpha(180);
        TextView textView = (TextView) viewGroup.getChildAt(0);
        textView.setTextSize(24);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(viewGroup);
    }

    private ToastUtils() {
        throw new UnsupportedOperationException("u can't fuck me...");
    }

    public static void show(Context context, int resId) {
        show(context, context.getResources().getString(resId));
    }

    public static void show(Context context, String msg) {
        show(context, msg, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String msg, int time) {
        if (toast == null) {
            initToast(context, 0);
            toast.setText(msg);
            toast.setDuration(time);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (msg.equals(oldMsg)) {
                if (twoTime - oneTime > 2000) {
                    toast.setDuration(time);
                    toast.show();
                }else {
                    return;
                }
            } else {
                oldMsg = msg;
                toast.setDuration(time);
                toast.setText(msg);
                toast.show();
            }
        }
        oneTime = twoTime;
    }

    public static void showShort(Context context, String msg) {
        show(context, msg, Toast.LENGTH_SHORT);
    }

    public static void showLong(Context context, String msg) {
        show(context, msg, Toast.LENGTH_LONG);
    }

    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
