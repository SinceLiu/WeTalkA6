package com.readboy.dialog;

import android.app.readboy.IReadboyWearListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.readboy.bean.Constant;
import com.readboy.utils.NotificationUtils;
import com.readboy.wetalk.bean.Friend;
import com.readboy.provider.Profile;
import com.readboy.utils.ToastUtils;
import com.readboy.utils.WearManagerProxy;
import com.readboy.wetalk.ConversationActivity;
import com.readboy.wetalk.R;


/**
 * @author oubin
 * @date 2018/12/28
 */
public class RequestFriendView extends FrameLayout implements View.OnClickListener, DialogInterface {
    private static final String TAG = "oubin-RequestFriend";

    private TextView mContentTv;
    private View mProgressBar;

    private Profile mFriend;
    private Context mContext;
    private Handler mHandler = new Handler();
    private OnDismissListener mDismissListener;

    public RequestFriendView(Context context) {
        this(context, null);
    }

    public RequestFriendView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RequestFriendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.dialog_request_friend, this);
        setBackgroundColor(Color.BLACK);
        findViewById(R.id.request_friend_refuse).setOnClickListener(this);
        findViewById(R.id.request_friend_agree).setOnClickListener(this);
        mContentTv = findViewById(R.id.request_friend_content);
        mProgressBar = findViewById(R.id.progress_bar);
        if (!WearManagerProxy.hadPhoneNumber(mContext)) {
            Log.i(TAG, "RequestFriendView: no have phone number.");
            ViewStub viewStub = findViewById(R.id.stub_no_phone_number);
            viewStub.inflate();
            View button = findViewById(R.id.dialog_no_phone_number_confirm);
            button.setOnClickListener(v -> handleDismissEvent());
        }
    }

    public void setFriend(Profile friend) {
        this.mFriend = friend;
        mContentTv.setText(getResources().getString(R.string.request_friend_content, friend.getName()));
    }

    @Override
    public void onClick(View v) {
        if (mProgressBar.getVisibility() == VISIBLE) {
            return;
        }
        switch (v.getId()) {
            case R.id.request_friend_refuse:
                dismiss();
                NotificationUtils.cancelNotification(mContext, mFriend.uuid.hashCode());
                break;
            case R.id.request_friend_agree:
                agreeClick();
                NotificationUtils.cancelNotification(mContext, mFriend.uuid.hashCode());
                break;
            default:
                break;
        }
    }

    private void agreeClick() {
        mProgressBar.setVisibility(VISIBLE);
        WearManagerProxy.addFriend(mContext, mFriend.uuid, new IReadboyWearListener.Stub() {
            @Override
            public void pushSuc(String cmd, String serial, int code, String data, String result) {
                Log.e(TAG, "addFriend pushSuc() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", data = " + data + ", result = " + result + "");
                mHandler.post(() -> gotoConversationActivity());
            }

            @Override
            public void pushFail(String cmd, String serial, int code, String errorMsg) {
                Log.e(TAG, "pushFail() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", errorMsg = " + errorMsg + "");
                mHandler.post(() -> {
                    ToastUtils.show(mContext, errorMsg);
                    dismiss();
                });
            }
        });
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mDismissListener = listener;
    }

    private void handleDismissEvent() {
        if (mDismissListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDismissListener.onDismiss(RequestFriendView.this);
                }
            });
        }
    }

    @Override
    public void cancel() {
        dismiss();
    }

    @Override
    public void dismiss() {
//        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//        windowManager.removeView(this);
        handleDismissEvent();
    }

    private void gotoConversationActivity() {
        Intent intent = new Intent(mContext, ConversationActivity.class);
        Friend friend = new Friend();
        friend.uuid = mFriend.uuid;
        friend.name = mFriend.getName();
        intent.putExtra(Constant.EXTRA_FRIEND, friend);
        mProgressBar.setVisibility(GONE);
        // startActivity();
        dismiss();
    }

    public static RequestFriendView addWindow(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        RequestFriendView dialog = new RequestFriendView(context);
        Log.i(TAG, "addAlertView: windowManager addView.");
        windowManager.addView(dialog, layoutParams);
        return dialog;
    }

}
