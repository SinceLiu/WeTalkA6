package com.readboy.receiver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 *
 * @author oubin
 * @date 2019/1/8
 */
public class MessageService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
