package com.readboy.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.readboy.bean.Constant;
import com.readboy.wetalk.bean.Friend;
import com.readboy.utils.GlideRoundTransform;
import com.readboy.wetalk.R;

import java.io.File;

/**
 * @author oubin
 * @date 2018/12/22
 */
public class FriendSelectorAdapter extends BaseCheckAdapter<Friend, FriendSelectorAdapter.ViewHolder> {
    private static final String TAG = "FriendSelectorAdapter";

    public FriendSelectorAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_friend_selector, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setOnInnerClickListener(this);
        viewHolder.setOnCheckedChangeListener(this);
        return viewHolder;
    }

    class ViewHolder extends BaseCheckViewHolder<Friend> {

        private ImageView mIcon;
        private TextView mName;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                mCheckBox.setChecked(!mCheckBox.isChecked());
                handleCheckedChangeEvent();
                handlerInnerItemClickEvent();
            });
            mIcon = itemView.findViewById(R.id.group_action_icon);
            mName = itemView.findViewById(R.id.group_action_name);
        }

        @Override
        public void bindView(int position, boolean isChecked, Friend friend) {
            super.bindView(position, isChecked, friend);
            Log.i(TAG, "bindView() called with: position = " + position + ", isChecked = " + isChecked + ", friend = " + friend + "");
            if (!TextUtils.isEmpty(friend.photoUri)) {
                Glide.with(mContext)
                        .load(friend.photoUri)
                        .error(R.drawable.common)
                        .transform(new GlideRoundTransform(mContext, 35))
                        .into(mIcon);
            } else {
                Glide.with(mContext)
                        .load(Constant.getAvatarUrl(friend.uuid))
                        .error(R.drawable.common)
                        .transform(new GlideRoundTransform(mContext, 35))
                        .into(mIcon);
            }
            mName.setText(friend.name);
        }

        @Override
        int getCheckBoxId() {
            return R.id.group_action_check_box;
        }

    }
}
