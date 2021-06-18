package com.digitaldukaan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.interfaces.ISwitchCheckChangeListener
import com.digitaldukaan.models.dto.PaymentModelDTO
import com.google.android.material.switchmaterial.SwitchMaterial

class PaymentModeAdapter(
    private var mContext: Context?,
    private var mIsKycActive: Boolean?,
    private var mPaymentOptionsList: ArrayList<PaymentModelDTO>?,
    private var mListener: ISwitchCheckChangeListener?
) :
    RecyclerView.Adapter<PaymentModeAdapter.ReferAndEarnViewHolder>() {

    inner class ReferAndEarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentModeChildRecyclerView: RecyclerView? = itemView.findViewById(R.id.paymentModeChildRecyclerView)
        val paymentModeTextView: TextView? = itemView.findViewById(R.id.paymentModeTextView)
        val lockImageView: ImageView? = itemView.findViewById(R.id.lockImageView)
        val paymentModeSwitch: SwitchMaterial? = itemView.findViewById(R.id.paymentModeSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferAndEarnViewHolder {
        return ReferAndEarnViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_payment_mode_item, parent, false)
        )
    }

    override fun getItemCount(): Int = mPaymentOptionsList?.size ?: 0

    override fun onBindViewHolder(holder: ReferAndEarnViewHolder, position: Int) {
        holder.apply {
            val item = mPaymentOptionsList?.get(position)
            if (mIsKycActive == true) {
                lockImageView?.visibility = View.GONE
            } else {
                lockImageView?.visibility = View.VISIBLE
            }
            paymentModeSwitch?.isChecked = 1 == item?.value?.get(0)?.status
            paymentModeTextView?.text = item?.name
            paymentModeChildRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mContext)
                adapter = PaymentModeChildAdapter(mContext, mIsKycActive, item?.value)
            }
            paymentModeSwitch?.setOnClickListener {
                mListener?.onSwitchCheckChangeListener(paymentModeSwitch, paymentModeSwitch.isChecked, item?.value?.get(0)?.paymentType)
            }
        }
    }

}