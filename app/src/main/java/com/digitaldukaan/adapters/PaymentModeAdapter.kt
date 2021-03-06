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
import com.digitaldukaan.interfaces.IActiveOfferDetailsListener
import com.digitaldukaan.interfaces.ISwitchCheckChangeListener
import com.digitaldukaan.models.dto.PaymentModelDTO
import com.google.android.material.switchmaterial.SwitchMaterial

class PaymentModeAdapter(
    private var mContext: Context?,
    private var mIsKycActive: Boolean?,
    private var mPaymentOptionsList: ArrayList<PaymentModelDTO>?,
    private var mListener: ISwitchCheckChangeListener?,
    private var mActiveOfferListener : IActiveOfferDetailsListener
) :
    RecyclerView.Adapter<PaymentModeAdapter.PaymentModeViewHolder>() {

    inner class PaymentModeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentModeChildRecyclerView: RecyclerView? = itemView.findViewById(R.id.paymentModeChildRecyclerView)
        val paymentModeTextView: TextView? = itemView.findViewById(R.id.paymentModeTextView)
        val lockImageView: ImageView? = itemView.findViewById(R.id.lockImageView)
        val paymentModeSwitch: SwitchMaterial? = itemView.findViewById(R.id.paymentModeSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentModeViewHolder {
        val view = PaymentModeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_payment_mode_item, parent, false))
        view.paymentModeSwitch?.setOnClickListener {
            val item = mPaymentOptionsList?.get(view.adapterPosition)
            mListener?.onSwitchCheckChangeListener(view.paymentModeSwitch, view.paymentModeSwitch.isChecked, item?.value?.get(0)?.paymentType)
        }
        return view
    }

    override fun getItemCount(): Int = mPaymentOptionsList?.size ?: 0

    override fun onBindViewHolder(holder: PaymentModeViewHolder, position: Int) {
        holder.apply {
            val item = mPaymentOptionsList?.get(position)
            lockImageView?.visibility = if (mIsKycActive == true) View.GONE else View.VISIBLE
            paymentModeSwitch?.isChecked = (1 == item?.value?.get(0)?.status)
            paymentModeTextView?.text = item?.name
            paymentModeChildRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mContext)
                adapter = PaymentModeChildAdapter(mContext, mIsKycActive, item?.value, mActiveOfferListener)
            }
        }
    }

}