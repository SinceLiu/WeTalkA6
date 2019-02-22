package com.readboy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.readboy.dialog.RequestFriendView;
import com.readboy.provider.Profile;
import com.readboy.wetalk.BaseActivity;

/**
 * @author oubin
 * @TODO 需要解决正在显示过程中，又有新的好友请求怎么办，打开多个吗
 * @date 2018/12/29
 */
public class RequestFriendActivity extends BaseActivity {
    public static final String ACTION_REQUEST = "request";
    public static final int REQUEST_CODE = 0x12;
    public static final String EXTRA_PROFILE = "uuid";
    private RequestFriendView mParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        mParent = new RequestFriendView(this);
        setContentView(mParent);
        mParent.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        parseIntent(getIntent());
    }

    private void parseIntent(Intent intent) {
        Profile profile = intent.getParcelableExtra(EXTRA_PROFILE);
        mParent.setFriend(profile);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }
}
