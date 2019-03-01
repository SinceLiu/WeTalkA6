package com.readboy.receiver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * TODO，android系统高版本，可能会限制应用后台运行，把MessageReceiver部分任务用service或者Jobscheduler实现。
 *
 * @author oubin
 * @date 2019/1/8
 */
public class MessageService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
