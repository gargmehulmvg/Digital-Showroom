package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class LearnYoutubeAdapter(private var mItemList: ArrayList<String>?) : RecyclerView.Adapter<LearnYoutubeAdapter.LearnYoutubeViewHolder>() {

    inner class LearnYoutubeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val numberingTextView: TextView = itemView.findViewById(R.id.numberingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearnYoutubeViewHolder =
        LearnYoutubeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.youtube_item, parent, false))

    override fun getItemCount(): Int = mItemList?.size ?: 0

    override fun onBindViewHolder(holder: LearnYoutubeViewHolder, position: Int) {
        val number = "${position + 1}."
        holder.numberingTextView.text = "$number"
        holder.textView.text = mItemList?.get(position)
    }

}