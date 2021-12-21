package com.digitaldukaan.adapters

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
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IOrderDetailListener
import com.digitaldukaan.models.response.OrderDetailItemResponse
import com.digitaldukaan.models.response.OrderDetailsResponse
import com.digitaldukaan.models.response.OrderDetailsStaticTextResponse

class OrderDetailsAdapter(
    private var mOrderDetailList: ArrayList<OrderDetailItemResponse>?,
    private var orderDetailResponse: OrderDetailsResponse?,
    private val mDeliveryStatus: String?,
    private val mOrderDetailStaticData: OrderDetailsStaticTextResponse?,
    private val mListener: IOrderDetailListener?
) :
    RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailViewHolder>() {

    private val mTag = "OrderDetailsAdapter"
    private val itemStatusActive = 1
    private val itemStatusRejected = 2

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
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        val holder = OrderDetailViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.order_detail_item, parent, false)
        )
        holder.closeImageView.setOnClickListener { mListener?.onOrderDetailItemClickListener(holder.adapterPosition) }
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
            if (Constants.ITEM_TYPE_LIST == item?.item_type || Constants.ITEM_TYPE_CATALOG == item?.item_type) {
                val quantityStr = "${mOrderDetailStaticData?.text_quantity}: ${item.item_quantity}"
                quantityTextView.text = quantityStr
            }
            val priceStr = "${if (Constants.ITEM_TYPE_DISCOUNT == item?.item_type) "- " else ""}${mOrderDetailStaticData?.text_rupees_symbol} ${item?.amount}"
            priceTextView.text = priceStr
            val isShareBillUI = ((Constants.DS_SEND_BILL == mDeliveryStatus || Constants.DS_NEW == mDeliveryStatus) && (1 == orderDetailResponse?.paymentStatus) && (Constants.StatusSeenByMerchant == orderDetailResponse?.status || Constants.StatusMerchantUpdated == orderDetailResponse?.status) && isNotEmpty(orderDetailResponse?.deliveryInfo?.shipmentId))
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
            if (itemStatusRejected == item?.item_status) {
                closeImageView.setImageResource(R.drawable.ic_undo)
                orderDetailContainer.alpha = 0.5f
                if (View.VISIBLE == priceEditText.visibility) priceEditText.isEnabled = false
            } else {
                closeImageView.setImageResource(R.drawable.ic_close_red)
                orderDetailContainer.alpha = 1f
                if (View.VISIBLE == priceEditText.visibility) priceEditText.isEnabled = true
            }
            if (!isEmpty(item?.variantName)) {
                orderDetailVariantNameTextView.visibility = View.VISIBLE
                orderDetailVariantNameTextView.text = item?.variantName
            } else orderDetailVariantNameTextView.visibility = View.GONE
            if (View.VISIBLE == priceEditText.visibility) {
                priceEditText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Log.d(mTag, "afterTextChanged: ")
                        var str = editable?.toString()?.trim()
                        if (isEmpty(str)) {
                            item?.item_price = 0.0
                            item?.amount = 0.0
                            item?.actualAmount = 0.0
                            item?.discountedPrice = 0.0
                        } else {
                            item?.item_price = if (str == ".") 0.0 else {
                                if (str?.startsWith(".") == true) {
                                    str = "0$str"
                                    str.toDouble()
                                }
                                else str?.toDouble()
                            }
                            item?.amount = str?.toDouble()
                            item?.actualAmount = str?.toDouble()
                            item?.discountedPrice = str?.toDouble() ?: 0.0
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