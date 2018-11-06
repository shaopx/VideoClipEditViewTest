package com.daasuu.epf;


import android.content.Context;
import android.graphics.*;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.daasuu.epf.filter.GlFilter;
import com.spx.library.player.mp.TextureSurfaceRenderer2;
import com.spx.library.util.RawResourceReader;
import com.spx.library.util.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

public class VideoTextureSurfaceRenderer2 extends TextureSurfaceRenderer2 implements
        SurfaceTexture.OnFrameAvailableListener {

    public static final String TAG = "Renderer2";

    /**
     *
     */
    private static float squareSize = 1.0f;
    private static float squareCoords[] = {
            -squareSize, squareSize,   // top left
            -squareSize, -squareSize,   // bottom left
            squareSize, -squareSize,    // bottom right
            squareSize, squareSize}; // top right

    private static short drawOrder[] = {0, 1, 2, 0, 2, 3};

    private Context context;

    // Texture to be shown in backgrund
    private FloatBuffer textureBuffer;
    private float textureCoords[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f};
    private int[] textures = new int[1];

    private int shaderProgram;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    private SurfaceTexture videoTexture;
    private float[] videoTextureTransform;
    private boolean frameAvailable = false;

    int textureParamHandle;
    int textureCoordinateHandle;
    int positionHandle;
    int textureTranformHandle;

    private EFramebufferObject framebufferObject;
    private GlFilter normalShader;

    public VideoTextureSurfaceRenderer2(Context context) {
        this.context = context;
        videoTextureTransform = new float[16];
    }

    private void setupGraphics() {
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(context, R.raw.vetext_sharder);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(context, R.raw.fragment_sharder);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        shaderProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"texture", "vPosition", "vTexCoordinate", "textureTransform"});

    }

    private void setupVertexBuffer() {
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }

    private void setupTexture() {
        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("Texture generate");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        checkGlError("Texture bind");

        videoTexture = new SurfaceTexture(textures[0]);
        videoTexture.setOnFrameAvailableListener(this);

//        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.image1);
    }

    int count = 0;
    @Override
    protected boolean draw() {
        synchronized (this) {
            if (frameAvailable) {
                videoTexture.updateTexImage();
                videoTexture.getTransformMatrix(videoTextureTransform);
                frameAvailable = false;
            } else {
                return false;
            }

        }



        framebufferObject.enable();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mWidth, mHeight);
        Log.d(TAG, "draw: textures[0]:"+textures[0] +", fbo.texture:"+framebufferObject.getTexName()+", normalShader:"+normalShader);
        this.drawTexture();

//        // 在最外层, 最终把输出从屏幕输出
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Log.d(TAG, "draw: textures[0]:"+textures[0] +", fbo.texture:"+framebufferObject.getTexName());
//        this.drawTexture();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());
//
        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        normalShader.draw(framebufferObject.getTexName(), null, null);
        checkGlError("Texture generate!!!!!!!!!!!!");

//        this.drawTexture();
        count++;
        return true;
    }

//    private int createTexture(){
//        int[] texture = new int[1];
//        if (mBitmap != null && !mBitmap.isRecycled()) {
//            //生成纹理
//            GLES30.glGenTextures(1, texture, 0);
//
//            // 要对texture[0]这个纹理进行初始化, 先把它对应到GL_TEXTURE_2D上,
//            // 然后调用后面的四句, 这四句调用看起来跟texture[0]没关系, 但是因为有了最开始的对应关系, 所以实际上是对texture[0]进行初始化
//            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
//            //根据以上指定的参数，生成一个2D纹理
//            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0);
//            return texture[0];
//        }
//        return 0;
//    }

    private void drawTexture() {
        // Draw texture


        GLES20.glUseProgram(shaderProgram);
        textureParamHandle = GLES20.glGetUniformLocation(shaderProgram, "texture");
        textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinate");
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        textureTranformHandle = GLES20.glGetUniformLocation(shaderProgram, "textureTransform");

        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("aaaa0000->"+textureParamHandle);
        GLES20.glUniform1i(textureParamHandle, 0);
        checkGlError("aaaa33"+textureParamHandle);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        checkGlError("aaaa1111");
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer);
        checkGlError("aaaa2222");
        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0);
        checkGlError("aaaa4444");
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
        checkGlError("aaaa5555");
    }


    @Override
    protected void initGLComponents() {
        setupVertexBuffer();
        setupTexture();
        setupGraphics();

        framebufferObject = new EFramebufferObject();
        normalShader = new GlFilter();
        normalShader.setup();

        framebufferObject.setup(mWidth, mHeight);
        normalShader.setFrameSize(mWidth, mHeight);

//        bitmapTexture = createTexture();
    }

    @Override
    protected void deinitGLComponents() {
        GLES20.glDeleteTextures(1, textures, 0);
        GLES20.glDeleteProgram(shaderProgram);
        videoTexture.release();
        videoTexture.setOnFrameAvailableListener(null);
    }


    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    @Override
    public SurfaceTexture getVideoTexture() {
        return videoTexture;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onFrameAvailable: ....");
        synchronized (this) {
            frameAvailable = true;
        }
    }
}

