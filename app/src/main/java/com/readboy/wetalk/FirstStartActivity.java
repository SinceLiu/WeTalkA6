package com.readboy.wetalk;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.readboy.bean.Constant;
import com.readboy.receiver.MessageReceiver;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.WTContactUtils;

public class FirstStartActivity extends BaseRequestPermissionActivity {
	private static final String TAG = "FirstStartActivity";


	private NetWorkUtils mNetWorkUtils;
	
	private static final String RB_UPDATE_PHOTO_PER_HOUR = "RB_UPDATE_PHOTO_PER_HOUR";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogInfo.i("hwj","FirstStartActivity ---------- onCreate");
//		startUpdateContactService();

	}

	private void startUpdateContactService() {
		try {
			long last = Settings.System.getLong(getContentResolver(), RB_UPDATE_PHOTO_PER_HOUR,0);
			if((System.currentTimeMillis() - last) > 60 * 60 * 1000 && !WeTalkApplication.IS_TEST_MODE){
				Intent serviceIntent = new Intent(this, UpdateContactPhotoService.class);
				startService(serviceIntent);
				Settings.System.putLong(getContentResolver(), RB_UPDATE_PHOTO_PER_HOUR, System.currentTimeMillis());
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	private void initDeviceInfo(){
    	if(TextUtils.isEmpty(MPrefs.getDeviceId(this)) ||
    			TextUtils.isEmpty(MPrefs.getNickName(this)) ||
    			TextUtils.isEmpty(MPrefs.getMessageTag(this))){
    		//获取手表设备信息
            MPrefs.setDeviceId(this, mNetWorkUtils.getDeviceUuid());
            MPrefs.setNickName(this, mNetWorkUtils.getNickName());
            mNetWorkUtils.saveMessageTag(this);
    	}
    }
	
	private void initIntentData(Class<?> class1){
		Intent intent = new Intent(this,class1);
		Intent dataIntent = getIntent();
		if(dataIntent != null){
			//图库分享照片
			String path = dataIntent.getStringExtra("share_image");
			if(!TextUtils.isEmpty(path)){
				LogInfo.i("分享图片的路径:" + path);
				intent.putExtra("share_image", path);
			}
			
			//联系人微聊
			String id = dataIntent.getStringExtra("friend_id");
			if(!TextUtils.isEmpty(id)){
				intent.putExtra(Constant.EXTRA_FRIEND_ID, id);
				intent.putExtra(Constant.EXTRA_FRIEND_NAME, WTContactUtils.getNameById(this, id));
				intent.putExtra(Constant.FRIEND_UNREAD_COUNT, WTContactUtils.getFriendUnreadCount(this, id));
			}
		}
		startActivity(intent);
		finish();
	}

	@Override
	protected void initContent() {

		startUpdateContactService();

		MPrefs.setNotificationType(this, false);
		mNetWorkUtils = NetWorkUtils.getInstance(this);
        MessageReceiver.getAllMessage(this);
        initDeviceInfo();
		Intent intent = getIntent();
		String path = intent.getStringExtra("share_image");
		String uuid = intent.getStringExtra("friend_id");
		if(!TextUtils.isEmpty(path)){
			if(!new File(path).exists()){
				Toast.makeText(this, getResources().getString(R.string.share_error), Toast.LENGTH_SHORT).show();
				finish();
			}
			initIntentData(ChooseFriendActivity.class);
		}else if(!TextUtils.isEmpty(uuid)){
			if(!TextUtils.isEmpty(WTContactUtils.getNameById(this, uuid))){
				initIntentData(ConversationActivity.class);			
			}else{
				Toast.makeText(this, getResources().getString(R.string.contact_fail), Toast.LENGTH_SHORT).show();
				finish();
			}
		}else{
			initIntentData(FriendActivity.class);
		}
	}

	@Override
	protected void initView() {
		
	}

	@Override
	protected void initData() {
		
	}
}
