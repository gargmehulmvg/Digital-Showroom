package com.digitaldukaan.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.MarketingSuggestedDomainItemResponse

class MarketingKnowMoreItemAdapter(
    private var mMarketingItemList: ArrayList<MarketingSuggestedDomainItemResponse?>?,
    private var mInterface: IAdapterItemClickListener?
) :
    RecyclerView.Adapter<MarketingKnowMoreItemAdapter.MarketingKnowMoreItemViewHolder>() {

    inner class MarketingKnowMoreItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val offerMessageTextView: TextView = itemView.findViewById(R.id.offerMessageTextView)
        val domainTextView: TextView = itemView.findViewById(R.id.domainTextView)
        val buyNowTextView: TextView = itemView.findViewById(R.id.buyNowTextView)
        val promoCodeTextView: TextView = itemView.findViewById(R.id.promoCodeTextView)
        val originalPriceTextView: TextView = itemView.findViewById(R.id.originalPriceTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingKnowMoreItemViewHolder {
        val view = MarketingKnowMoreItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.marketing_know_more_item, parent, false))
        view.buyNowTextView.setOnClickListener {
            val position = view.adapterPosition
            if (position < 0 || position >= (mMarketingItemList?.size ?: 0)) return@setOnClickListener
            mInterface?.onAdapterItemClickListener(position)
        }
        return view
    }

    override fun getItemCount(): Int = mMarketingItemList?.size ?: 0

    override fun onBindViewHolder(
        holder: MarketingKnowMoreItemViewHolder,
        position: Int
    ) {
        val item = mMarketingItemList?.get(position)
        holder.run {
            offerMessageTextView.text = item?.offerMessage
            domainTextView.text = item?.name
            promoCodeTextView.text = item?.promoCode
            messageTextView.text = item?.messageDomainOffer
            buyNowTextView.text = item?.cta?.text
            originalPriceTextView.text = item?.originalPrice
            priceTextView.text = item?.discountedPrice
            originalPriceTextView.paintFlags = (priceTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
            if (isNotEmpty(item?.offerTextColor)) {
                offerMessageTextView.setTextColor(Color.parseColor(item?.offerTextColor))
            }
            if (isNotEmpty(item?.offerBgColor)) {
                offerMessageTextView.setBackgroundColor(Color.parseColor(item?.offerBgColor))
            }
        }
    }

}