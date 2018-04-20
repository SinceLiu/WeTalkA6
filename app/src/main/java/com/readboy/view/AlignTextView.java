package com.readboy.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.readboy.wetalk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 两端对齐的text view，可以设置最后一行靠左，靠右，居中对齐
 * 20180420 修改加上单行这居中显示
 *
 * @author YD
 */
public class AlignTextView extends TextView {
    private static final String TAG = "hwj_AlignTextView";

    // 单行文字高度
    private float textLineSpaceExtra = 0;
    // 额外的行间距
    private float textHeight;
    private int width;
    // 分割后的行
    private List<String> lines = new ArrayList<String>();
    // 尾行
    private List<Integer> tailLines = new ArrayList<Integer>();
    // 默认最后一行左对齐
    private Align align = Align.ALIGN_LEFT;
    // 初始化计算,是否需要重新计算
    private boolean firstCalc = true;

    private float lineSpacingMultiplier = 1.0f;
    private float lineSpacingAdd = 0.0f;

    //原始高度
    private int originalHeight = 0;
    //原始行数
    private int originalLineCount = 0;
    //原始paddingBottom
    private int originalPaddingBottom = 0;
    private boolean setPaddingFromMe = false;

    /**
     * 尾行对齐方式，针对段落最后一行。
     * 当只有一行时，居中显示，有特殊处理。
     */
    public enum Align {
        ALIGN_LEFT,
        ALIGN_CENTER,
        ALIGN_RIGHT
    }

    public AlignTextView(Context context) {
        super(context);
        setTextIsSelectable(false);
    }

    public AlignTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextIsSelectable(false);

        int[] attributes = new int[]{android.R.attr.lineSpacingExtra, android.R.attr.lineSpacingMultiplier};
        TypedArray arr = context.obtainStyledAttributes(attrs, attributes);
        lineSpacingAdd = arr.getDimensionPixelSize(0, 0);
        lineSpacingMultiplier = arr.getFloat(1, 1.0f);
        originalPaddingBottom = getPaddingBottom();
        arr.recycle();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AlignTextView);

        int alignStyle = ta.getInt(R.styleable.AlignTextView_align, 0);
        switch (alignStyle) {
            case 1:
                align = Align.ALIGN_CENTER;
                break;
            case 2:
                align = Align.ALIGN_RIGHT;
                break;
            default:
                align = Align.ALIGN_LEFT;
                break;
        }

        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure: ");
