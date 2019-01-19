package com.readboy.wetalk;

import android.content.Intent;
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
import com.readboy.utils.FriendNameUtil;
import com.readboy.wetalk.bean.Friend;
import com.readboy.view.ConversationView;
import com.readboy.view.GroupMembersView;
import com.readboy.widget.ImageIndication;

import java.util.ArrayList;
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
    private GroupMembersView mMembersView;
    private ConversationView mConversationView;
    private Handler mHandler = new Handler();
    private Runnable mGroupRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        Friend friend = intent.getParcelableExtra(Constant.EXTRA_FRIEND);
        Log.i(TAG, "onCreate: friend = " + friend.toString());
        final String uuid = friend.uuid;
        if (friend.isFriendGroup()) {
            isGroup = true;
            Log.i(TAG, "onCreate:  1  >> ");
            GroupInfo info = GroupInfoManager.getDataFormDatabase(this, uuid);
            Log.i(TAG, "onCreate:  2  >> ");
            if (info != null && info.getFriends() != null) {
                updateMembersMap(info);
            }
            Log.i(TAG, "onCreate:  3  >> ");
        }
        Log.i(TAG, "onCreate: isFriendGroup = " + isGroup);

        initView();
        initData();
        if (isGroup) {
            mGroupRunnable = new Runnable() {
                @Override
                public void run() {
                    GroupInfoManager.getGroupInfoFromNet(ConversationActivity.this,
                            uuid, ConversationActivity.this);
                }
            };
            mHandler.post(mGroupRunnable);
        }
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

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConversationView.onPause();
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
        Log.i(TAG, "onSuccess: " + Thread.currentThread().getName());
        if (info != null) {
            updateMembersMap(info);
            mMembersView.updateGroupInfo(info);
        }
    }

    @Override
    public void onFailure(Exception exception) {
        Log.i(TAG, "onFailure: " + exception.toString());
    }


}
