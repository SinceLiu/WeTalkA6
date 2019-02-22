package com.readboy.wetalk.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.readboy.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.readboy.wetalk.bean.CreateGroup;
import com.readboy.wetalk.bean.Friend;
import com.readboy.wetalk.support.R;
import com.readboy.wetalk.utils.LogInfo;
import com.readboy.wetalk.utils.WTContactUtils;
import com.readboy.wetalk.utils.WeTalkConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author hwj
 */
public class WetalkFrameLayout extends FrameLayout {
    private static final String TAG = "hwj_WetalkView";
    private static final String PACKAGE_NAME = "com.readboy.wetalk";
    public static final String EXTRA_FRIENDS = "friends";
    public static final String EXTRA_FRIEND = "friend";
    private static final String CLASS_NAME_CONVERSATION = "com.readboy.wetalk.ConversationActivity";
    private static final String CLASS_NAME_FRIEND_SELECTOR = "com.readboy.activity.FriendSelectorActivity";

    public static final int MAX_MESSAGE_COUNT = 100;
    private static final int MESSAGE_UPDATE_CONTACT = 10;
    private static final int MESSAGE_UPDATE_UNREAD_COUNT = 20;
    private static final int DELAY_UPDATE_MILLIS_TIME = 100;

    private List<Friend> mFriends = new ArrayList<>();

    private GetFriendTask mGetFriendTask;
    private EmptyRecyclerView mFriendRecyclerList;
    private GridLayoutManager mLayoutManager;
    private FriendRecyclerAdapter mAdapter;
    private HeaderAndFooterWrapper mWrapperAdapter;
    private View mLoading;
    private boolean isUpdating = false;
    private boolean hasRegisterObserver = false;
    private int successCount;
    private Context mContext;
    private Activity mActivity;

