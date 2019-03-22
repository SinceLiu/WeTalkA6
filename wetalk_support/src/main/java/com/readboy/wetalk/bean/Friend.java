package com.readboy.wetalk.bean;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.view.View;

import com.readboy.wetalk.utils.WTContactUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * @author hwwjian
 * @date 2016/9/21
 * 好友
 */
public class Friend implements Parcelable {

    public static final int TYPE_CREATE_GROUP = 5;

    public static final int RELATION = 200;

    /**
     * 通讯录的查询Id
     */
    public int contactId;
    /**
     * 用户名
     */
    public String name;

    /**
     * 用户的唯一标识(UUID)
     */
    public String uuid;
    /**
     * 用户头像
     */
    public byte[] avatar;
    /**
     * 用户的未读信息数
     */
    public int unreadCount;
    /**
     * 短号
     */
    public String shortPhone;
    /**
     * 手机号码
     */
    public String phone;
    /**
     * 关系0-12
     */
    public int relation;
    /**
     * 头像uri
     */
    public String photoUri;

    /**
     * 机型
     */
    public Model model;

    public int icon;

    public int type;

    /**
     * 好友群成员，uuid
     */
    public List<Friend> members;

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
    }

    @Visibility
    public int addVisibility = View.VISIBLE;

    public boolean isFriendGroup() {
        return (members != null && members.size() > 0)
                || relation == 200
                || (!"家庭圈".equals(name) && uuid.startsWith("G"));
    }

    public boolean isSupportGroup() {
        return uuid != null && uuid.startsWith("D");
    }

    /**
     * 耗时操作，不要频繁更新
     */
    public void updateAddVisibility(Context context, String myUuid) {
        boolean can = !uuid.equals(myUuid) && !WTContactUtils.isContacts(context, uuid);
        addVisibility = can ? View.VISIBLE : View.GONE;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "contactId=" + contactId +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", unreadCount=" + unreadCount +
                ", shortPhone='" + shortPhone + '\'' +
                ", phone='" + phone + '\'' +
                ", relation=" + relation +
                ", photoUri='" + photoUri + '\'' +
                ", model=" + model +
                ", icon=" + icon +
                ", type=" + type +
                ", members=" + members +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.contactId);
        dest.writeString(this.name);
        dest.writeString(this.uuid);
        dest.writeByteArray(this.avatar);
        dest.writeInt(this.unreadCount);
        dest.writeString(this.shortPhone);
        dest.writeString(this.phone);
        dest.writeInt(this.relation);
        dest.writeString(this.photoUri);
        dest.writeInt(this.model == null ? -1 : this.model.ordinal());
        dest.writeInt(this.icon);
        dest.writeInt(this.type);
        dest.writeTypedList(this.members);
    }

    public Friend() {
    }

    protected Friend(Parcel in) {
        this.contactId = in.readInt();
        this.name = in.readString();
        this.uuid = in.readString();
        this.avatar = in.createByteArray();
        this.unreadCount = in.readInt();
        this.shortPhone = in.readString();
        this.phone = in.readString();
        this.relation = in.readInt();
        this.photoUri = in.readString();
        int tmpModel = in.readInt();
        this.model = tmpModel == -1 ? null : Model.values()[tmpModel];
        this.icon = in.readInt();
        this.type = in.readInt();
        this.members = in.createTypedArrayList(Friend.CREATOR);
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel source) {
            return new Friend(source);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
}
