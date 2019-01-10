package com.readboy.wetalk.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.readboy.wetalk.bean.Friend;
import com.readboy.wetalk.support.R;

/**
 * @author hwj
 */
public class FriendGridItem extends RelativeLayout {
    private static final String TAG = "hwj-FriendGridItem";

    //头像
    public ImageView mFriendImage;
    private View mAvatarMask;
    //未读信息数
    private TextView mFriendUnread;

    private TextView mFriendName;

    private View mAvatar;

    public static int[] defaultAvatars = new int[]{R.drawable.new_relation_head_0, R.drawable.new_relation_head_1,
            R.drawable.new_relation_head_2, R.drawable.new_relation_head_3, R.drawable.new_relation_head_4, R.drawable.new_relation_head_5,
            R.drawable.new_relation_head_6, R.drawable.new_relation_head_7, R.drawable.new_relation_head_8, R.drawable.new_relation_head_9,
            R.drawable.new_relation_head_10, R.drawable.new_relation_head_11, R.drawable.new_relation_head_12};

    public FriendGridItem(Context context) {
        super(context);
    }

    public FriendGridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FriendGridItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //初始化界面控件
        mFriendImage = (ImageView) findViewById(R.id.friend_pager_item_img);
        mAvatarMask = findViewById(R.id.friend_pager_item_img_mask);
        mFriendUnread = (TextView) findViewById(R.id.friend_pager_item_unread);
        mFriendUnread.setActivated(false);
        mFriendName = (TextView) findViewById(R.id.friend_pager_item_name);
        mAvatar = findViewById(R.id.avatar);
    }

    /**
     * 隐藏未读信息数量
     */
    public void hideUnreadCount() {
        if (mFriendUnread != null) {
            mFriendUnread.setVisibility(View.GONE);
        }
    }

    /**
     * 设置未读信息数
     *
     * @param count 数量
     */
    public void setUnreadCount(String count, boolean isSmall) {
        if (mFriendUnread != null) {
            mFriendUnread.setText(count);
            if (isSmall) {
                if (!mFriendUnread.isActivated()) {
                    mFriendUnread.setTextSize(14);
                    mFriendUnread.setActivated(true);
                }
            } else {
                if (mFriendUnread.isActivated()) {
                    mFriendUnread.setTextSize(16);
                    mFriendUnread.setActivated(false);
                }
            }
            if (mFriendUnread.getVisibility() == View.GONE) {
                mFriendUnread.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setFriendAvatar(Context context, Friend friend) {
        int relation = friend.relation;
        String path = friend.photoUri;
        if (friend.icon > 0) {
//            mFriendImage.setImageResource(friend.icon);
            Glide.with(context)
                    .load(friend.icon)
                    .placeholder(friend.icon)
                    .dontAnimate()
                    .centerCrop()
                    .error(R.drawable.common)
                    .into(mFriendImage);
        } else if (friend.isFriendGroup()) {
            mFriendImage.setImageResource(R.drawable.ic_friend_group);
        } else {
            if (path != null) {
                //好友头像有存储在本地
                Glide.with(context)
                        .load(path)
                        //增加签名
                        .signature(new StringSignature("01"))
                        .dontAnimate()
                        .centerCrop()
                        .error(R.drawable.common)
                        .into(mFriendImage);
            } else {
                if (relation < defaultAvatars.length) {
                    if (relation < 0) {
                        relation = 0;
                    }
                    //没有头像,根据关系设置默认头像
                    Glide.with(context)
                            .load(defaultAvatars[relation])
                            .placeholder(R.drawable.common)
                            .centerCrop()
                            .error(R.drawable.common)
                            .dontAnimate()
                            .into(mFriendImage);
                } else {
                    //没有头像,根据关系设置默认头像
                    Glide.with(context)
                            .load(R.drawable.common)
                            .placeholder(R.drawable.common)
                            .centerCrop()
                            .error(R.drawable.common)
                            .dontAnimate()
                            .into(mFriendImage);
                }
            }
        }
    }

    public void setFriendName(String name) {
        if (mFriendName != null && !TextUtils.isEmpty(name)) {
            mFriendName.setText(name);
        }
    }

    public View getFriendImageView() {
        return mFriendImage;
    }

    public View getMask() {
        return mAvatarMask;
    }

    public View getAvatarView() {
        return mAvatar;
    }

}
