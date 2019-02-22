package com.readboy.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.readboy.wetalk.R;

/**
 * @author oubin
 * @date 2019/2/18
 */
public class NoPhoneNumDialog extends AlertDialog {

    public NoPhoneNumDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_no_phone_number);
        findViewById(R.id.dialog_no_phone_number_confirm).setOnClickListener(v -> dismiss());
    }
}
