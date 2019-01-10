package com.readboy.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by 1-PC on 2016/9/23.
 */

public class CircleImageView extends ImageView {
    private static final String TAG = "oubin_CircleImageView";

    public static final int ROUND = 0;
    public static final int CIRCLE = 1;

    private Paint paint;
    private int mWidth;
    private int mHeight;
    private int mRadius;

    //默认是圆形
    private int type = ROUND;

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged() called with: w = " + w + ", h = " + h + ", oldw = " + oldw + ", oldh = " + oldh + "");
        mWidth = w;
        mHeight = h;
        mRadius = 10;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //因此只对设置背景有效
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            canvas.drawBitmap(createRoundCornerImage(bitmap), 0, 0, null);
//            switch (type) {
//                case ROUND:
//                    b = getRoundBitmap(bitmap, 20);
//                    break;
//                case CIRCLE:
//                    b = getRoundBitmap(bitmap, 102);
//                    break;
//                default:
//                    b = getCircleBitmap(bitmap);
//                    break;
//            }
//            if (b != null) {
//                //绘制的范围
//                final Rect rectSrc = new Rect(0, 0, b.getWidth(), b.getWidth());
//                final Rect rectDest = new Rect(0, 0, getWidth(), getWidth());
//                paint.reset();
//                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//                canvas.drawBitmap(b, rectSrc, rectDest, paint);
//            }
        } else {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            super.onDraw(canvas);
        }
    }

    @Override
    public void setBackground(Drawable background) {
        super.setImageDrawable(background);
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setImageResource(resid);
    }

    /**
     * 获得圆形的Bitmap
     *
     * @param bitmap bitmap
     * @return 圆形bitmap
     */
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        //创建一个同大小的Bitmap
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight()
                , Bitmap.Config.ARGB_8888);
        //绘制Bitmap
        Canvas canvas = new Canvas(output);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        final int color = 0xff424242;
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int x = bitmap.getWidth();
        canvas.drawCircle(x / 2, x / 2, x / 2, paint);
        //显示重叠部分
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private Bitmap getRoundBitmap(Bitmap bitmap, int roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_4444);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int x = bitmap.getWidth();

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 依据原图加入圆角
     *
     * @param source
     * @return
     */
    private Bitmap createRoundCornerImage(Bitmap source) {
        Log.i(TAG, "createRoundCornerImage: width = " + mWidth + ", height = " + mHeight);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        float x = mWidth / source.getWidth();
        float y = mHeight / source.getHeight();
        Bitmap target = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rect = new RectF(0, 0, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.scale(x, y);
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

}
