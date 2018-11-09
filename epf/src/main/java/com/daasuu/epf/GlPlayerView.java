package com.daasuu.epf;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;

import com.daasuu.epf.filter.GlFilter;
import com.spx.egl.MPlayerView;
import com.spx.library.player.mp.TextureSurfaceRenderer2;

public class GlPlayerView extends MPlayerView {
    public GlPlayerView(Context context) {
        super(context);
    }

    public GlPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public TextureSurfaceRenderer2 getVideoRender(SurfaceTexture surface, int width, int height) {
        VideoTextureSurfaceRenderer2 renderer = new VideoTextureSurfaceRenderer2(getContext());
        renderer.setUpSurfaceTexture(surface, width, height);
        return renderer;
    }

}
