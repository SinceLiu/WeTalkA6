package com.readboy.receiver;

import com.readboy.service.MessageService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import static com.readboy.wetalk.view.WetalkFrameLayout.ACTION_UPDATE_NOTIFICATION;

/**
 * @author hwwjian
 * @date 2016/12/14
 * 获取消息
 * TODO 如果被删除的联系人有未读数，发送通知时，未读数的获取有误，需要修改校验
 */

public class MessageReceiver extends BroadcastReceiver {
    private static final String TAG = "hwj_MessageReceiver";
    public static final String ACTION_NOTIFY_MESSAGE = "readboy.action.NOTIFY_MESSAGE";
    /**
     * 请求添加好友
     */
    public static final String ACTION_NOTIFY_FRIEND_ADD = "readboy.action.NOTIFY_FRIEND_ADD";
    public static final String ACTION_NOTIFY_FRIEND_REFUSE = "readboy.action.NOTIFY_FRIEND_REFUSE";
    public static final String ACTION_NOTIFY_FRIEND_REQUEST = "readboy.action.NOTIFY_FRIEND_REQUEST";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive2: action = " + action);
        if (TextUtils.isEmpty(action)) {
            return;
        }
        Intent messageIntent = new Intent(context, MessageService.class);
        messageIntent.putExtra("action", action);
        switch (action) {
            case ACTION_NOTIFY_MESSAGE:
                //收到消息
                //如果正在获取新消息，是否应已队列形式，排队获取。
                context.startForegroundService(messageIntent);
                break;
            case ACTION_NOTIFY_FRIEND_REQUEST:
                messageIntent.putExtra("id", intent.getStringExtra("id"));
                context.startForegroundService(messageIntent);
                break;
            case ACTION_UPDATE_NOTIFICATION:
                context.startForegroundService(messageIntent);
                break;
            default:
                break;
        }
    }
}
