package com.spx.opengleffectdemo;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.daasuu.epf.filter.GlFilter;
import com.daasuu.epf.filter.GlPreviewFilter;
import com.spx.library.util.GlUtil;
import com.spx.videoclipeditviewtest.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;

public class MyPreviewFilter extends GlPreviewFilter {

    private static final String TAG = "MyPreviewFilter";
    
    private int program = 0;
    private int vPosition= 0;
    private int vCoordinate = 0;
    private int vTexture = 0;
    private int saturationLocation = 0;
    private int matrixLocation = 0;

    private FloatBuffer bPos=null;
    private FloatBuffer bCoord=null;

    private float[] sPos = new float[]{-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f};

    private float[] sCoord = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};

    private float[] mTexRotateMatrix = new float[]{1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f};

    float saturationF = 0.5f;


    public MyPreviewFilter(Context context) {
        super(GlUtil.raw(context.getResources().openRawResource(R.raw.vertex)),
                GlUtil.raw(context.getResources().openRawResource(R.raw.fragment)));
    }

    @Override
    public void setup() {
        super.setup();
        ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        bPos = bb.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);
        ByteBuffer cc = ByteBuffer.allocateDirect(sCoord.length * 4);
        cc.order(ByteOrder.nativeOrder());
        bCoord = cc.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);
    }

    public void draw(final int texName) {
        useProgram();
        checkSetUp();

//        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnableVertexAttribArray(getHandle("vPosition"));
        GLES20.glEnableVertexAttribArray(getHandle("vCoordinate"));


        GLES20.glVertexAttribPointer(getHandle("vPosition"), 2, GLES20.GL_FLOAT, false, 0, bPos);
        GLES20.glVertexAttribPointer(getHandle("vCoordinate"), 2, GLES20.GL_FLOAT, false, 0, bCoord);
        GLES20.glUniform1f(saturationLocation, saturationF);
        GLES20.glUniformMatrix4fv(getHandle("uTexRotateMatrix"), 1, false, mTexRotateMatrix, 0);

        GLES20.glActiveTexture(GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texName);
        GLES20.glUniform1i(getHandle("vTexture"), 0);

        GLES20.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(getHandle("vPosition"));
        GLES20.glDisableVertexAttribArray(getHandle("vCoordinate"));
//        GLES20.glBindBuffer(GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0);

        GLES20.glFlush();
    }

    /**
     * 因为手机物理摄像头视角是横向的, 所以需要处理一下
     */
    private void updateTextureRotationMatrix() {
        float offset = 0f;

        Log.i(TAG, String.format("OFFSET: %f", offset));

        android.opengl.Matrix.setRotateM(mTexRotateMatrix, 0, offset, 0f, 0f, 1f);

        android.opengl.Matrix.setRotateM(mTexRotateMatrix, 0, -90.0f + offset, 0f, 0f, 1f);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        updateTextureRotationMatrix();
    }
}
