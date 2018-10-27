package com.spx.videoclipeditviewtest

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaPlayer.SEEK_CLOSEST
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.daasuu.mp4compose.composer.Mp4Composer
import kotlinx.android.synthetic.main.activity_video_clip.*
import java.io.File
import java.text.DecimalFormat

/**
 * 请根据手机中视频文件的地址更新下面的videoPlayUrl变量
 */
class VideoClipActivity : AppCompatActivity(), ClipContainer.Callback {

    companion object {
        val TAG = "VideoClipActivity"
//                val videoPlayUrl = "/storage/emulated/0/DCIM/Camera/VID_20180930_123107.mp4"
//        val videoPlayUrl = "/storage/emulated/0/download/VID_20181025.mp4"
//        val videoPlayUrl = "/storage/emulated/0/movies/201810_25sp.mp4"
        val videoPlayUrl = "/storage/emulated/0/movies/process.mp4"
        var MSG_UPDATE = 1
    }


    lateinit var videoPathInput: String
    lateinit var player: MediaPlayer

    var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            updatePlayPosition()
        }
    }
    private var millsecPerThumbnail = 1000
    private var secFormat = DecimalFormat("##0.0")

    var mediaDuration: Long = 0
    var startMillSec: Long = 0
    var endMillSec: Long = 0
    var frozontime = 0L

    private fun onNewThumbnail(bitmap: Bitmap, index: Int) {
        Log.d(TAG, "onNewThumbnail  bitmap($index):$bitmap, width:${bitmap.width}, height:${bitmap.height}")
        clipContainer.addThumbnail(index, bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_clip)

        videoPathInput = intent.getStringExtra("video_path")
        Log.d(TAG, "onCreate videoPathInput:$videoPathInput")

        player = MediaPlayer()
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

        // 因为使用了egl, 必须在一个新线程中启动

        startProcess()


//        onProcessCompleted()

    }

    private fun startProcess() {
        var mp4Composer = Mp4Composer(videoPathInput, videoPlayUrl)
                .frameRate(5)
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double) {
//                        Log.d(TAG, "onProgress = $progress")
                        runOnUiThread { pb_progress.progress = (progress * 100).toInt() }
                    }

                    override fun onCompleted() {
                        Log.d(TAG, "onCompleted()")
                        runOnUiThread {
                            pb_progress.visibility = View.GONE
                            view_shadow.visibility = View.INVISIBLE
                            onProcessCompleted()
                        }


                    }

                    override fun onCanceled() {

                    }

                    override fun onFailed(exception: Exception) {
                        Log.d(TAG, "onFailed()")
                    }
                })
                .start()

    }

    private fun onProcessCompleted() {

        var file = File(videoPlayUrl)
        if (!file.exists()) {
            Toast.makeText(this, "请更新videoPlayUrl变量为本地手机的视频文件地址", Toast.LENGTH_LONG).show()
        }

        var test = VideoFrameExtractor(this, Uri.parse(videoPlayUrl))

        mediaDuration = test.videoDuration
        Log.d(TAG, "onProcessCompleted mediaDuration:$mediaDuration")
        endMillSec = if (mediaDuration > ClipContainer.maxSelection) {
            ClipContainer.maxSelection
        } else {
            mediaDuration
        }

        clipContainer.initRecyclerList((mediaDuration / millsecPerThumbnail).toInt())


        Thread {
            test.getThumbnail(millsecPerThumbnail) { bitmap, index -> handler.post { onNewThumbnail(bitmap, index) } }
        }.start()


        startPlayer()

        clipContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                log("onGlobalLayout()  mediaDuration:$mediaDuration,  size:${clipContainer.list.size}")
                clipContainer.updateInfo(mediaDuration, clipContainer.list.size)
                updatePlayPosition()

                clipContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        clipContainer.callback = (this)
    }


    override fun onPreviewChang(startMillSec: Long, finished: Boolean) {
//        Log.d(TAG, "onPreviewChang   startMillSec:$startMillSec")
        var selSec = startMillSec / 1000f
        toast_msg_tv.text = "预览到${secFormat.format(selSec)}s"
        toast_msg_tv.visibility = View.VISIBLE
        if (!finished) {
            player.pause()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            player.seekTo(startMillSec, SEEK_CLOSEST)
        } else {
            player.seekTo(startMillSec.toInt())
        }

        if (finished) {
            frozontime = System.currentTimeMillis() + 500
            player.start()
        }

        handler.removeMessages(MSG_UPDATE)
        if (finished) {
            handler.sendEmptyMessageDelayed(MSG_UPDATE, 20)
        }
    }

    override fun onSelectionChang(totalCount: Int, startMillSec: Long, endMillSec: Long, finished: Boolean) {
//        Log.d(TAG, "onSelectionChang ...startMillSec:$startMillSec, endMillSec:$endMillSec")
        this.startMillSec = startMillSec
        this.endMillSec = endMillSec

        var time = (endMillSec - startMillSec)
        if (time > mediaDuration) {
            time = mediaDuration
        }
        var selSec = time / 1000f
        toast_msg_tv.text = "已截取${secFormat.format(selSec)}s"
        toast_msg_tv.visibility = View.VISIBLE

        handler.removeMessages(MSG_UPDATE)
        if (finished) {
            handler.sendEmptyMessageDelayed(MSG_UPDATE, 20)
        }

        if (!finished) {
            player.pause()
        }

        player.seekTo(startMillSec.toInt())

        if (finished) {
            frozontime = System.currentTimeMillis() + 500
            player.start()
        }
    }


    override fun onPause() {
        super.onPause()
        player?.pause()
        handler.removeCallbacksAndMessages(null)
    }

    fun updatePlayPosition() {
        player ?: return
        val currentPosition = player.currentPosition
        if (currentPosition > endMillSec) {
            player.seekTo(0)
        } else {
            clipContainer.setProgress(currentPosition.toLong(), frozontime)
        }

        handler.removeMessages(MSG_UPDATE)
        handler.sendEmptyMessageDelayed(MSG_UPDATE, 20)
    }

    private fun startPlayer() {

        player.setDataSource(videoPlayUrl)
        player.prepare()
        player.setOnPreparedListener {
            Log.d(TAG, "onPrepared: ...")
            player.start()
            player.isLooping = true
        }
    }
}
