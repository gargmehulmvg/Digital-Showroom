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
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IActiveOfferDetailsListener
import com.digitaldukaan.models.response.PaymentModesItemResponse

class PaymentModeChildAdapter(
    private var mContext: Context?,
    private var mIsKycActive: Boolean?,
    private var mList: ArrayList<PaymentModesItemResponse>?,
    private var mListener: IActiveOfferDetailsListener?
) :
    RecyclerView.Adapter<PaymentModeChildAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headingTextView: TextView? = itemView.findViewById(R.id.headingTextView)
        val upiTxnChargeTextView: TextView? = itemView.findViewById(R.id.upiTxnChargeTextView)
        val upiImageView: ImageView = itemView.findViewById(R.id.upiImageView)
        val activeOfferTextView: TextView = itemView.findViewById(R.id.activeOfferTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_payment_mode_child_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mList?.size ?: 0

    override fun onBindViewHolder(holder: ReferAndEarnViewHolder, position: Int) {
        holder.apply {
            val item = mList?.get(position)
            headingTextView?.text = item?.name
            val txnChargeStr = "${item?.transactionCharges}% txn Charges"
            upiTxnChargeTextView?.text = txnChargeStr
            if (!isEmpty(item?.imageUrl)) mContext?.let { context -> Glide.with(context).load(item?.imageUrl).into(upiImageView) }
            if (true == mIsKycActive) {
                headingTextView?.alpha = 1f
                upiTxnChargeTextView?.alpha = 1f
            } else {
                headingTextView?.alpha = 0.5f
                upiTxnChargeTextView?.alpha = 0.5f
            }
            if(!isEmpty(item?.offerActiveText)){ activeOfferTextView.text = item?.offerActiveText }

            activeOfferTextView.setOnClickListener{
                item?.offerInfoMap?.let { it1 -> mListener?.activeOfferDetailsListener(it1) }
            }
        }
    }

}