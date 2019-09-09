package com.readboy.wetalk.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.readboy.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.readboy.wetalk.bean.CreateGroup;
import com.readboy.wetalk.bean.Friend;
import com.readboy.wetalk.support.R;
import com.readboy.wetalk.utils.LogInfo;
import com.readboy.wetalk.utils.WTContactUtils;
import com.readboy.wetalk.utils.WeTalkConstant;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

/**
 * @author hwj
 */
public class WetalkFrameLayout extends FrameLayout {
    private static final String TAG = "hwj_WetalkView";
    private static final String PACKAGE_NAME = "com.readboy.wetalk";
    public static final String AUTHORITY = "com.readboy.wetalk.provider.Conversation";
    public static final String TABLE_NAME = "conversation";
    public static final Uri CONVERSATION_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final String ACTION_UPDATE_NOTIFICATION = "readboy.action.UPDATE_NOTIFICATION";

    private static final String ICON_URL = "http://img.readboy.com/avatar/";
    private static final String RB_UPDATE_PHOTO_PER_HOUR = "RB_UPDATE_PHOTO_PER_HOUR";
    private static final int UPDATE_CYCLE = 60 * 60 * 1000;
    public static final String EXTRA_FRIENDS = "friends";
    public static final String EXTRA_FRIEND = "friend";
    private static final String CLASS_NAME_CONVERSATION = "com.readboy.wetalk.ConversationActivity";
    private static final String CLASS_NAME_FRIEND_SELECTOR = "com.readboy.activity.FriendSelectorActivity";
    public static final String ACTION_APPCTRL = "readboy.action.NOTIFY_APP_CTRL";
    private static final int IMAGE_WIDTH = 126;
    public static final int MAX_MESSAGE_COUNT = 100;
    private static final int MESSAGE_UPDATE_CONTACT = 10;
    private static final int MESSAGE_UPDATE_UNREAD_COUNT = 20;
    private static final int MESSAGE_CLEAR_GROUP_UNREAD_COUNT = 30;
    private static final int DELAY_UPDATE_MILLIS_TIME = 100;

    private List<Friend> mFriends = new ArrayList<>();

    private GetFriendTask mGetFriendTask;
    private EmptyRecyclerView mFriendRecyclerList;
    private MyGridLayoutManager mLayoutManager;
    private FriendRecyclerAdapter mAdapter;
    private HeaderAndFooterWrapper mWrapperAdapter;
    private View mLoading;
    private boolean isUpdating = false;
    private boolean getAgain = false;
    private boolean hasRegisterObserver = false;
    private int successCount;
    private Context mContext;
    private Activity mActivity;

