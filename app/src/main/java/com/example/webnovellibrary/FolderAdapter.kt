package com.example.webnovellibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(private val folders: MutableList<String>, val clickListener: OnClickListener) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onItemClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderName = itemView.findViewById(R.id.tv_folder_name) as TextView
        val linearLayout = itemView.findViewById(R.id.linear_layout) as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = folders[position]

        holder.linearLayout.setOnClickListener {
            clickListener.onItemClicked(position)
        }

        holder.folderName.text = element

    }

    override fun getItemCount(): Int {
        return folders.size
    }

}