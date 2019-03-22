package com.readboy.utils;

import android.app.readboy.IReadboyWearListener;
import android.app.readboy.ReadboyWearManager;
import android.content.Context;
import android.os.Environment;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解决微聊消息tag丢失，或没有的问题
 *
 * @author oubin
 * @date 2019/3/20
 */
public final class WeTalkUtil {
    private static final String TAG = "WeTalkUtil";

    /**
     * 用于保存上次获取最新消息的时间的文件名，内容为单行时间戳，单位为毫秒
     */
    private final static String FILE_NAME = Environment.getExternalStorageDirectory() + "/get_message_time.txt";

    public static void checkoutMessageTag(Context context) {
        Log.i(TAG, "checkoutMessageTag: ");
        String tag = readTimeTag();
        if (!TextUtils.isEmpty(tag)) {
            return;
        }
        Log.i(TAG, "checkoutMessageTag: tag = " + tag);
        ReadboyWearManager manager = getManager(context);
        manager.getAllMessage("", new IReadboyWearListener.Stub() {
            @Override
            public void pushSuc(String cmd, String serial, int code, String data, String result) throws RemoteException {
                Log.i(TAG, "pushSuc: ");
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String tag = jsonObject.optString("t");
                    Log.i(TAG, "pushSuc: tag = " + tag);
                    if (!TextUtils.isEmpty(tag)) {
                        saveTimeTag(tag);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void pushFail(String cmd, String serial, int code, String errorMsg) throws RemoteException {
                Log.i(TAG, "pushFail: " + errorMsg);
            }
        });

    }

    private static ReadboyWearManager getManager(Context context) {
        return (ReadboyWearManager) context.getSystemService(Context.RBW_SERVICE);
    }

    /**
     * 保存消息时间标记到文件
     */
    private static boolean saveTimeTag(String content) {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(TAG, "saveTimeTag: cannot create new file, filename = " + FILE_NAME);
                    return false;
                }
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "saveTimeTag: e = " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "saveTimeTag: e = " + e.toString());
        }
        return false;
    }

    /**
     * 从文件从获取时间标记
     */
    private static String readTimeTag() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                Log.e(TAG, "readTimeTag: file is not exit");
                return "";
            }
            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }
            inputStream.close();
            arrayOutputStream.close();
            String content = new String(arrayOutputStream.toByteArray());
            String regEx = "[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(content);
            content = m.replaceAll("").trim();
//            Log.i("hwj", "time tag : " + content);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "readTimeTag: tag = null.");
        return "";
    }
}
