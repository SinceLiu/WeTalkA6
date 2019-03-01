package com.readboy.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.provider.WeTalkContract.GroupColumns;
import com.readboy.utils.JsonMapper;
import com.readboy.wetalk.bean.Friend;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oubin
 * @date 2019/1/6
 */
public class GroupInfo {
    private static final String TAG = "hwj_GroupInfo";

    /**
     * id : GXXXXXXXXXX
     * owner : <UUID>
     * name : xxxxxxxx
     * members : ["<UUID>|<name>","..."]
     * v : 123
     */
    /**
     * {
     *     "owner": "D05C2C75FA00413B",
     *     "members": [
     *         "D05C2C75FA00413B|OB-A6",
     *         "D05C63B49F002561|hwja6",
     *         "D05C2C78D500413C|Hwj-A6"
     *     ],
     *     "name": "OB-A6好友群②",
     *     "created_at": "2019-02-18 10:03:34.0",
     *     "id": "G05C6A127600AD7E"
     * }
     */

    private String id;
    private String owner;
    private String name;
    private int v;
    private String membersJsonStr;
    private List<String> members;
    private List<Friend> friends;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
        parseMembers();
    }

    /**
     * 直接返回，快捷，但是外部可以修改该对象{@link #friends}
     */
    public List<Friend> getFriends() {
        if (friends != null) {
            return friends;
        }
        parseMembers();
        return friends;
    }

    private void parseMembersJson() {
        if(TextUtils.isEmpty(membersJsonStr)) {
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(membersJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        members = JsonMapper.fromJsonArray(membersJsonStr, String.class);
    }

    private void parseMembers() {
        if (members == null) {
            parseMembersJson();
        }
        if (members == null) {
            return;
        }
        friends = new ArrayList<>();
        for (String member : members) {
            String[] array = member.split("\\|");
            if (array.length == 2) {
                Friend friend = new Friend();
                friend.uuid = array[0];
                friend.name = array[1];
                friends.add(friend);
            } else if (array.length == 1) {
                Friend friend = new Friend();
                friend.uuid = array[0];
                friends.add(friend);
            } else {
                Friend friend = new Friend();
                friend.uuid = member;
                friends.add(friend);
            }
        }
    }

    public static GroupInfo createGroupInfo(Cursor cursor) {
        if (cursor == null) {
            Log.w(TAG, "createGroupInfo: cursor == null.");
            return null;
        }
        GroupInfo info = new GroupInfo();
        info.id = cursor.getString(GroupColumns.INDEX_UUID);
        info.owner = cursor.getString(GroupColumns.INDEX_OWNER);
        info.name = cursor.getString(GroupColumns.INDEX_NAME);
        info.membersJsonStr = cursor.getString(GroupColumns.INDEX_MEMBERS);
        info.v = cursor.getInt(GroupColumns.INDEX_VERSION);
        return info;
    }

    public static GroupInfo createGroupInfo(ContentValues values) {
        GroupInfo info = new GroupInfo();
        info.id = values.getAsString(GroupColumns.UUID);
        return info;
    }

    public ContentValues createContentValues() {
        ContentValues values = new ContentValues();
        values.put(GroupColumns.UUID, id);
        values.put(GroupColumns.OWNER, owner);
        values.put(GroupColumns.NAME, name);
        if (TextUtils.isEmpty(membersJsonStr)) {
            membersJsonStr = JsonMapper.toJson(members);
        }
        values.put(GroupColumns.MEMBERS, membersJsonStr);
        values.put(GroupColumns.VERSION, v);
        return values;
    }

    @Override
    public String toString() {
        return "GroupInfo{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", v=" + v +
                ", membersJsonStr='" + membersJsonStr + '\'' +
                ", members=" + members +
                '}';
    }
}
