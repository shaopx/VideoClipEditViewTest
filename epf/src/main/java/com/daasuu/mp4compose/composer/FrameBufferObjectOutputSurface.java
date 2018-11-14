package com.daasuu.mp4compose.composer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.daasuu.epf.EFramebufferObject;
import com.daasuu.epf.EglUtil;
import com.daasuu.epf.filter.GlFilter;
import com.daasuu.mp4compose.utils.GlUtils;
import com.spx.egl.MagicJni;


import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

public abstract class FrameBufferObjectOutputSurface implements SurfaceTexture.OnFrameAvailableListener {

    private static final boolean VERBOSE = true;
    private static final String TAG = "FBOOutputSurface";
    private EFramebufferObject framebufferObject;
    private EFramebufferObject lastFrameFBO;
    private GlFilter normalShader;
    protected SurfaceTexture surfaceTexture;

    private static final int PBO_SIZE = 2;
    private int[] pboIds = new int[PBO_SIZE];
    private int lastPboId = -1;
    private int mWidth, mHeight;

    private Map<String, Integer> extraTextureIds = new HashMap<>();

    public final void setupAll() {
        mWidth = getOutputWidth();
        mHeight = getOutputHeight();
        framebufferObject = new EFramebufferObject();
        lastFrameFBO = new EFramebufferObject();
        normalShader = new GlFilter();
        normalShader.setup();

        framebufferObject.setup(mWidth, mHeight);
        lastFrameFBO.setup(mWidth, mHeight);
        normalShader.setFrameSize(mWidth, mHeight);

        pboIds = EglUtil.genPbo(PBO_SIZE, mWidth, mHeight);

        setup();
    }

    protected abstract int getOutputHeight();

    protected abstract int getOutputWidth();

    protected abstract void setup();

    private Object frameSyncObject = new Object();     // guards frameAvailable
    private boolean frameAvailable;
    private volatile boolean stopRun = false;

    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        if (VERBOSE) Log.d(TAG, "new frame available");
        synchronized (frameSyncObject) {
            if (frameAvailable) {
                throw new RuntimeException("frameAvailable already set, frame could be dropped");
            }
            frameAvailable = true;
            frameSyncObject.notifyAll();
        }
    }

    public void stopRun() {
        stopRun = true;
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the DecoderSurface object, after the onFrameAvailable callback has signaled that new
     * data is available.
     */
    public void awaitNewImage() {
        final int TIMEOUT_MS = 10000;
        synchronized (frameSyncObject) {
            while (!frameAvailable && !stopRun) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    frameSyncObject.wait(TIMEOUT_MS);
                    if (!frameAvailable && !stopRun) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Surface frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            frameAvailable = false;
        }
        if (stopRun) {
            return;
        }
        // Latch the data.
        GlUtils.checkGlError("before updateTexImage");
        surfaceTexture.updateTexImage();
    }


    public void drawImage(long presentationTimeUs) {
        Log.d(TAG, "drawImage: presentationTimeUs:" + presentationTimeUs);
        framebufferObject.enable();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());


        onDrawFrame(framebufferObject, presentationTimeUs, extraTextureIds);


        // 在最外层, 最终把输出从屏幕输出
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());
        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        normalShader.draw(framebufferObject.getTexName(), null, null);

        if (needLastFrame()) {
            // 先绘制到上一帧fbo中.
            lastFrameFBO.enable();
            GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());
            GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            normalShader.draw(framebufferObject.getTexName(), null, null);
            int lastTexName = lastFrameFBO.getTexName();
            extraTextureIds.put("last_frame_texture", lastTexName);
        }
    }

    protected boolean needLastFrame() {
        return false;
    }

    public abstract void onDrawFrame(EFramebufferObject framebufferObject, long presentationTimeUs, Map<String, Integer> extraTextureIds);

    public int getLastTextId(int offset) {
        return lastPboId;
    }
}
