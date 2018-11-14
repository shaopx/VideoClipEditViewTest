package com.spx.egl;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.daasuu.epf.filter.FilterType;
import com.daasuu.epf.filter.GlFilter;
import com.spx.library.util.GlUtil;

/**
 * 美肤滤镜
 */
public class GLImageComplexionBeautyFilter extends GlFilter {
    static String TAG ="GLImageComplexion";
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
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputTexture; // 图像texture\n" +
            "uniform sampler2D grayTexture;  // 灰度查找表\n" +
            "uniform sampler2D lookupTexture; // LUT\n" +
            "\n" +
            "uniform highp float levelRangeInv; // 范围\n" +
            "uniform lowp float levelBlack; // 灰度level \n" +
            "uniform lowp float alpha; // 肤色成都 \n" +
            "\n" +
            "void main() {\n" +
            "    lowp vec3 textureColor = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "\n" +
            "    textureColor = clamp((textureColor - vec3(levelBlack, levelBlack, levelBlack)) * levelRangeInv, 0.0, 1.0);\n" +
            "    textureColor.r = texture2D(grayTexture, vec2(textureColor.r, 0.5)).r;\n" +
            "    textureColor.g = texture2D(grayTexture, vec2(textureColor.g, 0.5)).g;\n" +
            "    textureColor.b = texture2D(grayTexture, vec2(textureColor.b, 0.5)).b;\n" +
            "\n" +
            "    mediump float blueColor = textureColor.b * 15.0;\n" +
            "\n" +
            "    mediump vec2 quad1;\n" +
            "    quad1.y = floor(blueColor / 4.0);\n" +
            "    quad1.x = floor(blueColor) - (quad1.y * 4.0);\n" +
            "\n" +
            "    mediump vec2 quad2;\n" +
            "    quad2.y = floor(ceil(blueColor) / 4.0);\n" +
            "    quad2.x = ceil(blueColor) - (quad2.y * 4.0);\n" +
            "\n" +
            "    highp vec2 texPos1;\n" +
            "    texPos1.x = (quad1.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);\n" +
            "    texPos1.y = (quad1.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);\n" +
            "\n" +
            "    highp vec2 texPos2;\n" +
            "    texPos2.x = (quad2.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);\n" +
            "    texPos2.y = (quad2.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);\n" +

            "    lowp vec4 newColor1 = texture2D(lookupTexture, texPos1);\n" +
            "    lowp vec4 newColor2 = texture2D(lookupTexture, texPos2);\n" +
            "\n" +
            "    lowp vec3 newColor = mix(newColor1.rgb, newColor2.rgb, fract(blueColor));\n" +
            "\n" +
            "    textureColor = mix(textureColor, newColor, alpha);\n" +
            "\n" +
            "    gl_FragColor = vec4(textureColor, 1.0); \n" +
            "}";

    private int grayTextureLoc;
    private int lookupTextureLoc;

    private int levelRangeInvLoc;
    private int levelBlackLoc;
    private int alphaLoc;

    private int mGrayTexture;
    private int mLookupTexture;

    private float levelRangeInv;
    private float levelBlack;
    private float alpha;


    public GLImageComplexionBeautyFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageComplexionBeautyFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.BEAUTY_CUS;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        grayTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "grayTexture");
        lookupTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "lookupTexture");
        levelRangeInvLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelRangeInv");
        levelBlackLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelBlack");
        alphaLoc = GLES30.glGetUniformLocation(mProgramHandle, "alpha");

        levelRangeInv = 1.040816f;
        levelBlack = 0.01960784f;
        alpha = 1.0f;

        createTexture();
    }

    private void createTexture() {
        Log.d(TAG, "createTexture: ....");
        mGrayTexture = GlUtil.createTextureFromAssets(mContext, "filters/skin_gray.png");
        mLookupTexture = GlUtil.createTextureFromAssets(mContext, "filters/skin_lookup.png");
//        mLookupTexture = OpenGLUtils.createTextureFromAssets(mContext, "filters/filter_langman.png");
        Log.d(TAG, "createTexture: ....mGrayTexture:"+mGrayTexture+",mLookupTexture:"+mLookupTexture);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();

        Log.d(TAG, "onDrawFrameBegin: ....mGrayTexture:"+mGrayTexture+",mLookupTexture:"+mLookupTexture);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mGrayTexture);
        GLES30.glUniform1i(grayTextureLoc, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mLookupTexture);
        GLES30.glUniform1i(lookupTextureLoc, 2);

        GLES30.glUniform1f(levelRangeInvLoc, levelRangeInv);
        GLES30.glUniform1f(levelBlackLoc, levelBlack);
        GLES30.glUniform1f(alphaLoc, alpha);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(2, new int[]{ mGrayTexture, mLookupTexture }, 0);
    }

    /**
     * 美肤程度
     * @param level 0 ~ 1.0f
     */
    public void setComplexionLevel(float level) {
        alpha = level;
    }

}
