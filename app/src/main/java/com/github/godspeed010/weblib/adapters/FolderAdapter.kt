package com.github.godspeed010.weblib.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.godspeed010.weblib.models.Folder
import com.github.godspeed010.weblib.R
import com.google.android.material.card.MaterialCardView
import java.util.*

class FolderAdapter(private val folders: MutableList<Folder>, val clickListener: OnClickListener) :
    RecyclerView.Adapter<FolderAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    interface OnClickListener {
        fun onItemClicked(position: Int)
        fun onMoreClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderName: TextView
        val cardView: MaterialCardView
        val moreButton: ImageButton

        init {
            folderName = itemView.findViewById(R.id.tv_folder_name)
            cardView = itemView.findViewById(R.id.card_view)
            moreButton = itemView.findViewById(R.id.bt_more)

            moreButton.setOnClickListener {
                clickListener.onMoreClicked(adapterPosition)
            }
            cardView.setOnClickListener {
                clickListener.onItemClicked(adapterPosition)
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

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(folders, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

}