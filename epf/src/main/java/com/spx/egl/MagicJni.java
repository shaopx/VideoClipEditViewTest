package com.spx.egl;

public class MagicJni {
    static {
        System.loadLibrary("MagicBeautify");
    }

    public static native void glReadPixels(int x, int y, int width, int height, int format, int type);
}
