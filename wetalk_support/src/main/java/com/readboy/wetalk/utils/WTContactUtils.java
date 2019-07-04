package com.readboy.wetalk.utils;

import android.app.readboy.PersonalInfo;
import android.app.readboy.ReadboyWearManager;
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
import android.text.TextUtils;
import android.util.Log;

import com.readboy.wetalk.bean.Friend;
import com.readboy.wetalk.support.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author hwj
 */
public class WTContactUtils {
    private static final String TAG = "hwj_WTContactUtils";
    public static final Uri CONVERSATION_URI = Uri.parse("content://com.readboy.wetalk.provider.Conversation/conversation");

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

    public static boolean isGroupControlled(Context context) {
        ReadboyWearManager rwm = (ReadboyWearManager) context.getSystemService(Context.RBW_SERVICE);
        PersonalInfo info = rwm.getPersonalInfo();
        if (info == null) {
            return false;
        }
        return info.isAppCtrl(true,"friendGroupChat");
    }

    public static List<Friend> getFriendFromContacts(Context context) {

        return getFriendFromContacts(context, null, null);
    }

    /**
     * 获取全部联系人,支持单聊了
     */
    public static List<Friend> getFriendFromContacts(Context context, String selection, String[] args) {
        boolean isGroupControlled = isGroupControlled(context);
        List<Friend> list = new ArrayList<Friend>();
        int oldrid = -1;
        int contactId = -1;
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, null, selection, args, Data.RAW_CONTACT_ID);
        if (cursor == null || cursor.getCount() == 0) {
            Log.i("hwj", "没有获取到联系人");
            return list;
        }
        Friend contact = null;
        while (cursor.moveToNext()) {
            contactId = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
            if (oldrid != contactId) {
                if (contact != null && contact.uuid != null && !contact.uuid.startsWith("M")) {
                    if (!isGroupControlled || "家庭圈".equals(contact.name) || !contact.uuid.startsWith("G")) {
                        list.add(contact);
                    }
                }
                contact = new Friend();
                contact.contactId = contactId;
                oldrid = contactId;
            }
            if (contact == null) {
                continue;
            }
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
                        if ("家庭圈".equals(contact.name)) {
                            contact.icon = R.drawable.ic_family_group;
                        } else if (contact.uuid.startsWith("G")) {
                            contact.icon = R.drawable.ic_friend_group;
                        }
//                        contact.unreadCount = cursor.getInt(cursor.getColumnIndex("data6"));
                        contact.unreadCount = getUnreadMessageCount(context, contact.uuid);
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
                if (!isGroupControlled || "家庭圈".equals(contact.name) || !contact.uuid.startsWith("G")) {
                    list.add(contact);
                }
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

    /**
     * 获取全部群组
     */
    public static List<Friend> getGroupFromContacts(Context context) {
        List<Friend> list = new ArrayList<Friend>();
        int oldrid = -1;
        int contactId = -1;
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, null, null, null, Data.RAW_CONTACT_ID);
        if (cursor == null || cursor.getCount() == 0) {
            Log.i("hwj", "没有获取到群组");
            return list;
        }
        Friend contact = null;
        while (cursor.moveToNext()) {
            contactId = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
            if (oldrid != contactId) {
                if (contact != null && contact.uuid != null && contact.uuid.startsWith("G") && !"家庭圈".equals(contact.name)) {
                    list.add(contact);
                }
                contact = new Friend();
                contact.contactId = contactId;
                oldrid = contactId;
            }
            if (contact == null) {
                continue;
            }
            switch (cursor.getString(cursor.getColumnIndex(Data.MIMETYPE))) {
                case StructuredName.CONTENT_ITEM_TYPE:
                    contact.name = cursor.getString(cursor.getColumnIndex(StructuredName.DISPLAY_NAME));
                    break;
                case StructuredPostal.CONTENT_ITEM_TYPE:
                    if (cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE)) == StructuredPostal.TYPE_WORK) {
                        contact.uuid = cursor.getString(cursor.getColumnIndex("data8"));
                    }
                    break;
                default:
                    break;
            }
        }
        cursor.close();
        try {
            if (contact != null && contact.uuid != null && contact.uuid.startsWith("G") && !"家庭圈".equals(contact.name)) {
                list.add(contact);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
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
     * 返回所有微聊未读数
     */
    public static int getUnreadMessageCount(Context context) {
        int result = 0;
        String selection = "unread = 1 AND send_id in (";
        StringBuilder builder = new StringBuilder(selection);
        List<String> list = getAllUuid(context);
        if (list == null || list.size() <= 0) {
            Log.w(TAG, "getUnreadMessageCount: list = null");
            return 0;
        }
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                builder.append("\"");
                builder.append(list.get(i));
                builder.append("\"");
            } else {
                builder.append(", ");
                builder.append("\"");
                builder.append(list.get(i));
                builder.append("\"");
            }
        }
        builder.append(")");
        try (Cursor cursor = context.getContentResolver().query(CONVERSATION_URI, null,
                builder.toString(), null, null)) {
            if (cursor != null) {
                result = cursor.getCount();
            }
        }
        return result;
    }

    /**
     * @return 未读数
     */
    public static int getUnreadMessageCount(Context context, String uuid) {
        int result = 0;
        String selection = "unread = 1 AND send_id = ?";
        try (Cursor cursor = context.getContentResolver().query(CONVERSATION_URI, null,
                selection, new String[]{uuid}, null)) {
            if (cursor != null) {
                result = cursor.getCount();
            }
        }
        return result;
    }

    public static boolean isContacts(Context context, String uuid) {
        try (Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[]{"data8"},
                "data8=?", new String[]{uuid}, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public static List<String> getAllUuid(Context context) {
        List<String> list = new ArrayList<>();
        String data = Data.DATA8;
        String selection = "data8 NOT LIKE \"M%\"";
        try (Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[]{data},
                selection, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String uuid = cursor.getString(cursor.getColumnIndex(data));
                    if (!TextUtils.isEmpty(uuid)) {
                        list.add(uuid);
                    } else {
                        Log.i(TAG, "getAllUuid: uuid = " + uuid);
                    }
                } while (cursor.moveToNext());
            }
        }
        return list;
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
