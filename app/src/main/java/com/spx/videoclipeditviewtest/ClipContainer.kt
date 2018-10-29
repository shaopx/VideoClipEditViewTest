package com.spx.videoclipeditviewtest


import android.content.Context
import android.graphics.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.spx.videoclipeditviewtest.util.decodeFile
import kotlinx.android.synthetic.main.activity_video_clip.view.*

class ClipContainer : FrameLayout {
    lateinit var recyclerView: RecyclerView
    lateinit var shadowPaint: Paint
    var framebarHeight: Int = 0
    var recyclerViewPadding: Int = 0
    var itemCount: Int = 0
    var itemWidth: Int = 0
    var totalItemsWidth = 0
    var itemCountInFrame = 10
    var mediaDutaion = 0 // 媒体文件时长  ms
    var frameWidth = 900
    var realProgressBarWidth = 6
    var minProgressBarX = 120
    var maxProgressBarX = 900

    private var paint: Paint? = null
    private var progressPaint: Paint? = null
    lateinit var leftFrameBar: View
    lateinit var rightFrameBar: View
    lateinit var playProgressBar: View
    lateinit var leftFrameBarIv: View
    lateinit var rightFrameBarIv: View

    var list: MutableList<String?> = mutableListOf()


    var startMillSec: Float = 0f
    var endMillSec: Float = 0f
    var leftShadowStart = 0
    var leftShadowEnd = 0

    var rightShadowStart = 0
    var rightShadowEnd = 0

    private var leftFrameLeft = 0f
    private var rightFrameLeft = 0f

    private var progressStart = 0
    private var progressWidth = 10

    var framebarPadding = 80
    var framebarImageWidth = 42

    private var minDistance = 120f
    var millSecInFrame = maxSelection

    var callback: Callback? = null

    lateinit var adapter: MyAdapter


