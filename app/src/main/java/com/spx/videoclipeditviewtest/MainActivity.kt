package com.spx.videoclipeditviewtest

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultLoadControl.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.DecimalFormat

/**
 * 请根据手机中视频文件的地址更新下面的videoPlayUrl变量
 */
class MainActivity : AppCompatActivity(), ClipContainer.Callback {

    companion object {
        val TAG = "MainActivity"
        //        val videoPlayUrl = "/storage/emulated/0/DCIM/Camera/VID_20180930_123107.mp4"
        val videoPlayUrl = "/storage/emulated/0/22.mp4"
        var MSG_UPDATE = 1
    }


    lateinit var player: MediaPlayer

    var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            updatePlayPosition()
        }
    }
    private var millsecPerThumbnail = 1000
    private var secFormat = DecimalFormat("##0.0")

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
        if (!finished) {
            player.pause()
        }

        player.seekTo(startMillSec.toInt())

        if (finished) {
            player.start()
        }
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

//        if (finished) {
//            updatePlayPosition()
//        }

        if (!finished) {
            player.pause()
        }

        player.seekTo(startMillSec.toInt())

        if (finished) {
            player.start()
        }
    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    fun updatePlayPosition() {
        val currentPosition = player.duration
        clipContainer.setProgress(currentPosition.toLong())
        handler.removeMessages(MSG_UPDATE)
        handler.sendEmptyMessageDelayed(MSG_UPDATE, 60)
    }

    private fun initPlayer() {
        player = MediaPlayer()
        player.setDataSource(videoPlayUrl)
        val holder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                player.setDisplay(holder)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
            }

        })

        player.prepare()
        player.setOnPreparedListener {
            Log.d(TAG, "onPrepared: ...")
            player.start()
            player.isLooping = true
        }
    }
}
