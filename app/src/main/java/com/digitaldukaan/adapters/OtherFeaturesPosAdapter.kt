package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class OtherFeaturesPosAdapter(private var mItemList: ArrayList<String>?) :
    RecyclerView.Adapter<OtherFeaturesPosAdapter.LearnYoutubeViewHolder>() {

    inner class LearnYoutubeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearnYoutubeViewHolder =
        LearnYoutubeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.other_feature_pos_item, parent, false))

    override fun getItemCount(): Int = mItemList?.size ?: 0

    override fun onBindViewHolder(holder: LearnYoutubeViewHolder, position: Int) {
        holder.textView.text = mItemList?.get(position)
    }

}