package com.readboy.wetalk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.readboy.utils.ToastUtils;

/**
 * Created by oubin on 2016/9/21.
 *
 * @author oubin
 * @date 2016/9/21
 */
public abstract class BaseActivity extends Activity {

    public static final String TAG = "hwj_WeTalk";
    private int animIn = R.anim.slide_in_right;
    private int animOut = R.anim.slide_out_right;

    private TrafficDialog mTrafficDialog;

    protected abstract void initView();

    protected abstract void initData();

    protected View getView(int id) {
        return findViewById(id);
    }

    protected void showMsg(String msg) {
        Log.d(TAG, "showMsg() called with: msg = " + msg + "");
        ToastUtils.show(this, msg);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
//        overridePendingTransition(animIn, animOut);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
//        overridePendingTransition(animIn, animOut);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(animIn, animOut);
    }

    private void showTrafficDialog() {
        if (mTrafficDialog == null) {
            mTrafficDialog = new TrafficDialog(this);
        }
        mTrafficDialog.show();
    }

}
