package com.readboy.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.net.ParseException;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.activity.RequestFriendActivity;
import com.readboy.provider.Profile;
import com.readboy.wetalk.FriendActivity;
import com.readboy.wetalk.R;
import com.readboy.wetalk.utils.WTContactUtils;
import com.tencent.bugly.crashreport.CrashReport;

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
    private static final String CHANCEL_ID_NORMAL = "normal";
    private static final String CHANCEL_ID_FLOAT = "float";

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
            Log.i(TAG, "notification: notify silent notification.");
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
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder = new NotificationCompat.Builder(context, Chancel.FLOAT.id);
            createChannel(context, Chancel.FLOAT);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setChannelId(CHANCEL_ID_FLOAT);
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
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) {
            builder = new NotificationCompat.Builder(context, Chancel.NORMAL.id);
            createChannel(context, Chancel.NORMAL);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setChannelId(CHANCEL_ID_NORMAL);
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
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder = new NotificationCompat.Builder(context, Chancel.SILENT.id);
            createChannel(context, Chancel.SILENT);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
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

    private static void createChannel(Context context, Chancel chancel) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(chancel.id, chancel.name, chancel.importance);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    public static void sendContactNotification(Context context, Profile profile) {
        Log.i(TAG, "sendContactNotification: uuid = " + profile.uuid);
        NotificationCompat.Builder builder = getBaseBuilder(context);
        builder.setSmallIcon(R.drawable.icon_findfriend);
        builder.setContentTitle("新好友");
        builder.setContentText(context.getResources().getString(R.string.request_friend_content, profile.getName()));
        Intent intent = new Intent(context, RequestFriendActivity.class);
        intent.putExtra(RequestFriendActivity.EXTRA_PROFILE, profile);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                profile.uuid.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(profile.uuid.hashCode(), notification);
        Log.i(TAG, "sendContactNotification: profile = " + profile.getName());
    }

    private static NotificationCompat.Builder getBaseBuilder(Context context) {
        return getBaseBuilder(context, Chancel.FLOAT);
    }

    private static NotificationCompat.Builder getBaseBuilder(Context context, Chancel chancel) {
        Bundle bundle = new Bundle();
        bundle.putString("extra_type", "readboy");
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && chancel != null) {
            builder = new NotificationCompat.Builder(context, chancel.id);
            createChannel(context, chancel);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setExtras(bundle);
        builder.setAutoCancel(false);
        builder.setContentTitle("微聊");
        builder.setSmallIcon(R.drawable.wetalk_icon);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setPriority(Notification.PRIORITY_MAX);
        return builder;
    }

    /**
     * 不用判断是否在上课禁用期间，SystemUI统一处理，
     * 期间，不主动亮屏，无振动，有弹窗，更新通知栏信息。
     */
    public static void sendNotification(Context context) {
        //在聊天界面的用户不是发送用户,就更新未读信息数
        //未开启上课禁用, 不用处理，NotificationManager统一处理。
//            if (!isTimeEnable(context, classState)) {
        Log.e(TAG, "sendNotification: ");
        notification(context);
//            } else {
//                notification(false, context);
//            }

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

    /**
     * 更新联系人未读信息数
     *
     * @param uuid
     * @return 受影响行数
     */
    public static void clearFriendUnreadCount(final Context context, final String uuid) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NotificationUtils.NOTIFY_ID);
        manager.cancel(NotificationUtils.NORMAL_NOTIFY_ID);
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
        values.put("data6", 0);
        try {
            context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, "data8 = ?",
                    new String[]{uuid});
        } catch (SQLiteException e) {
            CrashReport.postCatchedException(e);
        }
    }

    public static void cancelNotification(Context context, int id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

    /**
     * NotificationChancel
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private enum Chancel {
        /**
         * 通用
         */
        NORMAL("normal", "通用", NotificationManager.IMPORTANCE_DEFAULT),
        FLOAT("float", "弹窗", NotificationManager.IMPORTANCE_HIGH),
        SILENT("silent", "静默", NotificationManager.IMPORTANCE_LOW);
        public String id;
        public String name;
        public int importance;

        Chancel(String id, String name, int importance) {
            this.id = id;
            this.name = name;
            this.importance = importance;
        }
    }

}
