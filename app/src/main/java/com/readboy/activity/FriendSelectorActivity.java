package com.readboy.activity;

import android.app.Activity;
import android.app.readboy.IReadboyWearListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.readboy.adapter.BaseCheckAdapter;
import com.readboy.adapter.FriendSelectorAdapter;
import com.readboy.bean.Constant;
import com.readboy.bean.FriendGroup;
import com.readboy.wetalk.bean.Friend;
import com.readboy.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.readboy.utils.ToastUtils;
import com.readboy.utils.WearManagerProxy;
import com.readboy.utils.WearManagerProxy.Command;
import com.readboy.wetalk.ConversationActivity;
import com.readboy.wetalk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oubin
 * @date 2018/12/27
 */
public class FriendSelectorActivity extends Activity implements View.OnClickListener,
        BaseCheckAdapter.OnCheckedChangeListener {
    private static final String TAG = "hwj_FriendSelector";
    public static final String EXTRA_TYPE_ORDINAL = "type";
    public static final String EXTRA_FRIENDS = "friends";
    public static final String EXTRA_GROUP = "group";
    public static final String EXTRA_DATA = "data";

    private RecyclerView mRecyclerView;
    private FriendSelectorAdapter mAdapter;
    private Friend mGroup;
    private final List<Friend> mFriends = new ArrayList<>();
    private Button mBtnConfirm;
    private View mProgressBarParent;
    private ProgressBar mProgressBar;
    private Context mContext;
    private Type mType = Type.CREATE;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_friend_selector);
        initData();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastUtils.cancel();
    }

    public void initView() {
        mRecyclerView = findViewById(R.id.friend_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new FriendSelectorAdapter(mContext);
        mAdapter.setData(mFriends);
        mAdapter.setOnCheckedChangeListener(this);
        HeaderAndFooterWrapper footerWrapper = new HeaderAndFooterWrapper(mAdapter);
        footerWrapper.addFootView(LayoutInflater.from(this).inflate(R.layout.footer_friend_selector, null));
        mRecyclerView.setAdapter(footerWrapper);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnConfirm.setBackgroundResource(mType.resId);
        mBtnConfirm.setOnClickListener(this);
        mProgressBarParent = findViewById(R.id.progress_bar_parent);
        mProgressBarParent.setOnClickListener(v -> {
        });
        mProgressBar = findViewById(R.id.progress_bar);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mFriends.clear();
            List<Friend> members = intent.getParcelableArrayListExtra(EXTRA_FRIENDS);
            mFriends.addAll(members);
            mGroup = intent.getParcelableExtra(EXTRA_GROUP);
            int ordinal = intent.getIntExtra(EXTRA_TYPE_ORDINAL, 0);
            mType = Type.valueOf(ordinal);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setData(List<Friend> data) {
        mFriends.clear();
        if (data != null) {
            mFriends.addAll(data);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void getContacts() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                if (!mBtnConfirm.isActivated()) {
                    ToastUtils.show(mContext, mType.warning);
                } else {
                    showProgressBar();
                    groupAction();
                }
                break;
            default:
                break;
        }
    }

    private void groupAction() {
//        String data = mType == Type.CREATE ? getCreateGroupRequest() : getActionRequest();
        Log.i(TAG, "hwj: groupAction: ");
        WearManagerProxy.groupAction(mContext, mType.command, getActionRequest(), new IReadboyWearListener.Stub() {
            @Override
            public void pushSuc(String cmd, String serial, int code, String data, String result) {
                Log.i(TAG, mType.command + " pushSuc() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", data = " + data + ", result = " + result + "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mType == Type.CREATE) {
                            FriendGroup friendGroup = FriendGroup.parseData(data);
                            if (friendGroup != null && friendGroup.friend != null) {
                                gotoConversation(friendGroup.friend);
                            } else {
                                Log.w(TAG, "run: parse data fail.");
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_DATA, data);
                            setResult(RESULT_OK, intent);
                        }
                        finish();
                    }
                });
            }

            @Override
            public void pushFail(String cmd, String serial, int code, String errorMsg) {
                Log.e(TAG, "pushFail() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", errorMsg = " + errorMsg + "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(mContext, errorMsg);
                        hideProgressBar();
                    }
                });
            }
        });

    }

    @NonNull
    private String getActionRequest() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (mGroup != null) {
                jsonObject.put("id", mGroup.uuid);
            }
            JSONArray jsonArray = new JSONArray();
            for (Integer i : mAdapter.getSelectedPosition()) {
                jsonArray.put(mFriends.get(i).uuid);
            }
            jsonObject.put("members", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String getCreateGroupRequest() {
        JSONArray jsonArray = new JSONArray();
        for (Integer i : mAdapter.getSelectedPosition()) {
            jsonArray.put(mFriends.get(i).uuid);
        }
        return jsonArray.toString();
    }

    private int getSelectedCount() {
        return mAdapter.getSelectedPosition().size();
    }

    private void showProgressBar() {
        mProgressBarParent.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBarParent.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void gotoConversation(Friend friend) {
        Intent intent = new Intent(mContext, ConversationActivity.class);
        intent.putExtra(Constant.EXTRA_FRIEND, friend);
    }

    @Override
    public void onChecked(int position, boolean checked) {
        mBtnConfirm.setActivated(getSelectedCount() >= mType.limit);
    }

    public enum Type {
        /**
         * button图片resId；
         * 少于多少条就设置为activated；警告语resId
         */
        CREATE(R.drawable.btn_create_group_selector, 2, R.string.warning_group_create, Command.CREATE_GROUP),
        ADD(R.drawable.btn_add_group_selector, 1, R.string.warning_group_add, Command.ADD_GROUP),
        REMOTE(R.drawable.btn_remove_group_selector, 1, R.string.warning_group_remove, Command.REMOVE_GROUP);

        int resId;
        int limit;
        int warning;
        Command command;

        /**
         * @param resId   button图片resId；
         * @param limit   少于多少条就设置为activated
         * @param warning 警告语resId
         * @param command 服务器请求
         */
        Type(int resId, int limit, int warning, Command command) {
            this.resId = resId;
            this.limit = limit;
            this.warning = warning;
            this.command = command;
        }

        public static Type valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }

    }
}
