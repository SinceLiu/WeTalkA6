package com.readboy.wetalk;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.readboy.bean.Constant;
import com.readboy.bean.GroupInfo;
import com.readboy.bean.GroupInfoManager;
import com.readboy.provider.Conversations;
import com.readboy.utils.FriendNameUtil;
import com.readboy.utils.NotificationUtils;
import com.readboy.wetalk.bean.Friend;
import com.readboy.view.ConversationView;
import com.readboy.view.GroupMembersView;
import com.readboy.widget.ImageIndication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oubin
 * @date 2018/12/28
 */
public class ConversationActivity extends BaseActivity implements GroupInfoManager.CallBack {

    private static final int DELAY_LOAD_TIME = 1000;

    private ViewPager mViewPager;
    private List<View> mViewList = new ArrayList<>();
    private boolean isGroup = false;
    private GroupInfo mGroup;
    private Friend mFriend;
    private GroupMembersView mMembersView;
    private ConversationView mConversationView;
    private Handler mHandler = new Handler();
    private Runnable mGroupRunnable;
    private boolean hadUpdateGroupInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        Friend friend = mFriend = intent.getParcelableExtra(Constant.EXTRA_FRIEND);
        Log.i(TAG, "onCreate: friend = " + friend.toString());
        final String uuid = friend.uuid;
        if (friend.isFriendGroup()) {
            isGroup = true;
            GroupInfo info = GroupInfoManager.getDataFormDatabase(this, uuid);
            if (info != null && info.getFriends() != null) {
                updateMembersMap(info);
            }
        }
        Log.i(TAG, "onCreate: isFriendGroup = " + isGroup);

        initView();
        initData();
        NotificationUtils.cancelMessageNotification(this);
//        clearUnreadCount(this, mFriend.uuid);
    }

    private void updateGroupInfoFromNet(String uuid) {
        hadUpdateGroupInfo = true;
        mGroupRunnable = new Runnable() {
            @Override
            public void run() {
                GroupInfoManager.getGroupInfoFromNet(ConversationActivity.this,
                        uuid, ConversationActivity.this);
            }
        };
        mHandler.post(mGroupRunnable);
    }

    private void updateMembersMap(GroupInfo info) {
        Log.i(TAG, "updateMembersMap: ");
        mGroup = info;
        Map<String, Friend> map = new HashMap<>(info.getMembers().size());
        for (Friend f : info.getFriends()) {
            map.put(f.uuid, f);
        }
        FriendNameUtil.mMembersMap = map;
    }

    @Override
    protected void initView() {
        assignView();
    }

    @Override
    protected void initData() {
    }

    private void assignView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mConversationView = (ConversationView) inflater.inflate(R.layout.page_conversation, null);
        mConversationView.setActivity(this);
        mViewList.add(mConversationView);
        mViewPager = findViewById(R.id.conversation_view_pager);
        ImageIndication indication = findViewById(R.id.image_indication);
        if (isGroup) {
            mMembersView = (GroupMembersView) inflater.inflate(R.layout.page_group_members, null);
            mMembersView.setActivity(this);
            if (mGroup != null) {
                mMembersView.updateGroupInfo(mGroup);
            }
            mViewList.add(mMembersView);
            indication.setViewPager(mViewPager);
        } else {
            indication.setVisibility(View.GONE);
        }
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mViewList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View view = mViewList.get(position);
                container.addView(view);
                return view;
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 1 && !hadUpdateGroupInfo) {
                    Log.i(TAG, "onPageSelected() called with: i = " + i + "");
                    updateGroupInfoFromNet(mFriend.uuid);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConversationView.onPause();
        // 也需要清掉当前页面正在收到的信息
        clearUnreadCount(this, mFriend.uuid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConversationView.onDestroy();
        FriendNameUtil.clear();
        if (mGroupRunnable != null) {
            mHandler.removeCallbacks(mGroupRunnable);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult() called with: requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + data + "");
        if (requestCode == ConversationView.REQUEST_CODE_EMOJI
                || requestCode == ConversationView.REQUEST_CODE_IMAGE) {
            mConversationView.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == GroupMembersView.REQUEST_CODE_REMOVE
                || requestCode == GroupMembersView.REQUEST_CODE_ADD) {
            if (mMembersView != null) {
                mMembersView.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            Log.i(TAG, "onActivityResult() called with: requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + data + "");
        }
    }

    @Override
    public void onSuccess(GroupInfo info) {
        if (info != null) {
            updateMembersMap(info);
            mMembersView.updateGroupInfo(info);
        }
    }

    @Override
    public void onFailure(Exception exception) {
        Log.i(TAG, "onFailure: " + exception.toString());
    }

    private void clearUnreadCount(Context context, final String uuid) {
        Uri uri = Conversations.Conversation.CONVERSATION_URI;
        ContentValues values = new ContentValues();
        values.put(Conversations.Conversation.UNREAD, 0);
        String where = Conversations.Conversation.SEND_ID + "=?";
        String[] args = new String[]{uuid};
        ContentProviderOperation operation = ContentProviderOperation
                .newUpdate(uri)
                .withValue(Conversations.Conversation.UNREAD, 0)
                .withSelection(where, args)
                .build();
        int raws = context.getContentResolver().update(uri, values, where, args);
        Log.i(TAG, "clearUnreadCount: raws = " + raws);
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        operations.add(operation);
//        try {
//            ContentProviderResult[] results = context.getContentResolver().applyBatch(Conversations.AUTHORITY, operations);
//            Log.i(TAG, "clearUnreadCount: results = " + Arrays.toString(results));
//        } catch (OperationApplicationException e) {
//            e.printStackTrace();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

}
