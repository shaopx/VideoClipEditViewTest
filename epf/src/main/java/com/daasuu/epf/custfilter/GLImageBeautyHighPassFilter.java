package com.daasuu.epf.custfilter;

import android.opengl.GLES30;

import com.daasuu.epf.filter.GlFilter;

public class GLImageBeautyHighPassFilter extends GlFilter {
    protected static final String VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;                                   \n" +
            "attribute vec4 aPosition;                                  \n" +
            "attribute vec4 aTextureCoord;                              \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "void main() {                                              \n" +
            "    gl_Position = uMVPMatrix * aPosition;                  \n" +
            "    textureCoordinate = aTextureCoord.xy;                  \n" +
            "}                                                          \n";
    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture; // 输入原图\n" +
            "uniform sampler2D blurTexture;  // 高斯模糊图片\n" +
            "const float intensity = 24.0;   // 强光程度\n" +
            "void main() {\n" +
            "    lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    lowp vec4 blurColor = texture2D(blurTexture, textureCoordinate);\n" +
            "    // 高通滤波之后的颜色值\n" +
            "    highp vec4 highPassColor = sourceColor - blurColor;\n" +
            "    // 对应混合模式中的强光模式(color = 2.0 * color1 * color2)，对于高反差的颜色来说，color1 和color2 是同一个\n" +
            "    highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * intensity, 0.0, 1.0);\n" +
            "    highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * intensity, 0.0, 1.0);\n" +
            "    highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * intensity, 0.0, 1.0);\n" +
            "    // 输出的是把痘印等过滤掉\n" +
            "    gl_FragColor = vec4(highPassColor.rgb, 1.0);\n" +
            "}";

    private int mBlurTextureHandle;
    private int mBlurTexture;

    public GLImageBeautyHighPassFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBeautyHighPassFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public void setBlurTexture(int texture) {
        mBlurTexture = texture;
    }

    @Override
    protected void onDraw() {
        super.onDraw();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture( GLES30.GL_TEXTURE_2D, mBlurTexture);
        GLES30.glUniform1i(getHandle("blurTexture"), 1);
    }
}
