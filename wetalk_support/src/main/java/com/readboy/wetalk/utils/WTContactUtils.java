package com.readboy.wetalk.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings;
import android.util.Log;

import com.readboy.wetalk.bean.Friend;
import com.readboy.wetalk.support.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hwj
 */
public class WTContactUtils {
    private static final String TAG = "hwj_WTContactUtils";

    /**
     * 根据Id获取用户头像
     *
     * @param context
     * @param uuid
     * @return
     */
    public static Bitmap getAvatarById(Context context, String uuid) {
        Cursor c = context.getContentResolver().query(Data.CONTENT_URI,
                new String[]{Data.RAW_CONTACT_ID}, "data8 = ?",
                new String[]{uuid}, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int rawId = c.getInt(0);
            c.close();
            try (Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI,
                    null, Data.RAW_CONTACT_ID + "=? AND mimetype=?",
                    new String[]{rawId + "", Photo.CONTENT_ITEM_TYPE}, null)) {
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    byte[] avatar = cursor.getBlob(cursor.getColumnIndex(Photo.PHOTO));
                    if (avatar != null && avatar.length != 0) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 3;
                        return BitmapFactory.decodeByteArray(avatar, 0, avatar.length, options);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据rawId获取联系人名称
     *
     * @param uuid uuid
     * @return
     */
    public static String getNameById(Context context, String uuid) {
        Cursor c = context.getContentResolver().query(Data.CONTENT_URI,
                new String[]{Data.RAW_CONTACT_ID}, "data8 = ?",
                new String[]{uuid}, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int rawId = c.getInt(0);
            c.close();
            Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI,
                    new String[]{StructuredName.DISPLAY_NAME}, Data.RAW_CONTACT_ID + "=? AND mimetype=?",
                    new String[]{rawId + "", StructuredName.CONTENT_ITEM_TYPE}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String displayName = cursor.getString(0);
                cursor.close();
                return displayName;
            }
        }
        return "";
    }

    public static List<Friend> getFriendFromContacts(Context context) {
        return getFriendFromContacts(context, null, null);
    }

    /**
     * 获取全部联系人,支持单聊了
     */
    public static List<Friend> getFriendFromContacts(Context context, String selection, String[] args) {
        List<Friend> list = new ArrayList<Friend>();
        int oldrid = -1;
        int contactId = -1;
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, null, selection, args, Data.RAW_CONTACT_ID);
        if (cursor == null || cursor.getCount() == 0) {
            Log.i("hwj", "没有获取到联系人");
            return list;
        }
        Log.i(TAG, "getFriendFromContacts: count = " + cursor.getCount());
        Friend contact = null;
        while (cursor.moveToNext()) {
            contactId = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
            if (oldrid != contactId) {
                if (contact != null && contact.uuid != null && !contact.uuid.startsWith("M")) {
                    list.add(contact);
                }
                contact = new Friend();
                contact.contactId = contactId;
                oldrid = contactId;
            }
            if (contact == null) {
                continue;
            }
            String mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
            switch (cursor.getString(cursor.getColumnIndex(Data.MIMETYPE))) {
                case StructuredName.CONTENT_ITEM_TYPE:
                    contact.name = cursor.getString(cursor.getColumnIndex(StructuredName.DISPLAY_NAME));
                    break;
                case Phone.CONTENT_ITEM_TYPE:
                    String mobile = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                    switch (cursor.getInt(cursor.getColumnIndex(Phone.TYPE))) {
                        case Phone.TYPE_MOBILE:
                            contact.phone = mobile;
                            break;
                        case Phone.TYPE_HOME:
                            contact.shortPhone = mobile;
                            break;
                        default:
                            break;
                    }
                    break;
                case StructuredPostal.CONTENT_ITEM_TYPE:
                    if (cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE)) == StructuredPostal.TYPE_WORK) {
                        contact.uuid = cursor.getString(cursor.getColumnIndex("data8"));
                        if (contact.uuid.startsWith("G")) {
                            contact.icon = R.drawable.ic_friend_group;
                        }
//				    contact.unreadCount = mPrefs.getUserUnreadCount(contact.uuid);
                        contact.unreadCount = cursor.getInt(cursor.getColumnIndex("data6"));
                        contact.relation = cursor.getInt(cursor.getColumnIndex("data9"));
                    }
                    break;
                case Photo.CONTENT_ITEM_TYPE:
                    contact.avatar = cursor.getBlob(cursor.getColumnIndex(Photo.PHOTO));
                    contact.photoUri = cursor.getString(cursor.getColumnIndex(Photo.PHOTO_URI));
                    break;
                default:
                    break;
            }
        }
        cursor.close();
        try {
            if (contact != null && !list.contains(contact) && contact.uuid != null && !contact.uuid.startsWith("M")) {
                list.add(contact);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
//	  for(Friend friend : list){
//		  //头像保存本地
//		  saveFriendDataToLocal(context,friend);
//	  }
        return list;
    }

    private static void saveFriendDataToLocal(Context context, Friend friend) {
        if (friend.avatar == null) {
            return;
        }
        IOs.savePicInLocal(BitmapFactory.decodeByteArray(friend.avatar, 0, friend.avatar.length),
                friend.uuid, PathUtils.getAvatarPath(context));
    }

    /**
     * 更新联系人未读信息数
     *
     * @param context
     * @param uuid
     * @param count   增量  +-n
     * @return 受影响行数
     */
    public static int updateUnreadCount(Context context, String uuid, int count) {
        Cursor c = context.getContentResolver().query(Data.CONTENT_URI,
                new String[]{"data6"}, "data8=?",
                new String[]{uuid}, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int pre = c.getInt(0);
            pre += count;
            pre = pre < 0 ? 0 : pre;
            c.close();
            ContentValues values = new ContentValues();
            values.put(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
            values.put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK);
            values.put("data6", pre);
            int result = context.getContentResolver().update(Data.CONTENT_URI, values, "data8 = ?",
                    new String[]{uuid});
            if (result <= 0) {
                Log.i(TAG, "updateUnreadCount: update fail. result = " + result);
            }
        } else {
            Log.w(TAG, "updateUnreadCount: query unread count false.");
        }
        return 0;
    }

    /**
     * 获取联系人未读信息数
     *
     * @param context
     * @param uuid
     * @return
     */
    public static int getFriendUnreadCount(Context context, String uuid) {
        try (Cursor c = context.getContentResolver().query(Data.CONTENT_URI,
                new String[]{"data6"}, "data8=?",
                new String[]{uuid}, null)) {
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                return c.getInt(0);
            }
            return 0;
        }
    }

    public static int getAllContactsUnreadCount(Context context) {
        Cursor c = context.getContentResolver().query(Data.CONTENT_URI,
                new String[]{"data6", Data.RAW_CONTACT_ID},
                Data.MIMETYPE + "=? AND " + StructuredPostal.TYPE + "=?",
                new String[]{StructuredPostal.CONTENT_ITEM_TYPE, StructuredPostal.TYPE_WORK + ""},
                null);
        int count = 0;
        if (c != null && c.moveToFirst()) {
            do {
                int num = c.getInt(0);
                count += num;
            } while (c.moveToNext());
            c.close();
        } else {
            Log.w(TAG, "getAllContactsUnreadCount: query fail, cursor = " + c);
        }
        return count;
    }

    public static boolean isContacts(Context context, String uuid) {
        try (Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[]{"data8"},
                "data8=?", new String[]{uuid}, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public static boolean deleteContactsByUuid(Context context, String uuid) {
        String selection = "data8 = ?";
        try (Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, null,
                selection, new String[]{uuid}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String rawId = cursor.getString(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
                String where = "raw_contact_id in (SELECT raw_contact_id FROM data WHERE data8 =?)";
                int count = context.getContentResolver().delete(Data.CONTENT_URI, where, new String[]{uuid});
                Log.i(TAG, "deleteContactsByUuid: data count = " + count);
                return deleteContactsByRawId(context, rawId);
            } else {
                Log.i(TAG, "deleteContactsByUuid: query failure, cursor = " + cursor);
            }
        }
        return false;
    }

    private static boolean deleteContactsByRawId(Context context, String rawId) {
        Log.i(TAG, "deleteContactsByRawId: rawId = " + rawId);
        try {
            Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, Long.getLong(rawId));
            int raw = context.getContentResolver().delete(uri, null, null);
            Log.i(TAG, "deleteContactsByRawId: RawContacts raw = " + raw);
            return raw > 0;
        } catch (Exception e) {
            Log.w(TAG, "deleteContactsByRawId: ", e);
        }
        return false;
    }
}
