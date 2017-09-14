package com.cncoderx.wheelview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

    boolean itemSizeChanged = true;
    int centerX;
    int centerY;
    int upperLimit;
    int lowerLimit;
    int baseline;
    int maxTextWidth;
    int maxTextHeight;

    Paint mPaint;
    WheelScroller mScroller;

    final List<CharSequence> mEntries = new ArrayList<>();

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, R.style.WheelView);

    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.WheelView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WheelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, defStyleAttr, defStyleRes);
        boolean cyclic = a.getBoolean(R.styleable.WheelView_cyclic, false);
        int visibleItems = a.getInt(R.styleable.WheelView_visibleItems, mVisibleItems);
        int lineSpace = a.getDimensionPixelOffset(R.styleable.WheelView_lineSpace, mLineSpace);
        int textSize = a.getDimensionPixelSize(R.styleable.WheelView_textSize, mTextSize);
        int selectedColor = a.getColor(R.styleable.WheelView_selectedColor, 0);
        int unselectedColor = a.getColor(R.styleable.WheelView_unselectedColor, 0);
        Drawable dividerTop = a.getDrawable(R.styleable.WheelView_dividerTop);
        Drawable dividerBottom = a.getDrawable(R.styleable.WheelView_dividerBottom);
        CharSequence[] entries = a.getTextArray(R.styleable.WheelView_entries);
        a.recycle();

        mScroller = new WheelScroller(context, this);

        setCyclic(cyclic);
        setVisibleItems(visibleItems);
        setLineSpace(lineSpace);
        setTextSize(textSize);
        setSelectedColor(selectedColor);
        setUnselectedColor(unselectedColor);
        setDividerTop(dividerTop);
        setDividerBottom(dividerBottom);
        setEntries(entries);

        initPaint();
    }

    private void initPaint() {
        TextPaint paint = new TextPaint();
        paint.setTextSize(mTextSize);
        paint.setAntiAlias(true);
        paint.setColor(mUnselectedColor);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        baseline = (int) ((fontMetrics.top + fontMetrics.bottom) / 2);
        this.mPaint = paint;
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
            calcVisibleItems();
        } else if (widthSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSpecSize, getPrefHeight());
        } else if (heightSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(getPrefWidth(), heightSpecSize);
            calcVisibleItems();
        } else {
            setMeasuredDimension(getPrefWidth(), getPrefHeight());
        }

        centerX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2;
        centerY = (getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2;

        mScroller.setItemHeight(getItemHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 计算上方分割线的高度
        upperLimit = centerY - mScroller.getItemHeight() / 2;
        // 计算下方分割线的高度
        lowerLimit = centerY + mScroller.getItemHeight() / 2;
        if (mDividerTop != null) {
            int h = mDividerTop.getIntrinsicHeight();
            mDividerTop.setBounds(getPaddingLeft(), upperLimit, getWidth() - getPaddingRight(), upperLimit + h);
        }
        if (mDividerBottom != null) {
            int h = mDividerBottom.getIntrinsicHeight();
            mDividerBottom.setBounds(getPaddingLeft(), lowerLimit - h, getWidth() - getPaddingRight(), lowerLimit);
        }
    }

    /**
     * 当选项文字的尺寸发生改变时，计算每一项最大尺寸
     */
    void measureItemBounds() {
        if (itemSizeChanged) {
            maxTextWidth = 0;
            maxTextHeight = 0;
            Rect bounds = new Rect();
            if (mEntries.size() == 0) {
                mPaint.getTextBounds("0", 0, 1, bounds);
                maxTextWidth = Math.max(maxTextWidth, bounds.width());
                maxTextHeight = Math.max(maxTextHeight, bounds.height());
            } else {
                for (CharSequence cs : mEntries) {
                    mPaint.getTextBounds((String) cs, 0, cs.length(), bounds);
                    maxTextWidth = Math.max(maxTextWidth, bounds.width());
                    maxTextHeight = Math.max(maxTextHeight, bounds.height());
                }
            }
            itemSizeChanged = false;
        }
    }

    /**
     * @return 控件的预算宽度
     */
    public int getPrefWidth() {
        measureItemBounds();
        int padding = getPaddingLeft() + getPaddingRight();
        int innerWidth = (int) (maxTextWidth + mTextSize * .5f);
        return innerWidth + padding;
    }

    /**
     * @return 控件的预算高度
     */
    public int getPrefHeight() {
        measureItemBounds();
        int padding = getPaddingTop() + getPaddingBottom();
        int innerHeight = getItemHeight() * mVisibleItems;
        return innerHeight + padding;
    }

    /**
     * 根据控件的测量高度，计算可见项的数量
     */
    protected void calcVisibleItems() {
        measureItemBounds();
        int innerHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int items = innerHeight / getItemHeight();
        setVisibleItems(items);
    }

    public final int getItemHeight() {
        return maxTextHeight + mLineSpace;
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
        final int range = (index - mScroller.getItemIndex()) * mScroller.getItemHeight() - offset;

        int clipLeft = getPaddingLeft();
        int clipRight = getWidth() - getPaddingRight();
        int clipTop = getPaddingTop();
        int clipBottom = getHeight() - getPaddingBottom();

        int dl = mLineSpace / 2;
        int dh = dl + maxTextHeight;
        // 绘制两条分界线之间的文字
        if (Math.abs(range) <= dl) {
            mPaint.setColor(mSelectedColor);
            canvas.save();
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            canvas.drawText(text, 0, text.length(), centerX, centerY + range - baseline, mPaint);
            canvas.restore();
        }
        // 绘制与下分界线相交的文字
        else if (range > dl && range < dh) {
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
        else if (range < -dl && range > -dh) {
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

    private CharSequence getCharSequence(int index) {
        int size = mScroller.getItemSize();
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
        mScroller.setCyclic(cyclic);
        invalidate();
    }

    public int getVisibleItems() {
        return mVisibleItems;
    }

    public void setVisibleItems(int visibleItems) {
        mVisibleItems = Math.abs(visibleItems / 2 * 2 + 1); // 当传入的值为偶数时,换算成奇数;
        requestLayout();
    }

    public int getLineSpace() {
        return mLineSpace;
    }

    public void setLineSpace(int lineSpace) {
        mLineSpace = lineSpace;
        requestLayout();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        if (mPaint != null) {
            mPaint.setTextSize(textSize);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            baseline = (int) ((fontMetrics.top + fontMetrics.bottom) / 2);
        }
        itemSizeChanged = true;
        requestLayout();
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

    public Drawable getDividerTop() {
        return mDividerTop;
    }

    public void setDividerTop(Drawable dividerTop) {
        mDividerTop = dividerTop;
        requestLayout();
    }

    public Drawable getDividerBottom() {
        return mDividerBottom;
    }

    public void setDividerBottom(Drawable dividerBottom) {
        mDividerBottom = dividerBottom;
        requestLayout();
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
        mScroller.setItemSize(mEntries.size());
        mScroller.setCurrentIndex(0, false);
        itemSizeChanged = true;
        requestLayout();
    }

    public OnWheelChangedListener getOnWheelChangedListener() {
        return mScroller.onWheelChangedListener;
    }

    public void setOnWheelChangedListener(OnWheelChangedListener onWheelChangedListener) {
        mScroller.onWheelChangedListener = onWheelChangedListener;
    }
}
