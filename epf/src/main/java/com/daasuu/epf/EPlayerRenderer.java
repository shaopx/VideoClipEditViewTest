package com.daasuu.epf;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.daasuu.epf.filter.GlFilter;
import com.daasuu.epf.filter.GlLookUpTableFilter;
import com.daasuu.epf.filter.GlPreviewFilter;
import com.google.android.exoplayer2.SimpleExoPlayer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glViewport;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class EPlayerRenderer extends EFrameBufferObjectRenderer implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = EPlayerRenderer.class.getSimpleName();

    private ESurfaceTexture previewTexture;
    private boolean updateSurface = false;

    private int texName;


    private EFramebufferObject filterFramebufferObject;
    private GlPreviewFilter previewFilter;

    private GlFilter glFilter;
    private boolean isNewFilter;
    private final GLSurfaceView glPreview;


    private SimpleExoPlayer simpleExoPlayer;

    public interface RenderCallback {
        void onRenderCreated(SurfaceTexture surfaceTexture, int width, int height);
    }

    RenderCallback callback = null;

    public EPlayerRenderer(GLSurfaceView glPreview) {
        super();
        this.glPreview = glPreview;
    }

    public void setPreviewFilter(GlPreviewFilter previewFilter) {
        this.previewFilter = previewFilter;
    }

    public void setCallback(RenderCallback callback) {
        this.callback = callback;
    }

    public void setGlFilter(final GlFilter filter) {
        glPreview.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (glFilter != null) {
                    glFilter.release();
                    if (glFilter instanceof GlLookUpTableFilter) {
                        ((GlLookUpTableFilter) glFilter).releaseLutBitmap();
                    }
                    glFilter = null;
                }
                glFilter = filter;
                isNewFilter = true;
                glPreview.requestRender();
            }
        });
    }

    @Override
    public void onSurfaceCreated(final EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        final int[] args = new int[1];

        GLES20.glGenTextures(args.length, args, 0);
        texName = args[0];


        previewTexture = new ESurfaceTexture(texName);
        previewTexture.setOnFrameAvailableListener(this);
        if (callback != null) {
            Log.d(TAG, "onSurfaceCreated: width:"+glPreview.getWidth()+", height:"+glPreview.getHeight());
            callback.onRenderCreated(previewTexture.getSurfaceTexture(), glPreview.getWidth(), glPreview.getHeight());
        }

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texName);
        // GL_TEXTURE_EXTERNAL_OES
        EglUtil.setupSampler(GL_TEXTURE_EXTERNAL_OES, GL_NEAREST, GL_NEAREST);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0);


        filterFramebufferObject = new EFramebufferObject();
        // GL_TEXTURE_EXTERNAL_OES
        if (previewFilter == null) {
            previewFilter = new GlPreviewFilter(previewTexture.getTextureTarget());
            previewFilter.setup();
        }


        // 通过把previewTexture的对应的surface传给simpleExoPlayer, 那simpleExoPlayer播放的输出都会渲染到previewTexture对应的纹理texName上
        if (simpleExoPlayer != null) {
            Surface surface = new Surface(previewTexture.getSurfaceTexture());
            this.simpleExoPlayer.setVideoSurface(surface);
        }


        synchronized (this) {
            updateSurface = false;
        }

        if (glFilter != null) {
            isNewFilter = true;
        }

        GLES20.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);

    }

    @Override
    public void onSurfaceChanged(final int width, final int height) {
        Log.d(TAG, "onSurfaceChanged width = " + width + "  height = " + height);



        //因为这里设置了FBO, 所以exoplayer的输出不再是屏幕, 而是帧缓冲区对象
        //不明白的是里面又创建了新的纹理作为 fbo , 那前面那个纹理  是什么用处?   -- 上面那个纹理texName是作为视频播放的输出的, 见下面的onDrawFrame()
        filterFramebufferObject.setup(width, height);
        previewFilter.setFrameSize(width, height);
        if (glFilter != null) {
            glFilter.setFrameSize(width, height);
        }

        previewFilter.onSurfaceChanged(width, height);

    }


    @Override
    public void onDrawFrame(final EFramebufferObject fbo) {

        synchronized (this) {
            if (updateSurface) {
                previewTexture.updateTexImage();
                previewFilter.updateTransform(previewTexture.getSurfaceTexture());
                updateSurface = false;
            }
        }

        if (isNewFilter) {
            if (glFilter != null) {
                glFilter.setup();
                glFilter.setFrameSize(fbo.getWidth(), fbo.getHeight());
            }
            isNewFilter = false;
        }

        if (glFilter != null) {
            filterFramebufferObject.enable();
            glViewport(0, 0, filterFramebufferObject.getWidth(), filterFramebufferObject.getHeight());
        }

        GLES20.glClear(GL_COLOR_BUFFER_BIT);


        previewFilter.draw(texName);
        // 第一个管线输出到哪?  应该是framebuffer的颜色缓冲区吧.  -- 取决于framebuffer是否启用.filterFramebufferObject.enable();

        if (glFilter != null) {
            fbo.enable();  // 重新启用了最外层的fbo , 于是glFilter的输出就到了这个fbo .
            GLES20.glClear(GL_COLOR_BUFFER_BIT);
            glFilter.draw(filterFramebufferObject.getTexName(), fbo, null);
        }
    }

    @Override
    public synchronized void onFrameAvailable(final SurfaceTexture previewTexture) {
        updateSurface = true;
        glPreview.requestRender();
    }

    void setSimpleExoPlayer(SimpleExoPlayer simpleExoPlayer) {
        this.simpleExoPlayer = simpleExoPlayer;
    }

    void release() {
        if (glFilter != null) {
            glFilter.release();
        }
        if (previewTexture != null) {
            previewTexture.release();
        }
    }

}
