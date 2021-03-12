package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.getStringFromOrderDate
import com.digitaldukaan.constants.getTimeFromOrderString
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.models.response.OrderItemResponse
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import java.util.*

class OrderAdapterV2(
    private var mContext: Context,
    private var mOrderList: ArrayList<OrderItemResponse>?,
    private var mOrderHeadersList: ArrayList<OrderItemResponse>?
) : RecyclerView.Adapter<OrderAdapterV2.OrderViewHolder>(), StickyRecyclerHeadersAdapter<OrderAdapterV2.HeaderViewHolder> {

    private val mOrderListStaticData = BaseFragment.mStaticData.mStaticData.mOrderListStaticData

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTitle)
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderCheckBox: CheckBox = itemView.findViewById(R.id.orderCheckBox)
        val orderAddressTextView: TextView = itemView.findViewById(R.id.orderAddressTextView)
        val orderDetailsTextView: TextView = itemView.findViewById(R.id.orderDetailsTextView)
        val orderStatusTextView: TextView = itemView.findViewById(R.id.orderStatusTextView)
        val orderTimeTextView: TextView = itemView.findViewById(R.id.orderTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mOrderList?.size ?:0
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = mOrderList?.get(position)
        holder.apply {
            orderDetailsTextView.text = "#${item?.orderId} | ${getNameFromContactList(item?.phone) ?: item?.phone}"
            orderTimeTextView.text = getTimeFromOrderString(item?.updatedCompleteDate)
            orderAddressTextView.text = getAddress(item)
            getOrderStatus(item, orderStatusTextView)
        }
    }

    override fun getHeaderId(position: Int): Long {
        val item = mOrderList?.get(position)
        return item?.updatedDate?.time ?: 0
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.order_date_sticky_header_layout, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, position: Int) {
        holder?.apply {
            val item = mOrderList?.get(position)
            headerTextView.text = item?.updatedDate?.run { getStringFromOrderDate(this) }
        }
    }

    private fun getOrderStatus(item: OrderItemResponse?, orderAddressTextView: TextView) {
        when (item?.displayStatus) {
            Constants.DS_NEW -> {
                orderAddressTextView.text = mOrderListStaticData.newText
                orderAddressTextView.setTextColor(mContext.getColor(R.color.open_green))
                orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_new)
            }
            Constants.DS_SEND_BILL -> {
                orderAddressTextView.setTextColor(mContext.getColor(R.color.orange))
                orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_send_bill)
                orderAddressTextView.text = mOrderListStaticData.sendBillText
            }
            Constants.DS_BILL_SENT -> {
                orderAddressTextView.setTextColor(mContext.getColor(R.color.snack_bar_background))
                orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_bill_sent)
                orderAddressTextView.text = mOrderListStaticData.sendBillText
            }
            else -> {}
        }
    }

    private fun getAddress(item: OrderItemResponse?): String {
        return when (item?.orderType) {
            Constants.ORDER_TYPE_ADDRESS -> "${item.deliveryInfo.address1} ${item.deliveryInfo.address2}"
            Constants.ORDER_TYPE_PICK_UP -> mOrderListStaticData.pickUpOrder ?: ""
            Constants.ORDER_TYPE_SELF -> mOrderListStaticData.selfBilled ?: ""
            else -> ""
        }
    }

    private fun getNameFromContactList(phoneNumber: String?): String? {
        StaticInstances.sUserContactList.forEachIndexed { _, contact ->
            if (contact.mobileNumber == phoneNumber) return contact.name
        }
        return null
    }

}