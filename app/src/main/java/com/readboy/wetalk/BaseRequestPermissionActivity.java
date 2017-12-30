package com.readboy.wetalk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

/**
 * Created by hwj on 2017/3/21.
 *
 * @author hwj
 */

public abstract class BaseRequestPermissionActivity extends Activity {

    private static final int REQUEST_CODE = 0x213;

    /**
     * 初始化数据
     */
    protected abstract void initContent();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = REQUEST_PERMISSIONS;
            if (permissions != null) {
                boolean hasPermission = true;
                for (String permission : permissions) {
                    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        hasPermission = false;
                        break;
                    }
                }
                if (!hasPermission) {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
                } else {
                    initContent();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            boolean hasPermission = true;
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasPermission = false;
                    break;
                }
            }
            if (hasPermission) {
                initContent();
            } else {
                Toast.makeText(this, getString(R.string.missing_required_permission), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private int anim_in = R.anim.slide_in_right;
    private int state = R.anim.state;
    private int anim_out = R.anim.slide_out_right;

    private static final String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_LOGS,
            Manifest.permission.ACCESS_WIFI_STATE};

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
//        overridePendingTransition(anim_in,anim_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
//        overridePendingTransition(anim_in,anim_out);
    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(anim_in,anim_out);
    }
}
