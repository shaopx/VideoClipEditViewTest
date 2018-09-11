package com.spx.videoclipeditviewtest

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class VH : RecyclerView.ViewHolder {
    var title: TextView
    var image: ImageView
    constructor(itemview: View) : super(itemview) {
        title = itemview.findViewById(R.id.title)
        image = itemview.findViewById(R.id.image)
    }
}


class MyAdapter(var list:MutableList<Bitmap>) : RecyclerView.Adapter<VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return VH(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(viewholder: VH, position: Int) {
        viewholder.title.setText("$position")
        viewholder.image.setImageBitmap(list[position])
    }

}