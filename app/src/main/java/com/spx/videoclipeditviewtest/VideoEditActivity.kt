package com.spx.videoclipeditviewtest

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.daasuu.epf.EPlayerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.spx.library.player.MyLoadControl
import com.spx.library.player.VideoPlayerOfExoPlayer
import com.spx.library.player.initPlayer
import com.spx.videoclipeditviewtest.ext.createFilterOptions
import com.spx.videoclipeditviewtest.ext.getInt
import com.spx.videoclipeditviewtest.util.FilterType
import com.spx.videoclipeditviewtest.view.BottomDialogFragment
import com.spx.videoclipeditviewtest.view.BottomDialogFragment.Option
import kotlinx.android.synthetic.main.activity_video_edit.*

class VideoEditActivity : AppCompatActivity() {

    lateinit var exoPlayer: SimpleExoPlayer
    var listener: Player.DefaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(VideoPlayerOfExoPlayer.TAG, "player state $playbackState")
        }
    }
    lateinit var ePlayerView: EPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_edit)

        var mediaPath = intent.getStringExtra("video_path")

        exoPlayer = initPlayer(this, mediaPath, listener)
        ePlayerView = EPlayerView(this)
        ePlayerView.setSimpleExoPlayer(exoPlayer)
        ePlayerView.setLayoutParams(RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        (findViewById<View>(R.id.layout_movie_wrapper) as FrameLayout).addView(ePlayerView)
        ePlayerView.onResume()

        tv_filter.setOnClickListener { showFilterDialog() }

    }

    override fun onPause() {
        super.onPause()
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    private fun showFilterDialog() {
        var dialogFragment = BottomDialogFragment.getInstance(0, getSelection(), "选择滤镜", createFilterOptions())
        dialogFragment.setSelectionCallBack { selection, name ->
            ePlayerView.setGlFilter(FilterType.createGlFilter(FilterType.createFilterList().get(selection), applicationContext))
        }
        dialogFragment.show(supportFragmentManager, "filter_dialog")
    }

    private fun getSelection() = getInt(this, "filter_selection", 0)


}

fun initPlayer(context: Context, videoUrl: String,
               listener: Player.EventListener): SimpleExoPlayer {
    var TAG = "initPlayer"
    val defaultSourceFactory = DefaultDataSourceFactory(context, "luedong")

    var loadControl = MyLoadControl()

    var player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(context),
            DefaultTrackSelector(), DefaultLoadControl(), null)

    player!!.addListener(listener)

    player!!.repeatMode = Player.REPEAT_MODE_ALL
    player!!.playWhenReady = true

    var mVideoSource = ExtractorMediaSource.Factory(defaultSourceFactory).createMediaSource(Uri.parse(videoUrl)!!)

    if (player != null) {
        player!!.prepare(mVideoSource)
    }

    return player
}
