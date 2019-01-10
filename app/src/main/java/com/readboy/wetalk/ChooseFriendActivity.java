package com.readboy.wetalk;

import java.io.File;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.readboy.bean.Constant;
import com.readboy.wetalk.bean.Friend;
import com.readboy.utils.GlideCircleTransform;
import com.readboy.wetalk.utils.WTContactUtils;
import com.readboy.view.CircleImageView;

public class ChooseFriendActivity extends BaseRequestPermissionActivity {
    private static final String TAG = "ChooseFriendActivity";

    private RecyclerView mFriendList;
    private View mLoading;
    private View mNoContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        mFriendList = (RecyclerView) getView(R.id.choose_friend_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ChooseFriendActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mFriendList.setLayoutManager(mLayoutManager);
        mLoading = getView(R.id.loading);
        mNoContact = getView(R.id.no_contact);
        initData();
    }

    @Override
    protected void initData() {
        new GetFriendTask().execute();
    }

    private class GetFriendTask extends AsyncTask<Void, Void, List<Friend>> {

        @Override
        protected List<Friend> doInBackground(Void... params) {
            return WTContactUtils.getFriendFromContacts(ChooseFriendActivity.this);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(List<Friend> friends) {
            hideLoading();
            if (friends == null || friends.size() == 0) {
                mNoContact.setVisibility(View.VISIBLE);
            } else {
                mFriendList.setAdapter(new ChooseFriendListAdapter(friends));
            }
        }
    }

    private void showImage(String path, ImageView content) {
        Glide.with(this)
                .load(new File(path))
                .placeholder(R.drawable.common)
                .error(R.drawable.common)
                .transform(new GlideCircleTransform(this))
                .dontAnimate()
                .into(content);
    }

    private class ChooseFriendListAdapter extends RecyclerView.Adapter<ChooseFriendListAdapter.ViewHolder> {

        final List<Friend> mFriends;

        ChooseFriendListAdapter(List<Friend> friends) {
            mFriends = friends;
        }

        @Override
        public int getItemCount() {
            return mFriends == null ? 0 : mFriends.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mFriends == null || mFriends.size() == 0) {
                return;
            }
            final Friend friend = mFriends.get(position);
            if (friend.avatar == null && friend.name.equals(getResources().getString(R.string.homeGroup))) {
                holder.avatar.setImageResource(R.drawable.ic_family_group);
            } else if (friend.avatar == null) {
                holder.avatar.setImageResource(R.drawable.common);
            } else {
                showImage(Constant.getAvatarPath(ChooseFriendActivity.this) + friend.uuid, holder.avatar);

            }
            holder.name.setText(friend.name);
            holder.item.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = getIntent();
                    if (intent == null || TextUtils.isEmpty(intent.getStringExtra("share_image"))) {
                        showMsg(getString(R.string.share_error));
                        finish();
                        Log.e(TAG, "onClick: intent = null.");
                        return;
                    }
                    Intent sendImageIntent = new Intent(ChooseFriendActivity.this, ConversationActivity.class);
                    sendImageIntent.putExtra(Constant.FROM, Constant.SHOW_CHAT);
                    sendImageIntent.putExtra(Constant.EXTRA_FRIEND_ID, friend.uuid);
                    sendImageIntent.putExtra(Constant.EXTRA_FRIEND_NAME, friend.name);
                    sendImageIntent.putExtra(Constant.FRIEND_UNREAD_COUNT, friend.unreadCount);
                    sendImageIntent.putExtra(Constant.FRIEND_AVATAR, friend.avatar);
                    sendImageIntent.putExtra(Constant.SHARE_IMAGE_PATH, intent.getStringExtra("share_image"));
                    startActivity(sendImageIntent);
                    finish();
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.choose_friend_item, parent, false));
        }

        class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {

            CircleImageView avatar;
            TextView name;
            View item;

            public ViewHolder(View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.choose_friend_item);
                avatar = (CircleImageView) itemView.findViewById(R.id.choose_friend_item_avatar);
                name = (TextView) itemView.findViewById(R.id.choose_friend_item_name);
            }

        }
    }

    /**
     * 隐藏加载中动画
     */
    private void hideLoading() {
        Animator hide = ObjectAnimator.ofFloat(mLoading, "alpha", 0);
        hide.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                mLoading.setVisibility(View.GONE);
            }
        });
        hide.start();
    }

    @Override
    protected void initContent() {
        setContentView(R.layout.activity_choose_friend);
        initView();
    }
}
