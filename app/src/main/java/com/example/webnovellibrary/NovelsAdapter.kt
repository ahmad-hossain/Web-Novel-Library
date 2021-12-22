package com.example.webnovellibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class NovelsAdapter(private val webNovels: List<WebNovel>, val clickListener: OnClickListener) :
    RecyclerView.Adapter<NovelsAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    interface OnClickListener {
        fun onItemClicked(position: Int)
        fun onCopyClicked(position: Int)
        fun onMoreClicked(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val webNovelName: TextView
        val webNovelUrl: TextView
        val cardView: CardView

        val copyButton: ImageButton
        val moreButton: ImageButton

        init {
            webNovelName = itemView.findViewById(R.id.tv_webNovel_name)
            webNovelUrl = itemView.findViewById(R.id.tv_webNovel_url)
            cardView = itemView.findViewById(R.id.card_view)

            copyButton = itemView.findViewById(R.id.bt_copy)
            moreButton = itemView.findViewById(R.id.bt_more)

            cardView.setOnClickListener {
                clickListener.onItemClicked(adapterPosition)
            }
            copyButton.setOnClickListener {
                clickListener.onCopyClicked(adapterPosition)
            }
            moreButton.setOnClickListener {
                clickListener.onMoreClicked(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.novel_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = webNovels[position]

        holder.webNovelName.text = element.title
        holder.webNovelUrl.text = element.url

    }

    override fun getItemCount(): Int {
        return webNovels.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(webNovels, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

}