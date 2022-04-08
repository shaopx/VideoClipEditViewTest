package com.spx.videoclipeditviewtest

class Config{
    companion object {
        const val SPEED_RANGE = 30
        //                val videoPlayUrl = "/storage/emulated/0/DCIM/Camera/VID_20180930_123107.mp4"
//        val videoPlayUrl = "/storage/emulated/0/download/VID_20181025.mp4"
        const val DEFAULT_TEMP_VIDEO_LOCATION = "/storage/emulated/0/movies/process.mp4"

        var MSG_UPDATE = 1
        val USE_EXOPLAYER = true

        // 对于长视频, 每隔3s截取一个缩略图
        val MAX_FRAME_INTERVAL_MS = 3 * 1000

        // 默认显示10个缩略图
        val DEFAULT_FRAME_COUNT = 10

        // 裁剪最小时间为3s
        val minSelection = 3000 // 最短3s

        // 裁剪最长时间为30s
        val maxSelection: Long = 30000 // 最长30s
    }
}