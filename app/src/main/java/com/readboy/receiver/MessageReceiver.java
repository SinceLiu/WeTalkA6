package com.readboy.receiver;

import java.util.ArrayList;

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
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author hwwjian
 * @date 2016/12/14
 * 获取消息
 */

public class MessageReceiver extends BroadcastReceiver {
    private static final String TAG = "hwj_MessageReceiver";

    public static final String READBOY_ACTION_NOTIFY_MESSAGE = "readboy.action.NOTIFY_MESSAGE";
    /**
     * 发送监拍命令
     */
    public static final String READBOY_ACTION_SEND_CAPTURE = "com.readboy.action.SENDPICTURE";

    private static final int DOWNLOAD_MSG = 0x13;

    /**
     * TODO: 网络慢是否会导致内容丢失，不会马上获取到最新的消息。
     */
    private static boolean isGettingMessage = false;
    private static boolean isNotify = false;

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DOWNLOAD_MSG) {
//                mNetWorkUtils.downLoadVoiceFile(mContext, (Conversation) msg.obj);
            } else if (msg.what == NetWorkUtils.UPLOAD_SUCCEED) {
                LogInfo.i("hwj", "upload capture succeed");
            } else if (msg.what == NetWorkUtils.UPLOAD_FAIL) {
                LogInfo.i("hwj", "upload capture fail");
            }
        }
    };

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: action = " + action);
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case READBOY_ACTION_NOTIFY_MESSAGE:
                //收到消息
                isNotify = true;
                LogInfo.i("hwj", "onReceive: isGettingMessage : " + isGettingMessage);
                //如果正在获取新消息，是否应已队列形式，排队获取。
                if (!isGettingMessage) {
                    LogInfo.i("hwj", "------------------- getAllMessage start");
                    getAllMessage(context);
                    LogInfo.i("hwj", "------------------- getAllMessage finish");
                } else {
                    Log.e(TAG, "onReceive: isGettingMessage = " + isGettingMessage);
                }
                break;
            case READBOY_ACTION_SEND_CAPTURE:
                //发送监拍指令
                String path = intent.getStringExtra("picture_path");
                ArrayList<String> ids = intent.getStringArrayListExtra("capture_uuid");
                if (TextUtils.isEmpty(path) || ids == null) {
                    return;
                }
                NetWorkUtils.getInstance(context).uploadCaptureFile(ids, path, mHandler);
                break;
            default:
                break;
        }
    }

    private boolean canGetMessage(Context context) {
        return Constant.ENABLE_FAKE || NetWorkUtils.isWifiConnected(context);
    }

    public static void getAllMessage(final Context context) {
        final NetWorkUtils mNetWorkUtils = NetWorkUtils.getInstance(context);
        isGettingMessage = true;
        isNotify = false;
        final String messageTag = MPrefs.getMessageTag(context);
        Log.e(TAG, "getAllMessage: messageTag = " + messageTag);
        mNetWorkUtils.getAllMessage(messageTag, new PushResultListener() {

            @Override
            public void pushSucceed(String type, String s1, int code, String s,
                                    String response) {
                LogInfo.e(TAG, "pushSucceed() called with: type = " + type + ", s1 = " + s1 +
                        ", code = " + code + ", s = " + s + ", response = " + response + "");
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    MPrefs.setMessageTag(context, jsonObject.optString(NetWorkUtils.TIME));
                    JSONArray array = jsonObject.getJSONArray(NetWorkUtils.DATA);
                    int count = array.length();
                    //校准第一次无messageTag, 但是未必可靠。
                    if (TextUtils.isEmpty(messageTag) && count == 0) {
//                        getAllMessage(context);
                    }
                    Log.e(TAG, "pushSucceed: count = " + count);
                    if (count >= 10) {
                        isNotify = true;
                    }
                    boolean hasFile = false;
                    for (int i = 0; i < count; i++) {
                        JSONObject data = array.optJSONObject(i);
                        String msgHeader = data.optString(NetWorkUtils.HEADER);
                        String[] messageInfo = msgHeader.split("\\|");
                        final Conversation conversation = new Conversation();
                        //收件人的Id
                        conversation.recId = MPrefs.getDeviceId(context);
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
                        if (conversation.isHomeGroup == Constant.TRUE) {
                            conversation.senderName = WTContactUtils.getNameById(context, conversation.realSendId);
                        } else {
                            //判断发件人是否存在通讯录中
                            if (TextUtils.isEmpty(WTContactUtils.getNameById(context, conversation.realSendId))) {
                                LogInfo.e(TAG, conversation.realSendId + " 不在通讯录里。");
                                break;
                            } else {
                                conversation.senderName = WTContactUtils.getNameById(context, conversation.realSendId);
                            }
                        }
                        //消息类型,支持text、image、audio、video、link
                        switch (messageInfo[2]) {
                            case NetWorkUtils.TEXT:
                                String content = data.optString(NetWorkUtils.MESSAGE);
                                //表情，可能会收到旧编码，来自W5.
                                int emojiId = EmojiUtils.getEmojiIdContainOldCode(content);
                                Log.e(TAG, "pushSucceed: emojiId = " + emojiId);
                                if (emojiId != -1) {
                                    conversation.emojiCode = content;
                                    conversation.emojiId = emojiId;
                                    conversation.type = Constant.REC_EMOJI;
                                } else {
                                    //文本
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
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mNetWorkUtils.downLoadVoiceFile(context, conversation);
                                    }
                                });
                                break;
                            case NetWorkUtils.IMAGE:
                                conversation.imageUrl = data.optJSONObject(NetWorkUtils.A).optString(NetWorkUtils.SRC);
                                conversation.thumbImageUrl = data.optString(NetWorkUtils.MESSAGE);
                                conversation.type = Constant.REC_IMAGE;
                                addToDatabase(context, conversation);
                                break;
                            case NetWorkUtils.VIDEO:
                                break;
                            case NetWorkUtils.LINK:
                                break;
                            default:
                                break;
                        }
                    }
                    if (count != 0 && !hasFile) {
                        sendNotification(context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "pushSucceed: e:" + e.toString());
                }
                isGettingMessage = false;
                //两种情况会再次获取
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (isNotify) {
                            LogInfo.i("hwj", " --- getAllMessage again");
                            getAllMessage(context);
                        }
                    }
                });
            }

            @Override
            public void pushFail(String s, String s1, int i, String s2) {
                LogInfo.e(TAG, "pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
                isGettingMessage = false;
            }
        });
    }

    private static void addToDatabase(Context context, Conversation conversation) {
        //插入数据库
        Uri uri = context.getContentResolver().insert(Conversations.Conversation.CONVERSATION_URI,
                ConversationProvider.getContentValue(conversation, true));
        if (uri != null) {
            WTContactUtils.updateUnreadCount(context, conversation.sendId, 1);
        }
    }

    private static void sendNotification(Context context) {
        NotificationUtils.sendNotification(context);
    }

}
