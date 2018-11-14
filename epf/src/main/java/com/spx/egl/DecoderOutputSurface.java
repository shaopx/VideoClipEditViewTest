package com.spx.egl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.daasuu.epf.EFramebufferObject;
import com.daasuu.epf.EglUtil;
import com.daasuu.epf.filter.GlFilter;
import com.daasuu.epf.filter.GlPreviewFilter;
import com.daasuu.mp4compose.FillMode;
import com.daasuu.mp4compose.FillModeCustomItem;
import com.daasuu.mp4compose.Rotation;
import com.daasuu.mp4compose.composer.FrameBufferObjectOutputSurface;

import java.util.Map;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glViewport;

public class DecoderOutputSurface extends FrameBufferObjectOutputSurface {
    private static final String TAG = "DecoderSurface";
    private static final boolean VERBOSE = true;
    //    private EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
//    private EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
//    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private Surface surface;


    private float[] MVPMatrix = new float[16];
    private float[] STMatrix = new float[16];
    private float[] ProjMatrix = new float[16];
    private float[] MMatrix = new float[16];
    private float[] VMatrix = new float[16];



    private Rotation rotation = Rotation.NORMAL;
    private Resolution outputResolution;
    private Resolution inputResolution;
    private FillMode fillMode = FillMode.PRESERVE_ASPECT_FIT;
    private FillModeCustomItem fillModeCustomItem;
    private boolean flipVertical = false;
    private boolean flipHorizontal = false;
    private int textureID = -12345;

    private GlFilter glFilter;
    private GlFilterList filterList;
    private EFramebufferObject glFilterFrameBuffer;

    private GlPreviewFilter previewFilter;

    private boolean isNewFilter;

    /**
     * Creates an DecoderSurface using the current EGL context (rather than establishing a
     * new one).  Creates a Surface that can be passed to MediaCodec.configure().
     */
    public DecoderOutputSurface(GlFilter filter, GlFilterList filterList) {
        this.glFilter = filter;
        this.filterList = filterList;
        if (filterList != null) {
            isNewFilter = true;
        }

    }

    @Override
    protected int getOutputHeight() {
        return outputResolution.height();
    }

    @Override
    protected int getOutputWidth() {
        return outputResolution.width();
    }

