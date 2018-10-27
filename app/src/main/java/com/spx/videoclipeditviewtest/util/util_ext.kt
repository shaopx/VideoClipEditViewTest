package com.spx.videoclipeditviewtest.util

import android.content.ContentResolver
import android.content.Intent
import android.provider.MediaStore
import com.spx.videoclipeditviewtest.log

fun getVideoItem(contentResolver: ContentResolver, data: Intent):VideoItem?{
    var uri = data.getData()
    var cr = contentResolver
    var videoItem:VideoItem? = null
    /** 数据库查询操作。
     * 第一个参数 uri：为要查询的数据库+表的名称。
     * 第二个参数 projection ： 要查询的列。
     * 第三个参数 selection ： 查询的条件，相当于SQL where。
     * 第三个参数 selectionArgs ： 查询条件的参数，相当于 ？。
     * 第四个参数 sortOrder ： 结果排序。
     */
    var cursor = contentResolver.query(uri, null, null, null, null);
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            // 视频ID:MediaStore.Audio.Media._ID
            var videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
            // 视频名称：MediaStore.Audio.Media.TITLE
            var title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
            // 视频路径：MediaStore.Audio.Media.DATA
            var videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
            // 视频时长：MediaStore.Audio.Media.DURATION
            var duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
            // 视频大小：MediaStore.Audio.Media.SIZE
            var size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))

            // 视频缩略图路径：MediaStore.Images.Media.DATA
            var imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            // 缩略图ID:MediaStore.Audio.Media._ID
            var imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            // 方法一 Thumbnails 利用createVideoThumbnail 通过路径得到缩略图，保持为视频的默认比例
            // 第一个参数为 ContentResolver，第二个参数为视频缩略图ID， 第三个参数kind有两种为：MICRO_KIND和MINI_KIND 字面意思理解为微型和迷你两种缩略模式，前者分辨率更低一些。
            var bitmap1 = MediaStore.Video.Thumbnails.getThumbnail(cr, imageId.toLong(), MediaStore.Video.Thumbnails.MICRO_KIND, null);
            videoItem = VideoItem(title, videoPath, duration, size)

        }
        cursor.close()
    }

    return videoItem
}