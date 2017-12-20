package com.cncoderx.wheelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cncoderx
 */
public class WheelView extends View {
    private boolean mCyclic;
    private int mVisibleItems = 9;
    private int mLineSpace = 10;
    private int mTextSize = 20;
    private int mSelectedColor;
    private int mUnselectedColor;
    private Drawable mDividerTop;
    private Drawable mDividerBottom;

    int centerX;
    int centerY;
    int upperLimit;
    int lowerLimit;
    int baseline;
    int itemWidth;
    int itemHeight;

    Paint mPaint;
    WheelScroller mScroller;

    final List<CharSequence> mEntries = new ArrayList<>();

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, 0, R.style.WheelView);
        boolean cyclic = a.getBoolean(R.styleable.WheelView_cyclic, false);
        int visibleItems = a.getInt(R.styleable.WheelView_visibleItems, mVisibleItems);
        int lineSpace = a.getDimensionPixelOffset(R.styleable.WheelView_lineSpace, mLineSpace);
        int textSize = a.getDimensionPixelSize(R.styleable.WheelView_textSize, mTextSize);
        int selectedColor = a.getColor(R.styleable.WheelView_selectedColor, 0);
        int unselectedColor = a.getColor(R.styleable.WheelView_unselectedColor, 0);
        mDividerTop = a.getDrawable(R.styleable.WheelView_divider);
        mDividerBottom = a.getDrawable(R.styleable.WheelView_divider);
        CharSequence[] entries = a.getTextArray(R.styleable.WheelView_entries);
        a.recycle();

        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mScroller = new WheelScroller(context, this);

        setCyclic(cyclic);
        setVisibleItems(visibleItems);
        setLineSpace(lineSpace);
        setTextSize(textSize);
        setSelectedColor(selectedColor);
        setUnselectedColor(unselectedColor);
        setEntries(entries);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.EXACTLY
                && heightSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
            mVisibleItems = getPrefVisibleItems();
        } else if (widthSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSpecSize, getPrefHeight());
        } else if (heightSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(getPrefWidth(), heightSpecSize);
            mVisibleItems = getPrefVisibleItems();
        } else {
            setMeasuredDimension(getPrefWidth(), getPrefHeight());
        }

        centerX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2;
        centerY = (getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 计算上方分割线的高度
        upperLimit = centerY - itemHeight / 2;
        // 计算下方分割线的高度
        lowerLimit = centerY + itemHeight / 2;

        if (mDividerTop != null) {
            int h = mDividerTop.getIntrinsicHeight();
            mDividerTop.setBounds(getPaddingLeft(), upperLimit,
                    getWidth() - getPaddingRight(), upperLimit + h);

        }
        if (mDividerBottom != null) {
            int h = mDividerBottom.getIntrinsicHeight();
            mDividerBottom.setBounds(getPaddingLeft(), lowerLimit - h,
                    getWidth() - getPaddingRight(), lowerLimit);
        }
    }

    /**
     * @return 控件的预算宽度
     */
    public int getPrefWidth() {
        int padding = getPaddingLeft() + getPaddingRight();
        int innerWidth = (int) (itemWidth + mTextSize * .5f);
        return innerWidth + padding;
    }

    /**
     * @return 控件的预算高度
     */
    public int getPrefHeight() {
        int padding = getPaddingTop() + getPaddingBottom();
        int innerHeight = itemHeight * mVisibleItems;
        return innerHeight + padding;
    }

    /**
     * @return 可见项的预算数量
     */
    public int getPrefVisibleItems() {
        int innerHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        return innerHeight / itemHeight;
    }

    void measureItemSize() {
        int width = 0;
        for (CharSequence cs : mEntries) {
            float w = Layout.getDesiredWidth(cs, (TextPaint) mPaint);
            width = Math.max(width, Math.round(w));
        }
        int height = Math.round(mPaint.getFontMetricsInt(null) + mLineSpace);
        itemWidth = width;
        if (itemHeight != height) {
            // 每一项的高度改变时，需要重新计算滚动偏移量；
//            mScroller.setCurrentIndex(mScroller.getCurrentIndex(), false);
            itemHeight = height;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int index = mScroller.getItemIndex();
        final int offset = mScroller.getItemOffset();
        final int hf = (mVisibleItems + 1) / 2;
        final int minIdx, maxIdx;
        if (offset < 0) {
            minIdx = index - hf - 1;
            maxIdx = index + hf;
        } else if (offset > 0) {
            minIdx = index - hf;
            maxIdx = index + hf + 1;
        } else {
            minIdx = index - hf;
            maxIdx = index + hf;
        }
        for (int i = minIdx; i < maxIdx; i++) {
            drawItem(canvas, i, offset);
        }
        if (mDividerTop != null) {
            mDividerTop.draw(canvas);
        }
        if (mDividerBottom != null) {
            mDividerBottom.draw(canvas);
        }
    }

    protected void drawItem(Canvas canvas, int index, int offset) {
        CharSequence text = getCharSequence(index);
        if (text == null) return;

        // 和中间选项的距离
        final int range = (index - mScroller.getItemIndex()) * itemHeight - offset;

        int clipLeft = getPaddingLeft();
        int clipRight = getWidth() - getPaddingRight();
        int clipTop = getPaddingTop();
        int clipBottom = getHeight() - getPaddingBottom();

        // 绘制两条分界线之间的文字
        if (Math.abs(range) <= 0) {
            mPaint.setColor(mSelectedColor);
            canvas.save();
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            canvas.drawText(text, 0, text.length(), centerX, centerY + range - baseline, mPaint);
            canvas.restore();
        }
        // 绘制与下分界线相交的文字
        else if (range > 0 && range < itemHeight) {
            mPaint.setColor(mSelectedColor);
            canvas.save();
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            canvas.drawText(text, 0, text.length(), centerX, centerY + range - baseline, mPaint);
            canvas.restore();

            mPaint.setColor(mUnselectedColor);
            canvas.save();
            canvas.clipRect(clipLeft, lowerLimit, clipRight, clipBottom);
            canvas.drawText(text, 0, text.length(), centerX, centerY + range - baseline, mPaint);
            canvas.restore();
        }
        // 绘制与上分界线相交的文字
        else if (range < 0 && range > -itemHeight) {
            mPaint.setColor(mSelectedColor);
            canvas.save();
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            canvas.drawText(text, 0, text.length(), centerX, centerY + range - baseline, mPaint);
            canvas.restore();

            mPaint.setColor(mUnselectedColor);
            canvas.save();
            canvas.clipRect(clipLeft, clipTop, clipRight, upperLimit);
            canvas.drawText(text, 0, text.length(), centerX, centerY + range - baseline, mPaint);
            canvas.restore();
        } else {
            mPaint.setColor(mUnselectedColor);
            canvas.save();
            canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
            canvas.drawText(text, 0, text.length(), centerX, centerY + range - baseline, mPaint);
            canvas.restore();
        }
    }

    CharSequence getCharSequence(int index) {
        int size = mEntries.size();
        if (size == 0) return null;
        CharSequence text = null;
        if (isCyclic()) {
            int i = index % size;
            if (i < 0) {
                i += size;
            }
            text = mEntries.get(i);
        } else {
            if (index >= 0 && index < size) {
                text = mEntries.get(index);
            }
        }
        return text;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScroller.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        mScroller.computeScroll();
    }

    public boolean isCyclic() {
        return mCyclic;
    }

    public void setCyclic(boolean cyclic) {
        mCyclic = cyclic;
        mScroller.reset();
        invalidate();
    }

    public int getVisibleItems() {
        return mVisibleItems;
    }

    public void setVisibleItems(int visibleItems) {
        mVisibleItems = Math.abs(visibleItems / 2 * 2 + 1); // 当传入的值为偶数时,换算成奇数;
        mScroller.reset();
        requestLayout();
        invalidate();
    }

    public int getLineSpace() {
        return mLineSpace;
    }

    public void setLineSpace(int lineSpace) {
        mLineSpace = lineSpace;
        mScroller.reset();
        measureItemSize();
        requestLayout();
        invalidate();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mPaint.setTextSize(textSize);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        baseline = (int) ((fontMetrics.top + fontMetrics.bottom) / 2);
        mScroller.reset();
        measureItemSize();
        requestLayout();
        invalidate();
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
        invalidate();
    }

    public int getUnselectedColor() {
        return mUnselectedColor;
    }

    public void setUnselectedColor(int unselectedColor) {
        mUnselectedColor = unselectedColor;
        invalidate();
    }

    public int getItemSize() {
        return mEntries.size();
    }

    public CharSequence getItem(int index) {
        if (index < 0 && index >= mEntries.size())
            return null;

        return mEntries.get(index);
    }

    public CharSequence getCurrentItem() {
        return mEntries.get(getCurrentIndex());
    }

    public int getCurrentIndex() {
        return mScroller.getCurrentIndex();
    }

    public void setCurrentIndex(int index) {
        setCurrentIndex(index, false);
    }

    public void setCurrentIndex(int index, boolean animated) {
        mScroller.setCurrentIndex(index, animated);
    }

    public void setEntries(CharSequence... entries) {
        mEntries.clear();
        if (entries != null && entries.length > 0) {
            Collections.addAll(mEntries, entries);
        }
        mScroller.reset();
        measureItemSize();
        requestLayout();
        invalidate();
    }

    public OnWheelChangedListener getOnWheelChangedListener() {
        return mScroller.onWheelChangedListener;
    }

    public void setOnWheelChangedListener(OnWheelChangedListener onWheelChangedListener) {
        mScroller.onWheelChangedListener = onWheelChangedListener;
    }
}
