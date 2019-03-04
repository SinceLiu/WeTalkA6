package com.readboy.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readboy.adapter.ConversationListAdapterSimple;
import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.bean.VideoInfo;
import com.readboy.http.HttpClient;
import com.readboy.utils.MediaUtils;
import com.readboy.wetalk.bean.Friend;
import com.readboy.wetalk.bean.Model;
import com.readboy.provider.ConversationProvider;
import com.readboy.provider.Conversations;
import com.readboy.provider.Profile;
import com.readboy.record.AudioRecorder;
import com.readboy.record.RecordStrategy;
import com.readboy.utils.EmojiUtils;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MPrefs;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.NetWorkUtils.PushResultListener;
import com.readboy.utils.ToastUtils;
import com.readboy.wetalk.EmojiActivity;
import com.readboy.wetalk.GetImageActivity;
import com.readboy.wetalk.R;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author hwj
 * @author oubin
 */
public class ConversationView extends RelativeLayout implements OnClickListener,
        OnLongClickListener, OnTouchListener {
    private static final String TAG = "hwj_ConversationView";
    public static final int REQUEST_CODE_IMAGE = 21;
    public static final int REQUEST_CODE_EMOJI = 22;

    public static final int FILE_TYPE_IMAGE = 1;
    public static final int FILE_TYPE_VIDEO = 2;

    private static final int MAX_COUNT = Constant.MAX_CONVERSATION_SIZE;

    private View mParent;
    private ListView mConversationList;
    private View mSendEmojiBtn;
    private View mSendImageBtn;
    private TextView mConversationName;
    private View mHeaderView;
    private final List<Conversation> mConversations = new ArrayList<>();
    @Deprecated
    private Map<String, Friend> mMemberMap = new HashMap<>();
    private ConversationListAdapterSimple mAdapter;
    private Friend mCurrentFriend = null;
    private ContentResolver mResolver;
    private NetWorkUtils mNetWorkUtils;
    private boolean isOnPause = false;
    private Button mSendVoiceBtn;
    private ImageView mRecordStateImg;
    private View mLoading;

    /**
     * 录音相关
     */
    private static final int MIN_RECORD_TIME = 1;
    private int maxRecordTime = Model.DEFAULT_MAX_RECORD_TIME * 1000;
    private String maxRecordTimeSecond = String.valueOf(Model.DEFAULT_MAX_RECORD_TIME);
    private static final int MESSAGE_STOP_RECORD = 1;
    private int mRecordTime;
    private long startRecordTime;
    private Dialog mRecordDialog;
    private RecordStrategy mRecorder;
    private Context mContext;
    private Activity mActivity;

    //    private ThreadFactory mThreadFactory = new ThreadFactory() {
//        @Override
//        public Thread newThread(@NonNull Runnable r) {
//            return new Thread("RecordTimerThread");
//        }
//    };
//    private ExecutorService mTimerExecutor = new ThreadPoolExecutor(5, 200, 0L, TimeUnit.SECONDS,
//            new LinkedBlockingDeque<Runnable>(1024), mThreadFactory);
    private boolean isRecording = false;
    private String mCurrentImagePath = "";
    private String mCurrentVideoPath = "";

    private float density;

    /**
     * TODO，修改为LoaderManager加载更加合理？
     */
    private ContentObserver mContactsObserver;
    /**
     * TODO，修改为LoaderManager加载更加合理？
     * 监听消息ContentProvider数据变化,只有收到消息时候才会回调
     */
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            //获取最新的消息集合
            // TODO，对方正在插入数据，可能会获取到多条新消息
            List<Conversation> conversations = ConversationProvider.getConversationList(mContext, mCurrentFriend.uuid);
            if (conversations == null || conversations.size() <= mConversations.size()) {
                Log.e(TAG, "onChange: conversation = null.");
                CrashReport.postCatchedException(new UnknownError("新获取的conversations不大于现有的conversations, " +
                        "new size = " + (conversations != null ? conversations.size() : 0) + ", old size = " + mConversations.size()));
                return;
            }
            int length = conversations.size();
            int current = mConversations.size();
            boolean udpateGroupMember = false;
            final Conversation last = mConversations.size() == 0
                    ? null
                    : mConversations.get(mConversations.size() - 1);
            Log.i(TAG, "onChange: new length = " + length + ", current = " + current);
            for (int i = length - 1; i >= current; i--) {
                //按时间降序,获取最新的一条消息
                Conversation conversation = conversations.get(i);
//                if (last != null && last.conversationId.equals(conversation.conversationId)) {
//                    Log.i(TAG, "onChange: last = " + last.conversationId);
//                    break;
//                }
                if (!conversation.sendId.equals(MPrefs.getDeviceId(mContext))) {
                    //添加到显示的消息列表集合
                    Log.i(TAG, "onChange: i = " + i + ", conversation, " + i);
                    mConversations.add(conversation);
                    LogInfo.e(TAG, "onChange ---------- add conversation notifyDataSetChanged");
                }
            }
            Log.e(TAG, "onChange: current conversations size = " + mConversations.size());
            notifyAndScrollBottom();
        }
    };

    /**
     * 异步获取通讯录
     */
    private class GetConversationTask extends AsyncTask<Void, Void, List<Conversation>> {

        @Override
        protected List<Conversation> doInBackground(Void... params) {
            Log.i(TAG, "doInBackground: uuid = " + mCurrentFriend.uuid);
            return ConversationProvider.getConversationList(mContext, mCurrentFriend.uuid);
        }

        @Override
        protected void onPostExecute(List<Conversation> result) {
            List<Conversation> temp = new ArrayList<>();
            if (mConversations.size() > 0) {
                temp.addAll(mConversations);
            }
            mConversations.clear();
            mConversations.addAll(result);
            if (temp.size() > 0) {
                mConversations.addAll(temp);
            }
            Log.i(TAG, "onPostExecute: conversations size = " + mConversations.size());
            limitConversationSize();
            notifyAndScrollBottom();
            checkIsShareImage();
        }

    }

    public ConversationView(Context context) {
        this(context, null);
    }

    public ConversationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mParent = this;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "onFinishInflate: ");
        initView();
        initData();
        initFriendData(getActivity().getIntent());
        GetConversationTask mConversationTask = new GetConversationTask();
        mConversationTask.execute();
    }

    private void initView() {
        Log.e(TAG, "initView: ");
        mConversationList = (ListView) findViewById(R.id.conversation_list);
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.conversation_header, mConversationList, false);
        View mFooterView = LayoutInflater.from(mContext).inflate(R.layout.conversation_footer, mConversationList, false);
        mConversationList.addHeaderView(mHeaderView);
        mConversationList.addFooterView(mFooterView);
        mAdapter = new ConversationListAdapterSimple(mContext, mConversations);
        mAdapter.setSendMessageHandler(mSendMessageResultHandler);
        mConversationList.setAdapter(mAdapter);

        mSendEmojiBtn = findViewById(R.id.send_emoji_btn);
        mSendVoiceBtn = findViewById(R.id.send_voice_btn);
        mSendImageBtn = findViewById(R.id.send_image_btn);
        View mNoDataTip = findViewById(R.id.no_msg_tip);
        mConversationList.setEmptyView(mNoDataTip);
        mConversationName = findViewById(R.id.conversation_name_tip);
        mLoading = findViewById(R.id.loading);
    }

    private void initData() {
        Log.e(TAG, "initData: ");
        mResolver = mContext.getContentResolver();
        //初始化录音对象
        mRecorder = new AudioRecorder(mContext);
        mNetWorkUtils = NetWorkUtils.getInstance(mContext);
        mSendEmojiBtn.setOnClickListener(this);
        mSendImageBtn.setOnClickListener(this);
        mSendVoiceBtn.setOnLongClickListener(this);
        mSendVoiceBtn.setOnTouchListener(this);
        registerContentObserver();
    }

    private void registerContentObserver() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            mContactsObserver = new ContactsObserver(new Handler());
            mResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, false, mContactsObserver);
        }
        mResolver.registerContentObserver(Conversations.Conversation.CONVERSATION_URI, true, mObserver);
    }

    private void unregisterContentObserver() {
        mResolver.unregisterContentObserver(mObserver);
        mResolver.unregisterContentObserver(mContactsObserver);
    }

    /**
     * 限制消息占用内存
     */
    private void limitConversationSize() {
        if (mConversations.size() == 0) {
            return;
        }
        int count = mConversations.size();
        if (count > MAX_COUNT) {
            for (int i = 0; i < count - MAX_COUNT; i++) {
                deleteConversation(mConversations.get(0));
            }
        }
    }

    /**
     * 删除消息
     *
     * @param conversation 消息
     */
    private void deleteConversation(Conversation conversation) {
        boolean result = false;
        switch (conversation.type) {
            //不带文件的,直接删除数据库记录
            case Constant.REC_EMOJI:
            case Constant.SEND_EMOJI:
            case Constant.REC_TEXT:
            case Constant.REC_IMAGE:
                result = true;
                break;
            //带有文件的,先删除文件
            case Constant.SEND_IMAGE:
                result = deleteConversationFile(conversation.imageLocalPath);
                break;
            case Constant.REC_VOICE:
            case Constant.SEND_VOICE:
                result = deleteConversationFile(conversation.voiceLocalPath);
                break;
            default:
                break;
        }
        if (result) {
            mConversations.remove(conversation);
            mResolver.delete(Conversations.Conversation.CONVERSATION_URI,
                    Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId});
        }
    }

    /**
     * 删除消息的文件
     *
     * @param path 路径
     * @return 删除结果
     */
    private boolean deleteConversationFile(String path) {
        boolean result = false;
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                result = file.delete();
            }
        }
        return result;
    }

    /**
     * 初始化好友数据
     */
    private void initFriendData(Intent intent) {
        //获取传递参数(用户Id)
        Intent fromIntent = intent;
        if (fromIntent != null) {
            Friend friend = fromIntent.getParcelableExtra(Constant.EXTRA_FRIEND);
            if (friend != null) {
                mCurrentFriend = friend;
            } else {
                //普通的微聊界面
                mCurrentFriend = new Friend();
                mCurrentFriend.uuid = fromIntent.getStringExtra(Constant.EXTRA_FRIEND_ID);
                mCurrentFriend.name = fromIntent.getStringExtra(Constant.EXTRA_FRIEND_NAME);
                mCurrentFriend.unreadCount = fromIntent.getIntExtra(Constant.FRIEND_UNREAD_COUNT, 0);
            }

            mConversationName.setText(mCurrentFriend.name);
            Log.e(TAG, "initFriendData: uuid = " + mCurrentFriend.uuid);
            //uuid以D开头代表是手表，其他不用获取设备型号
            if (mCurrentFriend.uuid != null && mCurrentFriend.uuid.trim().startsWith(Model.UUID_BEGINNING_CHARACTER)) {
                mLoading.setVisibility(View.VISIBLE);
                initModel();
            } else {
//                mLoading.setVisibility(View.GONE);
            }
        } else {
            showMsg(getResources().getString(R.string.wrong_data));
        }
    }

    private void initModel() {
        Profile.getProfile(mContext, mCurrentFriend.uuid, new Profile.CallBack() {
            @Override
            public void onResponse(final Profile profile) {
                Log.e(TAG, "onSuccess() called with: profile = " + profile + "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(View.GONE);
                        mCurrentFriend.model = Model.getModel(profile.getImei());
                        adjustViewCaseModel(mCurrentFriend.model);
                    }
                });
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail() called with: e = " + e + "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void adjustViewCaseModel(Model model) {
        Log.e(TAG, "adjustViewCaseModel: model = " + model);
        if (model == null) {
            mSendVoiceBtn.setBackgroundResource(R.drawable.btn_send_voice_short_selector);
            mSendImageBtn.setVisibility(View.VISIBLE);
            mSendImageBtn.setVisibility(View.VISIBLE);
            return;
        }
        int maxTime = Model.getMaxRecordTime(model);
        maxRecordTime = maxTime * 1000;
        maxRecordTimeSecond = String.valueOf(maxTime);
        switch (model) {
            case W2S:
            case W2T:
            case W3T:
                mSendVoiceBtn.setBackgroundResource(R.drawable.btn_send_voice_long_selector);
                mSendEmojiBtn.setVisibility(View.GONE);
                mSendImageBtn.setVisibility(View.GONE);
                break;
            case W5:
                mSendVoiceBtn.setBackgroundResource(R.drawable.btn_send_voice_middle_selector);
                mSendEmojiBtn.setVisibility(View.VISIBLE);
                mSendImageBtn.setVisibility(View.GONE);
                break;
            default:
                mSendVoiceBtn.setBackgroundResource(R.drawable.btn_send_voice_short_selector);
                mSendImageBtn.setVisibility(View.VISIBLE);
                mSendImageBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showSmallSystemView() {

    }

    private void showAndroidSystemView() {

    }

    public void updateMembers(Map<String, Friend> map) {
        mMemberMap.clear();
        mMemberMap.putAll(map);
        post(() -> {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void notifyDataSetChanged() {
        post(() -> {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override

    public void onClick(final View v) {
        int id = v.getId();
        v.setEnabled(false);
        mSendMessageResultHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                v.setEnabled(true);
            }
        }, 1000);
        switch (id) {
            case R.id.send_emoji_btn:
                getEmojiId();
                break;
            case R.id.send_image_btn:
//                getImage();
                getImageOrVideo();
//                uploadVideo();
//                uploadTest();
                break;
            default:
                break;
        }
    }

    private void uploadTest(String path) {
        HttpClient.uploadImage(path, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure() called with: call = " + call + ", e = " + e + "");
            }

            @Override
            public void onResponse(Call call, @NonNull Response response) throws IOException {
                Log.i(TAG, "onResponse: ");
                Log.i(TAG, "onResponse: header = " + response.headers().toString());
                ResponseBody body = response.body();
                if (body != null) {
                    String result = body.string();
                    Log.i(TAG, "onResponse: body = " + result);
                }
            }
        });
    }

    /**
     * 获取本地图库图片
     */
    private void getImage() {
        Intent intent = new Intent(mContext, GetImageActivity.class);
        intent.putExtra("uuid", mCurrentFriend.uuid);
        startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }

    private void getImageOrVideo() {
        String action = "com.readboy.gallery3.get_media";
        Intent intent = new Intent(action);
        startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }

    private void uploadVideo() {
        String path = Environment.getExternalStorageDirectory().getPath()
                + "/Gallery3/tmp/Video/Video_1547274715584.mp4";
        Conversation video = getSendBaseConversation(Constant.SEND_VIDEO);
        try {
            video.lastTime = Integer.valueOf(MediaUtils.getVideoDuration(path));
        } catch (Exception e) {
            Log.i(TAG, "uploadVideo: e = " + e.toString());
            video.lastTime = 0;
        }
        video.voiceLocalPath = path;
        if (new File(path).exists()) {
//            saveConversationToLocal(video);
//            mNetWorkUtils.uploadFile(video, mUploadFileHandler);

        } else {
            Log.e(TAG, "uploadVideo: file not exit. " + path);
        }
    }

    /**
     * 获取表情Id(某种编码对应表情)
     */
    private void getEmojiId() {
        Intent intent = new Intent(mContext, EmojiActivity.class);
        intent.putExtra("uuid", mCurrentFriend.uuid);
        startActivityForResult(intent, REQUEST_CODE_EMOJI);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_EMOJI:
                Conversation emoji = getSendBaseConversation(Constant.SEND_EMOJI);
                emoji.emojiId = data.getIntExtra(Constant.REQUEST_EMOJI_ID, 0);
                if (mCurrentFriend.model == Model.W5) {
                    emoji.emojiCode = EmojiUtils.getOldCode(emoji.emojiId);
                } else {
                    emoji.emojiCode = EmojiUtils.getEmojiCode(emoji.emojiId);
                }
                emoji.recId = mCurrentFriend.uuid;
                saveConversationToLocal(emoji);
                sendConversationInfo(emoji);
                break;
            case REQUEST_CODE_IMAGE:
                Uri uri = data.getData();
                int fileType = data.getIntExtra("fileType", 1);
                if (uri != null) {
                    if (fileType == FILE_TYPE_IMAGE) {
                        mCurrentImagePath = MediaUtils.getImagePath(mContext, uri);
                        showImageConversation();
                    } else if (fileType == FILE_TYPE_VIDEO) {
                        handleVideoData(MediaUtils.getVideoInfo(mContext, uri));
                    }
                } else {
                    mCurrentImagePath = data.getStringExtra("path");
                    showImageConversation();
                }
                LogInfo.i("hwj", "send image path : " + mCurrentImagePath);
//                uploadTest();
                break;
            default:
                break;
        }
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            try {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        if (index > -1) {
                            data = cursor.getString(index);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "getRealFilePath: e = " + e.toString(), e);
                return "";
            }
        }
        return data;
    }

    private void getVideoInfo(Context context, final Uri uri) {

    }

    private void checkIsShareImage() {
        Intent fromIntent = getActivity().getIntent();
        if (fromIntent == null || TextUtils.isEmpty(fromIntent.getStringExtra(Constant.SHARE_IMAGE_PATH))) {
            return;
        }
        //相机分享图片,直接发送
        String sharePath = fromIntent.getStringExtra(Constant.SHARE_IMAGE_PATH);
        if (!TextUtils.isEmpty(sharePath)) {
            mCurrentImagePath = sharePath;
            showImageConversation();
        }
    }

    /**
     * 保存选择的图片并保存在软件根目录
     */
    private void showImageConversation() {
        Conversation img = getSendBaseConversation(Constant.SEND_IMAGE);
        img.imageLocalPath = mCurrentImagePath;
        if (new File(img.imageLocalPath).exists()) {
            saveConversationToLocal(img);
            mNetWorkUtils.uploadFile(img, mUploadFileHandler);
        } else {
            showMsg(getString(R.string.send_image_fail));
        }
    }

    private void handleVideoData(VideoInfo videoInfo) {
        if (videoInfo == null) {
            showMsg("视频获取失败");
            return;
        }
        String path = videoInfo.data;
        Log.i(TAG, "handleVideoData: path = " + path);
        mCurrentVideoPath = path;
        Conversation video = getSendBaseConversation(Constant.SEND_VIDEO);
        video.lastTime = videoInfo.duration;
        video.imageLocalPath = path;
        video.voiceLocalPath = path;
        video.content = path;
        if (new File(path).exists()) {
            saveConversationToLocal(video);
            mNetWorkUtils.uploadFile(video, mUploadFileHandler);
//            HttpClient.uploadVideo(path, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Log.i(TAG, "onFailure() called with: call = " + call + ", e = " + e + "");
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    Log.i(TAG, "onResponse: ");
//                    Log.i(TAG, "onResponse: header = " + response.headers().toString());
//                    ResponseBody body = response.body();
//                    if (body != null) {
//                        String result = body.string();
//                        Log.i(TAG, "onResponse: body = " + result);
//                    }
//                }
//            });
        } else {
            showMsg("视频不存在");
        }
    }

    /**
     * 保存聊天信息到本地
     *
     * @param conversation 消息
     */
    private void saveConversationToLocal(Conversation conversation) {
        Cursor cursor = mResolver.query(Conversations.Conversation.CONVERSATION_URI,
                new String[]{Conversations.Conversation.CONVERSATION_ID},
                Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId}, null);
        //本地没有当前信息,就保存
        if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
            mResolver.insert(Conversations.Conversation.CONVERSATION_URI,
                    ConversationProvider.getContentValue(conversation, false));
            //添加消息数据
            mConversations.add(conversation);
            LogInfo.i("hwj", "saveConversationToLocal ---------- add conversation notifyDataSetChanged");
            //刷新显示
            notifyAndScrollBottom();
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * 刷新，并跳转到最新一条
     */
    private void notifyAndScrollBottom() {
        Log.i(TAG, "notifyAndScrollBottom: thread = " + Thread.currentThread().getName());
        mAdapter.notifyDataSetChanged();
        //本应该是mAdapter.getCount-1的，但是发送图片时有bug，显示的不是最底下的图片。
        mConversationList.post(new Runnable() {
            @Override
            public void run() {
                mConversationList.setSelection(mAdapter.getCount());
            }
        });
    }

    public static final int SEND_MESSAGE_SUCCESS = 0x11;
    public static final int SEND_MESSAGE_FAIL = 0x12;

    private final Handler mSendMessageResultHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if (message.what == SEND_MESSAGE_SUCCESS) {
                handleSendMessageResult(message, false);
            } else if (message.what == SEND_MESSAGE_FAIL) {
                handleSendMessageResult(message, true);
            }
        }
    };

    /**
     * 根据发送结果更新界面显示
     *
     * @param message      结果消息
     * @param shouldResend 是否需要显示重发标识
     */
    private void handleSendMessageResult(Message message, boolean shouldResend) {
        Conversation conversation = (Conversation) message.obj;
        ContentValues values = new ContentValues();
        values.put(Conversations.Conversation.IS_SENDING, Constant.FALSE);
        conversation.isSending = Constant.FALSE;
        if (shouldResend) {
            values.put(Conversations.Conversation.SHOULD_RESEND, Constant.TRUE);
            conversation.shouldResend = Constant.TRUE;
        } else {
            values.put(Conversations.Conversation.SHOULD_RESEND, Constant.FALSE);
            conversation.shouldResend = Constant.FALSE;
        }
        mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId});
        LogInfo.i("hwj", "handleSendMessageResult ---------- change conversation sending state notifyDataSetChanged");
        notifyAndScrollBottom();
    }

    private void sendConversationInfo(final Conversation conversation) {
        mNetWorkUtils.sendMessage(conversation, new PushResultListener() {

            @Override
            public void pushSucceed(String type, String s1, int code, String s,
                                    String response) {
                LogInfo.i("hwj", "pushSucceed : " + response);
                //存储进数据库里
                mSendMessageResultHandler.obtainMessage(SEND_MESSAGE_SUCCESS, conversation).sendToTarget();
            }

            @Override
            public void pushFail(String s, String s1, int i, String s2) {
                LogInfo.i("hwj", "pushSucceed : " + s2);
                mSendMessageResultHandler.obtainMessage(SEND_MESSAGE_FAIL, conversation).sendToTarget();
            }
        });
    }

    /**
     * 弹出录音对话框
     */
    private void showRecordDialog() {
        if (mAdapter != null) {
            mAdapter.stopPlaying();
        }
        if (mRecordDialog == null) {
            mRecordDialog = new Dialog(mContext, R.style.FullScreenDialogTheme);
            mRecordDialog.setCancelable(false);
            mRecordDialog.setContentView(R.layout.record_dialog_small);
            mRecordStateImg = (ImageView) mRecordDialog.findViewById(R.id.record_state);
            adjustRecordBtn(mCurrentFriend.model);
            mRecordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    cancelScreenOn();
                }
            });
        }
        mRecordStateImg.setActivated(true);
        TextView name = (TextView) mRecordDialog.findViewById(R.id.record_name);
        name.setText(mCurrentFriend.name);
        ImageView recordAnim = (ImageView) mRecordDialog.findViewById(R.id.record_voice);
        AnimationDrawable drawable = (AnimationDrawable) recordAnim.getBackground();
        drawable.start();
        mRecordDialog.show();
        keepScreenOn();
    }

    private void adjustRecordBtn(Model model) {
        Log.e(TAG, "adjustRecordBtn() called with: model = " + model + "");
        if (model == null || mRecordStateImg != null) {
            LogInfo.e(TAG, "adjustRecordBtn model = " + model);
            return;
        }
        switch (model) {
            case W2S:
            case W2T:
            case W3T:
                break;
            case W5:
                mSendVoiceBtn.setBackgroundResource(R.drawable.btn_send_voice_middle_selector);
                mSendEmojiBtn.setVisibility(View.VISIBLE);
                mSendImageBtn.setVisibility(View.GONE);
                break;
            default:
                mSendVoiceBtn.setBackgroundResource(R.drawable.btn_send_voice_short_selector);
                mSendImageBtn.setVisibility(View.VISIBLE);
                mSendImageBtn.setVisibility(View.VISIBLE);
                break;
        }

    }

    /**
     * 开始录音
     */
    private void startRecording() {
        Log.e(TAG, "startRecording: ");
        showRecordDialog();
        //初始化录音相关变量
        isRecording = true;
        mRecorder.ready();
        //开始录音
        mRecorder.start();
        mTimerHandler.removeMessages(MESSAGE_STOP_RECORD);
        mTimerHandler.sendEmptyMessageDelayed(MESSAGE_STOP_RECORD, maxRecordTime);
        startRecordTime = System.currentTimeMillis();
        muteAudioFocus(mContext, true);
    }

    /**
     * 计时线程的Handler
     */
    private Handler mTimerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_STOP_RECORD) {
                //提示信息
                showMsg(getResources().getString(R.string.out_off_limit_format, maxRecordTimeSecond));
                muteAudioFocus(mContext, false);
                LogInfo.i("hwj", "timeout --- prepareSendVoiceMessage");
                //超时之后默认还是会发送
                prepareSendVoiceMessage(true);
            }
        }
    };

    /**
     * 计时线程。
     */
    private Runnable limitTimeRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.e(TAG, "run: limitTimeRunnable.");
