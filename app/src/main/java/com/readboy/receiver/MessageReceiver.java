package com.readboy.receiver;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.provider.ConversationProvider;
import com.readboy.provider.Conversations;
import com.readboy.provider.Profile;
import com.readboy.utils.EmojiUtils;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.NetWorkUtils.PushResultListener;
import com.readboy.utils.NotificationUtils;
import com.readboy.wetalk.utils.WTContactUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    public static final String ACTION_NOTIFY_MESSAGE = "readboy.action.NOTIFY_MESSAGE";
    /**
     * 发送监拍命令
     */
    public static final String ACTION_SEND_CAPTURE = "com.readboy.action.SENDPICTURE";

    /**
     * 请求添加好友
     */
    public static final String ACTION_NOTIFY_FRIEND_ADD = "readboy.action.NOTIFY_FRIEND_ADD";
    public static final String ACTION_NOTIFY_FRIEND_REFUSE = "readboy.action.NOTIFY_FRIEND_REFUSE";
    public static final String ACTION_NOTIFY_FRIEND_REQUEST = "readboy.action.NOTIFY_FRIEND_REQUEST";
    private static final int DOWNLOAD_MSG = 0x13;

    private static final int MAX_COUNT_SINGLE_RESPONSE = 10;

    private static final boolean FILE_ADVANCE_LOADING = true;

    /**
     * TODO: 网络慢是否会导致内容丢失，不会马上获取到最新的消息。
     */
    private static boolean isGettingMessage = false;
    private static boolean requestAgain = false;

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DOWNLOAD_MSG) {
//                mNetWorkUtils.downloadFile(mContext, (Conversation) msg.obj);
            } else if (msg.what == NetWorkUtils.UPLOAD_SUCCEED) {
                LogInfo.i("hwj", "uploadFile capture succeed");
            } else if (msg.what == NetWorkUtils.UPLOAD_FAIL) {
                LogInfo.i("hwj", "uploadFile capture fail");
            }
        }
    };

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive2: action = " + action);
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case ACTION_NOTIFY_MESSAGE:
                //收到消息
                LogInfo.i("hwj", "onReceive: isGettingMessage : " + isGettingMessage);
                //如果正在获取新消息，是否应已队列形式，排队获取。
                if (!isGettingMessage) {
                    getAllMessage(context);
                } else {
                    requestAgain = true;
                }
                break;
            case ACTION_SEND_CAPTURE:
                //发送监拍指令
                handleSendCapture(context, intent);
                break;
            case ACTION_NOTIFY_FRIEND_REQUEST:
            case ACTION_NOTIFY_FRIEND_ADD:
                requestAddFriend(context, intent);
                break;
            case ACTION_NOTIFY_FRIEND_REFUSE:
                break;
            default:
                break;
        }
    }

    private void handleSendCapture(Context context, Intent intent) {
        String path = intent.getStringExtra("picture_path");
        ArrayList<String> ids = intent.getStringArrayListExtra("capture_uuid");
        if (TextUtils.isEmpty(path) || ids == null) {
            Log.w(TAG, "handleSendCapture: data is null, path = " + path + ", uuid = " + ids);
            return;
        }
        NetWorkUtils.getInstance(context).uploadCaptureFile(ids, path, mHandler);
    }

    private boolean canGetMessage(Context context) {
        return Constant.ENABLE_FAKE || NetWorkUtils.isWifiConnected(context);
    }

    public static void getAllMessage(final Context context) {
        final NetWorkUtils mNetWorkUtils = NetWorkUtils.getInstance(context);
        isGettingMessage = true;
        requestAgain = false;
        final String messageTag = MPrefs.getMessageTag(context);
        mNetWorkUtils.getAllMessage(messageTag, new PushResultListener() {

            @Override
            public void pushSucceed(String type, String s1, int code, String s,
                                    String response) {
                LogInfo.e(TAG, "pushSucceed() called with: type = " + type + ", s1 = " + s1 +
                        ", code = " + code + ", s = " + s + ", response = " + response + "");
                Log.i(TAG, "pushSucceed: thread = " + Thread.currentThread().getName());
                int count = parseMessage(response, context);
                if (count > 0 && !requestAgain) {
                    NotificationUtils.sendNotification(context);
                } else {
                    Log.i(TAG, "pushSucceed: do not notify, count = " + count + ", requestAgain = " + requestAgain);
                }
                isGettingMessage = false;
                tryRequestAgain(context);
            }

            @Override
            public void pushFail(String s, String s1, int i, String s2) {
                LogInfo.e(TAG, "pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
                isGettingMessage = false;
            }
        });
    }

    /**
     * @return count，解析是否有数据，是否需要发通知，如果解析需要下载文件，基数也会减一
     */
    private static int parseMessage(String response, Context context) {
        JSONObject jsonObject;
        JSONArray array;
        try {
            jsonObject = new JSONObject(response);
            MPrefs.setMessageTag(context, jsonObject.optString(NetWorkUtils.TIME));
            array = jsonObject.getJSONArray(NetWorkUtils.DATA);
        } catch (JSONException e) {
            Log.w(TAG, "parseMessage: e: " + e.toString(), e);
            return -1;
        }
        int count = array.length();
        int result = count;
        Log.e(TAG, "pushSucceed: response count = " + count);
        if (count >= MAX_COUNT_SINGLE_RESPONSE) {
            requestAgain = true;
        }
        for (int i = 0; i < count; i++) {
            JSONObject data = array.optJSONObject(i);
            String msgHeader = data.optString(NetWorkUtils.HEADER);
            if (TextUtils.isEmpty(msgHeader)) {
                break;
            }
            String[] messageInfo = msgHeader.split("\\|");
            final Conversation conversation = parseBaseConversation(messageInfo, context);
            if (conversation == null) {
                Log.i(TAG, "pushSucceed: conversation = null.");
                result--;
                break;
            }
            //消息类型,支持text、image、audio、video、link
            if (messageInfo.length < 3) {
                Log.w(TAG, "parseMessage: h length < 3, h: " + msgHeader);
                result--;
                break;
            }
            switch (messageInfo[2]) {
                case NetWorkUtils.TEXT:
                    parseTextMessage(context, data, conversation);
                    break;
                case NetWorkUtils.AUDIO:
                    result--;
                    parseAudioMessage(context, data, conversation);
                    break;
                case NetWorkUtils.IMAGE:
                    conversation.imageUrl = data.optJSONObject(NetWorkUtils.A).optString(NetWorkUtils.SRC);
                    conversation.thumbImageUrl = data.optString(NetWorkUtils.MESSAGE);
                    conversation.type = Constant.REC_IMAGE;
                    addToDatabase(context, conversation);
                    break;
                case NetWorkUtils.VIDEO:
                    if (FILE_ADVANCE_LOADING) {
                        result--;
                        parseVideoMessage(context, data, conversation);
                    } else {

                    }
                    break;
                case NetWorkUtils.LINK:
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private static void parseAudioMessage(Context context, JSONObject data, Conversation conversation) {
        try {
            conversation.lastTime = data.optJSONObject(NetWorkUtils.A)
                    .getInt(NetWorkUtils.LENGTH);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        conversation.voiceUrl = data.optString(NetWorkUtils.MESSAGE);
        conversation.isUnPlay = Constant.TRUE;
        conversation.isPlaying = Constant.FALSE;
        conversation.type = Constant.REC_VOICE;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                NetWorkUtils.downloadFile(context, conversation);
            }
        });
    }

    private static void parseTextMessage(Context context, JSONObject data, Conversation conversation) {
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
    }

    private static void parseVideoMessage(Context context, JSONObject data, Conversation conversation) {
        Log.i(TAG, "parseVideoMessage: ");
        conversation.type = Constant.REC_VIDEO;
        conversation.voiceUrl = data.optString(NetWorkUtils.MESSAGE);
        NetWorkUtils.downloadFile(context, conversation);
//        addToDatabase(context, conversation);
    }

    private static Conversation parseBaseConversation(String[] messageInfo, Context context) {
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
            String name = WTContactUtils.getNameById(context, conversation.realSendId);
            if (TextUtils.isEmpty(name)) {
                LogInfo.e(TAG, conversation.realSendId + " 不在通讯录里。");
                isGettingMessage = false;
                return null;
            } else {
                conversation.senderName = name;
            }
        }
        return conversation;
    }

    private static void addToDatabase(Context context, Conversation conversation) {
        Log.i(TAG, "addToDatabase:  1 >> ");
        //插入数据库
        Uri uri = context.getContentResolver().insert(Conversations.Conversation.CONVERSATION_URI,
                ConversationProvider.getContentValue(conversation, true));
        if (uri != null) {
            WTContactUtils.updateUnreadCount(context, conversation.sendId, 1);
        } else {
            Log.i(TAG, "addToDatabase: insert fail, conversation = " + conversation.toString());
        }
        Log.i(TAG, "addToDatabase:  2 >> ");
    }

    private static void tryRequestAgain(Context context) {
        //两种情况会再次获取
        if (requestAgain) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogInfo.i("hwj", " --- getAllMessage again");
                    getAllMessage(context);
                }
            });
        }
    }

    /**
     * 有好友请求通知，发送通知，点击通知跳转请求界面。
     * [{"h":"D05C2C78D500413C|G05C3421220092AB|text|1546954006506","m":"\/emoji_6"}]
     * [{"a":{"sendmsgId":"UA593B49B4A276921546954151184"},"h":"UA593B49B4A27692|GD05C2C823C00413E|text|1546954150730","m":"\/emoji_14"}]
     * [{"a":{"sendmsgId":"UA593B49B4A276921546954216586"},"h":"UA593B49B4A27692|UA593B49B4A27692|text|1546954216128","m":"\/emoji_21"}]
     */
    private void requestAddFriend(final Context context, Intent intent) {
        Log.i(TAG, "requestAddFriend: intent = " + intent.getExtras().toString());
        String uuid = intent.getStringExtra("id");
        Profile.getProfile(context, uuid, new Profile.CallBack() {
            @Override
            public void onResponse(Profile profile) {
                Log.i(TAG, "onSuccess: " + profile.toString());
                NotificationUtils.sendContactNotification(context, profile);
            }

            @Override
            public void onFail(Exception e) {
                Log.w(TAG, "onFail: " + e.toString(), e);
            }
        });
    }

}
