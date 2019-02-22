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

/**
 * @author oubin
 * @date 2018/12/21
 */
public class GroupMembersAdapter extends BaseAdapter<Friend, GroupMembersAdapter.MemberViewHolder> {
    private static final String TAG = "GroupMembersAdapter";

    public static final int ACTION_ADD = 1;
    public static final int ACTION_REMOTE = 2;
    private static final int TYPE_NORMAL = 2;
    private static final int TYPE_ACTION_ADD = 3;
    private static final int TYPE_ACTION_REMOVE = 4;
    private static final int TYPE_FOOTER = 5;

    private boolean isOwner = false;
    private int extraCount = 2;


    public GroupMembersAdapter(Context context, boolean isOwner) {
        super(context);
        this.isOwner = isOwner;
        extraCount = isOwner ? 3 : 2;
    }

    public void setOwnerState(boolean isOwner) {
        this.isOwner = isOwner;
        extraCount = isOwner ? 3 : 2;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        MemberViewHolder viewHolder;
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.footer, viewGroup, false);
            viewHolder = new FooterViewHolder(view, isOwner);
        } else if (viewType == TYPE_ACTION_ADD) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_action, viewGroup, false);
            viewHolder = new ActionViewHolder(view, TYPE_ACTION_ADD);
        } else if (viewType == TYPE_ACTION_REMOVE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_action, viewGroup, false);
            viewHolder = new ActionViewHolder(view, TYPE_ACTION_REMOVE);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_group_members, viewGroup, false);
            viewHolder = new FriendViewHolder(view);
        }
        viewHolder.setOnInnerClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MemberViewHolder viewHolder, int i) {
        if (isFooterPosition(i) || isAddActionPosition(i) || isRemoveActionPosition(i)) {
            return;
        }
        viewHolder.bindView(mDataList.get(i));
    }

    @Override
    public int getItemCount() {
        return mDataList.size() + extraCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterPosition(position)) {
            return TYPE_FOOTER;
        } else if (isAddActionPosition(position)) {
            return TYPE_ACTION_ADD;
        } else if (isRemoveActionPosition(position)) {
            return TYPE_ACTION_REMOVE;
        } else {
            return TYPE_NORMAL;
        }
    }

    public boolean isFooterPosition(int position) {
        return getItemCount() - 1 == position;
    }

    public boolean isAddActionPosition(int position) {
        return getItemCount() - extraCount == position;
    }

    public boolean isRemoveActionPosition(int position) {
        return isOwner && getItemCount() - extraCount + 1 == position;
    }

    @Deprecated
    private int getFooterPosition() {
        return getItemCount() - 1;
    }

    private int getAddActionPosition() {
        return getItemCount() - 3;
    }

    private int getRemoveActionPosition() {
        return getItemCount() - 2;
    }

    abstract class MemberViewHolder extends BaseViewHolder<Friend> {
        MemberViewHolder(View itemView) {
            super(itemView);
        }
    }

    class FriendViewHolder extends MemberViewHolder {
        private ImageView mIcon;
        private View mAdd;
        private TextView mName;
        private Context mContext;

        FriendViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            itemView.findViewById(R.id.group_members_parent)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            handlerInnerItemClickEvent();
                        }
                    });
            mIcon = (ImageView) itemView.findViewById(R.id.group_members_icon);
            mAdd = itemView.findViewById(R.id.group_members_add);
            mName = itemView.findViewById(R.id.group_members_name);
        }

        @Override
        public void bindView(Friend friend) {
            if (!TextUtils.isEmpty(friend.photoUri)) {
                Glide.with(mContext)
                        .load(friend.photoUri)
                        .error(R.drawable.common)
                        .transform(new GlideRoundTransform(mContext, 32))
                        .into(mIcon);
            } else {
                String url = Constant.ICON_URL + friend.uuid;
                Glide.with(mContext)
                        .load(url)
                        .error(R.drawable.common)
                        .transform(new GlideRoundTransform(mContext, 32))
                        .into(mIcon);
            }
            mAdd.setVisibility(friend.addVisibility);
            mName.setText(friend.name);
        }

    }

    class ActionViewHolder extends MemberViewHolder {

        private ImageView action;

        ActionViewHolder(View itemView, int a) {
            super(itemView);
            action = (ImageView) itemView.findViewById(R.id.group_members_action);
            if (a == TYPE_ACTION_ADD) {
                action.setImageResource(R.drawable.btn_action_add_selector);
            } else {
                action.setImageResource(R.drawable.btn_action_remove_selector);
            }
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handlerInnerItemClickEvent();

                }
            });
        }
    }

    class FooterViewHolder extends MemberViewHolder {

        FooterViewHolder(View itemView, boolean isOwner) {
            super(itemView);
            ImageView footer = itemView.findViewById(R.id.footer);
            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handlerInnerItemClickEvent();
                }
            });
            if (!isOwner) {
                footer.setImageResource(R.drawable.btn_withdraw_group_normal);
            }
        }

        @Override
        public void bindView(Friend info) {

        }
    }

}
