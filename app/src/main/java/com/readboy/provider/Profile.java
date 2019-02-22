package com.readboy.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.utils.NetWorkUtils;
import com.readboy.wetalk.bean.Friend;
import com.readboy.provider.WeTalkContract.ProfileColumns;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 联系人的简略信息，通过服务器获取。
 *
 * @author oubin
 * @date 2018/1/30
 */

public class Profile implements ProfileColumns, Parcelable {
    private static final String TAG = "hwj_Profile";

    private static final List<String> OLD_DEVICE_MODE_LIST = new ArrayList<>(Arrays.asList("W2S", "W2T", "W3T", "W5"));

    /**
     * uuid : DA59F29B4E001B63
     * imei : 868706020000215
     * name : 宝贝
     * type : W5
     * sex : 0
     * grade : 0
     */

    private static final String[] QUERY_COLUMNS = {
            BaseColumns._ID,
            ProfileColumns.UUID,
            ProfileColumns.IMEI,
            ProfileColumns.NAME,
            ProfileColumns.TYPE,
            ProfileColumns.SEX,
            ProfileColumns.GRADE,
            ProfileColumns.DATA
    };

    public String uuid;
    private String imei;
    private String name;
    private String type;
    /**
     * 性别
     * 0代表男性，1代表女性
     * value数据类型:int
     */
    private int sex;
    /**
     * 年级信息
     * value: 数据类型:int
     * value: 0代表一年级
     */
    private int grade;

    public Profile() {
    }

    public Profile(String jsonStr) {
        try {
            JSONObject object = new JSONObject(jsonStr);
            this.uuid = object.optString("id");
            this.imei = object.optString(ProfileColumns.IMEI);
            this.name = object.optString(ProfileColumns.NAME);
            this.type = object.optString(ProfileColumns.TYPE);
            this.sex = object.optInt(ProfileColumns.SEX);
            this.grade = object.optInt(ProfileColumns.GRADE);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Profile: e = " + e.toString());
        }
    }

    /**
     * 小系统设备
     */
    public boolean isOldDevice() {
        if (TextUtils.isEmpty(type)) {
            Log.i(TAG, "isOldDevice: type = " + type);
            return false;
        }
        return OLD_DEVICE_MODE_LIST.contains(type);
    }

    public static Profile createProfile(Cursor cursor) {
        Profile profile = new Profile();
        profile.uuid = cursor.getString(cursor.getColumnIndex(ProfileColumns.UUID));
        profile.imei = cursor.getString(cursor.getColumnIndex(ProfileColumns.IMEI));
        profile.name = cursor.getString(cursor.getColumnIndex(ProfileColumns.NAME));
        profile.type = cursor.getString(cursor.getColumnIndex(ProfileColumns.TYPE));
        profile.sex = cursor.getInt(cursor.getColumnIndex(ProfileColumns.SEX));
        profile.grade = cursor.getInt(cursor.getColumnIndex(ProfileColumns.GRADE));
        return profile;
    }

    public static Profile createProfile(ContentValues values) {
        Profile profile = new Profile();
        profile.uuid = values.getAsString(ProfileColumns.UUID);
        profile.imei = values.getAsString(ProfileColumns.IMEI);
        profile.name = values.getAsString(ProfileColumns.NAME);
        profile.type = values.getAsString(ProfileColumns.TYPE);
        profile.sex = values.getAsInteger(ProfileColumns.SEX);
        profile.grade = values.getAsInteger(ProfileColumns.GRADE);
        return profile;
    }

    public ContentValues createContentValues() {
        ContentValues values = new ContentValues();
        values.put(ProfileColumns.UUID, uuid);
        values.put(ProfileColumns.IMEI, imei);
        values.put(ProfileColumns.NAME, name);
        values.put(ProfileColumns.TYPE, type);
        values.put(ProfileColumns.SEX, sex);
        values.put(ProfileColumns.GRADE, grade);
        return values;
    }

    public static Profile getProfile(final Context context, String uuid, @NonNull final CallBack callBack) {
        if (TextUtils.isEmpty(uuid)) {
            Log.e(TAG, "getProfile: uuid = " + uuid);
            return null;
        }
        final Profile profile = queryProfileWithUuid(context.getContentResolver(), uuid);
        if (profile == null) {
            NetWorkUtils.getProfile(context, uuid, new NetWorkUtils.PushResultListener() {
                @Override
                public void pushSucceed(String type, String s1, int code, String s, String response) {
                    Log.e(TAG, "pushSucceed() called with: type = " + type + ", s1 = " + s1 + ", code = " + code + ", s = " + s + ", response = " + response + "");
                    Profile p = new Profile(s);
                    if (p.imei != null) {
                        insert(context.getContentResolver(), p);
                        callBack.onResponse(p);
                    } else {
                        callBack.onFail(new UnknownServiceException("无法获取好友信息"));
                    }
                }

                @Override
                public void pushFail(String s, String s1, int i, String s2) {
                    Log.e(TAG, "pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
                    callBack.onFail(new UnknownServiceException(s2));
                }
            });
        } else {
            callBack.onResponse(profile);
        }
        return profile;
    }

    public static Profile queryProfileWithUuid(ContentResolver resolver, String uuid) {
        Profile profile = null;
        String selection = UUID + "=?";
        try (Cursor cursor = resolver.query(CONTENT_URI, QUERY_COLUMNS, selection, new String[]{uuid}, null)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                profile = createProfile(cursor);
            }
        }
        return profile;
    }

    public static Uri insert(ContentResolver resolver, Profile profile) {
        ContentValues values = profile.createContentValues();
        if (queryProfileWithUuid(resolver, profile.uuid) != null) {
            Log.e(TAG, "insert: delete old profile, uuid = " + profile.uuid);
            delete(resolver, profile.uuid);
        }
        return resolver.insert(ProfileColumns.CONTENT_URI, values);
    }

    public static int delete(ContentResolver resolver, String uuid) {
        String where = ProfileColumns.UUID + "=?";
        return resolver.delete(ProfileColumns.CONTENT_URI, where, new String[]{uuid});
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public Friend convertFriend() {
        Friend friend = new Friend();
        friend.name = name;
        friend.uuid = uuid;
        return friend;
    }


    public interface CallBack {
        /**
         * 获取Profile成功回调，可能是通过数据库或者网络获取的。
         *
         * @param profile 结果
         */
        void onResponse(Profile profile);

        /**
         * 失败
         *
         * @param e 失败情况
         */
        void onFail(Exception e);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "uuid='" + uuid + '\'' +
                ", imei='" + imei + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", sex=" + sex +
                ", grade=" + grade +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.imei);
        dest.writeString(this.name);
        dest.writeString(this.type);
        dest.writeInt(this.sex);
        dest.writeInt(this.grade);
    }

    protected Profile(Parcel in) {
        this.uuid = in.readString();
        this.imei = in.readString();
        this.name = in.readString();
        this.type = in.readString();
        this.sex = in.readInt();
        this.grade = in.readInt();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
