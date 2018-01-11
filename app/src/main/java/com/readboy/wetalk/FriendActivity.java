package com.readboy.wetalk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.bumptech.glide.Glide;
import com.readboy.bean.Constant;
import com.readboy.bean.Friend;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.WTContactUtils;
import com.readboy.view.EmptyRecyclerView;
import com.readboy.view.FriendGridItem;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class FriendActivity extends BaseRequestPermissionActivity {

    private List<Friend> mFriends;

    private GetFriendTask mGetFriendTask;
    private EmptyRecyclerView mFriendRecyclerList;
    private View mLoading;
    private boolean isUpdating = false;
    private Handler mHandler = new Handler();
    private boolean hasRegisterObserver = false;

    private Runnable mUpdateFriendThread = new Runnable() {

        @Override
        public void run() {
            if (!isUpdating) {
                LogInfo.i("contact notify");
                if (mGetFriendTask != null) {
                    mGetFriendTask.cancel(true);
                }
                mGetFriendTask = new GetFriendTask();
                mGetFriendTask.execute();
            }
        }
    };

    /**
     * 监听通讯录数据变化,只监听联系人未读信息数变化,不监听联系人信息变化
     */
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mHandler.removeCallbacks(mUpdateFriendThread);
            if (!isUpdating) {
                mHandler.post(mUpdateFriendThread);
            }
        }
    };


    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        LogInfo.i(" FriendActivity --- onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        mFriendRecyclerList = (EmptyRecyclerView) getView(R.id.square_friend_recycler_list);
        View mNoContact = getView(R.id.no_contact);
        mLoading = getView(R.id.loading);
        mFriendRecyclerList.setEmptyView(mNoContact);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mFriendRecyclerList.setLayoutManager(layoutManager);
        initData();
    }

    @Override
    protected void initData() {
        mGetFriendTask = new GetFriendTask();
        mGetFriendTask.execute();
    }

    @Override
    protected void onResume() {
        LogInfo.i(" FriendActivity --- onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogInfo.i(" FriendActivity --- onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogInfo.i(" FriendActivity --- onDestroy()");
        super.onDestroy();
        if (hasRegisterObserver) {
            getContentResolver().unregisterContentObserver(mObserver);
            hasRegisterObserver = false;
        }
        MPrefs.setNotificationType(this, true);
    }

    private class GetFriendTask extends AsyncTask<Void, Void, List<Friend>> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected List<Friend> doInBackground(Void... params) {
            isUpdating = true;
            return WTContactUtils.getFriendFromContact(FriendActivity.this);
        }

        @Override
        protected void onPostExecute(List<Friend> friends) {
//        	clearImageDiskCache();
            updateFriendData(friends);
            mFriendRecyclerList.setAdapter(new FriendRecyclerAdapter());
            hideLoading();
            isUpdating = false;
        }
    }

    /**
     * 清除图片磁盘缓存
     */
    public void clearImageDiskCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(FriendActivity.this).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(FriendActivity.this).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void showLoading() {
        if (mLoading.getVisibility() == View.GONE) {
            mLoading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 根据用户名获取用户的角标
     *
     * @param name 用户名
     * @return 用户角标
     */
    private int getFriendPosition(String name) {
        int pos = 0;
        for (Friend friend : mFriends) {
            if (friend.name.equals(name)) {
                break;
            }
            pos++;
        }
        return pos;
    }

    /**
     * 更新好友列表信息(用户名和未读信息数,不更新头像)
     *
     * @param friends 最新获取到的好友数据
     */
    @SuppressLint("NewApi")
    private void updateFriendData(List<Friend> friends) {
        if (friends == null || friends.size() == 0) {
            return;
        }
        if (mFriends == null) {
            mFriends = friends;
        } else {
            if (mFriends.size() != friends.size()) {
                //联系人增删了,整个重建
                mFriends.clear();
                mFriends = friends;
            } else {
                for (Friend newFriend : friends) {
                    for (Friend oldFriend : mFriends) {
                        if (oldFriend.uuid.equals(newFriend.uuid)) {
                            if (oldFriend.unreadCount != newFriend.unreadCount) {
                                //未读信息数变化
                                oldFriend.unreadCount = newFriend.unreadCount;
                            }
                            if (!oldFriend.name.equals(newFriend.name)) {
                                //用户名变化
                                oldFriend.name = newFriend.name;
                            }
                            oldFriend.photoUri = newFriend.photoUri;
                        }
                    }
                }
            }
        }
        Collections.sort(mFriends, new Comparator<Friend>() {

            @Override
            public int compare(Friend f1, Friend f2) {
                if (f1.unreadCount <= f2.unreadCount) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        int homeGroupPos = getFriendPosition(getString(R.string.homeGroup));
        if (homeGroupPos < mFriends.size()) {
            Friend homeGroup = mFriends.get(homeGroupPos);
            mFriends.remove(homeGroupPos);
            mFriends.add(0, homeGroup);
        }
    }

    private class FriendRecyclerAdapter extends RecyclerView.Adapter<FriendRecyclerAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {

            FriendGridItem item;

            public ViewHolder(View itemView) {
                super(itemView);
                item = (FriendGridItem) itemView.findViewById(R.id.friend_item);
            }

        }

        @Override
        public int getItemCount() {
            return mFriends == null ? 0 : mFriends.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Friend friend = mFriends.get(position);
            if (friend.unreadCount == 0) {
                holder.item.hideUnreadCount();
            } else {
                //根据未读数设置显示数量
                int count = friend.unreadCount;
                if (count > 99) {
                    holder.item.setUnreadCount("...");
                } else {
                    holder.item.setUnreadCount(String.valueOf(friend.unreadCount));
                }
            }
            holder.item.setFriendName(friend.name);
            holder.item.setFriendAvatar(FriendActivity.this, friend.name, friend.relation, friend.photoUri);
            //Constant.getAvatarPath(FriendActivity.this) + friend.uuid);
            holder.item.getAvatarView().setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                v.setScaleX(0.95f);
                                v.setScaleY(0.95f);
                                break;
                            case MotionEvent.ACTION_UP:
                                v.setScaleX(1.0f);
                                v.setScaleY(1.0f);
                                final Friend friend = mFriends.get(position);
                                Intent intent = new Intent(FriendActivity.this, ConversationActivity.class);
                                intent.putExtra(Constant.FRIEND_ID, friend.uuid);
                                intent.putExtra(Constant.FRIEND_NAME, friend.name);
                                intent.putExtra(Constant.FRIEND_UNREAD_COUNT, friend.unreadCount);
                                intent.putExtra(Constant.FRIEND_AVATAR, friend.avatar);
                                startActivity(intent);
                                break;
                            case MotionEvent.ACTION_CANCEL:
                                v.setScaleX(1.0f);
                                v.setScaleY(1.0f);
                                break;
                            default:
                                break;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.friend_grid_item, parent, false));
        }
    }

    @Override
    protected void initContent() {
        getContentResolver().registerContentObserver(ContactsContract.Data.CONTENT_URI, true, mObserver);
        hasRegisterObserver = true;
        setContentView(R.layout.activity_square_friend);
        initView();
        MPrefs.setNotificationType(this, false);
    }
}
