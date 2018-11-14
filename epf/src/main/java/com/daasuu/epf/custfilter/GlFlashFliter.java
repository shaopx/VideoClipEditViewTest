package com.daasuu.epf.custfilter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.daasuu.epf.R;
import com.daasuu.epf.filter.FilterType;
import com.daasuu.epf.filter.GlFilter;

public class GlFlashFliter extends GlFilter {


    private int mExposeHandle;
    private int mFrames;

    private int mMaxFrames = 8;

    private int mHalfFrames = mMaxFrames / 2;

    public GlFlashFliter(Context context) {
        super(context, R.raw.def_vertext, R.raw.fragment_flash);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SPX_FLASH;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mExposeHandle = GLES30.glGetUniformLocation(mProgramHandle, "uAdditionalColor");
    }

    @Override
    public void onDraw() {
        float progress;
        if (mFrames <= mHalfFrames) {
            progress = mFrames * 1.0f / mHalfFrames;
        } else {
            progress = 2.0f - mFrames * 1.0f / mHalfFrames;
        }
        mFrames++;
        if (mFrames > mMaxFrames) {
            mFrames = 0;
        }
        GLES20.glUniform1f(mExposeHandle, progress);


    }
}

