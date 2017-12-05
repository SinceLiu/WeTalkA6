package com.readboy.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 1-PC on 2016/10/8.
 * 数据库相关
 */

class DatabaseHelper extends SQLiteOpenHelper{

    //数据库名
    private static final String TABLE_NAME = "wetalk";
    //数据库版本
    private static final int VERSION = 2;

    static final String CONVERSATION_TABLE_NAME = "conversation";

    DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + CONVERSATION_TABLE_NAME + "("
                + Conversations.Conversation._ID + " INTEGER PRIMARY KEY,"
                + Conversations.Conversation.CONVERSATION_ID + " TEXT,"
                + Conversations.Conversation.SEND_ID + " TEXT,"
                + Conversations.Conversation.SENDER_NAME + " TEXT,"
                + Conversations.Conversation.REAL_SEND_ID + " TEXT,"
                + Conversations.Conversation.REC_ID + " TEXT,"
                + Conversations.Conversation.TYPE + " INTEGER,"
                + Conversations.Conversation.HOME_GROUP + " INTEGER,"
                + Conversations.Conversation.TIME + " TEXT,"
                + Conversations.Conversation.EMOJI_ID + " INTEGER,"
                + Conversations.Conversation.REC_EMOJI_CODE + " TEXT,"
                + Conversations.Conversation.IMAGE_PATH + " TEXT,"
                + Conversations.Conversation.THUMB_IMAGE_URL + " TEXT,"
                + Conversations.Conversation.IMAGE_URL + " TEXT,"
                + Conversations.Conversation.TEXT_CONTENT + " TEXT,"
                + Conversations.Conversation.VOICE_PATH + " TEXT,"
                + Conversations.Conversation.VOICE_URL + " TEXT,"
                + Conversations.Conversation.LAST_TIME + " INTEGER,"
                + Conversations.Conversation.UNREAD + " INTEGER,"
                + Conversations.Conversation.IS_UN_PLAY + " INTEGER,"
                + Conversations.Conversation.SHOULD_RESEND + " INTEGER,"
                + Conversations.Conversation.IS_SENDING + " INTEGER,"
                + Conversations.Conversation.IS_PLAYING + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
