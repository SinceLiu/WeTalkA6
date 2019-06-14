package com.readboy.wetalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oubin
 * @date 2018/12/28
 */
public class ConversationActivity extends BaseRequestPermissionActivity implements GroupInfoManager.CallBack,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "hwj-ConversationAct";

    private static final int LOADER_ID_CONTACTS = 23;
    private static final int DELAY_LOAD_TIME = 1000;
    private static final int WHAT_UPDATE_GROUP = 10;

    private ViewPager mViewPager;
    private List<View> mViewList = new ArrayList<>();
    private boolean isGroup = false;
    private GroupInfo mGroup;
    private Friend mFriend;
    private GroupMembersView mMembersView;
    private ConversationView mConversationView;
    private ImageIndication mIndication;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_UPDATE_GROUP:
                    updateGroupInfoFromNet(mFriend.uuid);
                    break;
                default:
                    break;
            }
        }
    };
    private Runnable mGroupRunnable;
    private boolean hadUpdateGroupInfo;

    @Override
    protected void initContent() {
        if (mConversationView != null) {
            mConversationView.recheckContactsObserver();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        if (intent != null) {
            parseIntent(intent);
        }
        Log.i(TAG, "onCreate: isFriendGroup = " + isGroup);

        initView();
        initData();
//        clearUnreadCount(this, mFriend.uuid);
        initLoader();
    }

    private void parseIntent(Intent intent) {
        Friend friend = mFriend = intent.getParcelableExtra(Constant.EXTRA_FRIEND);
        if (friend == null) {
            return;
        }
        Log.i(TAG, "onCreate: friend = " + friend.toString());
        final String uuid = friend.uuid;
        if (friend.isFriendGroup()) {
            isGroup = true;
            GroupInfo info = GroupInfoManager.getDataFormDatabase(this, uuid);
            // TODO, 不在数据库里，就马上网络获取，防止聊天列表没有名字。
            if (info != null && info.getFriends() != null) {
                Log.i(TAG, "onCreate: info = " + info.toString());
                updateMembersMap(info);
            } else {
                mHandler.sendEmptyMessageDelayed(WHAT_UPDATE_GROUP, 1000);
                Log.w(TAG, "onCreate: info = " + info);
            }
        } else {
            isGroup = false;
        }
    }

    private void initLoader() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "initLoader: no read contacts permission.");
            return;
        }
        getLoaderManager().initLoader(LOADER_ID_CONTACTS, null, this);
    }

    private void updateGroupInfoFromNet(String uuid) {
        hadUpdateGroupInfo = true;
        GroupInfoManager.getGroupInfoFromNet(ConversationActivity.this,
                uuid, ConversationActivity.this);
    }

    private void updateMembersMap(GroupInfo info) {
        Log.i(TAG, "updateMembersMap: ");
        mGroup = info;
        Map<String, Friend> map = new HashMap<>(info.getMembers().size());
        for (Friend f : info.getFriends()) {
            map.put(f.uuid, f);
        }
        FriendNameUtil.updateMembersMap(map);
        if (mConversationView != null) {
            // TODO, 取舍，是静态呢方便共享数据，还是传递过去，确定内存的准确以及释放
//            mConversationView.updateMembers(map);
            mConversationView.notifyDataSetChanged();
        }
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
        mViewList.clear();
        mViewList.add(mConversationView);
        mViewPager = findViewById(R.id.conversation_view_pager);
        mIndication = findViewById(R.id.image_indication);
        if (isGroup) {
            initMembersView();
            mIndication.setViewPager(mViewPager);
        } else {
            mIndication.setVisibility(View.GONE);
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
            @Override
            public void destroyItem(ViewGroup container, int position, Object object){
//                super.destroyItem(container,position,object);
                container.removeView((View)object);
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

    private void initMembersView() {
        mMembersView = (GroupMembersView) getLayoutInflater().inflate(R.layout.page_group_members, null);
        mMembersView.setActivity(this);
        if (mGroup != null) {
            mMembersView.updateGroupInfo(mGroup);
        }
        mViewList.add(mMembersView);
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
        //清除通知
        NotificationUtils.cancelMessageNotification(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 启动模式为：singleTask的需要处理该回调
        setIntent(intent);
        String temp = mFriend.uuid;
        parseIntent(intent);
        if (temp.equals(mFriend.uuid)) {
            Log.i(TAG, "onNewIntent: is same uuid, do nothing.");
            return;
        }
       initView();
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
//        ContentProviderOperation operation = ContentProviderOperation
//                .newUpdate(uri)
//                .withValue(Conversations.Conversation.UNREAD, 0)
//                .withSelection(where, args)
//                .build();
        int rows = context.getContentResolver().update(uri, values, where, args);
        Log.i(TAG, "clearUnreadCount: raws = " + rows);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.Data.DATA1,
                ContactsContract.Data.DATA2,
                ContactsContract.Data.DATA8
        };
        return new CursorLoader(this, ContactsContract.Data.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        SparseArray<Friend> array = new SparseArray<>();
        if (cursor == null || !cursor.moveToFirst() || cursor.getCount() <= 0) {
            return;
        }
        while (cursor.moveToNext()) {
            int rawId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
            Friend friend = array.get(rawId);
            if (friend == null) {
                friend = new Friend();
                friend.contactId = rawId;
                array.put(rawId, friend);
            }
            String mineType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
            switch (mineType) {
                case StructuredName.CONTENT_ITEM_TYPE:
                    friend.name = cursor.getString(cursor.getColumnIndex(StructuredName.DISPLAY_NAME));
                    break;
                case ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE:
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE))
                            == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK) {
                        friend.uuid = cursor.getString(cursor.getColumnIndex("data8"));
                    }
                default:
                    break;
            }
        }
        //key: uuid
        Map<String, Friend> friends = new HashMap<>(array.size());
        for (int size = array.size() - 1; size >= 0; size--) {
            Friend friend = array.valueAt(size);
            friends.put(friend.uuid, friend);
        }

        if (mConversationView != null) {
            mConversationView.onChange(friends);
        }
        if (mMembersView != null) {
            mMembersView.onChange(friends);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG, "onLoaderReset: ");
    }
}
