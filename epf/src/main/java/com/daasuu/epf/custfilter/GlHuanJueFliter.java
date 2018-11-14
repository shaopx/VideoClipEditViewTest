package com.daasuu.epf.custfilter;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.daasuu.epf.R;
import com.daasuu.epf.filter.FilterType;
import com.daasuu.epf.filter.GlFilter;
import com.spx.library.util.GlUtil;

import java.util.Map;

public class GlHuanJueFliter extends GlFilter {
    private static final String TAG = "GlHuanJueFliter";

    private int mLutTexture;
    private int uTexture1Handle;
    private int uTexture2Handle;
    private static final String DIR_NAME = "filters/";
    private String pngResName = "lookup_vertigo";

    public GlHuanJueFliter(Context context) {
        super(context, R.raw.def_vertext, R.raw.fragment_canying);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SPX_LUCION;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        uTexture1Handle = GLES30.glGetUniformLocation(mProgramHandle, "uTexture1");
        uTexture2Handle = GLES30.glGetUniformLocation(mProgramHandle, "uTexture2");
        createTexture();
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        mLutTexture = GlUtil.createTextureFromAssets(mContext,
                DIR_NAME + pngResName + ".png");
    }

    @Override
    public void onDraw(Map<String, Integer> extraTextureIds) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(getTextureType(), mLutTexture);
        GLES30.glUniform1i(uTexture2Handle, 3);


        if (extraTextureIds.containsKey("last_frame_texture")) {
            Integer last_frame_texture = extraTextureIds.get("last_frame_texture");
            Log.d(TAG, "onDraw: last_frame_texture:" + last_frame_texture);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, last_frame_texture);
            GLES30.glUniform1i(uTexture1Handle, 4);
        }


    }

    public boolean needLastFrame() {
        return true;
    }
}