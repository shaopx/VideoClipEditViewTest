package com.spx.videoclipeditviewtest

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Media3 播放演示.
 *
 * 与项目中已有的 ExoPlayer 2.x / MediaPlayer 播放演示相互独立,
 * 仅演示 AndroidX Media3 (ExoPlayer 的继任者) 的最简接入方式.
 */
class Media3PlayerActivity : AppCompatActivity() {

  private lateinit var playerView: PlayerView
  private lateinit var iv_back: ImageView

  private var player: ExoPlayer? = null
  private var mediaPath: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_media3_player)

    playerView = findViewById(R.id.player_view)
    iv_back = findViewById(R.id.iv_back)
    iv_back.setOnClickListener { finish() }

    mediaPath = intent.getStringExtra("video_path")
  }

  override fun onStart() {
    super.onStart()
    initializePlayer()
  }

  override fun onStop() {
    super.onStop()
    releasePlayer()
  }

  private fun initializePlayer() {
    val path = mediaPath
    if (path.isNullOrEmpty() || player != null) {
      return
    }
    player = ExoPlayer.Builder(this).build().also {
      playerView.player = it
      it.setMediaItem(MediaItem.fromUri(path))
      it.prepare()
      it.playWhenReady = true
    }
  }

  private fun releasePlayer() {
    player?.release()
    player = null
    playerView.player = null
  }
}
