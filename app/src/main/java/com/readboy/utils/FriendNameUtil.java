package com.readboy.utils;

import com.readboy.wetalk.bean.Friend;

import java.util.Map;

/**
 *
 * @author oubin
 * @date 2019/1/11
 */
public class FriendNameUtil {

    public static Map<String, Friend> mMembersMap;

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
