package com.daasuu.epf

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.util.AttributeSet

import com.daasuu.epf.filter.GlFilter
import com.spx.egl.MPlayerView
import com.spx.library.player.mp.TextureSurfaceRenderer2

class GlPlayerView : MPlayerView {

    var listener: ((Long) -> Unit)? = null
    var uiHandler: Handler = Handler()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun getVideoRender(surface: SurfaceTexture, width: Int, height: Int): TextureSurfaceRenderer2 {
        val renderer = VideoTextureSurfaceRenderer2(context)
        renderer.setUpSurfaceTexture(surface, width, height)
        return renderer
    }


    fun setProgressListener(function: (Long) -> Unit) {
        listener = function

        uiHandler.post { checkPlayProgress() }
    }

    private fun checkPlayProgress() {
        if (!notDestroyed) {
            return
        }
        if (listener != null && mMediaPlayer != null && mMediaPlayer.isPlaying) {
            currentPostion = mMediaPlayer.currentPosition.toLong()
            listener?.invoke(currentPostion)
            uiHandler?.postDelayed({ checkPlayProgress() }, 100)
        } else {
            uiHandler?.postDelayed({ checkPlayProgress() }, 200)
        }

    }

    override fun release() {
        super.release()
        uiHandler.removeCallbacksAndMessages(null)
    }
}
