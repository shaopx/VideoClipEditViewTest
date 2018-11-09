package com.spx.library.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class GlUtil {
    private static final String TAG = "GlUtil";

    public static void printMatrix(String matrxName, final float[] mvpMatrix) {
        Log.d(TAG, "----------------"+matrxName+"-----------------");
        String s = "";
        for (int i = 0; i < mvpMatrix.length; i++) {
            s += mvpMatrix[i]+",";
        }
        Log.d(TAG, " " + s);
    }

    /**
     * 加载Assets文件夹下的图片
     *
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 加载mipmap纹理
     *
     * @param context
     * @param name
     * @return
     */
    public static int createTextureFromAssets(Context context, String name) {
        int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] != 0) {
            Bitmap bitmap = getImageFromAssetsFile(context, name);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
        if (textureHandle[0] == 0) {
//            throw new RuntimeException("Error loading texture.");
        }
        return textureHandle[0];
    }

    public static String raw(InputStream inputStream){
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int ch = inputStream.read();
            while (ch != -1) {
                outputStream.write(ch);
                ch = inputStream.read();
            }
            byte[] bytes = outputStream.toByteArray();
            String result = new String(bytes, Charset.defaultCharset());
            result = result.replace("\\r\\n", "\n");

            outputStream.close();
            inputStream.close();
            return result;
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return "";
    }
}
