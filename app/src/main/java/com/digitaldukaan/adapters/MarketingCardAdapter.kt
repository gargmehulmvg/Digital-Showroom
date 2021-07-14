package com.digitaldukaan.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface

class MarketingCardAdapter(
    private var mContext: BaseFragment?,
    private var mMarketingItemList: ArrayList<MarketingCardsItemResponse>?,
    private var mMarketItemClickListener: IMarketingServiceInterface
) :
    RecyclerView.Adapter<MarketingCardAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val marketingCardParentContainer: View = itemView.findViewById(R.id.marketingCardParentContainer)
        val singleSpanContainer: View = itemView.findViewById(R.id.singleSpanContainer)
        val singleSpanBackgroundView: View = itemView.findViewById(R.id.singleSpanBackgroundView)
        val doubleSpanBackgroundView: View = itemView.findViewById(R.id.doubleSpanBackgroundView)
        val singleSpanImageView: ImageView = itemView.findViewById(R.id.singleSpanImageView)
        val doubleSpanImageView: ImageView = itemView.findViewById(R.id.doubleSpanImageView)
        val singleSpanVerticalImageView: ImageView = itemView.findViewById(R.id.singleSpanVerticalImageView)
        val stripTextView: TextView = itemView.findViewById(R.id.stripTextView)
        val singleSpanTextView: TextView = itemView.findViewById(R.id.singleSpanTextView)
        val singleSpanVerticalTextView: TextView = itemView.findViewById(R.id.singleSpanVerticalTextView)
        val singleSpanVerticalContinueTextView: TextView = itemView.findViewById(R.id.singleSpanVerticalContinueTextView)
        val doubleSpanTextView: TextView = itemView.findViewById(R.id.doubleSpanHeadingTextView)
        val doubleSpanContinueTextView: TextView = itemView.findViewById(R.id.doubleSpanContinueTextView)
        val doubleSpanContainer: View = itemView.findViewById(R.id.doubleSpanContainer)
        val singleSpanVerticalBackgroundView: View = itemView.findViewById(R.id.singleSpanVerticalBackgroundView)
        val singleSpanVerticalContainer: View = itemView.findViewById(R.id.singleSpanVerticalContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingCardViewHolder {
        val view = MarketingCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.marketing_card_item, parent, false))
        view.marketingCardParentContainer.setOnClickListener { mMarketItemClickListener.onMarketingItemClick(mMarketingItemList?.get(view.adapterPosition)) }
        return view
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
                doubleSpanImageView?.let { mContext?.let { context -> Glide.with(context).load(item.logo).into(it) } }
                doubleSpanTextView.text = item.text
                doubleSpanContinueTextView.text = item.viewNow
                doubleSpanBackgroundView.setBackgroundColor(Color.parseColor(item?.color))
                if (item.stripText?.isNotEmpty() == true) {
                    stripTextView.visibility = View.VISIBLE
                    stripTextView.text = item.stripText
                } else stripTextView.visibility = View.GONE
            } else if (item?.type == Constants.SPAN_TYPE_FULL_HEIGHT) {
                singleSpanVerticalContainer.visibility = View.VISIBLE
                singleSpanVerticalBackgroundView.setBackgroundColor(Color.parseColor(item.color))
                singleSpanVerticalImageView?.let { mContext?.let { context -> Glide.with(context).load(item.logo).into(it) } }
                singleSpanVerticalTextView.text = item.text
                singleSpanVerticalContinueTextView.text = item.viewNow
            } else {
                singleSpanContainer.visibility = View.VISIBLE
                singleSpanImageView?.let { mContext?.let { context -> Glide.with(context).load(item?.logo).into(it) } }
                singleSpanTextView.text = item?.heading
                singleSpanBackgroundView.setBackgroundColor(Color.parseColor(item?.color))
            }
        }
    }

}