package com.readboy.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.readboy.wetalk.R;

/**
 *
 * @author oubin
 * @date 2017/7/17
 */

public class DragFrameLayout extends FrameLayout {
    private static final String TAG = "DragFrameLayout";

    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    private static final float DISMISS_MIN_RATIO = 0.3F;
    private static final float MIN_ALPHA = 0.3F;

    private Point mOriginPoint = new Point();
    private float maxTranslationOffset = 0;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mMinDismissDistance;

    private ViewDragHelper mViewDragHelper;
    private DecelerateInterpolator mInterpolator = new DecelerateInterpolator();
    private ViewPager mViewPager;
    private int mOrientation = HORIZONTAL;

    public DragFrameLayout(Context context) {
        this(context, null);
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragFrameLayout);
        mOrientation = typedArray.getInt(R.styleable.DragFrameLayout_dragOrientation, HORIZONTAL);
        typedArray.recycle();
        mViewDragHelper = ViewDragHelper.create(this, 1, mCallback);
        if (mOrientation == HORIZONTAL) {
            mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        } else if (mOrientation == VERTICAL) {
            mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        maxTranslationOffset = (float) (w * 0.15);
        mMinDismissDistance = (int) (w * DISMISS_MIN_RATIO);
        View view = getChildAt(0);
        if (view != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
            mOriginPoint.x = view.getLeft() + layoutParams.leftMargin;
            mOriginPoint.y = view.getTop() + layoutParams.topMargin;
        }
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i(TAG, "onInterceptTouchEvent");
        if (mViewDragHelper.shouldInterceptTouchEvent(ev)) {
            if (mViewPager != null && mViewPager.getCurrentItem() > 0) {
                return false;
            }
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouchEvent");
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.e(TAG, "tryCaptureView: requestDisallow true");
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.i(TAG, "clampViewPositionHorizontal: left = " + left + ", dx = " + dx + ", orientation = " + mOrientation);
            if (mOrientation == VERTICAL) {
                return child.getLeft();
            }
            if (left >= 0) {
                if (mViewPager != null && mViewPager.getCurrentItem() > 0) {
                    return left;
                }
                float alpha = Math.abs(left) * (MIN_ALPHA - 1) / mWidth + 1;
                child.setAlpha(alpha);
                return left;
            } else {
                return child.getLeft();
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            Log.i(TAG, "clampViewPositionVertical: top = " + top + ", dy = " + dy + "orientation = " + mOrientation);
            if (mOrientation == HORIZONTAL) {
                return child.getTop();
            }
            if (top <= 0) {
                float alpha = Math.abs(top) * (MIN_ALPHA - 1) / mHeight + 1;
                child.setAlpha(alpha);
                return top;
            } else {
                return child.getTop();
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            if (mOrientation == VERTICAL) {
                return 1;
            }
            return 0;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            if (mOrientation == HORIZONTAL) {
                return 1;
            }
            return 0;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            Log.i(TAG, "onViewReleased: xvel = " + xvel + ", yvel = " + yvel);
            Log.i(TAG, "onViewReleased: left = " + releasedChild.getLeft() + ", top = " + releasedChild.getTop());
            if (mOrientation == HORIZONTAL) {
                if (releasedChild.getLeft() > mMinDismissDistance) {
                    handlerDismissEvent();
                } else {
                    Log.i(TAG, "onViewReleased: x = " + mOriginPoint.x + ", top = " + releasedChild.getTop());
                    mViewDragHelper.settleCapturedViewAt(mOriginPoint.x, releasedChild.getTop());
                    releasedChild.setAlpha(1.0F);
                    invalidate();
                }
            } else if (mOrientation == VERTICAL) {
                if ((mHeight - releasedChild.getBottom()) > mMinDismissDistance) {
                    handlerDismissEvent();
                } else {
                    mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), mOriginPoint.y);
                    releasedChild.setAlpha(1.0F);
                    invalidate();
                }
            }
        }
    };

    protected void handlerDismissEvent() {
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    private OnDismissListener mDismissListener;

    public void setOnDismissListener(OnDismissListener listener) {
        this.mDismissListener = listener;
    }

    public interface OnDismissListener {
        /**
         * 右滑退出回调
         */
        void onDismiss();
    }
}
