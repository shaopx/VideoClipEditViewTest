package com.spx.videoclipeditviewtest

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.spx.library.decodeFile
import com.spx.library.getVideoDuration
import com.spx.library.scale
import com.spx.videoclipeditviewtest.ext.createFilterOptions
import com.spx.videoclipeditviewtest.ext.getFilterByName
import com.spx.videoclipeditviewtest.ext.getInt
import com.spx.videoclipeditviewtest.view.BottomDialogFragment
import kotlinx.android.synthetic.main.activity_video_clip.view.*
import kotlinx.android.synthetic.main.activity_video_edit.*

class VideoEditActivity : AppCompatActivity() {


    companion object {
        const val TAG = "VideoEditActivity"
    }

    lateinit var mediaPath: String
    var mediaDuration: Long = 0
    var thumbnailCount = 0
    private var millsecPerThumbnail = 1000
    var list: MutableList<String?> = mutableListOf()
    var itemWidth = 100

    var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_edit)

        mediaPath = intent.getStringExtra("video_path")

        player_view_mp.setDataSource(mediaPath)
        player_view_mp.start()

        tv_filter.setOnClickListener { showFilterDialog() }
        tv_effect.setOnClickListener { switchToEffectEdit() }

        initEditInfo()
    }

    private fun initEditInfo() {
        mediaDuration = getVideoDuration(this, mediaPath)
        Log.d(TAG, "initEditInfo mediaDuration:$mediaDuration")
        millsecPerThumbnail = 500
        thumbnailCount = Math.ceil(((mediaDuration * 1f / millsecPerThumbnail).toDouble())).toInt()
        Log.d(TAG, "thumbnailCount:$thumbnailCount,  millsecPerThumbnail:$millsecPerThumbnail")
        for(i in 0 until  thumbnailCount){
            list.add(i, "")
        }
        var screenW = resources.displayMetrics.widthPixels
        itemWidth = screenW / 12

        var adapter = MyAdapter()
        recyclerview.adapter = adapter
        var layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        recyclerview.layoutManager = layoutManager

        var padding = screenW / 2
        recyclerview.setPaddingRelative(padding, 0, padding, 0)
        recyclerview.clipChildren = false

        recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d(TAG, "onScrolled  dx:" + dx)
                val (position, itemLeft, scrollX) = recyclerView.getScollXDistance()
                var total = itemWidth * adapter.itemCount
                var rate = 1f * (scrollX + padding) / total
                if (position == -1) {
                    rate = 1f
                }
//                Log.d(TAG, "onScrolled: position:$position, itemLeft:$itemLeft,  scrollX:$scrollX, total:$total, rate:$rate")
                onPreview(rate)
            }
        })

        player_view_exo_thumbnail.setDataSource(mediaPath, millsecPerThumbnail, thumbnailCount) { bitmap: String, index: Int ->
            Log.d(TAG, "[$index]bitmap:$bitmap")
            handler.post {
                list.set(index, bitmap)
                adapter.notifyDataSetChanged()
            }
        }


    }

    fun onPreview(rate:Float){
        var timems = mediaDuration*rate
        Log.d(TAG, "onPreview  time:$timems")
        player_view_mp.seekTo(timems.toLong())
    }

    private fun switchToEffectEdit() {
        player_view_mp.scale()

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
            player_view_mp.setFiler(0, mediaDuration, filter)
        }
        dialogFragment.show(supportFragmentManager, "filter_dialog")
    }

    private fun getSelection() = getInt(this, "filter_selection", 0)

    inner class VH : RecyclerView.ViewHolder {
        var title: TextView
        var image: ImageView

        constructor(itemview: View) : super(itemview) {
            title = itemview.findViewById(R.id.title)
            image = itemview.findViewById(R.id.image)
        }
    }


    inner class MyAdapter() : RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return VH(v)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(viewholder: VH, position: Int) {
            val layoutParams = viewholder.itemView.layoutParams
            layoutParams.width = itemWidth
            viewholder.itemView.layoutParams = layoutParams
//            viewholder.title.setText("$position")
            if (!list[position].isNullOrEmpty()) {
                viewholder.image.setImageBitmap(decodeFile(list[position]!!))
            } else {
                viewholder.image.setImageResource(R.drawable.ic_launcher_background)
            }

        }


    }
}


