package com.digitaldukaan.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IOrderDetailListener
import com.digitaldukaan.models.response.OrderDetailItemResponse
import com.digitaldukaan.models.response.OrderDetailsResponse
import com.digitaldukaan.models.response.OrderDetailsStaticTextResponse

class OrderDetailsAdapter(
    private var mContext: Context?,
    private var mOrderDetailList: ArrayList<OrderDetailItemResponse>?,
    private var mOrderDetailResponse: OrderDetailsResponse?,
    private val mDeliveryStatus: String?,
    private val mOrderDetailStaticData: OrderDetailsStaticTextResponse?,
    private val mListener: IOrderDetailListener?
) : RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailViewHolder>() {

    companion object {
        private const val TAG = "OrderDetailsAdapter"
        private const val ITEM_STATUS_REJECTED = 2
    }

    fun setOrderDetailList(list: ArrayList<OrderDetailItemResponse>?) {
        mOrderDetailList = list
        notifyDataSetChanged()
    }

    inner class OrderDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderDetailNameTextView: TextView = itemView.findViewById(R.id.orderDetailNameTextView)
        val priceEditText: EditText = itemView.findViewById(R.id.priceEditText)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val orderDetailVariantNameTextView: TextView = itemView.findViewById(R.id.orderDetailVariantNameTextView)
        val closeImageView: ImageView = itemView.findViewById(R.id.closeImageView)
        val orderDetailContainer: View = itemView.findViewById(R.id.orderDetailContainer)
        val orderDetailImageView: ImageView = itemView.findViewById(R.id.orderDetailImageView)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        val holder = OrderDetailViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_detail_item, parent, false))
        holder.closeImageView.setOnClickListener { mListener?.onOrderDetailItemClickListener(holder.adapterPosition) }
        return holder
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
                val quantityStr = "${mOrderDetailStaticData?.text_quantity}: ${item.itemQuantity}"
                quantityTextView.text = quantityStr
            }
            val priceStr = "${if (Constants.ITEM_TYPE_DISCOUNT == item?.itemType) "- " else ""}${mOrderDetailStaticData?.text_rupees_symbol} ${item?.amount}"
            priceTextView.text = priceStr
            val isShareBillUI = ((Constants.DS_SEND_BILL == mDeliveryStatus || Constants.DS_NEW == mDeliveryStatus) && (1 == mOrderDetailResponse?.paymentStatus) && (Constants.StatusSeenByMerchant == mOrderDetailResponse?.status || Constants.StatusMerchantUpdated == mOrderDetailResponse?.status) && isNotEmpty(mOrderDetailResponse?.deliveryInfo?.shipmentId))
            closeImageView.visibility = if ((Constants.DS_SEND_BILL == mDeliveryStatus || Constants.DS_NEW == mDeliveryStatus) && !isShareBillUI) View.VISIBLE else View.GONE
            if (0.0 == item?.amount && (Constants.DS_NEW == mDeliveryStatus || Constants.DS_SEND_BILL == mDeliveryStatus)) {
                priceEditText.visibility = View.VISIBLE
                priceTextView.visibility = View.GONE
            } else {
                if (true == item?.isItemEditable) {
                    priceEditText.visibility = View.VISIBLE
                    priceTextView.visibility = View.GONE
                } else {
                    priceTextView.visibility = View.VISIBLE
                    priceEditText.visibility = View.GONE
                }
            }
            if (ITEM_STATUS_REJECTED == item?.itemStatus) {
                closeImageView.setImageResource(R.drawable.ic_undo)
                orderDetailContainer.alpha = 0.5f
                if (View.VISIBLE == priceEditText.visibility) priceEditText.isEnabled = false
            } else {
                closeImageView.setImageResource(R.drawable.ic_close_red)
                orderDetailContainer.alpha = 1f
                if (View.VISIBLE == priceEditText.visibility) priceEditText.isEnabled = true
            }
            if (isNotEmpty(item?.variantName)) {
                orderDetailVariantNameTextView.visibility = View.VISIBLE
                orderDetailVariantNameTextView.text = item?.variantName
            } else orderDetailVariantNameTextView.visibility = View.GONE
            if (View.VISIBLE == priceEditText.visibility) {
                priceEditText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Log.d(TAG, "afterTextChanged: ")
                        var str = editable?.toString()?.trim()
                        if (isEmpty(str)) {
                            with(0.0) {
                                item?.itemPrice = this
                                item?.amount = this
                                item?.actualAmount = this
                                item?.discountedPrice = this
                            }
                        } else {
                            item?.itemPrice = if ("." == str) 0.0 else {
                                if (true == str?.startsWith(".")) {
                                    str = "0$str"
                                    str.toDouble()
                                }
                                else str?.toDouble()
                            }
                            with(str?.toDouble() ?: 0.0) {
                                item?.amount = this
                                item?.actualAmount = this
                                item?.discountedPrice = this
                            }
                        }
                        mListener?.onOrderDetailListUpdateListener()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                })
            }
        }
    }

}