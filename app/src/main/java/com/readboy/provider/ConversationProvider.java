package com.readboy.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.wetalk.utils.WTContactUtils;
import com.readboy.provider.WeTalkContract.ProfileColumns;
import com.readboy.provider.WeTalkContract.GroupColumns;

/**
 * @author hwwjian
 * @date 2016/12/1
 */

public class ConversationProvider extends ContentProvider {
    private static final String TAG = "hwj_ContentProvider";

    private static final int CODE_CONVERSATION = 1001;
    private static final int CODE_PROFILE = 1002;
    private static final int CODE_GROUP = 1003;
    private static final int CODE_GROUP_ID = 1004;

    public static final String IS_RECEIVE_MESSAGE = "receive";

    private final static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //为UriMatcher注册Uri
        sUriMatcher.addURI(Conversations.AUTHORITY, "conversation", CODE_CONVERSATION);
        sUriMatcher.addURI(WeTalkContract.AUTHORITY, "profiles", CODE_PROFILE);
        sUriMatcher.addURI(WeTalkContract.AUTHORITY, GroupColumns.TABLE_NAME, CODE_GROUP);
        sUriMatcher.addURI(WeTalkContract.AUTHORITY, GroupColumns.TABLE_NAME + "/#", CODE_GROUP_ID);
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
     * @param uri           uri
     * @param projection    查询的列
     * @param selection     查询条件
     * @param selectionArgs 匹配条件参数
     * @param sortOrder     排序条件
     * @return 查询结果
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        int code = sUriMatcher.match(uri);
        switch (code) {
            case CODE_CONVERSATION:
            case CODE_PROFILE:
            case CODE_GROUP:
                Table table = Table.valueOf(sUriMatcher.match(uri));
                if (table != null) {
                    qb.setTables(table.tableName);
                    cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                } else {
                    Log.w(TAG, "query: table is null, " + code);
                }
                break;
            case CODE_GROUP_ID:
                qb.setTables(GroupColumns.TABLE_NAME);
                qb.appendWhere(GroupColumns._ID + "=");
                qb.appendWhere(uri.getLastPathSegment());
                cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        switch (sUriMatcher.match(uri)) {
            case CODE_CONVERSATION:
                synchronized (this) {
                    boolean isReceive = contentValues.getAsBoolean(IS_RECEIVE_MESSAGE);
                    contentValues.remove(IS_RECEIVE_MESSAGE);
                    try (Cursor cursor = mHelper.getWritableDatabase().query(DatabaseHelper.CONVERSATION_TABLE_NAME,
                            new String[]{Conversations.Conversation.CONVERSATION_ID},
                            Conversations.Conversation.CONVERSATION_ID + " = ? AND " + Conversations.Conversation.TIME + " = ? ",
                            new String[]{contentValues.getAsString(Conversations.Conversation.CONVERSATION_ID),
                                    contentValues.getAsString(Conversations.Conversation.TIME)}, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst() && cursor.getCount() != 0) {
                            Log.i(TAG, "insert: insert fail. cursor = " + cursor);
                            return null;
                        }

                        //数据库不存在才插入数据
                        long rawId = mHelper.getWritableDatabase().insert(
                                DatabaseHelper.CONVERSATION_TABLE_NAME, null, contentValues);
                        //插入成功返回Uri
                        if (rawId > 0) {
                            Uri conUri = ContentUris.withAppendedId(
                                    Conversations.Conversation.CONVERSATION_URI, rawId);
                            if (isReceive && getContext() != null) {
                                notifyChange(conUri);
                            }
                            return conUri;
                        } else {
                            Log.i(TAG, "insert: insert fail, rawId = " + rawId);
                        }
                    }
                }
                break;
            case CODE_PROFILE:
                long rawId = mHelper.getWritableDatabase().insert(ProfileColumns.TABLE_NAME,
                        null, contentValues);
                if (rawId > 0) {
                    Uri result = ContentUris.withAppendedId(ProfileColumns.CONTENT_URI, rawId);
                    notifyChange(result);
                    return result;
                }
                break;
            case CODE_GROUP:
                // replace接口有问题，慎用
                String uuid = contentValues.getAsString(GroupColumns.UUID);
                if (TextUtils.isEmpty(uuid)) {
                    Log.i(TAG, "insert: uuid is null.");
                    return null;
                }
                int id = -1;
                Log.i(TAG, "insert: uuid = " + uuid);
                try (Cursor cursor = mHelper.getWritableDatabase().query(GroupColumns.TABLE_NAME,
                        null, GroupColumns.UUID + "=?", new String[]{uuid},
                        null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        id = cursor.getInt(cursor.getColumnIndex(GroupColumns._ID));
                        Log.i(TAG, "insert: id = " + id);
                    }
                }
                long row = -1;
                if (id > 0) {
                    contentValues.put(GroupColumns._ID, id);
                    row = mHelper.getWritableDatabase().update(GroupColumns.TABLE_NAME, contentValues,
                            null, new String[]{});
                } else {
                    row = mHelper.getWritableDatabase().insert(GroupColumns.TABLE_NAME,
                            null, contentValues);
                }
                Log.i(TAG, "insert: contentValues = " + contentValues.toString());

                if (row <= 0) {
                    Log.w(TAG, "insert: group table, replace fail.");
                    return null;
                }
                return ContentUris.withAppendedId(GroupColumns.CONTENT_URI, row);
            default:
                Log.e(TAG, "insert: default : " + uri);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] args) {
        int count = -1;
        int code = sUriMatcher.match(uri);
        switch (code) {
            case CODE_CONVERSATION:
            case CODE_PROFILE:
            case CODE_GROUP:
                Table table = Table.valueOf(code);
                if (table != null) {
                    count = deleteCommon(table, selection, args);
                }
                break;
            case CODE_GROUP_ID:
                String id = uri.getLastPathSegment();
                String where = "id=?";
                count = mHelper.getWritableDatabase().delete(GroupColumns.TABLE_NAME, where, new String[]{id});
                break;
            default:
                break;
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] args) {
        int count = -1;
        Table table = Table.valueOf(sUriMatcher.match(uri));
        if (table != null) {
            count = mHelper.getWritableDatabase().update(table.tableName, contentValues, selection, args);
        }
        if (count > 0) {
            notifyChange(uri);
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
        if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
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

                //上次意外中断，有发送中的消息,改成
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

    /**
     * 最好做到精确定位到哪个联系人应该更新了
     */
    private void notifyUnreadCountChange(String uuid) {
        notifyChange(WeTalkContract.CONVERSATION_UNREAD_URI);
    }

    /**
     * Notify affected URIs of changes.
     */
    private void notifyChange(Uri uri) {
        if (getContext() != null) {
            ContentResolver resolver = getContext().getContentResolver();
            resolver.notifyChange(uri, null);
        }
    }

    private Uri insertCommon(ContentValues values, Table table) {
        long rawId = mHelper.getWritableDatabase().insert(table.tableName,
                null, values);
        if (rawId > 0) {
            Uri result = ContentUris.withAppendedId(table.uri, rawId);
            notifyChange(result);
            return result;
        }
        return null;
    }

    private Long replaceCommon(ContentValues values, Table table) {
        return mHelper.getWritableDatabase().replace(table.tableName, null, values);
    }

    private int deleteCommon(Table table, String selection, String[] args) {
        return deleteCommon(table.tableName, selection, args);
    }

    private int deleteCommon(String tableName, String selection, String[] args) {
        return mHelper.getWritableDatabase().delete(tableName, selection, args);
    }

    private int updateCommon(Table table, ContentValues values, String selection, String[] args) {
        return mHelper.getWritableDatabase().update(
                DatabaseHelper.CONVERSATION_TABLE_NAME, values, selection, args);
    }

    private long parseId(Uri uri) {
        String last = uri.getLastPathSegment();
        try {
            return last == null ? -1 : Long.parseLong(last);
        } catch (Exception exception) {
            return -1;
        }
    }

    private enum Table {
        /**
         * 枚举，方便统一处理，便于维护
         */
        CONVERSATION(Conversations.Conversation.TABLE_NAME, Conversations.Conversation.CONVERSATION_URI, CODE_CONVERSATION),
        PROFILE(ProfileColumns.TABLE_NAME, ProfileColumns.CONTENT_URI, CODE_PROFILE),
        GROUP(GroupColumns.TABLE_NAME, GroupColumns.CONTENT_URI, CODE_GROUP);

        String tableName;
        Uri uri;
        /**
         * UriMather code.
         */
        int code;

        Table(String tableName, Uri uri, int code) {
            this.tableName = tableName;
            this.uri = uri;
            this.code = code;
        }

        static Table valueOf(int code) {
            for (Table table : values()) {
                if (table.code == code) {
                    return table;
                }
            }
            return null;
        }
    }
}
