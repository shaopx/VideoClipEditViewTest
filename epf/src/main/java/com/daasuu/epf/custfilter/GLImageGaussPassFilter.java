package com.daasuu.epf.custfilter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.daasuu.epf.filter.GlFilter;

import java.util.Map;

public class GLImageGaussPassFilter extends GlFilter {
    private static final String VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "\n" +
            "// 高斯算子左右偏移值，当偏移值为5时，高斯算子为 11 x 11\n" +
            "const int SHIFT_SIZE = 5;\n" +
            "\n" +
            "uniform highp float texelWidthOffset;\n" +
            "uniform highp float texelHeightOffset;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec4 blurShiftCoordinates[SHIFT_SIZE];\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    textureCoordinate = aTextureCoord.xy;\n" +
            "    // 偏移步距\n" +
            "    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
            "    // 记录偏移坐标\n" +
            "    for (int i = 0; i < SHIFT_SIZE; i++) {\n" +
            "        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,\n" +
            "                                       textureCoordinate.xy + float(i + 1) * singleStepOffset);\n" +
            "    }\n" +
            "}";

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D sTexture;\n" +
            "const int SHIFT_SIZE = 5; // 高斯算子左右偏移值\n" +
            "varying vec4 blurShiftCoordinates[SHIFT_SIZE];\n" +
            "void main() {\n" +
            "    // 计算当前坐标的颜色值\n" +
            "    vec4 currentColor = texture2D(sTexture, textureCoordinate);\n" +
            "    mediump vec3 sum = currentColor.rgb;\n" +
            "    // 计算偏移坐标的颜色值总和\n" +
            "    for (int i = 0; i < SHIFT_SIZE; i++) {\n" +
            "        sum += texture2D(sTexture, blurShiftCoordinates[i].xy).rgb;\n" +
            "        sum += texture2D(sTexture, blurShiftCoordinates[i].zw).rgb;\n" +
            "    }\n" +
            "    // 求出平均值\n" +
            "    gl_FragColor = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);\n" +
            "}";
    private static final String TAG = "GLImageGaussPassFilter";

    private int mType;
    private float mTexelWidth;
    private float mTexelHeight;

    // 变换矩阵
    protected float[] mMVPMatrix = new float[16];
    protected int mMVPMatrixHandle;

    public GLImageGaussPassFilter(int type) {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
        this.mType = type;
    }

    public GLImageGaussPassFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public String getName() {
        return super.getName() + "_" + mType;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        Log.d(TAG, "setFrameSize: width:" + width + ", height:" + height);
        this.mTexelWidth = width;
        this.mTexelHeight = height;
    }

    @Override
    public void onDraw(Map<String, Integer> extraTextureIds) {
        Log.d(TAG, "onDraw: extraTextureIds:" + extraTextureIds);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Log.d(TAG, "onDraw: mTexelWidth:" + mTexelWidth + ", mTexelHeight:" + mTexelHeight);
        GLES30.glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, mMVPMatrix, 0);

        if (mType == 0) {
            GLES20.glUniform1f(getHandle("texelWidthOffset"), 1.0f / 320f);
            GLES20.glUniform1f(getHandle("texelHeightOffset"), 0.0f);
        } else {
            GLES20.glUniform1f(getHandle("texelWidthOffset"), 0.0f);
            GLES20.glUniform1f(getHandle("texelHeightOffset"), 1.0f / 640f);
        }

    }
}