    /**
     * Creates instances of TextureRender and SurfaceTexture, and a Surface associated
     * with the SurfaceTexture.
     */
    public void setup() {
        int width = outputResolution.width();
        int height = outputResolution.height();
        Log.d(TAG, "setup: width:" + width + ", height:" + height);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureID = textures[0];

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureID);
        // GL_TEXTURE_EXTERNAL_OES
        EglUtil.setupSampler(GL_TEXTURE_EXTERNAL_OES, GL_LINEAR, GL_NEAREST);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0);


        glFilterFrameBuffer = new EFramebufferObject();
        glFilterFrameBuffer.setup(outputResolution.width(), outputResolution.height());

        previewFilter = new GlPreviewFilter(GL_TEXTURE_EXTERNAL_OES);
        previewFilter.setup();

        // GL_TEXTURE_EXTERNAL_OES
        // Even if we don't access the SurfaceTexture after the constructor returns, we
        // still need to keep a reference to it.  The Surface doesn't retain a reference
        // at the Java level, so if we don't either then the object can get GCed, which
        // causes the native finalizer to run.
        if (VERBOSE) Log.d(TAG, "textureID=" + textureID);
        surfaceTexture = new SurfaceTexture(textureID);
        // This doesn't work if DecoderSurface is created on the thread that CTS started for
        // these test cases.
        //
        // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
        // create a Handler that uses it.  The "frame available" message is delivered
        // there, but since we're not a Looper-based thread we'll never see it.  For
        // this to do anything useful, DecoderSurface must be created on a thread without
        // a Looper, so that SurfaceTexture uses the main application Looper instead.
        //
        // Java language note: passing "this" out of a constructor is generally unwise,
        // but we should be able to get away with it here.
        surfaceTexture.setOnFrameAvailableListener(this);
        surface = new Surface(surfaceTexture);

        Matrix.setIdentityM(STMatrix, 0);
    }


    /**
     * Discard all resources held by this class, notably the EGL context.
     */
    public void release() {

        surface.release();

        if (filterList != null) {
            filterList.release();
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
        }
        surface = null;
        surfaceTexture = null;
    }

    /**
     * Returns the Surface that we draw onto.
     */
    public Surface getSurface() {
        return surface;
    }


    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     *
     * @param presentationTimeUs
     * @param extraTextureIds
     */
    public void onDrawFrame(EFramebufferObject fbo, long presentationTimeUs, Map<String, Integer> extraTextureIds) {

        Matrix.setIdentityM(MVPMatrix, 0);

        float scaleDirectionX = flipHorizontal ? -1 : 1;
        float scaleDirectionY = flipVertical ? -1 : 1;

        if (isNewFilter) {
            if (filterList != null) {
//                filterList.setup();
                filterList.setFrameSize(fbo.getWidth(), fbo.getHeight());
            }
            isNewFilter = false;
        }

        float scale[];
        switch (fillMode) {
            case PRESERVE_ASPECT_FIT:
                scale = FillMode.getScaleAspectFit(rotation.getRotation(), inputResolution.width(), inputResolution.height(), outputResolution.width(), outputResolution.height());
                Matrix.scaleM(MVPMatrix, 0, scale[0] * scaleDirectionX, scale[1] * scaleDirectionY, 1);
                if (rotation != Rotation.NORMAL) {
                    Matrix.rotateM(MVPMatrix, 0, -rotation.getRotation(), 0.f, 0.f, 1.f);
                }
                break;
            case PRESERVE_ASPECT_CROP:
                scale = FillMode.getScaleAspectCrop(rotation.getRotation(), inputResolution.width(), inputResolution.height(), outputResolution.width(), outputResolution.height());
                Matrix.scaleM(MVPMatrix, 0, scale[0] * scaleDirectionX, scale[1] * scaleDirectionY, 1);
                if (rotation != Rotation.NORMAL) {
                    Matrix.rotateM(MVPMatrix, 0, -rotation.getRotation(), 0.f, 0.f, 1.f);
                }
                break;
            case CUSTOM:
                if (fillModeCustomItem != null) {
                    Matrix.translateM(MVPMatrix, 0, fillModeCustomItem.getTranslateX(), -fillModeCustomItem.getTranslateY(), 0f);
                    scale = FillMode.getScaleAspectCrop(rotation.getRotation(), inputResolution.width(), inputResolution.height(), outputResolution.width(), outputResolution.height());

                    if (fillModeCustomItem.getRotate() == 0 || fillModeCustomItem.getRotate() == 180) {
                        Matrix.scaleM(MVPMatrix,
                                0,
                                fillModeCustomItem.getScale() * scale[0] * scaleDirectionX,
                                fillModeCustomItem.getScale() * scale[1] * scaleDirectionY,
                                1);
                    } else {
                        Matrix.scaleM(MVPMatrix,
                                0,
                                fillModeCustomItem.getScale() * scale[0] * (1 / fillModeCustomItem.getVideoWidth() * fillModeCustomItem.getVideoHeight()) * scaleDirectionX,
                                fillModeCustomItem.getScale() * scale[1] * (fillModeCustomItem.getVideoWidth() / fillModeCustomItem.getVideoHeight()) * scaleDirectionY,
                                1);
                    }

                    Matrix.rotateM(MVPMatrix, 0, -(rotation.getRotation() + fillModeCustomItem.getRotate()), 0.f, 0.f, 1.f);

//                    Log.d(TAG, "inputResolution = " + inputResolution.width() + " height = " + inputResolution.height());
//                    Log.d(TAG, "out = " + outputResolution.width() + " height = " + outputResolution.height());
//                    Log.d(TAG, "rotation = " + rotation.getRotation());
//                    Log.d(TAG, "scale[0] = " + scale[0] + " scale[1] = " + scale[1]);


                }
            default:
                break;
        }
        Log.d(TAG, "onDrawFrame: ...filterList:"+filterList);
        if (filterList != null) {
            glFilterFrameBuffer.enable();
            glViewport(0, 0, glFilterFrameBuffer.getWidth(), glFilterFrameBuffer.getHeight());
        }
        surfaceTexture.getTransformMatrix(STMatrix);

        // 这句绘制的目的地是哪?  --如果glFilterFrameBuffer没有启用, 那就fbo, 否则就是glFilterFrameBuffer
        previewFilter.draw(textureID, MVPMatrix, STMatrix, 1.0f);

        if (filterList != null) {
            fbo.enable();  // 重新启用了最外层的fbo , 那么glFilter的输出就到了这个fbo .
            GLES20.glClear(GL_COLOR_BUFFER_BIT);
            filterList.draw(glFilterFrameBuffer.getTexName(), fbo, presentationTimeUs, extraTextureIds);
        }
    }

    protected boolean needLastFrame() {
        if (filterList != null) {
            return filterList.needLastFrame();
        }
        return false;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }


    public void setOutputResolution(Resolution resolution) {
        this.outputResolution = resolution;
    }

    public void setFillMode(FillMode fillMode) {
        this.fillMode = fillMode;
    }

    public void setInputResolution(Resolution resolution) {
        this.inputResolution = resolution;
    }

    public void setFillModeCustomItem(FillModeCustomItem fillModeCustomItem) {
        this.fillModeCustomItem = fillModeCustomItem;
    }

    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }
}

