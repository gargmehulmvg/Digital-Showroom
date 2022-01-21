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
import com.digitaldukaan.constants.isNotEmpty
import com.digitaldukaan.interfaces.IActiveOfferDetailsListener
import com.digitaldukaan.models.response.PaymentModesItemResponse

class PaymentModeChildAdapter(
    private var mContext: Context?,
    private var mIsKycActive: Boolean?,
    private var mList: ArrayList<PaymentModesItemResponse>?,
    private var mListener: IActiveOfferDetailsListener?
) :
    RecyclerView.Adapter<PaymentModeChildAdapter.PaymentModeChildViewHolder>() {

    inner class PaymentModeChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headingTextView: TextView? = itemView.findViewById(R.id.headingTextView)
        val upiTxnChargeTextView: TextView? = itemView.findViewById(R.id.upiTxnChargeTextView)
        val upiImageView: ImageView = itemView.findViewById(R.id.upiImageView)
        val activeOfferTextView: TextView = itemView.findViewById(R.id.activeOfferTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentModeChildViewHolder {
        val view = PaymentModeChildViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_payment_mode_child_item, parent, false))
        view.activeOfferTextView.setOnClickListener {
            val item = mList?.get(view.absoluteAdapterPosition)
            mListener?.onActiveOfferDetailsListener(item?.offerInfoList)
        }
        return view
    }

    override fun getItemCount(): Int = mList?.size ?: 0

    override fun onBindViewHolder(holder: PaymentModeChildViewHolder, position: Int) {
        holder.apply {
            val item = mList?.get(position)
            headingTextView?.text = item?.name
            val txnChargeStr = "${item?.transactionCharges}% txn Charges"
            upiTxnChargeTextView?.text = txnChargeStr
            if (isNotEmpty(item?.imageUrl)) mContext?.let { context -> Glide.with(context).load(item?.imageUrl).into(upiImageView) }
            headingTextView?.alpha = if (true == mIsKycActive) 1f else 0.5f
            upiTxnChargeTextView?.alpha = if (true == mIsKycActive) 1f else 0.5f
            if (isNotEmpty(item?.offerActiveText)) {
                activeOfferTextView.visibility = View.VISIBLE
                activeOfferTextView.text = item?.offerActiveText
            } else {
                activeOfferTextView.visibility = View.GONE
            }
        }
    }

}