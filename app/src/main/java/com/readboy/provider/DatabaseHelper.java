package com.readboy.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.readboy.provider.WeTalkContract.ProfileColumns;
import com.readboy.provider.WeTalkContract.GroupColumns;

/**
 * Created by 1-PC on 2016/10/8.
 * 数据库相关
 */

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "hwj_DatabaseHelper";

    /**
     * 数据库名
     */
    private static final String DATABASE_NAME = "wetalk";
    /**
     * 初始版本
     */
    private static final int VERSION_2 = 2;
    /**
     * 新添{@link ProfileColumns#TABLE_NAME}表,
     *
     * @date 20180201
     */
    private static final int VERSION_3 = 3;

    /**
     * 添加Group表，保存群成员信息。
     */
    private static final int VERSION_4 = 4;

    /**
     * 创建索引
     */
    private static final int VERSION_5 = 5;

    static final String CONVERSATION_TABLE_NAME = Conversations.Conversation.TABLE_NAME;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_5);
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

        createProfileTable(sqLiteDatabase);
        createGroupTable(sqLiteDatabase);
        createGroupIndex(sqLiteDatabase);
    }

    private void createProfileTable(SQLiteDatabase db) {
        Log.e(TAG, "createProfileTable: ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ProfileColumns.TABLE_NAME + " ("
                + ProfileColumns._ID + " INTEGER PRIMARY KEY,"
                + ProfileColumns.UUID + " TEXT,"
                + ProfileColumns.IMEI + " TEXT,"
                + ProfileColumns.NAME + " TEXT,"
                + ProfileColumns.TYPE + " TEXT,"
                + ProfileColumns.SEX + " INTEGER,"
                + ProfileColumns.GRADE + " INTEGER,"
                + ProfileColumns.DATA + " TEXT);");
    }

    private void createGroupTable(SQLiteDatabase db) {
        Log.i(TAG, "createGroupTable: create");
        db.execSQL("DROP TABLE IF EXISTS " + GroupColumns.TABLE_NAME);
        db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " ("
                + GroupColumns._ID + " INTEGER PRIMARY KEY,"
                + GroupColumns.UUID + " TEXT,"
                + GroupColumns.OWNER + " TEXT,"
                + GroupColumns.NAME + " TEXT,"
                + GroupColumns.MEMBERS + " TEXT,"
                + GroupColumns.VERSION + " INTEGER)");
    }

    /**
     * 在group表创建索引
     */
    private void createGroupIndex(SQLiteDatabase db) {
        Log.i(TAG, "createGroupIndex: ");
        db.execSQL("CREATE INDEX index_uuid ON " + GroupColumns.TABLE_NAME
                + " (" + GroupColumns.UUID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade: old = " + oldVersion + ", new = " + newVersion);
        if (oldVersion == VERSION_2 && newVersion >= VERSION_3) {
            createProfileTable(sqLiteDatabase);
        }

        if (newVersion >= VERSION_4 && oldVersion <= VERSION_3) {
            createGroupTable(sqLiteDatabase);
        }

        if (newVersion == VERSION_5) {
            createGroupIndex(sqLiteDatabase);
        }
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProfileColumns.TABLE_NAME);
//        onCreate(sqLiteDatabase);
    }
}
