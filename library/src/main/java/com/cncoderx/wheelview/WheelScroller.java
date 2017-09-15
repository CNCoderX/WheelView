package com.cncoderx.wheelview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;

/**
 * @author cncoderx
 */
public class WheelScroller extends Scroller {
    private boolean cyclic;
    private int mScrollOffset;
    private float lastTouchY;
    private boolean isScrolling;

    final WheelView mAttachedView;
    private VelocityTracker mVelocityTracker;
    OnWheelChangedListener onWheelChangedListener;

    public static final int JUSTIFY_DURATION = 400;

    public WheelScroller(Context context, WheelView attachedView) {
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
            final int maxOffset = (mAttachedView.getItemSize() - 1) * mAttachedView.itemHeight;
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
                onWheelChangedListener.onChanged(mAttachedView, oldValue, newValue);
            }
        }
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    public int getCurrentIndex() {
        final int itemHeight = mAttachedView.itemHeight;
        final int itemSize = mAttachedView.getItemSize();
        if (itemSize == 0) return 0;

        int itemIndex;
        if (mScrollOffset < 0) {
            itemIndex = (mScrollOffset - itemHeight / 2) / itemHeight;
        } else {
            itemIndex = (mScrollOffset + itemHeight / 2) / itemHeight;
        }
        int currentIndex = itemIndex % itemSize;
        if (currentIndex < 0) {
            currentIndex += itemSize;
        }
        return currentIndex;
    }

    public void setCurrentIndex(int index, boolean animated) {
        int position = index * mAttachedView.itemHeight;
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
        return mAttachedView.itemHeight == 0 ? 0 : mScrollOffset / mAttachedView.itemHeight;
    }

    public int getItemOffset() {
        return mAttachedView.itemHeight == 0 ? 0 : mScrollOffset % mAttachedView.itemHeight;
    }

    /**
     * 当滚轮结束滑行后，调整滚轮的位置，需要调用该方法
     */
    void justify() {
        final int itemHeight = mAttachedView.itemHeight;
        final int offset = mScrollOffset % itemHeight;
        if (offset > 0 && offset < itemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, -offset, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        } else if (offset >= itemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, itemHeight - offset, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        } else if (offset < 0 && offset > -itemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, -offset, JUSTIFY_DURATION);
            mAttachedView.invalidate();
        } else if (offset <= -itemHeight / 2) {
            isScrolling = true;
            startScroll(0, mScrollOffset, 0, -itemHeight - offset, JUSTIFY_DURATION);
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
