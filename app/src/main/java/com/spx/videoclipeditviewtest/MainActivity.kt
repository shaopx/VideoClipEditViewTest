package com.spx.videoclipeditviewtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.spx.videoclipeditviewtest.util.VideoItem
import com.spx.videoclipeditviewtest.util.getVideoItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1000
        const val REQUEST_PICK_VIDEO_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_start_video_clip.setOnClickListener { selectVideo() }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    /**
     * 从系统中选择视频
     */
    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_PICK_VIDEO_CODE)
    }

    private fun startVideoClipActivity(videoItem: VideoItem) {
        startActivity(Intent(this, VideoClipActivity::class.java).apply {
            putExtra("video_path", videoItem.path)
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK_VIDEO_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val videoItem = getVideoItem(contentResolver, data!!)
                videoItem?.run {
                    log("video title:$title, duration:${durationSec}, size:$size, path:$path")
                    startVideoClipActivity(this)
                }

            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        // request permission if it has not been grunted.
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "permission has been grunted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "[WARN] permission is not grunted.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
