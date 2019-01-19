package com.readboy.dialog;

import android.app.AlertDialog;
import android.app.readboy.IReadboyWearListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.readboy.wetalk.bean.Friend;
import com.readboy.utils.ToastUtils;
import com.readboy.utils.WearManagerProxy;
import com.readboy.wetalk.R;

/**
 * @author oubin
 * @date 2018/12/28
 */
public class AddFriendDialog extends AlertDialog implements View.OnClickListener {
    private static final String TAG = "oubin-AddFriendDialog";

    private Context mContext;
    private TextView mContentTv;
    private Friend mFriend;
    private Handler mHandler = new Handler();

    public AddFriendDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_friend);
        Log.i(TAG, "onCreate: ");
        mContentTv = findViewById(R.id.add_friend_content);
        if (mFriend != null) {
            mContentTv.setText(mFriend.name);
        }
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.confirm).setOnClickListener(this);
    }

    public void setFriend(Friend friend) {
        this.mFriend = friend;
        Log.e(TAG, "setFriend: " + friend.toString());
        if (mContentTv != null) {
            mContentTv.setText(mContext.getResources().getString(R.string.add_friend_content, friend.name));
        }
    }

    private void cancelClick(View view) {
        dismiss();
    }

    private void confirmClick(View view) {
        WearManagerProxy.requestAddFriend(mContext, mFriend.uuid, new IReadboyWearListener.Stub() {
            @Override
            public void pushSuc(String cmd, String serial, int code, String data, String result) throws RemoteException {
                Log.i(TAG, "pushSuc() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", data = " + data + ", result = " + result + "");
                Log.i(TAG, "pushSuc: Thread = " + Thread.currentThread().getName());
                mHandler.post(getToastRunnable("好友请求已发送" ));
            }

            @Override
            public void pushFail(String cmd, String serial, int code, String errorMsg) throws RemoteException {
                Log.e(TAG, "pushFail() called with: cmd = " + cmd + ", serial = " + serial + ", code = " + code + ", errorMsg = " + errorMsg + "");
                mHandler.post(getToastRunnable("发送失败：" + errorMsg));
            }
        });
        dismiss();
    }

    private Runnable getToastRunnable(String message) {
        return new Runnable() {
            @Override
            public void run() {
                ToastUtils.show(mContext, message);
                dismiss();
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                cancelClick(v);
                break;
            case R.id.confirm:
                confirmClick(v);
                break;
            default:
                break;
        }
    }
}
