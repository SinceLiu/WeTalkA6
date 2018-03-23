package com.readboy.utils;

import android.animation.StateListAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.TypedArrayUtils;

/**
 * @author oubin
 * @date 2018/3/15
 */

public final class PermissionUtils {

    public static boolean hadPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean getPermission(Context context, String permission){
        return true;
    }

    public static boolean requestPermission(Context context, String permission){
        return true;
    }

}
