package com.readboy.wetalk;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * @author oubin
 * @date 2018/4/17
 */

public class TextDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "hwj_TextDialog";

    private TextView textView;
    private String text;

    public TextDialog(@NonNull Context context, String text) {
        super(context, R.style.FullScreenDialogTheme);
        this.text = text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message_full_screen);
        textView = (TextView) findViewById(R.id.message);
        textView.setOnClickListener(this);
        textView.setText(text);
        findViewById(R.id.space1).setOnClickListener(this);
        findViewById(R.id.space2).setOnClickListener(this);
//        adjustTextViewGravity();

    }

    /**
     * @see com.readboy.view.AlignTextView 实现该功能
     */
    private void adjustTextViewGravity() {
        textView.post(new Runnable() {
            @Override
            public void run() {
                int lineCount = textView.getLineCount();
                int gravity = textView.getGravity();
                Log.d(TAG, "run: line count = " + lineCount);
                if (lineCount > 1 && gravity != (Gravity.LEFT | Gravity.CENTER_VERTICAL)) {
                    textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                } else if (lineCount == 1 && (gravity != Gravity.CENTER)) {
                    textView.setGravity(Gravity.CENTER);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public void setText(String text) {
        textView.setText(text);
//        adjustTextViewGravity();
    }
}
