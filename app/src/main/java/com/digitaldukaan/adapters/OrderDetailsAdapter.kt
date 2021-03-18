package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.models.response.OrderDetailItemResponse

class OrderDetailsAdapter(
    private var mOrderDetailList: ArrayList<OrderDetailItemResponse>?
) :
    RecyclerView.Adapter<OrderDetailsAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderDetailNameTextView: TextView = itemView.findViewById(R.id.orderDetailNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.order_detail_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mOrderDetailList?.size ?: 0

    override fun onBindViewHolder(
        holder: ReferAndEarnViewHolder,
        position: Int
    ) {
        val item = mOrderDetailList?.get(position)
        holder.apply {
            orderDetailNameTextView.text = item?.item_name
            quantityTextView.text = "Qty: ${item?.item_quantity}"
            priceTextView.text = "₹ ${item?.item_price}"
        }
    }

}