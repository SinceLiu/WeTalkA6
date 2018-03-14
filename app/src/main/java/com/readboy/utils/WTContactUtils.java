package com.readboy.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.app.readboy.IReadboyWearListener;
import android.app.readboy.ReadboyWearManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.util.Log;

import com.readboy.bean.Constant;
import com.readboy.bean.Friend;

/**
 * @author hwj
 */
public class WTContactUtils {
    private static final String TAG = "hwj_WTContactUtils";


    public static String getLocalAvatarPath(Context context) {
        //获取当前手表的头像
        return Environment.getExternalStorageDirectory() + "/personal/"
                + MPrefs.getDeviceId(context) + ".png";
    }

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

    /**
     * 获取全部联系人,支持单聊了
     *
     */
    public static List<Friend> getFriendFromContact(Context context) {
        ReadboyWearManager manager = (ReadboyWearManager) context.getSystemService(Context.RBW_SERVICE);
        List<Friend> list = new ArrayList<Friend>();
        int oldrid = -1;
        int contactId = -1;
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, null, null, null, Data.RAW_CONTACT_ID);
        if (cursor == null || cursor.getCount() == 0) {
            LogInfo.i("hwj", "没有获取到联系人");
            return list;
        }
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
                        //				    if(unread != 0){
//				    	mPrefs.setUserUnreadCount(contact.uuid, unread);
                        Log.e(TAG, "getFriendFromContact: uuid = " + contact.uuid);
                        String imei = "868706020000215";
                        String uuid = "DA59F29B4E001B63";
//                        manager.getInfoWithKeyAndData("info", contact.uuid, new IReadboyWearListener.Stub() {
//                            @Override
//                            public void pushSuc(String s, String s1, int i, String s2, String s3) throws RemoteException {
//                                Log.e(TAG, "info pushSuc() called imei with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + ", s3 = " + s3 + "");
//                            }
//
//                            @Override
//                            public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
//                                Log.e(TAG, "info pushFail() called imei with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
//                            }
//                        });
//                        manager.getInfoWithKeyAndData("info", contact.uuid, new IReadboyWearListener.Stub() {
//                            @Override
//                            public void pushSuc(String s, String s1, int i, String s2, String s3) throws RemoteException {
//                                Log.e(TAG, "info pushSuc() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + ", s3 = " + s3 + "");
//                            }
//
//                            @Override
//                            public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
//                                Log.e(TAG, "info pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
//                            }
//                        });
//                        manager.getInfoWithKeyAndData("profile", imei, new IReadboyWearListener.Stub() {
//                            @Override
//                            public void pushSuc(String s, String s1, int i, String s2, String s3) throws RemoteException {
//                                Log.e(TAG, "profile imei pushSuc() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + ", s3 = " + s3 + "");
//                            }
//
//                            @Override
//                            public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
//                                Log.e(TAG, "profile imei pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
//                            }
//                        });
//                        manager.getInfoWithKeyAndData("profile", uuid, new IReadboyWearListener.Stub() {
//                            @Override
//                            public void pushSuc(String s, String s1, int i, String s2, String s3) throws RemoteException {
//                                Log.e(TAG, "profile uuid pushSuc() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + ", s3 = " + s3 + "");
//                            }
//
//                            @Override
//                            public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
//                                Log.e(TAG, "profile uuid pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
//                            }
//                        });
//                        manager.operateDeviceContacts("add", contact.uuid, null, new IReadboyWearListener.Stub() {
//                            @Override
//                            public void pushSuc(String s, String s1, int i, String s2, String s3) throws RemoteException {
//                                Log.e(TAG, "pushSuc() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + ", s3 = " + s3 + "");
//                            }
//
//                            @Override
//                            public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
//                                Log.e(TAG, "pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
//                            }
//                        });
//				    }
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
        IOs.savePicInLocal(BitmapFactory.decodeByteArray(friend.avatar, 0, friend.avatar.length), friend.uuid, Constant.getAvatarPath(context));
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
            return context.getContentResolver().update(Data.CONTENT_URI, values, "data8 = ?",
                    new String[]{uuid});
        }
        return 0;
    }

    /**
     * 更新联系人未读信息数
     *
     * @param uuid
     * @return 受影响行数
     */
    public static void clearFriendUnreadCount(final Context context, final String uuid) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NotificationUtils.NOTIFY_ID);
        manager.cancel(NotificationUtils.NORMAL_NOTIFY_ID);
        ContentValues values = new ContentValues();
        values.put(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
        values.put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK);
        values.put("data6", 0);
        context.getContentResolver().update(Data.CONTENT_URI, values, "data8 = ?",
                new String[]{uuid});
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
        }
        return count;
    }
}