    private ContentObserver mConversationObserver;
    private boolean isShowing = false;
    private boolean unreadCountChange = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_CONTACT:
                    if (!isUpdating) {
                        if (mGetFriendTask != null) {
                            mGetFriendTask.cancel(true);
                        }
                        mGetFriendTask = new GetFriendTask();
                        mGetFriendTask.execute();
                    } else {
                        Log.w(TAG, "handleMessage: udpate contact, but is getting contacts.");
                    }
                    break;
                case MESSAGE_UPDATE_UNREAD_COUNT:
                    updateUnreadCount();
                    break;
                default:
                    break;
            }
        }
    };

    private Runnable mUpdateFriendThread = new Runnable() {

        @Override
        public void run() {
            if (!isUpdating) {
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
            mHandler.removeMessages(MESSAGE_UPDATE_CONTACT);
            if (!isUpdating) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_CONTACT, DELAY_UPDATE_MILLIS_TIME);
            }
        }

    };

    public WetalkFrameLayout(Context context) {
        this(context, null);
    }

    public WetalkFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WetalkFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.pager_friend, this);
        initView();
        initData();
        registerObserver();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "onFinishInflate: ");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "onAttachedToWindow: ");
        isShowing = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow: ");
        isShowing = false;
        unreadCountChange = false;
    }

    protected void initView() {
        mFriendRecyclerList = findViewById(R.id.square_friend_recycler_list);
        View mNoContact = findViewById(R.id.no_contact);
        mLoading = findViewById(R.id.loading);
        mFriendRecyclerList.setEmptyView(mNoContact);
        mLayoutManager = new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false);
        mFriendRecyclerList.setLayoutManager(mLayoutManager);
        mAdapter = new FriendRecyclerAdapter();
        // 异步获取数据
        mFriendRecyclerList.setAdapter(mAdapter);
    }

    protected void initData() {
        mGetFriendTask = new GetFriendTask();
        mGetFriendTask.execute();
    }

    private void registerObserver() {
        if (getActivity() != null && !hasRegisterObserver) {
            getActivity().getContentResolver().registerContentObserver(ContactsContract.Data.CONTENT_URI,
                    true, mObserver);
            hasRegisterObserver = true;
        }
        if (getActivity() != null && mConversationObserver == null) {
            mConversationObserver = new ConversationObserver(new Handler());
            getActivity().getContentResolver().registerContentObserver(WeTalkConstant.CONVERSATION_URI,
                    true, mConversationObserver);
        }

    }

    public void onResume() {
        isShowing = true;
        if (unreadCountChange) {
            updateUnreadCount();
            unreadCountChange = false;
        }
    }

    public void onPause() {
        isShowing = false;
        unreadCountChange = false;
    }

    public void onDestroy() {
        LogInfo.i(TAG, " FriendActivity --- onDestroy()");
        if (hasRegisterObserver) {
            getActivity().getContentResolver().unregisterContentObserver(mObserver);
            hasRegisterObserver = false;
        }
        if (mConversationObserver != null) {
            getActivity().getContentResolver().unregisterContentObserver(mConversationObserver);
            mConversationObserver = null;
        }
    }

    private class GetFriendTask extends AsyncTask<Void, Void, List<Friend>> {

        @Override
        protected void onPreExecute() {
            //因为收到新消息，改写了未读信息数，所有也会回调到该方法，所有此处不显示加载窗口。
//            showLoading();
        }

        @Override
        protected List<Friend> doInBackground(Void... params) {
            isUpdating = true;
            return WTContactUtils.getFriendFromContacts(mContext);
        }

        @Override
        protected void onPostExecute(List<Friend> friends) {
//        	clearImageDiskCache();
            if (mLoading != null) {
//                hideLoading();
                mLoading.setVisibility(GONE);
            }
            sortFriends(friends);
            if (friends != null && friends.size() > 0) {
                friends.add(new CreateGroup());
            }
            updateFriendData(friends);
            mAdapter.notifyDataSetChanged();
            isUpdating = false;
        }
    }

    /**
     * 清除图片磁盘缓存
     */
    private void clearImageDiskCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(mContext).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(mContext).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏加载中动画
     */
    private void hideLoading() {
        Animator hide = ObjectAnimator.ofFloat(mLoading, "alpha", 1.0F, 0);
        hide.setDuration(500);
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
     * @param friends 最新获取到的好友数据，不包括创建群
     */
    @SuppressLint("NewApi")
    private void updateFriendData(List<Friend> friends) {
        mFriends.clear();
        if (friends == null || friends.size() == 0) {
            return;
        }
        mFriends.addAll(friends);
    }

    private void sortFriends(List<Friend> friends) {
        Collections.sort(friends, new Comparator<Friend>() {

            @Override
            public int compare(Friend f1, Friend f2) {
                if ("家庭圈".equals(f1.name)) {
                    return -2;
                } else if ("家庭圈".equals(f2.name)) {
                    return 2;
                } else if (f1.type == Friend.TYPE_CREATE_GROUP) {
                    return 3;
                } else if (f2.type == Friend.TYPE_CREATE_GROUP) {
                    return -3;
                } else if (f1.unreadCount < f2.unreadCount) {
                    return 1;
                } else if (f1.unreadCount > f2.unreadCount){
                    return -1;
                } else {
                    return 0;
                }
            }
        });
//        int homeGroupPos = getFriendPosition(getString(R.string.wetalk_home_group));
//        if (homeGroupPos < friends.size()) {
//            Friend homeGroup = friends.get(homeGroupPos);
//            homeGroup.icon = R.drawable.ic_family_group;
//            friends.remove(homeGroupPos);
//            friends.add(0, homeGroup);
//        }
//        for (Friend friend : friends) {
//            if (friend.type == Friend.TYPE_CREATE_GROUP) {
//                friends.remove(friend);
//                friends.add(friend);
//            }
//        }

    }

    private void gotoFriendSelector() {
        Intent intent = new Intent();
        ComponentName name = new ComponentName(PACKAGE_NAME, CLASS_NAME_FRIEND_SELECTOR);
        intent.setComponent(name);
        ArrayList<Friend> data = new ArrayList<>();
        for (Friend friend : mFriends) {
            if (friend.isSupportGroup()) {
                data.add(friend);
            }
        }
        intent.putParcelableArrayListExtra(EXTRA_FRIENDS, data);
        startActivity(intent);
    }

    public void addHeader(int resId) {
        View view = LayoutInflater.from(mContext).inflate(resId, null, false);
        addHeader(view);
    }

    public void addHeader(View view) {
        if (mWrapperAdapter == null) {
            mWrapperAdapter = new HeaderAndFooterWrapper(mAdapter);
        }
        mWrapperAdapter.addHeaderView(view);
        mFriendRecyclerList.setAdapter(mWrapperAdapter);
        mWrapperAdapter.notifyDataSetChanged();
    }

    public RecyclerView getRecyclerView() {
        return mFriendRecyclerList;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
        registerObserver();
    }

    private Activity getActivity() {
        return mActivity;
    }

    private void startActivity(Intent intent) {
        getActivity().startActivity(intent);
    }

    private String getString(int resId) {
        return getResources().getString(resId);
    }

    private void gotoConversation(Friend friend) {
        Intent intent = new Intent();
        intent.setClassName(PACKAGE_NAME, CLASS_NAME_CONVERSATION);
        intent.putExtra(EXTRA_FRIEND, friend);
        startActivity(intent);
    }

    private void updateUnreadCount() {
        Log.i(TAG, "updateUnreadCount: 1  >> " + Thread.currentThread().getName());
        if (mFriends == null || mFriends.size() <= 0) {
            Log.i(TAG, "updateUnreadCount: mFriends = " + mFriends);
            return;
        }
        List<Friend> friends = new ArrayList<>(mFriends);
        if (friends.size() > 0) {
            for (Friend friend : friends) {
                if (friend.uuid != null && friend.type != Friend.TYPE_CREATE_GROUP) {
                    friend.unreadCount = WTContactUtils.getUnreadMessageCount(mContext, friend.uuid);
                }
            }
        }
        sortFriends(friends);
        updateFriendData(friends);
        mAdapter.notifyDataSetChanged();
        Log.i(TAG, "updateUnreadCount: 2  >> ");
    }

    private class FriendRecyclerAdapter extends RecyclerView.Adapter<FriendRecyclerAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            FriendGridItem item;

            ViewHolder(View itemView) {
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
                if (count > MAX_MESSAGE_COUNT) {
                    holder.item.setUnreadCount("99+", true);
                } else {
                    holder.item.setUnreadCount(String.valueOf(friend.unreadCount), false);
                }
            }
            holder.item.setFriendName(friend.name);
            holder.item.setFriendAvatar(mContext, friend);
            holder.item.getAvatarView().setTag(position);
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
                                final int p = (int) v.getTag();
                                final Friend friend = mFriends.get(p);
                                if (friend.type == Friend.TYPE_CREATE_GROUP) {
                                    gotoFriendSelector();
                                } else {
                                    gotoConversation(friend);
                                }
                                v.performClick();
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
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_friend_gride, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

    private class ConversationObserver extends ContentObserver {

        public ConversationObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.i(TAG, "onChange: ");
            if (isShowing) {
                mHandler.removeMessages(MESSAGE_UPDATE_UNREAD_COUNT);
                mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_UNREAD_COUNT, 200);
            } else {
                unreadCountChange = true;
            }
        }
    }


}
