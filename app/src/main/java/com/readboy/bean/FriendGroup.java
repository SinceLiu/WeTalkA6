package com.readboy.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.readboy.utils.JsonMapper;
import com.readboy.wetalk.bean.Friend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oubin
 * @date 2018/12/30
 */
public class FriendGroup implements Parcelable {
    private static final String TAG = "hwj_FriendGroup";

    public String owner;
    public Friend friend;

    public FriendGroup() {
    }

    public String getUuid() {
        if (friend != null) {
            return friend.uuid;
        } else {
            return null;
        }
    }

    /**
     * '
     * "data": {
     * "id": "GXXXXXXXXXX",
     * "owner": "<UUID>",
     * "name": "xxxxxxxx",
     * "members": ["<UUID>", "<UUID>", "..."],
     * "v": 123
     * }
     *
     * @param data JSON数据
     * @return FriendGroup
     */
    public static FriendGroup parseData(String data) {
        GroupInfo groupInfo = JsonMapper.fromJson(data, GroupInfo.class);
        if (groupInfo != null) {
            FriendGroup friendGroup = new FriendGroup();
            friendGroup.owner = groupInfo.getOwner();
            Friend friend = new Friend();
            friend.members = groupInfo.getFriends();
            friend.uuid = groupInfo.getId();
            friend.name = groupInfo.getName();
            friendGroup.friend = friend;
            return friendGroup;
        }
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.owner);
        dest.writeParcelable(this.friend, flags);
    }

    protected FriendGroup(Parcel in) {
        this.owner = in.readString();
        this.friend = in.readParcelable(Friend.class.getClassLoader());
    }

    public static final Parcelable.Creator<FriendGroup> CREATOR = new Parcelable.Creator<FriendGroup>() {
        @Override
        public FriendGroup createFromParcel(Parcel source) {
            return new FriendGroup(source);
        }

        @Override
        public FriendGroup[] newArray(int size) {
            return new FriendGroup[size];
        }
    };
}
