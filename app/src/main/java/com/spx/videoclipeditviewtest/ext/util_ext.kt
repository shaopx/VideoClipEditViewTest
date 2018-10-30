package com.spx.videoclipeditviewtest.ext

import android.content.Context

val spname:String = "litedo"

fun putInt(context: Context?, key: String, value: Int) {
    if (context == null) return
    val sp = context.getSharedPreferences(spname, Context.MODE_PRIVATE)
    val editor = sp.edit()
    editor.putInt(key, value)
    editor.commit()
}

fun getInt(context: Context?, key: String, defValue: Int): Int {
    if (context == null) return defValue
    val sp = context.getSharedPreferences(spname, Context.MODE_PRIVATE)
    return sp.getInt(key, defValue)
}