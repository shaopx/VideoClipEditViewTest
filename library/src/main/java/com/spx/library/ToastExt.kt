package com.spx.library


import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast

var toast: Toast? = null

fun cancelToast() {
    toast?.run {
        cancel()
        toast = null
    }
}

/**
 * 通用的toast
 */
fun Context.showToast(desc: String) {

    toast = if (toast == null) {
        Toast(this)
    } else {
        toast!!.cancel()
        Toast(this)
    }

    val view = View.inflate(this, R.layout.baseres_toast_layout, null)

    val messageText = view.findViewById<TextView>(R.id.message)
//    ViewCompat.setElevation(messageText, 2.0f)
    if (!TextUtils.isEmpty(desc)) {
        messageText.text = desc
    }
    toast!!.duration = Toast.LENGTH_SHORT
    toast!!.view = view
    toast!!.setGravity(Gravity.TOP, 0, this.resources.displayMetrics.heightPixels / 2)
    toast!!.show()
}