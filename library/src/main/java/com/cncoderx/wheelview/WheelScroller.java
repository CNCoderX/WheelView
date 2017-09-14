package com.cncoderx.wheelview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

/**
 * @author cncoderx
 */
public class WheelScroller extends Scroller {
    private int mItemSize;
    private int mItemHeight;

    private boolean cyclic;
    private int mScrollOffset;
    private float lastTouchY;
    private boolean isScrolling;

    final View mAttachedView;
    private VelocityTracker mVelocityTracker;
    OnWheelChangedListener onWheelChangedListener;

    public static final int JUSTIFY_DURATION = 400;

    public WheelScroller(Context context, View attachedView) {
        super(context);
        mAttachedView = attachedView;
    }

    public void computeScroll() {
        if (isScrolling) {
            isScrolling = computeScrollOffset();
            doScroll(getCurrY() - mScrollOffset);
            if (isScrolling) {
                mAttachedView.postInvalidate();
            } else {
                // 滚动结束后，重新调整位置
                justify();
            }
        }
    }

    private int currentIndex;

    private void doScroll(int distance) {
        mScrollOffset += distance;
        if (!cyclic) {
            // 限制滚动边界
            final int maxOffset = (mItemSize - 1) * mItemHeight;
            if (mScrollOffset < 0) {
                mScrollOffset = 0;
            } else if (mScrollOffset > maxOffset) {
                mScrollOffset = maxOffset;
            }
        }
        int oldValue = currentIndex;
        int newValue = getCurrentIndex();
        if (oldValue != newValue) {
            currentIndex = newValue;
            if (onWheelChangedListener != null) {
                onWheelChangedListener.onChanged((WheelView) mAttachedView, oldValue, newValue);
            }
        }
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    public int getItemHeight() {
        return mItemHeight;
    }

    public void setItemHeight(int itemHeight) {
        if (mItemHeight != itemHeight) {
            // 每一项的高度改变时，需要重新计算滚动偏移量；
            mScrollOffset = mItemHeight == 0 ? 0 :
                    itemHeight * mScrollOffset / mItemHeight;
            mItemHeight = itemHeight;
        }
    }

    public int getItemSize() {
        return mItemSize;
    }

    public void setItemSize(int itemSize) {
        mItemSize = itemSize;
    }

    public int getCurrentIndex() {
        if (mItemSize == 0) return 0;
        if (mItemHeight == 0) return 0;

        int itemIndex;
        if (mScrollOffset < 0) {
            itemIndex = (mScrollOffset - mItemHeight / 2) / mItemHeight;
        } else {
            itemIndex = (mScrollOffset + mItemHeight / 2) / mItemHeight;
        }
        int currentIndex = itemIndex % mItemSize;
        if (currentIndex < 0) {
            currentIndex += mItemSize;
        }
        return currentIndex;
    }

    public void setCurrentIndex(int index, boolean animated) {
        int position = index * mItemHeight;
        int distance = position - mScrollOffset;
        if (distance == 0) return;
        if (animated) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, distance, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        } else {
            doScroll(distance);
            mAttachedView.invalidate();
        }
    }

    public int getItemIndex() {
        return mItemHeight == 0 ? 0 : mScrollOffset / mItemHeight;
    }

    public int getItemOffset() {
        return mItemHeight == 0 ? 0 : mScrollOffset % mItemHeight;
    }

    /**
     * 当滚轮结束滑行后，调整滚轮的位置，需要调用该方法
     */
    void justify() {
        final int offset = mScrollOffset % mItemHeight;
        if (offset > 0 && offset < mItemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, -offset, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        } else if (offset >= mItemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, mItemHeight - offset, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        } else if (offset < 0 && offset > -mItemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, -offset, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        } else if (offset <= -mItemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, -mItemHeight - offset, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchY = event.getY();
                forceFinished(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float touchY = event.getY();
                int deltaY = (int) (touchY - lastTouchY);
                if (deltaY != 0) {
                    doScroll(-deltaY);
                    mAttachedView.invalidate();
                }
                lastTouchY = touchY;
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityY = mVelocityTracker.getYVelocity();

                if (Math.abs(velocityY) > 0) {
                    isScrolling = true;
                    fling(0, mScrollOffset, 0, (int) -velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    mAttachedView.invalidate();
                } else {
                    justify();
                }
            case MotionEvent.ACTION_CANCEL:
                // 当触发抬起、取消事件后，回收VelocityTracker
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }
}
