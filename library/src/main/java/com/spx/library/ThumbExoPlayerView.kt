package com.spx.library

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.util.*


class ThumbExoPlayerView(context: Context?, attrs: AttributeSet?) :
  StyledPlayerView(context!!, attrs) {
  companion object {
    const val CHECK_INTERVAL_MS: Long = 30
    const val TAG = "ThumbExoPlayerView"
  }

  private lateinit var mediaPath: String
  private lateinit var textureView: TextureView
  private var callback: ((String, Int) -> Boolean)? = null
  private var bitmapIndex = 0
  private val thumbnailMillSecList = ArrayList<Long>()
  private var exoPlayer: ExoPlayer? = null

  override fun onFinishInflate() {
    super.onFinishInflate()
    textureView = videoSurfaceView as TextureView
  }

  fun setDataSource(
    source: String,
    millsecsPerFrame: Int,
    thubnailCount: Int,
    callback: (String, Int) -> Boolean
  ) {
    mediaPath = source
    exoPlayer = ExoPlayer.Builder(context).build()
    exoPlayer?.volume = 0f
    exoPlayer!!.repeatMode = Player.REPEAT_MODE_OFF

    player = exoPlayer

    exoPlayer!!.playWhenReady = true
    val mediaItem: MediaItem = MediaItem.fromUri(mediaPath)
    exoPlayer!!.setMediaItem(mediaItem)
    exoPlayer!!.prepare()

    val param = PlaybackParameters(20f)
    player?.setPlaybackParameters(param)

    this.callback = callback

    Thread {
      var duration = getVideoDuration(context, mediaPath)

      var millSec = 0L
//      var mMMR = MediaMetadataRetriever()
//      mMMR.setDataSource(mediaPath)

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
    Log.d(TAG, "startPlayAndCapture()  size:${thumbnailMillSecList.size}")
    if (thumbnailMillSecList.size == 0) {
      return
    }

    val timeMs = thumbnailMillSecList.get(0)
    Log.d(TAG, "startPlayAndCapture()  player:${player}")
    player?.run {
      Log.d(TAG, "startPlayAndCapture()  current position:${player!!.currentPosition}, want timems:$timeMs")
      if (player!!.currentPosition > timeMs) {
        player?.playWhenReady = false
        val bitmap = textureView.bitmap
        Log.d(TAG, "startPlayAndCapture()  bitmap:$bitmap")
        bitmap?.run {
          var fileName =
            context.externalCacheDir?.absolutePath + "thumbnail_" + bitmapIndex
          writeToFile(bitmap, fileName)
          callback?.invoke(fileName, bitmapIndex++)
          thumbnailMillSecList.removeAt(0)
        }
      }
    }

    player?.playWhenReady = true
    postDelayed({ startPlayAndCapture() }, CHECK_INTERVAL_MS)
  }

  fun release() {

  }
}