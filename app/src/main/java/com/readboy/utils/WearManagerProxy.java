package com.readboy.utils;

import android.app.readboy.IReadboyWearListener;
import android.app.readboy.ReadboyWearManager;
import android.app.readboy.PersonalInfo;
import android.content.Context;

import com.readboy.wetalk.bean.Friend;

import org.json.JSONObject;

/**
 * @author oubin
 * @date 2018/12/28
 */
public class WearManagerProxy {
    public static final String ACTION_ADD = "readboy.action.NOTIFY_FRIEND_ADD";
    public static final String ACTION_REFUSE = "readboy.action.NOTIFY_FRIEND_REFUSE";

    private WearManagerProxy(Context context) {
    }

    public static ReadboyWearManager getManager(Context context) {
        return (ReadboyWearManager) context.getSystemService(Context.RBW_SERVICE);
    }

    public static String getMyUuid(Context context) {
        PersonalInfo info = getManager(context).getPersonalInfo();
        if (info != null) {
            return info.getUuid();
        }
        return "";
    }

    /**
     * {
     * "q": "mgroup",
     * "k": "create",
     * "o": "123",
     * "data": {
     * "members": ["<UUID>", "<UUID>", "..."]
     * }
     * }
     *
     * {"r":"mgroup","data":{"owner":"D05C2C75FA00413B","members":["D05C2C823C00413E","D05C2C75FA00413B","D05C2C78D500413C"],"name":"宝贝1好友群①","id":"G05C305A77006198"},"o":"5"}
     */
    public static void createGroup(Context context, String data, IReadboyWearListener listener) {
        getManager(context).customRequest("mgroup", "create", data, listener);
    }

    /**
     * {
     * "q": "mgroup",
     * "k": "get",
     * "o": "123",
     * "data": {
     * "id": "GXXXXXXXXXXXXX",
     * "v": 123
     * }
     * }
     */
    public static void getGroupInfo(Context context, String data, IReadboyWearListener listener) {
        getManager(context).customRequest("mgroup", "get", data, listener);
    }

    /**
     * "q": "mgroup",
     * "k": "add",
     * "o": "123",
     * "data": {
     * "id": "GXXXXXXXXXX",
     * "members": ["<UUID>", "..."]
     * }
     */
    public static void addMember(Context context, String data, IReadboyWearListener listener) {
        getManager(context).customRequest("mgroup", "add", data, listener);
    }

    public static void groupAction(Context context, Command command, String data, IReadboyWearListener listener) {
        getManager(context).customRequest(command.cmd, command.key, data, listener);
    }

    public static void addFriend(Context context, String uuid, final IReadboyWearListener listener) {
        getManager(context).operateDeviceContacts("add", uuid, null, listener);
    }

    public static void requestAddFriend(Context context, String uuid, final IReadboyWearListener listener) {
        getManager(context).operateDeviceContacts("friending", uuid, null, listener);
    }

    public static void refuseFriend(Context context, String uuid, final IReadboyWearListener listener) {
        getManager(context).operateDeviceContacts("refuse", uuid, null, listener);
    }

    public enum Command {
        /**
         * 群操作
         */
        CREATE_GROUP("mgroup", "create"),
        GET_GROUP("mgroup", "get"),
        ADD_GROUP("mgroup", "add"),
        REMOVE_GROUP("mgroup", "kick"),
        LEAVE_GROUP("mgroup", "leave");

        private String cmd;
        private String key;

        private Command(String cmd, String key) {
            this.cmd = cmd;
            this.key = key;
        }
    }

}
