package com.readboy.receiver;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.provider.ConversationProvider;
import com.readboy.provider.Conversations;
import com.readboy.provider.Profile;
import com.readboy.service.MessageService;
import com.readboy.utils.EmojiUtils;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.NetWorkUtils.PushResultListener;
import com.readboy.utils.NotificationUtils;
import com.readboy.view.GroupMembersView;
import com.readboy.wetalk.utils.WTContactUtils;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author hwwjian
 * @date 2016/12/14
 * 获取消息
 * TODO 如果被删除的联系人有未读数，发送通知时，未读数的获取有误，需要修改校验
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

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive2: action = " + action);
        if (TextUtils.isEmpty(action)) {
            return;
        }
        Intent messageIntent = new Intent(context, MessageService.class);
        messageIntent.putExtra("action", action);
        switch (action) {
            case ACTION_NOTIFY_MESSAGE:
                //收到消息
                //如果正在获取新消息，是否应已队列形式，排队获取。
                context.startForegroundService(messageIntent);
                break;
            case ACTION_SEND_CAPTURE:
                //发送监拍指令
                messageIntent.putExtra("picture_path", intent.getStringArrayListExtra("capture_uuid"));
                context.startForegroundService(messageIntent);
                break;
            case ACTION_NOTIFY_FRIEND_REQUEST:
            case ACTION_NOTIFY_FRIEND_ADD:
                messageIntent.putExtra("id", intent.getStringExtra("id"));
                context.startForegroundService(messageIntent);
//                requestAddFriend(context, intent);
                break;
            case ACTION_NOTIFY_FRIEND_REFUSE:
                break;
            default:
                break;
        }
    }


}
