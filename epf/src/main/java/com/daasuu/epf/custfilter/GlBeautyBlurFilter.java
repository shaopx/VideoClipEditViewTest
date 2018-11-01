package com.daasuu.epf.custfilter;

import android.opengl.GLES20;

import com.daasuu.epf.filter.GlFilter;

public class GlBeautyBlurFilter extends GlFilter {
    private static final String FRAGMENT_SHADER = "" ;
    private float opacity = 0.5f;
    private int width = 1080;
    private int height = 1920;
    // 高斯模糊处理的图像缩放倍数
    private float mBlurScale = 0.5f;

    public GlBeautyBlurFilter() {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(final float opacity) {
        this.opacity = opacity;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDraw() {
        GLES20.glUniform1f(getHandle("opacity"), opacity);
        GLES20.glUniform1i(getHandle("width"), width);
        GLES20.glUniform1i(getHandle("height"), height);
    }
}
