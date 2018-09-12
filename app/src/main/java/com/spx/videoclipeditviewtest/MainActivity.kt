package com.spx.videoclipeditviewtest

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.TextureView
import android.view.View
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    var userAgent = "spx"
//    var videoPlayUrl = "http://vod.leasewebcdn.com/bbb.flv?ri=1024&rs=150&start=0"
    //    var referer = "https://www.bilibili.com/video/av31055163/?spm_id_from=333.334.bili_dance.9"
    var videoPlayUrl = "rawresource:///" + R.raw.video
    lateinit var player: SimpleExoPlayer
    var bitmapList = mutableListOf<Bitmap>()
    lateinit var adapter: MyAdapter
    var handler = Handler()
    var playEndOnece = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = MyAdapter(bitmapList)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        initPlayer()

        Thread({
            var test = ExtractMpegFramesTest()
            test.extractMpegFrames()
        }).start()


        start_decode.setOnClickListener {
            var intent = Intent(this@MainActivity, DecodeActivity::class.java)
            startActivity(intent)
        }
    }


    var listener: Player.DefaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                Log.d(TAG, "player started!")
                if (bitmapList.size == 0 && !isDestroyed) {
                    getBitmap()
                }


            } else if (playbackState == Player.STATE_ENDED) {
                Log.d(TAG, "player end!")
                playEndOnece = true
                handler.removeCallbacksAndMessages(null)
            }
        }
    }

    fun getBitmap() {
        val videoSurfaceView = player_view.videoSurfaceView as TextureView
        val bitmap = videoSurfaceView.bitmap
//        Log.d(TAG, "bitmap:" + bitmap)
        bitmapList.add(bitmap)
        adapter.notifyDataSetChanged()

        if (!playEndOnece) {
            handler.postDelayed({ getBitmap() }, 500)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun initPlayer() {

        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        var mTrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(this),
                mTrackSelector)

        player_view.visibility = View.VISIBLE
        player_view.player = player
        player.addListener(listener)
//        player.repeatMode = Player.REPEAT_MODE_ALL
        player.repeatMode = Player.REPEAT_MODE_OFF
        player.playWhenReady = true

        var videoUrl = videoPlayUrl


        var mVideoSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, userAgent)).createMediaSource(Uri.parse(videoUrl)!!)
        player.prepare(mVideoSource)

    }
}
