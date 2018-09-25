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
    override fun onSelectionChang(totalCount: Int, startMillSec: Long, endMillSec: Long, finished: Boolean) {
        var selSec = (endMillSec - startMillSec) / 1000f
        toast_msg_tv.text = "[$startMillSec - $endMillSec], 取${secFormat.format(selSec)}s"
        toast_msg_tv.visibility = View.VISIBLE
        updatePlayPosition()
    }

    fun updatePlayPosition(){
        val currentPosition = player.currentPosition
        Log.d(TAG, "currentPosition:$currentPosition")
        clipContainer.setProgress(currentPosition)
        handler.removeMessages(MSG_UPDATE)
        handler.sendEmptyMessageDelayed(MSG_UPDATE, 60)
    }

    val TAG = "MainActivity"
    var userAgent = "spx"
    //    var videoPlayUrl = "http://vod.leasewebcdn.com/bbb.flv?ri=1024&rs=150&start=0"
    //    var referer = "https://www.bilibili.com/video/av31055163/?spm_id_from=333.334.bili_dance.9"
    var videoPlayUrl = "rawresource:///" + R.raw.video2
    lateinit var player: SimpleExoPlayer
    var bitmapList = mutableListOf<Bitmap>()

    var MSG_UPDATE = 1
    var handler = object :Handler(){
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

        for (i in (1..8)) {
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_00))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_01))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_02))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_03))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_04))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_05))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_06))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_07))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_08))
            bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_09))
        }
        bitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.frame_09))



        initPlayer()

        clipContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                clipContainer.updateInfo(81000, bitmapList)
                clipContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        clipContainer.callback = (this)

//        Thread({
//            ExtractMpegFramesTest().extractMpegFrames()
//        }).start()
    }

    /**
     * 用户选择裁剪区域后的回调方法
     * offsetRatio 是开始位置距离整个区间的比例
     * selectionRatio 是用户选择的区间占可选择区间的比例.  注意这里的可选择区间是指屏幕上显示的区间(max_selectabe_length), 并不是媒体文件总的时长. 媒体文件的整个时长可能是100s 但是整个显示裁剪区间可能只有30s
     */
    fun onSelectionChanged(totalCount: Int, offsetRatio: Float, endRatio: Float, selectionRatio: Float) {
        Log.d(TAG, "onSelectionChanged  offsetRatio:$offsetRatio, endRatio:$endRatio,  selectionRatio:$selectionRatio")


        var itemCount = totalCount
        var itemWidth = resources.getDimensionPixelSize(R.dimen.screencap_item_width)
        var totalWidth = itemCount * itemWidth

        val (position, itemLeft, scrollX) = recyclerview.getScollXDistance()

        var clipFrameBarWidth = resources.getDimensionPixelSize(R.dimen.clip_frame_bar_width)

        var finalLeft = itemLeft + clipFrameBarWidth

        var scrollXRatio = (position * itemWidth + finalLeft) * 1f / totalWidth

        var clipWidth = clipContainer.width - clipFrameBarWidth * 2
        var clipRatio = clipWidth * 1f / totalWidth

        Log.d(TAG, "onSelectionChanged  recyclerview. position:$position, finalLeft:$finalLeft, scrollX:$scrollX, itemCount:$itemCount, scrollXRatio:$scrollXRatio, clipRatio:$clipRatio")

        var finalRatio = scrollXRatio + clipRatio * offsetRatio
        Log.d(TAG, "onSelectionChanged  clipRatio:$clipRatio,   finalRatio:$finalRatio")

        var selectionStartPos = (11000f * finalRatio).toLong()
        var selectionEndPos = (11000f * (scrollXRatio + clipRatio * endRatio)).toLong()
        Log.d(TAG, "onSelectionChanged  selectionStartPos:$selectionStartPos, selectionEndPos:$selectionEndPos")
        if (selectionStartPos > 0) {
            player.seekTo(selectionStartPos)
            player.playWhenReady = true
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
