package com.cncoderx.wheelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;

/**
 * @author cncoderx
 */
public class Wheel3DView extends WheelView {
    public static final int TOWARD_NONE = 0;
    public static final int TOWARD_LEFT = -1;
    public static final int TOWARD_RIGHT = 1;

    private int mToward;

    private Camera mCamera;
    private Matrix mMatrix;

    public Wheel3DView(Context context) {
        this(context, null);
    }

    public Wheel3DView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Wheel3DView);
        int toward = a.getInt(R.styleable.Wheel3DView_toward, 0);
        a.recycle();

        setToward(toward);

        mCamera = new Camera();
        mMatrix = new Matrix();
    }

    /**
     * @return 控件的预算宽度
     */
    public int getPrefWidth() {
        int prefWidth = super.getPrefWidth();
        int innerHeight = (int) (itemHeight * getVisibleItems() * 2 / Math.PI);
        int towardRange = (int) (Math.sin(Math.PI / 48) * innerHeight);
        // 必须增加滚轮的内边距,否则当toward不为none时文字显示不全
        prefWidth += towardRange;
        return prefWidth;
    }

    /**
     * @return 控件的预算高度
     */
    public int getPrefHeight() {
        int padding = getPaddingTop() + getPaddingBottom();
        int innerHeight = (int) (itemHeight * getVisibleItems() * 2 / Math.PI);
        return innerHeight + padding;
    }

    /**
     * 根据控件的测量高度，计算可见项的数量
     */
    protected void calcVisibleItems() {
        int innerHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int items = (int) (innerHeight * Math.PI / itemHeight / 2);
        setVisibleItems(items);
    }

    float getRefractX() {
        float dx;
        switch (mToward) {
            case TOWARD_LEFT:
                dx = -getTextSize() * .07f;
                break;
            case TOWARD_RIGHT:
                dx = getTextSize() * .07f;
                break;
            default:
                dx = getTextSize() * .05f;
                break;
        }
        return dx;
    }

    protected void drawItem(Canvas canvas, int index, int offset) {
        CharSequence text = getCharSequence(index);
        if (text == null) return;
        // 滚轮的半径
        final int r = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        // 和中间选项的距离
        final int range = (index - mScroller.getItemIndex()) * itemHeight - offset;
        // 当滑动的角度和y轴垂直时（此时文字已经显示为一条线），不绘制文字
        if (Math.abs(range) > r * Math.PI / 2) return;
        final double angle = (double) range / r;
        // x轴滚动的角度
        float rotate = (float) Math.toDegrees(-angle);
        // 滚动的距离映射到x轴的长度
        float translateX = (float) (Math.cos(angle) * Math.sin(Math.PI / 48) * r * mToward);
        // 滚动的距离映射到y轴的长度
        float translateY = (float) (Math.sin(angle) * r);

        float scaleX = (float) Math.cos(angle / 3);
        // 折射偏移量x
        float refractX = getRefractX();

        int clipLeft = getPaddingLeft();
        int clipRight = getWidth() - getPaddingRight();
        int clipTop = getPaddingTop();
        int clipBottom = getHeight() - getPaddingBottom();

        // 绘制两条分界线之间的文字
        if (Math.abs(range) <= 0) {
            mPaint.setColor(getSelectedColor());
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            drawText(canvas, text, translateX, translateY, rotate, 1);
            canvas.restore();
        }
        // 绘制与下分界线相交的文字
        else if (range > 0 && range < itemHeight) {
            mPaint.setColor(getSelectedColor());
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            drawText(canvas, text, translateX, translateY, rotate, 1);
            canvas.restore();

            mPaint.setColor(getUnselectedColor());
            canvas.save();
            canvas.clipRect(clipLeft, lowerLimit, clipRight, clipBottom);
            drawText(canvas, text, translateX, translateY, rotate, scaleX);
            canvas.restore();
        }
        // 绘制与上分界线相交的文字
        else if (range < 0 && range > -itemHeight) {
            mPaint.setColor(getSelectedColor());
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(clipLeft, upperLimit, clipRight, lowerLimit);
            drawText(canvas, text, translateX, translateY, rotate, 1);
            canvas.restore();

            mPaint.setColor(getUnselectedColor());
            canvas.save();
            canvas.clipRect(clipLeft, clipTop, clipRight, upperLimit);
            drawText(canvas, text, translateX, translateY, rotate, scaleX);
            canvas.restore();
        } else {
            mPaint.setColor(getUnselectedColor());
            canvas.save();
            canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
            drawText(canvas, text, translateX, translateY, rotate, scaleX);
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas, CharSequence text, float translateX, float translateY, float rotate, float scale) {
        mCamera.save();
        mCamera.rotateX(rotate);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        // 设置绕x轴旋转的中心点位置
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX + translateX, centerY + translateY);

        canvas.scale(scale, 1, centerX, centerY);
        canvas.concat(mMatrix);
        canvas.drawText(text, 0, text.length(), centerX, centerY - baseline, mPaint);
    }

    public int getToward() {
        return mToward;
    }

    public void setToward(int toward) {
        switch (toward) {
            case TOWARD_LEFT:
                mToward = TOWARD_LEFT;
                break;
            case TOWARD_RIGHT:
                mToward = TOWARD_RIGHT;
                break;
            case TOWARD_NONE:
            default:
                mToward = TOWARD_NONE;
                break;
        }
        requestLayout();
    }
}
