package com.readboy.wetalk;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.readboy.bean.Constant;
import com.readboy.receiver.MessageReceiver;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.WTContactUtils;

public class FirstStartActivity extends BaseRequestPermissionActivity {

	private NetWorkUtils mNetWorkUtils;
	
	private MPrefs mPrefs;
	
	private static final String RB_UPDATE_PHOTO_PER_HOUR = "RB_UPDATE_PHOTO_PER_HOUR";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogInfo.i("hwj","FirstStartActivity ---------- onCreate");
		super.onCreate(savedInstanceState);
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
	
	private void initDeviceInfo(){
    	if(TextUtils.isEmpty(mPrefs.getDeviceId()) ||
    			TextUtils.isEmpty(mPrefs.getNickName()) ||
    			TextUtils.isEmpty(mPrefs.getMessageTag())){
    		//获取手表设备信息
            mPrefs.setDeviceId(mNetWorkUtils.getDevicedUuid());
            mPrefs.setNickName(mNetWorkUtils.getNickName());
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
				intent.putExtra(Constant.FRIEND_ID, id);
				intent.putExtra(Constant.FRIEND_NAME, WTContactUtils.getNameById(this, id));
				intent.putExtra(Constant.FRIEND_UNREAD_COUNT, WTContactUtils.getFriendUnreadCount(this, id));
			}
		}
		startActivity(intent);
		finish();
	}

	@Override
	protected void initContent() {
		mPrefs = MPrefs.getInstance(this);
		mPrefs.setNotificationType(false);
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
