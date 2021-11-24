package com.digitaldukaan.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.constants.startShinningAnimation
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface

class MarketingCardAdapter(
    private var mContext: BaseFragment?,
    private var mMarketingItemList: ArrayList<MarketingCardsItemResponse?>?,
    private var mMarketItemClickListener: IMarketingServiceInterface
) :
    RecyclerView.Adapter<MarketingCardAdapter.MarketingCardViewHolder>() {

    inner class MarketingCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val marketingCardParentContainer: View = itemView.findViewById(R.id.marketingCardParentContainer)
        val doubleSpanShimmerEffectView: View = itemView.findViewById(R.id.doubleSpanShimmerEffectView)
        val singleSpanContainer: View = itemView.findViewById(R.id.singleSpanContainer)
        val singleSpanBackgroundView: View = itemView.findViewById(R.id.singleSpanBackgroundView)
        val doubleSpanBackgroundImage: ImageView = itemView.findViewById(R.id.doubleSpanBackgroundImage)
        val doubleSpanBackgroundView: View = itemView.findViewById(R.id.doubleSpanBackgroundView)
        val singleSpanImageView: ImageView = itemView.findViewById(R.id.singleSpanImageView)
        val doubleSpanImageView: ImageView = itemView.findViewById(R.id.doubleSpanLeftImageView)
        val singleSpanInfoText: TextView = itemView.findViewById(R.id.singleSpanInfoText)
        val singleSpanTextView: TextView = itemView.findViewById(R.id.singleSpanTextView)
        val doubleSpanContinueTextView: TextView = itemView.findViewById(R.id.doubleSpanContinueTextView)
        val doubleSpanHeadingTextView: TextView = itemView.findViewById(R.id.doubleSpanHeadingTextView)
        val doubleSpanSubHeadingTextView: TextView = itemView.findViewById(R.id.doubleSpanSubHeadingTextView)
        val doubleSpanContainer: View = itemView.findViewById(R.id.doubleSpanContainer)
        val singleSpanLockNowTextView: TextView = itemView.findViewById(R.id.singleSpanLockNowTextView)
        val doubleSpanLockNowTextView: TextView = itemView.findViewById(R.id.doubleSpanLockNowTextView)
        val singleSpanLockGroup: Group = itemView.findViewById(R.id.singleSpanLockGroup)
        val doubleSpanLockGroup: Group = itemView.findViewById(R.id.doubleSpanLockGroup)
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
            when (item?.type) {
                Constants.SPAN_TYPE_FULL_WIDTH -> {
                    doubleSpanContainer.visibility = View.VISIBLE
                    singleSpanContainer.visibility = View.GONE
                    doubleSpanHeadingTextView.text = item.heading
                    doubleSpanSubHeadingTextView.text = item.subHeading
                    doubleSpanContinueTextView.text = item.marketingCta?.text
                    doubleSpanContinueTextView.setTextColor(Color.parseColor(item.marketingCta?.textColor))
                    doubleSpanContinueTextView.setBackgroundColor(Color.parseColor(item.marketingCta?.bgColor))
                    doubleSpanHeadingTextView.setTextColor(Color.parseColor(item.headingColor))
                    doubleSpanSubHeadingTextView.setTextColor(Color.parseColor(item.subHeadingColor))
                    doubleSpanImageView.let { mContext?.let { context -> Glide.with(context).load(item.logo).into(it) } }
                    if (isNotEmpty(item.background)) {
                        mContext?.let { context -> Glide.with(context).load(item.background).into(doubleSpanBackgroundImage) }
                    } else doubleSpanBackgroundView.setBackgroundColor(Color.parseColor(item.bgColor))
                    if (true == item.isShimmer) {
                        doubleSpanShimmerEffectView.visibility = View.VISIBLE
                        startShinningAnimation(doubleSpanShimmerEffectView)
                    } else doubleSpanShimmerEffectView.visibility = View.GONE
                    if (true == item.isStaffFeatureLocked) {
                        doubleSpanLockGroup.visibility = View.VISIBLE
                        doubleSpanContinueTextView.visibility = View.GONE
                        doubleSpanLockNowTextView.text = "Locked"
                    } else doubleSpanLockGroup.visibility = View.GONE
                }
                else -> {
                    singleSpanContainer.visibility = View.VISIBLE
                    doubleSpanContainer.visibility = View.GONE
                    singleSpanImageView.let { mContext?.let { context -> Glide.with(context).load(item?.logo).into(it) } }
                    singleSpanTextView.text = item?.heading
                    singleSpanInfoText.text = item?.infoText
                    if (isNotEmpty(item?.bgColor)) { singleSpanBackgroundView.setBackgroundColor(Color.parseColor(item?.bgColor)) }
                    if (isNotEmpty(item?.infoTextBgColor)) {
                        singleSpanInfoText.setBackgroundColor(Color.parseColor(item?.infoTextBgColor))
                    }
                    if (isNotEmpty(item?.infoTextColor)) {
                        singleSpanInfoText.visibility = View.VISIBLE
                        singleSpanInfoText.setTextColor(Color.parseColor(item?.infoTextColor))
                    } else {
                        singleSpanInfoText.visibility = View.GONE
                    }
                    if (true == item?.isStaffFeatureLocked) {
                        singleSpanLockGroup.visibility = View.VISIBLE
                        singleSpanInfoText.visibility = View.GONE
                        singleSpanLockNowTextView.text = "Locked"
                    } else singleSpanLockGroup.visibility = View.GONE
                }
            }
        }
    }

}