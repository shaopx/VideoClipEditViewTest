package com.spx.videoclipeditviewtest.ext

import android.content.Context
import com.spx.egl.GLImageComplexionBeautyFilter
import com.daasuu.epf.custfilter.*
import com.daasuu.epf.filter.GlFilter
import com.spx.videoclipeditviewtest.R
import com.spx.videoclipeditviewtest.view.BottomDialogFragment

fun createFilterOptions(): List<BottomDialogFragment.Option> {
    return arrayListOf(
            BottomDialogFragment.Option(R.drawable.ic_beauty_no, "无"),
            BottomDialogFragment.Option(R.drawable.ic_beauty_white, "美颜"),
            BottomDialogFragment.Option(R.drawable.ic_beauty_white, "美白"),
            BottomDialogFragment.Option(R.drawable.ic_filter_langman, "浪漫"),
            BottomDialogFragment.Option(R.drawable.ic_filter_qinxin, "清新"),
            BottomDialogFragment.Option(R.drawable.ic_filter_weimei, "唯美"),
            BottomDialogFragment.Option(R.drawable.ic_filter_fennen, "粉嫩"),
            BottomDialogFragment.Option(R.drawable.ic_filter_huaijiu, "怀旧"),
            BottomDialogFragment.Option(R.drawable.ic_filter_landiao, "蓝调"),
            BottomDialogFragment.Option(R.drawable.ic_filter_qingliang, "清凉"),
            BottomDialogFragment.Option(R.drawable.ic_filter_rixi, "日系")
    )
}

fun getFilterByName(name: String, context: Context): GlFilter {
    return when {
        name.equals("无") -> GlFilter()
        name.equals("美颜") -> GLImageComplexionBeautyFilter(context)
        else -> GlPngFliter(context, getFilterPngByType(name))
    }
}

fun getFilterPngByType(type: String): String {
    return when (type) {
        "美白" -> "filter_white"
        "浪漫" -> "filter_langman"
        "清新" -> "filter_qingxin"
        "唯美" -> "filter_weimei"
        "粉嫩" -> "filter_fennen"
        "怀旧" -> "filter_huaijiu"
        "蓝调" -> "filter_landiao"
        "清凉" -> "filter_qingliang"
        "日系" -> "filter_rixi"
        else -> "filter_white"
    }
}

//-------------------------------------    #####特效 #######-----------------------
fun createEffectOptions(): List<BottomDialogFragment.Option> {
    return arrayListOf(
            BottomDialogFragment.Option(R.drawable.ic_beauty_no, "无特效"),
            BottomDialogFragment.Option(R.drawable.ic_filter_langman, "灵魂出窍"),
            BottomDialogFragment.Option(R.drawable.ic_filter_rixi, "幻觉"),
            BottomDialogFragment.Option(R.drawable.ic_filter_qingliang, "闪电"),
            BottomDialogFragment.Option(R.drawable.ic_filter_langman, "毛刺"),
            BottomDialogFragment.Option(R.drawable.ic_filter_langman, "缩放"),
            BottomDialogFragment.Option(R.drawable.ic_filter_langman, "抖动"),
            BottomDialogFragment.Option(R.drawable.ic_filter_langman, "四分镜")
//            BottomDialogFragment.Option(R.drawable.ic_beauty_white, "动感光波"),
//            BottomDialogFragment.Option(R.drawable.ic_beauty_white, "暗黑幻境"),
//            BottomDialogFragment.Option(R.drawable.ic_filter_qinxin, "画面分裂"),
//            BottomDialogFragment.Option(R.drawable.ic_filter_weimei, "百叶窗"),
//            BottomDialogFragment.Option(R.drawable.ic_filter_fennen, "鬼影"),
//            BottomDialogFragment.Option(R.drawable.ic_filter_huaijiu, "幻影"),
//            BottomDialogFragment.Option(R.drawable.ic_filter_landiao, "幽灵"),
//            BottomDialogFragment.Option(R.drawable.ic_filter_rixi, "镜像")

    )
}

fun getEffectFilterByName(name: String, context: Context): GlFilter {
    return when {
        name.equals("无特效") -> GlFilter()
        name.equals("缩放") -> GlScaleFilter(context)
        name.equals("抖动") -> GlShakeFilter(context)
        name.equals("四分镜") -> Gl4SplitFilter(context)
        name.equals("灵魂出窍") -> GlSoulOutFilter(context)
        name.equals("幻觉") -> GlHuanJueFliter(context)
        name.equals("闪电") -> GlFlashFliter(context)
        name.equals("毛刺") -> GlItchFilter(context)
        else -> {
            GLImageComplexionBeautyFilter(context)
        }
    }
}