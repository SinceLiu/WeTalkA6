package com.readboy.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by 1-PC on 2016/9/28.
 * 发送图片项
 */

public class SendDialogImageView extends ImageView{

    private Paint paint;

    public SendDialogImageView(Context context) {
        super(context);
    }

    public SendDialogImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SendDialogImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if(drawable != null){
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap b = getDialogBitmap(bitmap);
            final Rect rectSrc = new Rect(0,0,b.getWidth(),b.getHeight());
            final Rect rectDest = new Rect(0,0,getWidth(),getHeight());
            paint.reset();
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.drawBitmap(b,rectSrc,rectDest,paint);
        } else{
        	canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            super.onDraw(canvas);
        }
    }

    private Bitmap getDialogBitmap(Bitmap bitmap){
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight()
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        final int color = 0xff424242;
        final Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Path path = new Path();

        path.moveTo(w,h / 2);
        path.lineTo(w - 10,h / 2 - 15);
        path.lineTo(w - 10,h / 2 + 15);
        path.close();
        RectF rectF = new RectF(0,0,w - 10,h);
        canvas.drawRoundRect(rectF, 10, 10, paint);
        canvas.drawPath(path, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        return output;
    }
}
