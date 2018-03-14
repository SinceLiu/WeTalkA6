package com.readboy.wetalk;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

/**
 * @author oubin
 * @date 2018/2/5
 */

public class TrafficDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "hwj_TrafficDialog";

    private onExitListener mOnExitListener;

    public TrafficDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_traffic);
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick: v ");
        switch (v.getId()) {
            case R.id.exit:
                exit();
                break;
            case R.id.goto_wifi_setting:
                gotoSetting();
                break;
            default:
                break;
        }
    }

    private void exit() {
        if (mOnExitListener != null){
            mOnExitListener.onExit();
        }
    }

    private void gotoSetting() {
        getContext().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public interface onExitListener {
        /**
         * 取消按钮，点击事件回调
         */
        void onExit();
    }

}
