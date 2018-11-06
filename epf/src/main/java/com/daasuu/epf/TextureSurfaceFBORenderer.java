package com.daasuu.epf;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import com.daasuu.epf.filter.GlFilter;
import com.spx.library.player.mp.TextureSurfaceRenderer;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

public abstract class TextureSurfaceFBORenderer extends TextureSurfaceRenderer {

    private EFramebufferObject framebufferObject;
    private GlFilter normalShader;

    public TextureSurfaceFBORenderer() {
        super();
    }

    @Override
    protected void initGLComponents() {
        framebufferObject = new EFramebufferObject();
        normalShader = new GlFilter();
        normalShader.setup();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        framebufferObject.setup(width, height);
        normalShader.setFrameSize(width, height);
    }

    @Override
    protected boolean onDrawFrame() {
        framebufferObject.enable();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        onDrawFrame(framebufferObject);


        // 在最外层, 最终把输出从屏幕输出
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        normalShader.draw(framebufferObject.getTexName(), null, null);
        return true;
    }



    @Override
    protected void deinitGLComponents() {

    }

//    @Override
//    public SurfaceTexture getVideoTexture() {
//        return null;
//    }

    public abstract void onSurfaceCreated(EGLConfig config);

    public abstract void onDrawFrame(EFramebufferObject fbo);
}
