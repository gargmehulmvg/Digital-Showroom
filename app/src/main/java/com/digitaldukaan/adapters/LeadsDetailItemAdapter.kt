package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.models.response.LeadDetailStaticTextResponse
import com.digitaldukaan.models.response.OrderDetailItemResponse

class LeadsDetailItemAdapter(
    private var mContext: Context?,
    private var mOrderDetailStaticData: LeadDetailStaticTextResponse?,
    private var mOrderDetailList: ArrayList<OrderDetailItemResponse>?,
) : RecyclerView.Adapter<LeadsDetailItemAdapter.OrderDetailViewHolder>() {

    inner class OrderDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderDetailNameTextView: TextView = itemView.findViewById(R.id.abandonedCartDetailNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val orderDetailVariantNameTextView: TextView = itemView.findViewById(R.id.abandonedCartDetailVariantNameTextView)
        val orderDetailImageView: ImageView = itemView.findViewById(R.id.abandonedCartDetailImageView)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        return OrderDetailViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.abandoned_cart_detail_item, parent, false))
    }

    override fun getItemCount(): Int = mOrderDetailList?.size ?: 0

    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
        val item = mOrderDetailList?.get(position)
        holder.apply {
            mContext?.let { context ->
                if (isNotEmpty(item?.imageUrl)) {
                    orderDetailImageView.visibility = View.VISIBLE
                    Glide.with(context).load(item?.imageUrl).into(orderDetailImageView)
                } else orderDetailImageView.visibility = View.GONE
            }
            orderDetailNameTextView.text = item?.itemName
            if (Constants.ITEM_TYPE_LIST == item?.itemType || Constants.ITEM_TYPE_CATALOG == item?.itemType) {
                val quantityStr = "${mOrderDetailStaticData?.textQty}: ${item.itemQuantity}"
                quantityTextView.text = quantityStr
            }
            val priceStr = "${if (Constants.ITEM_TYPE_DISCOUNT == item?.itemType) "- " else ""}₹ ${item?.amount}"
            priceTextView.text = priceStr
            if (isNotEmpty(item?.variantName)) {
                orderDetailVariantNameTextView.visibility = View.VISIBLE
                orderDetailVariantNameTextView.text = item?.variantName
            } else orderDetailVariantNameTextView.visibility = View.GONE
        }
    }

}