//        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e(TAG, "onLayout() called with: changed = " + changed + ", left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom + "");

        //首先进行高度调整
        if (firstCalc) {
            width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            String text = getText().toString();
            TextPaint paint = getPaint();
            lines.clear();
            tailLines.clear();

            // 文本含有换行符时，分割单独处理
            String[] items = text.split("\\n");
            for (String item : items) {
                calc(paint, item);
            }

            //使用替代textview计算原始高度与行数
            measureTextViewHeight(text, paint.getTextSize(), getMeasuredWidth() -
                    getPaddingLeft() - getPaddingRight());

            //获取行高
            textHeight = 1.0f * originalHeight / originalLineCount;

            textLineSpaceExtra = textHeight * (lineSpacingMultiplier - 1) + lineSpacingAdd;

            //计算实际高度,加上多出的行的高度(一般是减少)
            int heightGap = (int) ((textLineSpaceExtra + textHeight) * (lines.size() -
                    originalLineCount));

            setPaddingFromMe = true;
            //调整textview的paddingBottom来缩小底部空白
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(),
                    originalPaddingBottom + heightGap);

            firstCalc = false;
            LinearLayout.LayoutParams layoutParams;
            Log.e(TAG, "onLayout: width = " + width + ", padding = " + getPaddingLeft());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();

        width = getMeasuredWidth();

        Paint.FontMetrics fm = paint.getFontMetrics();
        float firstHeight = getTextSize() - (fm.bottom - fm.descent + fm.ascent - fm.top);

        int gravity = getGravity();
        // 是否垂直居中
        if ((gravity & 0x1000) == 0) {
            firstHeight = firstHeight + (textHeight - firstHeight) / 2;
        }

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        width = width - paddingLeft - paddingRight;

        int size = lines.size();
        for (int i = 0; i < size; i++) {
            float drawY = i * textHeight + firstHeight;
            String line = lines.get(i);
            // 绘画起始x坐标
            float drawSpacingX = paddingLeft;
            float gap = (width - paint.measureText(line));
            float interval = gap / (line.length() - 1);

            // 绘制最后一行
            //20180420 加上当只有一行时居中显示，两端不对齐。
            if (size == 1) {
                Log.e(TAG, "onDraw: is one line.");
                interval = 0;
                drawSpacingX += gap / 2;
            } else if (tailLines.contains(i)) {
                interval = 0;
                if (align == Align.ALIGN_CENTER) {
                    drawSpacingX += gap / 2;
                } else if (align == Align.ALIGN_RIGHT) {
                    drawSpacingX += gap;
                }
            }

            for (int j = 0; j < line.length(); j++) {
                float drawX = paint.measureText(line.substring(0, j)) + interval * j;
                canvas.drawText(line.substring(j, j + 1), drawX + drawSpacingX, drawY +
                        paddingTop + textLineSpaceExtra * i, paint);
            }
        }
    }

    /**
     * 设置尾行对齐方式
     *
     * @param align 对齐方式
     */
    public void setAlign(Align align) {
        this.align = align;
        invalidate();
    }

    /**
     * 计算每行应显示的文本数
     *
     * @param text 要计算的文本
     */
    private void calc(Paint paint, String text) {
        if (text.length() == 0) {
            lines.add("\n");
            return;
        }
        // 起始位置
        int startPosition = 0;
        float oneChineseWidth = paint.measureText("中");
        Log.e(TAG, "calc: oneChineseWidth = " + oneChineseWidth);
        // 忽略计算的长度
        int ignoreCalcLength = (int) (width / oneChineseWidth);
        StringBuilder sb = new StringBuilder(text.substring(0, Math.min(ignoreCalcLength + 1,
                text.length())));

        for (int i = ignoreCalcLength + 1; i < text.length(); i++) {
            if (paint.measureText(text.substring(startPosition, i + 1)) > width) {
                startPosition = i;
                //将之前的字符串加入列表中
                lines.add(sb.toString());

                sb = new StringBuilder();

                //添加开始忽略的字符串，长度不足的话直接结束,否则继续
                if ((text.length() - startPosition) > ignoreCalcLength) {
                    sb.append(text.substring(startPosition, startPosition + ignoreCalcLength));
                } else {
                    lines.add(text.substring(startPosition));
                    break;
                }

                i = i + ignoreCalcLength - 1;
            } else {
                sb.append(text.charAt(i));
            }
        }
        if (sb.length() > 0) {
            lines.add(sb.toString());
        }

        tailLines.add(lines.size() - 1);
        Log.e(TAG, "calc: lines.size = " + lines.size());
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        firstCalc = true;
        super.setText(text, type);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (!setPaddingFromMe) {
            originalPaddingBottom = bottom;
        }
        setPaddingFromMe = false;
        super.setPadding(left, top, right, bottom);
    }


    /**
     * 获取文本实际所占高度，辅助用于计算行高,行数
     *
     * @param text        文本
     * @param textSize    字体大小
     * @param deviceWidth 屏幕宽度
     */
    private void measureTextViewHeight(String text, float textSize, int deviceWidth) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(deviceWidth, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        originalLineCount = textView.getLineCount();
        originalHeight = textView.getMeasuredHeight();
        Log.e(TAG, "measureTextViewHeight: originalLine = " + originalLineCount + ", height = " + originalHeight);
    }
}