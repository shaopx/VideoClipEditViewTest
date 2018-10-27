package com.spx.videoclipeditviewtest

import android.app.Activity
import android.util.Log

fun Activity.log(message:String){
    Log.d(this.javaClass.simpleName, message)
}