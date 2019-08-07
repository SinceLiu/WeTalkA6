package com.readboy.wetalk;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.wetalk.view.WetalkFrameLayout;

/**
 * @author hwj
 */
public class FriendActivity extends BaseRequestPermissionActivity {
    private static final String TAG = "hwj_FriendActivity";

    private WetalkFrameLayout mParent;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        LogInfo.i(TAG, " --- onCreate()");
        super.onCreate(savedInstanceState);
//        AudioUtils.requestAudioFocus(this);

    }

    @Override
    protected void initView() {
        mParent = findViewById(R.id.wetalk_parent);
        mParent.setActivity(this);

    }

    @Override
    protected void initData() {
        Log.e(TAG, "initData: ");
    }

    @Override
    protected void onResume() {
        LogInfo.i(TAG, " FriendActivity --- onResume()");
        super.onResume();
        mParent.onResume();
    }

    @Override
    protected void onPause() {
        LogInfo.i(TAG, " FriendActivity --- onPause()");
        super.onPause();
        mParent.onPause();
    }

    @Override
    protected void onDestroy() {
        LogInfo.i(TAG, " FriendActivity --- onDestroy()");
        super.onDestroy();
        mParent.onDestroy();
//        MPrefs.setNotificationType(this, true);
//        AudioUtils.abandonAudioFocus(this);
        killBackgroundProcesses();
    }

    private void killBackgroundProcesses() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (info.service != null
                    && this.getPackageName().equals(info.service.getPackageName())) {
                Log.i(TAG, "stopService: removeTask: " + info.service);
            }
        }
        manager.killBackgroundProcesses(getPackageName());
    }

    @Override
    protected void initContent() {
        setContentView(R.layout.activity_friend);
//        MPrefs.setNotificationType(this, false);
        initView();
    }
}
