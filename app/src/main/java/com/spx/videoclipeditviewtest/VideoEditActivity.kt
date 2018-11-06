package com.spx.videoclipeditviewtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.spx.library.scale
import com.spx.videoclipeditviewtest.ext.createFilterOptions
import com.spx.videoclipeditviewtest.ext.getFilterByName
import com.spx.videoclipeditviewtest.ext.getInt
import com.spx.videoclipeditviewtest.view.BottomDialogFragment
import kotlinx.android.synthetic.main.activity_video_edit.*

class VideoEditActivity : AppCompatActivity() {


    companion object {
        const val TAG = "VideoEditActivity"
    }

    lateinit var mediaPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_edit)

        mediaPath = intent.getStringExtra("video_path")

        player_view_mp.setDataSource(mediaPath)
        player_view_mp.start()

        tv_filter.setOnClickListener { showFilterDialog() }
        tv_effect.setOnClickListener { player_view_mp.scale()}

    }

    override fun onResume() {
        super.onResume()
        player_view_mp.resumePlay()
    }

    override fun onPause() {
        super.onPause()
        player_view_mp.pausePlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        player_view_mp.release()
    }


    private fun showFilterDialog() {
        var dialogFragment = BottomDialogFragment.getInstance(0, getSelection(),
                "选择滤镜", createFilterOptions())
        dialogFragment.setSelectionCallBack { selection, option ->
            val filter = getFilterByName(option.optionName, applicationContext)
            Log.d(TAG, "selection:$selection, filter:$filter")
            player_view_mp.setGlFilter(filter)
        }
        dialogFragment.show(supportFragmentManager, "filter_dialog")
    }

    private fun getSelection() = getInt(this, "filter_selection", 0)

}


