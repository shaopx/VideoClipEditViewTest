package com.spx.library.player

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.source.ExtractorMediaSource


class VideoPlayerOfExoPlayer(val playerView: PlayerView) : VideoPlayer {

    companion object {
        val TAG = "VideoPlayerOfExoPlayer"
        val PREVIEW_MODE_MS_LONG = 250
        val PREVIEW_MODE_MS_SHORT = 100
    }

    var lastSeekingPosition = 0L
    var previewModeTimeMs = PREVIEW_MODE_MS_LONG
    var exoPlayer: SimpleExoPlayer? = null

    override fun enableFramePreviewMode() {
        previewModeTimeMs = PREVIEW_MODE_MS_SHORT
    }

    var videoListener = object : SimpleExoPlayer.VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, _un: Int, _p: Float) {
            if (width < height) {
                return
            }
            playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
        }

        override fun onRenderedFirstFrame() {
        }

    }

    var listener: Player.DefaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "player state $playbackState")
//            when (playbackState) {
//                Player.STATE_BUFFERING -> progressBar.visibility = View.VISIBLE
//                Player.STATE_IDLE -> {
//                    progressBar.visibility = View.INVISIBLE
////                    play_iv.visibility = View.VISIBLE
//                }
//                Player.STATE_READY -> {
//                    imageview?.run {
//                        imageview.visibility = View.INVISIBLE
//                        play_iv.visibility = View.INVISIBLE
//                        progressBar.visibility = View.INVISIBLE
//                    }
//                }
//                Player.STATE_ENDED -> {
//                    imageview?.run {
//                        imageview.visibility = View.VISIBLE
//                        play_iv.visibility = View.VISIBLE
//                    }
//                    videoPlayer?.run {
//                        videoPlayer!!.playWhenReady = false
//                    }
//                }
//            }

        }
    }

    override fun setupPlayer(context: Context, mediaPath: String) {
        exoPlayer = com.spx.library.player.initPlayer(context, mediaPath, playerView!!, listener)
        startPlayer()
    }

    override fun initPlayer() {

    }

    override fun pausePlayer() {
        exoPlayer?.playWhenReady = false
    }

    override fun startPlayer() {
        exoPlayer?.playWhenReady = true
    }


    var lastSeekingTime = 0L

    override fun seekToPosition(position: Long) {
//        pausePlayer()
        if (Math.abs(position - lastSeekingPosition) < previewModeTimeMs) {
            return
        }
//        if(System.currentTimeMillis()-lastSeekingTime<200){
//
//        }

        exoPlayer?.seekTo(position)
//        exoPlayer?.setVolume(0f)
        exoPlayer?.playWhenReady = true
        lastSeekingPosition = position
    }

    override fun getPlayerCurrentPosition(): Int {
        return exoPlayer?.currentPosition?.toInt() ?: 0
    }

    override fun setPlaySpeed(speed: Float) {
        val param = PlaybackParameters(speed)
        exoPlayer?.setPlaybackParameters(param)
    }

    override fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer= null
    }

    override fun isPlaying(): Boolean {
       return exoPlayer?.playWhenReady == true && exoPlayer?.playbackState == STATE_READY
    }

    override fun getDuration(): Int {
        return (exoPlayer?.duration?:0).toInt()
    }
}