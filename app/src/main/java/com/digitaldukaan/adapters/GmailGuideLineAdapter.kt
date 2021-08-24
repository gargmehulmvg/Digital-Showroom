package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class GmailGuideLineAdapter(
    private var mList: ArrayList<String>?
) :
    RecyclerView.Adapter<GmailGuideLineAdapter.GmailGuideLineViewHolder>() {

    inner class GmailGuideLineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GmailGuideLineViewHolder {
        return GmailGuideLineViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.gmail_guideline_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mList?.size ?: 0

    override fun onBindViewHolder(
        holder: GmailGuideLineViewHolder,
        position: Int
    ) {
        holder.apply {
            textView.text = mList?.get(position)
        }
    }

}