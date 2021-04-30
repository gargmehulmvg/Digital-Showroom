package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.getStringFromOrderDate
import com.digitaldukaan.constants.getTimeFromOrderString
import com.digitaldukaan.interfaces.IOrderListItemListener
import com.digitaldukaan.models.response.OrderItemResponse
import com.digitaldukaan.models.response.OrderPageStaticTextResponse
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import java.util.*

class OrderAdapterV2(
    private var mContext: Context,
    private var mOrderList: ArrayList<OrderItemResponse>?
) : RecyclerView.Adapter<OrderAdapterV2.OrderViewHolder>(), StickyRecyclerHeadersAdapter<OrderAdapterV2.HeaderViewHolder> {

    private var mListItemListener: IOrderListItemListener? = null
    private var mOrderPageInfoStaticData: OrderPageStaticTextResponse? = StaticInstances.sOrderPageInfoStaticData

    fun setCheckBoxListener(listener: IOrderListItemListener) {
        this.mListItemListener = listener
    }

    fun setOrderList(orderList: ArrayList<OrderItemResponse>?) {
        this.mOrderList = orderList
        this.notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTitle)
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderCheckBox: CheckBox = itemView.findViewById(R.id.orderCheckBox)
        val orderItemContainer: View = itemView.findViewById(R.id.orderItemContainer)
        val orderStatusImageView: ImageView = itemView.findViewById(R.id.orderStatusImageView)
        val orderAddressTextView: TextView = itemView.findViewById(R.id.orderAddressTextView)
        val orderDetailsTextView: TextView = itemView.findViewById(R.id.orderDetailsTextView)
        val orderStatusTextView: TextView = itemView.findViewById(R.id.orderStatusTextView)
        val orderTimeTextView: TextView = itemView.findViewById(R.id.orderTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = OrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false))
        view.orderCheckBox.setOnClickListener {
            mListItemListener?.onOrderCheckBoxChanged(view.orderCheckBox.isChecked, mOrderList?.get(view.adapterPosition))
            view.orderCheckBox.isChecked = false
            view.orderCheckBox.isSelected = false
        }
        view.orderItemContainer.setOnClickListener {
            mListItemListener?.onOrderItemCLickChanged(mOrderList?.get(view.adapterPosition))
        }
        return view
    }

    override fun getItemCount(): Int {
        return mOrderList?.size ?:0
    }

    override fun onBindViewHolder(holder: OrderAdapterV2.OrderViewHolder, position: Int) {
        val item = mOrderList?.get(position)
        holder.apply {
            orderDetailsTextView.text = "#${item?.orderId} | ${getNameFromContactList(item?.phone) ?: item?.phone}"
            orderTimeTextView.text = getTimeFromOrderString(item?.updatedCompleteDate)
            orderAddressTextView.text = getAddress(item)
            orderCheckBox.isSelected = false
            orderCheckBox.isChecked = false
            getOrderStatus(item, orderStatusTextView, orderItemContainer, orderCheckBox, orderStatusImageView)
        }
    }

    override fun getHeaderId(position: Int): Long {
        val item = mOrderList?.get(position)
        return item?.updatedDate?.time ?: 0
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): OrderAdapterV2.HeaderViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.order_date_sticky_header_layout, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, position: Int) {
        holder?.apply {
            val item = mOrderList?.get(position)
            headerTextView.text = item?.updatedDate?.run { getStringFromOrderDate(this) }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun getOrderStatus(item: OrderItemResponse?, orderAddressTextView: TextView, orderItemContainer: View, orderCheckBox: CheckBox, orderStatusImageView: ImageView) {
        when (item?.displayStatus) {
            Constants.DS_NEW -> {
                orderAddressTextView.text = mOrderPageInfoStaticData?.newText
                orderAddressTextView.setTextColor(mContext.getColor(R.color.open_green))
                orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_new)
                orderCheckBox.isEnabled = false
                orderCheckBox.alpha = 0.2f
                orderAddressTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                orderStatusImageView.visibility = View.GONE
            }
            Constants.DS_SEND_BILL -> {
                orderAddressTextView.setTextColor(mContext.getColor(R.color.orange))
                orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_send_bill)
                orderAddressTextView.text = mOrderPageInfoStaticData?.sendBillText
                orderCheckBox.isEnabled = true
                orderStatusImageView.visibility = View.GONE
            }
            Constants.DS_BILL_SENT -> {
                orderAddressTextView.setTextColor(mContext.getColor(R.color.snack_bar_background))
                orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_bill_sent)
                orderAddressTextView.text = mOrderPageInfoStaticData?.sentBillText
                orderCheckBox.isEnabled = true
                orderStatusImageView.visibility = View.GONE
            }
            Constants.DS_PAID_ONLINE -> {
                orderAddressTextView.text = null
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_order_status_paid))
            }
            Constants.DS_REJECTED -> {
                orderAddressTextView.setTextColor(mContext.getColor(R.color.red))
                orderAddressTextView.text = mOrderPageInfoStaticData?.text_rejected
                orderAddressTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reject_icon, 0, 0, 0)
                orderCheckBox.isEnabled = false
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderStatusImageView.visibility = View.GONE
            }
            Constants.DS_COMPLETED_CASH -> {
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderCheckBox.isSelected = true
                orderCheckBox.isChecked = true
                orderCheckBox.isEnabled = false
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_order_status_cash))
            }
            Constants.DS_COMPLETED_ONLINE -> {
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderCheckBox.isSelected = true
                orderCheckBox.isChecked = true
                orderCheckBox.isEnabled = false
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_order_status_paid))
            }
            else -> {
                orderCheckBox.alpha = 0.2f
                orderCheckBox.isEnabled = false
                orderStatusImageView.visibility = View.GONE
            }
        }
    }

    private fun getAddress(item: OrderItemResponse?): String {
        return when (item?.orderType) {
            Constants.ORDER_TYPE_ADDRESS -> "${item.deliveryInfo.address1} ${item.deliveryInfo.address2}"
            Constants.ORDER_TYPE_PICK_UP -> mOrderPageInfoStaticData?.pickUpOrder ?: ""
            Constants.ORDER_TYPE_SELF -> mOrderPageInfoStaticData?.selfBilled ?: ""
            Constants.ORDER_TYPE_SELF_V2 -> mOrderPageInfoStaticData?.selfBilled ?: ""
            else -> ""
        }
    }

    private fun getNameFromContactList(phoneNumber: String?): String? {
        StaticInstances.sUserContactList.forEachIndexed { _, contact ->
            if (contact.number == phoneNumber) return contact.name
        }
        return null
    }

}