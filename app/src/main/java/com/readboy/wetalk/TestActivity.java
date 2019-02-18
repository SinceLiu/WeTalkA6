package com.readboy.wetalk;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.LocalActivityManager;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ListView;

import com.readboy.receiver.MessageService;
import com.readboy.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author oubin
 * @date 2018/1/18
 */

public class TestActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String str = "";
        StringBuilder builder = new StringBuilder();
        builder.append("test");
        builder.delete(0, 12);

    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
    }
}
