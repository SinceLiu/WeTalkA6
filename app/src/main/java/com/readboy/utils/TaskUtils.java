package com.readboy.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class TaskUtils {

	public static boolean isBackground(Context context) {
	    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    for (RunningAppProcessInfo appProcess : appProcesses) {
	         if (appProcess.processName.equals(context.getPackageName())) {
	                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
	                	LogInfo.i("hwj", "background --- " + appProcess.processName);
	                    return true;
	                }else{
	                	LogInfo.i("hwj", "foregraound --- " + appProcess.processName);
	                    return false;
	                }
	           }
	    }
	    return false;
	}

}
