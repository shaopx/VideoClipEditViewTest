package com.spx.videoclipeditviewtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.spx.library.player.VideoPlayerOfExoPlayer
import com.spx.library.player.initPlayer
import kotlinx.android.synthetic.main.activity_video_edit.*

class VideoEditActivity : AppCompatActivity() {



    lateinit var exoPlayer:SimpleExoPlayer
    var listener: Player.DefaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(VideoPlayerOfExoPlayer.TAG, "player state $playbackState")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_edit)

        var mediaPath = intent.getStringExtra("video_path")

        exoPlayer = initPlayer(this,
                mediaPath, exo_playview!!, listener)
    }
}