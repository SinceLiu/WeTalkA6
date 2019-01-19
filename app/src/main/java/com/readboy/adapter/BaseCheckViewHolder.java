package com.readboy.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * @author oubin
 * @date 2017/9/29
 */

public abstract class BaseCheckViewHolder<D> extends BaseViewHolder<D> {

    CheckBox mCheckBox;

    BaseCheckViewHolder(View itemView) {
        super(itemView);
        mCheckBox = (CheckBox) itemView.findViewById(getCheckBoxId());
        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCheckedChangeEvent();
            }
        });
    }

    public void bindView(int position, boolean isChecked, D d) {
        super.bindView(position, d);
        mCheckBox.setTag(position);
        mCheckBox.setChecked(isChecked);
    }

    /**
     * checkBox res id.
     *
     * @return id
     */
    abstract int getCheckBoxId();

    /**
     * Adapter.onCreateViewHolder必须回调，事件回传给Adapter
     * @deprecated use {@link #setOnCheckedChangeListener(OnCheckedChangeListener)}
     * @param listener mCheckBox点击事件
     */
    public void setCheckOnClickListener(View.OnClickListener listener) {
        mCheckBox.setOnClickListener(listener);
    }

    private OnCheckedChangeListener mCheckedListener;

    /**
     * 最好Adapter.onCreateViewHolder调用，把事件传给Adapter.
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mCheckedListener = listener;
    }

    protected void handleCheckedChangeEvent() {
        handleCheckedChangeEvent(mCheckBox, mCheckBox.isChecked());
    }

    protected void handleCheckedChangeEvent(CompoundButton button, boolean isChecked) {
        if (mCheckedListener != null) {
            mCheckedListener.onCheckedChanged(button, isChecked);
        }
    }

    public interface OnCheckedChangeListener {
        /**
         * checked状态改变
         *
         * @param buttonView mCheckedBox
         * @param isChecked  checked
         */
        void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }
}
