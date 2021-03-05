package com.digitaldukaan.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.squareup.picasso.Picasso

class MarketingCardAdapter(
    private var mMarketingItemList: ArrayList<MarketingCardsItemResponse>?
) :
    RecyclerView.Adapter<MarketingCardAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singleSpanContainer: View = itemView.findViewById(R.id.singleSpanContainer)
        val singleSpanBackgroundView: View = itemView.findViewById(R.id.singleSpanBackgroundView)
        val singleSpanImageView: ImageView = itemView.findViewById(R.id.singleSpanImageView)
        val doubleSpanImageView: ImageView = itemView.findViewById(R.id.doubleSpanImageView)
        val singleSpanTextView: TextView = itemView.findViewById(R.id.singleSpanTextView)
        val doubleSpanTextView: TextView = itemView.findViewById(R.id.doubleSpanHeadingTextView)
        val doubleSpanContinueTextView: TextView = itemView.findViewById(R.id.doubleSpanContinueTextView)
        val doubleSpanContainer: View = itemView.findViewById(R.id.doubleSpanContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        return MarketingCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.marketing_card_item, parent, false))
    }

    override fun getItemCount(): Int = mMarketingItemList?.size ?: 0

    override fun onBindViewHolder(
        holder: MarketingCardViewHolder,
        position: Int
    ) {
        val item = mMarketingItemList?.get(position)
        holder.run {
            if (item?.type == Constants.SPAN_TYPE_FULL_WIDTH) {
                doubleSpanContainer.visibility = View.VISIBLE
                Picasso.get().load(item.logo).into(doubleSpanImageView)
                doubleSpanTextView.text = item.text
                doubleSpanContinueTextView.text = item.viewNow
            } else {
                singleSpanContainer.visibility = View.VISIBLE
                Picasso.get().load(item?.logo).into(singleSpanImageView)
                singleSpanTextView.text = item?.heading
                singleSpanBackgroundView.setBackgroundColor(Color.parseColor(item?.color))
            }
        }
    }

}