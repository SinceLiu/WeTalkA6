package com.readboy.bean;

import android.util.Log;

import com.readboy.wetalk.bean.Friend;

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

    private String id;
    private String owner;
    private String name;
    private int v;
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

    private void parseMembers() {
        friends = new ArrayList<>();
        for (String member : members) {
            String[] array = member.split("\\|");
            if (array != null && array.length == 2) {
                Friend friend = new Friend();
                friend.uuid = array[0];
                friend.name = array[1];
                friends.add(friend);
            } else if (array != null && array.length == 1) {
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

}
