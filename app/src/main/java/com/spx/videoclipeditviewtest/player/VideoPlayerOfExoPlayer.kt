package com.spx.videoclipeditviewtest.player

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.PlaybackParameters



class VideoPlayerOfExoPlayer : VideoPlayer {

    companion object {
        val TAG ="VideoPlayerOfExoPlayer"
    }

    var exoPlayer: SimpleExoPlayer? = null
    var player_view:PlayerView? = null

    var videoListener = object : SimpleExoPlayer.VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, _un: Int, _p: Float) {
            if (width < height) {
                return
            }
            player_view?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
        }

        override fun onRenderedFirstFrame() {
        }

    }

    var listener: Player.EventListener = object : Player.EventListener {
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

    override fun setupPlayer(context: Context, mediaPath: String, view: Any) {
        player_view =  view as PlayerView
        exoPlayer = com.spx.videoclipeditviewtest.player.initPlayer(context, mediaPath,player_view!!, listener)
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

    override fun seekToPosition(position: Long) {
        pausePlayer()
        exoPlayer?.seekTo(position)
    }

    override fun getPlayerCurrentPosition(): Int {
        return exoPlayer?.currentPosition?.toInt()?:0
    }

    override fun setPlaySpeed(speed: Float) {
        val param = PlaybackParameters(speed)
        exoPlayer?.setPlaybackParameters(param)
    }

}