    private val LeftTouchListener = object : View.OnTouchListener {
        private var downX: Float = 0.toFloat()

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> downX = event.x
                MotionEvent.ACTION_MOVE -> {
                    val xDistance = event.x - downX
                    if (xDistance != 0f) {
                        var newTransx = v.translationX + xDistance
                        if (newTransx < 0) {
                            newTransx = 0f
                        }

                        if (newTransx + v.width > rightFrameLeft - minDistance) {
                            newTransx = rightFrameLeft - minDistance - v.width.toFloat()
                        }
                        v.translationX = newTransx
                        leftFrameLeft = newTransx + leftFrameBar.left
                        progressStart = (leftFrameLeft + v.width).toInt()
                        onFrameMoved(false)
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onFrameMoved(true)
                }
            }
            return false
        }

    }


    private val rightTouchListener = object : View.OnTouchListener {
        private var downX: Float = 0.toFloat()

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> downX = event.x
                MotionEvent.ACTION_MOVE -> {
                    val xDistance = event.x - downX
                    if (xDistance != 0f) {
                        var newTransx = v.translationX + xDistance
                        if (newTransx > 0) {
                            newTransx = 0f
                        }
                        if (width - v.width + newTransx < leftFrameLeft + leftFrameBar!!.width.toFloat() + minDistance) {
                            newTransx = -(width.toFloat() - (leftFrameLeft + leftFrameBar!!.width.toFloat() + minDistance) - v.width.toFloat())
                        }

                        v.translationX = newTransx
                        rightFrameLeft = v.left + newTransx
                        onFrameMoved(false)
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onFrameMoved(true)
                }
            }
            return false
        }

    }

    private val progressBarTouchListener = object : View.OnTouchListener {
        private var downX: Float = 0.toFloat()

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> downX = event.x
                MotionEvent.ACTION_MOVE -> {
                    val xDistance = event.x - downX
                    if (xDistance != 0f) {

                        var newTransx = v.translationX + xDistance
                        Log.d(TAG, "onTouch  xDistance:$xDistance, newTransx: $newTransx")
                        adjustProgressBar(v, newTransx)
                        onPreviewChange(false)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onPreviewChange(true)
                }
            }
            return false
        }

    }

    fun onPreviewChange(finished: Boolean) {
        var previewPosition = playProgressBar.translationX

        var previewMillSec = (previewPosition - getFrameFixLeftX()) * 1f / frameWidth * millSecInFrame

        if (mediaDutaion > maxSelection) {

            val (position, itemLeft, scrollX) = recyclerView.getScollXDistance()

            var scrollXTotal = scrollX + getFrameFixLeftX()


            var scrollMillSec = scrollXTotal * 1f / totalItemsWidth * mediaDutaion

            previewMillSec += scrollMillSec
        }




        if (callback != null) {
            callback!!.onPreviewChang(previewMillSec.toLong(), finished)
        }
        invalidate()
        return
    }

    fun adjustProgressBar(v: View, transX_: Float) {
        var transX = transX_

        if (transX + realProgressBarWidth > getCutRightX()) {
            transX = getCutRightX() - realProgressBarWidth
        }

        if (transX < getCutLeftX()) {
            transX = getCutLeftX()
        }

        if (transX < minProgressBarX) {
            transX = minProgressBarX.toFloat()
        }
//        Log.d(TAG, "adjustProgressBar  transX_:$transX_, transX: $transX")

        v.translationX = transX
    }


    interface Callback {
        fun onSelectionChang(totalCount: Int, startMillSec: Long, endMillSec: Long, finished: Boolean)
        fun onPreviewChang(startMillSec: Long, finished: Boolean)
    }


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    fun init(context: Context) {
        setWillNotDraw(false)
        shadowPaint = Paint()
        shadowPaint.color = context.resources.getColor(R.color.clip_shadow_color)
        shadowPaint.style = Paint.Style.FILL

        paint = Paint()
        paint!!.color = context.resources.getColor(R.color.frame_bar_color)
        paint!!.style = Paint.Style.FILL

        progressPaint = Paint()
        progressPaint!!.color = context.resources.getColor(R.color.video_clip_progress_color)
        progressPaint!!.style = Paint.Style.FILL

        shadowPaint = Paint()
        shadowPaint!!.color = context.resources.getColor(R.color.clip_shadow_color)
        shadowPaint!!.style = Paint.Style.FILL
        minDistance = context.resources.getDimensionPixelSize(R.dimen.video_clip_min_length).toFloat()
        progressWidth = context.resources.getDimensionPixelSize(R.dimen.video_clip_progressbar_width)

        with(context.resources) {
            recyclerViewPadding = getDimensionPixelSize(R.dimen.clip_recyclerview_paddingleft)
            framebarHeight = getDimensionPixelSize(R.dimen.clip_frame_bar_height)
            itemWidth = getDimensionPixelSize(R.dimen.clip_frame_item_width)
            framebarPadding = getDimensionPixelSize(R.dimen.clip_frame_bar_width_outer) - getDimensionPixelSize(R.dimen.clip_frame_bar_width)
            framebarImageWidth = getDimensionPixelSize(R.dimen.clip_frame_bar_width)
            realProgressBarWidth = getDimensionPixelSize(R.dimen.clip_frame_progressbar_width)
            minProgressBarX = getDimensionPixelSize(R.dimen.clip_recyclerview_paddingleft)
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView = findViewById(R.id.recyclerview)
//        clipFrameLayout = findViewById(R.id.clipContainer)

        leftFrameBar = findViewById(R.id.frame_left)
        rightFrameBar = findViewById(R.id.frame_right)
        playProgressBar = findViewById(R.id.clip_play_progress_ll)
        leftFrameBarIv = findViewById(R.id.frame_left_iv)
        rightFrameBarIv = findViewById(R.id.frame_right_iv)


        View.OnClickListener { }.run {
            leftFrameBar.setOnClickListener(this)
            rightFrameBar.setOnClickListener(this)
            playProgressBar.setOnClickListener(this)
        }



        leftFrameBar.setOnTouchListener(LeftTouchListener)
        rightFrameBar.setOnTouchListener(rightTouchListener)
        playProgressBar.setOnTouchListener(progressBarTouchListener)

        adapter = MyAdapter()
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.d(TAG, "onScrolled  dx:$dx, dy:$dy")
                if (dx != 0) {
                    clipContainer.updateSelection()
                }

            }
        })

    }


    fun updateInfo(mediaDutaion: Long, itemCount: Int) {
        this.itemCount = itemCount
        this.mediaDutaion = mediaDutaion.toInt()

        playProgressBar.visibility = View.VISIBLE

        if (rightFrameLeft == 0f) {
            initUiValues()
        }

        frameWidth = width - leftFrameBar!!.width - rightFrameBar!!.width

        itemWidth = (frameWidth * 1f / itemCountInFrame).toInt()
        totalItemsWidth = itemCount * itemWidth

        val selection = Math.min(maxSelection, mediaDutaion)

        minDistance = frameWidth * (minSelection * 1f / selection)

        millSecInFrame = if (mediaDutaion > maxSelection) {
            maxSelection
        } else {
            mediaDutaion
        }

        adapter.notifyDataSetChanged()

        adjustProgressBar(playProgressBar, playProgressBar.translationX)

        if (mediaDutaion > maxSelection) {

            rightShadowStart = (rightFrameLeft + framebarImageWidth).toInt() - SHADOW_DELTA
            rightShadowEnd = getFrameFixLeftX() + totalItemsWidth
            if (rightShadowEnd > width) {
                rightShadowEnd = width
            }
        }

        updateFramebarBg()

        invalidate()
    }

    private fun initUiValues() {
        rightFrameLeft = (width - rightFrameBar!!.width).toFloat()
        progressStart = (leftFrameLeft + leftFrameBar!!.width).toInt()

//        rightFrameLeft = rightFrameBar.translationX
        maxProgressBarX = width - resources.getDimensionPixelSize(R.dimen.clip_recyclerview_paddingleft)
        frameWidth = width - leftFrameBar!!.width - rightFrameBar!!.width
    }

    fun getCutLeftX(): Float {
        return leftFrameLeft + leftFrameBar.width
    }

    fun getCutRightX(): Float {
        return rightFrameLeft
    }

    fun getFrameFixLeftX() = leftFrameBar.width

    fun updateSelection() {
        onFrameMoved(true)
    }


    fun setProgress(currentPosition: Long, frozonTime:Long) {
//        Log.d(TAG, "setProgress: currentPosition:$currentPosition")
        if (mediaDutaion <= maxSelection) {
            val ratio = currentPosition * 1f / mediaDutaion
            progressStart = (getFrameFixLeftX() + ratio * frameWidth).toInt()
        } else {
            var millsecs = currentPosition - startMillSec
            if (millsecs < 0) {
                millsecs = 0f
            }
            if (millsecs > maxSelection) {
                millsecs = maxSelection.toFloat()
            }
            val ratio = millsecs * 1f / maxSelection
//            Log.d(TAG, "setProgress: millsecs:$millsecs, ratio:$ratio")
            progressStart = (getCutLeftX() + ratio * frameWidth).toInt()
        }

        if (progressStart < getCutLeftX()) {
            progressStart = getCutLeftX().toInt()
        }
        if (progressStart > getCutRightX()) {
            progressStart = getCutRightX().toInt()
        }
        adjustProgressBar(playProgressBar, progressStart.toFloat())

        invalidate()
    }


    private fun onFrameMoved(finished: Boolean) {

        adjustProgressBar(playProgressBar, playProgressBar.translationX)


        startMillSec = (getCutLeftX() - getFrameFixLeftX()) * 1f / frameWidth * millSecInFrame
        endMillSec = (getCutRightX() - getFrameFixLeftX()) * 1f / frameWidth * millSecInFrame

        if (mediaDutaion <= maxSelection) {

            leftShadowStart = getFrameFixLeftX()
            if (leftShadowStart < 0) {
                leftShadowStart = 0
            }
            leftShadowEnd = leftFrameLeft.toInt() + framebarPadding + SHADOW_DELTA
//            Log.d(TAG, "onFrameMoved: leftShadowStart:$leftShadowStart, leftShadowEnd:$leftShadowEnd")

            rightShadowStart = (rightFrameLeft + framebarImageWidth).toInt() - +SHADOW_DELTA
            rightShadowEnd = getFrameFixLeftX() + totalItemsWidth
            if (rightShadowEnd > width) {
                rightShadowEnd = width
            }
            updateFramebarBg()
            Log.d(TAG, "onFrameMoved: rightShadowStart:$rightShadowStart, rightShadowEnd:$rightShadowEnd")

            if (callback != null) {
                callback!!.onSelectionChang(itemCount, startMillSec.toLong(), endMillSec.toLong(), finished)
            }
            invalidate()
            return
        }

        val (position, itemLeft, scrollX) = recyclerView.getScollXDistance()
        Log.d(TAG, "onFrameMoved: position:$position, itemLeft:$itemLeft,  scrollX:$scrollX")

        var scrollXTotal = scrollX + getFrameFixLeftX()

        leftShadowStart = getFrameFixLeftX() - scrollXTotal
        if (leftShadowStart < 0) {
            leftShadowStart = 0
        }


        leftShadowEnd = leftFrameLeft.toInt() + framebarPadding + SHADOW_DELTA

        rightShadowStart = (rightFrameLeft + framebarImageWidth).toInt() - SHADOW_DELTA
        rightShadowEnd = getFrameFixLeftX() + totalItemsWidth - scrollXTotal
        if (rightShadowEnd > width) {
            rightShadowEnd = width
        }


        updateFramebarBg()

        Log.d(TAG, "onFrameMoved: rightShadowStart:$rightShadowStart, rightShadowEnd:$rightShadowEnd")
//        leftShadowStart = scrollXTotal

        var scrollMillSec = scrollXTotal * 1f / totalItemsWidth * mediaDutaion
//        Log.d(TAG, "onFrameMoved: totalItemsWidth:$totalItemsWidth, scrollXTotal:$scrollXTotal, scrollMillSec:$scrollMillSec")

        startMillSec += scrollMillSec
        endMillSec += scrollMillSec
//        Log.e(TAG, "onFrameMoved: final startMillSec:$startMillSec, endMillSec:$endMillSec, range:${endMillSec - startMillSec}")

        if (callback != null) {
            callback!!.onSelectionChang(itemCount, startMillSec.toLong(), endMillSec.toLong(), finished)
        }
        invalidate()
        return
    }

    fun updateFramebarBg(){
        if (rightShadowEnd > rightShadowStart) {
            rightFrameBarIv.setBackgroundResource(R.color.clip_shadow_color)
        } else {
            rightFrameBarIv.setBackgroundColor(Color.TRANSPARENT)
        }

        if (leftShadowEnd > leftShadowStart) {
            leftFrameBarIv.setBackgroundResource(R.color.clip_shadow_color)
        } else {
            leftFrameBarIv.setBackgroundColor(Color.TRANSPARENT)
        }
    }
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus && rightFrameLeft == 0f) {
            initUiValues()
        }
    }


    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        // 绘制阴影
        if (leftShadowEnd > leftShadowStart) {
            canvas.drawRect(Rect(leftShadowStart, 0, leftShadowEnd+2, height), shadowPaint)
        }

        if (rightShadowEnd > rightShadowStart) {
            canvas.drawRect(Rect(rightShadowStart-2, 0, rightShadowEnd, height), shadowPaint)
        }


        // 绘制上下边框矩形
        canvas.drawRect(Rect((leftFrameLeft + leftFrameBar.width).toInt(),
                0, (rightFrameLeft + DELTA).toInt(), framebarHeight), paint!!)
        canvas.drawRect(Rect((leftFrameLeft + leftFrameBar.width).toInt(),
                height - framebarHeight, (rightFrameLeft + DELTA).toInt(), height), paint!!)


    }


    fun updateBitmapList(toList: List<String>) {
        list.clear()
        list.addAll(toList)
        adapter?.notifyDataSetChanged()
    }

    fun addThumbnail(index: Int, bitmapPath: String) {
        list.set(index, bitmapPath)
//        Log.d(TAG, "addThumbnail  $index  size:${list.size}  bitmap.size:${bitmap.byteCount}, width:${bitmap.width}, height:${bitmap.height}")
        adapter.notifyDataSetChanged()
    }

    fun initRecyclerList(count: Int) {
        for (i in 0 until count) {
            list.add(null)
        }
    }


    companion object {

        private val TAG = "ClipContainer"
        val minSelection = 3000 // 最短3s
        val maxSelection: Long = 30000 // 最长30s
        private val DELTA = 6
        private val SHADOW_DELTA = 0
    }

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
            if (list[position] != null) {
                viewholder.image.setImageBitmap(decodeFile(list[position]!!))
            } else {
                viewholder.image.setImageResource(R.drawable.ic_launcher_background)
            }

        }

    }
}


fun RecyclerView.getScollXDistance(): Triple<Int, Int, Int> {
    var layoutManager = getLayoutManager() as LinearLayoutManager
    var position = layoutManager.findFirstVisibleItemPosition()
    var firstVisiableChildView = layoutManager.findViewByPosition(position)
    var itemwidth = firstVisiableChildView!!.width
    return Triple(position, -firstVisiableChildView.left, (position) * itemwidth - firstVisiableChildView.left)
}