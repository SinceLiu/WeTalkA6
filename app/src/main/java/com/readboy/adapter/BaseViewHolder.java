package com.readboy.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 *
 * @author oubin
 * @date 2017/9/8
 */

public class BaseViewHolder<E> extends RecyclerView.ViewHolder {

    protected E mData;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void bindView(E data) {
    }

    public void bindView(int position, E data){
        bindView(data);
        this.mData = data;
    }

    public void recycler() {

    }

    protected OnInnerClickListener mInnerClickListener;

    /**
     * 必须在onCreateViewHolder调用该方法
     * @param listener BaseAdapter;
     */
    public void setOnInnerClickListener(OnInnerClickListener listener) {
        this.mInnerClickListener = listener;
    }

    protected void handlerInnerItemClickEvent() {
        if (mInnerClickListener != null) {
            mInnerClickListener.onInnerClick(getAdapterPosition(), this);
        }
    }

    /**
     * 用于反馈给Adapter的OnItemClickListener事件
     */
    public interface OnInnerClickListener {
        /**
         * ViewHolder调用，用户外部监听itemOnClick.
         * @param position 位置
         * @param holder viewHolder
         */
        void onInnerClick(int position, BaseViewHolder holder);
    }

}
