package com.example.webnovellibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class NovelsAdapter(private val webNovels: List<WebNovel>, val clickListener: NovelsAdapter.OnClickListener) : RecyclerView.Adapter<NovelsAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onItemClicked(position: Int)
        fun onCopyClicked(position: Int)
        fun onEditClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val webNovelName = itemView.findViewById(R.id.tv_webNovel_name) as TextView
        val webNovelUrl = itemView.findViewById(R.id.tv_webNovel_url) as TextView
        val cardView = itemView.findViewById(R.id.card_view) as CardView

        val copyButton = itemView.findViewById(R.id.bt_copy) as ImageButton
        val editButton = itemView.findViewById(R.id.bt_edit) as ImageButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.novel_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = webNovels[position]

        holder.cardView.setOnClickListener {
            clickListener.onItemClicked(position)
        }
        holder.copyButton.setOnClickListener {
            clickListener.onCopyClicked(position)
        }
        holder.editButton.setOnClickListener {
            clickListener.onEditClicked(position)
        }


        holder.webNovelName.text = element.title
        holder.webNovelUrl.text = element.url

    }

    override fun getItemCount(): Int {
        return webNovels.size
    }

}