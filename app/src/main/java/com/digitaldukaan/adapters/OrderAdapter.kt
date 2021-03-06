package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.getStringFromOrderDate
import com.digitaldukaan.interfaces.StickyHeaderInterface
import com.digitaldukaan.models.response.OrderItemResponse
import java.util.*


class OrderAdapter(
    private var mOrderList: ArrayList<OrderItemResponse>?,
    private var mOrderHeadersList: ArrayList<OrderItemResponse>?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderInterface {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (Constants.VIEW_TYPE_HEADER == viewType) {
            HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_date_sticky_header_layout, parent, false))
        } else {
            OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false))

        }
    }

    override fun getItemViewType(position: Int): Int {
        return mOrderList?.get(position)?.viewType ?: Constants.VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int = mOrderList?.size ?: 0

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (holder is HeaderViewHolder) holder.bindData(mOrderList?.get(position))
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var headerPosition = 0
        var itemPositionLocal = itemPosition
        do {
            if (isHeader(itemPositionLocal)) {
                headerPosition = itemPositionLocal
                break
            }
            itemPositionLocal -= 1
        } while (itemPositionLocal >= 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return R.layout.order_date_sticky_header_layout
    }

    override fun bindHeaderData(headerView: View?, headerPosition: Int) {
        headerView?.findViewById<TextView>(R.id.headerTitle)?.text = mOrderHeadersList?.get(headerPosition)?.updatedDate?.run { getStringFromOrderDate(this) }
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return Constants.VIEW_TYPE_HEADER == mOrderList?.get(itemPosition)?.viewType
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvHeader: TextView = itemView.findViewById(R.id.headerTitle)

        fun bindData(item: OrderItemResponse?) {
            tvHeader.text = item?.updatedDate?.run { getStringFromOrderDate(this) }
        }
    }

}