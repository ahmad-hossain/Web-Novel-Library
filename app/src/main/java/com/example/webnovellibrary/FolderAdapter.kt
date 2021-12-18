package com.example.webnovellibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(private val folders: MutableList<Folder>, val clickListener: OnClickListener, val longClickListener: OnLongClickListener) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onItemClicked(position: Int)
    }
    interface OnLongClickListener {
        fun onItemLongClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderName: TextView
        val linearLayout: LinearLayout

        init {
            folderName = itemView.findViewById(R.id.tv_folder_name)
            linearLayout = itemView.findViewById(R.id.linear_layout)

            linearLayout.setOnClickListener {
                clickListener.onItemClicked(adapterPosition)
            }
            linearLayout.setOnLongClickListener {
                longClickListener.onItemLongClicked(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = folders[position]

        holder.folderName.text = element.name

    }

    override fun getItemCount(): Int {
        return folders.size
    }

}