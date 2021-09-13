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
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.MarketingExpandableItemResponse

class MarketingMoreOptionsBottomSheetItemAdapter(
    private var mContext: BaseFragment?,
    private var mMarketingItemList: ArrayList<MarketingExpandableItemResponse?>?,
    private var mInterface: IAdapterItemClickListener?
) :
    RecyclerView.Adapter<MarketingMoreOptionsBottomSheetItemAdapter.MarketingMoreOptionsBottomSheetItemViewHolder>() {

    inner class MarketingMoreOptionsBottomSheetItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singleSpanContainer: View = itemView.findViewById(R.id.singleSpanContainer)
        val singleSpanBackgroundView: View = itemView.findViewById(R.id.singleSpanBackgroundView)
        val singleSpanImageView: ImageView = itemView.findViewById(R.id.singleSpanImageView)
        val arrowImageView: ImageView = itemView.findViewById(R.id.arrowImageView)
        val singleSpanTextView: TextView = itemView.findViewById(R.id.singleSpanTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketingMoreOptionsBottomSheetItemViewHolder {
        val view = MarketingMoreOptionsBottomSheetItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.marketing_more_options_bottom_sheet_item, parent, false))
        view.singleSpanContainer.setOnClickListener {
            val position = view.adapterPosition
            if (position < 0 || position >= (mMarketingItemList?.size ?: 0)) return@setOnClickListener
            val item = mMarketingItemList?.get(position)
            if (true == item?.is_clickable && item.is_enabled) mInterface?.onAdapterItemClickListener(position)
        }
        return view
    }

    override fun getItemCount(): Int = mMarketingItemList?.size ?: 0

    override fun onBindViewHolder(
        holder: MarketingMoreOptionsBottomSheetItemViewHolder,
        position: Int
    ) {
        val item = mMarketingItemList?.get(position)
        holder.run {
            singleSpanImageView.let {
                mContext?.let { context ->
                    Glide.with(context).load(item?.logo).into(it)
                }
            }
            arrowImageView.let {
                mContext?.let { context ->
                    Glide.with(context).load(item?.cta_cdn).into(it)
                }
            }
            singleSpanTextView.text = item?.heading
            if (isNotEmpty(item?.color)) {
                singleSpanBackgroundView.setBackgroundColor(Color.parseColor(item?.color))
            }
        }
    }

}