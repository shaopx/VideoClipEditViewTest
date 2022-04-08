package com.spx.library.player

import android.annotation.TargetApi
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class VideoPlayerOfMediaPlayer(val surfaceView: SurfaceView) : VideoPlayer {

    var mediaPlayer: MediaPlayer? = null
    var surfaceHolder: SurfaceHolder? = null

    override fun setupPlayer(context: Context, mediaPath: String) {
        mediaPlayer?.setDataSource(mediaPath)
        mediaPlayer?.prepare()
        mediaPlayer?.setOnPreparedListener {
            mediaPlayer!!.start()
            mediaPlayer!!.isLooping = true
        }
    }

    override fun initPlayer() {
        mediaPlayer = MediaPlayer()
        val holder = surfaceView.holder
        if (surfaceHolder != null) {
            mediaPlayer!!.setDisplay(surfaceHolder)
        }
        holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                //
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                mediaPlayer!!.setDisplay(holder)
                surfaceHolder = holder
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
               //
            }

        })

    }

    override fun pausePlayer() {
        mediaPlayer?.pause()
    }

    override fun startPlayer() {
        mediaPlayer?.start()
    }

    override fun seekToPosition(position: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer?.seekTo(position, MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer?.seekTo(position.toInt())
        }
    }

    override fun getPlayerCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun setPlaySpeed(speed: Float) {
        setMediaPlayerSpeed(speed)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun setMediaPlayerSpeed(speed: Float) {
        if (mediaPlayer!!.isPlaying()) {
            mediaPlayer!!.setPlaybackParams(mediaPlayer!!.getPlaybackParams().setSpeed(speed))
            mediaPlayer!!.start()
        } else {
            mediaPlayer!!.setPlaybackParams(mediaPlayer!!.getPlaybackParams().setSpeed(speed))
            mediaPlayer!!.pause()
        }
    }

    override fun enableFramePreviewMode() {

    }

    override fun releasePlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    override fun getDuration(): Int {
        return mediaPlayer?.duration?:0
    }
}