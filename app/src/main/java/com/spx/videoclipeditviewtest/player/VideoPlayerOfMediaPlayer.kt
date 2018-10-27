package com.spx.videoclipeditviewtest.player

import android.annotation.TargetApi
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.spx.videoclipeditviewtest.VideoClipActivity

class VideoPlayerOfMediaPlayer(val surfaceView: SurfaceView) : VideoPlayer {


    lateinit var mediaPlayer: MediaPlayer

    override fun setupPlayer(context: Context, mediaPath: String, view: Any) {
        mediaPlayer.setDataSource(mediaPath)
        mediaPlayer.prepare()
        mediaPlayer.setOnPreparedListener {
            Log.d(VideoClipActivity.TAG, "onPrepared: ...")
            mediaPlayer.start()
            mediaPlayer.isLooping = true
        }
    }

    override fun initPlayer() {
        mediaPlayer = MediaPlayer()
        val holder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                mediaPlayer.setDisplay(holder)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
            }

        })
    }

    override fun pausePlayer() {
        mediaPlayer.pause()
    }

    override fun startPlayer() {
        mediaPlayer.start()
    }

    override fun seekToPosition(position: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(position, MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer.seekTo(position.toInt())
        }
    }

    override fun getPlayerCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun setPlaySpeed(speed: Float) {
        setMediaPlayerSpeed(speed)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun setMediaPlayerSpeed(speed: Float) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed))
            mediaPlayer.start()
        } else {
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed))
            mediaPlayer.pause()
        }
    }

}