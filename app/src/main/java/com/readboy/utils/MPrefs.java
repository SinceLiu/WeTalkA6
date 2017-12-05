package com.readboy.utils;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.SharedPreferences;

import com.readboy.bean.Constant;

/**
 * Created by hwwjian on 2017/1/16.
 */

public class MPrefs {

    private static MPrefs mPrefs;
    private SharedPreferences mSp;
    
    public static final String APP_NAME = "weTalk";
    public static final String LAST_POSITION = "last";
    public static final String MESSAGE_TAG = "tag";
    public static final String IS_REFRESH_UNREAD_COUNT = "refresh_unread_count";
    public static final String DEVICE_ID = "deviceId";
    public static final String NICKNAME = "nick_name";
    public static final String DEVICE_MODE = "device_mode";
    public static final String UPDATE_UNREAD_COUNT = "update_unread_count";
    public static final String CURRENT_FFRIEND_ID = "current_friend_id";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String USER = "user";

    public static final String IS_FLOAT_VIEW = "float";
    
    public static Context mContext;
    
    private MPrefs(Context context){
        mSp = context.getSharedPreferences(Constant.APP_NAME,MODE_PRIVATE);
        mContext = context;
    }

    public static MPrefs getInstance(Context context){
        if(mPrefs == null){
            mPrefs = new MPrefs(context);
        }
        return mPrefs;
    }

    public SharedPreferences.Editor getEditor(){
        return mSp.edit();
    }

    public SharedPreferences getSharePreference(){
        return mSp;
    }
    
    /**
     * 获取服务器消息标识
     * @return
     */
    public String getMessageTag(){
//        return mSp.getString(MESSAGE_TAG,"");
    	return IOs.readTimeTag(mContext);
    }

    public void setMessageTag(String tag){
//        mSp.edit().putString(MESSAGE_TAG,tag).apply();
    	IOs.saveTimeTag(mContext, tag);
    }
    
    /**
     * 设备信息
     * @param id
     */
    public void setDeviceId(String id){
    	mSp.edit().putString(DEVICE_ID,id).apply();
    }
    
    public String getDeviceId(){
    	return mSp.getString(DEVICE_ID, "");
    }
    
    public void setNickName(String name){
    	mSp.edit().putString(NICKNAME, name).apply();
    }
    
    public String getNickName(){
    	return mSp.getString(NICKNAME, "");
    }
    
    public String getHomeGroupId(){
    	return "G" + getDeviceId();
    }
    
    /**
     * 判断是否处于好友界面,通知栏样式更改
     * @param state
     */
    public void setNotificationType(boolean floating){
    	mSp.edit().putBoolean(IS_FLOAT_VIEW, floating).apply();
    }
    
    public boolean getNotificationType(){
    	return mSp.getBoolean(IS_FLOAT_VIEW, true);
    }
    
}
