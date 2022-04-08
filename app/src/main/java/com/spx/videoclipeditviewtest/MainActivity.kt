package com.spx.videoclipeditviewtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spx.library.VideoItem
import com.spx.library.getVideoItem
import com.spx.library.log

class MainActivity : AppCompatActivity() {

  companion object {
    const val PERMISSION_REQUEST_CODE = 1000
    const val REQUEST_PICK_CLIP_CODE = 1001
    const val REQUEST_PICK_EDIT_CODE = 1002
  }

  lateinit var tv_start_video_clip: TextView
  lateinit var tv_local_video_edit: TextView
  lateinit var tv_camera_preview: TextView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    tv_start_video_clip = findViewById(R.id.tv_start_video_clip)
    tv_local_video_edit = findViewById(R.id.tv_local_video_edit)
    tv_camera_preview = findViewById(R.id.tv_camera_preview)
    // 裁剪视频
    tv_start_video_clip.setOnClickListener { selectVideo(REQUEST_PICK_CLIP_CODE) }
    // 编辑视频(特效 滤镜)
    tv_local_video_edit.setOnClickListener { selectVideo(REQUEST_PICK_EDIT_CODE) }

    tv_camera_preview.setOnClickListener {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val intent = Intent(this, CameraEffectActivity::class.java)
        startActivity(intent)
      } else {
        Toast.makeText(
          this@MainActivity,
          "目前摄像头预览滤镜效果只支持L以上版本",
          Toast.LENGTH_LONG
        ).show()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    checkPermission()
  }

  /**
   * 从系统中选择视频
   */
  private fun selectVideo(requestCode: Int) {
    val intent = Intent(
      Intent.ACTION_PICK,
      android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    )
    startActivityForResult(intent, requestCode)
  }


  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (resultCode == RESULT_OK && data != null) {
      val videoItem = getVideoItem(contentResolver, data!!)
      videoItem?.run {
        log("video title:$title, duration:${durationSec}, size:$size, path:$path")
        when (requestCode) {
          REQUEST_PICK_CLIP_CODE -> startActivity(
            this,
            VideoClipActivity::class.java
          )
          REQUEST_PICK_EDIT_CODE -> startActivity(
            this,
            VideoEditActivity::class.java
          )
          else -> {
          }
        }

      }
    }

    super.onActivityResult(requestCode, resultCode, data)
  }

  private fun startActivity(videoItem: VideoItem, activityClass: Class<*>) {
    startActivity(Intent(this, activityClass).apply {
      videoItem?.run {
        putExtra("video_path", path)
        putExtra("video_duration", durationSec)
      }
    })
  }

  private fun checkPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true
    }
    // request permission if it has not been grunted.
    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        PERMISSION_REQUEST_CODE
      )
      return false
    }
      if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
          requestPermissions(
              arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
              PERMISSION_REQUEST_CODE
          )
          return false
      }

    return true
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(
          this@MainActivity,
          "permission has been grunted.",
          Toast.LENGTH_SHORT
        ).show()
      } else {
        Toast.makeText(
          this@MainActivity,
          "[WARN] permission is not grunted.",
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }
}
