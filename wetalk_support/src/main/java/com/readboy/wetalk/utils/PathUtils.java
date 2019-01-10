package com.readboy.wetalk.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 *
 * @author oubin
 * @date 2019/1/7
 */
public class PathUtils {

    /**
     * 路径相关
     */
    private static String getExternalFileDirectory(Context context) {
        File file = context.getExternalFilesDir(null);
        if (file != null) {
            return file.getPath();
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/com.readboy.wetalk/files";
        }
    }

    public static String getVoicePath(Context context) {
        return getExternalFileDirectory(context) + "/voice/";
    }

    public static String getImagePath(Context context) {
        return getExternalFileDirectory(context) + "/image/";
    }

    public static String getDownloadPath(Context context) {
        return getExternalFileDirectory(context) + "/download/";
    }

    public static String getAvatarPath(Context context) {
        return getExternalFileDirectory(context) + "/avatar/";
    }
}
