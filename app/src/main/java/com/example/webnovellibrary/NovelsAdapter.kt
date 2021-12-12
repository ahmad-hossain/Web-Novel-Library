package com.example.webnovellibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class NovelsAdapter(private val webNovels: List<WebNovel>, val clickListener: NovelsAdapter.OnClickListener) : RecyclerView.Adapter<NovelsAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onItemClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val webNovelName = itemView.findViewById(R.id.tv_webNovel_name) as TextView
        val webNovelUrl = itemView.findViewById(R.id.tv_webNovel_url) as TextView
        val cardView = itemView.findViewById(R.id.card_view) as CardView
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

        holder.webNovelName.text = element.title
        holder.webNovelUrl.text = element.url

    }

    override fun getItemCount(): Int {
        return webNovels.size
    }

}