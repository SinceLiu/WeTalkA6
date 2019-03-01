package com.readboy.wetalk.support;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.readboy.wetalk.view.WetalkFrameLayout;


/**
 * @author oubin
 * @date 2019/2/17
 */
public class WetalkFragment extends Fragment {
    private static final String TAG = "WetalkFragment";
    public static final String KEY_HAD_TITLE = "title";

    private WetalkFrameLayout mParent;
    private boolean hadTitle = true;

    public static WetalkFragment newInstance() {
        return newInstance(true);
    }

    public static WetalkFragment newInstance(boolean hadTitle) {
        WetalkFragment fragment = new WetalkFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_HAD_TITLE, hadTitle);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: " + getArguments());
        if (getArguments() != null) {
            hadTitle = getArguments().getBoolean(KEY_HAD_TITLE, hadTitle);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mParent = (WetalkFrameLayout) inflater.inflate(R.layout.fragment_wetalk, container, false);
        mParent.setActivity(getActivity());
        if (hadTitle) {
            mParent.addHeader(R.layout.item_title);
        }
        return mParent;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mParent != null) {
            mParent.onDestroy();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged() called with: hidden = " + hidden + "");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mParent != null) {
                mParent.onResume();
            }
        } else {
            if (mParent != null) {
                mParent.onPause();
            }
        }
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        Log.i(TAG, "setArguments: ");
    }

    public RecyclerView getRecyclerView() {
        return mParent == null ? null : mParent.getRecyclerView();
    }

}
