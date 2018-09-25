package com.spx.videoclipeditviewtest

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
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
import java.io.File
import java.text.DecimalFormat

/**
 * 请根据手机中视频文件的地址更新下面的videoPlayUrl变量
 */
class MainActivity : AppCompatActivity(), ClipContainer.Callback {

    companion object {
        val TAG = "MainActivity"
        val videoPlayUrl = "/storage/emulated/0/DCIM/Camera/1111.mp4"
        var MSG_UPDATE = 1
    }


    lateinit var player: SimpleExoPlayer

    var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            updatePlayPosition()
        }
    }
    private var millsecPerThumbnail = 1000
    private var secFormat = DecimalFormat("##0.0")

    var playEndOnece = false
    var mediaDuration: Long = 0


    private fun onNewThumbnail(bitmap: Bitmap, index: Int) {
        Log.d(TAG, "onNewThumbnail  bitmap:$bitmap, index:$index")
        clipContainer.addThumbnail(index, bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var file = File(videoPlayUrl)
        if (!file.exists()) {
            Toast.makeText(this, "请更新videoPlayUrl变量为本地手机的视频文件地址", Toast.LENGTH_LONG).show()
        }

        var test = VideoFrameExtractor(this, Uri.parse(videoPlayUrl))

        mediaDuration = test.videoDuration
        Log.d(TAG, "onCreate mediaDuration:$mediaDuration")

        clipContainer.initRecyclerList((mediaDuration / millsecPerThumbnail).toInt())


        // 因为使用了egl, 必须在一个新线程中启动
        Thread {
            test.getThumbnail(millsecPerThumbnail) { bitmap, index -> handler.post { onNewThumbnail(bitmap, index) } }
        }.start()


        initPlayer()



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
        var selSec = startMillSec / 1000f
        toast_msg_tv.text = "预览到${secFormat.format(selSec)}s"
        toast_msg_tv.visibility = View.VISIBLE
    }

    override fun onSelectionChang(totalCount: Int, startMillSec: Long, endMillSec: Long, finished: Boolean) {
        Log.d(TAG, "onSelectionChang ...startMillSec:$startMillSec, endMillSec:$endMillSec")
        var time = (endMillSec - startMillSec)
        if (time > mediaDuration) {
            time = mediaDuration
        }
        var selSec = time / 1000f
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


    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    fun updatePlayPosition() {
        val currentPosition = player.currentPosition
        clipContainer.setProgress(currentPosition)
        handler.removeMessages(MSG_UPDATE)
        handler.sendEmptyMessageDelayed(MSG_UPDATE, 60)
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


        var mVideoSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, "spx")).createMediaSource(Uri.parse(videoUrl)!!)
        player.prepare(mVideoSource)

    }
}
