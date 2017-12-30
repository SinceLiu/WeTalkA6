package com.readboy.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.readboy.wetalk.FriendActivity;
import com.readboy.wetalk.R;

/**
 * Created by hwwjian on 2016/12/21.
 * 发通知的情况:
 * 1.非微聊界面收到消息,正常通知(声音,震动,弹出)
 * 2.微聊好友界面(震动)
 * 3.当前好友聊天界面(震动)
 * 4.非当前好友聊天界面(震动)
 */

public class NotificationUtils {

    public static final int NOTIFY_ID = "wetlak".hashCode();

    //"normal_wetalk".hashCode();
    public static final int NORMAL_NOTIFY_ID = "wetlak".hashCode();

    public static void notification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        LogInfo.i("hwj", "process state = " + TaskUtils.isBackground(context));
        //在联系人信息界面或者
        if (!TaskUtils.isBackground(context)) {
            manager.notify(NORMAL_NOTIFY_ID, getNotification(context));
        } else {
            manager.notify(NOTIFY_ID, getFloatingNotification(context));
        }
    }

    public static Notification getFloatingNotification(Context context) {
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, new Intent(context, FriendActivity.class), 0);
        Bundle bundle = new Bundle();
        bundle.putString("extra_type", "readboy");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setExtras(bundle);
        int count = WTContactUtils.getAllContactsUnreadCount(context);
        if (count >= 100) {
            builder.setContentText("收到99+条新消息");
        } else if (count > 0) {
            builder.setContentText("收到" + WTContactUtils.getAllContactsUnreadCount(context) + "条新消息");
        } else {
            builder.setContentText("收到新消息");
        }
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setContentIntent(pendingIntent3);
        builder.setPriority(Notification.PRIORITY_HIGH);
        return builder.build();
    }

    public static Notification getNotification(Context context) {
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, new Intent(context, FriendActivity.class), 0);
        Bundle bundle = new Bundle();
        bundle.putString("extra_type", "readboy");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setExtras(bundle);
        int count = WTContactUtils.getAllContactsUnreadCount(context);
        if (count >= 100) {
            builder.setContentText("收到99+条新消息");
        } else if (count > 0) {
            builder.setContentText("收到" + WTContactUtils.getAllContactsUnreadCount(context) + "条新消息");
        } else {
            builder.setContentText("收到新消息");
        }
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setContentIntent(pendingIntent3);
        return builder.build();
    }


}
