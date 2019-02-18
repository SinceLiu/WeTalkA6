package com.readboy.bean;

import org.json.JSONObject;

/**
 * @author oubin
 * @date 2019/2/14
 * TODO 重构，不同消息类型，避免无数的if else or switch，代码结构清晰。
 */
public class VideoConversation {

    /**
     * 子类可以重写
     *
     * @return 上发给服务器的json数据
     */
    public String toJsonData() {
        JSONObject jsonObject = new JSONObject();

        return jsonObject.toString();
    }

}
