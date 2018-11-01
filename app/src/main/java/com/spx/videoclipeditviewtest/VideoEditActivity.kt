package com.spx.videoclipeditviewtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.daasuu.epf.EPlayerView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.spx.library.player.VideoPlayerOfExoPlayer
import com.spx.library.player.initPlayer
import com.spx.videoclipeditviewtest.ext.createFilterOptions
import com.spx.videoclipeditviewtest.ext.getInt
import com.spx.videoclipeditviewtest.util.FilterType
import com.spx.videoclipeditviewtest.view.BottomDialogFragment
import kotlinx.android.synthetic.main.activity_video_edit.*

class VideoEditActivity : AppCompatActivity() {

    companion object {
        const val TAG = "VideoEditActivity"
    }

    lateinit var exoPlayer: SimpleExoPlayer
    lateinit var ePlayerView: EPlayerView
    var listener: Player.DefaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(VideoPlayerOfExoPlayer.TAG, "player state $playbackState")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_edit)

        var mediaPath = intent.getStringExtra("video_path")

        exoPlayer = initPlayer(this, mediaPath, listener)
        ePlayerView = EPlayerView(this)
        ePlayerView.setSimpleExoPlayer(exoPlayer)
        ePlayerView.layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
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
        var dialogFragment = BottomDialogFragment.getInstance(0, getSelection(),
                "选择滤镜", createFilterOptions())
        dialogFragment.setSelectionCallBack { selection, name ->
            val filter = FilterType.createFilterList().get(selection)
            Log.d(TAG, "selection:$selection, filter:$filter")
            ePlayerView.setGlFilter(FilterType.createGlFilter( filter, applicationContext))
        }
        dialogFragment.show(supportFragmentManager, "filter_dialog")
    }

    private fun getSelection() = getInt(this, "filter_selection", 0)


}


