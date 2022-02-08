package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.getStringFromOrderDate
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.ILeadsListItemListener
import com.digitaldukaan.models.response.LeadsResponse
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import java.util.*

class LeadsAdapter(
    private var mContext: Context,
    private var mLeadsList: ArrayList<LeadsResponse>?,
    private var mListItemListener: ILeadsListItemListener?
) : RecyclerView.Adapter<LeadsAdapter.LeadsViewHolder>(), StickyRecyclerHeadersAdapter<LeadsAdapter.HeaderViewHolder> {

    fun setLeadsList(leadsList: ArrayList<LeadsResponse>?) {
        this.mLeadsList = leadsList
        this.notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTitle)
    }

    override fun getHeaderId(position: Int): Long {
        val item = mLeadsList?.get(position)
        return item?.updatedDate?.time ?: 0
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.order_date_sticky_header_layout, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, position: Int) {
        holder?.apply {
            val item = mLeadsList?.get(position)
            headerTextView.text = item?.updatedDate?.run { getStringFromOrderDate(this) }
        }
    }
    
    inner class LeadsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cartImageView: ImageView = itemView.findViewById(R.id.cartImageView)
        val leadItemContainer: View = itemView.findViewById(R.id.leadItemContainer)
        val leadDetailTextView: TextView = itemView.findViewById(R.id.leadDetailTextView)
        val leadLastUpdatedTextView: TextView = itemView.findViewById(R.id.leadLastUpdatedTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val cartStatusTextView: TextView = itemView.findViewById(R.id.cartStatusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadsViewHolder {
        val view = LeadsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.leads_item, parent, false))
        view.leadItemContainer.setOnClickListener {
            mListItemListener?.onLeadsItemCLickChanged(mLeadsList?.get(view.absoluteAdapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int = mLeadsList?.size ?:0

    override fun onBindViewHolder(holder: LeadsAdapter.LeadsViewHolder, position: Int) {
        val item = mLeadsList?.get(position)
        holder.apply {
            var displayStr: String = if (isEmpty(item?.customerName)) "${item?.phoneNumber}" else "${item?.phoneNumber} | ${item?.customerName}"
            leadDetailTextView.text = displayStr
            displayStr = "₹${item?.orderValue}"
            priceTextView.text = displayStr
            leadLastUpdatedTextView.text = item?.lastUpdateOn
            Glide.with(mContext)
                .load(if (Constants.CART_TYPE_ABANDONED == item?.cartType) R.drawable.ic_abandoned_cart else R.drawable.ic_active_cart)
                .into(cartImageView)
            cartStatusTextView.text = if (Constants.CART_TYPE_ABANDONED == item?.cartType) "Cart Abandoned" else "Cart Active"
            cartStatusTextView.background = ContextCompat.getDrawable(mContext, if (Constants.CART_TYPE_ABANDONED == item?.cartType) R.drawable.curve_red_cart_abandoned_background else R.drawable.curve_blue_cart_active_background)
            cartStatusTextView.setTextColor(ContextCompat.getColor(mContext, if (Constants.CART_TYPE_ABANDONED == item?.cartType) R.color.leads_cart_abandoned_text_color else R.color.leads_cart_active_text_color))
        }
    }
}