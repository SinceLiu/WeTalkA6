package com.readboy.wetalk;

import java.io.File;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.readboy.bean.Constant;
import com.readboy.receiver.MessageReceiver;
import com.readboy.utils.AudioUtils;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.ToastUtils;
import com.readboy.wetalk.utils.WTContactUtils;

/**
 * Created by hwj on 2017/3/21.
 * @deprecated
 * @author hwj
 */
public class FirstStartActivity extends BaseRequestPermissionActivity {
    private static final String TAG = "hwj_FirstStartActivity";

    private static final String RB_UPDATE_PHOTO_PER_HOUR = "RB_UPDATE_PHOTO_PER_HOUR";
    private static final int UPDATE_CYCLE = 60 * 60_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogInfo.i("hwj", "FirstStartActivity ---------- onCreate");
//		startUpdateContactService();

    }

    private void startUpdateContactService() {
        try {
            long last = Settings.Global.getLong(getContentResolver(), RB_UPDATE_PHOTO_PER_HOUR, 0);
            Log.e(TAG, "startUpdateContactService() called: last = " + last);
            if ((System.currentTimeMillis() - last) > UPDATE_CYCLE && !WeTalkApplication.IS_TEST_MODE) {
                Intent serviceIntent = new Intent(this, UpdateContactPhotoService.class);
                startService(serviceIntent);
                Settings.Global.putLong(getContentResolver(), RB_UPDATE_PHOTO_PER_HOUR, System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startUpdateContactService: e = " + e.toString());
        }
    }

    private Intent getAppDetailSettingIntent() {
        Log.e(TAG, "getAppDetailSettingIntent: ");
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }

    private void initDeviceInfo() {
        if (TextUtils.isEmpty(MPrefs.getDeviceId(this)) ||
                TextUtils.isEmpty(MPrefs.getNickName(this)) ||
                TextUtils.isEmpty(MPrefs.getMessageTag(this))) {
            //获取手表设备信息
            MPrefs.setDeviceId(this, NetWorkUtils.getDeviceUuid(this));
            MPrefs.setNickName(this, NetWorkUtils.getNickName(this));
            NetWorkUtils.saveMessageTag(this);
        }
    }

    private void initIntentData(Class<?> class1) {
        Intent intent = new Intent(this, class1);
        Intent dataIntent = getIntent();
        if (dataIntent != null) {
            //图库分享照片
            String path = dataIntent.getStringExtra("share_image");
            if (!TextUtils.isEmpty(path)) {
                LogInfo.i("分享图片的路径:" + path);
                intent.putExtra("share_image", path);
            }

            //联系人微聊
            String id = dataIntent.getStringExtra("friend_id");
            if (!TextUtils.isEmpty(id)) {
                intent.putExtra(Constant.EXTRA_FRIEND_ID, id);
                intent.putExtra(Constant.EXTRA_FRIEND_NAME, WTContactUtils.getNameById(this, id));
                intent.putExtra(Constant.FRIEND_UNREAD_COUNT, WTContactUtils.getUnreadMessageCount(this, id));
            }
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void initContent() {

        startUpdateContactService();

        MPrefs.setNotificationType(this, false);
        initDeviceInfo();
        Intent intent = getIntent();
        String path = intent.getStringExtra("share_image");
        String uuid = intent.getStringExtra("friend_id");
        if (!TextUtils.isEmpty(path)) {
            if (!new File(path).exists()) {
                ToastUtils.show(this, R.string.share_error);
                finish();
            }
            initIntentData(ChooseFriendActivity.class);
        } else if (!TextUtils.isEmpty(uuid)) {
            if (!TextUtils.isEmpty(WTContactUtils.getNameById(this, uuid))) {
                initIntentData(ConversationActivity.class);
            } else {
                ToastUtils.show(this, R.string.contact_fail);
                finish();
            }
        } else {
            initIntentData(FriendFragmentActivity.class);
        }
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }
}
