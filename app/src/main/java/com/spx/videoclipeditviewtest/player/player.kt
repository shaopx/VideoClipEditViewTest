package com.spx.videoclipeditviewtest.player

import android.content.Context
import android.net.Uri
import android.view.View
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.video.VideoListener



fun initPlayer(context: Context, videoUrl:String, playerView: PlayerView,
               listener: Player.EventListener ):SimpleExoPlayer {
    var TAG ="initPlayer"
    val defaultSourceFactory = DefaultDataSourceFactory(context,"luedong")


    var player = ExoPlayerFactory.newSimpleInstance(context)

    playerView.visibility = View.VISIBLE
    playerView.player = player
    player!!.addListener(listener)

    player!!.repeatMode = Player.REPEAT_MODE_ALL
    player!!.playWhenReady = true

//    var mVideoSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, "spx")).createMediaSource(Uri.parse(videoUrl)!!)
//    player.prepare(mVideoSource)

    var mVideoSource = ExtractorMediaSource.Factory(defaultSourceFactory).createMediaSource(Uri.parse(videoUrl)!!)

    if (player != null) {
        player!!.prepare(mVideoSource)
    }

    return player
}
