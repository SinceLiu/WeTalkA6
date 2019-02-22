package com.readboy.utils;

import com.readboy.wetalk.bean.Friend;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 内部没有做线程同步问题
 *
 * @author oubin
 * @date 2019/1/11
 */
public class FriendNameUtil {

    private static volatile Map<String, Friend> mMembersMap = new Hashtable<>();

    public static void updateMembersMap(Map<String, Friend> map) {
        if (mMembersMap != null) {
            mMembersMap.clear();
        } else {
            mMembersMap = new Hashtable<>();
        }
        mMembersMap.putAll(map);
    }

    public static String resolveName(String uuid) {
        return resolveName(uuid, "");
    }

    public static String resolveName(String uuid, String defaultValue) {
        if (mMembersMap != null) {
            Friend friend = mMembersMap.get(uuid);
            if (friend != null) {
                return friend.name;
            }
        }
        return defaultValue;
    }

    public static void clear() {
        if (mMembersMap != null) {
            mMembersMap.clear();
            mMembersMap = null;
        }
    }

}
