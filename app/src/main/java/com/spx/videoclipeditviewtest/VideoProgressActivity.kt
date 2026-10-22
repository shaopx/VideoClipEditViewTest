package com.spx.videoclipeditviewtest

import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.daasuu.epf.filter.FilterType
import com.daasuu.mp4compose.composer.Mp4Composer
import com.spx.egl.GlFilterList
import com.spx.egl.GlFilterPeriod
import com.spx.egl.VideoProcessConfig

class VideoProgressActivity : AppCompatActivity() {
    companion object {
        const val TAG = "VideoProgressActivity"
        private const val PROGRESS_UPDATE_INTERVAL_MS = 200L
    }

    lateinit var videoProcessConfig: VideoProcessConfig
    var glFilterList = GlFilterList()

    // Views (替代已废弃的 kotlin-android-extensions synthetic)
    lateinit var pb_progress: ProgressBar
    lateinit var tv_progress: TextView
    lateinit var tv_status: TextView
    lateinit var tv_path: TextView
    lateinit var btn_done: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var progression = 0
    private var progressLoopRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.video_process_activity_layout)
        pb_progress = findViewById(R.id.pb_progress)
        tv_progress = findViewById(R.id.tv_progress)
        tv_status = findViewById(R.id.tv_status)
        tv_path = findViewById(R.id.tv_path)
        btn_done = findViewById(R.id.btn_done)

        btn_done.setOnClickListener { finish() }

        videoProcessConfig = intent.getSerializableExtra("videoProcessConfig") as VideoProcessConfig
        Log.d(TAG, "on create mediaPath:${videoProcessConfig.srcMediaPath}")

        val filterConfigList = videoProcessConfig.filterConfigList

        for (fconfig in filterConfigList) {
            glFilterList.putGlFilter(GlFilterPeriod(fconfig.startTimeMs, fconfig.endTimeMs, FilterType.createGlFilter(fconfig.filterName, null, this)))
        }
    }

    override fun onResume() {
        super.onResume()
        val s = System.currentTimeMillis()
        Mp4Composer(videoProcessConfig.srcMediaPath, videoProcessConfig.outMediaPath)
                .frameRate(30)
                .filterList(glFilterList)
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(_p: Double) {
                        Log.d(TAG, "onProgress $_p")
                        progression = (100 * _p).toInt()
                    }

                    override fun onCompleted() {
                        Log.d(TAG, "onCompleted()")
                        progression = 100
                        // 通知媒体库扫描, 让生成的视频出现在相册中
                        MediaScannerConnection.scanFile(
                            this@VideoProgressActivity,
                            arrayOf(videoProcessConfig.outMediaPath),
                            arrayOf("video/mp4"),
                            null
                        )
                        runOnUiThread {
                            val e = System.currentTimeMillis()
                            tv_progress.text = "100%"
                            pb_progress.progress = 100
                            tv_status.text = "生成视频成功, 耗时${e - s}ms"
                            tv_path.text = "文件: ${videoProcessConfig.outMediaPath}"
                            tv_path.visibility = View.VISIBLE
                            btn_done.text = "完成"
                            btn_done.visibility = View.VISIBLE
                        }
                    }

                    override fun onCanceled() {
                        runOnUiThread {
                            tv_status.text = "生成视频已取消"
                            btn_done.text = "返回"
                            btn_done.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailed(exception: Exception) {
                        Log.d(TAG, "onFailed()", exception)
                        runOnUiThread {
                            tv_status.text = "生成视频失败: ${exception.message}"
                            btn_done.text = "返回"
                            btn_done.visibility = View.VISIBLE
                        }
                    }
                })
                .start()

        startProgressLoop()
    }

    private fun startProgressLoop() {
        if (progressLoopRunning) {
            return
        }
        progressLoopRunning = true
        showProgress()
    }

    private fun showProgress() {
        if (!progressLoopRunning) {
            return
        }
        pb_progress.progress = progression
        tv_progress.text = "$progression%"
        if (progression < 100) {
            handler.postDelayed({
                if (progression < 100) {
                    showProgress()
                } else {
                    progressLoopRunning = false
                }
            }, PROGRESS_UPDATE_INTERVAL_MS)
        } else {
            progressLoopRunning = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressLoopRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}
