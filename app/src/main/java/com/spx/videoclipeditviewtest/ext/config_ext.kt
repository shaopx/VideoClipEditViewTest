package com.spx.videoclipeditviewtest.ext

import com.spx.videoclipeditviewtest.R
import com.spx.videoclipeditviewtest.view.BottomDialogFragment

fun createFilterOptions(): List<BottomDialogFragment.Option> {
    return arrayListOf(
            BottomDialogFragment.Option(R.drawable.ic_beauty_no, "无"),
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