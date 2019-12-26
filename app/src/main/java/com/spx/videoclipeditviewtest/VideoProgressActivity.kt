package com.spx.videoclipeditviewtest

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.daasuu.epf.custfilter.GlFlashFliter
import com.daasuu.epf.custfilter.GlShakeFilter
import com.daasuu.epf.custfilter.GlSoulOutFilter
import com.daasuu.epf.filter.FilterType
import com.spx.egl.GlFilterList
import com.spx.egl.GlFilterPeriod
import com.daasuu.mp4compose.composer.Mp4Composer
import com.spx.egl.VideoProcessConfig
import kotlinx.android.synthetic.main.video_process_activity_layout.*

class VideoProgressActivity : AppCompatActivity() {
    companion object {
        const val TAG = "VideoProgressActivity"
    }

    lateinit var videoProcessConfig: VideoProcessConfig
    var glFilterList = GlFilterList()

    var handler = Handler()
    var progression = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.video_process_activity_layout)

        videoProcessConfig = intent.getSerializableExtra("videoProcessConfig") as VideoProcessConfig
        Log.d(TAG, "on create mediaPath:${videoProcessConfig.srcMediaPath}")

        val filterConfigList = videoProcessConfig.filterConfigList

        for (fconfig in filterConfigList){
            glFilterList.putGlFilter(GlFilterPeriod(fconfig.startTimeMs, fconfig.endTimeMs, FilterType.createGlFilter(fconfig.filterName, null, this)))
        }
    }

    override fun onResume() {
        super.onResume()
        var s = System.currentTimeMillis();
        var mp4Composer = Mp4Composer(videoProcessConfig.srcMediaPath, videoProcessConfig.outMediaPath)
                .frameRate(30)
                .filterList(glFilterList)
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(_p: Double) {
                        Log.d(TAG, "onProgress $_p")
                        progression = (100 * _p).toInt()
                    }

                    override fun onCompleted() {
                        Log.d(TAG, "onCompleted()")
                        runOnUiThread {
                            var e = System.currentTimeMillis()
                          Toast.makeText(this@VideoProgressActivity, "生成视频成功,耗时${e-s}ms, 文件放在:${videoProcessConfig.outMediaPath}", Toast.LENGTH_LONG).show()
                        }
                        progression = 100
                        finish()
                    }

                    override fun onCanceled() {
                        runOnUiThread {
                            Toast.makeText(this@VideoProgressActivity, "生成视频取消", Toast.LENGTH_LONG).show()
                        }

                    }

                    override fun onFailed(exception: Exception) {
                        Log.d(TAG, "onFailed()")
                        runOnUiThread {
                            Toast.makeText(this@VideoProgressActivity, "生成视频失败", Toast.LENGTH_LONG).show()
                        }

                    }
                })
                .start()

        showProgress()
    }

    private fun showProgress() {
        if (progression > 100) {
            return
        }
        pb_progress.progress = progression
        tv_progress.text = "$progression%"
        if (progression <= 100) {
            handler.postDelayed({ showProgress() }, 200)
        }

    }
}