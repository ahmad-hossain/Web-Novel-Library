package com.github.godspeed010.weblib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoveNovelAdapter(private val folders: MutableList<String>, val clickListener: OnClickListener) :
    RecyclerView.Adapter<MoveNovelAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onItemClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderName: TextView
        val folderItem: LinearLayout

        init {
            folderName = itemView.findViewById(R.id.tv_folder_name)
            folderItem = itemView.findViewById(R.id.ll_folder)

            folderItem.setOnClickListener {
                clickListener.onItemClicked(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_item_simple, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = folders[position]

        holder.folderName.text = element
    }

    override fun getItemCount(): Int {
        return folders.size
    }

}