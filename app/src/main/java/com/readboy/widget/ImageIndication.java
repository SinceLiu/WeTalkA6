package com.readboy.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.readboy.wetalk.R;

/**
 *
 * @author oubin
 * @date 2018/12/24
 */
public class ImageIndication extends LinearLayout {
    private static final String TAG = "ImageIndication";

    private int count = 2;
    private int padding = 6;
    private int[] resIds = {R.drawable.indicator_conversation_selector, R.drawable.indicator_group_selector};
    private ImageView leftImageView;
    private ImageView rightImageView;
    private ImageView[] imageViews = new ImageView[count];
    private ViewPager viewPager;
    private int mCurrentPosition;

    public ImageIndication(Context context) {
        this(context, null);
    }

    public ImageIndication(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageIndication(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            LayoutParams layoutParams = new LayoutParams(10, 10);
            if (i > 0) {
                layoutParams.leftMargin = padding;
            }
            imageView.setLayoutParams(layoutParams);
            imageView.setImageResource(resIds[i]);
            imageViews[i] = imageView;
            addView(imageView);
        }
        updateView();
    }

    public void setViewPager(ViewPager viewPager) {
        if (this.viewPager == viewPager) {
            return;
        }
        this.viewPager = viewPager;
        initListener();
    }

    private void initListener() {
        if (viewPager == null) {
            return;
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                Log.i(TAG, "onPageSelected() called with: i = " + i + "");
                int temp = mCurrentPosition;
                resolvePosition(i);
                if (temp != mCurrentPosition) {
                    updateView();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void resolvePosition(int position) {
        mCurrentPosition = Math.max(0, Math.min(position, count - 1));
    }

    private void updateView() {
        for (ImageView imageView : imageViews) {
            imageView.setSelected(false);
        }
        imageViews[mCurrentPosition].setSelected(true);
    }

}