    private ContentObserver mConversationObserver;
    private BroadcastReceiver mReceiver;
    private boolean isShowing = false;
    private boolean unreadCountChange = false;
    private String updateLastTime;
    private boolean lastIsGroupControlled = false;

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
                        getAgain = true;
                        Log.w(TAG, "handleMessage: udpate contact, but is getting contacts.");
                    }
                    break;
                case MESSAGE_UPDATE_UNREAD_COUNT:
                    updateUnreadCount();
                    break;
                case MESSAGE_CLEAR_GROUP_UNREAD_COUNT:
                    if (WTContactUtils.isGroupControlled(mContext)) {
                        List<Friend> groupList = WTContactUtils.getGroupFromContacts(mContext);
                        clearGroupUnreadCount(mContext, groupList);
                        sendBroadcastToUpdateNotification(mContext);
                    }
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
     * TODO use {@link android.app.LoaderManager}
     */
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mHandler.removeMessages(MESSAGE_UPDATE_CONTACT);
            mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_CONTACT, DELAY_UPDATE_MILLIS_TIME);
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
        lastIsGroupControlled = WTContactUtils.isGroupControlled(context);
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
        mLayoutManager = new MyGridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false);
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

        if (getActivity() != null && mReceiver == null) {
            mReceiver = new InnerReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_APPCTRL);
            getActivity().registerReceiver(mReceiver, filter);
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
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
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
            if (friends != null && friends.size() > 0 && !WTContactUtils.isGroupControlled(mContext)) {
                friends.add(new CreateGroup(mContext));
            }
            updateFriendData(friends);
            mAdapter.notifyDataSetChanged();
            isUpdating = false;
            if (getAgain) {
                getAgain = false;
                mHandler.removeMessages(MESSAGE_UPDATE_CONTACT);
                mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_CONTACT, DELAY_UPDATE_MILLIS_TIME);
            }
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
                if (getString(R.string.home_group).equals(f1.name)) {
                    return -2;
                } else if (getString(R.string.home_group).equals(f2.name)) {
                    return 2;
                } else if (f1.type == Friend.TYPE_CREATE_GROUP) {
                    return 3;
                } else if (f2.type == Friend.TYPE_CREATE_GROUP) {
                    return -3;
                } else if (f1.unreadCount < f2.unreadCount) {
                    return 1;
                } else if (f1.unreadCount > f2.unreadCount) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    private void gotoFriendSelector() {
        ArrayList<Friend> data = new ArrayList<>();
        for (Friend friend : mFriends) {
            if (friend.isSupportGroup()) {
                data.add(friend);
            }
        }
        if (data.size() == 0) {
            Toast.makeText(mContext, getString(R.string.no_watch_friend), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        ComponentName name = new ComponentName(PACKAGE_NAME, CLASS_NAME_FRIEND_SELECTOR);
        intent.setComponent(name);
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
                if (count >= MAX_MESSAGE_COUNT) {
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
                                    //点击时更新该好友头像
                                    updatePhoto(friend);
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
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_friend_grid, parent, false));
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

    private class InnerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_APPCTRL.equals(action)) {
                //群管控后隐藏群、清除未读消息数
                boolean isGroupControlled = WTContactUtils.isGroupControlled(context);
                if (isGroupControlled != lastIsGroupControlled) {
                    mHandler.removeMessages(MESSAGE_UPDATE_CONTACT);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_CONTACT, DELAY_UPDATE_MILLIS_TIME);
                    mHandler.removeMessages(MESSAGE_CLEAR_GROUP_UNREAD_COUNT);
                    mHandler.sendEmptyMessage(MESSAGE_CLEAR_GROUP_UNREAD_COUNT);
                    lastIsGroupControlled = isGroupControlled;
                }
            }
        }
    }

    /**
     * 更新一个联系人的头像
     */
    public void updatePhoto(final Friend friend) {
        String url = ICON_URL + friend.uuid;
        updateLastTime = friend.uuid + RB_UPDATE_PHOTO_PER_HOUR;
        if (TextUtils.isEmpty(friend.photoUri)) {
            ImageRequest request = new ImageRequest(url, new Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    // TODO Auto-generated method stub
                    insertPhoto(mContext, friend.contactId, response);
                }
            }, IMAGE_WIDTH, IMAGE_WIDTH, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.ARGB_8888, errorListener);
            getQueue(mContext).add(request);
        } else {
            long last = Settings.Global.getLong(mContext.getContentResolver(), updateLastTime, 0);
            //超过一小时才更新
            if ((System.currentTimeMillis() - last) < UPDATE_CYCLE) {
                return;
            }
            ImageRequest request = new ImageRequest(url, new Listener<Bitmap>() {

                @Override
                public void onResponse(Bitmap response) {
                    // TODO Auto-generated method stub
                    updateContactPhoto(mContext, friend.contactId, response);
                }
            }, IMAGE_WIDTH, IMAGE_WIDTH, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.ARGB_8888, errorListener);
            getQueue(mContext).add(request);
        }

    }

    /**
     * 插入一个联系人的头像
     */
    private void insertPhoto(Context context, long rawContactId, Bitmap bmp) {
        if (bmp == null) {
            return;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] avatar = os.toByteArray();
        ContentValues cv = new ContentValues();
        cv.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        cv.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        cv.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
        Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
        Log.i(TAG, "insertPhoto: uri = " + uri);
        if (uri == null) {
            Settings.Global.putLong(mContext.getContentResolver(), updateLastTime, 0);
        } else {
            Settings.Global.putLong(mContext.getContentResolver(), updateLastTime, System.currentTimeMillis());
        }
    }

    /**
     * 更新头像
     */
    private void updateContactPhoto(Context context, long rawContactId, Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        ContentValues values = new ContentValues();
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, data);
        int rows = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
                ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=? ",
                new String[]{rawContactId + "", ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE});
        Log.i(TAG, "updatePhoto: rows = " + rows);
        if (rows <= 0) {
            Settings.Global.putLong(mContext.getContentResolver(), updateLastTime, 0);
        } else {
            Settings.Global.putLong(mContext.getContentResolver(), updateLastTime, System.currentTimeMillis());
        }
    }

    static RequestQueue mQueue;

    public static RequestQueue getQueue(Context context) {
        if (mQueue == null) {
            synchronized (context) {
                if (mQueue == null) {
                    mQueue = Volley.newRequestQueue(context);
                }
            }
        }
        return mQueue;
    }

    private ErrorListener errorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            Log.i(TAG, "VolleyError = " + error.toString());
            //更新失败，置零，下次进入可以更新。
            // 404代表目标文件不存在
            if (error.networkResponse == null || error.networkResponse.statusCode != 404) {
                if (error.networkResponse != null) {
                    byte[] data = error.networkResponse.data;
                    String response = data != null ? new String(data) : "";
                    Log.i(TAG, "onErrorResponse: status = " + error.networkResponse.statusCode
                            + ", " + response);
                }
                Settings.Global.putLong(mContext.getContentResolver(), updateLastTime, 0);
            }
        }
    };

    private void clearGroupUnreadCount(Context context, List<Friend> groupList) {
        for (Friend group : groupList) {
            ContentValues values = new ContentValues();
            values.put("unread", 0);
            String where = "send_id=?";
            String[] args = new String[]{group.uuid};
            int rows = context.getContentResolver().update(CONVERSATION_URI, values, where, args);
        }
    }

    private void sendBroadcastToUpdateNotification(Context context) {
        //只有主界面的实例发送
        if ("com.readboy.wetalk".equals(context.getPackageName())) {
            return;
        }
        Intent intent = new Intent(ACTION_UPDATE_NOTIFICATION);
        ComponentName componentName = new ComponentName("com.readboy.wetalk", "com.readboy.receiver.MessageReceiver");
        intent.setComponent(componentName);
        context.sendBroadcast(intent);
    }
}
