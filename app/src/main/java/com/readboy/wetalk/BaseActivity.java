package com.readboy.wetalk;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by 1-PC on 2016/9/21.
 */
public abstract class BaseActivity extends Activity {

    public static final String TAG = "WeTalk";

    private int anim_in = R.anim.slide_in_right;
    private int anim_out = R.anim.slide_out_right;

    protected abstract void initView();

    protected abstract void initData();

    protected View getView(int id) {
        return findViewById(id);
    }

    protected void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
//        overridePendingTransition(anim_in, anim_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
//        overridePendingTransition(anim_in, anim_out);
    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(anim_in, anim_out);
    }
}
