package com.spx.videoclipeditviewtest

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.spx.videoclipeditviewtest.util.getVideoDuration
import java.util.*


class ThumbExoPlayerView(context: Context?, attrs: AttributeSet?) : PlayerView(context, attrs) {
    companion object {
        const val CHECK_INTERVAL_MS:Long = 20
        const val TAG = "ThumbExoPlayerView"
    }

    private lateinit var mediaPath: String
    private lateinit var textureView: TextureView
    private var callback: ((Bitmap, Int) -> Boolean)? = null
    private var bitmapIndex = 0
    private val thumbnailMillSecList = ArrayList<Long>()
    private var exoPlayer: SimpleExoPlayer? = null

    private var listener: Player.DefaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "player state $playbackState")
        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        textureView = videoSurfaceView as TextureView
    }

    fun setDataSource(source: String, millsecsPerFrame: Int, thubnailCount: Int, callback: (Bitmap, Int) -> Boolean) {
        mediaPath = source
        exoPlayer = com.spx.videoclipeditviewtest.player.initPlayer(context, mediaPath, this, listener)
        exoPlayer?.volume = 0f
        exoPlayer!!.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer!!.playWhenReady = false
        val param = PlaybackParameters(20f)
        player.setPlaybackParameters(param)

        this.callback = callback

        Thread {
            var duration = getVideoDuration(context, mediaPath)

            var millSec = 0L
            var mMMR = MediaMetadataRetriever()
            mMMR.setDataSource(mediaPath)

            for (i in 0 until thubnailCount) {
                if (millSec > duration) {
                    millSec = duration
                }
                thumbnailMillSecList.add(millSec)
                Log.d(TAG, "getThumbnail()  [$i] time:$millSec")

                millSec += millsecsPerFrame.toLong()
            }

            post {
                startPlayAndCapture()
            }
        }.start()
    }

    private fun startPlayAndCapture() {

        if (thumbnailMillSecList.size == 0) {
            return
        }

        val timeMs = thumbnailMillSecList.get(0)
//        Log.d(TAG, "startPlayAndCapture()  current position:${exoPlayer!!.currentPosition}, want timems:$timeMs")
        if (exoPlayer!!.currentPosition > timeMs) {
            exoPlayer?.playWhenReady = false
            val bitmap = textureView.bitmap
//            Log.d(TAG, "startPlayAndCapture()  bitmap:$bitmap")
            bitmap?.run {
                callback?.invoke(this, bitmapIndex++)
                thumbnailMillSecList.removeAt(0)
            }
        }
        exoPlayer?.playWhenReady = true
        postDelayed({ startPlayAndCapture() },CHECK_INTERVAL_MS)
    }


}