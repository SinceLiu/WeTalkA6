package com.readboy.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.readboy.bean.Constant;

/**
 *
 * @author hwwjian
 * @date 2017/1/16
 */

public class MPrefs {

    public static final String APP_NAME = "weTalk";
    public static final String LAST_POSITION = "last";
    public static final String MESSAGE_TAG = "tag";
    public static final String IS_REFRESH_UNREAD_COUNT = "refresh_unread_count";
    private static final String DEVICE_ID = "deviceId";
    private static final String NICKNAME = "nick_name";
    public static final String DEVICE_MODE = "device_mode";
    public static final String UPDATE_UNREAD_COUNT = "update_unread_count";
    public static final String CURRENT_FFRIEND_ID = "current_friend_id";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String USER = "user";

    public static final String IS_FLOAT_VIEW = "float";

    private MPrefs() throws IllegalAccessException {
        throw new IllegalAccessException("u can not create me.");
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getSharePreference(context).edit();
    }

    public static SharedPreferences getSharePreference(Context context) {
        return context.getSharedPreferences(Constant.APP_NAME, MODE_PRIVATE);
    }

    /**
     * 获取服务器消息标识
     */
    public static String getMessageTag(Context context) {
//        return mSp.getString(MESSAGE_TAG,"");
        return IOs.readTimeTag(context);
    }

    public static void setMessageTag(Context context, String tag) {
//        mSp.edit().putString(MESSAGE_TAG,tag).apply();
        IOs.saveTimeTag(context, tag);
    }

    /**
     * 设备信息
     *
     * @param id
     */
    public static void setDeviceId(Context context, String id) {
        getSharePreference(context).edit().putString(DEVICE_ID, id).apply();
    }

    public static void setDeviceId(SharedPreferences preferences, String id){
        preferences.edit().putString(DEVICE_ID, id).apply();
    }

    public static String getDeviceId(Context context) {
        return getSharePreference(context).getString(DEVICE_ID, "");
    }

    public static void setNickName(Context context, String name) {
        getSharePreference(context).edit().putString(NICKNAME, name).apply();
    }

    public static String getNickName(Context context) {
        return getSharePreference(context).getString(NICKNAME, "");
    }

    public static String getHomeGroupId(Context context) {
        return "G" + getDeviceId(context);
    }

    /**
     * 判断是否处于好友界面,通知栏样式更改
     */
    public static void setNotificationType(Context context, boolean floating) {
        getSharePreference(context).edit().putBoolean(IS_FLOAT_VIEW, floating).apply();
    }

    public static boolean getNotificationType(Context context) {
        return getSharePreference(context).getBoolean(IS_FLOAT_VIEW, true);
    }

}
