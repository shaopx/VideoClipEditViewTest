package com.spx.videoclipeditviewtest.player

import android.content.Context

interface VideoPlayer {

    fun setupPlayer(context: Context, mediaPath:String)

    fun initPlayer()

    fun pausePlayer()

    fun startPlayer()

    fun seekToPosition(position: Long)

    fun getPlayerCurrentPosition(): Int

    fun setPlaySpeed(speed:Float)

    fun enableFramePreviewMode()
    fun releasePlayer()
    fun isPlaying(): Boolean
}