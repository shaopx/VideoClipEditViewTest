package com.daasuu.epf.custfilter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.daasuu.epf.R;
import com.daasuu.epf.filter.FilterType;
import com.daasuu.epf.filter.GlFilter;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform2fv;

public class GlItchFilter extends GlFilter {

    private int mScanLineJitterLocation;
    private int mColorDriftLocation;
    private int mGlobalTimeLocation;

    private long mStartTime;

    private int mFrames = 0;

    /**
     * 动画总共8帧
     */
    private int mMaxFrames = 8;

    private float[] mDriftSequence = new float[]{0f, 0.03f, 0.032f, 0.035f, 0.03f, 0.032f, 0.031f, 0.029f, 0.025f};

    private float[] mJitterSequence = new float[]{0f, 0.03f, 0.01f, 0.02f, 0.05f, 0.055f, 0.03f, 0.02f, 0.025f};

    private float[] mThreshHoldSequence = new float[]{1.0f, 0.965f, 0.9f, 0.9f, 0.9f, 0.6f, 0.8f, 0.5f, 0.5f};


    public GlItchFilter(Context context) {
        super(context, R.raw.def_vertext, R.raw.fragment_itch);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SPX_ITCH;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mScanLineJitterLocation = glGetUniformLocation(mProgramHandle, "uScanLineJitter");
        mColorDriftLocation = glGetUniformLocation(mProgramHandle, "uColorDrift");
        mGlobalTimeLocation = glGetUniformLocation(mProgramHandle, "uGlobalTime");
    }


    @Override
    public void onDraw() {
        long time = System.currentTimeMillis();
        if (mStartTime == 0) {
            mStartTime = time;
        }
        glUniform1f(mGlobalTimeLocation, mFrames);
        mStartTime = time;

        float slDisplacement = mJitterSequence[mFrames];
        float slThreshold = mThreshHoldSequence[mFrames];
        float drift = mDriftSequence[mFrames];
        mFrames++;
        if (mFrames > mMaxFrames) {
            mFrames = 0;
        }
        glUniform2fv(mScanLineJitterLocation, 1, new float[]{slDisplacement, slThreshold}, 0);
        glUniform1f(mColorDriftLocation, drift);

    }

}

