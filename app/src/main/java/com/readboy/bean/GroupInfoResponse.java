package com.readboy.bean;

import com.readboy.wetalk.bean.Friend;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oubin
 * @date 2019/1/6
 */
public class GroupInfoResponse {

    /**
     * r : mgroup
     * k : get
     * o : 123
     * data : {"id":"GXXXXXXXXXX","owner":"<UUID>","name":"xxxxxxxx","members":["<UUID>|<name>","..."],"v":123}
     */

    private String r;
    private String k;
    private String o;
    private Data data;

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
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
                String[] array = member.split("|");
                if (array != null && array.length == 2) {
                    Friend friend = new Friend();
                    friend.uuid = array[0];
                    friend.name = array[1];
                    friends.add(friend);
                }
            }
        }
    }
}
