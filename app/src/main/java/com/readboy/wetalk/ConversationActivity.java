package com.readboy.wetalk;

import android.content.Intent;
import android.os.Bundle;
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
import com.readboy.wetalk.bean.Friend;
import com.readboy.view.ConversationView;
import com.readboy.view.GroupMembersView;
import com.readboy.widget.ImageIndication;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oubin
 * @date 2018/12/28
 */
public class ConversationActivity extends BaseActivity implements GroupInfoManager.CallBack {

    private ViewPager mViewPager;
    private List<View> mViewList = new ArrayList<>();
    private boolean isGroup = false;
    private GroupMembersView mMembersView;
    private ConversationView mConversationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        Friend friend = intent.getParcelableExtra(Constant.EXTRA_FRIEND);
        Log.i(TAG, "onCreate: friend = " + friend.toString());
        if (friend.members != null && friend.members.size() > 0 || friend.relation == 200) {
            isGroup = true;
        }
        Log.i(TAG, "onCreate: isFriendGroup = " + isGroup);

        initView();
        initData();
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
        mConversationView = (ConversationView) inflater.inflate(R.layout.activity_square_conversation, null);
        mConversationView.setActivity(this);
        mViewList.add(mConversationView);
        mViewPager = findViewById(R.id.conversation_view_pager);
        ImageIndication indication = findViewById(R.id.image_indication);
        if (isGroup) {
            mMembersView = (GroupMembersView) inflater.inflate(R.layout.page_group_members, null);
            mMembersView.setActivity(this);
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
        Log.i(TAG, "onSuccess: ");
    }

    @Override
    public void onFailure(Exception exception) {
        Log.i(TAG, "onFailure: ");
    }
}
