package com.daasuu.epf.filter;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import com.daasuu.epf.EglUtil;
import com.daasuu.epf.EFramebufferObject;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUseProgram;
import static com.spx.library.util.GlUtil.raw;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class GlFilter {

    public static final String DEFAULT_UNIFORM_SAMPLER = "sTexture";


    protected static final String DEFAULT_VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "gl_Position = aPosition;\n" +
                    "vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

//    protected static final String VERTEX_SHADER = DEFAULT_VERTEX_SHADER;

    protected static final String DEFAULT_FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";


    private static final float[] VERTICES_DATA = new float[]{
            // X, Y, Z, U, V
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f
    };
    private static final String TAG = "glfilter";

//    private static final float[] VERTICES_DATA = new float[]{
//            // X, Y, Z, U, V
//            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
//            1.0f, -1.0f, 0.0f, 1.0f, 0.0f
//    };

    private static HashMap<String, String> nickNames = new HashMap<>();

    static {
        nickNames.put("sTexture", "inputTexture");
    }

    private static final int FLOAT_SIZE_BYTES = 4;
    protected static final int VERTICES_DATA_POS_SIZE = 3;
    protected static final int VERTICES_DATA_UV_SIZE = 2;
    protected static final int VERTICES_DATA_STRIDE_BYTES = (VERTICES_DATA_POS_SIZE + VERTICES_DATA_UV_SIZE) * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_POS_OFFSET = 0 * FLOAT_SIZE_BYTES;
    protected static final int VERTICES_DATA_UV_OFFSET = VERTICES_DATA_POS_OFFSET + VERTICES_DATA_POS_SIZE * FLOAT_SIZE_BYTES;

    private final String vertexShaderSource;
    private final String fragmentShaderSource;
    protected Context mContext;
    protected boolean hasSetUp = false;

    protected int mProgramHandle;
    protected int mMVPMatrixHandle;
    private int vertexShader;
    private int fragmentShader;

    private int vertexBufferName;

    protected final HashMap<String, Integer> handleMap = new HashMap<String, Integer>();

    // 变换矩阵
    protected float[] mMVPMatrix = new float[16];

    protected int mWidth, mHeight;

    public GlFilter() {
        this(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
    }

    public GlFilter(final Context context, final int vertexShaderSourceResId, final int fragmentShaderSourceResId) {
        this(context, raw(context.getResources().openRawResource(vertexShaderSourceResId)),
                raw(context.getResources().openRawResource(fragmentShaderSourceResId)));
    }

//    public GlFilter(final Resources res, final int vertexShaderSourceResId, final int fragmentShaderSourceResId) {
//        this(res.getString(vertexShaderSourceResId), res.getString(fragmentShaderSourceResId));
//    }
    public GlFilter(final Context context,final String vertexShaderSource, final String fragmentShaderSource) {
        this.mContext = context;
        this.vertexShaderSource = vertexShaderSource;
        this.fragmentShaderSource = fragmentShaderSource;
    }

    public GlFilter(final String vertexShaderSource, final String fragmentShaderSource) {
        this(null, vertexShaderSource, fragmentShaderSource);
    }

    public String getName(){
        return this.getClass().getSimpleName();
    }

    public FilterType getType(){
        return this.getFilterType();
    }

    public FilterType getFilterType() {
        return FilterType.DEFAULT;
    }

    public void initProgramHandle() {

    }

    public void setup() {
        release();
        EglUtil.checkEglError("setup() release");
        vertexShader = EglUtil.loadShader(vertexShaderSource, GL_VERTEX_SHADER);
        EglUtil.checkEglError("setup() load vertex Shader");

        Log.d(TAG, "setup: "+getName()+", vertexShader:"+vertexShader);
        fragmentShader = EglUtil.loadShader(fragmentShaderSource, GL_FRAGMENT_SHADER);
        EglUtil.checkEglError("setup() load fragment Shader");

        Log.d(TAG, "setup: "+getName()+", fragmentShader:"+fragmentShader);
        mProgramHandle = EglUtil.createProgram(vertexShader, fragmentShader);
        Log.d(TAG, "setup: "+getName()+", mProgramHandle:"+mProgramHandle);
        vertexBufferName = EglUtil.createBuffer(VERTICES_DATA);

        initProgramHandle();

        hasSetUp = true;
    }

    public void onDrawFrameBegin() {
        // 绑定总变换矩阵
        Matrix.setIdentityM(mMVPMatrix, 0);
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    public int getTextureType() {
        return GLES30.GL_TEXTURE_2D;
    }

    public void setFrameSize(final int width, final int height) {
        mWidth = width;
        mHeight = height;
    }
    public void onDisplaySizeChanged(final int width, final int height) {
        setFrameSize(width, height);
    }
    public void onInputSizeChanged(final int width, final int height){

    }


    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = 0;
        GLES20.glDeleteShader(vertexShader);
        vertexShader = 0;
        GLES20.glDeleteShader(fragmentShader);
        fragmentShader = 0;
        GLES20.glDeleteBuffers(1, new int[]{vertexBufferName}, 0);
        vertexBufferName = 0;

        handleMap.clear();
    }
    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }
    public int draw(final int sourceTextId, final EFramebufferObject fbo, Map<String,Integer> extraTextureIds) {
        checkSetUp();
        useProgram();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferName);
        GLES20.glEnableVertexAttribArray(getHandle("aPosition"));
        GLES20.glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        GLES20.glEnableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glVertexAttribPointer(getHandle("aTextureCoord"), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sourceTextId);
        GLES20.glUniform1i(getHandle("sTexture"), 0);
        onDrawFrameBegin();
        onDraw();
        onDraw(extraTextureIds);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(getHandle("aPosition"));
        GLES20.glDisableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        if (fbo != null) {
            return fbo.getTexName();
        }
        return -1;
    }

    protected void checkSetUp() {
        if(!hasSetUp){
            setup();
        }
    }

    protected void onDraw() {
    }
    protected void onDraw( Map<String,Integer> extraTextureIds) {
    }

    protected final void useProgram() {
//        Log.d(TAG, "useProgram: mProgramHandle:"+mProgramHandle);
        glUseProgram(mProgramHandle);
    }

    protected final int getVertexBufferName() {
        return vertexBufferName;
    }

    protected final int getHandle(final String name) {
        final Integer value = handleMap.get(name);
        if (value != null) {
            return value.intValue();
        }

        int location = glGetAttribLocation(mProgramHandle, name);
        if (location == -1) {
            location = glGetUniformLocation(mProgramHandle, name);
        }
        if (location == -1 && nickNames.containsKey(name)) {
            String nickName = nickNames.get(name);
            return getHandle(nickName);
        }
        if (location == -1) {
            throw new IllegalStateException("Could not get attrib or uniform location for " + name);
        }
        handleMap.put(name, Integer.valueOf(location));
        return location;
    }

    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        return draw(textureId, null, null);
    }

    public void initFrameBuffer(final int width, final int height){

    }

    public void destroyFrameBuffer(){
    }

    protected static final String VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;                                   \n" +
            "attribute vec4 aPosition;                                  \n" +
            "attribute vec4 aTextureCoord;                              \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "void main() {                                              \n" +
            "    gl_Position = uMVPMatrix * aPosition;                  \n" +
            "    textureCoordinate = aTextureCoord.xy;                  \n" +
            "}                                                          \n";

    public boolean needLastFrame() {
        return false;
    }
}
