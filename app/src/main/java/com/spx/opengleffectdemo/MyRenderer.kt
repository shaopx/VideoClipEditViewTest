package com.spx.opengleffectdemo


import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 渲染逻辑都在这
 */
class MyRenderer : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    var TAG = "MyRenderer"

    interface RenderCallback {
        fun onRenderCreated(surfaceTexture: SurfaceTexture, width:Int, height:Int)
    }

    var renderCallback: RenderCallback? = null
    private var program: Int = 0
    private var vPosition: Int = 0
    private var vCoordinate: Int = 0
    private var vTexture: Int = 0
    private var saturationLocation: Int = 0
    private var matrixLocation: Int = 0

    private val bPos: FloatBuffer
    private val bCoord: FloatBuffer

    private val sPos = floatArrayOf(-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f)

    private val sCoord = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)

    private val mTexRotateMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

    var saturationF = 0.5f

    init {
        val bb = ByteBuffer.allocateDirect(sPos.size * 4)
        bb.order(ByteOrder.nativeOrder())
        bPos = bb.asFloatBuffer()
        bPos.put(sPos)
        bPos.position(0)
        val cc = ByteBuffer.allocateDirect(sCoord.size * 4)
        cc.order(ByteOrder.nativeOrder())
        bCoord = cc.asFloatBuffer()
        bCoord.put(sCoord)
        bCoord.position(0)
    }


    lateinit var vertexShaderSource: String
    lateinit var fragmentShaderSource: String


    lateinit var mSurfaceTexture: SurfaceTexture
    private var hTex = IntArray(1)
    private var mUpdateSurfaceTexture = false

    private fun initTex() {
        GLES20.glGenTextures(1, hTex, 0)
        checkGlError("glGenTextures")
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
    }

    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    lateinit var mSurfaceView: GLSurfaceView
    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        Log.d(TAG, "onFrameAvailable() ...")
        mUpdateSurfaceTexture = true
        mSurfaceView.requestRender()
    }

    /**
     */
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        initTex()
        mSurfaceTexture = SurfaceTexture(hTex[0])
        mSurfaceTexture.setDefaultBufferSize(mSurfaceView.width, mSurfaceView.height)
        mSurfaceTexture.setOnFrameAvailableListener(this)
        Log.d(TAG, "onSurfaceCreated: width:" + mSurfaceView.width + ", height:" + mSurfaceView.height)
        renderCallback?.onRenderCreated(mSurfaceTexture, mSurfaceView.width, mSurfaceView.height)

        // Create a minimum supported OpenGL ES context, then check:
        gl10.glGetString(GL10.GL_VERSION).also {
            Log.w(TAG, "onSurfaceCreated Version: $it")
        }

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
//        GLES30.glEnable(GLES30.GL_TEXTURE_2D)  //!!!!!这句不要留

        program = ShaderUtil.createProgram(vertexShaderSource, fragmentShaderSource)
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        vPosition = GLES30.glGetAttribLocation(program, "vPosition")
        vCoordinate = GLES30.glGetAttribLocation(program, "vCoordinate")
        vTexture = GLES30.glGetUniformLocation(program, "vTexture")
        saturationLocation = GLES30.glGetUniformLocation(program, "saturation")
        matrixLocation = GLES20.glGetUniformLocation(program, "uTexRotateMatrix")

        // 使用某套shader程序
        GLES30.glUseProgram(program)

        // 允许顶点位置数据数组
        GLES30.glEnableVertexAttribArray(vPosition)
        GLES30.glEnableVertexAttribArray(vCoordinate)

//        GLES30.glEnableVertexAttribArray(saturationLocation)
//        GLES30.glEnableVertexAttribArray(matrixLocation)

        GLES30.glUniform1i(vTexture, 0)

        // 好像目前也不需要glActiveTexture
//        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
//        checkGlError("glActiveTexture")

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0])
        checkGlError("glBindTexture")
    }

    /**
     * 当GLSurfaceView中的Surface被改变的时候回调此方法(一般是大小变化)
     *
     * @param gl10   同onSurfaceCreated()
     * @param width  Surface的宽度
     * @param height Surface的高度
     */
    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        // 设置绘图的窗口(可以理解成在画布上划出一块区域来画图)
        GLES30.glViewport(0, 0, width, height)
        updateTextureRotationMatrix()
    }


    /**
     * 当Surface需要绘制的时候回调此方法
     * 根据GLSurfaceView.setRenderMode()设置的渲染模式不同回调的策略也不同：
     * GLSurfaceView.RENDERMODE_CONTINUOUSLY : 固定一秒回调60次(60fps)
     * GLSurfaceView.RENDERMODE_WHEN_DIRTY   : 当调用GLSurfaceView.requestRender()之后回调一次
     *
     * @param gl10 同onSurfaceCreated()
     */
    override fun onDrawFrame(gl10: GL10) {
        Log.d(TAG, "onDrawFrame ...")
        // 清屏
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)

        /**
         * 这里需要注意
         */
        synchronized(this) {
            if (mUpdateSurfaceTexture) {
                mSurfaceTexture.updateTexImage()
                mUpdateSurfaceTexture = false
            }
        }


        GLES30.glVertexAttribPointer(vPosition, 2, GLES30.GL_FLOAT, false, 0, bPos)
        GLES30.glVertexAttribPointer(vCoordinate, 2, GLES30.GL_FLOAT, false, 0, bCoord)
        GLES30.glUniform1f(saturationLocation, saturationF)
        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, mTexRotateMatrix, 0)



        GLES30.glUniform1i(vTexture, 0)
        checkGlError("glUniform1i")

        // 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glFlush()
    }

    /**
     * 因为手机物理摄像头视角是横向的, 所以需要处理一下
     */
    private fun updateTextureRotationMatrix() {
        var offset = 0f

        Log.i(TAG, String.format("OFFSET: %f", offset))

        android.opengl.Matrix.setRotateM(mTexRotateMatrix, 0, offset, 0f, 0f, 1f)

        if (mSurfaceView.resources.configuration.orientation === Configuration.ORIENTATION_PORTRAIT) {
            android.opengl.Matrix.setRotateM(mTexRotateMatrix, 0, -90.0f + offset, 0f, 0f, 1f)
            Log.i(TAG, String.format("rotate: 0, %f x, 0.f, 0f, 1f", 90.0f + offset))
            //Matrix.scaleM(mTexRotateMatrix, 0, mTexRotateMatrix, 0, 1, -1, 1f);
        } else {
            // Matrix.setRotateM(mTexRotateMatrix, 0, offset, 0f, 0f, 1f);
            Log.i(TAG, String.format("rotate: 0, %f x, 0.f, 0f, 1f", offset))
        }

    }
}

