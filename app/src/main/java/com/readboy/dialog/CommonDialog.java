package com.readboy.dialog;

import android.app.Dialog;
import android.app.readboy.IReadboyWearListener;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.readboy.wetalk.bean.Friend;
import com.readboy.utils.WearManagerProxy;
import com.readboy.wetalk.R;

import java.util.Optional;

/**
 * @author oubin
 * @date 2018/12/28
 */
public class CommonDialog extends Dialog {
    private static final String TAG = "hwj_CommonDialog";

    private Context mContext;
    private TextView mContentTv;
    private TextView mLeftButton;
    private TextView mRightButton;
    private String mContent;
    private View.OnClickListener mLeftButtonListener;
    private View.OnClickListener mRightButtonListener;

    private CommonDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    private CommonDialog(Context context, Builder builder) {
        this(context);
        this.mContent = builder.mContent;
        this.mLeftButtonListener = builder.mLeftButtonListener;
        this.mRightButtonListener = builder.mRightButtonListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        setContentView(R.layout.dialog_common);
        mContentTv = findViewById(R.id.dialog_content);
        if (mContent != null) {
            mContentTv.setText(mContent);
        }
        mLeftButton = findViewById(R.id.dialog_left_button);
        if (mLeftButtonListener != null) {
            mLeftButton.setOnClickListener(mLeftButtonListener);
        }
        mRightButton = findViewById(R.id.dialog_right_button);
        if (mRightButtonListener != null) {
            mRightButton.setOnClickListener(mRightButtonListener);
        } else {
            mRightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    @Override
    public void show() {
        super.show();
        Log.i(TAG, "show: ");
    }

    /**
     * 有可能View，
     */
    public void setContent(String content) {
        mContentTv.setText(content);
    }

    public void setLeftButton(String text, View.OnClickListener listener) {
        if (text != null) {
            mLeftButton.setText(text);
        }
        mLeftButton.setOnClickListener(listener);
    }

    public void setRightButton(String text, View.OnClickListener listener) {
        if (text != null) {
            mRightButton.setText(text);
        }
        mLeftButton.setOnClickListener(listener);
    }

    public static class Builder {
        private String mContent;
        private View.OnClickListener mLeftButtonListener;
        private View.OnClickListener mRightButtonListener;

        public Builder content(String mContent) {
            this.mContent = mContent;
            return this;
        }

        public Builder leftButtonListener(View.OnClickListener mLeftButtonListener) {
            this.mLeftButtonListener = mLeftButtonListener;
            return this;
        }

        public Builder rightButtonListener(View.OnClickListener mRightButtonListener) {
            this.mRightButtonListener = mRightButtonListener;
            return this;
        }

        public Builder fromPrototype(CommonDialog prototype) {
            mContent = prototype.mContent;
            mLeftButtonListener = prototype.mLeftButtonListener;
            mRightButtonListener = prototype.mRightButtonListener;
            return this;
        }

        public CommonDialog build(Context context) {
            return new CommonDialog(context, this);
        }
    }
}
