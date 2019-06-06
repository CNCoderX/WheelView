package com.cncoderx.wheelview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * @author cncoderx
 */
public class Wheel3DView extends WheelView {
    private Camera mCamera;
    private Matrix mMatrix;

    public Wheel3DView(Context context) {
        this(context, null);
    }

    public Wheel3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCamera = new Camera();
        mMatrix = new Matrix();
    }

    @Override
    public int getPrefHeight() {
        int padding = getPaddingTop() + getPaddingBottom();
        int innerHeight = (int) (mItemHeight * mItemCount * 2 / Math.PI);
        return innerHeight + padding;
    }

    protected void drawItem(Canvas canvas, int index, int offset) {
        CharSequence text = getCharSequence(index);
        if (text == null) return;
        // 滚轮的半径
        final int r = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        // 和中间选项的距离
        final int range = (index - mScroller.getItemIndex()) * mItemHeight - offset;
        // 当滑动的角度和y轴垂直时（此时文字已经显示为一条线），不绘制文字
        if (Math.abs(range) > r * Math.PI / 2) return;

        final int centerX = mClipRectMiddle.centerX();
        final int centerY = mClipRectMiddle.centerY();

        final double angle = (double) range / r;
        // 绕x轴滚动的角度
        float rotate = (float) Math.toDegrees(-angle);
        // 滚动的距离映射到x轴的长度
//        float translateX = (float) (Math.cos(angle) * Math.sin(Math.PI / 36) * r * mToward);
        // 滚动的距离映射到y轴的长度
        float translateY = (float) (Math.sin(angle) * r);
        // 滚动的距离映射到z轴的长度
        float translateZ = (float) ((1 - Math.cos(angle)) * r);
        // 折射偏移量x
        float refractX = getTextSize() * .05f;
        // 透明度
        int alpha = (int) (Math.cos(angle) * 255);

        // 绘制与下分界线相交的文字
        if (range > 0 && range < mItemHeight) {
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(mClipRectMiddle);
            drawText(canvas, text, centerX, centerY, 0, translateY, translateZ, rotate, mSelectedTextPaint);
            canvas.restore();

            mTextPaint.setAlpha(alpha);
            canvas.save();
            canvas.clipRect(mClipRectBottom);
            drawText(canvas, text, centerX, centerY, 0, translateY, translateZ, rotate, mTextPaint);
            canvas.restore();
        }
        // 绘制下分界线下方的文字
        else if (range >= mItemHeight) {
            mTextPaint.setAlpha(alpha);
            canvas.save();
            canvas.clipRect(mClipRectBottom);
            drawText(canvas, text, centerX, centerY, 0, translateY, translateZ, rotate, mTextPaint);
            canvas.restore();
        }
        // 绘制与上分界线相交的文字
        else if (range < 0 && range > -mItemHeight) {
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(mClipRectMiddle);
            drawText(canvas, text, centerX, centerY, 0, translateY, translateZ, rotate, mSelectedTextPaint);
            canvas.restore();

            mTextPaint.setAlpha(alpha);
            canvas.save();
            canvas.clipRect(mClipRectTop);
            drawText(canvas, text, centerX, centerY, 0, translateY, translateZ, rotate, mTextPaint);
            canvas.restore();
        }
        // 绘制上分界线上方的文字
        else if (range <= -mItemHeight) {
            mTextPaint.setAlpha(alpha);
            canvas.save();
            canvas.clipRect(mClipRectTop);
            drawText(canvas, text, centerX, centerY, 0, translateY, translateZ, rotate, mTextPaint);
            canvas.restore();
        }
        // 绘制两条分界线之间的文字
        else {
            canvas.save();
            canvas.translate(refractX, 0);
            canvas.clipRect(mClipRectMiddle);
            drawText(canvas, text, centerX, centerY, 0, translateY, translateZ, rotate, mSelectedTextPaint);
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas,
                          CharSequence text,
                          float centerX,
                          float centerY,
                          float translateX,
                          float translateY,
                          float translateZ,
                          float rotateX,
                          Paint paint) {
        mCamera.save();
        mCamera.translate(translateX, 0, translateZ);
        mCamera.rotateX(rotateX);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        final float x = centerX;
        final float y = centerY + translateY;

        // 设置绕x轴旋转的中心点位置
        mMatrix.preTranslate(-x, -y);
        mMatrix.postTranslate(x, y);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int baseline = (int) ((fontMetrics.top + fontMetrics.bottom) / 2);

        canvas.concat(mMatrix);
        canvas.drawText(text, 0, text.length(), x, y - baseline, paint);
    }
}
