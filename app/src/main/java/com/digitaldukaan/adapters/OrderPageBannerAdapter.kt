package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R

class OrderPageBannerAdapter : RecyclerView.Adapter<OrderPageBannerAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doubleSpanImageView: ImageView = itemView.findViewById(R.id.doubleSpanImageView)
        val doubleSpanTextView: TextView = itemView.findViewById(R.id.doubleSpanHeadingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        return MarketingCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_page_banner_item, parent, false))
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(
        holder: MarketingCardViewHolder,
        position: Int
    ) {
       holder.run {
       }
    }

}