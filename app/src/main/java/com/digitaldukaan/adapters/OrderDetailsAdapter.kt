package com.digitaldukaan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.models.response.OrderDetailItemResponse
import com.digitaldukaan.models.response.OrderDetailsStaticTextResponse

class OrderDetailsAdapter(
    private var mOrderDetailList: ArrayList<OrderDetailItemResponse>?,
    private val mDeliveryStatus: String?,
    private val mOrderDetailStaticData: OrderDetailsStaticTextResponse?,
    private val mListener: IChipItemClickListener?
) :
    RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailViewHolder>() {

    private val itemStatusActive = 1
    private val itemStatusRejected = 2

    fun setOrderDetailList(list: ArrayList<OrderDetailItemResponse>?) {
        mOrderDetailList = list
        notifyDataSetChanged()
    }

    inner class OrderDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderDetailNameTextView: TextView = itemView.findViewById(R.id.orderDetailNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val closeImageView: ImageView = itemView.findViewById(R.id.closeImageView)
        val orderDetailContainer: View = itemView.findViewById(R.id.orderDetailContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        val holder = OrderDetailViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.order_detail_item, parent, false)
        )
        holder.closeImageView.setOnClickListener { mListener?.onChipItemClickListener(holder.adapterPosition) }
        return holder
    }

    override fun getItemCount(): Int = mOrderDetailList?.size ?: 0

    override fun onBindViewHolder(
        holder: OrderDetailViewHolder,
        position: Int
    ) {
        val item = mOrderDetailList?.get(position)
        holder.apply {
            orderDetailNameTextView.text = item?.item_name
            quantityTextView.text = "${mOrderDetailStaticData?.text_quantity}: ${item?.item_quantity}"
            priceTextView.text = "${mOrderDetailStaticData?.text_rupees_symbol} ${item?.item_price}"
            if (mDeliveryStatus == Constants.DS_BILL_SENT) closeImageView.visibility = View.INVISIBLE
            if (item?.item_status == itemStatusRejected) {
                closeImageView.setImageResource(R.drawable.ic_undo)
                orderDetailContainer.alpha = 0.5f
            } else {
                closeImageView.setImageResource(R.drawable.ic_close_red)
                orderDetailContainer.alpha = 1f
            }
        }
    }

}