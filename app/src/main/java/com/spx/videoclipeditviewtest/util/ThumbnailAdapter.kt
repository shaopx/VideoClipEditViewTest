package com.spx.videoclipeditviewtest.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.spx.library.decodeFile
import com.spx.videoclipeditviewtest.R

class VH : androidx.recyclerview.widget.RecyclerView.ViewHolder {
  var title: TextView
  var image: ImageView

  constructor(itemview: View) : super(itemview) {
    title = itemview.findViewById(R.id.title)
    image = itemview.findViewById(R.id.image)
  }
}

class ThumnaiAdapter(val list: MutableList<String?>, val itemWidth: Int) :
  androidx.recyclerview.widget.RecyclerView.Adapter<VH>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val v = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_layout, parent, false)
    return VH(v)
  }

  override fun getItemCount() = list.size

  override fun onBindViewHolder(viewholder: VH, position: Int) {
    val layoutParams = viewholder.itemView.layoutParams
    layoutParams.width = itemWidth
    viewholder.itemView.layoutParams = layoutParams
    if (!list[position].isNullOrEmpty()) {
      viewholder.image.setImageBitmap(decodeFile(list[position]!!))
    } else {
      viewholder.image.setImageResource(R.drawable.ic_launcher_background)
    }
  }
}