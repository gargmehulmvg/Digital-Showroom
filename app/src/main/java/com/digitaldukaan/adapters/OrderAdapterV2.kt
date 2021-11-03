package com.digitaldukaan.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
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
    private val mTag =  OrderAdapterV2::class.java.simpleName

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
            if (isEmpty(mOrderList)) return@setOnClickListener
            val position = view.adapterPosition
            if (position < 0 || position >= mOrderList?.size ?: 0) return@setOnClickListener
            mListItemListener?.onOrderCheckBoxChanged(view.orderCheckBox.isChecked, mOrderList?.get(position))
            view.orderCheckBox.isChecked = false
            view.orderCheckBox.isSelected = false
        }
        view.orderItemContainer.setOnClickListener {
            try {
                mListItemListener?.onOrderItemCLickChanged(mOrderList?.get(view.adapterPosition))
            } catch (e: Exception) {
                Log.e(mTag, "onCreateViewHolder: ${e.message}", e)
            }
        }
        return view
    }

    override fun getItemCount(): Int = mOrderList?.size ?:0

    override fun onBindViewHolder(holder: OrderAdapterV2.OrderViewHolder, position: Int) {
        val item = mOrderList?.get(position)
        holder.apply {
            val str = "#${item?.orderId}${if (mContext.getString(R.string.default_mobile) == item?.phone) "" else " | ${getNameFromContactList(item?.phone) ?: item?.phone}"}"
            orderDetailsTextView.text = str
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

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    private fun getOrderStatus(item: OrderItemResponse?, orderStatusTextView: TextView, orderItemContainer: View, orderCheckBox: CheckBox, orderStatusImageView: ImageView) {
        when (item?.displayStatus) {
            Constants.DS_NEW -> {
                orderStatusTextView.text = if (isEmpty(mOrderPageInfoStaticData?.newText)) mOrderPageInfoStaticData?.newText else "New"
                orderStatusTextView.setTextColor(mContext.getColor(R.color.open_green))
                orderStatusTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_new)
                orderCheckBox.isEnabled = false
                orderCheckBox.alpha = 0.2f
                orderStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                orderStatusImageView.visibility = View.GONE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_SEND_BILL -> {
                orderStatusTextView.setTextColor(mContext.getColor(R.color.orange))
                orderStatusTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_send_bill)
                Log.d(mTag, "getOrderStatus: ${mOrderPageInfoStaticData?.sendBillText}")
                orderStatusTextView.text = if (mOrderPageInfoStaticData?.sendBillText?.isEmpty() == true) mOrderPageInfoStaticData?.sendBillText else "Send Bill"
                orderCheckBox.isEnabled = true
                orderStatusImageView.visibility = View.GONE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_MARK_READY -> {
                orderStatusTextView.setTextColor(mContext.getColor(R.color.orange))
                orderStatusTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_send_bill)
                Log.d(mTag, "getOrderStatus: ${mOrderPageInfoStaticData?.markReadyText}")
                orderStatusTextView.text = if (mOrderPageInfoStaticData?.markReadyText?.isEmpty() == true) mOrderPageInfoStaticData?.markReadyText else "Mark Ready"
                orderCheckBox.isEnabled = true
                orderStatusImageView.visibility = View.GONE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_OUT_FOR_DELIVERY -> {
                orderStatusTextView.setTextColor(mContext.getColor(R.color.orange))
                orderStatusTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_send_bill)
                Log.d(mTag, "getOrderStatus: ${mOrderPageInfoStaticData?.outForDeliveryText}")
                orderStatusTextView.text = if (mOrderPageInfoStaticData?.outForDeliveryText?.isEmpty() == true) mOrderPageInfoStaticData?.outForDeliveryText else "Out For Delivery?"
                orderCheckBox.isEnabled = true
                orderStatusImageView.visibility = View.GONE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_BILL_SENT -> {
                orderStatusTextView.setTextColor(mContext.getColor(R.color.snack_bar_background))
                orderStatusTextView.background = ContextCompat.getDrawable(mContext, R.drawable.order_adapter_bill_sent)
                Log.d(mTag, "getOrderStatus: ${mOrderPageInfoStaticData?.sentBillText}")
                orderStatusTextView.text = if (mOrderPageInfoStaticData?.sentBillText?.isEmpty() == true) mOrderPageInfoStaticData?.sentBillText else "Bill Sent"
                orderCheckBox.isEnabled = true
                orderStatusImageView.visibility = View.GONE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_PENDING_PAYMENT_LINK -> {
                orderStatusTextView.background = null
                Log.d(mTag, "getOrderStatus: ${mOrderPageInfoStaticData?.payment_link}")
                orderStatusTextView.text = null
                orderCheckBox.isEnabled = true
                orderStatusImageView.visibility = View.GONE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_PAID_ONLINE -> {
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_order_status_paid))
                orderStatusTextView.text = null
                orderStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                orderStatusTextView.visibility = View.INVISIBLE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_REJECTED -> {
                orderStatusTextView.setTextColor(mContext.getColor(R.color.red))
                orderStatusTextView.text = if (mOrderPageInfoStaticData?.text_rejected?.isEmpty() == true) mOrderPageInfoStaticData?.text_rejected else "Rejected"
                Log.d(mTag, "getOrderStatus: ${mOrderPageInfoStaticData?.text_rejected}")
                orderStatusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reject_icon, 0, 0, 0)
                orderCheckBox.isEnabled = false
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderStatusImageView.visibility = View.GONE
                if (Constants.ORDER_TYPE_PREPAID == item.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                }
            }
            Constants.DS_COMPLETED_CASH -> {
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderCheckBox.isSelected = true
                orderCheckBox.isChecked = true
                orderCheckBox.isEnabled = false
                orderStatusTextView.text = null
                orderStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_order_status_cash))
            }
            Constants.DS_PREPAID_PICKUP_COMPLETED -> {
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderCheckBox.isSelected = true
                orderCheckBox.isChecked = true
                orderCheckBox.isEnabled = false
                orderStatusTextView.text = null
                orderStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
            }
            Constants.DS_PREPAID_DELIVERY_COMPLETED -> {
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderCheckBox.isSelected = true
                orderCheckBox.isChecked = true
                orderCheckBox.isEnabled = false
                orderStatusTextView.text = null
                orderStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
            }
            Constants.DS_COMPLETED_ONLINE -> {
                orderCheckBox.alpha = 0.2f
                orderItemContainer.alpha = 0.2f
                orderCheckBox.isSelected = true
                orderCheckBox.isChecked = true
                orderCheckBox.isEnabled = false
                orderStatusTextView.text = null
                orderStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                orderStatusImageView.visibility = View.VISIBLE
                orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_order_status_paid))
            }
            else -> {
                orderStatusTextView.text = null
                if (Constants.ORDER_TYPE_PREPAID == item?.prepaidFlag) {
                    orderStatusImageView.visibility = View.VISIBLE
                    orderStatusImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_prepaid))
                } else {
                    orderCheckBox.alpha = 0.2f
                    orderCheckBox.isEnabled = false
                    orderStatusImageView.visibility = View.GONE
                }
            }
        }
    }

    private fun getAddress(item: OrderItemResponse?): String {
        return when (item?.orderType) {
            Constants.ORDER_TYPE_ADDRESS -> "${item.deliveryInfo?.address1} ${item.deliveryInfo?.address2}"
            Constants.ORDER_TYPE_PICK_UP -> mOrderPageInfoStaticData?.pickUpOrder ?: "Pick up Order"
            Constants.ORDER_TYPE_SELF -> mOrderPageInfoStaticData?.selfBilled ?: "Self Billed"
            Constants.ORDER_TYPE_SELF_IMAGE -> mOrderPageInfoStaticData?.payment_link ?: "Payment Link"
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