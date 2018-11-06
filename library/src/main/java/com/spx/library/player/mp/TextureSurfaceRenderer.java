package com.spx.library.player.mp;


import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLUtils;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;


/**
 * 绘制前，renderer的配置，初始化EGL，开始一个绘制线程.
 * 这个类需要子类去实现相应的绘制工作.
 *
 * 具体流程可以参考http://www.cnblogs.com/kiffa/archive/2013/02/21/2921123.html
 * 相应的函数可以查看： https://www.khronos.org/registry/egl/sdk/docs/man/
 */
public abstract class TextureSurfaceRenderer implements Runnable{
    public static String LOG_TAG = TextureSurfaceRenderer.class.getSimpleName();

    protected SurfaceTexture surfaceTexture = null;
    protected int mWidth;
    protected int mHeight;

    private EGL10 egl;
    private EGLContext eglContext;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;


    /***
     * 是否正在绘制(draw)
     */
    private volatile boolean running = false;

    public TextureSurfaceRenderer() {

    }

    public void setUpSurfaceTexture(SurfaceTexture surfaceTexture, int width, int height){
        this.surfaceTexture = surfaceTexture;
        Log.e("TAG", "surfaceTexture obj="+ surfaceTexture.toString());
        this.mWidth = width;
        this.mHeight = height;
        this.running = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        initEGL();
        initGLComponents();
        Log.d(LOG_TAG, "OpenGL init OK. start draw...");

        onSurfaceCreated(null, null);

        while (running) {
            if (onDrawFrame()) {
                egl.eglSwapBuffers(eglDisplay, eglSurface);
            }
        }

        deinitGLComponents();
        deinitEGL();
    }

    private void initEGL() {
        egl = (EGL10)EGLContext.getEGL();
        //获取显示设备
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //version中存放EGL 版本号，int[0]为主版本号，int[1]为子版本号
        int version[] = new int[2];
        egl.eglInitialize(eglDisplay, version);

        EGLConfig eglConfig = chooseEglConfig();
        //创建EGL 的window surface 并且返回它的handles(eslSurface)
        eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null);

        eglContext = createContext(egl, eglDisplay, eglConfig);

        //设置当前的渲染环境
        try {
            if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
                throw new RuntimeException("GL error:" + GLUtils.getEGLErrorString(egl.eglGetError()));
            }
            if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                throw new RuntimeException("GL Make current Error"+ GLUtils.getEGLErrorString(egl.eglGetError()));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deinitEGL() {
        egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(eglDisplay, eglSurface);
        egl.eglDestroyContext(eglDisplay, eglContext);
        egl.eglTerminate(eglDisplay);
        Log.d(LOG_TAG, "OpenGL deinit OK.");
    }

    /**
     * 主要的绘制函数， 需在子类中去实现绘制
     */
    protected abstract boolean onDrawFrame();

    /***
     * 初始化opengl的一些组件比如vertextBuffer,sharders,textures等，
     * 通常在Opengl context 初始化以后被调用，需要子类去实现
     */
    protected abstract void initGLComponents();
    protected abstract void deinitGLComponents();

    public abstract SurfaceTexture getVideoTexture();

    /**
     * 为当前渲染的API创建一个渲染上下文
     * @return a handle to the context
     */
    private EGLContext createContext(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig) {
        int[] attrs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        return egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrs);
    }

    /***
     *  refer to https://www.khronos.org/registry/egl/sdk/docs/man/
     * @return a EGL frame buffer configurations that match specified attributes
     */
    private EGLConfig chooseEglConfig() {
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] attributes = getAttributes();
        int confSize = 1;

        if (!egl.eglChooseConfig(eglDisplay, attributes, configs, confSize, configsCount)) {    //获取满足attributes的config个数
            throw new IllegalArgumentException("Failed to choose config:"+ GLUtils.getEGLErrorString(egl.eglGetError()));
        }
        else if (configsCount[0] > 0) {
            return configs[0];
        }

        return null;
    }

    /**
     * 构造绘制需要的特性列表,ARGB,DEPTH...
     */
    private int[] getAttributes()
    {
        return new int[] {
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,  //指定渲染api类别
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 16,			/*default depth buffer 16 choose a RGB_888 surface */
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE      //总是以EGL10.EGL_NONE结尾
        };
    }

    /**
     * Call when activity pauses. This stops the rendering thread and deinitializes OpenGL.
     */
    public void onPause()
    {
        running = false;
    }

    @Override
    protected  void finalize() throws Throwable {
        super.finalize();
        running = false;
    }

    public abstract void onSurfaceChanged(int width, int height);

    public abstract void onSurfaceCreated(final GL10 gl, final EGLConfig config);
}
