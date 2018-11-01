package com.spx.library.player

import android.content.Context
import android.net.Uri
import android.view.View
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.exoplayer2.PlaybackParameters





fun initPlayer(context: Context, videoUrl:String, playerView: PlayerView,
               listener: Player.EventListener ):SimpleExoPlayer {
    var TAG ="initPlayer"
    val defaultSourceFactory = DefaultDataSourceFactory(context,"luedong")

    var loadControl = MyLoadControl()

    var player = ExoPlayerFactory.newSimpleInstance( DefaultRenderersFactory(context),
            DefaultTrackSelector(),DefaultLoadControl(),null )

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

fun initPlayer(context: Context, videoUrl: String,
               listener: Player.EventListener): SimpleExoPlayer {
    val defaultSourceFactory = DefaultDataSourceFactory(context, "luedong")

    var player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())

    player!!.addListener(listener)

    player!!.repeatMode = Player.REPEAT_MODE_ALL
    player!!.playWhenReady = true

    var mVideoSource = ExtractorMediaSource.Factory(defaultSourceFactory).createMediaSource(Uri.parse(videoUrl)!!)

    if (player != null) {
        player!!.prepare(mVideoSource)
    }

    return player
}