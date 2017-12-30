package com.readboy.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.utils.WTContactUtils;

/**
 * Created by hwwjian on 2016/12/1.
 */

public class ConversationProvider extends ContentProvider {

    private static final int CONVERSATION = 0;

    public static final String IS_RECEIVE_MESSAGE = "receive";

    private static UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //为UriMatcher注册Uri
        mMatcher.addURI(Conversations.AUTHORITY, "conversation", CONVERSATION);
    }

    private DatabaseHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * 获取邮件数量
     *
     * @param uri        uri
     * @param projection 查询的列
     * @param where      查询条件
     * @param whereArgs  匹配条件参数
     * @param sortOrder  排序条件
     * @return 查询结果
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
        if (mMatcher.match(uri) == CONVERSATION) {
            Cursor cursor = null;
            if (sortOrder != null) {
                cursor = mHelper.getWritableDatabase()
                        .query(DatabaseHelper.CONVERSATION_TABLE_NAME,
                                projection, where, whereArgs, null, null, sortOrder);
            } else {
                cursor = mHelper.getWritableDatabase()
                        .query(DatabaseHelper.CONVERSATION_TABLE_NAME,
                                projection, where, whereArgs, null, null, Conversations.Conversation.DEFAULT_SORT_ORDER);
            }
            return cursor;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        if (mMatcher.match(uri) == CONVERSATION) {
            synchronized (this) {
                boolean isReceive = contentValues.getAsBoolean(IS_RECEIVE_MESSAGE);
                contentValues.remove(IS_RECEIVE_MESSAGE);
                try (Cursor cursor = mHelper.getWritableDatabase().query(DatabaseHelper.CONVERSATION_TABLE_NAME,
                        new String[]{Conversations.Conversation.CONVERSATION_ID},
                        Conversations.Conversation.CONVERSATION_ID + " = ? AND " + Conversations.Conversation.TIME + " = ? ",
                        new String[]{contentValues.getAsString(Conversations.Conversation.CONVERSATION_ID),
                                contentValues.getAsString(Conversations.Conversation.TIME)}, null, null, null)) {
                    if (cursor != null && cursor.getCount() != 0) {
                        return null;
                    }

                    //数据库不存在才插入数据
                    long rawId = mHelper.getWritableDatabase().insert(
                            DatabaseHelper.CONVERSATION_TABLE_NAME, null, contentValues);
                    //插入成功返回Uri
                    if (rawId > CONVERSATION) {
                        Uri conUri = ContentUris.withAppendedId(
                                Conversations.Conversation.CONVERSATION_URI, rawId);
                        if (isReceive) {
                            getContext().getContentResolver().notifyChange(conUri, null);
                        }
                        return conUri;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] args) {
        int count = -1;
        if (mMatcher.match(uri) == CONVERSATION) {
            count = mHelper.getWritableDatabase().delete(
                    DatabaseHelper.CONVERSATION_TABLE_NAME, selection, args);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] args) {
        int count = -1;
        if (mMatcher.match(uri) == CONVERSATION) {
            count = mHelper.getWritableDatabase().update(
                    DatabaseHelper.CONVERSATION_TABLE_NAME, contentValues, selection, args);
        }
        return count;
    }

    public static ContentValues getContentValue(Conversation conversation, boolean isReceive) {
        ContentValues values = new ContentValues();
        values.put(Conversations.Conversation.CONVERSATION_ID, conversation.conversationId);
        values.put(Conversations.Conversation.SEND_ID, conversation.sendId);
        values.put(Conversations.Conversation.SENDER_NAME, conversation.senderName);
        values.put(Conversations.Conversation.REAL_SEND_ID, conversation.realSendId);
        values.put(Conversations.Conversation.REC_ID, conversation.recId);
        values.put(Conversations.Conversation.TYPE, conversation.type);
        values.put(Conversations.Conversation.HOME_GROUP, conversation.isHomeGroup);
        values.put(Conversations.Conversation.UNREAD, conversation.isUnread);
        values.put(Conversations.Conversation.SHOULD_RESEND, conversation.shouldResend);
        values.put(Conversations.Conversation.IS_SENDING, conversation.isSending);
        values.put(Conversations.Conversation.EMOJI_ID, conversation.emojiId);
        values.put(Conversations.Conversation.REC_EMOJI_CODE, conversation.emojiCode);
        values.put(Conversations.Conversation.IMAGE_PATH, conversation.imageLocalPath);
        values.put(Conversations.Conversation.THUMB_IMAGE_URL, conversation.thumbImageUrl);
        values.put(Conversations.Conversation.IMAGE_URL, conversation.imageUrl);
        values.put(Conversations.Conversation.TEXT_CONTENT, conversation.textContent);
        values.put(Conversations.Conversation.VOICE_PATH, conversation.voiceLocalPath);
        values.put(Conversations.Conversation.LAST_TIME, conversation.lastTime);
        values.put(Conversations.Conversation.IS_UN_PLAY, conversation.isUnPlay);
        values.put(Conversations.Conversation.VOICE_URL, conversation.voiceUrl);
        values.put(Conversations.Conversation.TIME, conversation.time);
        values.put(Conversations.Conversation.IS_PLAYING, conversation.isPlaying);
        //接收消息的标识
        values.put(IS_RECEIVE_MESSAGE, isReceive);
        return values;
    }

    public static Conversation getConversation(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else if (cursor.moveToFirst()) {
            Conversation conversation = new Conversation();
            conversation.conversationId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.CONVERSATION_ID));
            conversation.sendId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.SEND_ID));
            conversation.recId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.REC_ID));
            conversation.type = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.TYPE));
            conversation.isHomeGroup = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.HOME_GROUP));
            conversation.senderName = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.SENDER_NAME));
            conversation.realSendId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.REAL_SEND_ID));
            conversation.isUnread = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.UNREAD));
            conversation.shouldResend = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.SHOULD_RESEND));
            conversation.isSending = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.IS_SENDING));
            conversation.emojiId = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.EMOJI_ID));
            conversation.emojiCode = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.REC_EMOJI_CODE));
            conversation.imageLocalPath = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.IMAGE_PATH));
            conversation.thumbImageUrl = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.THUMB_IMAGE_URL));
            conversation.imageUrl = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.IMAGE_URL));
            conversation.textContent = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.TEXT_CONTENT));
            conversation.voiceLocalPath = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.VOICE_PATH));
            conversation.lastTime = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.LAST_TIME));
            conversation.isUnPlay = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.IS_UN_PLAY));
            conversation.voiceUrl = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.VOICE_URL));
            conversation.time = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.TIME));
            conversation.isPlaying = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.IS_PLAYING));
            cursor.close();
            return conversation;
        }
        return null;
    }

    public static void removeUsersConversationById(Context context, String uuid) {
        String name = WTContactUtils.getNameById(context, uuid);
        if (TextUtils.isEmpty(name)) {
            context.getContentResolver().delete(Conversations.Conversation.CONVERSATION_URI,
                    Conversations.Conversation.SEND_ID + " = ? OR " + Conversations.Conversation.REC_ID + " = ? ",
                    new String[]{uuid, uuid});
        }
    }

    public static List<Conversation> getConversationList(Context context, String uuid) {
        Cursor cursor = context.getContentResolver().query(Conversations.Conversation.CONVERSATION_URI, null,
                Conversations.Conversation.SEND_ID + " = ? OR " + Conversations.Conversation.REC_ID + " = ? ",
                new String[]{uuid, uuid}, Conversations.Conversation.TIME + " ASC");
        List<Conversation> conversations = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Conversation conversation = new Conversation();
                conversation.conversationId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.CONVERSATION_ID));
                conversation.sendId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.SEND_ID));
                conversation.recId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.REC_ID));
                conversation.type = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.TYPE));
                conversation.senderName = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.SENDER_NAME));
                conversation.realSendId = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.REAL_SEND_ID));
                conversation.isHomeGroup = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.HOME_GROUP));
                conversation.isUnread = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.UNREAD));
                conversation.shouldResend = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.SHOULD_RESEND));
                conversation.isSending = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.IS_SENDING));
                conversation.emojiId = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.EMOJI_ID));
                conversation.emojiCode = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.REC_EMOJI_CODE));
                conversation.imageLocalPath = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.IMAGE_PATH));
                conversation.thumbImageUrl = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.THUMB_IMAGE_URL));
                conversation.imageUrl = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.IMAGE_URL));
                conversation.textContent = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.TEXT_CONTENT));
                conversation.voiceLocalPath = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.VOICE_PATH));
                conversation.lastTime = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.LAST_TIME));
                conversation.isUnPlay = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.IS_UN_PLAY));
                conversation.voiceUrl = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.VOICE_URL));
                conversation.time = cursor.getString(cursor.getColumnIndex(Conversations.Conversation.TIME));
                conversation.isPlaying = cursor.getInt(cursor.getColumnIndex(Conversations.Conversation.IS_PLAYING));

                //有发送中的消息,改成
                if (conversation.isSending == Constant.TRUE) {
                    conversation.isSending = Constant.FALSE;
                    conversation.shouldResend = Constant.TRUE;
                }
                //如果本地的图片被删除了,那这条记录也删了
                if (conversation.type == Constant.SEND_IMAGE) {
                    if (new File(conversation.imageLocalPath).exists()) {
                        conversations.add(conversation);
                    }
                } else {
                    conversations.add(conversation);
                }
            }
            cursor.close();
        }
        return conversations;
    }
}
