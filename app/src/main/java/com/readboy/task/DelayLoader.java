package com.readboy.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 规避简单频繁的操作
 *
 * @author oubin
 * @date 2019/3/18
 */
public class DelayLoader extends CursorLoader {

    private static final int MESSAGE_DELAY_START = 10;

    private long mDelayTime;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_DELAY_START:
                    start();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    public DelayLoader(Context context) {
        super(context);
    }

    public DelayLoader(Context context, TimeUnit unit, int time) {
        super(context);
        this.mDelayTime = unit.toMillis(time);
    }

    public DelayLoader(Context context, Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder, long time) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        this.mDelayTime = time;
    }

    @Override
    protected void onStartLoading() {
        mHandler.removeMessages(MESSAGE_DELAY_START);
        mHandler.sendEmptyMessageDelayed(MESSAGE_DELAY_START, this.mDelayTime);
    }

    private void start() {
        super.onStartLoading();
    }
}
