package com.spx.library.player

import android.os.Handler

class VideoPlayTimeController(val player: VideoPlayer) {

  var handler: Handler = Handler()
  var startTime: Long = 0
  var endTime: Long = 0

  fun setPlayTimeRange(start: Long, end: Long) {
    startTime = start
    endTime = end
  }

  fun start() {
    handler.postDelayed({ check() }, 500)
  }

  fun check() {
    if (endTime == 0L || endTime < startTime) {
      endTime = player.getDuration().toLong()
    }

    if (player.getPlayerCurrentPosition() > endTime) {
      player.seekToPosition(startTime)
      player.startPlayer()
    } else if (player.getPlayerCurrentPosition() < startTime) {
      player.seekToPosition(startTime)
      player.startPlayer()
    }
    handler.postDelayed({ check() }, 16)
  }

  fun stop() {
    handler.removeCallbacksAndMessages(null)
  }
}