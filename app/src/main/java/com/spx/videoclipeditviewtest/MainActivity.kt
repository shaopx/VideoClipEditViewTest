package com.spx.videoclipeditviewtest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
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
import com.google.android.exoplayer2.PlaybackParameters
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), ClipContainer.Callback {



    fun updatePlayPosition() {
        val currentPosition = player.currentPosition
//        Log.d(TAG, "currentPosition:$currentPosition")
        clipContainer.setProgress(currentPosition)
        handler.removeMessages(MSG_UPDATE)
        handler.sendEmptyMessageDelayed(MSG_UPDATE, 60)
    }

    val TAG = "MainActivity"
    var userAgent = "spx"
    //    var videoPlayUrl = "http://vod.leasewebcdn.com/bbb.flv?ri=1024&rs=150&start=0"
    //    var referer = "https://www.bilibili.com/video/av31055163/?spm_id_from=333.334.bili_dance.9"
    var videoPlayUrl = "rawresource:///" + R.raw.video
    var mediaDuration:Long = 81000

    lateinit var player: SimpleExoPlayer
    var bitmapList = mutableListOf<Bitmap>()

    var MSG_UPDATE = 1
    var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            updatePlayPosition()
        }
    }
    var playEndOnece = false
    var delay = 980  //ms  之所以不是1000, 是因为每次生成一帧的bitmap大约需要20ms
    var secFormat = DecimalFormat("##0.0")
    var hasStated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        initPlayer()

        with(clipContainer.list) {
            for (i in (1..8)) {
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_00))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_01))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_02))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_03))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_04))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_05))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_06))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_07))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_08))
                add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_09))
            }
            add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_09))
        }


        clipContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                clipContainer.updateInfo(mediaDuration, clipContainer.list.size)
                updatePlayPosition()

                clipContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        clipContainer.callback = (this)

    }

    override fun onPreviewChang(startMillSec: Long, finished: Boolean) {
        Log.d(TAG, "onPreviewChang   startMillSec:$startMillSec")
        var selSec =  startMillSec / 1000f
        toast_msg_tv.text = "预览到${secFormat.format(selSec)}s"
        toast_msg_tv.visibility = View.VISIBLE
    }

    override fun onSelectionChang(totalCount: Int, startMillSec: Long, endMillSec: Long, finished: Boolean) {
        Log.d(TAG, "onSelectionChang ...startMillSec:$startMillSec, endMillSec:$endMillSec" )
        var time = (endMillSec - startMillSec)
        if (time > mediaDuration) {
            time = mediaDuration
        }
        var selSec =  time / 1000f
        toast_msg_tv.text = "已截取${secFormat.format(selSec)}s"
        toast_msg_tv.visibility = View.VISIBLE

        if (finished) {
            updatePlayPosition()
        }

    }




    var listener: Player.DefaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                Log.d(TAG, "player started!  duration:" + player.duration)


            } else if (playbackState == Player.STATE_ENDED) {
                Log.d(TAG, "player end!")
                playEndOnece = true
                handler.removeCallbacksAndMessages(null)
            }
        }
    }

//    fun getBitmap() {
//        var start = System.currentTimeMillis()
//        val videoSurfaceView = player_view.videoSurfaceView as TextureView
//        val bitmap = videoSurfaceView.bitmap
//        var end = System.currentTimeMillis()
////        Log.d(TAG, "create new bitmap use ${(end - start)}ms")
//        bitmapList.add(bitmap)
//        adapter.notifyDataSetChanged()
//
//        if (!playEndOnece) {
//            handler.postDelayed({ getBitmap() }, delay.toLong())
//        }
//    }

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

        player.repeatMode = Player.REPEAT_MODE_ALL
//        player.repeatMode = Player.REPEAT_MODE_OFF
        player.playWhenReady = true
        val param = PlaybackParameters(1f)
        player.playbackParameters = (param)
        player.volume = 0f

        var videoUrl = videoPlayUrl


        var mVideoSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, userAgent)).createMediaSource(Uri.parse(videoUrl)!!)
        player.prepare(mVideoSource)

    }
}
