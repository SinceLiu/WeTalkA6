package com.readboy.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 *
 * @author hwwjian
 * @date 2016/12/1
 */

public class Conversations {

    static final String AUTHORITY = "com.readboy.wetalk.provider.Conversation";

    private Conversations() {
    }

    public static final class Conversation implements BaseColumns {

        private Conversation() {
        }

        //访问Uri
        public static final Uri CONVERSATION_URI = Uri.parse("content://" + AUTHORITY + "/conversation");
        //默认排序常量,按id升序
        static final String DEFAULT_SORT_ORDER = "_id ASC";
        //字段
        static final String _ID = "_id";
        public static final String CONVERSATION_ID = "conversation_id";
        public static final String SEND_ID = "send_id";
        public static final String REC_ID = "rec_id";
        public static final String REAL_SEND_ID = "real_send_id";
        static final String TYPE = "message_type";
        public static final String HOME_GROUP = "home_group";
        public static final String UNREAD = "unread";
        public static final String SHOULD_RESEND = "resend";
        public static final String IS_SENDING = "is_sending";
        static final String EMOJI_ID = "emoji_id";
        static final String REC_EMOJI_CODE = "rec_emoji_code";
        public static final String IMAGE_PATH = "image_path";
        public static final String THUMB_IMAGE_URL = "thumb_url";
        public static final String IMAGE_URL = "image_url";
        static final String TEXT_CONTENT = "text_content";
        static final String VOICE_PATH = "voice_path";
        static final String LAST_TIME = "last_time";
        public static final String IS_UN_PLAY = "is_un_play";
        public static final String VOICE_URL = "voice_url";
        public static final String TIME = "time";
        public static final String SENDER_NAME = "senderName";
        public static final String IS_PLAYING = "isPlaying";
    }
}
