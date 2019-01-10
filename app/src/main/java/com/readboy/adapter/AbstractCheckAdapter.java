package com.readboy.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author oubin
 * @date 2019/1/3
 * 或者使用Adapter.getItemCount(), 是否可靠，发生的先后顺序，待测试。
 */

public abstract class AbstractCheckAdapter<D, VH extends AbstractCheckAdapter.AbstractCheckViewHolder<D>> extends BaseAdapter<D, VH> implements
        View.OnClickListener {
    private static final String TAG = "DownloadCheckAdapter";

    /**
     * 是不是效率更高，不用拆箱，装箱
     */
    private boolean[] mSelectedArray;
    //    private SparseBooleanArray mSelectedSparse = new SparseBooleanArray();
    private boolean isAllChecked = false;
    private final RecyclerView.AdapterDataObserver mObserver;
    private boolean isUpdateCheckBox = false;

    public AbstractCheckAdapter(Context context) {
        super(context);
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.e(TAG, "onChanged: isUpdateCheckBox = " + isUpdateCheckBox);
                if (!isUpdateCheckBox) {
                    initSelectedArray();
                }
                isUpdateCheckBox = false;
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                Log.e(TAG, "onItemRangeInserted() called with: positionStart = " + positionStart
                        + ", itemCount = " + itemCount + "");
                initSelectedArray();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                Log.e(TAG, "onItemRangeRemoved() called with: positionStart = " + positionStart
                        + ", itemCount = " + itemCount + "");
                initSelectedArray();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                Log.e(TAG, "onItemRangeChanged() called with: positionStart = " + positionStart + ", itemCount = " + itemCount + "");
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                Log.e(TAG, "onItemRangeChanged() called with: positionStart = " + positionStart + ", itemCount = " + itemCount + ", payload = " + payload + "");
            }
        };
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bindView(position, mSelectedArray[position], mDataList.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        registerAdapterDataObserver(mObserver);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        unregisterAdapterDataObserver(mObserver);
    }


    @Override
    public void update(List<D> list) {
        super.update(list);
        initSelectedArray();
    }

    @Override
    public void setData(List<D> list) {
        super.setData(list);
        initSelectedArray();
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        updateCheckedState(position, ((CompoundButton) v).isChecked());
        handlerAllCheckedChange(isAllChecked());
    }

    private void initSelectedArray() {
        mSelectedArray = new boolean[getItemCount()];
        handlerAllCheckedChange(false);
    }

    private void updateCheckedState(int position, boolean isChecked) {
        if (0 <= position && position < mSelectedArray.length) {
            mSelectedArray[position] = isChecked;
//            mSelectedSparse.put(position, isChecked);
        }
        handleCheckedChange(position, isChecked);
    }

    /**
     * 调用该方法后，切记不可外部调用notifyDataSetChanged().
     * 会导致全部为非选择状态。
     * {@link #mObserver}
     */
    public void setAllChecked(boolean checked) {
        isAllChecked = checked;
        if (mSelectedArray == null) {
            mSelectedArray = new boolean[getItemCount()];
        }
        int size = mSelectedArray.length;
        for (int i = 0; i < size; i++) {
            updateCheckedState(i, checked);
        }
        isUpdateCheckBox = true;
        notifyDataSetChanged();
    }

    public void setAllChecked() {
        setAllChecked(!isAllChecked());
    }

    public boolean hasChecked() {
        for (boolean b : mSelectedArray) {
            if (b) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllChecked() {
        if (mSelectedArray == null || mSelectedArray.length == 0) {
            return false;
        }
        Log.e(TAG, "isAllChecked: size = " + mSelectedArray.length);
        for (boolean b : mSelectedArray) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public boolean[] getSelectedArray() {
        return Arrays.copyOf(mSelectedArray, mSelectedArray.length);
    }

    public List<Integer> getSelectedPosition() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0, length = mSelectedArray.length; i < length; i++) {
            if (mSelectedArray[i]) {
                positions.add(i);
            }
        }
        return positions;
    }

    private OnCheckedChangeListener mCheckedListener;

    private void handleCheckedChange(int position, boolean isChecked) {
        Log.i(TAG, "handleCheckedChange() called with: position = " + position + ", isChecked = " + isChecked + "");
        if (mCheckedListener != null) {
            mCheckedListener.onChecked(position, isChecked);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mCheckedListener = listener;
    }

    public interface OnCheckedChangeListener {
        /**
         * 每个位置check变化回调
         *
         * @param position 位置
         * @param checked  checked值
         */
        void onChecked(int position, boolean checked);
    }

    private OnAllCheckedChangeListener mAllCheckedListener;

    private void handlerAllCheckedChange(boolean isChecked) {
        if (mAllCheckedListener != null && isChecked != isAllChecked) {
            mAllCheckedListener.onAllChecked(isChecked);
            isAllChecked = isChecked;
        }
    }

    public void setAllCheckedChangeListener(OnAllCheckedChangeListener listener) {
        this.mAllCheckedListener = listener;
    }

    public interface OnAllCheckedChangeListener {
        /**
         * 全选，或者非全选
         *
         * @param isChecked 是否全选。
         */
        void onAllChecked(boolean isChecked);
    }

    public static abstract class AbstractCheckViewHolder<T> extends BaseViewHolder<T>{

        protected CheckBox mCheckBox;

        public AbstractCheckViewHolder(View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(getCheckBarId());
        }

        @Override
        public void bindView(int position, T data) {
            super.bindView(position, data);
        }

        public void bindView(int position, boolean isChecked, T d) {
            super.bindView(position, d);
            mCheckBox.setTag(position);
            mCheckBox.setChecked(isChecked);
        }

        /**
         * checkBox res id.
         * @return id
         */
        protected abstract int getCheckBarId();
    }

}
