package com.readboy.wetalk;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.ViewParent;

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

}
