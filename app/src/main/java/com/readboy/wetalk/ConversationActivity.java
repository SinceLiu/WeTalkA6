package com.readboy.wetalk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.readboy.adapter.ConversationListAdapterSimple;
import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.bean.Friend;
import com.readboy.bean.Model;
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
import com.readboy.utils.WTContactUtils;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author hwj
 * @author oubin
 */
public class ConversationActivity extends BaseActivity implements OnClickListener,
        OnScrollListener, OnLongClickListener, OnTouchListener {
    private static final String TAG = "hwj_ConversaActivity";

    private static final int MAX_COUNT = 100;

    private View mParent;
    private ListView mConversationList;
    private View mSendEmojiBtn;
    private View mSendImageBtn;
    private TextView mConversationName;
    private View mHeaderView;
    private List<Conversation> mConversations;
    private ConversationListAdapterSimple mAdapter;
    private Friend mCurrentFriend = null;
    private ContentResolver mResolver;
    private NetWorkUtils mNetWorkUtils;
    private View mSendBtnLayout;
    private boolean isOnPause = false;
    private Button mSendVoiceBtn;
    private ImageView mRecordStateImg;
    private View mLoading;

    /**
     * 录音相关
     */
    private static final int MIN_RECORD_TIME = 1;
    private static final int MAX_RECORD_TIME = 10_000;
    private static final int MESSAGE_STOP_RECORD = 1;
    private int mRecordTime;
    private long startRecordTime;
    private Dialog mRecordDialog;
    private RecordStrategy mRecorder;

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

    private float density;

    /**
     * 监听消息ContentProvider数据变化,只有收到消息时候才会回调
     */
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            //获取最新的消息集合
            List<Conversation> conversations = ConversationProvider.getConversationList(ConversationActivity.this, mCurrentFriend.uuid);
            if (conversations == null || mConversations == null ||
                    conversations.size() <= mConversations.size()) {
                CrashReport.postCatchedException(new UnknownError("新获取的conversations不大于旧的conversations"));
                return;
            }
            int length = conversations.size();
            for (int i = length - 1; i >= 0; i--) {
                //按时间降序,获取最新的一条消息
                Conversation conversation = conversations.get(i);
                if (!conversation.sendId.equals(MPrefs.getDeviceId(ConversationActivity.this))) {
                    //添加到显示的消息列表集合
                    mConversations.add(conversation);
                    LogInfo.i("hwj", "onChange ---------- add conversation notifyDataSetChanged");
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
            mConversationList.setSelection(ListView.FOCUS_DOWN);
        }
    };

    /**
     * 异步获取通讯录
     */
    private class GetConversationTask extends AsyncTask<Void, Void, List<Conversation>> {

        @Override
        protected List<Conversation> doInBackground(Void... params) {
            return ConversationProvider.getConversationList(ConversationActivity.this, mCurrentFriend.uuid);
        }

        @Override
        protected void onPostExecute(List<Conversation> result) {
            Log.e(TAG, "onPostExecute: mConversations = " + mConversations);
            List<Conversation> temp = new ArrayList<>();
            if (mConversations != null && mConversations.size() > 0) {
                temp.addAll(mConversations);
            }
            mConversations = result;
            if (temp.size() > 0) {
                mConversations.addAll(temp);
            }
            limitConversationSize();
            mAdapter = new ConversationListAdapterSimple(ConversationActivity.this, mConversations);
            mAdapter.setSendMessageHandler(mSendMessageResultHandler);
            mConversationList.setAdapter(mAdapter);
            mConversationList.setSelection(ListView.FOCUS_DOWN);
            checkIsShareImage();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogInfo.i("ConversationActivity --- onCreate");
        super.onCreate(savedInstanceState);
        mParent = LayoutInflater.from(this).inflate(R.layout.activity_square_conversation, null);
        setContentView(mParent);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        density = displayMetrics.density;
        initView();
        Log.e(TAG, "onCreate: homeGroup = " + MPrefs.getHomeGroupId(this));

//        test();
    }

    private void test() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Collection<String> collection = EmojiUtils.test3();
                for (String s : collection) {
                    Log.e(TAG, "run: s = " + s);
                    Conversation emoji = getSendBaseConversation(Constant.SEND_EMOJI);
                    emoji.emojiCode = s;
                    emoji.recId = mCurrentFriend.uuid;
                    emoji.shouldResend = Constant.FALSE;
                    emoji.isSending = Constant.TRUE;
                    saveConversationToLocal(emoji);
                    sendConversationInfo(emoji);
                }
            }
        }, 3000);
    }

    @Override
    protected void initView() {
        Log.e(TAG, "initView: ");
        mConversationList = (ListView) getView(R.id.conversation_list);
        mHeaderView = getLayoutInflater().inflate(R.layout.conversation_header, mConversationList, false);
        View mFooterView = getLayoutInflater().inflate(R.layout.conversation_footer, mConversationList, false);
        mConversationList.addHeaderView(mHeaderView);
        mConversationList.addFooterView(mFooterView);
        mSendEmojiBtn = getView(R.id.send_emoji_btn);
        mSendVoiceBtn = (Button) getView(R.id.send_voice_btn);
        mSendImageBtn = getView(R.id.send_image_btn);
        mSendBtnLayout = getView(R.id.conversation_send_msg_tab);
        View mNoDataTip = getView(R.id.no_msg_tip);
        mConversationList.setEmptyView(mNoDataTip);
        mConversationName = (TextView) getView(R.id.conversation_name_tip);
        mLoading = getView(R.id.loading);
        initData();
        GetConversationTask mConversationTask = new GetConversationTask();
        mConversationTask.execute();
    }

    @Override
    protected void initData() {
        Log.e(TAG, "initData: ");
        mResolver = getContentResolver();
        //初始化录音对象
        mRecorder = new AudioRecorder(this);
        mNetWorkUtils = NetWorkUtils.getInstance(this);
        mResolver.registerContentObserver(Conversations.Conversation.CONVERSATION_URI, true, mObserver);
        mSendEmojiBtn.setOnClickListener(this);
        mSendImageBtn.setOnClickListener(this);
        mSendVoiceBtn.setOnLongClickListener(this);
        mSendVoiceBtn.setOnTouchListener(this);
        mConversationList.setOnScrollListener(this);
        initFriendData();
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

    @Override
    protected void onResume() {
        LogInfo.i("ConversationActivity --- onResume");
        super.onResume();
        MPrefs.setNotificationType(this, false);
    }

    /**
     * 初始化好友数据
     */
    private void initFriendData() {
        Log.e(TAG, "initFriendData() called");
        //获取传递参数(用户Id)
        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            //普通的微聊界面
            mCurrentFriend = new Friend();
            mCurrentFriend.uuid = fromIntent.getStringExtra(Constant.EXTRA_FRIEND_ID);
            mCurrentFriend.name = fromIntent.getStringExtra(Constant.EXTRA_FRIEND_NAME);
            mCurrentFriend.unreadCount = fromIntent.getIntExtra(Constant.FRIEND_UNREAD_COUNT, 0);
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
            showMsg(getString(R.string.wrong_data));
            finish();
        }
    }

    private void initModel() {
        Profile.getProfile(this, mCurrentFriend.uuid, new Profile.CallBack() {
            @Override
            public void onResponse(final Profile profile) {
                Log.e(TAG, "onResponse() called with: profile = " + profile + "");
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
                getImage();
                break;
            default:
                break;
        }
    }

    /**
     * 获取本地图库图片
     */
    private void getImage() {
        Intent intent = new Intent(this, GetImageActivity.class);
        intent.putExtra("uuid", mCurrentFriend.uuid);
        startActivityForResult(intent, Constant.REQUEST_IMAGE);
    }

    protected void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        //相片类型
        intent.setType("image/*");
        startActivityForResult(intent, Constant.REQUEST_IMAGE);
    }

    /**
     * 获取表情Id(某种编码对应表情)
     */
    private void getEmojiId() {
        Intent intent = new Intent(this, EmojiActivity.class);
        intent.putExtra("uuid", mCurrentFriend.uuid);
        startActivityForResult(intent, Constant.REQUEST_EMOJI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Constant.REQUEST_EMOJI:
                Conversation emoji = getSendBaseConversation(Constant.SEND_EMOJI);

                emoji.emojiId = data.getIntExtra(Constant.REQUEST_EMOJI_ID, 0);
                if (mCurrentFriend.model == Model.W5) {
                    emoji.emojiCode = EmojiUtils.getOldCode(emoji.emojiId);
                } else {
                    emoji.emojiCode = EmojiUtils.getEmojiCode(emoji.emojiId);
                }
                emoji.recId = mCurrentFriend.uuid;
                emoji.shouldResend = Constant.FALSE;
                emoji.isSending = Constant.TRUE;
                saveConversationToLocal(emoji);
                sendConversationInfo(emoji);
                break;
            case Constant.REQUEST_IMAGE:
                if (data != null) {
                    mCurrentImagePath = data.getStringExtra("path");
                    LogInfo.i("hwj", "send image path : " + mCurrentImagePath);
                    showImageConversation();
                } else {
                    showMsg(getString(R.string.get_image_fail));
                }
                break;
            default:
                break;
        }
    }

    private void checkIsShareImage() {
        Intent fromIntent = getIntent();
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
        img.shouldResend = Constant.FALSE;
        img.isSending = Constant.TRUE;
        if (new File(img.imageLocalPath).exists()) {
            saveConversationToLocal(img);
            mNetWorkUtils.uploadFile(img, mUploadFileHandler);
        } else {
            showMsg(getString(R.string.send_image_fail));
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
        if (cursor == null || cursor.getCount() == 0) {
            mResolver.insert(Conversations.Conversation.CONVERSATION_URI,
                    ConversationProvider.getContentValue(conversation, false));
            //添加消息数据
            if (mConversations == null) {
                mConversations = new ArrayList<>();
                BuglyLog.e(TAG, "mConversation = null.");
            }
            mConversations.add(conversation);
            LogInfo.i("hwj", "saveConversationToLocal ---------- add conversation notifyDataSetChanged");
            //刷新显示
            mAdapter.notifyDataSetChanged();
            //自动跳转到最新一条
            mConversationList.setSelection(ListView.FOCUS_DOWN);
        }
        if (cursor != null) {
            cursor.close();
        }
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
        mAdapter.notifyDataSetChanged();
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
            mRecordDialog = new Dialog(this, R.style.FullScreenDialogTheme);
            mRecordDialog.setCancelable(false);
            mRecordDialog.setContentView(R.layout.record_dialog_small);
            mRecordStateImg = (ImageView) mRecordDialog.findViewById(R.id.record_state);
            adjustRecordBtn(mCurrentFriend.model);
        }
        mRecordStateImg.setActivated(true);
        TextView name = (TextView) mRecordDialog.findViewById(R.id.record_name);
        name.setText(mCurrentFriend.name);
        ImageView recordAnim = (ImageView) mRecordDialog.findViewById(R.id.record_voice);
        AnimationDrawable drawable = (AnimationDrawable) recordAnim.getBackground();
        drawable.start();
        mRecordDialog.show();
    }

    private void adjustRecordBtn(Model model) {
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
        mTimerHandler.sendEmptyMessageDelayed(MESSAGE_STOP_RECORD, MAX_RECORD_TIME);
        startRecordTime = System.currentTimeMillis();
        muteAudioFocus(this, true);
    }

    /**
     * 计时线程的Handler
     */
    private Handler mTimerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_STOP_RECORD) {
                //提示信息
                showMsg(getString(R.string.out_off_limit));
                muteAudioFocus(ConversationActivity.this, false);
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
     * @param isTimeout TODO 针对最长录音时间，投机取巧，强制显示为10s
     */
    protected void prepareSendVoiceMessage(boolean isTimeout) {
        isRecording = false;
        //更新录音时间
//        mRecordTime = MAX_RECORD_TIME - limitTime;
        float temp = System.currentTimeMillis() - startRecordTime;
        Log.e(TAG, "prepareSendVoiceMessage: record time = " + temp);
        if (isTimeout) {
            mRecordTime = 10;
        } else {
            mRecordTime = (int) (temp * 0.001);
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
                if (msg.arg1 == Constant.SEND_VOICE) {
                    Conversation voice = msg.getData().getParcelable(NetWorkUtils.CONVERSATION_TAG);
                    if (voice != null) {
                        ContentValues values = new ContentValues();
                        values.put(Conversations.Conversation.VOICE_URL, voice.voiceUrl);
                        mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                                Conversations.Conversation.CONVERSATION_ID + " = ?",
                                new String[]{voice.conversationId});
                        LogInfo.i("hwj", "mUploadFileHandler --- sendConversationInfo voice");
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
                    LogInfo.i("hwj", "mUploadFileHandler ---------- change conversation upload state notifyDataSetChanged");
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
        //重发标识
        voice.shouldResend = Constant.FALSE;
        //发送进度
        voice.isSending = Constant.TRUE;
        if (new File(voice.voiceLocalPath).exists()) {
            //存入数据库
            saveConversationToLocal(voice);
            //上传语音文件,上传成功之后再发送消息
            mNetWorkUtils.uploadFile(voice, mUploadFileHandler);
        } else {
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
        conversation.senderName = MPrefs.getNickName(this);
        //发件人的Id
        conversation.sendId = MPrefs.getDeviceId(this);
        //收件人的Id
        conversation.recId = mCurrentFriend.uuid;
        LogInfo.i("hwj", "recId = " + conversation.recId + "  homeGroupId = " + MPrefs.getHomeGroupId(this));
        if (conversation.recId.equals(MPrefs.getHomeGroupId(this))) {
            conversation.isHomeGroup = Constant.TRUE;
        }
        //消息类型
        conversation.type = type;
        if (type == Constant.SEND_VOICE) {
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
                    clickY = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    //处于录音状态
                    if (isRecording) {
                        muteAudioFocus(this, false);
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

    @Override
    public boolean onLongClick(View v) {
        startRecording();
        return true;
    }

    private boolean isHiding = false;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
//		if(isHiding){
//			isHiding = false;
//			Animation hide = AnimationUtils.loadAnimation(this, R.anim.hide);
//        	hide.setFillAfter(true);
//        	mSendBtnLayout.startAnimation(hide);
//		}
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //滑动结束状态
        if (scrollState == SCROLL_STATE_IDLE) {
//        	if(!isHiding){
//	        	isHiding = true;
//	        	Animation show = AnimationUtils.loadAnimation(this, R.anim.show);
//				show.setFillAfter(true);
//				mSendBtnLayout.startAnimation(show);
//        	}
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mResolver.unregisterContentObserver(mObserver);
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
        muteAudioFocus(this, false);

//        mTimerExecutor.shutdownNow();
    }

    @Override
    protected void onPause() {
        LogInfo.i("ConversationActivity --- onPause");
        super.onPause();
        //用户未读信息数清零
        WTContactUtils.clearFriendUnreadCount(ConversationActivity.this, mCurrentFriend.uuid);
        if (mAdapter != null) {
            mAdapter.stopPlaying();
        }
        if (isRecording) {
            muteAudioFocus(this, false);
            LogInfo.i("hwj", "onPause --- prepareSendVoiceMessage");
            prepareSendVoiceMessage();
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LogInfo.i("ConversationActivity ------ onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LogInfo.i("ConversationActivity ------ onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
