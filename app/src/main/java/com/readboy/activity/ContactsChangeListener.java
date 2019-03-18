package com.readboy.activity;

import android.util.SparseArray;

import com.readboy.wetalk.bean.Friend;

import java.util.Map;

/**
 *
 * @author oubin
 * @date 2019/3/18
 */
public interface ContactsChangeListener {

    /**
     * 联系人变动
     * @param map 变动后的联系人，key为uuid
     */
    void onChange(Map<String, Friend> map);
}
