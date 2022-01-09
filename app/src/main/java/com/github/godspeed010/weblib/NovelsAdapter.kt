package com.github.godspeed010.weblib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class NovelsAdapter(private var webNovels: List<WebNovel>, val clickListener: OnClickListener, val cardLayoutType: Int = 0) :
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
        val moreButton: ImageButton?

        init {

            webNovelName = itemView.findViewById(R.id.tv_webNovel_name)
            webNovelUrl = itemView.findViewById(R.id.tv_webNovel_url)
            cardView = itemView.findViewById(R.id.card_view)
            moreButton = itemView.findViewById(R.id.bt_more)

            copyButton = itemView.findViewById(R.id.bt_copy)

            cardView.setOnClickListener {
                clickListener.onItemClicked(adapterPosition)
            }
            copyButton.setOnClickListener {
                clickListener.onCopyClicked(adapterPosition)
            }

            //more button is only needed in cardLayout 0
            if (cardLayoutType == 0) {

                moreButton.setOnClickListener {
                    clickListener.onMoreClicked(adapterPosition)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //when cardLayoutType is set to anything but 0, different card layout will be used for search fragment
        val view = when (cardLayoutType) {
            0 -> LayoutInflater.from(parent.context)
                .inflate(R.layout.novel_item, parent, false)
            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.novel_item_search, parent, false)
        }

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

    fun filterList(filteredList: List<WebNovel>) {
        //set webNovels list to the filtered list of WebNovels
        webNovels = filteredList
        //notify adapter of the change
        notifyDataSetChanged()
    }

}