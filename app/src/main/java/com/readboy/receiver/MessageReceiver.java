package com.readboy.receiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.provider.ConversationProvider;
import com.readboy.provider.Conversations;
import com.readboy.utils.EmojiUtils;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.NetWorkUtils.PushResultListener;
import com.readboy.utils.NotificationUtils;
import com.readboy.utils.WTContactUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Created by hwwjian on 2016/12/14.
 * 获取消息
 */

public class MessageReceiver extends BroadcastReceiver{

	public static final String READBOY_ACTION_NOTIFY_MESSAGE = "readboy.action.NOTIFY_MESSAGE";
	public static final String READBOY_ACTION_SEND_CAPTURE = "com.readboy.action.SENDPICTURE";
	
    private static final int DOWNLOAD_MSG = 0x13;
    private static Context mContext;
    
    private static boolean isGettingMessage = false;
    private static boolean isNotify = false;
    
    private static NetWorkUtils mNetWorkUtils;

    private static Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == DOWNLOAD_MSG){
                mNetWorkUtils.downLoadVoiceFile(mContext,(Conversation) msg.obj);
            }else if(msg.what == NetWorkUtils.UPLOAD_SUCCEED){
            	LogInfo.i("hwj","upload capture succeed");
            }else if(msg.what == NetWorkUtils.UPLOAD_FAIL){
            	LogInfo.i("hwj","upload capture fail");
            }
        }
    };

    @Override
    public void onReceive(final Context context, Intent intent) {
    	String action = intent.getAction();
    	if(TextUtils.isEmpty(action)){
    		return;
    	}
    	switch (action) {
		case READBOY_ACTION_NOTIFY_MESSAGE://收到消息
			isNotify = true;
			LogInfo.i("hwj","isGettingMessage : " + isGettingMessage);
			if(!isGettingMessage) {
				LogInfo.i("hwj","------------------- getAllMessage start");
	    		getAllMessage(context);
	    		LogInfo.i("hwj","------------------- getAllMessage finish");
	        }
			break;
		case READBOY_ACTION_SEND_CAPTURE://发送监拍指令
			String path = intent.getStringExtra("picture_path");
			ArrayList<String> ids = intent.getStringArrayListExtra("capture_uuid");
			if(TextUtils.isEmpty(path) || ids == null){
				return;
			}
			NetWorkUtils.getInstance(context).uploadCaptureFile(ids, path, mHandler);
			break;
		default:
			break;
		}
    }

    public static void getAllMessage(final Context context) {
        final MPrefs mPrefs = MPrefs.getInstance(context);
        mContext = context;
        mNetWorkUtils = NetWorkUtils.getInstance(context);
        isGettingMessage = true;
        isNotify = false;
        mNetWorkUtils.getAllMessage(mPrefs.getMessageTag(), new PushResultListener() {
			
			@Override
			public void pushSucceed(String type, String s1, int code, String s,
					String response) {
				LogInfo.i("hwj","receive message respon:" + response);
				try {
                    JSONObject jsonObject = new JSONObject(response);
                    mPrefs.setMessageTag(jsonObject.optString(NetWorkUtils.TIME));
                    JSONArray array = jsonObject.getJSONArray(NetWorkUtils.DATA);
                    int count = array.length();
                    if (count >= 10) {
						isNotify = true;
					}
                    boolean hasFile = false;
                    for (int i = 0 ; i < count ; i++){
                        JSONObject data = array.optJSONObject(i);
                        String msgHeader = data.optString(NetWorkUtils.HEADER);
                        String[] messageInfo = msgHeader.split("\\|");
                        final Conversation conversation = new Conversation();
                        //收件人的Id
                        conversation.recId = mPrefs.getDeviceId();
                        //真正的发件人Id
                        conversation.realSendId = messageInfo[0];
                        //发件人的Id,分为家庭圈发送和单聊发送两种
                        conversation.sendId = messageInfo[1];
                        conversation.time = String.valueOf(System.currentTimeMillis());
                        conversation.isUnread = Constant.TRUE;
                        conversation.conversationId = String.valueOf(NetWorkUtils.md5(
                        		String.valueOf(System.currentTimeMillis())));
                        //是否是家庭圈的消息,根据发件人的群Id判断
                        conversation.isHomeGroup = conversation.sendId.startsWith("G") ? Constant.TRUE : Constant.FALSE;
                        if(conversation.isHomeGroup == Constant.TRUE){
                        	conversation.senderName = WTContactUtils.getNameById(context, conversation.realSendId);
                        }else{
                        	//判断发件人是否存在通讯录中
                            if(TextUtils.isEmpty(WTContactUtils.getNameById(context,conversation.realSendId))){
                                break;
                            }else{
                            	conversation.senderName = WTContactUtils.getNameById(context, conversation.realSendId);
                            }
                        }
                        //消息类型,支持text、image、audio、video、link
                        switch (messageInfo[2]) {
                            case NetWorkUtils.TEXT:
                                String content = data.optString(NetWorkUtils.MESSAGE);
                                //表情
                                if(EmojiUtils.getEmojiId(content) != -1) {
                                    conversation.emojiCode = content;
                                    conversation.emojiId = EmojiUtils.getEmojiId(content);
                                    conversation.type = Constant.REC_EMOJI;
                                }
                                //文本
                                else {
                                    conversation.textContent = content;
                                    conversation.type = Constant.REC_TEXT;
                                }
                                addToDatabase(context, conversation);
                                break;
                            case NetWorkUtils.AUDIO:
                                conversation.lastTime = data.optJSONObject(NetWorkUtils.A)
                                        .getInt(NetWorkUtils.LENGTH);
                                conversation.voiceUrl = data.optString(NetWorkUtils.MESSAGE);
                                conversation.isUnPlay = Constant.TRUE;
                                conversation.isPlaying = Constant.FALSE;
                                conversation.type = Constant.REC_VOICE;
                                hasFile = true;
                                mHandler.obtainMessage(DOWNLOAD_MSG,conversation).sendToTarget();
                                break;
                            case NetWorkUtils.IMAGE:
                            	conversation.imageUrl = data.optJSONObject(NetWorkUtils.A).optString(NetWorkUtils.SRC);
                            	conversation.thumbImageUrl = data.optString(NetWorkUtils.MESSAGE);
                            	conversation.type = Constant.REC_IMAGE;
                            	addToDatabase(context, conversation);
                            	break;
                            case NetWorkUtils.VIDEO:break;
                            case NetWorkUtils.LINK:break;
                            default:break;
                        }
                    }
                    if(count != 0 && !hasFile){
                    	sendNotification(context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
				isGettingMessage = false;
				/**
				 * 两种情况会再次获取
				 */
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						if(isNotify){
							LogInfo.i("hwj"," --- getAllMessage again");
							getAllMessage(context);
						}						
					}
				});
			}
			
			@Override
			public void pushFail(String s, String s1, int i, String s2) {
				isGettingMessage = false;
			}
		});
    }
    
    private static void addToDatabase(Context context,Conversation conversation){
    	//插入数据库
        Uri uri = context.getContentResolver().insert(Conversations.Conversation.CONVERSATION_URI,
                ConversationProvider.getContentValue(conversation,true));
        if(uri != null) {
        	WTContactUtils.updateUnreadCount(context, conversation.sendId,1);
        }
    }
    
    private static void sendNotification(Context context){
    	//在聊天界面的用户不是发送用户,就更新未读信息数
    	String classState = Settings.Global.getString(context.getContentResolver(), "class_disabled");
    	LogInfo.i("hwj","classState = " + classState);
    	if(!TextUtils.isEmpty(classState)){
    		LogInfo.i("hwj","isNowEnable = " + isTimeEnable(classState));
    		//未开启上课禁用
			if(!isTimeEnable(classState)) {
			    NotificationUtils.notification(context);
			}
    	}else{
    		NotificationUtils.notification(context);
    	}
    }

    private static boolean isTimeEnable(String data){
    	long time = System.currentTimeMillis();
    	SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");
        boolean isEnable = false;
        boolean isWeekEnable = false;
        boolean isTimeEnable = false;
        boolean isSingleTime = false;
        try {
            Date date = new Date(time);
            long startSetTime = Settings.Global.getLong(mContext.getContentResolver(),"class_disable_time",0);
            Date startSetData = new Date(startSetTime);
            boolean isSameDay = isSameDay(date,startSetData);
            int week = (date.getDay() + 6) % 7;
            week = 1 << (6 - week);
            JSONObject jsonObject = new JSONObject(data);
            isEnable = jsonObject.optBoolean("enabled",false);
            String repeatStr = jsonObject.optString("repeat","0000000");
            int repeatWeek = Integer.parseInt(repeatStr,2);
            isSingleTime = isSameDay && (repeatWeek == 0);
            isWeekEnable = (week & repeatWeek) != 0;
            JSONArray jsonArray = jsonObject.optJSONArray("time");
            int length = jsonArray.length();
            for(int i = 0; i < length; i++){
                JSONObject jsonSun = jsonArray.getJSONObject(i);
                String startTime = jsonSun.optString("start","00:00");
                String endTime = jsonSun.optString("end","00:00");
                String nowTime = mDateFormat.format(date);
                Date date1 = mDateFormat.parse(startTime.trim());
                Date date2 = mDateFormat.parse(endTime.trim());
                Date dateNow = mDateFormat.parse(nowTime.trim());
                if(dateNow.getTime() >= date1.getTime() && dateNow.getTime() < date2.getTime()){
                    isTimeEnable = true;
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return isEnable && (isWeekEnable || isSingleTime) && isTimeEnable;
    }
    
    private static boolean isSameDay(Date day1, Date day2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ds1 = sdf.format(day1);
        String ds2 = sdf.format(day2);
        if (ds1.equals(ds2)) {
            return true;
        } else {
            return false;
        }
    }
}
