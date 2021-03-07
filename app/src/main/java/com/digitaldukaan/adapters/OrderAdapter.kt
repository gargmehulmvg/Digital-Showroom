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
import com.digitaldukaan.interfaces.StickyHeaderInterface
import com.digitaldukaan.models.response.OrderItemResponse
import java.util.*


class OrderAdapter(
    private var mContext: Context,
    private var mOrderList: ArrayList<OrderItemResponse>?,
    private var mOrderHeadersList: ArrayList<OrderItemResponse>?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderInterface {

    private val mOrderListStaticData = BaseFragment.mStaticData.mStaticData.mOrderListStaticData

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderCheckBox: CheckBox = itemView.findViewById(R.id.orderCheckBox)
        private val orderAddressTextView: TextView = itemView.findViewById(R.id.orderAddressTextView)
        private val orderDetailsTextView: TextView = itemView.findViewById(R.id.orderDetailsTextView)
        private val orderStatusTextView: TextView = itemView.findViewById(R.id.orderStatusTextView)
        private val orderTimeTextView: TextView = itemView.findViewById(R.id.orderTimeTextView)

        fun bindData(item: OrderItemResponse?) {
            orderDetailsTextView.text = "#${item?.orderId} | ${getNameFromContactList(item?.phone) ?: item?.phone}"
            orderTimeTextView.text = getTimeFromOrderString(item?.updatedCompleteDate)
            orderAddressTextView.text = getAddress(item)
            getOrderStatus(item, orderStatusTextView)
        }

        private fun getOrderStatus(item: OrderItemResponse?, orderAddressTextView: TextView) {
            when (item?.displayStatus) {
                Constants.DS_NEW -> {
                    orderAddressTextView.text = mOrderListStaticData.newText
                    orderAddressTextView.setTextColor(mContext.getColor(R.color.open_green))
                    orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.curve_green_border)
                }
                Constants.DS_SEND_BILL -> {
                    orderAddressTextView.setTextColor(mContext.getColor(R.color.orange))
                    orderAddressTextView.background = ContextCompat.getDrawable(mContext, R.drawable.curve_orange_border)
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
        if (holder is OrderViewHolder) holder.bindData(mOrderList?.get(position))
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

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        header?.findViewById<TextView>(R.id.headerTitle)?.text = mOrderHeadersList?.get(headerPosition)?.updatedDate?.run { getStringFromOrderDate(this) }
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