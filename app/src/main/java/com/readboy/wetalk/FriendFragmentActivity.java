package com.readboy.wetalk;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.service.MessageService;
import com.readboy.utils.AudioUtils;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.wetalk.support.WetalkFragment;

/**
 * @author oubin
 * @date 2019/02/18
 */
public class FriendFragmentActivity extends BaseFragmentActivity {
    private static final String TAG = "hwj_FriendActivity";
    private NetWorkUtils mNetWorkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogInfo.i(TAG, " --- onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_friend);
        initFragment();
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_wetalk);
//        if (fragment != null) {
//            Bundle bundle = new Bundle();
//            bundle.putBoolean(WetalkFragment.KEY_HAD_TITLE, false);
//            fragment.setArguments(bundle);
//        } else {
//            Log.i(TAG, "onCreate: fragment = null.");
//        }
    }

    private void initFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        WetalkFragment fragment = WetalkFragment.newInstance(false);
        transaction.replace(R.id.fragment_wetalk, fragment);
        transaction.commit();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        Log.i(TAG, "onEnterAnimationComplete: ");
        AudioUtils.requestAudioFocus(this);
    }

    @Override
    protected void onResume() {
        LogInfo.i(TAG, " FriendActivity --- onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogInfo.i(TAG, " FriendActivity --- onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogInfo.i(TAG, " FriendActivity --- onDestroy()");
        super.onDestroy();
        MPrefs.setNotificationType(this, true);
        AudioUtils.abandonAudioFocus(this);
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
        MPrefs.setNotificationType(this, false);
        mNetWorkUtils = NetWorkUtils.getInstance(this);
        initDeviceInfo();
    }

    private void initDeviceInfo() {
        if (TextUtils.isEmpty(MPrefs.getDeviceId(this)) ||
                TextUtils.isEmpty(MPrefs.getNickName(this)) ||
                TextUtils.isEmpty(MPrefs.getMessageTag(this))) {
            //获取手表设备信息
            MPrefs.setDeviceId(this, mNetWorkUtils.getDeviceUuid());
            MPrefs.setNickName(this, mNetWorkUtils.getNickName());
            mNetWorkUtils.saveMessageTag(this);
//            MessageService.getAllMessage(this);
        }
    }
}
