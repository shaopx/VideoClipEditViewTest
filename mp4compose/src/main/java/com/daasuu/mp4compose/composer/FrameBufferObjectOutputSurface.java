package com.daasuu.mp4compose.composer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import com.daasuu.epf.EFramebufferObject;
import com.daasuu.epf.filter.GlFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

public abstract class FrameBufferObjectOutputSurface implements SurfaceTexture.OnFrameAvailableListener {

    private EFramebufferObject framebufferObject;
    private GlFilter normalShader;

    public final void setupAll(int width, int height) {
        framebufferObject = new EFramebufferObject();
        normalShader = new GlFilter();
        normalShader.setup();

        framebufferObject.setup(width, height);
        normalShader.setFrameSize(width, height);

        setup();
    }

    protected abstract void setup();


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    }

    public void drawImage(long presentationTimeUs) {
        framebufferObject.enable();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        onDrawFrame(framebufferObject, presentationTimeUs);


        // 在最外层, 最终把输出从屏幕输出
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        normalShader.draw(framebufferObject.getTexName(), null, null);
    }

    public abstract void onDrawFrame(EFramebufferObject framebufferObject, long presentationTimeUs);
}
