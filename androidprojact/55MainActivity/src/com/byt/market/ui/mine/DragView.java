/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.byt.market.ui.mine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;

public class DragView extends View implements TweenCallback {
    // Number of pixels to add to the dragged item for scaling.  Should be even for pixel alignment.
    private static final int DRAG_SCALE = 30;
    private static final String TAG = "Launcher.DragView";

    private Bitmap mBitmap;
    private Paint mPaint;
    private int mRegistrationX;
    private int mRegistrationY;

    SymmetricalLinearTween mTween;

    private float mPosX;
    private float mPosY;

    private float mTargetX;
    private float mTargetY;

    private float mScale;
    private float mAnimationScale = 1.0f;

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;

    private DragController mDragController;
    public void setDragController(DragController dc) {
        mDragController = dc;
    }

    /**
     * Construct the drag view.
     * <p>
     * The registration point is the point inside our view that the touch events should
     * be centered upon.
     *
     * @param context A context
     * @param bitmap The view that we're dragging around.  We scale it up when we draw it.
     * @param registrationX The x coordinate of the registration point.
     * @param registrationY The y coordinate of the registration point.
     */
    public DragView(Context context, Bitmap bitmap, int registrationX, int registrationY,
            int left, int top, int width, int height) {
        super(context);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        	//WindowManagerImpl.getDefault();
        
        mTween = new SymmetricalLinearTween(false, 200 /*ms duration*/, this);

        Matrix scale = new Matrix();
        float scaleFactor = width;
        scaleFactor = mScale = (scaleFactor + DRAG_SCALE) / scaleFactor;
        scale.setScale(scaleFactor, scaleFactor);

        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height, scale, true);

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX + (DRAG_SCALE / 2);
        mRegistrationY = registrationY + (DRAG_SCALE / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (false) {
            // for debugging
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xaaffffff);
            canvas.drawRect(0, 0, getWidth(), getHeight(), p);
        }
        float scale = mAnimationScale;
        if (scale < 0.999f) { // allow for some float error
            float width = mBitmap.getWidth();
            float offset = (width-(width*scale))/2;
            canvas.translate(offset, offset);
            canvas.scale(scale, scale);
        }
        if(mBitmap != null && !mBitmap.isRecycled()){
        	canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
        }
        
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBitmap.recycle();
    }

    public void onTweenValueChanged(float value, float oldValue) {
        mAnimationScale = (1.0f+((mScale-1.0f)*value))/mScale;
        invalidate();
    }

    public void onTweenStarted() {
    }

    public void onTweenFinished() {
    }

    
    
    //@Override
    public void setAlpha(float alpha) {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setAlpha((int) (255 * alpha));
        
        invalidate();
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
        mPaint.setAntiAlias(true);
        invalidate();
    }

    /**
     * Create a window containing this view and show it.
     *
     * @param windowToken obtained from v.getWindowToken() from one of your views
     * @param touchX the x coordinate the user touched in screen coordinates
     * @param touchY the y coordinate the user touched in screen coordinates
     */
    public void show(IBinder windowToken, int touchX, int touchY) {
        WindowManager.LayoutParams lp;
        int pixelFormat;

        pixelFormat = PixelFormat.TRANSLUCENT;

        lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                touchX-mRegistrationX, touchY-mRegistrationY,
                WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    /*| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM*/,
                pixelFormat);
//        lp.token = mStatusBarView.getWindowToken();
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.token = windowToken;
        lp.setTitle("DragView");
        mLayoutParams = lp;

        mWindowManager.addView(this, lp);

        mAnimationScale = 1.0f/mScale;
        
        mTween.start(true);
        scaleIn();
    }
    private void scaleIn(){
    	ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f);
    	scaleAnimation.setDuration(2000);
    	this.startAnimation(scaleAnimation);
    }
    
    /**
     * Move the window containing this view.
     *
     * @param touchX the x coordinate the user touched in screen coordinates
     * @param touchY the y coordinate the user touched in screen coordinates
     */
    void move(int touchX, int touchY) {
        WindowManager.LayoutParams lp = mLayoutParams;
        lp.x = touchX - mRegistrationX;
        lp.y = touchY - mRegistrationY;
        mWindowManager.updateViewLayout(this, lp);
    }

    public float getX() {
        return mLayoutParams.x;
    }

    public float getY() {
        return mLayoutParams.y;
    }

    void remove() {
        mWindowManager.removeView(this);
    }
    
    void resetToDefault() {
    	mAnimationScale = 1.0f/mScale;
    	setAlpha(1.0f);
    	
    	invalidate();
    }
}

