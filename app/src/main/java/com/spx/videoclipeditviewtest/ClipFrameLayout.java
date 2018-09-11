package com.spx.videoclipeditviewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class ClipFrameLayout extends FrameLayout {

    private static final String TAG = "ClipFrameLayout";

    private static final int DELTA = 6;

    private Paint paint;
    private Paint progressPaint;
    private Paint shadowPaint;
    private View frameLeftBar;
    private View frameRightBar;
    private float leftFrameLeft = 0;
    private float rightFrameLeft = 0;
    private float minDistance = 120;

    private int progressStart = (int) leftFrameLeft;
    private int progressWidth = 10;

    public ClipFrameLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        init(context);
    }

    public ClipFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        init(context);
    }

    public ClipFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.colorAccent));
        paint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint();
        progressPaint.setColor(context.getResources().getColor(R.color.progress_color));
        progressPaint.setStyle(Paint.Style.FILL);

        shadowPaint = new Paint();
        shadowPaint.setColor(context.getResources().getColor(R.color.shadow_color));
        shadowPaint.setStyle(Paint.Style.FILL);
        minDistance = context.getResources().getDimensionPixelSize(R.dimen.min_distance);
        progressWidth = context.getResources().getDimensionPixelSize(R.dimen.progressbar_width);
    }


    private View.OnTouchListener LeftTouchListener = new View.OnTouchListener() {
        private float downX;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    final float xDistance = event.getX() - downX;
                    if (xDistance != 0) {
                        float newTransx = v.getTranslationX() + xDistance;
                        if (newTransx < 0) {
                            newTransx = 0;
                        }

                        if (newTransx + v.getWidth() > rightFrameLeft - minDistance) {
                            newTransx = rightFrameLeft - minDistance - v.getWidth();
                        }
                        v.setTranslationX(newTransx);
                        leftFrameLeft = newTransx;
                        progressStart = (int) (leftFrameLeft + v.getWidth());
                        invalidate();
                    }
                    break;
            }
            return false;
        }

    };

    private View.OnTouchListener rightTouchListener = new View.OnTouchListener() {
        private float downX;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    final float xDistance = event.getX() - downX;
                    if (xDistance != 0) {
                        float newTransx = v.getTranslationX() + xDistance;
                        if (newTransx > 0) {
                            newTransx = 0;
                        }
                        if ((getWidth() - v.getWidth() + newTransx) < leftFrameLeft + frameLeftBar.getWidth() + minDistance) {
                            newTransx = -(getWidth() - (leftFrameLeft + frameLeftBar.getWidth() + minDistance) - v.getWidth());
                        }

                        v.setTranslationX(newTransx);
                        rightFrameLeft = getWidth() - v.getWidth() + newTransx;
                        invalidate();
                    }
                    break;
            }
            return false;
        }

    };

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && rightFrameLeft == 0) {
            rightFrameLeft = getWidth() - frameRightBar.getWidth();
            progressStart = (int) (leftFrameLeft + frameLeftBar.getWidth());
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        frameLeftBar = findViewById(R.id.frame_left);
        frameRightBar = findViewById(R.id.frame_right);

        frameLeftBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: ...");
            }
        });

        frameRightBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: ...");
            }
        });

        frameLeftBar.setOnTouchListener(LeftTouchListener);
        frameRightBar.setOnTouchListener(rightTouchListener);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制一个矩形
        canvas.drawRect(new Rect((int) leftFrameLeft + frameLeftBar.getWidth() - DELTA,
                0, (int) rightFrameLeft + DELTA, 10), paint);
        canvas.drawRect(new Rect((int) leftFrameLeft + frameLeftBar.getWidth() - DELTA,
                getHeight() - 10, (int) rightFrameLeft + DELTA, getHeight()), paint);

        canvas.drawRect(new Rect(progressStart, 10, progressStart + progressWidth, getHeight() - 10), progressPaint);

        canvas.drawRect(new Rect(0, 5, (int) leftFrameLeft + DELTA, getHeight() - 5), shadowPaint);
        canvas.drawRect(new Rect((int) (rightFrameLeft + frameRightBar.getWidth()) - DELTA, 5, getWidth(), getHeight() - 5), shadowPaint);
    }


}
