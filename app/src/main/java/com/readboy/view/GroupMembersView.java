package com.readboy.view;

import android.app.Activity;
import android.app.readboy.IReadboyWearListener;
import android.app.readboy.PersonalInfo;
import android.app.readboy.ReadboyWearManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.readboy.adapter.BaseAdapter;
import com.readboy.adapter.BaseViewHolder;
import com.readboy.bean.GroupInfo;
import com.readboy.bean.GroupInfoManager;
import com.readboy.adapter.GroupMembersAdapter;
import com.readboy.bean.Constant;
import com.readboy.utils.JsonMapper;
import com.readboy.wetalk.bean.Friend;
import com.readboy.dialog.AddFriendDialog;
import com.readboy.dialog.CommonDialog;
import com.readboy.activity.FriendSelectorActivity;
import com.readboy.utils.ToastUtils;
import com.readboy.wetalk.utils.WTContactUtils;
import com.readboy.utils.WearManagerProxy;
import com.readboy.wetalk.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author oubin
 * @date 2018/12/22
 */
public class GroupMembersView extends FrameLayout implements BaseAdapter.OnItemClickListener,
        GroupInfoManager.CallBack {
    private static final String TAG = "hwj_GroupMembersView";
    public static final int REQUEST_CODE_REMOVE = 11;
    public static final int REQUEST_CODE_ADD = 12;

    private RecyclerView mGroupRv;
    private View mProgressBar;
    private TextView mMessageTv;
    private GroupMembersAdapter mAdapter;
    private GroupInfo mGroupInfo;
    private Friend mGroup;
    private final List<Friend> mMemberList = new ArrayList<>();
    private Context mContext;
    private Activity mActivity;
    private Handler mHandler = new Handler();

    public GroupMembersView(Context context) {
        this(context, null);
    }

    public GroupMembersView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupMembersView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

    }

    private void initView() {
        mProgressBar = findViewById(R.id.progress_bar);
        mMessageTv = (TextView) findViewById(R.id.message);
        mGroupRv = (RecyclerView) findViewById(R.id.group_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int i) {
                return mAdapter.isFooterPosition(i) ? layoutManager.getSpanCount() : 1;
            }
        });
        mGroupRv.setLayoutManager(layoutManager);
        mAdapter = new GroupMembersAdapter(getContext(), false);
        mGroupRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    public void setData(Intent intent) {
        Friend friend = mGroup = intent.getParcelableExtra(Constant.EXTRA_FRIEND);
        mAdapter.setOwnerState(isOwner());
        List<Friend> friends = friend.members;
        if (friends == null || friends.size() <= 0) {
            Log.w(TAG, "setData: members is null or size is o.");
//            GroupInfoManager.getGroupInfo(mContext, friend.uuid, this);
            return;
        }
        for (Friend f : friends) {
            if (WTContactUtils.isContacts(mContext, f.uuid)) {
                f.addVisibility = View.GONE;
            } else {
                f.addVisibility = View.VISIBLE;
            }
        }
        setData(friends);
        mProgressBar.setVisibility(GONE);
    }

    public void setData(List<Friend> data) {
        mMemberList.clear();
        if (data != null) {
            Log.i(TAG, "setData: data size = " + data.size());
            mMemberList.addAll(data);
        }
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
    }

    public void setActivity(Activity activity) {
        Log.i(TAG, "setActivity: ");
        this.mActivity = activity;
        if (mAdapter != null) {
            setData(mActivity.getIntent());
        }
    }

    private Activity getActivity() {
        return mActivity;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "onFinishInflate: ");
        initView();
        if (getActivity() != null) {
            Log.i(TAG, "onFinishInflate: activity = null.");
            setData(getActivity().getIntent());
        }
    }

    @Override
    public void onItemClick(int position, BaseViewHolder viewHolder) {
        Log.i(TAG, "onItemClick: ");
        if (mAdapter.isFooterPosition(position)) {
            leaveAction();
        } else if (mAdapter.isAddActionPosition(position)) {
            addAction();
        } else if (mAdapter.isRemoveActionPosition(position)) {
            removeAction();
        } else {
            Friend friend = mMemberList.get(position);
            if (friend.addVisibility == View.GONE) {
                return;
            }
            addFriend(friend);
        }
    }

    private void addFriend(Friend member) {
        Log.i(TAG, "addFriend: ");
        AddFriendDialog dialog = new AddFriendDialog(mContext);
        dialog.show();
        dialog.setFriend(member);
    }

    private void removeAction() {
        Intent intent = new Intent(getActivity(), FriendSelectorActivity.class);
        intent.putExtra(FriendSelectorActivity.EXTRA_GROUP, mGroup);
        intent.putExtra(FriendSelectorActivity.EXTRA_TYPE_ORDINAL, FriendSelectorActivity.Type.REMOTE.ordinal());
        ArrayList<Friend> data = new ArrayList<>(mMemberList);
        intent.putParcelableArrayListExtra(FriendSelectorActivity.EXTRA_FRIENDS, data);
        getActivity().startActivityForResult(intent, REQUEST_CODE_REMOVE);
    }

    private void addAction() {
        String selection = "raw_contact_id in (SELECT raw_contact_id FROM data WHERE data8 LIKE \"D%\")";
        // 查询所有设备类型联系人
        Log.i(TAG, "addAction: start");
        List<Friend> friends = WTContactUtils.getFriendFromContacts(mContext, selection, null);
        Log.i(TAG, "addAction: end.");
        if (friends == null) {
            ToastUtils.show(mContext, "无可用添加的好友");
            return;
        }
        ArrayList<Friend> data = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (Friend member : mMemberList) {
            set.add(member.uuid);
        }
        for (Friend friend : friends) {
            if (!set.contains(friend.uuid)) {
                data.add(friend);
            }
        }
        if (data.size() <= 0) {
            ToastUtils.show(mContext, "无可添加的好友");
            return;
        }
        Intent intent = new Intent(getActivity(), FriendSelectorActivity.class);
        intent.putExtra(FriendSelectorActivity.EXTRA_GROUP, mGroup);
        intent.putExtra(FriendSelectorActivity.EXTRA_TYPE_ORDINAL, FriendSelectorActivity.Type.ADD.ordinal());
        intent.putExtra(FriendSelectorActivity.EXTRA_FRIENDS, data);
        getActivity().startActivityForResult(intent, REQUEST_CODE_ADD);
    }

    private void leaveAction() {
        String content;
        if (isOwner()) {
            content = getResources().getString(R.string.warning_disband_group);
        } else {
            content = getResources().getString(R.string.warning_leave_group);
        }
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.content(content)
                .leftButtonListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveGroup();
                    }
                });
        CommonDialog dialog = builder.build(mContext);
        dialog.show();

    }

    private void leaveGroup() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", mGroup.uuid);
            String data = jsonObject.toString();
            WearManagerProxy.groupAction(mContext, WearManagerProxy.Command.LEAVE_GROUP, data, new IReadboyWearListener.Stub() {
                @Override
                public void pushSuc(String cmd, String serial, int code, String data, String result) {
                    Log.i(TAG, "pushSuc() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", data = " + data + ", result = " + result + "");
                    if (code == 0) {
                        //这过程中可能已经收到notify_contact，正在更新联系人了。
//                        WTContactUtils.deleteContactsByUuid(mContext, mGroupInfo.getUuid());
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (code == 0) {
                                if (isOwner()) {
                                    ToastUtils.show(mContext, "成功解散群");
                                } else {
                                    ToastUtils.show(mContext, "成功退群");
                                }
                                getActivity().finish();
                            } else {
                                ToastUtils.show(mContext, "操作失败.");
                            }
                        }
                    });
                }

                @Override
                public void pushFail(String cmd, String serial, int code, String errorMsg) {
                    Log.i(TAG, "pushFail() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", errorMsg = " + errorMsg + "");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.show(mContext, "失败：" + errorMsg);
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            ToastUtils.show(mContext, "数据解析失败: " + e.toString());
        }
    }

    private boolean isOwner() {
        PersonalInfo info = WearManagerProxy.getManager(mContext).getPersonalInfo();
        if (info == null || mGroupInfo == null || TextUtils.isEmpty(mGroupInfo.getOwner())) {
            Log.w(TAG, "isOwner: info = " + info + ", owner = " + mGroupInfo);
            return false;
        }
        return mGroupInfo.getOwner().equals(info.getUuid());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult() called with: requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + data + "");
            return;
        }
        String response = data.getStringExtra(FriendSelectorActivity.EXTRA_DATA);
        try {
            JSONObject object = new JSONObject(response);
            String id = object.optString("id");
            if (id != null && id.equals(mGroup.uuid)) {
                handleActivityResult(data);
//                if (requestCode == REQUEST_CODE_REMOVE) {
//                    handleRemoveResult(data);
//                } else {
//                    handleAddResult(data);
//                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleActivityResult(Intent intent) {
        String data = intent.getStringExtra(FriendSelectorActivity.EXTRA_DATA);
        if (!TextUtils.isEmpty(data)) {
            GroupInfo info = JsonMapper.fromJson(data, GroupInfo.class);
            if (info != null && info.getFriends() != null) {
                updateGroupInfo(info);
            }
        }
    }

    private void handleAddResult(Intent intent) {
        String data = intent.getStringExtra(FriendSelectorActivity.EXTRA_DATA);
    }

    private void handleRemoveResult(Intent intent) {

    }

    @Override
    public void onSuccess(GroupInfo info) {
        Log.i(TAG, "onSuccess: thread = " + Thread.currentThread().getName());
        updateGroupInfo(info);
    }

    @Override
    public void onFailure(Exception exception) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(GONE);
                mMessageTv.setVisibility(VISIBLE);
            }
        });
    }

    private void updateMembersState() {
        for (Friend member : mMemberList) {
            if (WTContactUtils.isContacts(mContext, member.uuid)) {
                member.addVisibility = View.VISIBLE;
            } else {
                member.addVisibility = View.VISIBLE;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void updateGroupInfo(GroupInfo info) {
        Log.i(TAG, "updateGroupInfo: ");
        String myUuid = WearManagerProxy.getMyUuid(mContext);
        this.mGroupInfo = info;
        if (mGroupInfo.getFriends() != null) {
            for (Friend member : mGroupInfo.getFriends()) {
                member.updateAddVisibility(mContext, myUuid);
            }
        }
        updateView();
    }

    private void updateView() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setData(mGroupInfo.getFriends());
                mProgressBar.setVisibility(GONE);
                if (mMemberList.size() > 0) {
                    mMessageTv.setVisibility(GONE);
                    mGroupRv.setVisibility(VISIBLE);
                } else {
                    Log.i(TAG, "run: size = 0.");
                    mMessageTv.setVisibility(VISIBLE);
                    mGroupRv.setVisibility(GONE);
                }
            }
        });
    }

}
