package com.readboy.adapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.readboy.activity.VideoActivity;
import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.provider.Conversations;
import com.readboy.utils.ClickUtils;
import com.readboy.utils.EmojiUtils;
import com.readboy.utils.FriendNameUtil;
import com.readboy.utils.LogInfo;
import com.readboy.utils.MyTimeUtils;
import com.readboy.utils.NetWorkUtils;
import com.readboy.utils.NetWorkUtils.PushResultListener;
import com.readboy.utils.ToastUtils;
import com.readboy.view.ConversationView;
import com.readboy.wetalk.DisplayImageActivity;
import com.readboy.wetalk.R;
import com.readboy.wetalk.TextDialog;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author hwj
 */
public class ConversationListAdapterSimple extends BaseAdapter {
    private static final String TAG = "hwj_ConversationAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Conversation> mConversations;
    private boolean mIsToastShow = false;
    private Handler mSendMessageHandler;
    private ContentResolver mResolver;
    private MediaPlayer mMediaPlayer;
    private TextDialog textDialog;
    private View mKeepScreenView;

    public ConversationListAdapterSimple(Context context, List<Conversation> data) {
        mConversations = data;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mResolver = context.getContentResolver();
    }

    @Override
    public int getItemViewType(int position) {
        //根据消息类型显示布局
        return mConversations != null ? mConversations.get(position).type : super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        //相当于消息类型数
        return 12;
    }

