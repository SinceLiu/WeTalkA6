package com.readboy.wetalk.bean;


import android.content.Context;

import com.readboy.wetalk.support.R;

/**
 *
 * @author oubin
 * @date 2018/12/30
 */
public class CreateGroup extends Friend {

    public CreateGroup(Context context) {
        // 防止内部引用出错
        this.uuid = "createGroup";
        this.name = context.getString(R.string.create_group);
        this.icon = R.drawable.ic_create_group;
        this.type = TYPE_CREATE_GROUP;
    }

 }
