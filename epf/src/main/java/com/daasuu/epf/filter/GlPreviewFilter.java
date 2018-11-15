package com.daasuu.epf.filter;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.spx.library.util.GlUtil;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class GlPreviewFilter extends GlFilter {

    public static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private static final String TAG = "GlPreviewFilter";

    private float[] MVPMatrix = new float[16];
    private float[] ProjMatrix = new float[16];
    private float[] MMatrix = new float[16];
    private float[] VMatrix = new float[16];
    private float[] STMatrix = new float[16];
    private float aspectRatio = 1f;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "uniform float uCRatio;\n" +

                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +

                    "void main() {\n" +
                    "vec4 scaledPos = aPosition;\n" +
                    "scaledPos.x = scaledPos.x * uCRatio;\n" +
                    "gl_Position = uMVPMatrix * scaledPos;\n" +
                    "vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    public int texTarget = GL_TEXTURE_EXTERNAL_OES;

    public GlPreviewFilter(final int texTarget) {
        this(VERTEX_SHADER, createFragmentShaderSourceOESIfNeed(texTarget));
        this.texTarget = texTarget;
    }

    public GlPreviewFilter(final String vertexShaderSource, final String fragmentShaderSource) {
        super(vertexShaderSource, fragmentShaderSource);
    }


    public void setup() {
        super.setup();

        Matrix.setIdentityM(STMatrix, 0);

        Matrix.setLookAtM(VMatrix, 0,
                0.0f, 0.0f, 5.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );
    }

    private static String createFragmentShaderSourceOESIfNeed(final int texTarget) {
        if (texTarget == GL_TEXTURE_EXTERNAL_OES) {
            return new StringBuilder()
                    .append("#extension GL_OES_EGL_image_external : require\n")
                    .append(DEFAULT_FRAGMENT_SHADER.replace("sampler2D", "samplerExternalOES"))
                    .toString();
        }
        return DEFAULT_FRAGMENT_SHADER;
    }

    public void draw(final int texName) {

        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);

        draw(texName, MVPMatrix, STMatrix, aspectRatio);
    }

    public void draw(final int texName, final float[] mvpMatrix, final float[] stMatrix, final float aspectRatio) {
        useProgram();
        GlUtil.printMatrix("mvpMatrix", mvpMatrix);
        GlUtil.printMatrix("stMatrix", stMatrix);
        GLES20.glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(getHandle("uSTMatrix"), 1, false, stMatrix, 0);
        GLES20.glUniform1f(getHandle("uCRatio"), aspectRatio);

        GLES20.glBindBuffer(GL_ARRAY_BUFFER, getVertexBufferName());
        GLES20.glEnableVertexAttribArray(getHandle("aPosition"));
        GLES20.glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        GLES20.glEnableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glVertexAttribPointer(getHandle("aTextureCoord"), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

        GLES20.glActiveTexture(GL_TEXTURE0);
        GLES20.glBindTexture(texTarget, texName);
        GLES20.glUniform1i(getHandle(DEFAULT_UNIFORM_SAMPLER), 0);

        GLES20.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(getHandle("aPosition"));
        GLES20.glDisableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glBindBuffer(GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0);

    }

    public void onSurfaceChanged(int width, int height) {
        aspectRatio = (float) width / height;
        Matrix.frustumM(ProjMatrix, 0, -aspectRatio, aspectRatio, -1, 1, 5, 7);
        Matrix.setIdentityM(MMatrix, 0);
    }

    public void updateTransform(SurfaceTexture previewTexture) {
        previewTexture.getTransformMatrix(STMatrix);
    }


}