    @Override
    public int getCount() {
        return mConversations != null ? mConversations.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mConversations != null ? mConversations.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //列表项变为可见的时候就会调用此方法
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        //接收的消息比发送的消息会多一项发件人名字,只会在家庭圈显示
        ImageItemHolder imageItemHolder = null;
        VoiceItemHolder voiceItemHolder = null;
        EmojiItemHolder emojiItemHolder = null;
        BaseHolder commonHolder = null;
        final Conversation conversation = mConversations.get(position);
        int type = conversation.type;
        if (view == null) {
            if (type == Constant.SEND_VIDEO) {
                //TODO layoutId可否抽象起来，统一处理
                view = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_video_right, viewGroup, false);
                commonHolder = new VideoItemHolder(view);
                view.setTag(commonHolder);
            } else if (type == Constant.REC_VIDEO) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_video_left, viewGroup, false);
                commonHolder = new VideoItemHolder(view);
                commonHolder.userName = (TextView) view.findViewById(R.id.item_conversation_name);
                view.setTag(commonHolder);
            } else if (type == Constant.REC_SYSTEM) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_system, viewGroup, false);
                commonHolder = new SystemItemHolder(view);
                view.setTag(commonHolder);
            } else if (type == Constant.REC_TEXT) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_text_left, viewGroup, false);
                commonHolder = new TextItemHolder(view);
                commonHolder.userName = (TextView) view.findViewById(R.id.item_conversation_name);
                view.setTag(commonHolder);
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.conversation_item_simple, viewGroup, false);
                switch (type) {
                    //发送表情项
                    case Constant.SEND_EMOJI:
                        emojiItemHolder = new EmojiItemHolder();
                        emojiItemHolder.content = (ImageView) view.findViewById(R.id.conversation_item_send_emoji_content);
                        emojiItemHolder.item = view.findViewById(R.id.conversation_item_send_emoji);
                        emojiItemHolder.progress = (ImageView) view.findViewById(R.id.conversation_item_send_emoji_progress);
                        emojiItemHolder.retry = (ImageView) view.findViewById(R.id.conversation_item_send_emoji_resend_btn);
                        emojiItemHolder.sendOrReceiveTime = (TextView) view.findViewById(R.id.conversation_item_time);
                        view.setTag(emojiItemHolder);
                        break;
                    //接收表情项
                    case Constant.REC_EMOJI:
                        emojiItemHolder = new EmojiItemHolder();
                        emojiItemHolder.content = (ImageView) view.findViewById(R.id.conversation_item_rec_emoji_content);
                        emojiItemHolder.item = view.findViewById(R.id.conversation_item_rec_emoji);
                        emojiItemHolder.userName = (TextView) view.findViewById(R.id.conversation_item_rec_emoji_user_name);
                        emojiItemHolder.sendOrReceiveTime = (TextView) view.findViewById(R.id.conversation_item_time);
                        view.setTag(emojiItemHolder);
                        break;
                    //发送图片项
                    case Constant.SEND_IMAGE:
                        imageItemHolder = new ImageItemHolder(view);
                        break;
                    //接收图片项
                    case Constant.REC_IMAGE:
                        imageItemHolder = new ImageItemHolder();
                        imageItemHolder.content = (ImageView) view.findViewById(R.id.conversation_item_rec_image_content);
                        imageItemHolder.item = view.findViewById(R.id.conversation_item_rec_image);
                        imageItemHolder.userName = (TextView) view.findViewById(R.id.conversation_item_rec_image_user_name);
                        imageItemHolder.sendOrReceiveTime = (TextView) view.findViewById(R.id.conversation_item_time);
                        view.setTag(imageItemHolder);
                        break;
                    //发送语音项
                    case Constant.SEND_VOICE:
                        voiceItemHolder = new VoiceItemHolder();
                        voiceItemHolder.play = view.findViewById(R.id.conversation_item_send_voice_play);
                        voiceItemHolder.time = (TextView) view.findViewById(R.id.conversation_item_send_voice_time);
                        voiceItemHolder.playAnim = (ImageView) view.findViewById(R.id.conversation_item_send_voice_play_anim);
                        voiceItemHolder.item = view.findViewById(R.id.conversation_item_send_voice_item);
                        voiceItemHolder.playImg = view.findViewById(R.id.conversation_item_send_voice_play_imgv);
                        voiceItemHolder.retry = (ImageView) view.findViewById(R.id.conversation_item_send_voice_resend_btn);
                        voiceItemHolder.progress = (ImageView) view.findViewById(R.id.conversation_item_send_voice_progress);
                        voiceItemHolder.sendOrReceiveTime = (TextView) view.findViewById(R.id.conversation_item_time);
                        view.setTag(voiceItemHolder);
                        break;
                    //接收语音项
                    case Constant.REC_VOICE:
                        voiceItemHolder = new VoiceItemHolder();
                        voiceItemHolder.play = view.findViewById(R.id.conversation_item_rec_voice_play);
                        voiceItemHolder.time = (TextView) view.findViewById(R.id.conversation_item_rec_voice_time);
                        voiceItemHolder.unread = view.findViewById(R.id.conversation_item_rec_voice_unread);
                        voiceItemHolder.progress = (ImageView) view.findViewById(R.id.conversation_item_rec_voice_progress);
                        voiceItemHolder.playAnim = (ImageView) view.findViewById(R.id.conversation_item_rec_voice_play_anim);
                        voiceItemHolder.item = view.findViewById(R.id.conversation_item_rec_voice_item);
                        voiceItemHolder.userName = (TextView) view.findViewById(R.id.conversation_item_rec_voice_user_name);
                        voiceItemHolder.playImg = view.findViewById(R.id.conversation_item_rec_voice_play_imgv);
                        voiceItemHolder.sendOrReceiveTime = (TextView) view.findViewById(R.id.conversation_item_time);
                        view.setTag(voiceItemHolder);
                        break;
                    default:
                        Log.e(TAG, "getView: unknown message type, " + conversation.content);
                        CrashReport.postCatchedException(new IllegalAccessException("message send type = " + type));
                        view = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_text_left, viewGroup, false);
                        commonHolder = new UnknownItemHolder(view);
                        view.setTag(commonHolder);
                        break;
                }
            }
        } else {
            switch (type) {
                case Constant.SEND_EMOJI:
                case Constant.REC_EMOJI:
                    emojiItemHolder = (EmojiItemHolder) view.getTag();
                    break;
                case Constant.SEND_IMAGE:
                case Constant.REC_IMAGE:
                    imageItemHolder = (ImageItemHolder) view.getTag();
                    break;
                case Constant.SEND_VOICE:
                case Constant.REC_VOICE:
                    voiceItemHolder = (VoiceItemHolder) view.getTag();
                    break;
                case Constant.REC_TEXT:
                case Constant.SEND_VIDEO:
                case Constant.REC_VIDEO:
                case Constant.REC_SYSTEM:
                    commonHolder = (BaseHolder) view.getTag();
                    break;
                default:
                    Log.i(TAG, "getView: default type.");
                    break;
            }
        }
        //设置显示的数据
        switch (type) {
            case Constant.SEND_EMOJI:
                //发送的表情显示本地的
                emojiItemHolder.item.setVisibility(View.VISIBLE);
                if (conversation.emojiId == 0 && !TextUtils.isEmpty(conversation.emojiCode)) {
                    emojiItemHolder.content.setImageResource(EmojiUtils.getEmojiIdContainOldCode(conversation.emojiCode));
                } else {
                    emojiItemHolder.content.setImageResource(conversation.emojiId);
                }
                showSendEmojiProgressOrResend(conversation, emojiItemHolder);
                showReceiveOrSendTime(conversation, position, emojiItemHolder);
                break;
            case Constant.REC_EMOJI:
                //接收的图片
//                LogInfo.e(TAG, " type = " + type + ", emojiId = " + conversation.emojiId);
                updateName(emojiItemHolder, conversation);
                emojiItemHolder.item.setVisibility(View.VISIBLE);
                emojiItemHolder.content.setImageResource(EmojiUtils.getEmojiIdContainOldCode(conversation.emojiCode));
                showReceiveOrSendTime(conversation, position, emojiItemHolder);
                break;
            case Constant.SEND_IMAGE:
                displayImage(conversation, imageItemHolder, true);
                showUploadFileProgressOrResend(conversation, imageItemHolder);
                showReceiveOrSendTime(conversation, position, imageItemHolder);
                break;
            case Constant.REC_IMAGE:
                updateName(imageItemHolder, conversation);
                displayImage(conversation, imageItemHolder, false);
                showReceiveOrSendTime(conversation, position, imageItemHolder);
                break;
            case Constant.SEND_VOICE:
                voiceItemHolder.item.setVisibility(View.VISIBLE);
                voiceItemHolder.time.setText(String.valueOf(conversation.lastTime + "''"));
                playVoice(conversation, voiceItemHolder, true);
                showUploadFileProgressOrResend(conversation, voiceItemHolder);
                if (conversation.isPlaying == Constant.TRUE) {
                    voiceItemHolder.playAnim.setVisibility(View.VISIBLE);
                    ((AnimationDrawable) voiceItemHolder.playAnim.getBackground()).start();
                    voiceItemHolder.playImg.setVisibility(View.GONE);
                } else {
                    voiceItemHolder.playImg.setVisibility(View.VISIBLE);
                    voiceItemHolder.playAnim.setVisibility(View.GONE);
                }
                showReceiveOrSendTime(conversation, position, voiceItemHolder);
                break;
            case Constant.REC_VOICE:
                //语音数据
                updateName(voiceItemHolder, conversation);
                voiceItemHolder.item.setVisibility(View.VISIBLE);
                voiceItemHolder.time.setText(String.valueOf(conversation.lastTime + "''"));
                if (conversation.isUnPlay == Constant.TRUE) {
                    voiceItemHolder.unread.setVisibility(View.VISIBLE);
                } else {
                    voiceItemHolder.unread.setVisibility(View.GONE);
                }
                playVoice(conversation, voiceItemHolder, false);
                if (conversation.isPlaying == Constant.TRUE) {
                    voiceItemHolder.playAnim.setVisibility(View.VISIBLE);
                    ((AnimationDrawable) voiceItemHolder.playAnim.getBackground()).start();
                    voiceItemHolder.playImg.setVisibility(View.GONE);
                } else {
                    voiceItemHolder.playImg.setVisibility(View.VISIBLE);
                    voiceItemHolder.playAnim.setVisibility(View.GONE);
                }
                showReceiveOrSendTime(conversation, position, voiceItemHolder);
                break;
            case Constant.REC_TEXT:
            case Constant.REC_VIDEO:
                updateName(commonHolder, conversation);
            case Constant.SEND_VIDEO:
            case Constant.REC_SYSTEM:
                if (commonHolder != null) {
                    commonHolder.bindView(position, conversation);
                }
                break;
            default:
                if (commonHolder != null) {
                    commonHolder.bindView(position, conversation);
                }
                Log.e(TAG, "getView: other type = " + type);
        }
        return view;
    }

    private static void updateName(BaseHolder holder, Conversation conversation) {
        if (holder.userName == null || Constant.isSendMessage(conversation.type)) {
            // 系统消息没有名字、不显示发送者自己的名字
            return;
        }
        if (conversation.isHomeGroup == Constant.TRUE) {
            holder.userName.setText(FriendNameUtil.resolveName(conversation.realSendId, conversation.senderName));
            //WTContactUtils.getNameById(mContext,conversation.realSendId));
        } else {
            holder.userName.setVisibility(View.GONE);
        }
    }

    private void showReceiveOrSendTime(Conversation conversation, int position, BaseHolder holder) {
        if (position != 0) {
            long index = Long.parseLong(mConversations.get(position - 1).time) - Long.parseLong(conversation.time);
            //相差1分钟的就显示
            if (Math.abs(index) > 60 * 1000) {
                holder.sendOrReceiveTime.setText(MyTimeUtils.getListTime((Long.parseLong(conversation.time) / 1000)));
            } else {
                holder.sendOrReceiveTime.setText("");
            }
        } else {
            holder.sendOrReceiveTime.setText(MyTimeUtils.getListTime((Long.parseLong(conversation.time) / 1000)));
        }
    }

    /**
     * 发送表情
     *
     * @param conversation 表情项
     * @param holder       holder
     */
    private void showSendEmojiProgressOrResend(final Conversation conversation, final EmojiItemHolder holder) {
        //默认不显示
        holder.retry.setVisibility(View.GONE);
        holder.progress.setVisibility(View.GONE);
        //显示重新发送
        if (conversation.shouldResend == Constant.TRUE) {
            holder.retry.setVisibility(View.VISIBLE);
            holder.progress.setVisibility(View.GONE);
            holder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!NetWorkUtils.isConnectingToInternet(mContext)) {
                        ToastUtils.show(mContext, R.string.internet_down);
                        return;
                    }
                    holder.retry.setVisibility(View.GONE);
                    holder.progress.setVisibility(View.VISIBLE);
                    Glide.with(mContext).
                            load(R.drawable.loading_anim).asGif().into(holder.progress);
                    sendConversationInfo(conversation);
                }
            });
        } else if (conversation.isSending == Constant.TRUE) {
            //正在发送
            holder.retry.setOnClickListener(null);
            holder.retry.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(R.drawable.loading_anim).into(holder.progress);
        }
    }

    public void setSendMessageHandler(Handler handler) {
        mSendMessageHandler = handler;
    }

    protected void sendConversationInfo(final Conversation conversation) {
        if (mSendMessageHandler != null) {
            NetWorkUtils.sendMessage(mContext, conversation, new PushResultListener() {

                @Override
                public void pushSucceed(String type, String s1, int code, String s,
                                        String response) {
                    //存储进数据库里
                    Log.i(TAG, "pushSucceed() called with: type = " + type + ", s1 = " + s1 + ", code = " + code + ", s = " + s + ", response = " + response + "");
                    mSendMessageHandler.obtainMessage(ConversationView.SEND_MESSAGE_SUCCESS, conversation).sendToTarget();
                }

                @Override
                public void pushFail(String s, String s1, int i, String s2) {
                    Log.i(TAG, "send message pushFail() called with: s = " + s + ", s1 = " + s1 + ", i = " + i + ", s2 = " + s2 + "");
                    mSendMessageHandler.obtainMessage(ConversationView.SEND_MESSAGE_FAIL, conversation).sendToTarget();
                }
            });
        }
    }

    private void showUploadFileProgressOrResend(final Conversation conversation, final BaseHolder holder) {
        //默认不显示
        holder.retry.setVisibility(View.GONE);
        holder.progress.setVisibility(View.GONE);
        //显示重新发送
//        Log.e(TAG, "showUploadFileProgressOrResend: shouldResend = " + conversation.shouldResend
//                + ", isSending = " + conversation.isSending + ", id = " + conversation.conversationId);
        if (conversation.shouldResend == Constant.TRUE &&
                conversation.isSending == Constant.TRUE) {
            //点击重发后，正在重发。点击播放按钮可能会刷新该状态
            return;
        }
        if (conversation.shouldResend == Constant.TRUE) {
            holder.progress.setVisibility(View.GONE);
            holder.retry.setVisibility(View.VISIBLE);
            holder.retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!NetWorkUtils.isConnectingToInternet(mContext)) {
                        ToastUtils.show(mContext, R.string.internet_down);
                        return;
                    }
                    int type = conversation.type;
                    Log.i(TAG, "onClick: type = " + type);
                    holder.retry.setVisibility(View.GONE);
                    holder.progress.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(R.drawable.loading_anim).asGif().into(holder.progress);
                    //状态为正在发送，shouldResend应该为false.
                    conversation.shouldResend = Constant.FALSE;
                    if (type == Constant.SEND_IMAGE) {
                        //文件不存在
                        if (!new File(conversation.imageLocalPath).exists()) {
                            ToastUtils.show(mContext, R.string.file_not_found);
                            //删除数据库中的记录
                            mResolver.delete(Conversations.Conversation.CONVERSATION_URI,
                                    Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId});
                            return;
                        }
                        if (TextUtils.isEmpty(conversation.imageUrl)) {
                            //文件已经上传失败,重新上传
                            NetWorkUtils.uploadFile(conversation, mReUploadFileHandler);
                        } else {
                            sendConversationInfo(conversation);
                        }
                    } else if (type == Constant.SEND_VOICE || type == Constant.SEND_VIDEO) {
                        if (TextUtils.isEmpty(conversation.voiceUrl)) {
                            NetWorkUtils.uploadFile(conversation, mReUploadFileHandler);
                        } else {
                            LogInfo.i("hwj", "showUploadFileProgressOrResend --- sendConversationInfo voice");
                            sendConversationInfo(conversation);
                        }
                    } else if (type == Constant.REC_VIDEO || type == Constant.REC_VOICE) {
                        if (!TextUtils.isEmpty(conversation.voiceUrl)) {
                            NetWorkUtils.downloadFile(mContext, conversation, null);
                        } else {
                            Log.i(TAG, "showUploadFileProgressOrResend: url is null.");
                        }
                    }
                }
            });
        } else if (conversation.isSending == Constant.TRUE) {
            //正在发送
            holder.retry.setOnClickListener(null);
            holder.retry.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(R.drawable.loading_anim).into(holder.progress);
        }
    }

    /**
     * 图片项显示
     *
     * @param conversation    消息主体
     * @param imageItemHolder holder
     */
    protected void displayImage(final Conversation conversation, ImageItemHolder imageItemHolder,
                                final boolean isSend) {
        imageItemHolder.item.setVisibility(View.VISIBLE);
        if (isSend) {
            String path = conversation.imageLocalPath;
            //图片文件不存在,删除数据库的记录
            if (!new File(path).exists()) {
                mResolver.delete(Conversations.Conversation.CONVERSATION_URI,
                        Conversations.Conversation.IMAGE_PATH + " = ?", new String[]{path});
            }
            //加载小图
            Glide.with(mContext).load(conversation.imageLocalPath)
                    .placeholder(R.drawable.pic_holder)
                    .error(R.drawable.error)
                    .dontAnimate()
                    .centerCrop()
                    .into(imageItemHolder.content);
        } else {
            //接收图片先显示缩略图
            Glide.with(mContext).load(conversation.thumbImageUrl)
                    .placeholder(R.drawable.pic_holder)
                    .error(R.drawable.error)
                    .dontAnimate()
                    .centerCrop()
                    .into(imageItemHolder.content);
        }

        /**
         * 点击查看大图
         */
        imageItemHolder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "";
                if (isSend) {
                    url = Uri.fromFile(new File(conversation.imageLocalPath)).toString();
                } else {
                    url = conversation.imageUrl;
                }
                Intent intent = new Intent(mContext, DisplayImageActivity.class);
                intent.putExtra("url", url);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * 点击查看文字详情
     *
     * @param conversation   消息
     * @param textItemHolder holder
     */
    protected void displayTextDetail(final Conversation conversation, TextItemHolder textItemHolder) {
        final String text = conversation.textContent;
        textItemHolder.item.setVisibility(View.VISIBLE);
        textItemHolder.content.setText(text);
        textItemHolder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickUtils.isFastMultiClick(2)) {
                    showMessageFullScreen(text);
                }
            }
        });
    }

    private void showMessageFullScreen(String text) {
        if (textDialog == null) {
            textDialog = new TextDialog(mContext, text);
        } else {
            textDialog = new TextDialog(mContext, text);
            //多次设置，并且文本长度不一，可能AlignTextView显示会有问题,
//            textDialog.setText(text);
        }
        textDialog.show();
    }


    /**
     * 播放语音文件
     *
     * @param holder holder
     */
    protected void playVoice(final Conversation conv, final VoiceItemHolder holder, final boolean isSend) {
        //播放的点击事件
        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //语音文件不存在
                if (TextUtils.isEmpty(conv.voiceLocalPath) || !new File(conv.voiceLocalPath).exists()) {
                    //下载，然后播放
                    holder.progress.setVisibility(View.VISIBLE);
                    holder.play.setClickable(false);
                    Glide.with(mContext).load(R.drawable.loading_anim).asGif().into(holder.progress);
                    NetWorkUtils.downloadFile(mContext, conv, new NetWorkUtils.DownLoadFileListener() {
                        @Override
                        public void downLoadSucceed() {
                            holder.progress.setVisibility(View.GONE);
                            holder.play.setClickable(true);
                            play(view, conv, isSend, conv.voiceLocalPath);
                            //TODO 播放语音动画
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    holder.playAnim.setVisibility(View.VISIBLE);
                                    ((AnimationDrawable) holder.playAnim.getBackground()).stop();
                                    ((AnimationDrawable) holder.playAnim.getBackground()).start();
                                    holder.playImg.setVisibility(View.GONE);
                                }
                            }, 100);
                        }

                        @Override
                        public void downLoadFailed() {
                            holder.progress.setVisibility(View.GONE);
                            holder.play.setClickable(true);
                            if (!mIsToastShow) {
                                mIsToastShow = true;
                                ToastUtils.show(mContext, R.string.play_voice_error);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIsToastShow = false;
                                    }
                                }, 2000);
                            }
                        }
                    });
                } else {
                    play(view, conv, isSend, conv.voiceLocalPath);
                }
            }
        });
    }

    //播放
    private void play(View view, Conversation conv, boolean isSend, String path) {
        if (conv.isPlaying == Constant.FALSE) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                stopPlayingAnimation();
                mMediaPlayer = null;
            }
            //初始化MediaPlayer
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setVolume(1.0f, 1.0f);
            //设置播放结束监听器
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    setKeepScreenOn(view, false);
                    ConversationView.muteAudioFocus(mContext, false);
                    //更新接收语音信息,要更新列表的数据才能刷新界面显示
                    if (!isSend && conv.isUnPlay == Constant.TRUE) {
                        //数据库更新播放状态
                        ContentValues values = new ContentValues();
                        values.put(Conversations.Conversation.IS_UN_PLAY, Constant.FALSE);
                        values.put(Conversations.Conversation.IS_PLAYING, Constant.FALSE);
                        mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                                Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conv.conversationId});
                        //列表显示更新,已播放
                        conv.isUnPlay = Constant.FALSE;
                        conv.isPlaying = Constant.FALSE;
                        notifyDataSetChanged();
                    }
                    if (conv.isPlaying == Constant.TRUE) {
                        conv.isPlaying = Constant.FALSE;
                        //数据库更新播放状态
                        ContentValues values = new ContentValues();
                        values.put(Conversations.Conversation.IS_PLAYING, Constant.FALSE);
                        mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                                Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conv.conversationId});
                        notifyDataSetChanged();
                    }
                }
            });
            //播放语音文件
            try {
                ConversationView.muteAudioFocus(mContext, true);
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                conv.isPlaying = Constant.TRUE;
                //数据库更新播放状态
                ContentValues values = new ContentValues();
                values.put(Conversations.Conversation.IS_PLAYING, Constant.TRUE);
                mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                        Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conv.conversationId});
                notifyDataSetChanged();
                setKeepScreenOn(view, true);
            } catch (IOException e) {
                conv.isPlaying = Constant.FALSE;
                //数据库更新播放状态
                ContentValues values = new ContentValues();
                values.put(Conversations.Conversation.IS_PLAYING, Constant.FALSE);
                mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                        Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conv.conversationId});
                notifyDataSetChanged();
                e.printStackTrace();
            }
        } else {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                ConversationView.muteAudioFocus(mContext, false);
                mMediaPlayer.stop();
                stopPlayingAnimation();
                mMediaPlayer = null;
                conv.isPlaying = Constant.FALSE;
                //数据库更新播放状态
                ContentValues values = new ContentValues();
                values.put(Conversations.Conversation.IS_PLAYING, Constant.FALSE);
                mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                        Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conv.conversationId});
                notifyDataSetChanged();
                setKeepScreenOn(view, false);
            }
        }
    }

    private void setKeepScreenOn(View view, boolean keepScreenOn) {
        if (mKeepScreenView != null && mKeepScreenView.getKeepScreenOn()) {
            mKeepScreenView.setKeepScreenOn(false);
        }
        if (view != null) {
            view.setKeepScreenOn(keepScreenOn);
        }
        mKeepScreenView = view;
    }

    /**
     * TODO，重构，公共部分放到Holder里
     */
    class BaseHolder {
        //用户名
        TextView userName;
        //重新发送
        ImageView retry;
        // TODO, 用ProgressBar
        //进度
        ImageView progress;
        //语音项
        View item;
        //时间
        TextView sendOrReceiveTime;

        BaseHolder() {
        }

        BaseHolder(View view) {
            sendOrReceiveTime = view.findViewById(R.id.item_conversation_time);
            userName = view.findViewById(R.id.item_conversation_name);
        }

        void bindView(int position, Conversation conversation) {
            updateName(this, conversation);
            showReceiveOrSendTime(conversation, position, this);
        }
    }

    /**
     * 图片项
     * TODO，重构，公共部分放到Holder里
     */
    protected class ImageItemHolder extends BaseHolder {
        //内容
        ImageView content;

        ImageItemHolder() {
        }

        ImageItemHolder(View view) {
            super(view);
            content = (ImageView) view.findViewById(R.id.conversation_item_send_image_content);
            item = view.findViewById(R.id.conversation_item_send_image);
            retry = (ImageView) view.findViewById(R.id.conversation_item_send_image_resend_btn);
            progress = (ImageView) view.findViewById(R.id.conversation_item_send_image_progress);
            sendOrReceiveTime = (TextView) view.findViewById(R.id.conversation_item_time);
            view.setTag(this);
        }
    }

    /**
     * 语音项
     * TODO，重构，公共部分放到Holder里， 参考{@link VideoItemHolder}
     */
    protected class VoiceItemHolder extends BaseHolder {
        /**
         * 语音时长
         */
        TextView time;
        /**
         * 播放项
         */
        View play;
        /**
         * 播放的动画
         */
        ImageView playAnim;
        /**
         * 未读小红点
         */
        View unread;
        /**
         * 语音动画项
         */
        View playImg;
    }

    /**
     * 表情项
     */
    protected class EmojiItemHolder extends BaseHolder {
        //内容
        ImageView content;

        EmojiItemHolder() {
        }

        EmojiItemHolder(View view) {
            super(view);

        }
    }

    /**
     * 文本项
     */
    protected class TextItemHolder extends BaseHolder {
        TextView content;
        String text;

        TextItemHolder(View view) {
            super(view);
            content = view.findViewById(R.id.item_conversation_text);
            content.setOnClickListener(v -> {
                if (ClickUtils.isFastMultiClick(2)) {
                    showMessageFullScreen(text);
                }
            });
        }

        @Override
        void bindView(int position, Conversation conversation) {
            super.bindView(position, conversation);
            this.text = conversation.textContent;
            content.setText(conversation.textContent);
        }
    }

    class UnknownItemHolder extends TextItemHolder {

        UnknownItemHolder(View view) {
            super(view);
            content.setText("不支持该类型消息");
//            content.setTextColor(Color.RED);
            content.setTextColor(0xFFFF4444);
        }

        @Override
        void bindView(int position, Conversation conversation) {
            updateName(this, conversation);
        }
    }

    protected class VideoItemHolder extends BaseHolder {
        ImageView content;
        Conversation mConversation;

        VideoItemHolder(View view) {
            super(view);
            sendOrReceiveTime = view.findViewById(R.id.item_conversation_time);
            userName = view.findViewById(R.id.item_conversation_name);
            retry = view.findViewById(R.id.item_conversation_retry);
            progress = view.findViewById(R.id.item_conversation_progress_bar);
            content = view.findViewById(R.id.item_conversation_content);
            content.setClickable(true);
            view.setTag(this);
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = mConversation.voiceLocalPath;
                    if (TextUtils.isEmpty(path)) {
//                        mConversation.shouldResend = Constant.TRUE;
//                        showUploadFileProgressOrResend(mConversation, VideoItemHolder.this);
                        content.setClickable(false);
                        progress.setVisibility(View.VISIBLE);
                        Glide.with(mContext).load(R.drawable.loading_anim).asGif().into(progress);
                        NetWorkUtils.downloadFile(mContext, mConversation, new NetWorkUtils.DownLoadFileListener() {
                            @Override
                            public void downLoadSucceed() {
                                content.setClickable(true);
                                notifyDataSetChanged();
                                Intent intent = new Intent(mContext, VideoActivity.class);
                                intent.putExtra(VideoActivity.EXTRA_DATA, mConversation.voiceLocalPath);
                                mContext.startActivity(intent);
                            }

                            @Override
                            public void downLoadFailed() {
                                content.setClickable(true);
                                progress.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        Intent intent = new Intent(mContext, VideoActivity.class);
                        intent.putExtra(VideoActivity.EXTRA_DATA, mConversation.voiceLocalPath);
                        mContext.startActivity(intent);
                    }
                }
            });
        }

        @Override
        void bindView(int position, Conversation conversation) {
            super.bindView(position, conversation);
            this.mConversation = conversation;
            String path = conversation.thumbImageUrl;
            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                Glide.with(mContext)
                        .load(path)
                        .error(R.drawable.error)
                        .into(content);
            } else {
                Glide.with(mContext)
                        .load(conversation.voiceLocalPath)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .crossFade()
                        .centerCrop()
                        .error(R.drawable.error)
                        .into(content);
            }
            showUploadFileProgressOrResend(conversation, this);
        }
    }

    private class SystemItemHolder extends BaseHolder {
        private TextView mContentTv;

        public SystemItemHolder(View view) {
            super(view);
            mContentTv = view.findViewById(R.id.item_conversation_system_content);
        }

        @Override
        void bindView(int position, Conversation conversation) {
            super.bindView(position, conversation);
            mContentTv.setText(conversation.content);
        }
    }

    /**
     * 停止播放语音
     */
    public void stopPlaying() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            ConversationView.muteAudioFocus(mContext, false);
            mMediaPlayer.stop();
            stopPlayingAnimation();
            setKeepScreenOn(null, false);
        }
    }

    private void stopPlayingAnimation() {
        for (Conversation conversation : mConversations) {
            if (conversation.type == Constant.SEND_VOICE || conversation.type == Constant.REC_VOICE) {
                if (conversation.isPlaying == Constant.TRUE) {
                    conversation.isPlaying = Constant.FALSE;
                    //数据库更新播放状态
                    ContentValues values = new ContentValues();
                    values.put(Conversations.Conversation.IS_PLAYING, Constant.FALSE);
                    mResolver.update(Conversations.Conversation.CONVERSATION_URI, values,
                            Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId});
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    public List<Conversation> getConversations() {
        return mConversations;
    }

    //文件上传Handler
    /**
     * 文件上传Handler.
     */
    @SuppressLint("HandlerLeak")
    private Handler mReUploadFileHandler = new Handler() {
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
                        LogInfo.i("hwj", "mReUploadFileHandler ----- sendConversationInfo voice, row = " + row);
                        sendConversationInfo(voice);
                    }
                } else if (msg.arg1 == Constant.SEND_IMAGE) {
                    Conversation image = msg.getData().getParcelable(NetWorkUtils.CONVERSATION_TAG);
                    if (image != null) {
                        ContentValues values = new ContentValues();
                        Log.e(TAG, "handleMessage: imageUrl = " + image.imageUrl
                                + ", thumb = " + image.thumbImageUrl);
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
                    notifyDataSetChanged();
                }
            }
        }
    };
}
