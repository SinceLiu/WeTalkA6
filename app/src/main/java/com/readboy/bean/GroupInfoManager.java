package com.readboy.bean;

import android.app.readboy.IReadboyWearListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.provider.WeTalkContract.GroupColumns;
import com.readboy.utils.JsonMapper;
import com.readboy.utils.WearManagerProxy;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author oubin
 * @date 2019/1/5
 */
public class GroupInfoManager {
    private static final String TAG = "hwj_GroupInfo";

    private static final String PREFERENCES_NAME = "groupInfo";
    private static final Uri CONTENT_URI = GroupColumns.CONTENT_URI;

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void saveGroupInfo(Context context, GroupInfo info) {
        if (info == null) {
            Log.w(TAG, "saveGroupInfo: response = null.");
            return;
        }
        saveGroupInfoToDatabase(context, info);
//        saveGroupInfoToPreferences(context, info);
    }

    private static void saveGroupInfoToPreferences(Context context, GroupInfo info) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(info.getId(), JsonMapper.toJson(info));
        editor.apply();
    }

    public static GroupInfo getDataFormDatabase(Context context, String uuid) {
        String selection = GroupColumns.UUID + "=?";
        try (Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, selection,
                new String[]{uuid}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return GroupInfo.createGroupInfo(cursor);
            }
        }
        Log.i(TAG, "getDataFormDatabase: cursor is null.");
        return null;
    }

    private static Uri saveGroupInfoToDatabase(Context context, GroupInfo info) {
        ContentValues values = info.createContentValues();
        Uri result = context.getContentResolver().insert(CONTENT_URI, values);
        Log.i(TAG, "saveGroupInfoToDatabase: result = " + result);
        return result;
    }

    private static GroupInfo getDataFromPreferences(Context context, String uuid) {
        String data = getPreferences(context).getString(uuid, "");
        Log.i(TAG, "getDataFromPreferences: data = " + data);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        return JsonMapper.fromJson(data, GroupInfo.class);
    }

    public static void getGroupInfo(Context context, String uuid, final CallBack callBack) {
//        GroupInfo data = getDataFromPreferences(context, uuid);
        GroupInfo data = getDataFormDatabase(context, uuid);
        if (data != null && false) {
            callBack.onSuccess(data);
        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", uuid);
                WearManagerProxy.groupAction(context, WearManagerProxy.Command.GET_GROUP,
                        jsonObject.toString(), new IReadboyWearListener.Stub() {
                            @Override
                            public void pushSuc(String cmd, String serial, int code, String data, String result) {
                                Log.i(TAG, "pushSuc() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", data = " + data + ", result = " + result + "");
                                if (code == 0) {
                                    GroupInfo info = JsonMapper.fromJson(data, GroupInfo.class);
                                    saveGroupInfo(context, JsonMapper.fromJson(data, GroupInfo.class));
                                    callBack.onSuccess(info);
                                } else {
                                    callBack.onFailure(new Exception("code:" + code));
                                }
                            }

                            @Override
                            public void pushFail(String cmd, String serial, int code, String errorMsg) {
                                Log.i(TAG, "pushFail() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", errorMsg = " + errorMsg + "");
                                callBack.onFailure(new Exception(errorMsg));
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
                callBack.onFailure(e);
            }
        }
    }

    public static void getGroupInfoFromNet(Context context, String uuid, CallBack callBack) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", uuid);
            WearManagerProxy.groupAction(context, WearManagerProxy.Command.GET_GROUP,
                    jsonObject.toString(), new IReadboyWearListener.Stub() {
                        @Override
                        public void pushSuc(String cmd, String serial, int code, String data, String result) {
                            Log.i(TAG, "pushSuc() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", data = " + data + ", result = " + result + "");
                            Log.i(TAG, "pushSuc: thread " + Thread.currentThread());
                            if (code == 0) {
                                GroupInfo info = JsonMapper.fromJson(data, GroupInfo.class);
                                saveGroupInfo(context, JsonMapper.fromJson(data, GroupInfo.class));
                                callBack.onSuccess(info);
                            } else {
                                callBack.onFailure(new Exception("code:" + code));
                            }
                        }

                        @Override
                        public void pushFail(String cmd, String serial, int code, String errorMsg) {
                            Log.i(TAG, "pushFail() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", errorMsg = " + errorMsg + "");
                            callBack.onFailure(new Exception(errorMsg));
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.onFailure(e);
        }
    }

    public interface CallBack {
        /**
         * 数据库获取，或者网络获取.
         *
         * @param info GroupInfoManager
         */
        void onSuccess(GroupInfo info);

        /**
         * 请求失败
         *
         * @param exception 异常
         */
        void onFailure(Exception exception);
    }

}
