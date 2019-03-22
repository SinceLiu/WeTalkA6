package com.readboy.bean;

import android.content.Context;
import android.os.Environment;

import com.readboy.wetalk.utils.IOs;

import java.io.File;

/**
 * Created by 1-PC on 2016/9/21.
 * 常量
 *
 * @author hwj
 */
public final class Constant {

    //能否切换测试服务器,正式apk不允许切换
    public static final boolean CAN_CHANGE_TO_TEST_SERVER = true;
    /**
     * 消息类型，为了便于统一处理，扩展，确保双数为发送，单数为接收
     */
    public static final int SEND_VOICE = 0;
    public static final int REC_VOICE = 1;
    public static final int SEND_IMAGE = 2;
    public static final int REC_IMAGE = 3;
    public static final int SEND_EMOJI = 4;
    public static final int REC_EMOJI = 5;
    public static final int REC_TEXT = 6;
    public static final int SEND_VIDEO = 8;
    public static final int REC_VIDEO = 9;
    public static final int REC_SYSTEM = 11;
    //增长类型，记得Adapter.getViewTypeCount也要增加

    public static boolean isSendMessage(int type) {
        return type % 2 == 0 && type != REC_TEXT;
    }

    public static final String EMPTY = "";

    /**
     * 虚构的值，是否有开启仅wifi情况下使用。
     */
    public static final boolean ENABLE_FAKE = true;

    /**
     * 可显示新消息最大数量值，超过该值，显示“...”
     * 后修改为显示“99+”
     */
    public static final int MAX_MESSAGE_COUNT = 99;

    /**
     * 现在最大聊天记录数。
     */
    public static final int MAX_CONVERSATION_SIZE = 100;

    /**
     * 跳转参数
     */
    //跳转图库,相机requestCode
    public static final int REQUEST_IMAGE = 0x123;
    public static final int REQUEST_CAMERA = 0x124;
    //主界面用户跳转
    public static final String FROM = "from";
    public static final String EXTRA_FRIEND = "friend";
    public static final String FRIEND_DATA = "friend_data";
    public static final String EXTRA_FRIEND_ID = "friend_id";
    public static final String FRIEND_CONTRACT_ID = "friend_contact_id";
    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_FRIEND_MODEL = "model";
    public static final String FRIEND_UNREAD_COUNT = "friend_unread_count";
    public static final String FRIEND_AVATAR = "friend_avatar";
    public static final String SHARE_IMAGE_PATH = "share_image_path";
    //未读信息跳转
    public static final String SHOW_UNREAD = "show_unread";
    public static final String SHOW_CHAT = "show_chat";

    //表情选择requestCode
    public static final int REQUEST_EMOJI = 0x125;
    public static final String REQUEST_EMOJI_ID = "emoji_id";

    /**
     * 判断标志
     */
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    /**
     * 生命周期的标志
     */
    public static final int RESUME = 0;

    public static final int RECORDING = 92;

    /**
     * 图片操作相关
     */
    public static final int IMAGE_DONE = IOs.IMAGE_DONE;

    /**
     * 永久化数据相关
     */
    public static final String APP_NAME = "weTalk";
    public static final String START_TIME = "startTimes";
    public static final String LAST_POSITION = "last";

    /**
     * 设备相关
     */
    public static final String PARAM_APPID = "AndroidWear";
    public static final String PARAM_SN = "65cbcdeef24de25e5ed45338f06a1b37";

    public static final String ICON_URL = "http://img.readboy.com/avatar/";

    public static String getAvatarUrl(String uuid) {
        return ICON_URL + uuid;
    }

    /**
     * 路径相关
     */
    private static String getExternalFileDirectory(Context context) {
        File file = context.getExternalFilesDir(null);
        if (file != null) {
            return file.getPath();
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/com.readboy.wetalk/files";
        }
    }

    public static String getVoicePath(Context context) {
        return getExternalFileDirectory(context) + "/voice/";
    }

    public static String getImagePath(Context context) {
        return getExternalFileDirectory(context) + "/image/";
    }

    public static String getDownloadPath(Context context) {
        return getExternalFileDirectory(context) + "/download/";
    }

    public static String getVideoPath(Context context) {
        return getExternalFileDirectory(context) + File.separator + "video" + File.separator;
    }

    public static String getAvatarPath(Context context) {
        return getExternalFileDirectory(context) + "/avatar/";
    }
}