//            while (isRecording) {
//                LogInfo.i("hwj", "limitTime ------------- " + limitTime);
//                try {
//                    //录音过程中间隔1s执行一次
//                    Thread.sleep(1000);
//                    limitTime--;
//                    if (limitTime == 0) {
//                        //超过最大时长
//                        mTimerHandler.sendEmptyMessage(MESSAGE_STOP_RECORD);
//                        mTimerHandler.sendEmptyMessageDelayed(MESSAGE_STOP_RECORD, limitTime);
//                        break;
//                    }
//                } catch (InterruptedException e) {
//                    //当线程处于Sleep时调用会出现这个异常,所以要再执行一次
//                    CrashReport.postCatchedException(e);
////                    mTimerThread.interrupt();
//                }
//            }
        }
    };

    protected void prepareSendVoiceMessage() {
        prepareSendVoiceMessage(false);
    }

    /**
     * 录音结束,准备发送
     *
     * @param isTimeout TODO 针对最长录音时间，投机取巧，强制显示为10s, 或者20s
     */
    protected void prepareSendVoiceMessage(boolean isTimeout) {
        isRecording = false;
        //更新录音时间
//        mRecordTime = maxRecordTime - limitTime;
        float temp = System.currentTimeMillis() - startRecordTime;
        Log.e(TAG, "prepareSendVoiceMessage: record time = " + temp);
        int maxRecordTime = (int) (this.maxRecordTime * 0.001F);
        if (isTimeout) {
            mRecordTime = maxRecordTime;
        } else {
            mRecordTime = (int) (temp * 0.001);
            //测试测出该问题，但是不知为什么，手动调整。
            if (mRecordTime > maxRecordTime) {
                mRecordTime = maxRecordTime;
            }
        }
        Log.e(TAG, "prepareSendVoiceMessage: mRecordTime = " + mRecordTime);
        //停止录音
        stopRecording();
        //录音时间太短
        if (mRecordTime < MIN_RECORD_TIME) {
            Log.e(TAG, "prepareSendVoiceMessage: startRecordTime = " + startRecordTime +
                    ", offset = " + (System.currentTimeMillis() - startRecordTime));
            mRecordDialog.dismiss();
            showMsg(getString(R.string.voice_error_msg));
            File oldFile = new File(mRecorder.getFilePath());
            //删除该语音文件
            if (oldFile.exists()) {
                oldFile.delete();
                oldFile.deleteOnExit();
            }
        } else {
            //显示发送语音项
            showVoiceMessage();
        }
    }

    /**
     * 文件上传Handler
     */
    private Handler mUploadFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == NetWorkUtils.UPLOAD_SUCCEED) {
                //文件上传成功
                if (msg.arg1 == Constant.SEND_VOICE
                        || msg.arg1 == Constant.SEND_VIDEO) {
                    Conversation voice = msg.getData().getParcelable(NetWorkUtils.CONVERSATION_TAG);
                    if (voice != null) {
                        ContentValues values = new ContentValues();
                        values.put(Conversations.Conversation.VOICE_URL, voice.voiceUrl);
                        int row = mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                                Conversations.Conversation.CONVERSATION_ID + " = ?",
                                new String[]{voice.conversationId});
                        LogInfo.i("hwj", "mUploadFileHandler --- sendConversationInfo voice, row = " + row);
                        sendConversationInfo(voice);
                    }
                } else if (msg.arg1 == Constant.SEND_IMAGE) {
                    //上传图片的其他信息
                    Conversation image = msg.getData().getParcelable(NetWorkUtils.CONVERSATION_TAG);
                    if (image != null) {
                        ContentValues values = new ContentValues();
                        values.put(Conversations.Conversation.IMAGE_URL, image.imageUrl);
                        values.put(Conversations.Conversation.THUMB_IMAGE_URL, image.thumbImageUrl);
                        mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                                Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{image.conversationId});
                        sendConversationInfo(image);
                    }
                }
            } else if (msg.what == NetWorkUtils.UPLOAD_FAIL) {
                Conversation conversation = msg.getData().getParcelable(NetWorkUtils.CONVERSATION_TAG);
                if (conversation != null) {
                    ContentValues values = new ContentValues();
                    values.put(Conversations.Conversation.IS_SENDING, Constant.FALSE);
                    values.put(Conversations.Conversation.SHOULD_RESEND, Constant.TRUE);
                    mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                            Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId});
                    conversation.isSending = Constant.FALSE;
                    conversation.shouldResend = Constant.TRUE;
                    LogInfo.i("hwj", "mUploadFileHandler ---------- change conversation uploadFile state notifyDataSetChanged");
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * 显示发送语音消息
     */
    private void showVoiceMessage() {
        if (mRecordDialog != null) {
            //隐藏对话框
            mRecordDialog.dismiss();
        }
        //语音消息基本参数
        Conversation voice = getSendBaseConversation(Constant.SEND_VOICE);
        //语音消息时长,本地的记录方法
        voice.lastTime = mRecordTime;
        //保存语音文件的本地路径
        voice.voiceLocalPath = mRecorder.getFilePath();
        if (new File(voice.voiceLocalPath).exists()) {
            //存入数据库
            saveConversationToLocal(voice);
            //上传语音文件,上传成功之后再发送消息
            mNetWorkUtils.uploadFile(voice, mUploadFileHandler);
        } else {
            Log.d(TAG, "showVoiceMessage: voice not exists.");
            showMsg(getString(R.string.send_voice_fail));
        }
    }

    /**
     * 发送消息
     *
     * @param type
     */
    private Conversation getSendBaseConversation(int type) {
        Conversation conversation = new Conversation();
        conversation.conversationId = NetWorkUtils.md5(String.valueOf(System.currentTimeMillis()));
        conversation.senderName = MPrefs.getNickName(mContext);
        //发件人的Id
        conversation.sendId = MPrefs.getDeviceId(mContext);
        //收件人的Id
        conversation.recId = mCurrentFriend.uuid;
        conversation.isSending = Constant.TRUE;
        conversation.shouldResend = Constant.FALSE;
        if (conversation.recId.equals(MPrefs.getHomeGroupId(mContext))) {
            conversation.isHomeGroup = Constant.TRUE;
        }
        //消息类型
        conversation.type = type;
        if (type == Constant.SEND_VOICE || type == Constant.SEND_VIDEO) {
            conversation.isPlaying = Constant.FALSE;
        }
        conversation.time = String.valueOf(System.currentTimeMillis());
        return conversation;
    }

    /**
     * 停止录音线程
     */
    private void stopRecording() {
        Log.e(TAG, "stopRecording: ");
        isRecording = false;
        mTimerHandler.removeMessages(MESSAGE_STOP_RECORD);
        mRecorder.stop();
    }

    private float clickY = 0;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();
        //长按录音按钮
        if (id == R.id.send_voice_btn) {
            //长按松手监听
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onTouch: action_down.");
                    clickY = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    //处于录音状态
                    Log.e(TAG, "onTouch: action_up");
                    if (isRecording) {
                        muteAudioFocus(mContext, false);
                        //上滑距离小于20
                        if (clickY - motionEvent.getY() < 20) {
                            isRecording = false;
                            LogInfo.i("hwj", "ACTION_UP --- prepareSendVoiceMessage");
//                            if (mTimerThread != null && !mTimerThread.isInterrupted()) {
//                                mTimerThread.interrupt();
//                            }
                            prepareSendVoiceMessage();
                        } else {
                            //上移取消录音
                            cancelRecord();
                        }
                        requestParentDisallowInterceptTouchEvent(false);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    //处于录音状态
                    if (isRecording && mRecordStateImg != null) {
                        //上滑距离小于20
                        if (clickY - motionEvent.getY() > 20) {
                            //上移取消录音
                            if (!mRecordStateImg.isActivated()) {
                                mRecordStateImg.setActivated(true);
                                mRecordStateImg.setImageResource(R.drawable.btn_cancel_send_normal);
                            }
                        } else {
                            if (mRecordStateImg.isActivated()) {
                                mRecordStateImg.setActivated(false);
                                mRecordStateImg.setImageResource(R.drawable.btn_send_voice_common_normal);
                            }
                        }
                        requestParentDisallowInterceptTouchEvent(true);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.d(TAG, "onTouch: action_cancel.");
                    cancelRecord();
                    requestParentDisallowInterceptTouchEvent(false);
                    break;
            }
        }
        return false;
    }

    /**
     * 上滑取消录音
     */
    private void cancelRecord() {
        Log.e(TAG, "cancelRecord: ");
        if (!isRecording) {
            return;
        }
        stopRecording();
//        showMsg(getString(R.string.cancel_record));
        mRecordDialog.dismiss();
        File oldFile = new File(mRecorder.getFilePath());
        //删除该语音文件
        if (oldFile.exists()) {
            oldFile.delete();
        }
    }

    private void keepScreenOn() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void cancelScreenOn() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onLongClick(View v) {
        startRecording();
        return true;
    }

    private boolean isHiding = false;

    public void onDestroy() {
        unregisterContentObserver();
        if (mAdapter != null) {
            mAdapter.stopPlaying();
            mAdapter.release();
        }
        if (mUploadFileHandler != null) {
            mUploadFileHandler.removeCallbacksAndMessages(null);
        }
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
        }
        mSendMessageResultHandler.removeCallbacksAndMessages(null);
        muteAudioFocus(mContext, false);

