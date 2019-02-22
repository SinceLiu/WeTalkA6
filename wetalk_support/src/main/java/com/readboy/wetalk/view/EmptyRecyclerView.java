package com.readboy.wetalk.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.readboy.recyclerview.wrapper.HeaderAndFooterWrapper;

/**
 * @author hwj
 */
public class EmptyRecyclerView extends RecyclerView {

    private View emptyView;
    private static final String TAG = "wetalk_RecyclerView";

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            Log.i(TAG, "onItemRangeInserted" + itemCount);
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
    }

    private void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            int emptyCount = 0;
            if (getAdapter() instanceof HeaderAndFooterWrapper) {
                HeaderAndFooterWrapper wrapper = (HeaderAndFooterWrapper) getAdapter();
                emptyCount = wrapper.getFootersCount() + wrapper.getHeadersCount();
            }
            final boolean emptyViewVisible =
                    getAdapter().getItemCount() == emptyCount;
            Log.i(TAG, "checkIfEmpty: ");
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
//        checkIfEmpty();
    }
}