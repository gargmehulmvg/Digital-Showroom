package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.constants.setHtmlData
import com.digitaldukaan.models.response.MoreControlsInnerItemResponse
import com.digitaldukaan.models.response.MoreControlsStaticTextResponse
import com.digitaldukaan.services.serviceinterface.IMoreControlsItemClickListener

class MoreControlsInnerItemAdapter(
    private var mContext: Context?,
    private var mStoreControlItemsList: ArrayList<MoreControlsInnerItemResponse>?,
    private var mStoreControlStaticText: MoreControlsStaticTextResponse?,
    private var mListener: IMoreControlsItemClickListener
) : RecyclerView.Adapter<MoreControlsInnerItemAdapter.MoreControlsItemViewHolder>() {

    companion object {
        private const val SET_MIN_ORDER_VALUE       = "set-minimum-order-value"
        private const val SET_DELIVERY_CHARGE       = "set-delivery-charge"
        private const val SET_PREPAID_ORDERS        = "set-prepaid-orders"
        private const val SET_PAYMENT_MODE          = "set-payment-mode"
        private const val SET_ORDER_NOTIFICATION    = "set-order-notification"
        private const val CUSTOMER_ADDRESS_FIELD    = "edit-customer-address-fields"
    }

    inner class MoreControlsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lockedBackground: ImageView = itemView.findViewById(R.id.lockedBackground)
        val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)
        val unlockNowTextView: TextView = itemView.findViewById(R.id.unlockNowTextView)
        val valueTextView: TextView = itemView.findViewById(R.id.valueTextView)
        val newTextView: TextView = itemView.findViewById(R.id.newTextView)
        val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val paymentModeLockGroup: View = itemView.findViewById(R.id.paymentModeLockGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreControlsItemViewHolder {
        val view = MoreControlsItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.more_controls_inner_item, parent, false))
        view.container.setOnClickListener {
            if (view.adapterPosition < 0 && view.adapterPosition > (mStoreControlItemsList?.size ?: 0)) return@setOnClickListener
            val item = mStoreControlItemsList?.get(view.adapterPosition)
            if (true == item?.isClickable) {
                when(item.action) {
                    SET_MIN_ORDER_VALUE -> mListener.onMoreControlsEditMinOrderValueClicked(item)
                    SET_DELIVERY_CHARGE -> mListener.onMoreControlsSetDeliveryChargeClicked(item)
                    CUSTOMER_ADDRESS_FIELD -> mListener.onMoreControlsEditCustomerAddressFieldClicked(item)
                    SET_PREPAID_ORDERS -> mListener.onMoreControlsPrepaidOrderClicked(item)
                    SET_PAYMENT_MODE -> mListener.onMoreControlsPaymentModesClicked(item)
                    SET_ORDER_NOTIFICATION -> mListener.onMoreControlsOrderNotificationClicked(item)
                }
            }
        }
        return view
    }

    override fun getItemCount(): Int = mStoreControlItemsList?.size ?: 0

    override fun onBindViewHolder(holder: MoreControlsItemViewHolder, position: Int) {
        val item = mStoreControlItemsList?.get(position)
        holder.apply {
            headingTextView.text = item?.heading
            setHtmlData(valueTextView, if (isNotEmpty(item?.value)) item?.value else item?.subHeading)
            valueTextView.visibility = if (isEmpty(item?.value) && isEmpty(item?.subHeading)) View.GONE else View.VISIBLE
            newTextView.visibility = if (true == item?.isNew) {
                newTextView.text = mStoreControlStaticText?.text_new
                View.VISIBLE
            } else View.GONE
            mContext?.let { context ->
                when {
                    //SINGLE ITEM
                    1 == (mStoreControlItemsList?.size ?: 0) -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,15,5,15)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_slight_curve_grey_white_background)
                    }
                    //FIRST ITEM
                    0 == position -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,15,5,0)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_upper_curve_grey_white_background)
                    }
                    //LAST ITEM
                    ((mStoreControlItemsList?.size ?: 0) - 1) == position -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,1,5,15)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_lower_curve_grey_white_background)
                    }
                    //MIDDLE ITEM
                    else -> {
                        val param = container.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(5,1,5,0)
                        container.layoutParams = param
                        container.background = ContextCompat.getDrawable(context, R.drawable.ripple_rect_grey_white_background)
                    }
                }
                if (true == item?.isLocked) {
                    paymentModeLockGroup.visibility = View.VISIBLE
                    unlockNowTextView.text = mStoreControlStaticText?.text_unlock_now
                    when {
                        1 == (mStoreControlItemsList?.size ?: 0) -> {
                            lockedBackground.background = ContextCompat.getDrawable(context, R.drawable.curve_black_background_v2)
                            lockedBackground.alpha = 0.5f
                        }
                        0 == position -> {
                            lockedBackground.background = ContextCompat.getDrawable(context, R.drawable.upper_curve_black_background)
                            lockedBackground.alpha = 0.5f
                        }
                        ((mStoreControlItemsList?.size ?: 0) - 1) == position -> {
                            lockedBackground.background = ContextCompat.getDrawable(context, R.drawable.lower_curve_black_background)
                            lockedBackground.alpha = 0.5f
                        }
                        else -> {
                            lockedBackground.background = ContextCompat.getDrawable(context, R.drawable.rect_black_background)
                            lockedBackground.alpha = 0.5f
                        }
                    }
                } else paymentModeLockGroup.visibility = View.GONE
            }
        }
    }

}