//        mTimerExecutor.shutdownNow();
    }

    public void onPause() {
        LogInfo.i("ConversationActivity --- onPause");
        //用户未读信息数清零
//        NotificationUtils.cancelMessageNotification(mContext);
//        clearUnreadCount(mContext, mCurrentFriend.uuid);
        if (mAdapter != null) {
            mAdapter.stopPlaying();
        }
        if (isRecording) {
            muteAudioFocus(mContext, false);
            LogInfo.i("hwj", "onPause --- prepareSendVoiceMessage");
            prepareSendVoiceMessage();
        }
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    private Activity getActivity() {
        if (mActivity != null) {
            return mActivity;
        } else {
            return (Activity) mContext;
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        getActivity().startActivityForResult(intent, requestCode);
    }

    private String getString(int resId) {
        return getResources().getString(resId);
    }

    private String getString(int resId, Object... args) {
        return getResources().getString(resId, args);
    }

    private void showMsg(String message) {
        ToastUtils.show(mContext, message);
    }

    private String resolveMemberName(String uuid) {
        return resolveMemberName(uuid, "");
    }

    private String resolveMemberName(String uuid, String defaultValue) {
        if (mMemberMap != null) {
            Friend friend = mMemberMap.get(uuid);
            if (friend != null) {
                return friend.name;
            }
        }
        return defaultValue;
    }

    public void setMemberMap(Map<String, Friend> map) {
        this.mMemberMap = map;
    }

    private void runOnUiThread(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }

    private void resolverConversations(List<Conversation> conversations) {
        Log.i(TAG, "resolverConversations:  1  >> ");
        for (Conversation conversation : conversations) {
            if (TextUtils.isEmpty(conversation.voiceUrl) && TextUtils.isEmpty(conversation.voiceLocalPath)) {
                conversation.shouldResend = Constant.TRUE;
            }
        }
        Log.i(TAG, "resolverConversations:  2 >> ");
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent p = mParent.getParent();
        if (p != null) {
            p.requestDisallowInterceptTouchEvent(disallowIntercept);
        } else {
            Log.e(TAG, "requestParentDisallowInterceptTouchEvent: null.");
        }
    }

    /**
     * 录音时调用该方法暂停播放器的声音
     *
     * @param context 上下文
     * @param bMute   true：关闭背景音乐；false：恢复背景音乐。背景音乐是指播放器后台播放音乐
     * @return
     */
    public static boolean muteAudioFocus(Context context, boolean bMute) {
        AudioManager am = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int result = -1;
        if (bMute) {
            result = am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        } else {
            result = am.abandonAudioFocus(null);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private class ContactsObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ContactsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.e(TAG, "contacts onChange() called with: selfChange = " + selfChange + "");
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.e(TAG, "contacts onChange() called with: selfChange = " + selfChange + ", uri = " + uri);
        }
    }
}
