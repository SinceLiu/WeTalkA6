package com.readboy.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.wetalk.FriendActivity;
import com.readboy.wetalk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hwwjian on 2016/12/21.
 * 发通知的情况:
 * 1.非微聊界面收到消息,正常通知(声音,震动,弹出)
 * 2.微聊好友界面(震动)
 * 3.当前好友聊天界面(震动)
 * 4.非当前好友聊天界面(震动)
 */

public class NotificationUtils {
    private static final String TAG = "hwj_NotificationUtils";

    private static final int MAX_MESSAGE_COUNT = 100;

    public static final int NOTIFY_ID = "wetlak".hashCode();

    //"normal_wetalk".hashCode();
    public static final int NORMAL_NOTIFY_ID = "wetlak".hashCode();

    private static void notification(Context context) {
        notification(false, context);
    }

    private static void notification(boolean isSilent, Context context) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //在联系人信息界面或者
        if (isSilent) {
            manager.notify(NOTIFY_ID, getSilentNotification(context));
        } else if (!TaskUtils.isBackground(context)) {
            Log.e(TAG, "notification: normal. only vibrate.");
            manager.notify(NORMAL_NOTIFY_ID, getNotification(context));
        } else {
            Log.e(TAG, "notification: floating. sound and vibrate.");
            manager.notify(NOTIFY_ID, getFloatingNotification(context));
        }
    }

    /**
     * sound and vibrate
     */
    private static Notification getFloatingNotification(Context context) {
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, new Intent(context, FriendActivity.class), 0);
        Bundle bundle = new Bundle();
        bundle.putString("extra_type", "readboy");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setExtras(bundle);
        int count = WTContactUtils.getAllContactsUnreadCount(context);
        Log.e(TAG, "getFloatingNotification: count = " + count);
        if (count >= MAX_MESSAGE_COUNT) {
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

    /**
     * only vibrate.
     */
    private static Notification getNotification(Context context) {
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, new Intent(context, FriendActivity.class), 0);
        Bundle bundle = new Bundle();
        bundle.putString("extra_type", "readboy");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setExtras(bundle);
        int count = WTContactUtils.getAllContactsUnreadCount(context);
        if (count >= MAX_MESSAGE_COUNT) {
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

    /**
     * 针对上课禁用类型，静默，无振动，无响铃，无弹窗，只更新通知栏
     */
    private static Notification getSilentNotification(Context context) {
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, new Intent(context, FriendActivity.class), 0);
        Bundle bundle = new Bundle();
        bundle.putString("extra_type", "readboy");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setExtras(bundle);
        int count = WTContactUtils.getAllContactsUnreadCount(context);
        if (count >= MAX_MESSAGE_COUNT) {
            builder.setContentText("收到99+条新消息");
        } else if (count > 0) {
            builder.setContentText("收到" + WTContactUtils.getAllContactsUnreadCount(context) + "条新消息");
        } else {
            builder.setContentText("收到新消息");
        }
//        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setPriority(Notification.PRIORITY_LOW);
        builder.setContentIntent(pendingIntent3);
        return builder.build();
    }

    /**
     * 不用判断是否在上课禁用期间，SystemUI统一处理，
     * 期间，不主动亮屏，无振动，有弹窗，更新通知栏信息。
     */
    public static void sendNotification(Context context) {
        //在聊天界面的用户不是发送用户,就更新未读信息数
        String classState = Settings.Global.getString(context.getContentResolver(), "class_disabled");
        LogInfo.i("hwj", "classState = " + classState);
        if (!TextUtils.isEmpty(classState)) {
            //未开启上课禁用
//            if (!isTimeEnable(context, classState)) {
                Log.e(TAG, "sendNotification: ");
                notification(context);
//            } else {
//                notification(false, context);
//            }
        } else {
            notification(context);
        }
    }

    /**
     * 判断data时间是否在上课禁用时间段内。
     *
     * @param data 当前时间
     */
    private static boolean isTimeEnable(Context context, String data) {
        long time = System.currentTimeMillis();
        SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
        boolean isEnable = false;
        boolean isWeekEnable = false;
        boolean isTimeEnable = false;
        boolean isSingleTime = false;
        try {
            Date date = new Date(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            long startSetTime = Settings.Global.getLong(context.getContentResolver(), "class_disable_time", 0);
            Date startSetData = new Date(startSetTime);
            boolean isSameDay = isSameDay(date, startSetData);
            int week = (date.getDay() + 6) % 7;
            week = 1 << (6 - week);
            JSONObject jsonObject = new JSONObject(data);
            isEnable = jsonObject.optBoolean("enabled", false);
            String repeatStr = jsonObject.optString("repeat", "0000000");
            int repeatWeek = Integer.parseInt(repeatStr, 2);
            isSingleTime = isSameDay && (repeatWeek == 0);
            isWeekEnable = (week & repeatWeek) != 0;
            JSONArray jsonArray = jsonObject.optJSONArray("time");
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonSun = jsonArray.getJSONObject(i);
                String startTime = jsonSun.optString("start", "00:00");
                String endTime = jsonSun.optString("end", "00:00");
                String nowTime = mDateFormat.format(date);
                Date date1 = mDateFormat.parse(startTime.trim());
                Date date2 = mDateFormat.parse(endTime.trim());
                Date dateNow = mDateFormat.parse(nowTime.trim());
                if (dateNow.getTime() >= date1.getTime() && dateNow.getTime() < date2.getTime()) {
                    isTimeEnable = true;
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isEnable && (isWeekEnable || isSingleTime) && isTimeEnable;
    }

    private static boolean isSameDay(Date day1, Date day2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        String ds1 = sdf.format(day1);
        String ds2 = sdf.format(day2);
        return ds1.equals(ds2);
    }

}
